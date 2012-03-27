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
	@SuppressWarnings("serial")
	private static final Map<ReviewSort, Integer> SORT_NO_RESULTS_MESSAGE = new HashMap<ReviewSort, Integer>() {
		{
			put(ReviewSort.NEWEST_REVIEW_FIRST, R.string.user_review_no_recent_reviews);
			put(ReviewSort.HIGHEST_RATING_FIRST, R.string.user_review_no_favorable_reviews);
			put(ReviewSort.LOWEST_RATING_FIRST, R.string.user_review_no_critical_reviews);
		}
	};
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
	private ReviewSort mCurrentReviewSort = ReviewSort.NEWEST_REVIEW_FIRST;

	// Stores the counts retrieved from BazaarVoice using FilteredStats param
	private int mTotalReviewCount;
	private int mRecommendedReviewCount;
	private float mAverageOverallRating;

	private boolean mHasReviewStats = false;

	/* 
	 * keeps a mapping of the different containers holding the list views (and empty views) for each review sort type
	 * 8605: keeping different list views (backed by corresponding adapter) helps preserve list position across orientation changes
	 */
	private Map<ReviewSort, ViewGroup> mListViewContainersMap = new HashMap<ReviewSort, ViewGroup>();

	/* 
	 * keeps a mapping of the different adapters maintaining data for each of their corresponding list views.
	 * when a user navigates from one sort type to the next, the appropriate list view is made visible.
	 * the adapters are updated with data in the background to ensure that each list maintaints its own data
	 */
	private Map<ReviewSort, UserReviewsAdapter> mListAdaptersMap = new HashMap<ReviewSort, UserReviewsAdapter>();

	/*
	 * keeps track of whether the loading indicator is showing as the footer in the list view.
	 * This bookkeeping is not preserved across orientation change so as to re-show the loading
	 * indicator on orientation change if there are more reviews to display
	 */
	private Set<ReviewSort> mIsLoadingIndicatorShowingForReviewSort = new HashSet<ReviewSort>();

	/*
	 * this map keeps track of different sorts, that are stored in separate tabs
	 */
	private Map<ReviewSort, TabSort> mTabMap;

	// Tracking data structures
	private Set<String> mViewedReviews;

	// Network classes
	private BackgroundDownloader mReviewsDownloader = BackgroundDownloader.getInstance();
	private ExpediaServices mExpediaServices;

	/////////////////////////////////////////////////////////////////////////////////////
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
			for (ReviewSort sort : SORT_BGDL_KEY.keySet()) {
				mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(sort));
			}

			configureHeader();

			mViewedReviews = new HashSet<String>();
		}
		else {
			extractActivityState(state);
			configureHeader();
			updateReviewNumbers();

			ViewGroup listViewContainer = mListViewContainersMap.get(mCurrentReviewSort);
			UserReviewsAdapter adapter = mListAdaptersMap.get(mCurrentReviewSort);

			adapter.setUserReviews(new ArrayList<ReviewWrapper>(mTabMap.get(mCurrentReviewSort).mReviewsWrapped));
			adapter.notifyDataSetChanged();

			bringContainerToFront(listViewContainer);
			showListOrEmptyView(mCurrentReviewSort, listViewContainer, adapter);
		}

		if (!mHasReviewStats) {
			mHandler.post(mReviewStatisticsDownloadTask);
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

	@Override
	protected void onResume() {
		super.onResume();
		mReviewsDownloader.registerDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD, mReviewStatisticsDownloadCallback);
		for (ReviewSort sort : SORT_BGDL_KEY.keySet()) {
			mReviewsDownloader.registerDownloadCallback(SORT_BGDL_KEY.get(sort), mTabMap.get(sort).mDownloadCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mReviewsDownloader.unregisterDownloadCallback(REVIEWS_STATISTICS_DOWNLOAD);

		for (ReviewSort sort : SORT_BGDL_KEY.keySet()) {
			mReviewsDownloader.unregisterDownloadCallback(SORT_BGDL_KEY.get(sort));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			for (ReviewSort sort : SORT_BGDL_KEY.keySet()) {
				mReviewsDownloader.cancelDownload(SORT_BGDL_KEY.get(sort));
			}

			// Track # of reviews seen
			int numReviewsSeen = mViewedReviews.size();
			Log.d("Tracking # of reviews seen: " + numReviewsSeen);
			String referrerId = "App.Hotels.Reviews." + numReviewsSeen + "ReviewsViewed";
			TrackingUtils.trackSimpleEvent(this, null, null, "Shopper", referrerId);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// OnScrollListener interface implementation

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
		boolean hasMore = false;

		LinkedList<ReviewLanguageSet> list = mTabMap.get(mCurrentReviewSort).mLanguageList;

		if (list != null && list.size() > 0) {
			hasMore = true;
		}

		if (loadMore && hasMore && mHasReviewStats) {
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
			if (!mIsLoadingIndicatorShowingForReviewSort.contains(mCurrentReviewSort)) {
				// add a divider ?
				UserReviewsAdapter adapter = mListAdaptersMap.get(mCurrentReviewSort);
				if (adapter.mAddDivider) {
					adapter.addDivider();
					adapter.mAddDivider = false;
				}

				// send message to put loading footer
				mHandler.sendMessage(prepareMessage(true, mCurrentReviewSort));
				mIsLoadingIndicatorShowingForReviewSort.add(mCurrentReviewSort);
			}
		}

		// Track which reviews are visible, add them to the list
		ListAdapter adapter = view.getAdapter();
		int count = adapter.getCount();
		for (int a = 0; a < visibleItemCount && firstVisibleItem + a < count; a++) {
			Object item = adapter.getItem(firstVisibleItem + a);
			if (item instanceof ReviewWrapper && !((ReviewWrapper) item).mIsDivider) {
				mViewedReviews.add(((ReviewWrapper) item).mReview.getReviewId());
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// Private helper methods

	private void ensureExpediaServicesCacheFilled() {
		if (mExpediaServices == null) {
			mExpediaServices = new ExpediaServices(mContext);
		}
	}

	/**
	 * Create the message to be sent to the handler from the DL callback
	 * @param addFooter - true if footer should be added, false if footer should be removed
	 * @param reviewSort - which ListView to perform add/remove the footer
	 * @return
	 */
	private Message prepareMessage(boolean addFooter, ReviewSort reviewSort) {
		Object[] data = new Object[2];
		Message msg = Message.obtain(mHandler);
		data[0] = addFooter;
		data[1] = reviewSort.toString();
		msg.obj = data;
		return msg;
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

	private void configureHeader() {
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
				showListOrEmptyView(mCurrentReviewSort, listViewContainer, adapter);
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
	private void updateReviewNumbers() {
		if (mHasReviewStats) {
			for (ViewGroup viewContainer : mListViewContainersMap.values()) {
				TextView recommendText = (TextView) viewContainer.findViewById(R.id.user_reviews_recommendation_tag);

				String text = String.format(getString(R.string.user_review_recommendation_tag_text),
						mRecommendedReviewCount, mTotalReviewCount);
				CharSequence styledText = Html.fromHtml(text);

				ImageView thumbView = (ImageView) findViewById(R.id.user_reviews_thumb);

				if (mTotalReviewCount > 0) {
					if (mRecommendedReviewCount * 10 / mTotalReviewCount >= THUMB_CUTOFF_INCLUSIVE) {
						thumbView.setImageResource(R.drawable.review_thumbs_up);
					}
					else {
						thumbView.setImageResource(R.drawable.review_thumbs_down);
					}
					recommendText.setText(styledText);
				}
			}
		}
		configureBottomBar();
	}

	/**
	 * Configure the bottom bar with the book now button
	 * @param visible - whether or not to set the bottom bar visible or not
	 */
	private void configureBottomBar() {
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

		}
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// Reviews model + controller stuff

	/**
	 * This class is used to encapsulate all of the data + network controls pertaining to a given review sort
	 * I had originally designed this activity to store all of this data in separate maps. Unfortunately, this 
	 * started to get out of control as more and more data needed to be stored. Instead of having multiple maps
	 * to store all of these data points, there is now one map that stores instances of this class (which store
	 * the data points)
	 * 
	 * Note: This class is declared static for a very important reason that may not be obvious to all. Instances of
	 * TabSort are stored across orientation changes because it stores all of the reviews, etc... Additionally, this
	 * inner class access objects from its enclosing class, UserReviewsListActivity. By declaring the class as static,
	 * the enclosing class must be explicitly declared, and updated across orientation changes in order to access the
	 * correct objects from the enclosing class.
	 * 
	 * @author brad
	 *
	 */
	private static class TabSort {

		public UserReviewsListActivity mActivity;

		public ReviewSort mReviewSort;
		public LinkedList<ReviewLanguageSet> mLanguageList;
		public ArrayList<ReviewWrapper> mReviewsWrapped;

		public String mStatusMessage;

		public boolean mAttemptedDownload;

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
			this.mAttemptedDownload = false;
		}

		/**
		 * Call this function after orientation change in order to refer to the correct activity instance
		 * @param activity - current activity, enclosing class
		 */
		public void setActivity(UserReviewsListActivity activity) {
			this.mActivity = activity;
		}

		public class ReviewDownloadTask implements Download {

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
				LinkedList<String> languages = meta.getLanguageCodes();

				return mActivity.mExpediaServices.reviews(mActivity.mProperty, mReviewSort, pageNumber, languages);
			}
		}

		public class ReviewDownloadCallback implements OnDownloadComplete {

			public ReviewDownloadCallback() {
			}

			@Override
			public void onDownload(Object results) {
				ReviewsResponse response = (ReviewsResponse) results;

				ReviewLanguageSet rls;

				// update attempted download flag
				if (mLanguageList != null && mLanguageList.size() > 0) {
					// 13012 - droid2 does not have version of java with LinkedList.pop/push in the impl
					// We were doing a pop, then push anyways, this is better
					rls = mLanguageList.getLast();
					rls.setAttemptedDownload(true);
				}
				mAttemptedDownload = true;

				UserReviewsAdapter adapter = mActivity.mListAdaptersMap.get(mReviewSort);
				ViewGroup listViewContainer = mActivity.mListViewContainersMap.get(mReviewSort);

				if (response != null) {

					if (response.hasErrors()) {
						mStatusMessage = mActivity.getResources().getString(R.string.user_review_unavailable);

						mActivity.mIsLoadingIndicatorShowingForReviewSort.remove(mReviewSort);
						if (mReviewSort == mActivity.mCurrentReviewSort) {
							mActivity.bringContainerToFront(listViewContainer);
						}
						mActivity.showListOrEmptyView(mReviewSort, listViewContainer, adapter);

					}
					else {
						// grab the meta object from the list within the map for modification(s)
						// 13012 - droid2 does not have version of java with LinkedList.pop/push in the impl
						rls = mLanguageList.getLast();
						mLanguageList.removeLast();

						if (response.getReviewCount() > 0) {
							rls.setTotalCount(response.getTotalCount());
							ArrayList<ReviewWrapper> newlyLoadedReviewsWrapped = mActivity
									.reviewWrapperListInit(response
											.getReviews());

							rls.incrementPageNumber();

							// push object back if there are more pages to download
							if (rls.hasMore()) {
								// 13012 - droid2 does not have version of java with LinkedList.pop/push in the impl
								mLanguageList.addLast(rls);
							}
							else {
								adapter.mAddDivider = true;
							}

							// append the new reviews to old collection, remove loading view, refresh
							mReviewsWrapped.addAll(newlyLoadedReviewsWrapped);

							//send message to remove loading footer
							mActivity.mHandler.sendMessage(mActivity.prepareMessage(false, mReviewSort));

							adapter.setUserReviews(mReviewsWrapped);

							mActivity.mIsLoadingIndicatorShowingForReviewSort.remove(mReviewSort);
							if (mReviewSort == mActivity.mCurrentReviewSort) {
								mActivity.bringContainerToFront(listViewContainer);
							}
							mActivity.showListOrEmptyView(mReviewSort, listViewContainer, adapter);

						}
						else {
							// there are no reviews in the response, only display results if there are no more to attempt to DL
							if (mLanguageList == null || (mLanguageList != null && mLanguageList.size() < 1)) {
								mStatusMessage = mActivity.getResources().getString(
										SORT_NO_RESULTS_MESSAGE.get(mReviewSort));
								mActivity.mIsLoadingIndicatorShowingForReviewSort.remove(mReviewSort);
								if (mReviewSort == mActivity.mCurrentReviewSort) {
									mActivity.bringContainerToFront(listViewContainer);
								}
								mActivity.showListOrEmptyView(mReviewSort, listViewContainer, adapter);
							}

							// remove divider
							adapter.removeDivider();

							// send message to remove loading footer
							mActivity.mHandler.sendMessage(mActivity.prepareMessage(false, mReviewSort));
						}
					}

				}
				else {
					//send message to remove loading footer
					mActivity.mHandler.sendMessage(mActivity.prepareMessage(false, mReviewSort));

					if (response == null || response.hasErrors()) {
						TrackingUtils.trackErrorPage(mActivity.mContext, "UserReviewLoadFailed");
					}

					mActivity.mIsLoadingIndicatorShowingForReviewSort.remove(mReviewSort);
					if (mReviewSort == mActivity.mCurrentReviewSort) {
						mActivity.bringContainerToFront(listViewContainer);
					}
					mActivity.showListOrEmptyView(mReviewSort, listViewContainer, adapter);
				}

				// chain the downloads in the callback, if the download has not been attempted, make sure to start the download
				String key;
				TabSort nextTab;
				if (!mActivity.mTabMap.get(ReviewSort.NEWEST_REVIEW_FIRST).mAttemptedDownload
						&& !mActivity.mReviewsDownloader.isDownloading(SORT_BGDL_KEY
								.get(ReviewSort.NEWEST_REVIEW_FIRST))) {
					key = SORT_BGDL_KEY.get(ReviewSort.NEWEST_REVIEW_FIRST);
					nextTab = mActivity.mTabMap.get(ReviewSort.NEWEST_REVIEW_FIRST);
					mActivity.mReviewsDownloader.startDownload(key, nextTab.mDownloadTask, nextTab.mDownloadCallback);
				}
				else if (!mActivity.mTabMap.get(ReviewSort.HIGHEST_RATING_FIRST).mAttemptedDownload
						&& !mActivity.mReviewsDownloader.isDownloading(SORT_BGDL_KEY
								.get(ReviewSort.HIGHEST_RATING_FIRST))) {
					key = SORT_BGDL_KEY.get(ReviewSort.HIGHEST_RATING_FIRST);
					nextTab = mActivity.mTabMap.get(ReviewSort.HIGHEST_RATING_FIRST);
					mActivity.mReviewsDownloader.startDownload(key, nextTab.mDownloadTask, nextTab.mDownloadCallback);
				}
				else if (!mActivity.mTabMap.get(ReviewSort.LOWEST_RATING_FIRST).mAttemptedDownload
						&& !mActivity.mReviewsDownloader.isDownloading(SORT_BGDL_KEY
								.get(ReviewSort.LOWEST_RATING_FIRST))) {
					key = SORT_BGDL_KEY.get(ReviewSort.LOWEST_RATING_FIRST);
					nextTab = mActivity.mTabMap.get(ReviewSort.LOWEST_RATING_FIRST);
					mActivity.mReviewsDownloader.startDownload(key, nextTab.mDownloadTask, nextTab.mDownloadCallback);
				}
			}
		}
	}

	/**
	 * Helper class that constructs the map of the three different TabSort objects
	 * @return
	 */
	private HashMap<ReviewSort, TabSort> getTabMap() {
		HashMap<ReviewSort, TabSort> map = new HashMap<ReviewSort, TabSort>();

		LinkedList<String> languages = LocaleUtils.getLanguages(mContext);

		// RECENT SORT ORDER
		LinkedList<ReviewLanguageSet> recentReviewLanguageSet = new LinkedList<ReviewLanguageSet>();
		for (String languageCode : languages) {
			ReviewLanguageSet rls = new ReviewLanguageSet();
			LinkedList<String> codes = new LinkedList<String>();
			codes.add(languageCode);
			rls.addLanguageCodes(codes);
			recentReviewLanguageSet.add(rls);
		}

		TabSort recentTabSort = new TabSort(this, ReviewSort.NEWEST_REVIEW_FIRST, recentReviewLanguageSet);

		map.put(ReviewSort.NEWEST_REVIEW_FIRST, recentTabSort);

		// FAVORABLE SORT ORDER
		LinkedList<ReviewLanguageSet> favorableReviewLanguageSet = new LinkedList<ReviewLanguageSet>();
		ReviewLanguageSet frls = new ReviewLanguageSet();
		frls.addLanguageCodes(languages);
		favorableReviewLanguageSet.add(frls);

		TabSort favorableTabSort = new TabSort(this, ReviewSort.HIGHEST_RATING_FIRST, favorableReviewLanguageSet);

		map.put(ReviewSort.HIGHEST_RATING_FIRST, favorableTabSort);

		// CRITICAL SORT ORDER
		LinkedList<ReviewLanguageSet> criticalReviewLanguageSet = new LinkedList<ReviewLanguageSet>();
		ReviewLanguageSet crls = new ReviewLanguageSet();
		crls.addLanguageCodes(languages);
		criticalReviewLanguageSet.add(crls);

		TabSort criticalTabSort = new TabSort(this, ReviewSort.LOWEST_RATING_FIRST, criticalReviewLanguageSet);

		map.put(ReviewSort.LOWEST_RATING_FIRST, criticalTabSort);

		return map;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// Reviews statistics code

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
				if (!response.hasErrors()) {
					mRecommendedReviewCount = response.getRecommendedCount();
					mTotalReviewCount = response.getTotalReviewCount();
					mAverageOverallRating = response.getAverageOverallRating();
					mHasReviewStats = true;

					if (mTotalReviewCount == 0) {
						for (ReviewSort sort : mListViewContainersMap.keySet()) {
							TabSort tab = mTabMap.get(sort);

							// force this flag to true so that error message displaying code will support this exception to the rule
							tab.mAttemptedDownload = true;
							tab.mStatusMessage = getResources().getString(
									SORT_NO_RESULTS_MESSAGE.get(sort));
							ViewGroup listViewContainer = mListViewContainersMap.get(sort);
							UserReviewsAdapter adapter = mListAdaptersMap.get(sort);
							showListOrEmptyView(sort, listViewContainer, adapter);
						}
					}
				}
				else {
					for (ReviewSort sort : mListViewContainersMap.keySet()) {
						TabSort tab = mTabMap.get(sort);

						// force this flag to true so that error message displaying code will support this exception to the rule
						tab.mAttemptedDownload = true;
						tab.mStatusMessage = mContext.getResources().getString(R.string.user_review_unavailable);

						ViewGroup listViewContainer = mListViewContainersMap.get(sort);
						UserReviewsAdapter adapter = mListAdaptersMap.get(sort);
						showListOrEmptyView(sort, listViewContainer, adapter);
					}
				}

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

	/////////////////////////////////////////////////////////////////////////////////////
	// ReviewWrapper meta data

	/**
	 * Wrapper class that stores the review and also extra book-keeping related to how it is displayed
	 * from within the applications, such as whether or not the review is expanded, and the reduced body
	 * @author brad
	 *
	 */
	public static class ReviewWrapper {
		public Review mReview;
		public boolean mBodyWasReduced;
		public boolean mIsDisplayingFull;
		public String mBodyReduced;

		public boolean mIsDivider = false;

		public ReviewWrapper() {
			mIsDivider = false;
		}

		public ReviewWrapper(boolean isDivider) {
			mIsDivider = isDivider;
		}
	}

	private ArrayList<ReviewWrapper> reviewWrapperListInit(List<Review> reviews) {
		ArrayList<ReviewWrapper> loadedReviews = new ArrayList<ReviewWrapper>();
		if (reviews == null) {
			return null;
		}
		for (Review review : reviews) {
			ReviewWrapper loadedReview = new ReviewWrapper();
			loadedReview.mReview = review;

			String body = review.getBody();
			if (body.length() > BODY_LENGTH_CUTOFF) {
				loadedReview.mBodyReduced = body.substring(0, BODY_LENGTH_CUTOFF);
				loadedReview.mBodyReduced += "...";
				loadedReview.mBodyWasReduced = true;
			}
			else {
				loadedReview.mBodyWasReduced = false;
			}

			loadedReview.mIsDisplayingFull = false;
			loadedReviews.add(loadedReview);
		}
		return loadedReviews;
	}

	/**
	 * The purpose of this class is to contain all of the bookkeeping related to the paging of reviews
	 * for a priority list of languages. For instance, using the language priority algorithm for the recent
	 * sort order, this instances of this class will store the pageNumber, the totalCount, localeCode
	 * 
	 * The UserReviewsListActivity will create a list of ReviewLanguageSet objects that are relevant to its POS
	 * and device language. Some POS will have only one object in its list, if there is no priority exhibited
	 * in the Expedia behavior. This makes the implementation extensible for all configurations that Expedia could
	 * possibly throw our way.
	 * 
	 * @author brad
	 *
	 */
	public static class ReviewLanguageSet {

		private LinkedList<String> mLanguageCodes;

		private int mTotalCount;
		private int mPageNumber;
		private boolean mAttemptedDownload;

		public ReviewLanguageSet() {
			this.mLanguageCodes = new LinkedList<String>();
			this.mPageNumber = 0;
			this.mAttemptedDownload = false;
		}

		/**
		 * Function returns true if there are more reviews to be requested, i.e. another network call should be made
		 */
		public boolean hasMore() {
			if (mPageNumber * ExpediaServices.REVIEWS_PER_PAGE >= mTotalCount) {
				return false;
			}
			return true;
		}

		public void setTotalCount(int count) {
			this.mTotalCount = count;
		}

		public void addLanguageCodes(LinkedList<String> codes) {
			mLanguageCodes.addAll(codes);
		}

		public int getPageNumber() {
			return mPageNumber;
		}

		public void incrementPageNumber() {
			mPageNumber++;
		}

		public LinkedList<String> getLanguageCodes() {
			return mLanguageCodes;
		}

		public boolean getAttemptedDownload() {
			return mAttemptedDownload;
		}

		public void setAttemptedDownload(boolean attempted) {
			this.mAttemptedDownload = attempted;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////
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
		state.mHasReviewStats = mHasReviewStats;

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
		mAverageOverallRating = state.mAverageOverallRating;
		mHasReviewStats = state.mHasReviewStats;

		mViewedReviews = state.viewedReviews;
	}

	private void showListOrEmptyView(ReviewSort sort, ViewGroup listViewContainer, UserReviewsAdapter adapter) {
		if (adapter.getCount() == 0 && mTabMap.get(mCurrentReviewSort).mAttemptedDownload) {
			TextView emptyTextView = (TextView) listViewContainer.findViewById(R.id.empty_text_view);
			ProgressBar progressBar = (ProgressBar) listViewContainer.findViewById(R.id.progress_bar);
			progressBar.setVisibility(View.GONE);
			String text;
			if (!NetUtils.isOnline(getApplicationContext())) {
				text = getString(R.string.widget_error_no_internet);
			}
			else {
				text = mTabMap.get(sort).mStatusMessage;
			}
			emptyTextView.setText(text);
		}
		else if (mTabMap.get(mCurrentReviewSort).mAttemptedDownload) {
			getListView(listViewContainer).setVisibility(View.VISIBLE);
			listViewContainer.findViewById(R.id.user_reviews_list_empty_view).setVisibility(View.GONE);
		}
	}

	private static class ActivityState {
		public ReviewSort mCurrentReviewSort;

		public int mRecommendedReviewCount;
		public int mTotalReviewCount;
		public float mAverageOverallRating;
		public boolean mHasReviewStats;

		public Map<ReviewSort, TabSort> tabMap;
		public Set<String> viewedReviews;
	}

}
