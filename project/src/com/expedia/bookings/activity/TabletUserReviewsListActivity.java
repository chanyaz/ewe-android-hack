package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.expedia.bookings.widget.UserReviewsFragmentPagerAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.widget.SegmentedControlGroup;

public class TabletUserReviewsListActivity extends FragmentActivity implements UserReviewsFragmentListener,
		OnPageChangeListener, OnCheckedChangeListener {

	// Download keys
	private static final String REVIEWS_STATISTICS_DOWNLOAD = TabletUserReviewsListActivity.class.getName() + ".stats";

	// Instance variable names
	private static final String INSTANCE_VIEWED_REVIEWS = "INSTANCE_VIEWED_REVIEWS";

	// Fragments and Views
	private ViewPager mViewPager;
	private UserReviewsFragmentPagerAdapter mPagerAdapter;

	// Tabs
	private SegmentedControlGroup mSortGroup;

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();

	private Set<String> mViewedReviews;

	// Gesture Detector
	private View mRootView;
	private GestureDetector mDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		mRootView = findViewById(R.id.user_reviews_view);
		mDetector = new GestureDetector(this, new CloseGestureDetector());

		if (savedInstanceState != null) {
			mViewedReviews = new HashSet<String>(savedInstanceState.getStringArrayList(INSTANCE_VIEWED_REVIEWS));
		}
		else {
			mViewedReviews = new HashSet<String>();
		}

		setContentView(R.layout.activity_user_reviews);

		initializePager(savedInstanceState);
		initializeTabs();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Start the download of the user reviews statistics (if needed)
		/*if (Db.getSelectedReviewsStatisticsResponse() != null) {
			populateReviewsStats();
		}
		else*/if (mBackgroundDownloader.isDownloading(REVIEWS_STATISTICS_DOWNLOAD)) {
			mBackgroundDownloader.registerDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD,
					mReviewStatisticsDownloadCallback);
		}
		else {
			mBackgroundDownloader.startDownload(REVIEWS_STATISTICS_DOWNLOAD, mReviewStatisticsDownload,
					mReviewStatisticsDownloadCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBackgroundDownloader.unregisterDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			UserReviewsUtils.getInstance().clearCache();

			// Track # of reviews seen
			int numReviewsSeen = mViewedReviews.size();
			Log.d("Tracking # of reviews seen: " + numReviewsSeen);
			String referrerId = "App.Hotels.Reviews." + numReviewsSeen + "ReviewsViewed";
			TrackingUtils.trackSimpleEvent(this, null, null, "Shopper", referrerId);

			// cancel all downloads
			mBackgroundDownloader.cancelDownload(REVIEWS_STATISTICS_DOWNLOAD);
			mPagerAdapter.cancelDownloads();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ArrayList<String> viewedReviews = new ArrayList<String>(mViewedReviews);
		outState.putStringArrayList(INSTANCE_VIEWED_REVIEWS, viewedReviews);

		mPagerAdapter.onSaveInstanceState(getSupportFragmentManager(), outState);
	}

	private final Download<ReviewsStatisticsResponse> mReviewStatisticsDownload = new Download<ReviewsStatisticsResponse>() {
		@Override
		public ReviewsStatisticsResponse doDownload() {
			ExpediaServices expediaServices = new ExpediaServices(TabletUserReviewsListActivity.this);
			mBackgroundDownloader.addDownloadListener(REVIEWS_STATISTICS_DOWNLOAD, expediaServices);
			return expediaServices.reviewsStatistics(Db.getSelectedProperty());
		}
	};

	private final OnDownloadComplete<ReviewsStatisticsResponse> mReviewStatisticsDownloadCallback = new OnDownloadComplete<ReviewsStatisticsResponse>() {
		@Override
		public void onDownload(ReviewsStatisticsResponse response) {
			if (response == null) {
				showReviewsUnavailableError();
			}
			else {
				if (response.hasErrors()) {
					showReviewsUnavailableError();
				}
				else {
					Db.addReviewsStatisticsResponse(response);
					// grab the stats

					mPagerAdapter.populateReviewsStats();
				}
			}
		}
	};

	private void showReviewsUnavailableError() {
		TextView emptyTextView = Ui.findView(this, R.id.empty_text_view);

		if (emptyTextView != null) {
			String text = getResources().getString(R.string.user_review_unavailable);
			emptyTextView.setText(text);
		}

		ProgressBar progressBar = Ui.findView(this, R.id.progress_bar);

		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FragmentListener

	/**
	 * This override is part of the FragmentListener implementation. The fragment will invoke this callback
	 * once it has completed downloading a set of reviews. This method determines the logic for "chaining" of
	 * reviews, that is download the first page of all three sets of reviews automatically, one after the other
	 * such that no two downloads are happening at once
	 */
	@Override
	public void onDownloadComplete(UserReviewsFragment fragmentDone) {
		mPagerAdapter.attemptNextDownload(fragmentDone);
	}

	/**
	 * This override is used to track the number of reviews seen
	 */
	@Override
	public void addMoreReviewsSeen(Set<String> reviews) {
		mViewedReviews.addAll(reviews);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Tabs

	private void initializeTabs() {
		mSortGroup = Ui.findView(this, R.id.user_review_sort_group);
		mSortGroup.setOnCheckedChangeListener(this);

		int position = mViewPager.getCurrentItem();
		((RadioButton) mSortGroup.getChildAt(position)).setChecked(true);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		String referrerId = null;
		int position = 0;
		switch (checkedId) {
		case R.id.user_review_button_recent:
			referrerId = "App.Hotels.Reviews.Sort.Recent";
			position = 0;
			break;
		case R.id.user_review_button_favorable:
			referrerId = "App.Hotels.Reviews.Sort.Favorable";
			position = 1;
			break;
		case R.id.user_review_button_critical:
			referrerId = "App.Hotels.Reviews.Sort.Critical";
			position = 2;
			break;
		}

		Log.d("Tracking \"App.Hotels.Reviews\" pageLoad");
		TrackingUtils.trackSimpleEvent(this, "App.Hotels.Reviews", null, "Shopper", referrerId);

		mViewPager.setCurrentItem(position);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FragmentViewPager

	private void initializePager(Bundle savedInstanceState) {
		mPagerAdapter = new UserReviewsFragmentPagerAdapter(getSupportFragmentManager(), savedInstanceState);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OnPageChangeListener

	@Override
	public void onPageScrollStateChanged(int state) {
		// nothing to do here
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// nothing to do here
	}

	@Override
	public void onPageSelected(int position) {
		((RadioButton) mSortGroup.getChildAt(position)).setChecked(true);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Gesture Detector

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mDetector.onTouchEvent(ev);

		return super.dispatchTouchEvent(ev);
	}

	private class CloseGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			if (!isFinishing() && mRootView != null) {
				Rect bounds = new Rect();
				mRootView.getHitRect(bounds);

				if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
					finish();
				}
			}

			return true;
		}
	}
}
