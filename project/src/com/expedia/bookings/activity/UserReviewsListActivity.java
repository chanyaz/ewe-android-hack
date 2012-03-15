package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.LocaleUtils.ReviewLanguageSet;
import com.expedia.bookings.widget.UserReviewsAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.widget.SegmentedControlGroup;

public class UserReviewsListActivity extends Activity implements OnScrollListener {

	private static final String REVIEWS_STATISTICS_DOWNLOAD = "REVIEWS_STATISTICS_DOWNLOAD";
	@SuppressWarnings("serial")
	private static final Map<ReviewSort, String> SORT_BGDL_KEY = new HashMap<ReviewSort, String>() {
		{
			put(ReviewSort.HIGHEST_RATING_FIRST, "KEY_REVIEWS_HIGHEST");
			put(ReviewSort.LOWEST_RATING_FIRST, "KEY_REVIEWS_LOWEST");
			put(ReviewSort.NEWEST_REVIEW_FIRST, "KEY_REVIEWS_NEWEST");
		}
	};

	private static final int THUMB_CUTOFF_INCLUSIVE = 5;
	private static final int BODY_LENGTH_CUTOFF = 270;

	// Views
	private SegmentedControlGroup mSortGroup;
	private LayoutInflater mLayoutInflater;
	private ViewGroup mFooterLoadingMore;

