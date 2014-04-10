package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewSort;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.expedia.bookings.widget.UserReviewsAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.Ui;

public class UserReviewsFragment extends ListFragment implements OnScrollListener {

	// Constants
	private static String REVIEWS_DOWNLOAD_KEY_PREFIX = "com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsDownload.";
	private static final int BODY_LENGTH_CUTOFF = 270;

	// Bundle strings
	private static final String ARGUMENT_SORT_STRING = "ARGUMENT_SORT_STRING";
	private static final String INSTANCE_ATTEMPTED_DOWNLOAD = "INSTANCE_ATTEMPTED_DOWNLOAD";
	private static final String INSTANCE_HAS_FILTERED_REVIEW = "INSTANCE_HAS_FILTERED_REVIEW";
	private static final String INSTANCE_HAS_REVIEWS = "INSTANCE_HAS_REVIEWS";
	private static final String INSTANCE_STATUS_RES_ID = "INSTANCE_STATUS_RES_ID";
	private static final String INSTANCE_PAGE_NUMBER = "INSTANCE_PAGE_NUMBER";
	private static final String INSTANCE_NUM_DOWNLOADED = "INSTANCE_NUM_DOWNLOADED";

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();

	private UserReviewsFragmentListener mUserReviewsFragmentListener;

	// STATIC DATABASE FOR REVIEWS
	private UserReviewsUtils mUserReviewsUtils = UserReviewsUtils.getInstance();

	private ReviewSort mReviewSort;
	private Property mProperty;
	private Property mCurrentProperty;

	private String mReviewsDownloadKey;
	private int mPageNumber = 0;
	private int mNumReviewsDownloaded = 0;

	private List<ReviewWrapper> mUserReviews;

	private boolean mAttemptedInitialDownload = false;
	private boolean mHasFilteredOutAReview = false;
	private int mStatusResId;

	private UserReviewsAdapter mUserReviewsAdapter;

	private View mHeaderView;

	// Use this variable to disable download when onScroll gets invoked automatically when the ScrollListener is set
	private boolean mScrollListenerSet;

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Public Overrides

