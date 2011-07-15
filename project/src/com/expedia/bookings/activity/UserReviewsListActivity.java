package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.UserReviewsAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Review;
import com.mobiata.hotellib.data.ReviewRating;
import com.mobiata.hotellib.data.ReviewsResponse;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.server.ExpediaServices.ReviewSort;
import com.mobiata.hotellib.utils.JSONUtils;

public class UserReviewsListActivity extends ListActivity implements OnScrollListener {

	// CONSTANTS
	private static final String KEY_REVIEWS_HIGHEST = "KEY_REVEWS_HIGHEST";
	private static final String KEY_REVIEWS_LOWEST = "KEY_REVIEWS_LOWEST";
	private static final String KEY_REVIEWS_NEWEST = "KEY_REVIEWS_NEWEST";
	private static final int FAVORABLE_REVIEW_CUTOFF = 3;
	private static final int CRITICAL_REVIEW_CUTOFF = 2;
	private static final int THUMB_CUTOFF_INCLUSIVE = 5;

	// Views
	private ListView mUserReviewsListView;
	private UserReviewsAdapter mAdapter;
	private SegmentedControlGroup mSortGroup;
	private LayoutInflater mLayoutInflater;
	private ViewGroup mFooterLoadingMore;

	private Handler mHandler;

	// Member variables
	private Context mContext;
	private Property mProperty;

	// Review data structures
	private ReviewSort mCurrentReviewSort = ReviewSort.NEWEST_REVIEW_FIRST;
	private HashMap<ReviewSort, ArrayList<Review>> mReviewsMap = new HashMap<ReviewSort, ArrayList<Review>>();
	private HashMap<ReviewSort, Boolean> mReviewsAttemptDownloadMap = new HashMap<ReviewSort, Boolean>();
	public HashMap<ReviewSort, Integer> mPageNumberMap = new HashMap<ReviewSort, Integer>();
	public boolean moreCriticalPages = true;
	public boolean moreFavorablePages = true;

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
				Message msg = new Message();
				boolean addFooter = true;
				msg.obj = addFooter;
				mHandler.sendMessage(msg);

