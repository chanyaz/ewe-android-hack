package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.ConfirmationState.Type;
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

public class UserReviewsListActivity extends SherlockFragmentActivity implements UserReviewsFragmentListener,
		TabListener, OnPageChangeListener {

	private static final long RESUME_TIMEOUT = 1000 * 60 * 20; // 20 minutes
	private long mLastResumeTime = -1;

	// Download keys
	private static final String REVIEWS_STATISTICS_DOWNLOAD = UserReviewsListActivity.class.getName() + ".stats";

	// Instance variable names
	private static final String INSTANCE_VIEWED_REVIEWS = "INSTANCE_VIEWED_REVIEWS";

	// Fragments and Views
	private ViewPager mViewPager;
	private UserReviewsFragmentPagerAdapter mPagerAdapter;

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();

	private Set<String> mViewedReviews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (checkFinishConditionsAndFinish())
			return;

		if (savedInstanceState != null) {
			mViewedReviews = new HashSet<String>(savedInstanceState.getStringArrayList(INSTANCE_VIEWED_REVIEWS));
		}
		else {
			mViewedReviews = new HashSet<String>();
		}

		setContentView(R.layout.activity_user_reviews);

		initializePager(savedInstanceState);
		initializeActionBar();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkFinishConditionsAndFinish())
			return;

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

	private boolean checkFinishConditionsAndFinish() {
		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return true;
		}
		// Haxxy fix for #13798, only required on pre-Honeycomb
		if (ConfirmationState.hasSavedData(this, Type.HOTEL)) {
			finish();
			return true;
		}

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return true;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();

		return false;
	}

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_hotel_details, menu);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		Button tv = (Button) getLayoutInflater().inflate(R.layout.actionbar_select_hotel, null);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(select);
			}
		});
		select.setActionView(tv);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home: {
			Intent intent = new Intent(this, HotelDetailsFragmentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Codes.OPENED_FROM_WIDGET, getIntent().getBooleanExtra(Codes.OPENED_FROM_WIDGET, false));
			startActivity(intent);
			finish();
			return true;
		}
		case R.id.menu_select_hotel: {
			Intent intent = new Intent(this, RoomsAndRatesListActivity.class);
			startActivity(intent);
		}
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
			ExpediaServices expediaServices = new ExpediaServices(UserReviewsListActivity.this);
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

					populateReviewsStats();
				}
			}
		}
	};

	/**
	 * Call this method once the review statistics call has been successfully completed.
	 * 
	 * Note, if it is ever the case that E3 fixes the review count to accurately reflect the actual total,
	 * this method could included as part of the initial onCreate setup, no extra network call required
	 */
	private void populateReviewsStats() {

		mPagerAdapter.populateReviewsStats();

		View titleView = getSupportActionBar().getCustomView();
		if (titleView == null) {
			return;
		}

		TextView titleTextView = (TextView) titleView.findViewById(R.id.title);
		RatingBar ratingBar = (RatingBar) titleView.findViewById(R.id.user_rating);

		ReviewsStatisticsResponse stats = Db.getSelectedReviewsStatisticsResponse();
		if (stats == null) {
			if (ratingBar != null) {
				ratingBar.setVisibility(View.GONE);
			}
			return;
		}

		if (titleTextView != null) {
			String title = getResources().getQuantityString(R.plurals.number_of_reviews,
					stats.getTotalReviewCount(), stats.getTotalReviewCount());
			titleTextView.setText(title);
		}

		if (ratingBar != null) {
			float rating = stats.getAverageOverallRating();
			ratingBar.setRating(rating);
			ratingBar.setVisibility(View.VISIBLE);
		}
	}

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

	private void initializeActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setCustomView(getLayoutInflater().inflate(R.layout.actionbar_reviews, null));

		Tab recentTab = actionBar.newTab().setText(R.string.user_review_sort_button_recent);
		recentTab.setTabListener(this);
		recentTab.setTag(0);
		actionBar.addTab(recentTab);

		Tab favorableTab = actionBar.newTab().setText(R.string.user_review_sort_button_favorable);
		favorableTab.setTabListener(this);
		favorableTab.setTag(1);
		actionBar.addTab(favorableTab);

		Tab criticalTab = actionBar.newTab().setText(R.string.user_review_sort_button_critical);
		criticalTab.setTabListener(this);
		criticalTab.setTag(2);
		actionBar.addTab(criticalTab);
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
	// FragmentViewPager

	private void initializePager(Bundle savedInstanceState) {
		mPagerAdapter = new UserReviewsFragmentPagerAdapter(getSupportFragmentManager(), savedInstanceState);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ActionBar.TabListener

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int index = (Integer) tab.getTag();

		if (mViewPager.getCurrentItem() != index) {
			mViewPager.setCurrentItem(index);
		}

		String referrerId = null;
		if (index == 0) {
			referrerId = "App.Hotels.Reviews.Sort.Recent";
		}
		else if (index == 1) {
			referrerId = "App.Hotels.Reviews.Sort.Favorable";
		}
		else if (index == 2) {
			referrerId = "App.Hotels.Reviews.Sort.Critical";
		}
		Log.d("Tracking \"App.Hotels.Reviews\" pageLoad");
		TrackingUtils.trackSimpleEvent(this, "App.Hotels.Reviews", null, "Shopper", referrerId);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// nothing to do here
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// nothing to do here
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
		ActionBar actionBar = getSupportActionBar();
		Tab tab = actionBar.getTabAt(position);
		if (tab != actionBar.getSelectedTab()) {
			tab.select();
		}
	}
}
