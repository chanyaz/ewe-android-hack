package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.widget.SegmentedControlGroup;

public class UserReviewsListActivity extends FragmentActivity implements UserReviewsFragmentListener {

	// Download keys
	public static final String REVIEWS_STATISTICS_DOWNLOAD = "com.expedia.bookings.activity.UserReviewsActivity.ReviewsStatisticsDownload";

	// Member variables
	private Context mContext;
	public Property mProperty;

	// Fragments and Views
	private UserReviewsFragment mRecentReviewsFragment;
	private UserReviewsFragment mFavorableReviewsFragment;
	private UserReviewsFragment mCriticalReviewsFragment;

	private Map<ReviewSort, UserReviewsFragment> mReviewSortFragmentMap;

	// Instance variable names
	private static final String INSTANCE_RECENT_REVIEWS_FRAGMENT = "INSTANCE_RECENT_REVIEWS_FRAGMENT";
	private static final String INSTANCE_FAVORABLE_REVIEWS_FRAGMENT = "INSTANCE_FAVORABLE_REVIEWS_FRAGMENT";
	private static final String INSTANCE_CRITICAL_REVIEWS_FRAGMENT = "INSTANCE_CRITICAL_REVIEWS_FRAGMENT";

	private static final String INSTANCE_HAS_REVIEW_STATS = "INSTANCE_HAS_REVIEW_STATS";
	private static final String INSTANCE_TOTAL_REVIEW_COUNT = "INSTANCE_TOTAL_REVIEW_COUNT";
	private static final String INSTANCE_RECOMMENDED_REVIEW_COUNT = "INSTANCE_RECOMMENDED_REVIEW_COUNT";
	private static final String INSTANCE_AVERAGE_OVERALL_RATING = "INSTANCE_AVERAGE_OVERALL_RATING";
	private static final String INSTANCE_VIEWED_REVIEWS = "INSTANCE_VIEWED_REVIEWS";

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();

	// Stores the counts retrieved from BazaarVoice using FilteredStats param
	private boolean mHasReviewStats = false;
	private int mTotalReviewCount;
	private int mRecommendedReviewCount;
	private float mAverageOverallRating;

	// Tab stuff
	private ReviewSort mCurrentReviewSort;
	private SegmentedControlGroup mSortGroup;