				pageNumber = mPageNumberMap.get(mReviewSort).intValue();
				mPageNumberMap.put(mReviewSort, new Integer(1 + pageNumber));
				return services.reviews(mProperty, pageNumber, mReviewSort);
			}
			else {
				mPageNumberMap.put(mReviewSort, new Integer(1 + pageNumber));
				return services.reviews(mProperty, pageNumber, mReviewSort);
			}
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
			mReviewsAttemptDownloadMap.put(thisReviewSort, true);

			if (response != null && response.getReviewCount() > 0) {
				ArrayList<Review> previouslyLoadedReviews = mReviewsMap.get(thisReviewSort);
				ArrayList<Review> newlyLoadedReviews = new ArrayList<Review>(response.getReviews());
				ArrayList<Review> filteredReviews = new ArrayList<Review>();

				boolean reviewsFiltered = false;

				for (Review review : newlyLoadedReviews) {
					ReviewRating reviewRating = review.getRating();
					if (reviewRating != null) {
						int ratingNumber = reviewRating.getOverallSatisfaction();

						if (thisReviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
							if (ratingNumber >= FAVORABLE_REVIEW_CUTOFF) {
								filteredReviews.add(review);
								reviewsFiltered = true;
							}
							else {
								moreFavorablePages = false;
							}
						}
						else if (thisReviewSort == ReviewSort.LOWEST_RATING_FIRST) {
							if (ratingNumber <= CRITICAL_REVIEW_CUTOFF) {
								filteredReviews.add(review);
								reviewsFiltered = true;
							}
							else {
								moreCriticalPages = false;
							}
						}
					}
				}

				if (thisReviewSort == ReviewSort.NEWEST_REVIEW_FIRST) {
					reviewsFiltered = true;
				}

				if (filteredReviews.size() > 0 && reviewsFiltered) {
					filteredReviews.trimToSize();
					newlyLoadedReviews = filteredReviews;
				}
				else if (!reviewsFiltered) {
					newlyLoadedReviews = new ArrayList<Review>();
				}

				if (previouslyLoadedReviews != null) {
					//send message to remove loading footer
					Message msg = new Message();
					boolean addFooter = false;
					msg.obj = addFooter;
					mHandler.sendMessage(msg);

					previouslyLoadedReviews.addAll(newlyLoadedReviews);
				}
				else {
					previouslyLoadedReviews = newlyLoadedReviews;
				}

				mReviewsMap.put(thisReviewSort, previouslyLoadedReviews);
				if (thisReviewSort == mCurrentReviewSort) {
					mAdapter.switchUserReviews(previouslyLoadedReviews);
					setNoReviewsText();
				}

				if (mReviewsMap.get(ReviewSort.HIGHEST_RATING_FIRST) == null) {
					mReviewsDownloader.startDownload(KEY_REVIEWS_HIGHEST, mHighestRatingFirstDownload,
							mHighestRatingFirstDownloadCallback);
				}
				else if (mReviewsMap.get(ReviewSort.LOWEST_RATING_FIRST) == null && moreCriticalPages) {
					mReviewsDownloader.startDownload(KEY_REVIEWS_LOWEST, mLowestRatingFirstDownload,
							mLowestRatingFirstDownloadCallback);
				}
				else if (mReviewsMap.get(ReviewSort.NEWEST_REVIEW_FIRST) == null) {
					mReviewsDownloader.startDownload(KEY_REVIEWS_NEWEST, mNewestReviewFirstDownload,
							mNewestReviewFirstDownloadCallback);
				}
			}
			else {
				//send message to remove loading footer
				Message msg = new Message();
				boolean addFooter = false;
				msg.obj = addFooter;
				mHandler.sendMessage(msg);
			}
		}
	}

	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_reviews_list);

		mContext = this;
		mUserReviewsListView = getListView();

		// Retrieve data to build this with
		final Intent intent = getIntent();
		mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);

		// Initialize the ReviewSort attempted download map
		mReviewsAttemptDownloadMap.put(ReviewSort.HIGHEST_RATING_FIRST, false);
		mReviewsAttemptDownloadMap.put(ReviewSort.LOWEST_RATING_FIRST, false);
		mReviewsAttemptDownloadMap.put(ReviewSort.NEWEST_REVIEW_FIRST, false);

		// Load the three different lists as the adapter is being constructed
		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state == null) {
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
			mAdapter = new UserReviewsAdapter(mContext, mProperty);
			setListAdapter(mAdapter);
		}
		else {
			extractActivityState(state);
			configureHeader();
			if (mReviewsMap.get(mCurrentReviewSort) != null) {
				mAdapter = new UserReviewsAdapter(mContext, mProperty);
				setListAdapter(mAdapter);
				mAdapter.addUserReviews(new ArrayList<Review>(mReviewsMap.get(mCurrentReviewSort)));
				mAdapter.notifyDataSetChanged();
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
				mAdapter = new UserReviewsAdapter(mContext, mProperty);
				setListAdapter(mAdapter);
			}

		}
		mUserReviewsListView.setOnScrollListener(this);

		mFooterLoadingMore = (ViewGroup) mLayoutInflater.inflate(R.layout.footer_user_reviews_list_loading_more,
				mUserReviewsListView, false);

		// this handler will add/remove the loading more footer, required for accessing UI thread from other download threads
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				boolean addFooter = (Boolean) msg.obj;
				if (addFooter) {
					mUserReviewsListView.addFooterView(mFooterLoadingMore);
				}
				else {
					mUserReviewsListView.removeFooterView(mFooterLoadingMore);
				}
			}
		};

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

	public void configureHeader() {
		if (mLayoutInflater == null) {
			mLayoutInflater = getLayoutInflater();
		}

		ViewGroup header = (ViewGroup) mLayoutInflater.inflate(R.layout.header_user_reviews_list, mUserReviewsListView,
				false);
		mUserReviewsListView.addHeaderView(header);

		mSortGroup = (SegmentedControlGroup) findViewById(R.id.user_review_sort_group);
		mSortGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.user_review_button_recent:
					mCurrentReviewSort = ReviewSort.NEWEST_REVIEW_FIRST;
					break;
				case R.id.user_review_button_favorable:
					mCurrentReviewSort = ReviewSort.HIGHEST_RATING_FIRST;
					break;
				case R.id.user_review_button_critical:
					mCurrentReviewSort = ReviewSort.LOWEST_RATING_FIRST;
					break;
				}
				// A new adapter is set to so that the list scrolls to the top upon switch
				mAdapter = new UserReviewsAdapter(mContext, mProperty);
				setListAdapter(mAdapter);
				mAdapter.switchUserReviews(mReviewsMap.get(mCurrentReviewSort));
				setNoReviewsText();
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
		String totalReviewsText = String.format(getString(R.string.user_review_total_reviews), numberTotal);
		CharSequence styledTotalText = Html.fromHtml(totalReviewsText);
		totalReviews.setText(styledTotalText);

		RatingBar bottomRatingBar = (RatingBar) findViewById(R.id.user_review_rating_bar_bottom);
		bottomRatingBar.setRating((float) mProperty.getAverageExpediaRating());
	}

	public void setNoReviewsText() {
		if (mAdapter.getCount() == 0 && mReviewsAttemptDownloadMap.get(mCurrentReviewSort)) {
			TextView emptyTextView = (TextView) findViewById(R.id.empty_text_view);
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
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
	}

	// Scroll listener infinite loading implementation

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

		if (loadMore && mReviewsMap.get(mCurrentReviewSort) != null
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
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
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
		state.reviewsMap = mReviewsMap;
		state.pageNumberMap = mPageNumberMap;
		state.moreCriticalPages = moreCriticalPages;
		state.moreFavorablePages = moreFavorablePages;
		return state;
	}

	private void extractActivityState(ActivityState state) {
		mCurrentReviewSort = state.mCurrentReviewSort;
		mReviewsMap = state.reviewsMap;
		mPageNumberMap = state.pageNumberMap;
		moreCriticalPages = state.moreCriticalPages;
		moreFavorablePages = state.moreFavorablePages;
	}

	private class ActivityState {
		public ReviewSort mCurrentReviewSort;
		public HashMap<ReviewSort, ArrayList<Review>> reviewsMap;
		public HashMap<ReviewSort, Integer> pageNumberMap;
		public boolean moreCriticalPages;
		public boolean moreFavorablePages;
	}
}
