package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.widget.Button;
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
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.widget.UserReviewsAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Review;
import com.mobiata.hotellib.data.ReviewRating;
import com.mobiata.hotellib.data.ReviewsResponse;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.server.ExpediaServices.ReviewSort;

public class UserReviewsListActivity extends Activity implements OnScrollListener {

	// CONSTANTS
	private static final String KEY_REVIEWS_HIGHEST = "KEY_REVEWS_HIGHEST";
	private static final String KEY_REVIEWS_LOWEST = "KEY_REVIEWS_LOWEST";
	private static final String KEY_REVIEWS_NEWEST = "KEY_REVIEWS_NEWEST";
	private static final int FAVORABLE_REVIEW_CUTOFF = 3;
	private static final int CRITICAL_REVIEW_CUTOFF = 2;
	private static final int THUMB_CUTOFF_INCLUSIVE = 5;
	private static final int BODY_LENGTH_CUTOFF = 270;

	// Views
	private SegmentedControlGroup mSortGroup;
	private LayoutInflater mLayoutInflater;
	private ViewGroup mFooterLoadingMore;

	// this handler will add/remove the loading more footer, required for accessing UI thread from other download threads
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Object[] data = (Object[]) msg.obj;
			Boolean addFooter = (Boolean) data[0];
			ReviewSort reviewSort = ReviewSort.valueOf((String) data[1]);
			ListView listView = getListView(mListViewContainersMap.get(reviewSort));
			if (addFooter) {
				listView.addFooterView(mFooterLoadingMore);
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
	private HashMap<ReviewSort, ArrayList<ReviewWrapper>> mReviewsMapWrapped = new HashMap<ReviewSort, ArrayList<ReviewWrapper>>();

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
	 * keeps track of whether an attempt has been made to start the download of a particular 
	 * review type. This helps to know whether or not to start a download (in a chained manner)
	 * for a particular review sort type
	 */
	private HashMap<ReviewSort, Boolean> mReviewSortDownloadAttemptedMap = new HashMap<ReviewSort, Boolean>();

	public HashMap<ReviewSort, Integer> mPageNumberMap = new HashMap<ReviewSort, Integer>();
	public boolean moreCriticalPages = true;
	public boolean moreFavorablePages = true;

	// Tracking data structures
	private Set<String> mViewedReviews;

	// Downloading tasks and callbacks
	private BackgroundDownloader mReviewsDownloader = BackgroundDownloader.getInstance();

	private Download mHighestRatingFirstDownload = new ReviewDownloadTask(ReviewSort.HIGHEST_RATING_FIRST);
	private OnDownloadComplete mHighestRatingFirstDownloadCallback = new ReviewDownloadCallback(
			ReviewSort.HIGHEST_RATING_FIRST);

	private Download mLowestRatingFirstDownload = new ReviewDownloadTask(ReviewSort.LOWEST_RATING_FIRST);
	private OnDownloadComplete mLowestRatingFirstDownloadCallback = new ReviewDownloadCallback(
			ReviewSort.LOWEST_RATING_FIRST);

	private Download mNewestReviewFirstDownload = new ReviewDownloadTask(ReviewSort.NEWEST_REVIEW_FIRST);
	private OnDownloadComplete mNewestReviewFirstDownloadCallback = new ReviewDownloadCallback(
			ReviewSort.NEWEST_REVIEW_FIRST);

	/**
	 * These private classes are the task/callback method for the BackgroundDownloader
	 * They exist so that code can be reused for the different sort API calls
	 */

	private class ReviewDownloadTask implements Download {

		private ReviewSort mReviewSort;

		public ReviewDownloadTask(ReviewSort sort) {
			mReviewSort = sort;
		}

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			mReviewsDownloader.addDownloadListener(KEY_REVIEWS_HIGHEST, services);
			int pageNumber = 1;

			if (mPageNumberMap.containsKey(mReviewSort)) {
				//send message to put loading footer
				mHandler.sendMessage(prepareMessage(true, mReviewSort));

				pageNumber = mPageNumberMap.get(mReviewSort).intValue();
				mPageNumberMap.put(mReviewSort, new Integer(1 + pageNumber));
			}
			else {
				mPageNumberMap.put(mReviewSort, new Integer(1 + pageNumber));
			}

			return services.reviews(mProperty, pageNumber, mReviewSort);
		}
	}

	private class ReviewDownloadCallback implements OnDownloadComplete {

		private ReviewSort thisReviewSort;

		public ReviewDownloadCallback(ReviewSort sort) {
			thisReviewSort = sort;
		}

