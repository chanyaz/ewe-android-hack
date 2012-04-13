package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.expedia.bookings.widget.UserReviewsAdapter;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UserReviewsFragment extends ListFragment implements OnScrollListener {

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constants
	private String REVIEWS_DOWNLOAD_KEY_PREFIX = "com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsDownload.";
	private String mReviewsDownloadKey;

	private static final int BODY_LENGTH_CUTOFF = 270;
	private static final int THUMB_CUTOFF_INCLUSIVE = 5;

	// Bundle strings
	private static final String ARGUMENT_PROPERTY_JSON = "ARGUMENT_PROPERTY_JSON";
	private static final String ARGUMENT_SORT_STRING = "ARGUMENT_SORT_STRING";

	private static final String INSTANCE_ATTEMPTED_DOWNLOAD = "INSTANCE_ATTEMPTED_DOWNLOAD";
	private static final String INSTANCE_HAS_REVIEWS = "INSTANCE_HAS_REVIEWS";
	private static final String INSTANCE_RECOMMENDED_REVIEW_COUNT = "INSTANCE_RECOMMENDED_REVIEW_COUNT";
	private static final String INSTANCE_TOTAL_REVIEW_COUNT = "INSTANCE_TOTAL_REVIEW_COUNT";
	private static final String INSTANCE_STATUS_RES_ID = "INSTANCE_STATUS_RES_ID";
	private static final String INSTANCE_LANGUAGE_LIST_META = "INSTANCE_LANGUAGE_LIST_META";

	private static final String JSONABLE_LANGUAGE_CODES = "JSONABLE_LANGUAGE_CODES";
	private static final String JSONABLE_TOTAL_COUNT = "JSONABLE_TOTAL_COUNT";
	private static final String JSONABLE_PAGE_NUMBER = "JSONABLE_PAGE_NUMBER";

	// Network classes
	private BackgroundDownloader mBackgroundDownloader = BackgroundDownloader.getInstance();
	private ExpediaServices mExpediaServices;

	// STATIC DATABASE FOR REVIEWS
	private UserReviewsUtils mUserReviewsUtils = UserReviewsUtils.getInstance();

	// Reviews bookkeeping data
	private ReviewSort mReviewSort;
	private Property mProperty;
	private LinkedList<ReviewLanguageSet> mMetaLanguageList;
	private List<ReviewWrapper> mUserReviews;

	private int mRecommendedReviewCount;
	private int mTotalReviewCount;

	private boolean mAttemptedDownload = false;
	private int mStatusResId;

	private UserReviewsAdapter mUserReviewsAdapter;

	// List views
	private ViewGroup mHeaderView;
	private ViewGroup mFooterView;

	private boolean mScrollListenerSet;

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Public Overrides

	public static UserReviewsFragment newInstance(Property property, ReviewSort sort) {
		UserReviewsFragment fragment = new UserReviewsFragment();

		Bundle args = new Bundle();

		// property
		String propertyJson = property.toJson().toString();
		args.putString(ARGUMENT_PROPERTY_JSON, propertyJson);

		// review sort
		String sortString = sort.toString();
		args.putString(ARGUMENT_SORT_STRING, sortString);

		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof UserReviewsFragmentListener)) {
			throw new RuntimeException("UserReviewsFragment Activity must implement UserReviewsFragmentListener!");
		}

		mUserReviewsFragmentListener = (UserReviewsFragmentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// property
		final Bundle args = getArguments();
		JSONObject property = JSONUtils.parseJSONObjectFromBundle(args, ARGUMENT_PROPERTY_JSON);
		mProperty = new Property();
		mProperty.fromJson(property);

		// review sort
		String sortString = args.getString(ARGUMENT_SORT_STRING);
		mReviewSort = ReviewSort.valueOf(sortString);

		mReviewsDownloadKey = REVIEWS_DOWNLOAD_KEY_PREFIX + mReviewSort.toString();

		if (savedInstanceState == null) {
			// create the meta data for language lists based on the review sort
			mMetaLanguageList = new LinkedList<ReviewLanguageSet>();
			LinkedList<String> languages = LocaleUtils.getLanguages(getActivity());

			if (mReviewSort == ReviewSort.NEWEST_REVIEW_FIRST && languages.size() != 3) {
				// segregate the reviews by language for the recent tab
				for (String languageCode : languages) {
					ReviewLanguageSet rls = new ReviewLanguageSet();
					LinkedList<String> codes = new LinkedList<String>();
					codes.add(languageCode);
					rls.addLanguageCodes(codes);
					mMetaLanguageList.add(rls);
				}
			}
			else {
				// group all of the languages in to one group for the favorable/critical sorts (or if a triple language for review POS: AR, PT)
				ReviewLanguageSet rls = new ReviewLanguageSet();
				rls.addLanguageCodes(languages);
				mMetaLanguageList.add(rls);
			}
		}
		else {
			ArrayList<String> jsonArray = savedInstanceState.getStringArrayList(INSTANCE_LANGUAGE_LIST_META);

			mMetaLanguageList = new LinkedList<ReviewLanguageSet>();
			for (String json : jsonArray) {
				JSONObject obj;
				try {
					obj = new JSONObject(json);
					ReviewLanguageSet rls = new ReviewLanguageSet();
					rls.fromJson(obj);
					mMetaLanguageList.add(rls);
				}
				catch (JSONException e) {
					Log.d("Could not create JSONObject from JSON string representation of the ReviewLanguageSet object");
					e.printStackTrace();
				}
			}
		}

		mScrollListenerSet = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user_review_list, container, false);

		if (mHeaderView == null) {
			mHeaderView = (ViewGroup) inflater.inflate(R.layout.header_user_reviews_list, null, false);
		}

		if (mFooterView == null) {
			mFooterView = (ViewGroup) inflater.inflate(R.layout.footer_user_reviews_list_loading_more, null,
					false);
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ListView listView = getListView();

		// must add the header/footer views in onActivityCreated, and before setListAdapter
		listView.addHeaderView(mHeaderView, null, false);

		mUserReviewsAdapter = new UserReviewsAdapter(getActivity(), listView);

		if (savedInstanceState != null) {
			mAttemptedDownload = savedInstanceState.getBoolean(INSTANCE_ATTEMPTED_DOWNLOAD, false);
			boolean reincarnatedReviews = savedInstanceState.getBoolean(INSTANCE_HAS_REVIEWS, false);
			if (reincarnatedReviews) {
				mRecommendedReviewCount = savedInstanceState.getInt(INSTANCE_RECOMMENDED_REVIEW_COUNT);
				mTotalReviewCount = savedInstanceState.getInt(INSTANCE_TOTAL_REVIEW_COUNT);

				populateListHeader();

				String propertyId = mProperty.getPropertyId();

				mUserReviews = mUserReviewsUtils.getReviews(propertyId, mReviewSort);
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
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mAddFooterTask);
		mHandler.removeCallbacks(mRemoveFooterTask);
		mBackgroundDownloader.unregisterDownloadCallback(mReviewsDownloadKey);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mBackgroundDownloader.isDownloading(mReviewsDownloadKey)) {
			mBackgroundDownloader.registerDownloadCallback(mReviewsDownloadKey, mUserReviewDownloadCallback);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_ATTEMPTED_DOWNLOAD, mAttemptedDownload);

		boolean hasReviews = false;
		if (mUserReviews != null && mUserReviews.size() > 0) {
			hasReviews = true;
		}
		else {
			outState.putInt(INSTANCE_STATUS_RES_ID, mStatusResId);
		}
		outState.putBoolean(INSTANCE_HAS_REVIEWS, hasReviews);

		outState.putInt(INSTANCE_RECOMMENDED_REVIEW_COUNT, mRecommendedReviewCount);
		outState.putInt(INSTANCE_TOTAL_REVIEW_COUNT, mTotalReviewCount);

		// pack/bounce the meta language list
		ArrayList<String> jsonStringArray = new ArrayList<String>();
		for (ReviewLanguageSet meta : mMetaLanguageList) {
			jsonStringArray.add(meta.toString());
		}

		outState.putStringArrayList(INSTANCE_LANGUAGE_LIST_META, jsonStringArray);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Public Methods

	public void populateListHeader() {
		populateListHeader(mRecommendedReviewCount, mTotalReviewCount);

	}

	public void populateListHeader(int recommendedCount, int totalCount) {
		mRecommendedReviewCount = recommendedCount;
		mTotalReviewCount = totalCount;

		View view = getView();

		TextView recommendText = (TextView) view.findViewById(R.id.user_reviews_recommendation_tag);

		String text = String.format(getString(R.string.user_review_recommendation_tag_text),
				recommendedCount, totalCount);
		CharSequence styledText = Html.fromHtml(text);

		ImageView thumbView = (ImageView) view.findViewById(R.id.user_reviews_thumb);

		if (totalCount > 0) {
			if (recommendedCount * 10 / totalCount >= THUMB_CUTOFF_INCLUSIVE) {
				thumbView.setImageResource(R.drawable.review_thumbs_up);
			}
			else {
				thumbView.setImageResource(R.drawable.review_thumbs_down);
			}
			recommendText.setText(styledText);
		}
	}

	public boolean getHasAttemptedDownload() {
		return mAttemptedDownload;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Reviews Download

	public void startReviewsDownload() {
		mBackgroundDownloader.startDownload(mReviewsDownloadKey, mUserReviewDownload, mUserReviewDownloadCallback);
	}

	public void cancelReviewsDownload() {
		mBackgroundDownloader.cancelDownload(mReviewsDownloadKey);
	}

	private Download mUserReviewDownload = new Download() {

		@Override
		public Object doDownload() {
			ensureExpediaServicesCacheFilled();

			mBackgroundDownloader.addDownloadListener(mReviewsDownloadKey, mExpediaServices);

			// grab the correct numbers to send to reviews service, to pull the correct page of reviews
			ReviewLanguageSet rls = mMetaLanguageList.getFirst();
			int pageNumber = rls.mPageNumber;
			List<String> langs = rls.mLanguageCodes;

			return mExpediaServices.reviews(mProperty, mReviewSort, pageNumber, langs);
		}
	};

	private OnDownloadComplete mUserReviewDownloadCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			ReviewsResponse response = (ReviewsResponse) results;

			mAttemptedDownload = true;

			if (response == null) {
				updateEmptyMessage(R.string.user_review_unavailable);
			}
			else {
				if (response.hasErrors()) {
					updateEmptyMessage(R.string.user_review_unavailable);
				}
				else {
					// grab the meta object for this dl/callback
					ReviewLanguageSet rls = mMetaLanguageList.removeFirst();

					if (response.getTotalCount() < 1) {
						// add the divider, only if there are more reviews to download
						if (hasMoreReviews()) {
							addDivider();
						}
						else {
							updateEmptyMessage(R.string.user_review_no_favorable_reviews);

							removeDivider();

							mHandler.post(mRemoveFooterTask);
						}
					}
					else {

						// update bookkeeping - set the total count which is discovered after the call, increment page number
						rls.setTotalCount(response.getTotalCount());
						rls.incrementPageNumber();

						// wrap the list of reviews
						ArrayList<ReviewWrapper> newlyLoadedReviews = reviewWrapperListInit(response.getReviews());

						// determine whether or not this meta object is spent
						boolean addDivider = false;
						if (rls.hasMore()) {
							// re-attach the modified meta object to the front of the list
							mMetaLanguageList.addFirst(rls);
						}
						else {
							// add the divider only if there exists another meta object, ie another set of reviews, in a different language
							if (hasMoreReviews()) {
								addDivider = true;
							}
						}

						if (mUserReviews == null) {
							mUserReviews = new ArrayList<ReviewWrapper>();
						}

						mUserReviews.addAll(newlyLoadedReviews);
						if (addDivider) {
							addDivider();
						}

						mUserReviewsUtils.addReviews(mProperty.getPropertyId(), mReviewSort, mUserReviews);
						mUserReviewsAdapter.setUserReviews(mUserReviews);

						mHandler.post(mRemoveFooterTask);
					}
				}
			}
			mUserReviewsFragmentListener.onDownloadComplete(mReviewSort);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Handler + Runnables

	private final Handler mHandler = new Handler() {

	};

	private final Runnable mAddFooterTask = new Runnable() {

		@Override
		public void run() {
			ListView lv = getListView();
			lv.addFooterView(mFooterView, null, false);
		}

	};

	private final Runnable mRemoveFooterTask = new Runnable() {

		@Override
		public void run() {
			ListView lv = getListView();
			lv.removeFooterView(mFooterView);
		}

	};

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ScrollListener

	/**
	 * ScrollListener implementation used to have an automatically loading list. This way the user does not have to
	 * manually page the reviews, rather a review download kicks off when it is necessary. Note: there exists an object
	 * and manual setting of this ScrollListener, rather than having the class implement the interface so as to be more
	 * deterministic with the setting of the scroll listener
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
		boolean notDownloading = !mBackgroundDownloader.isDownloading(mReviewsDownloadKey);

		if (mScrollListenerSet && loadMore && hasMoreReviews() && notDownloading) {
			startReviewsDownload();
			mHandler.post(mAddFooterTask);
		}

		Set<String> viewedReviews = new HashSet<String>();
		int count = mUserReviewsAdapter.getCount();
		for (int a = 0; a < visibleItemCount && firstVisibleItem + a < count; a++) {
			Object item = mUserReviewsAdapter.getItem(firstVisibleItem + a);
			if (item instanceof ReviewWrapper && !((ReviewWrapper) item).mIsDivider) {
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
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Divider

	/**
	 * Divider to note the segregated reviews that we use for the recent sort, displays
	 * a localized message "Reviews in other languages"
	 */
	private void addDivider() {
		if (mUserReviews == null) {
			mUserReviews = new ArrayList<ReviewWrapper>();
		}
		ReviewWrapper rw = new ReviewWrapper(true);

		mUserReviews.add(rw);
		mUserReviewsUtils.addReviews(mProperty.getPropertyId(), mReviewSort, mUserReviews);
		mUserReviewsAdapter.setUserReviews(mUserReviews);
	}

	private void removeDivider() {
		if (mUserReviews != null) {
			int size = mUserReviews.size();
			if (size > 0) {
				int pos = size - 1;
				ReviewWrapper last = mUserReviews.get(pos);
				if (last.mIsDivider) {
					mUserReviews.remove(pos);
					mUserReviewsUtils.addReviews(mProperty.getPropertyId(), mReviewSort, mUserReviews);
					mUserReviewsAdapter.setUserReviews(mUserReviews);
				}
			}
		}
	}

	private void updateEmptyMessage(int msgId) {
		mStatusResId = msgId;
		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
		progressBar.setVisibility(View.GONE);

		if (msgId != 0) {
			String emptyText;
			TextView emptyTextView = (TextView) getView().findViewById(R.id.user_review_empty_text_view);

			if (!NetUtils.isOnline(getActivity())) {
				emptyText = getString(R.string.widget_error_no_internet);
			}
			else {
				emptyText = getString(msgId);
			}

			emptyTextView.setText(emptyText);
		}
	}

	/**
	 * This method determines whether or not the fragment has more reviews to download. This means that there
	 * still exists a ReviewLanguageSet meta object as part of the mMetaLanguageList
	 * @return boolean whether or not to make another network call
	 */
	private boolean hasMoreReviews() {
		if (mMetaLanguageList != null && mMetaLanguageList.size() > 0) {
			return true;
		}
		return false;
	}

	private void ensureExpediaServicesCacheFilled() {
		if (mExpediaServices == null) {
			mExpediaServices = new ExpediaServices(getActivity());
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Review helper classes/methods

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
	private static class ReviewLanguageSet implements JSONable {

		private List<String> mLanguageCodes;

		private int mTotalCount;
		private int mPageNumber;

		public ReviewLanguageSet() {
			this.mLanguageCodes = new LinkedList<String>();
			this.mPageNumber = 0;
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

		public void addLanguageCodes(List<String> codes) {
			mLanguageCodes.addAll(codes);
		}

		public void incrementPageNumber() {
			mPageNumber++;
		}

		@Override
		public JSONObject toJson() {
			try {
				JSONObject obj = new JSONObject();

				JSONUtils.putStringList(obj, JSONABLE_LANGUAGE_CODES, mLanguageCodes);

				obj.putOpt(JSONABLE_TOTAL_COUNT, mTotalCount);
				obj.putOpt(JSONABLE_PAGE_NUMBER, mPageNumber);

				return obj;
			}
			catch (JSONException e) {
				Log.e("Could not convert ReviewLanguageSet object to JSON.", e);
				return null;
			}
		}

		@Override
		public boolean fromJson(JSONObject obj) {
			mLanguageCodes = (List<String>) JSONUtils.getStringList(obj, JSONABLE_LANGUAGE_CODES);

			mTotalCount = obj.optInt(JSONABLE_TOTAL_COUNT);
			mPageNumber = obj.optInt(JSONABLE_PAGE_NUMBER);

			return true;
		}

		@Override
		public String toString() {
			JSONObject obj = toJson();
			try {
				return obj.toString(2);
			}
			catch (JSONException e) {
				return obj.toString();
			}
		}

	}

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

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Fragment listener

	/**
	 * Fragment listener interface used to communicate from fragment to enclosing activity. By defining an interface
	 * that the enclosing activity must define, the fragment is not as tightly coupled to its enclosing activity. Invoke
	 * onDownloadComplete after a set of reviews is downloaded in order to trigger the activity to chain (or not) the downloads
	 * for the other review sorts
	 * 
	 * @author brad
	 *
	 */
	public interface UserReviewsFragmentListener {

		public void onDownloadComplete(ReviewSort sort);

		public void addMoreReviewsSeen(Set<String> reviews);

	}

	private UserReviewsFragmentListener mUserReviewsFragmentListener;

}
