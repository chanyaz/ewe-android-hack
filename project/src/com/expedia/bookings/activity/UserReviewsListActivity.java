package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class UserReviewsListActivity extends SherlockFragmentActivity implements UserReviewsFragmentListener, TabListener,
		OnPageChangeListener {

	// Download keys
	private static final String REVIEWS_STATISTICS_DOWNLOAD = UserReviewsListActivity.class.getName() + ".stats";

	// Member variables
	private Context mContext;
	public Property mProperty;

	// Fragments and Views
	private UserReviewsFragment mRecentReviewsFragment;
	private UserReviewsFragment mFavorableReviewsFragment;
	private UserReviewsFragment mCriticalReviewsFragment;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	// Instance variable names
	private static final String INSTANCE_RECENT_REVIEWS_FRAGMENT = "INSTANCE_RECENT_REVIEWS_FRAGMENT";
	private static final String INSTANCE_FAVORABLE_REVIEWS_FRAGMENT = "INSTANCE_FAVORABLE_REVIEWS_FRAGMENT";
	private static final String INSTANCE_CRITICAL_REVIEWS_FRAGMENT = "INSTANCE_CRITICAL_REVIEWS_FRAGMENT";
	private static final String INSTANCE_VIEWED_REVIEWS = "INSTANCE_VIEWED_REVIEWS";

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();

	private Set<String> mViewedReviews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		mContext = this;

		mProperty = Db.getSelectedProperty();

		if (savedInstanceState != null) {
			mRecentReviewsFragment = Ui.findSupportFragment(this, INSTANCE_RECENT_REVIEWS_FRAGMENT);
			mFavorableReviewsFragment = Ui.findSupportFragment(this, INSTANCE_FAVORABLE_REVIEWS_FRAGMENT);
			mCriticalReviewsFragment = Ui.findSupportFragment(this, INSTANCE_CRITICAL_REVIEWS_FRAGMENT);
			mViewedReviews = new HashSet<String>(savedInstanceState.getStringArrayList(INSTANCE_VIEWED_REVIEWS));
		}
		else {
			mRecentReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.NEWEST_REVIEW_FIRST);
			mFavorableReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.HIGHEST_RATING_FIRST);
			mCriticalReviewsFragment = UserReviewsFragment.newInstance(mProperty, ReviewSort.LOWEST_RATING_FIRST);
			mViewedReviews = new HashSet<String>();
		}

		setContentView(R.layout.activity_user_reviews);

		initializePager();
		initializeActionBar();
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
			mRecentReviewsFragment.cancelReviewsDownload();
			mFavorableReviewsFragment.cancelReviewsDownload();
			mCriticalReviewsFragment.cancelReviewsDownload();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ArrayList<String> viewedReviews = new ArrayList<String>(mViewedReviews);
		outState.putStringArrayList(INSTANCE_VIEWED_REVIEWS, viewedReviews);
	}

	private final Download<ReviewsStatisticsResponse> mReviewStatisticsDownload = new Download<ReviewsStatisticsResponse>() {
		@Override
		public ReviewsStatisticsResponse doDownload() {
			ExpediaServices expediaServices = new ExpediaServices(mContext);
			mBackgroundDownloader.addDownloadListener(REVIEWS_STATISTICS_DOWNLOAD, expediaServices);
			return expediaServices.reviewsStatistics(mProperty);
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

		// populate the list header for all three fragments
		mRecentReviewsFragment.populateListHeader();
		mFavorableReviewsFragment.populateListHeader();
		mCriticalReviewsFragment.populateListHeader();

		LinearLayout titleView = (LinearLayout) getSupportActionBar().getCustomView();
		if (titleView == null) {
			return;
		}

		mRecentReviewsFragment.startReviewsDownload();

		TextView titleTextView = (TextView) titleView.findViewById(R.id.title);
		RatingBar ratingBar = (RatingBar) titleView.findViewById(R.id.user_rating);

		ReviewsStatisticsResponse stats = Db.getSelectedReviewsStatisticsResponse();
		if (stats != null) {
			String title = getResources().getQuantityString(R.plurals.number_of_reviews, stats.getTotalReviewCount(),
					stats.getTotalReviewCount());
			titleTextView.setText(title);

			float rating = stats.getAverageOverallRating();
			ratingBar.setRating(rating);
			ratingBar.setVisibility(View.VISIBLE);
		}
		else {
			ratingBar.setVisibility(View.GONE);
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
	public void onDownloadComplete(UserReviewsFragment fragmentDone) {
		UserReviewsFragment fragmentStart;
		if (fragmentDone == mRecentReviewsFragment) {
			fragmentStart = mFavorableReviewsFragment;
		}
		else if (fragmentDone == mFavorableReviewsFragment) {
			fragmentStart = mCriticalReviewsFragment;
		}
		else {
			fragmentStart = mRecentReviewsFragment;
		}

		if (!fragmentStart.getHasAttemptedDownload()) {
			fragmentStart.startReviewsDownload();
		}
	}

	/**
	 * This override is used to track the number of reviews seen
	 */
	@Override
	public void addMoreReviewsSeen(Set<String> reviews) {
		mViewedReviews.addAll(reviews);
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
	// FragmentViewPager

	private void initializePager() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(mRecentReviewsFragment);
		fragments.add(mFavorableReviewsFragment);
		fragments.add(mCriticalReviewsFragment);
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setCurrentItem(0);
	}

	private static class PagerAdapter extends FragmentPagerAdapter {
		private List<Fragment> mFragments;

		public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			mFragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ActionBar.TabListener

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int index = (Integer)tab.getTag();

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