	//this handler will add/remove the loading more footer, required for accessing UI thread from other download threads
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Object[] data = (Object[]) msg.obj;
			Boolean addFooter = (Boolean) data[0];
			ReviewSort reviewSort = ReviewSort.valueOf((String) data[1]);
			ListView listView = getListView(mListViewContainersMap.get(reviewSort));
			if (addFooter.booleanValue()) {
				listView.addFooterView(mFooterLoadingMore, null, false);
			}
			else {
				listView.removeFooterView(mFooterLoadingMore);
			}
		}
	};

	// Member variables
	private Context mContext;
	private Property mProperty;

	// Review data structures
	private ReviewSort mCurrentReviewSort = ReviewSort.NEWEST_REVIEW_FIRST;

	/* 
	 * keeps a mapping of the different containers holding the list views (and empty views) for each review sort type
	 * 8605: keeping different list views (backed by corresponding adapter) helps preserve list position across orientation changes
	 */
	private HashMap<ReviewSort, ViewGroup> mListViewContainersMap = new HashMap<ReviewSort, ViewGroup>();

	/* 
	 * keeps a mapping of the different adapters maintaining data for each of their corresponding list views.
	 * when a user navigates from one sort type to the next, the appropriate list view is made visible.
	 * the adapters are updated with data in the background to ensure that each list maintaints its own data
	 */
	private HashMap<ReviewSort, UserReviewsAdapter> mListAdaptersMap = new HashMap<ReviewSort, UserReviewsAdapter>();

	/*
	 * keeps track of whether the loading indicator is showing as the footer in the list view.
	 * This bookkeeping is not preserved across orientation change so as to re-show the loading
	 * indicator on orientation change if there are more reviews to display
	 */
	private HashSet<ReviewSort> isLoadingIndicatorShowingForReviewSort = new HashSet<ReviewSort>();

	/*
	 * this map keeps track of different sorts, that are stored in separate tabs
	 */
	private HashMap<ReviewSort, TabSort> mTabMap;

	// Tracking data structures
	private Set<String> mViewedReviews;

	private int mTotalReviewCount = -1;
	private int mRecommendedReviewCount = -1;
	private float mAverageOverallRating = -1;

	// Downloading tasks and callbacks
	private BackgroundDownloader mReviewsDownloader = BackgroundDownloader.getInstance();
	private ExpediaServices mExpediaServices;

	private void ensureExpediaServicesCacheFilled() {
		if (mExpediaServices == null) {
			mExpediaServices = new ExpediaServices(mContext);
		}
	}

	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_user_reviews);

		mContext = getApplicationContext();
		ViewGroup listsContainer = (ViewGroup) findViewById(R.id.lists_container);

		/*
		 * Inflate a container comprising a list view and an empty view per review sort type
		 * so that we can easily preserve the scroll position across tab clicks and orientation
		 * changes. 
		 */
		ViewGroup recentReviewsListViewContainer = (ViewGroup) getLayoutInflater().inflate(
				R.layout.include_review_list_container, null);
		ViewGroup criticalReviewsListViewContainer = (ViewGroup) getLayoutInflater().inflate(
				R.layout.include_review_list_container, null);
		ViewGroup favorableReviewsListViewContainer = (ViewGroup) getLayoutInflater().inflate(
				R.layout.include_review_list_container, null);

		listsContainer.addView(criticalReviewsListViewContainer);
		listsContainer.addView(favorableReviewsListViewContainer);
		listsContainer.addView(recentReviewsListViewContainer);

		UserReviewsAdapter recentReviewsAdapter = new UserReviewsAdapter(mContext, mProperty,
				getListView(recentReviewsListViewContainer));
		UserReviewsAdapter criticalReviewsAdapter = new UserReviewsAdapter(mContext, mProperty,
				getListView(criticalReviewsListViewContainer));
		UserReviewsAdapter favorableReviewsAdapter = new UserReviewsAdapter(mContext, mProperty,
				getListView(favorableReviewsListViewContainer));

		mListViewContainersMap.put(ReviewSort.HIGHEST_RATING_FIRST, favorableReviewsListViewContainer);
		mListViewContainersMap.put(ReviewSort.LOWEST_RATING_FIRST, criticalReviewsListViewContainer);
		mListViewContainersMap.put(ReviewSort.NEWEST_REVIEW_FIRST, recentReviewsListViewContainer);

		mListAdaptersMap.put(ReviewSort.HIGHEST_RATING_FIRST, favorableReviewsAdapter);
		mListAdaptersMap.put(ReviewSort.LOWEST_RATING_FIRST, criticalReviewsAdapter);
		mListAdaptersMap.put(ReviewSort.NEWEST_REVIEW_FIRST, recentReviewsAdapter);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);

		// This code allows us to test the UserReviewsListActivity standalone, for layout purposes.
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

		// init the map of objects storing the reviews for each tab
		mTabMap = getTabMap();

		// Load the three different lists as the adapter is being constructed
		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state == null) {
			// Cancel any existing downloads
			mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(ReviewSort.HIGHEST_RATING_FIRST));
			mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(ReviewSort.NEWEST_REVIEW_FIRST));
			mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(ReviewSort.LOWEST_RATING_FIRST));

			configureHeader();

			mViewedReviews = new HashSet<String>();
		}
		else {
			extractActivityState(state);
			configureHeader();
			updateReviewNumbers();
			if (mTabMap.get(mCurrentReviewSort).mReviewsWrapped != null) {
				ViewGroup listViewContainer = mListViewContainersMap.get(mCurrentReviewSort);
				UserReviewsAdapter adapter = mListAdaptersMap.get(mCurrentReviewSort);
				adapter.setUserReviews(new ArrayList<ReviewWrapper>(mTabMap.get(mCurrentReviewSort).mReviewsWrapped));
				adapter.notifyDataSetChanged();
				bringContainerToFront(listViewContainer);
				showListOrEmptyView(listViewContainer, adapter);
			}
			else {
				// start the downloads based on the currently selected sort option
				String key = SORT_BGDL_KEY.get(mCurrentReviewSort);
				TabSort tab = mTabMap.get(mCurrentReviewSort);

				mReviewsDownloader.startDownload(key, tab.mDownloadTask, tab.mDownloadCallback);
			}
		}

		if (mTotalReviewCount == -1) {
			mHandler.postDelayed(mReviewStatisticsDownloadTask, 0);
		}

		ListView recentReviewsListView = getListView(recentReviewsListViewContainer);
		ListView criticalReviewsListView = getListView(criticalReviewsListViewContainer);
		ListView favorableReviewsListView = getListView(favorableReviewsListViewContainer);

		recentReviewsListView.setAdapter(recentReviewsAdapter);
		criticalReviewsListView.setAdapter(criticalReviewsAdapter);
		favorableReviewsListView.setAdapter(favorableReviewsAdapter);

		recentReviewsListView.setOnScrollListener(this);
		criticalReviewsListView.setOnScrollListener(this);
		favorableReviewsListView.setOnScrollListener(this);

		configureBottomBar();

		mFooterLoadingMore = (ViewGroup) mLayoutInflater.inflate(R.layout.footer_user_reviews_list_loading_more, null,
				false);

	}

	/**
	 * Configure the bottom bar with the book now button
	 * @param visible - whether or not to set the bottom bar visible or not
	 */
	public void configureBottomBar() {
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

			TextView totalReviews = (TextView) findViewById(R.id.user_review_total_reviews);
			totalReviews.setText(getResources().getQuantityString(R.plurals.number_of_reviews, mTotalReviewCount,
					mTotalReviewCount));

			RatingBar bottomRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar_bottom);
			bottomRatingBar.setRating(mAverageOverallRating);

			if (mTotalReviewCount == -1) {
				bottomBar.setVisibility(View.GONE);
			}
			else {
				bottomBar.setVisibility(View.VISIBLE);
			}
		}
	}

	public void startReviewsDownload() {
		// start the downloads based on the currently selected sort option
		String key = SORT_BGDL_KEY.get(mCurrentReviewSort);
		TabSort tab = mTabMap.get(mCurrentReviewSort);
		//		mReviewsDownloader.cancelDownload(key);
		mReviewsDownloader.startDownload(key, tab.mDownloadTask, tab.mDownloadCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// start the downloads based on the currently selected sort option
		//		TabSort tab = mTabMap.get(mCurrentReviewSort);
		//		if (tab.mReviewsWrapped == null && !tab.attemptedDownload) {
		//			String key = SORT_BGDL_KEY.get(mCurrentReviewSort);
		//			mReviewsDownloader.cancelDownload(key);
		//			mReviewsDownloader.startDownload(key, tab.mDownloadTask, tab.mDownloadCallback);
		//		}
	}

	@Override
	protected void onPause() {
		mReviewsDownloader.unregisterDownloadCallback(SORT_BGDL_KEY.get(ReviewSort.HIGHEST_RATING_FIRST));
		mReviewsDownloader.unregisterDownloadCallback(SORT_BGDL_KEY.get(ReviewSort.LOWEST_RATING_FIRST));
		mReviewsDownloader.unregisterDownloadCallback(SORT_BGDL_KEY.get(ReviewSort.NEWEST_REVIEW_FIRST));

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			// Cancel all current review downloads
			mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(ReviewSort.HIGHEST_RATING_FIRST));
			mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(ReviewSort.LOWEST_RATING_FIRST));
			mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(ReviewSort.NEWEST_REVIEW_FIRST));

			// Track # of reviews seen
			int numReviewsSeen = mViewedReviews.size();
			Log.d("Tracking # of reviews seen: " + numReviewsSeen);
			String referrerId = "App.Hotels.Reviews." + numReviewsSeen + "ReviewsViewed";
			TrackingUtils.trackSimpleEvent(this, null, null, "Shopper", referrerId);
		}
	}

	private ListView getListView(ViewGroup listViewContainer) {
		return (ListView) listViewContainer.findViewById(R.id.user_reviews_list);
	}

	private void bringContainerToFront(ViewGroup listViewContainer) {
		listViewContainer.bringToFront();
		for (ViewGroup viewContainer : mListViewContainersMap.values()) {
			if (!viewContainer.equals(listViewContainer)) {
				viewContainer.setVisibility(View.GONE);
			}
			else {
				viewContainer.setVisibility(View.VISIBLE);
			}
		}
	}

	public void configureHeader() {
		if (mLayoutInflater == null) {
			mLayoutInflater = getLayoutInflater();
		}

		ViewGroup header = (ViewGroup) mLayoutInflater.inflate(R.layout.header_user_reviews_list, null, false);
		for (ViewGroup viewContainer : mListViewContainersMap.values()) {
			getListView(viewContainer).addHeaderView(header, null, false);
		}

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

				ViewGroup listViewContainer = mListViewContainersMap.get(mCurrentReviewSort);
				UserReviewsAdapter adapter = mListAdaptersMap.get(mCurrentReviewSort);
				adapter.setUserReviews(mTabMap.get(mCurrentReviewSort).mReviewsWrapped);
				bringContainerToFront(listViewContainer);
				showListOrEmptyView(listViewContainer, adapter);
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
	}

	/**
	 * Populates the list header with the correct review total + recommendation total, and the footer with number of reviews
	 * called after the BV request has been made
	 */
	public void updateReviewNumbers() {
		if (mRecommendedReviewCount != -1 && mTotalReviewCount != -1) {
			for (ViewGroup viewContainer : mListViewContainersMap.values()) {
				TextView recommendText = (TextView) viewContainer.findViewById(R.id.user_reviews_recommendation_tag);

				String text = String.format(getString(R.string.user_review_recommendation_tag_text),
						mRecommendedReviewCount, mTotalReviewCount);
				CharSequence styledText = Html.fromHtml(text);

				ImageView thumbView = (ImageView) findViewById(R.id.user_reviews_thumb);

				if (mRecommendedReviewCount * 10 / mTotalReviewCount >= THUMB_CUTOFF_INCLUSIVE) {
					thumbView.setImageResource(R.drawable.review_thumbs_up);
				}
				else {
					thumbView.setImageResource(R.drawable.review_thumbs_down);
				}
				recommendText.setText(styledText);
			}
			configureBottomBar();
		}
	}

	// Scroll listener infinite loading implementation
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

		List<ReviewWrapper> reviews = mTabMap.get(mCurrentReviewSort).mReviewsWrapped;

		boolean hasMore = false;
		LinkedList<ReviewLanguageSet> list = mTabMap.get(mCurrentReviewSort).mLanguageList;
		if (list != null && list.size() > 0) {
			hasMore = true;
		}

		if (loadMore && reviews != null && hasMore) {
			// start the downloads based on the currently selected sort option
			String key = SORT_BGDL_KEY.get(mCurrentReviewSort);
			TabSort tab = mTabMap.get(mCurrentReviewSort);
			mReviewsDownloader.startDownload(key, tab.mDownloadTask, tab.mDownloadCallback);

			// only show the loading indicator if its not as yet shown. 
			// Note that we keep track of whether or not the loading indicator is shown 
			// independently of starting a download as doDownload is only called
			// if a new download needs to be started, not when re-registerng the callback 
			// for a download thats in progress. Consequently, on orientation change,
			// we need to be able to re-show the loading indicator in the footer of the list view
			// while a download does not need to be restarted; instead a callback can just be 
			// re-registered
			if (!isLoadingIndicatorShowingForReviewSort.contains(mCurrentReviewSort)) {
				//send message to put loading footer
				mHandler.sendMessage(prepareMessage(true, mCurrentReviewSort));
				isLoadingIndicatorShowingForReviewSort.add(mCurrentReviewSort);
			}
		}

		// Track which reviews are visible, add them to the list
		ListAdapter adapter = view.getAdapter();
		int count = adapter.getCount();
		for (int a = 0; a < visibleItemCount && firstVisibleItem + a < count; a++) {
			Object item = adapter.getItem(firstVisibleItem + a);
			if (item instanceof ReviewWrapper) {
				mViewedReviews.add(((ReviewWrapper) item).review.getReviewId());
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public static class TabSort {

		public UserReviewsListActivity mActivity;

		public ReviewSort mReviewSort;

		public LinkedList<ReviewLanguageSet> mLanguageList;

		public ArrayList<ReviewWrapper> mReviewsWrapped;

		public boolean attemptedDownload;

		public ReviewDownloadTask mDownloadTask;

		public ReviewDownloadCallback mDownloadCallback;

		//DOWNLOAD OBJECTS

		public TabSort(UserReviewsListActivity activity, ReviewSort sort, LinkedList<ReviewLanguageSet> languageList) {
			this.mActivity = activity;
			this.mDownloadTask = new ReviewDownloadTask();
			this.mDownloadCallback = new ReviewDownloadCallback();

			this.mReviewSort = sort;
			this.mLanguageList = languageList;
			this.mReviewsWrapped = new ArrayList<ReviewWrapper>();
			this.attemptedDownload = false;
		}

		public void setActivity(UserReviewsListActivity activity) {
			this.mActivity = activity;
		}

		private class ReviewDownloadTask implements Download {

			public ReviewDownloadTask() {
			}

			@Override
			public Object doDownload() {
				mActivity.ensureExpediaServicesCacheFilled();

				mActivity.mReviewsDownloader.addDownloadListener(SORT_BGDL_KEY.get(mReviewSort),
						mActivity.mExpediaServices);

				// grab the correct page number
				ReviewLanguageSet meta = mLanguageList.getFirst();
				int pageNumber = meta.getPageNumber();
				String localesString = meta.getLocalesString();

				return mActivity.mExpediaServices.reviews(mActivity.mProperty, mReviewSort, pageNumber, localesString);
			}
		}

		private class ReviewDownloadCallback implements OnDownloadComplete {

			public ReviewDownloadCallback() {
			}

			@Override
			public void onDownload(Object results) {
				ReviewsResponse response = (ReviewsResponse) results;

				ReviewLanguageSet rls;

				// update attempted download flag
				if (mLanguageList != null && mLanguageList.size() > 0) {
					rls = mLanguageList.pop();
					rls.setAttemptedDownload(true);
					mLanguageList.push(rls);
				}
				attemptedDownload = true;

				UserReviewsAdapter adapter = mActivity.mListAdaptersMap.get(mReviewSort);
				ViewGroup listViewContainer = mActivity.mListViewContainersMap.get(mReviewSort);

				if (response != null) {

					// grab the meta object from the list within the map for modification(s)
					rls = mLanguageList.pop();

					if (response.getReviewCount() > 0) {
						rls.setTotalCount(response.getTotalCount());
						ArrayList<ReviewWrapper> newlyLoadedReviewsWrapped = mActivity.reviewWrapperListInit(response
								.getReviews());

						rls.incrementPageNumber();

						// push object back if there are more pages to download
						if (rls.hasMore()) {
							mLanguageList.push(rls);
						}

						// append the new reviews to old collection, remove loading view, refresh
						if (mReviewsWrapped != null) {
							mReviewsWrapped.addAll(newlyLoadedReviewsWrapped);
						}
						else {
							mReviewsWrapped = newlyLoadedReviewsWrapped;
						}

						//send message to remove loading footer
						mActivity.mHandler.sendMessage(mActivity.prepareMessage(false, mReviewSort));

						adapter.setUserReviews(mReviewsWrapped);
					}
				}
				else {
					//send message to remove loading footer
					mActivity.mHandler.sendMessage(mActivity.prepareMessage(false, mReviewSort));

					if (response == null || response.hasErrors()) {
						TrackingUtils.trackErrorPage(mActivity.mContext, "UserReviewLoadFailed");
					}
				}

				mActivity.isLoadingIndicatorShowingForReviewSort.remove(mReviewSort);

				if (mReviewSort == mActivity.mCurrentReviewSort) {
					mActivity.bringContainerToFront(listViewContainer);
				}
				mActivity.showListOrEmptyView(listViewContainer, adapter);

				// chain the downloads in the callback, if the download has not been attempted, make sure to start the download
				String key;
				TabSort nextTab;
				if (!mActivity.mTabMap.get(ReviewSort.NEWEST_REVIEW_FIRST).attemptedDownload
						&& !mActivity.mReviewsDownloader.isDownloading(SORT_BGDL_KEY
								.get(ReviewSort.NEWEST_REVIEW_FIRST))) {
					key = SORT_BGDL_KEY.get(ReviewSort.NEWEST_REVIEW_FIRST);
					nextTab = mActivity.mTabMap.get(ReviewSort.NEWEST_REVIEW_FIRST);
					//					mActivity.mReviewsDownloader.cancelDownload(key);
					mActivity.mReviewsDownloader.startDownload(key, nextTab.mDownloadTask, nextTab.mDownloadCallback);
				}
				else if (!mActivity.mTabMap.get(ReviewSort.HIGHEST_RATING_FIRST).attemptedDownload
						&& !mActivity.mReviewsDownloader.isDownloading(SORT_BGDL_KEY
								.get(ReviewSort.HIGHEST_RATING_FIRST))) {
					key = SORT_BGDL_KEY.get(ReviewSort.HIGHEST_RATING_FIRST);
					nextTab = mActivity.mTabMap.get(ReviewSort.HIGHEST_RATING_FIRST);
					//					mActivity.mReviewsDownloader.cancelDownload(key);
					mActivity.mReviewsDownloader.startDownload(key, nextTab.mDownloadTask, nextTab.mDownloadCallback);
				}
				else if (!mActivity.mTabMap.get(ReviewSort.LOWEST_RATING_FIRST).attemptedDownload
						&& !mActivity.mReviewsDownloader.isDownloading(SORT_BGDL_KEY
								.get(ReviewSort.LOWEST_RATING_FIRST))) {
					key = SORT_BGDL_KEY.get(ReviewSort.LOWEST_RATING_FIRST);
					nextTab = mActivity.mTabMap.get(ReviewSort.LOWEST_RATING_FIRST);
					//					mActivity.mReviewsDownloader.cancelDownload(key);
					mActivity.mReviewsDownloader.startDownload(key, nextTab.mDownloadTask, nextTab.mDownloadCallback);
				}
			}
		}
	}

	private HashMap<ReviewSort, TabSort> getTabMap() {
		HashMap<ReviewSort, TabSort> map = new HashMap<ReviewSort, TabSort>();

		LinkedList<String> languages = LocaleUtils.getLanguages(mContext);

		// RECENT SORT ORDER
		LinkedList<ReviewLanguageSet> recentReviewLanguageSet = new LinkedList<ReviewLanguageSet>();
		for (String languageCode : languages) {
			ReviewLanguageSet rls = new ReviewLanguageSet();
			rls.addLanguage(languageCode);
			recentReviewLanguageSet.add(rls);
		}

		TabSort recentTabSort = new TabSort(this, ReviewSort.NEWEST_REVIEW_FIRST, recentReviewLanguageSet);

		map.put(ReviewSort.NEWEST_REVIEW_FIRST, recentTabSort);

		// FAVORABLE SORT ORDER
		LinkedList<ReviewLanguageSet> favorableReviewLanguageSet = new LinkedList<ReviewLanguageSet>();
		ReviewLanguageSet frls = new ReviewLanguageSet();
		frls.setLocalesString(LocaleUtils.formatLanguageCodes(languages));
		favorableReviewLanguageSet.add(frls);

		TabSort favorableTabSort = new TabSort(this, ReviewSort.HIGHEST_RATING_FIRST, favorableReviewLanguageSet);

		map.put(ReviewSort.HIGHEST_RATING_FIRST, favorableTabSort);

		// CRITICAL SORT ORDER
		LinkedList<ReviewLanguageSet> criticalReviewLanguageSet = new LinkedList<ReviewLanguageSet>();
		ReviewLanguageSet crls = new ReviewLanguageSet();
		crls.setLocalesString(LocaleUtils.formatLanguageCodes(languages));
		criticalReviewLanguageSet.add(crls);

		TabSort criticalTabSort = new TabSort(this, ReviewSort.LOWEST_RATING_FIRST, criticalReviewLanguageSet);

		map.put(ReviewSort.LOWEST_RATING_FIRST, criticalTabSort);

		return map;
	}

	private Runnable mReviewStatisticsDownloadTask = new Runnable() {

		@Override
		public void run() {
			mReviewsDownloader.startDownload(REVIEWS_STATISTICS_DOWNLOAD, mReviewStatisticsDownload,
					mReviewStatisticsDownloadCallback);
		}

	};

	private Download mReviewStatisticsDownload = new Download() {

		@Override
		public Object doDownload() {
			ensureExpediaServicesCacheFilled();

			mReviewsDownloader.addDownloadListener(REVIEWS_STATISTICS_DOWNLOAD, mExpediaServices);

			return mExpediaServices.reviewsStatistics(mProperty);
		}

	};

	private OnDownloadComplete mReviewStatisticsDownloadCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			ReviewsStatisticsResponse response = (ReviewsStatisticsResponse) results;
			if (response != null) {
				mRecommendedReviewCount = response.getRecommendedCount();
				mTotalReviewCount = response.getTotalReviewCount();
				mAverageOverallRating = response.getAverageOverallRating();

				mHandler.removeCallbacks(mUpdateListHeaderTask);
				mHandler.post(mUpdateListHeaderTask);
			}
		}
	};

	private Runnable mUpdateListHeaderTask = new Runnable() {

		@Override
		public void run() {
			//update the header
			updateReviewNumbers();
		}

	};

	// store the read more state in a wrapper class
	public class ReviewWrapper {
		public Review review;
		public boolean bodyWasReduced;
		public boolean isDisplayingFull;
		public String bodyReduced;
	}

	private ArrayList<ReviewWrapper> reviewWrapperListInit(List<Review> reviews) {
		ArrayList<ReviewWrapper> loadedReviews = new ArrayList<ReviewWrapper>();
		if (reviews == null) {
			return null;
		}
		for (Review review : reviews) {
			ReviewWrapper loadedReview = new ReviewWrapper();
			loadedReview.review = review;

			String body = review.getBody();
			if (body.length() > BODY_LENGTH_CUTOFF) {
				loadedReview.bodyReduced = body.substring(0, BODY_LENGTH_CUTOFF);
				loadedReview.bodyReduced += "...";
				loadedReview.bodyWasReduced = true;
			}
			else {
				loadedReview.bodyWasReduced = false;
			}

			loadedReview.isDisplayingFull = false;
			loadedReviews.add(loadedReview);
		}
		return loadedReviews;
	}

	// Configuration change code

	@Override
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = buildActivityState();
		return state;
	}

	private ActivityState buildActivityState() {
		ActivityState state = new ActivityState();

		state.tabMap = mTabMap;

		state.mCurrentReviewSort = mCurrentReviewSort;

		state.mRecommendedReviewCount = mRecommendedReviewCount;
		state.mTotalReviewCount = mTotalReviewCount;
		state.mAverageOverallRating = mAverageOverallRating;

		state.viewedReviews = mViewedReviews;
		return state;
	}

	private void extractActivityState(ActivityState state) {
		mCurrentReviewSort = state.mCurrentReviewSort;
		mTabMap = state.tabMap;

		for (ReviewSort key : mTabMap.keySet()) {
			TabSort tab = mTabMap.get(key);
			tab.setActivity(this);
			mTabMap.put(key, tab);
		}

		mRecommendedReviewCount = state.mRecommendedReviewCount;
		mTotalReviewCount = state.mTotalReviewCount;
		mViewedReviews = state.viewedReviews;
		mAverageOverallRating = state.mAverageOverallRating;
	}

	private Message prepareMessage(boolean addFooter, ReviewSort reviewSort) {
		Object[] data = new Object[2];
		Message msg = Message.obtain(mHandler);
		data[0] = addFooter;
		data[1] = reviewSort.toString();
		msg.obj = data;
		return msg;
	}

	private void showListOrEmptyView(ViewGroup listViewContainer, UserReviewsAdapter adapter) {
		if (adapter.getCount() == 0 && mTabMap.get(mCurrentReviewSort).attemptedDownload) {
			TextView emptyTextView = (TextView) listViewContainer.findViewById(R.id.empty_text_view);
			ProgressBar progressBar = (ProgressBar) listViewContainer.findViewById(R.id.progress_bar);
			progressBar.setVisibility(View.GONE);
			String text;
			if (!NetUtils.isOnline(getApplicationContext())) {
				text = getString(R.string.widget_error_no_internet);
			}
			else if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
				text = getString(R.string.user_review_no_favorable_reviews);
			}
			else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST) {
				text = getString(R.string.user_review_no_critical_reviews);
			}
			else {
				text = getString(R.string.user_review_no_recent_reviews);
			}
			emptyTextView.setText(text);
		}
		else if (mTabMap.get(mCurrentReviewSort).attemptedDownload) {
			getListView(listViewContainer).setVisibility(View.VISIBLE);
			listViewContainer.findViewById(R.id.user_reviews_list_empty_view).setVisibility(View.GONE);
		}
	}

	private static class ActivityState {
		public ReviewSort mCurrentReviewSort;

		public int mRecommendedReviewCount;
		public int mTotalReviewCount;
		public float mAverageOverallRating;

		public HashMap<ReviewSort, TabSort> tabMap;
		public Set<String> viewedReviews;
	}

}