		@Override
		public void onDownload(Object results) {
			ReviewsResponse response = (ReviewsResponse) results;
			mReviewSortDownloadAttemptedMap.put(thisReviewSort, true);

			UserReviewsAdapter adapter = mListAdaptersMap.get(thisReviewSort);
			ViewGroup listViewContainer = mListViewContainersMap.get(thisReviewSort);

			if (response != null && response.getReviewCount() > 0) {
				ArrayList<ReviewWrapper> previouslyLoadedReviewsWrapped = mReviewsMapWrapped.get(thisReviewSort);
				ArrayList<ReviewWrapper> newlyLoadedReviewsWrapped = reviewWrapperListInit(response.getReviews());
				ArrayList<ReviewWrapper> filteredReviewsWrapped = new ArrayList<ReviewWrapper>();

				boolean reviewsFiltered = false;

				for (ReviewWrapper review : newlyLoadedReviewsWrapped) {
					ReviewRating reviewRating = review.review.getRating();
					if (reviewRating != null) {
						int ratingNumber = reviewRating.getOverallSatisfaction();

						if (thisReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
							if (ratingNumber >= FAVORABLE_REVIEW_CUTOFF) {
								filteredReviewsWrapped.add(review);
								reviewsFiltered = true;
							}
							else {
								moreFavorablePages = false;
							}
						}
						else if (thisReviewSort == ReviewSort.LOWEST_RATING_FIRST) {
							if (ratingNumber <= CRITICAL_REVIEW_CUTOFF) {
								filteredReviewsWrapped.add(review);
								reviewsFiltered = true;
							}
							else {
								moreCriticalPages = false;
							}
						}
					}
				}

				if (thisReviewSort == ReviewSort.NEWEST_REVIEW_FIRST) {
					reviewsFiltered = true; // no reviews filtered for recent, set flag true
				}

				if (filteredReviewsWrapped.size() > 0 && reviewsFiltered) {
					filteredReviewsWrapped.trimToSize();
					newlyLoadedReviewsWrapped = filteredReviewsWrapped;
				}
				else if (!reviewsFiltered) {
					newlyLoadedReviewsWrapped = new ArrayList<ReviewWrapper>();
				}

				if (previouslyLoadedReviewsWrapped != null) {
					//send message to remove loading footer
					mHandler.sendMessage(prepareMessage(false, thisReviewSort));

					previouslyLoadedReviewsWrapped.addAll(newlyLoadedReviewsWrapped);
				}
				else {
					previouslyLoadedReviewsWrapped = newlyLoadedReviewsWrapped;
				}

				mReviewsMapWrapped.put(thisReviewSort, previouslyLoadedReviewsWrapped);
				adapter.switchUserReviews(previouslyLoadedReviewsWrapped);
				adapter.notifyDataSetChanged();
			}
			else {
				//send message to remove loading footer
				mHandler.sendMessage(prepareMessage(false, thisReviewSort));
			}

			if (thisReviewSort == mCurrentReviewSort) {
				bringContainerToFront(listViewContainer);
			}
			showListOrEmptyView(listViewContainer, adapter);

			// chain the downloads in the callback, if the download has not been attempted, make sure to start the download
			if (!mReviewSortDownloadAttemptedMap.get(ReviewSort.HIGHEST_RATING_FIRST)) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_HIGHEST, mHighestRatingFirstDownload,
						mHighestRatingFirstDownloadCallback);
			}
			else if (!mReviewSortDownloadAttemptedMap.get(ReviewSort.LOWEST_RATING_FIRST) && moreCriticalPages) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_LOWEST, mLowestRatingFirstDownload,
						mLowestRatingFirstDownloadCallback);
			}
			else if (!mReviewSortDownloadAttemptedMap.get(ReviewSort.NEWEST_REVIEW_FIRST)) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_NEWEST, mNewestReviewFirstDownload,
						mNewestReviewFirstDownloadCallback);
			}

		}
	}

	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_reviews_list);

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

		UserReviewsAdapter recentReviewsAdapter = new UserReviewsAdapter(mContext, mProperty);
		UserReviewsAdapter criticalReviewsAdapter = new UserReviewsAdapter(mContext, mProperty);
		UserReviewsAdapter favorableReviewsAdapter = new UserReviewsAdapter(mContext, mProperty);

		mListViewContainersMap.put(ReviewSort.HIGHEST_RATING_FIRST, favorableReviewsListViewContainer);
		mListViewContainersMap.put(ReviewSort.LOWEST_RATING_FIRST, criticalReviewsListViewContainer);
		mListViewContainersMap.put(ReviewSort.NEWEST_REVIEW_FIRST, recentReviewsListViewContainer);

		mListAdaptersMap.put(ReviewSort.HIGHEST_RATING_FIRST, favorableReviewsAdapter);
		mListAdaptersMap.put(ReviewSort.LOWEST_RATING_FIRST, criticalReviewsAdapter);
		mListAdaptersMap.put(ReviewSort.NEWEST_REVIEW_FIRST, recentReviewsAdapter);

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);

		// Load the three different lists as the adapter is being constructed
		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state == null) {
			// Initialize the ReviewSort attempted download map
			mReviewSortDownloadAttemptedMap.put(ReviewSort.HIGHEST_RATING_FIRST, false);
			mReviewSortDownloadAttemptedMap.put(ReviewSort.LOWEST_RATING_FIRST, false);
			mReviewSortDownloadAttemptedMap.put(ReviewSort.NEWEST_REVIEW_FIRST, false);

			configureHeader();

			if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_HIGHEST, mHighestRatingFirstDownload,
						mHighestRatingFirstDownloadCallback);
			}
			else if (mCurrentReviewSort == ReviewSort.NEWEST_REVIEW_FIRST) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_NEWEST, mNewestReviewFirstDownload,
						mNewestReviewFirstDownloadCallback);
			}
			else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST && moreCriticalPages) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_LOWEST, mLowestRatingFirstDownload,
						mLowestRatingFirstDownloadCallback);
			}

			mViewedReviews = new HashSet<String>();
		}
		else {
			extractActivityState(state);
			configureHeader();
			if (mReviewsMapWrapped.get(mCurrentReviewSort) != null) {
				ViewGroup listViewContainer = mListViewContainersMap.get(mCurrentReviewSort);
				UserReviewsAdapter adapter = mListAdaptersMap.get(mCurrentReviewSort);
				adapter.switchUserReviews(new ArrayList<ReviewWrapper>(mReviewsMapWrapped.get(mCurrentReviewSort)));
				adapter.notifyDataSetChanged();
				bringContainerToFront(listViewContainer);
				showListOrEmptyView(listViewContainer, adapter);
			}
			else {
				if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
					mReviewsDownloader.startDownload(KEY_REVIEWS_HIGHEST, mHighestRatingFirstDownload,
							mHighestRatingFirstDownloadCallback);
				}
				else if (mCurrentReviewSort == ReviewSort.NEWEST_REVIEW_FIRST) {
					mReviewsDownloader.startDownload(KEY_REVIEWS_NEWEST, mNewestReviewFirstDownload,
							mNewestReviewFirstDownloadCallback);
				}
				else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST && moreCriticalPages) {
					mReviewsDownloader.startDownload(KEY_REVIEWS_LOWEST, mLowestRatingFirstDownload,
							mLowestRatingFirstDownloadCallback);
				}
			}

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

		mFooterLoadingMore = (ViewGroup) mLayoutInflater.inflate(R.layout.footer_user_reviews_list_loading_more, null,
				false);

		// Configure the book now button
		Button bookNowButton = (Button) findViewById(R.id.book_now_button);
		bookNowButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent newIntent = new Intent(mContext, RoomsAndRatesListActivity.class);
				newIntent.fillIn(getIntent(), 0);
				startActivity(newIntent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(mReviewsMapWrapped.get(mCurrentReviewSort) == null) {
			if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_HIGHEST, mHighestRatingFirstDownload,
						mHighestRatingFirstDownloadCallback);
			}
			else if (mCurrentReviewSort == ReviewSort.NEWEST_REVIEW_FIRST) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_NEWEST, mNewestReviewFirstDownload,
						mNewestReviewFirstDownloadCallback);
			}
			else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST && moreCriticalPages) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_LOWEST, mLowestRatingFirstDownload,
						mLowestRatingFirstDownloadCallback);
			}
		}
	}
	
	
	@Override
	protected void onPause() {
		mReviewsDownloader.unregisterDownloadCallback(KEY_REVIEWS_HIGHEST);
		mReviewsDownloader.unregisterDownloadCallback(KEY_REVIEWS_LOWEST);
		mReviewsDownloader.unregisterDownloadCallback(KEY_REVIEWS_NEWEST);
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
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
			getListView(viewContainer).addHeaderView(header);
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
				adapter.switchUserReviews(mReviewsMapWrapped.get(mCurrentReviewSort));
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

		TextView recommendText = (TextView) findViewById(R.id.user_reviews_recommendation_tag);

		int numberRec = mProperty.getTotalRecommendations();
		int numberTotal = mProperty.getTotalReviews();
		String text = String.format(getString(R.string.user_review_recommendation_tag_text), numberRec, numberTotal);
		CharSequence styledText = Html.fromHtml(text);

		ImageView thumbView = (ImageView) findViewById(R.id.user_reviews_thumb);

		if (numberRec * 10 / numberTotal >= THUMB_CUTOFF_INCLUSIVE) {
			thumbView.setImageResource(R.drawable.review_thumbs_up);
		}
		else {
			thumbView.setImageResource(R.drawable.review_thumbs_down);
		}
		recommendText.setText(styledText);

		TextView totalReviews = (TextView) findViewById(R.id.user_review_total_reviews);
		totalReviews.setText(getResources().getQuantityString(R.plurals.number_of_reviews, numberTotal, numberTotal));

		RatingBar bottomRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar_bottom);
		bottomRatingBar.setRating((float) mProperty.getAverageExpediaRating());
	}

	// Scroll listener infinite loading implementation
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

		if (loadMore && mReviewsMapWrapped.get(mCurrentReviewSort) != null
				&& ExpediaServices.hasMoreReviews(mProperty, mPageNumberMap.get(mCurrentReviewSort))) {
			if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST && moreFavorablePages) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_HIGHEST, mHighestRatingFirstDownload,
						mHighestRatingFirstDownloadCallback);
			}
			else if (mCurrentReviewSort == ReviewSort.LOWEST_RATING_FIRST && moreCriticalPages) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_LOWEST, mLowestRatingFirstDownload,
						mLowestRatingFirstDownloadCallback);
			}
			else if (mCurrentReviewSort == ReviewSort.NEWEST_REVIEW_FIRST) {
				mReviewsDownloader.startDownload(KEY_REVIEWS_NEWEST, mNewestReviewFirstDownload,
						mNewestReviewFirstDownloadCallback);
			}
		}

		// Track which reviews are visible, add them to the list
		ListAdapter adapter = view.getAdapter();
		for (int a = 0; a < visibleItemCount; a++) {
			Object item = adapter.getItem(firstVisibleItem + a);
			if (item instanceof ReviewWrapper) {
				mViewedReviews.add(((ReviewWrapper) item).review.getReviewId());
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

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
		state.mCurrentReviewSort = mCurrentReviewSort;

		UserReviewsAdapter adapter = mListAdaptersMap.get(mCurrentReviewSort);
		// make sure to save the read more state from the adapter
		mReviewsMapWrapped.put(mCurrentReviewSort, adapter.mLoadedReviews);
		state.reviewsMapWrapped = mReviewsMapWrapped;
		state.reviewsAttemptDownloadMap = mReviewSortDownloadAttemptedMap;
		state.pageNumberMap = mPageNumberMap;
		state.moreCriticalPages = moreCriticalPages;
		state.moreFavorablePages = moreFavorablePages;
		state.viewedReviews = mViewedReviews;
		return state;
	}

	private void extractActivityState(ActivityState state) {
		mCurrentReviewSort = state.mCurrentReviewSort;
		mReviewsMapWrapped = state.reviewsMapWrapped;
		mReviewSortDownloadAttemptedMap = state.reviewsAttemptDownloadMap;
		mPageNumberMap = state.pageNumberMap;
		moreCriticalPages = state.moreCriticalPages;
		moreFavorablePages = state.moreFavorablePages;
		mViewedReviews = state.viewedReviews;
	}

	private Message prepareMessage(boolean addFooter, ReviewSort reviewSort) {
		Object[] data = new Object[2];
		Message msg = new Message();
		data[0] = addFooter;
		data[1] = reviewSort.toString();
		msg.obj = data;
		return msg;
	}

	private void showListOrEmptyView(ViewGroup listViewContainer, UserReviewsAdapter adapter) {
		if (adapter.getCount() == 0 && mReviewSortDownloadAttemptedMap.get(mCurrentReviewSort)) {
			TextView emptyTextView = (TextView) listViewContainer.findViewById(R.id.empty_text_view);
			ProgressBar progressBar = (ProgressBar) listViewContainer.findViewById(R.id.progress_bar);
			progressBar.setVisibility(View.GONE);
			String text;
			if (mCurrentReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
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
		else if (mReviewSortDownloadAttemptedMap.get(mCurrentReviewSort)) {
			getListView(listViewContainer).setVisibility(View.VISIBLE);
			listViewContainer.findViewById(R.id.user_reviews_list_empty_view).setVisibility(View.GONE);
		}
	}

	private class ActivityState {
		public ReviewSort mCurrentReviewSort;
		public HashMap<ReviewSort, ArrayList<ReviewWrapper>> reviewsMapWrapped;
		public HashMap<ReviewSort, Boolean> reviewsAttemptDownloadMap;
		public HashMap<ReviewSort, Integer> pageNumberMap;
		public boolean moreCriticalPages;
		public boolean moreFavorablePages;
		public Set<String> viewedReviews;
	}

}