	private Set<String> mViewedReviews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.activity_user_reviews);
		
		mProperty = Db.getSelectedProperty();

		if (savedInstanceState != null) {
			mRecentReviewsFragment = Ui.findSupportFragment(this, INSTANCE_RECENT_REVIEWS_FRAGMENT);
			mFavorableReviewsFragment = Ui.findSupportFragment(this, INSTANCE_FAVORABLE_REVIEWS_FRAGMENT);
			mCriticalReviewsFragment = Ui.findSupportFragment(this, INSTANCE_CRITICAL_REVIEWS_FRAGMENT);

			mHasReviewStats = savedInstanceState.getBoolean(INSTANCE_HAS_REVIEW_STATS);
			mTotalReviewCount = savedInstanceState.getInt(INSTANCE_TOTAL_REVIEW_COUNT);
			mRecommendedReviewCount = savedInstanceState.getInt(INSTANCE_RECOMMENDED_REVIEW_COUNT);
			mAverageOverallRating = savedInstanceState.getFloat(INSTANCE_AVERAGE_OVERALL_RATING);
			mViewedReviews = new HashSet<String>(savedInstanceState.getStringArrayList(INSTANCE_VIEWED_REVIEWS));
		}
		else {
			// add the user reviews list fragment to the framelayout container
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			mRecentReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.NEWEST_REVIEW_FIRST);
			mFavorableReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.HIGHEST_RATING_FIRST);
			mCriticalReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.LOWEST_RATING_FIRST);

			ft.add(R.id.user_review_content_container, mRecentReviewsFragment, INSTANCE_RECENT_REVIEWS_FRAGMENT);
			ft.add(R.id.user_review_content_container, mFavorableReviewsFragment, INSTANCE_FAVORABLE_REVIEWS_FRAGMENT);
			ft.add(R.id.user_review_content_container, mCriticalReviewsFragment, INSTANCE_CRITICAL_REVIEWS_FRAGMENT);
			ft.commit();

			mViewedReviews = new HashSet<String>();

			// start the download of the user reviews statistics
			mBackgroundDownloader.startDownload(REVIEWS_STATISTICS_DOWNLOAD, mReviewStatisticsDownload,
					mReviewStatisticsDownloadCallback);
		}

		mReviewSortFragmentMap = new HashMap<ReviewSort, UserReviewsFragment>();
		mReviewSortFragmentMap.put(ReviewSort.NEWEST_REVIEW_FIRST, mRecentReviewsFragment);
		mReviewSortFragmentMap.put(ReviewSort.HIGHEST_RATING_FIRST, mFavorableReviewsFragment);
		mReviewSortFragmentMap.put(ReviewSort.LOWEST_RATING_FIRST, mCriticalReviewsFragment);

		mSortGroup = Ui.findView(this, R.id.user_review_sort_group);
		mSortGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				String referrerId = null;
				switch (checkedId) {
				case R.id.user_review_button_recent:
					mCurrentReviewSort = ReviewSort.NEWEST_REVIEW_FIRST;
					referrerId = "App.Hotels.Reviews.Sort.Recent";
					break;
				case R.id.user_review_button_favorable:
					mCurrentReviewSort = ReviewSort.HIGHEST_RATING_FIRST;
					referrerId = "App.Hotels.Reviews.Sort.Favorable";
					break;
				case R.id.user_review_button_critical:
					mCurrentReviewSort = ReviewSort.LOWEST_RATING_FIRST;
					referrerId = "App.Hotels.Reviews.Sort.Critical";
					break;
				}

				Log.d("Tracking \"App.Hotels.Reviews\" pageLoad");
				TrackingUtils.trackSimpleEvent(mContext, "App.Hotels.Reviews", null, "Shopper", referrerId);

				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				for (ReviewSort sort : mReviewSortFragmentMap.keySet()) {
					if (sort == mCurrentReviewSort) {
						ft.show(mReviewSortFragmentMap.get(sort));
					}
					else {
						ft.hide(mReviewSortFragmentMap.get(sort));
					}
				}
				ft.commit();
			}
		});

		RadioButton btn;
		if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
			btn = Ui.findView(this, R.id.user_review_button_favorable);
		}
		else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST) {
			btn = Ui.findView(this, R.id.user_review_button_critical);
		}
		else {
			btn = Ui.findView(this, R.id.user_review_button_recent);
		}

		btn.setChecked(true);

		if (mHasReviewStats) {
			populateBottomBar();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mBackgroundDownloader.isDownloading(REVIEWS_STATISTICS_DOWNLOAD)) {
			mBackgroundDownloader.registerDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD,
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

			for (ReviewSort sort : mReviewSortFragmentMap.keySet()) {
				mReviewSortFragmentMap.get(sort).cancelReviewsDownload();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// store the primitives which need to be saved on configuration changes
		outState.putBoolean(INSTANCE_HAS_REVIEW_STATS, mHasReviewStats);
		outState.putInt(INSTANCE_TOTAL_REVIEW_COUNT, mTotalReviewCount);
		outState.putInt(INSTANCE_RECOMMENDED_REVIEW_COUNT, mRecommendedReviewCount);
		outState.putFloat(INSTANCE_AVERAGE_OVERALL_RATING, mAverageOverallRating);

		ArrayList<String> viewedReviews = new ArrayList<String>(mViewedReviews);
		outState.putStringArrayList(INSTANCE_VIEWED_REVIEWS, viewedReviews);
	}

	private Download mReviewStatisticsDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices expediaServices = new ExpediaServices(mContext);
			mBackgroundDownloader.addDownloadListener(REVIEWS_STATISTICS_DOWNLOAD, expediaServices);
			return expediaServices.reviewsStatistics(mProperty);
		}
	};

	private OnDownloadComplete mReviewStatisticsDownloadCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			ReviewsStatisticsResponse response = (ReviewsStatisticsResponse) results;

			if (response == null) {
				showReviewsUnavailableError();
			}
			else {
				if (response.hasErrors()) {
					showReviewsUnavailableError();
				}
				else {
					// grab the stats
					mRecommendedReviewCount = response.getRecommendedCount();
					mTotalReviewCount = response.getTotalReviewCount();
					mAverageOverallRating = response.getAverageOverallRating();
					mHasReviewStats = true;

					populateBottomBar();

					mReviewSortFragmentMap.get(mCurrentReviewSort).startReviewsDownload();

					// populate the list header for all three fragments
					for (ReviewSort sort : mReviewSortFragmentMap.keySet()) {
						mReviewSortFragmentMap.get(sort).populateListHeader(mRecommendedReviewCount, mTotalReviewCount);
					}
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
	private void populateBottomBar() {
		View bottomBar = Ui.findView(this, R.id.bottom_bar);
		if (bottomBar != null) {
			// Configure the book now button
			TextView bookNowButton = Ui.findView(this, R.id.book_now_button);
			bookNowButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent newIntent = new Intent(mContext, RoomsAndRatesListActivity.class);
					newIntent.fillIn(getIntent(), 0);
					startActivity(newIntent);
				}
			});

			if (mHasReviewStats) {
				TextView totalReviews = Ui.findView(this, R.id.user_review_total_reviews);
				totalReviews.setText(getResources().getQuantityString(R.plurals.number_of_reviews, mTotalReviewCount,
						mTotalReviewCount));

				RatingBar bottomRatingBar = Ui.findView(this, R.id.user_review_rating_bar_bottom);
				bottomRatingBar.setRating(mAverageOverallRating);
				bottomRatingBar.setVisibility(View.VISIBLE);
			}
			else {
				RatingBar bottomRatingBar = Ui.findView(this, R.id.user_review_rating_bar_bottom);
				bottomRatingBar.setVisibility(View.GONE);
			}

			bottomBar.setVisibility(View.VISIBLE);
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

	/**
	 * This override is part of the FragmentListener implementation. The fragment will invoke this callback
	 * once it has completed downloading a set of reviews. This method determines the logic for "chaining" of
	 * reviews, that is download the first page of all three sets of reviews automatically, one after the other
	 * such that no two downloads are happening at once
	 */
	@Override
	public void onDownloadComplete(ReviewSort completedSort) {
		UserReviewsFragment fragment;
		if (completedSort == ReviewSort.NEWEST_REVIEW_FIRST) {
			fragment = mReviewSortFragmentMap.get(ReviewSort.HIGHEST_RATING_FIRST);
		}
		else if (completedSort == ReviewSort.HIGHEST_RATING_FIRST) {
			fragment = mReviewSortFragmentMap.get(ReviewSort.LOWEST_RATING_FIRST);
		}
		else {
			fragment = mReviewSortFragmentMap.get(ReviewSort.NEWEST_REVIEW_FIRST);
		}

		if (!fragment.getHasAttemptedDownload()) {
			fragment.startReviewsDownload();
		}
	}

	/**
	 * This override is used to track the number of reviews seen
	 */
	@Override
	public void addMoreReviewsSeen(Set<String> reviews) {
		mViewedReviews.addAll(reviews);
	}
}
