package com.expedia.bookings.activity;

import org.json.JSONException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.json.JSONUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public class UserReviewsActivity extends FragmentActivity {

	// Download keys
	public static final String REVIEWS_STATISTICS_DOWNLOAD = "com.expedia.bookings.activity.UserReviewsActivity.ReviewsStatisticsDownload";

	// Member variables
	private Context mContext;
	public Property mProperty;

	// Fragments and Views
	private UserReviewsFragment mUserReviewsFragment;

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();

	// Stores the counts retrieved from BazaarVoice using FilteredStats param
	private boolean mHasReviewStats = false;
	private int mTotalReviewCount;
	private int mRecommendedReviewCount;
	private float mAverageOverallRating;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("bradley", "activity: onCreate");

		mContext = this;

		setContentView(R.layout.activity_user_reviews_new);

		extractPropertyFromIntent();

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState != null) {
			mUserReviewsFragment = (UserReviewsFragment) fm.findFragmentByTag("mUserReviewsFragment");

			mHasReviewStats = savedInstanceState.getBoolean("mHasReviewStats");
			mTotalReviewCount = savedInstanceState.getInt("mTotalReviewCount");
			mRecommendedReviewCount = savedInstanceState.getInt("mRecommendedReviewCount");
			mAverageOverallRating = savedInstanceState.getFloat("mAverageOverallRating");
		}
		else {
			// add the user reviews list fragment to the framelayout container
			FragmentTransaction ft = fm.beginTransaction();

			ReviewSort sort = ReviewSort.NEWEST_REVIEW_FIRST;
			sort = ReviewSort.HIGHEST_RATING_FIRST;

			mUserReviewsFragment = UserReviewsFragment.newInstance(mProperty, sort);
			ft.add(R.id.user_review_content_container, mUserReviewsFragment, "mUserReviewsFragment");
			ft.commit();

			// start the download of the user reviews statistics
			mBackgroundDownloader.startDownload(REVIEWS_STATISTICS_DOWNLOAD, mReviewStatisticsDownload,
					mReviewStatisticsDownloadCallback);
		}

		if (mHasReviewStats) {
			populateBottomBar();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("bradley", "activity: onResume");

		if (mBackgroundDownloader.isDownloading(REVIEWS_STATISTICS_DOWNLOAD)) {
			mBackgroundDownloader.registerDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD,
					mReviewStatisticsDownloadCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.d("bradley", "activity: onPause");

		mBackgroundDownloader.unregisterDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("bradley", "activity: onDestroy");

		// TODO: ACTIVITY IS ACTUALLY BEING CLOSED, 	
		if (isFinishing()) {
			Log.d("bradley", "activity: onDestroy, isFinishing");
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
			Log.d("bradley", "download callback");
			ReviewsStatisticsResponse response = (ReviewsStatisticsResponse) results;

			if (response == null) {
				Log.d("bradley", "response null");

				showReviewsUnavailableError();
			}
			else {
				if (response.hasErrors()) {
					Log.d("bradley", "response has errors");

					showReviewsUnavailableError();
				}
				else {
					// grab the stats
					mRecommendedReviewCount = response.getRecommendedCount();
					mTotalReviewCount = response.getTotalReviewCount();
					mAverageOverallRating = response.getAverageOverallRating();
					mHasReviewStats = true;

					populateBottomBar();

					mUserReviewsFragment.startReviewsDownload();

					mUserReviewsFragment.populateListHeader(mRecommendedReviewCount, mTotalReviewCount);

					// TODO: DISPLAY NO REVIEWS MESSAGE
					if (mTotalReviewCount > 0) {
					}
					else {
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
}