	public static UserReviewsFragment newInstance(ReviewSort sort) {
		UserReviewsFragment fragment = new UserReviewsFragment();
		Bundle args = new Bundle();
		args.putString(ARGUMENT_SORT_STRING, sort.name());
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mUserReviewsFragmentListener = Ui.findFragmentListener(this, UserReviewsFragmentListener.class);

		mReviewSort = ReviewSort.valueOf(getArguments().getString(ARGUMENT_SORT_STRING));
		mReviewsDownloadKey = REVIEWS_DOWNLOAD_KEY_PREFIX + mReviewSort.name();

		mScrollListenerSet = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_review_list, container, false);
		if (!ExpediaBookingApp.useTabletInterface(getActivity())) {
			mHeaderView = inflater.inflate(R.layout.header_user_reviews_list, null, false);
		}
		return view;
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		if (mUserReviewsFragmentListener != null) {
			mUserReviewsFragmentListener.onUserReviewsFragmentReady(this);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView listView = getListView();
		if (mHeaderView != null) {
			listView.addHeaderView(mHeaderView, null, false);
		}

		mUserReviewsAdapter = new UserReviewsAdapter(getActivity(), listView);

		if (savedInstanceState != null) {
			mPageNumber = savedInstanceState.getInt(INSTANCE_PAGE_NUMBER);
			mNumReviewsDownloaded = savedInstanceState.getInt(INSTANCE_NUM_DOWNLOADED);
			mAttemptedInitialDownload = savedInstanceState.getBoolean(INSTANCE_ATTEMPTED_DOWNLOAD, false);
			mHasFilteredOutAReview = savedInstanceState.getBoolean(INSTANCE_HAS_FILTERED_REVIEW, false);
			boolean reincarnatedReviews = savedInstanceState.getBoolean(INSTANCE_HAS_REVIEWS, false);
			if (reincarnatedReviews) {
				mProperty = Db.getHotelSearch().getSelectedProperty();
				mUserReviews = mUserReviewsUtils.getReviews(mProperty.getPropertyId(), mReviewSort);
				if (mUserReviews != null) {
					mUserReviewsAdapter.setUserReviews(mUserReviews);
				}
			}
			else {
				updateEmptyMessage(savedInstanceState.getInt(INSTANCE_STATUS_RES_ID));
			}
		}

		listView.setOnScrollListener(this);
		setListAdapter(mUserReviewsAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mBackgroundDownloader.isDownloading(mReviewsDownloadKey)) {
			mBackgroundDownloader.registerDownloadCallback(mReviewsDownloadKey, mUserReviewDownloadCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity() != null && getActivity().isFinishing()) {
			mBackgroundDownloader.cancelDownload(mReviewsDownloadKey);
		}
		else {
			mBackgroundDownloader.unregisterDownloadCallback(mReviewsDownloadKey);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_ATTEMPTED_DOWNLOAD, mAttemptedInitialDownload);
		outState.putBoolean(INSTANCE_HAS_FILTERED_REVIEW, mHasFilteredOutAReview);

		boolean hasReviews = false;
		if (mUserReviews != null && mUserReviews.size() > 0) {
			hasReviews = true;
		}
		else {
			outState.putInt(INSTANCE_STATUS_RES_ID, mStatusResId);
		}
		outState.putBoolean(INSTANCE_HAS_REVIEWS, hasReviews);

		outState.putInt(INSTANCE_PAGE_NUMBER, mPageNumber);
		outState.putInt(INSTANCE_NUM_DOWNLOADED, mNumReviewsDownloaded);
	}

	public void bind() {
		mProperty = Db.getHotelSearch().getSelectedProperty();
		attemptInitialReviewsDownload();
		populateListHeader();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Reviews Download

	private void attemptInitialReviewsDownload() {
		if (mAttemptedInitialDownload && mCurrentProperty != null && mCurrentProperty != mProperty) {
			mAttemptedInitialDownload = false;
			mUserReviews = null;
			if (mBackgroundDownloader.isDownloading(mReviewsDownloadKey)) {
				mBackgroundDownloader.cancelDownload(mReviewsDownloadKey);
			}
		}

		if (!mAttemptedInitialDownload && mProperty != null) {
			mAttemptedInitialDownload = true;
			mCurrentProperty = mProperty;
			startReviewsDownload();
		}
	}

	private void startReviewsDownload() {
		if (mProperty != null) {
			mBackgroundDownloader.startDownload(mReviewsDownloadKey, mUserReviewDownload, mUserReviewDownloadCallback);
		}
	}

	private final Download<ReviewsResponse> mUserReviewDownload = new Download<ReviewsResponse>() {
		@Override
		public ReviewsResponse doDownload() {
			ExpediaServices expediaServices = new ExpediaServices(getActivity());
			mBackgroundDownloader.addDownloadListener(mReviewsDownloadKey, expediaServices);
			return expediaServices.reviews(mProperty, mReviewSort, mPageNumber);
		}
	};

	private final OnDownloadComplete<ReviewsResponse> mUserReviewDownloadCallback = new OnDownloadComplete<ReviewsResponse>() {
		@Override
		public void onDownload(ReviewsResponse response) {
			if (response == null || response.hasErrors()) {
				showReviewsUnavailableMessage();
			}
			else {
				removeLoadingFooter();
				mPageNumber++;
				mNumReviewsDownloaded += response.getNumReviewsInResponse();

				ArrayList<ReviewWrapper> newlyLoadedReviews = reviewWrapperListInit(response.getReviews());
				if (mUserReviews == null) {
					mUserReviews = new ArrayList<ReviewWrapper>();
				}

				if (newlyLoadedReviews.size() == 0) {
					showReviewsNotPresentMessage();
				}

				mUserReviews.addAll(newlyLoadedReviews);

				mUserReviewsUtils.putReviews(mProperty.getPropertyId(), mReviewSort, mUserReviews);
				mUserReviewsAdapter.setUserReviews(mUserReviews);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ScrollListener

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (Db.getHotelSearch() == null || Db.getHotelSearch().getSelectedProperty() == null) {
			return;
		}

		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
		boolean hasMore = mNumReviewsDownloaded < Db.getHotelSearch().getSelectedProperty().getTotalReviews();
		boolean shouldAttempt = !mHasFilteredOutAReview;
		boolean isDownloading = mBackgroundDownloader.isDownloading(mReviewsDownloadKey);

		if (mScrollListenerSet && loadMore && hasMore && shouldAttempt && !isDownloading) {
			startReviewsDownload();
			addLoadingFooter();
		}

		Set<String> viewedReviews = new HashSet<String>();
		int count = mUserReviewsAdapter.getCount();
		for (int a = 0; a < visibleItemCount && firstVisibleItem + a < count; a++) {
			Object item = mUserReviewsAdapter.getItem(firstVisibleItem + a);
			if (item instanceof ReviewWrapper && !((ReviewWrapper) item).mIsDivider
					&& !((ReviewWrapper) item).mIsLoadingFooter) {
				viewedReviews.add(((ReviewWrapper) item).mReview.getReviewId());
			}
		}
		mUserReviewsFragmentListener.addMoreReviewsSeen(viewedReviews);

		if (!mScrollListenerSet) {
			mScrollListenerSet = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// We don't care about the scrollState changing right now
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Views

	private void populateListHeader() {
		if (mHeaderView == null || mProperty == null) {
			return;
		}

		TextView recommendText = Ui.findView(mHeaderView, R.id.user_reviews_recommendation_tag);

		int numRec = mProperty.getTotalRecommendations();
		int numTotal = mProperty.getTotalReviews();
		float percentRecommend = mProperty.getPercentRecommended();
		String text = getString(R.string.user_review_recommendation_tag_text, numRec, numTotal);
		CharSequence styledText = Html.fromHtml(text);

		if (numTotal > 0) {
			int drawableResId = percentRecommend >= 50.0f ? R.drawable.ic_good_rating : R.drawable.ic_bad_rating;
			recommendText.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
			recommendText.setText(styledText);
		}

		// In landscape mode, "19 reviews" and user rating bar are also present in this view
		TextView numReviews = Ui.findView(mHeaderView, R.id.num_reviews);
		if (numReviews != null) {
			Resources res = getResources();
			String title = res.getQuantityString(R.plurals.number_of_reviews, numTotal, numTotal);
			numReviews.setText(title);
		}
		RatingBar userRating = Ui.findView(mHeaderView, R.id.user_rating);
		if (userRating != null) {
			float rating = (float) mProperty.getAverageExpediaRating();
			userRating.setRating(rating);
			userRating.setVisibility(View.VISIBLE);

		}
	}

	private void showReviewsUnavailableMessage() {
		removeLoadingFooter();
		updateEmptyMessage(R.string.user_review_unavailable);
	}

	private void showReviewsNotPresentMessage() {
		removeLoadingFooter();
		updateEmptyMessage(mReviewSort.getNoReviewsMessageResId());
	}

	private void addLoadingFooter() {
		ReviewWrapper rw = new ReviewWrapper();
		rw.mIsLoadingFooter = true;

		if (mUserReviews == null) {
			mUserReviews = new ArrayList<ReviewWrapper>();
		}
		mUserReviews.add(rw);
		mUserReviewsUtils.putReviews(mProperty.getPropertyId(), mReviewSort, mUserReviews);
		mUserReviewsAdapter.setUserReviews(mUserReviews);
	}

	private void removeLoadingFooter() {
		if (mUserReviews != null) {
			int size = mUserReviews.size();
			int pos = size - 1;
			if (pos >= 0) {
				ReviewWrapper last = mUserReviews.get(pos);
				if (last.mIsLoadingFooter) {
					mUserReviews.remove(pos);
					mUserReviewsUtils.putReviews(mProperty.getPropertyId(), mReviewSort, mUserReviews);
					mUserReviewsAdapter.setUserReviews(mUserReviews);
				}
			}
		}
	}

	private void updateEmptyMessage(int msgId) {
		mStatusResId = msgId;

		View view = getView();
		ProgressBar progressBar = Ui.findView(view, R.id.progress_bar);
		progressBar.setVisibility(View.GONE);

		if (msgId != 0) {
			String emptyText;
			TextView emptyTextView = Ui.findView(view, R.id.user_review_empty_text_view);

			if (!NetUtils.isOnline(getActivity())) {
				emptyText = getString(R.string.widget_error_no_internet);
			}
			else {
				emptyText = getString(msgId);
			}

			emptyTextView.setText(emptyText);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Review helper classes/methods

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
		public boolean mIsLoadingFooter = false;
	}

	private ArrayList<ReviewWrapper> reviewWrapperListInit(List<Review> reviews) {
		ArrayList<ReviewWrapper> loadedReviews = new ArrayList<ReviewWrapper>();
		if (reviews == null) {
			return null;
		}
		for (Review review : reviews) {
			// Check to see if this review passes through the filter before processing and adding to the list
			if (!mReviewSort.reviewPassesFilter(review)) {
				mHasFilteredOutAReview = true;
				continue;
			}

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

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Fragment listener

	/**
	 * Fragment listener interface used to communicate what reviews the user looked at for tracking purposes.
	 * @author brad
	 *
	 */
	public interface UserReviewsFragmentListener {
		public void onUserReviewsFragmentReady(UserReviewsFragment frag);
		public void addMoreReviewsSeen(Set<String> reviews);
	}

}
