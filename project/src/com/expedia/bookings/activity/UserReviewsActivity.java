package com.expedia.bookings.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.SegmentedControlGroup;

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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UserReviewsActivity extends FragmentActivity implements UserReviewsFragmentListener {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.activity_user_reviews_new);

		extractPropertyFromIntent();

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState != null) {
			mRecentReviewsFragment = (UserReviewsFragment) fm.findFragmentByTag("mRecentReviewsFragment");
			mFavorableReviewsFragment = (UserReviewsFragment) fm.findFragmentByTag("mFavorableReviewsFragment");
			mCriticalReviewsFragment = (UserReviewsFragment) fm.findFragmentByTag("mCriticalReviewsFragment");

			mHasReviewStats = savedInstanceState.getBoolean("mHasReviewStats");
			mTotalReviewCount = savedInstanceState.getInt("mTotalReviewCount");
			mRecommendedReviewCount = savedInstanceState.getInt("mRecommendedReviewCount");
			mAverageOverallRating = savedInstanceState.getFloat("mAverageOverallRating");
		}
		else {
			// add the user reviews list fragment to the framelayout container
			FragmentTransaction ft = fm.beginTransaction();

			mRecentReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.NEWEST_REVIEW_FIRST);
			mFavorableReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.HIGHEST_RATING_FIRST);
			mCriticalReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.LOWEST_RATING_FIRST);

			ft.add(R.id.user_review_content_container, mFavorableReviewsFragment, "mFavorableReviewsFragment");
			ft.add(R.id.user_review_content_container, mCriticalReviewsFragment, "mCriticalReviewsFragment");
			ft.add(R.id.user_review_content_container, mRecentReviewsFragment, "mRecentReviewsFragment");
			ft.commit();

			// start the download of the user reviews statistics
			mBackgroundDownloader.startDownload(REVIEWS_STATISTICS_DOWNLOAD, mReviewStatisticsDownload,
					mReviewStatisticsDownloadCallback);
		}

		mReviewSortFragmentMap = new HashMap<ReviewSort, UserReviewsFragment>();
		mReviewSortFragmentMap.put(ReviewSort.NEWEST_REVIEW_FIRST, mRecentReviewsFragment);
		mReviewSortFragmentMap.put(ReviewSort.HIGHEST_RATING_FIRST, mFavorableReviewsFragment);
		mReviewSortFragmentMap.put(ReviewSort.LOWEST_RATING_FIRST, mCriticalReviewsFragment);

		mSortGroup = (SegmentedControlGroup) findViewById(R.id.user_review_sort_group);
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
			btn = (RadioButton) findViewById(R.id.user_review_button_favorable);
		}
		else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST) {
			btn = (RadioButton) findViewById(R.id.user_review_button_critical);
		}
		else {
			btn = (RadioButton) findViewById(R.id.user_review_button_recent);
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
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// store the primitives which need to be saved on configuration changes
		outState.putBoolean("mHasReviewStats", mHasReviewStats);
		outState.putInt("mTotalReviewCount", mTotalReviewCount);
		outState.putInt("mRecommendedReviewCount", mRecommendedReviewCount);
		outState.putFloat("mAverageOverallRating", mAverageOverallRating);
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
		View bottomBar = findViewById(R.id.bottom_bar);
		if (bottomBar != null) {
			// Configure the book now button
			TextView bookNowButton = (TextView) findViewById(R.id.book_now_button);
			bookNowButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent newIntent = new Intent(mContext, RoomsAndRatesListActivity.class);
					newIntent.fillIn(getIntent(), 0);
					startActivity(newIntent);
				}
			});

			if (mHasReviewStats) {
				TextView totalReviews = (TextView) findViewById(R.id.user_review_total_reviews);
				totalReviews.setText(getResources().getQuantityString(R.plurals.number_of_reviews, mTotalReviewCount,
						mTotalReviewCount));

				RatingBar bottomRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar_bottom);
				bottomRatingBar.setRating(mAverageOverallRating);
				bottomRatingBar.setVisibility(View.VISIBLE);
			}
			else {
				RatingBar bottomRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar_bottom);
				bottomRatingBar.setVisibility(View.GONE);
			}

			bottomBar.setVisibility(View.VISIBLE);
		}
	}

	private void showReviewsUnavailableError() {
		TextView emptyTextView = (TextView) findViewById(R.id.empty_text_view);

		if (emptyTextView != null) {
			String text = getResources().getString(R.string.user_review_unavailable);
			emptyTextView.setText(text);
		}

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
	}

	private void extractPropertyFromIntent() {
		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);

		// This code allows us to test the UserReviewsActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			try {
				mProperty = new Property();
				mProperty.fillWithTestData();
			}
			catch (JSONException e) {
				Log.e("Couldn't create dummy data!", e);
			}
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
}
