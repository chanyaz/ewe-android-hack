package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.ResultsHotelsListState;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.fragment.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IAddToBucketListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsFilterDoneClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelGalleryBackClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelGalleryClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelReviewsBackClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelReviewsClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.ISiblingListTouchListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdImpressionTracking;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;
import com.mobiata.android.util.TimingLogger;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsHotelControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to HOTELS results
 */
public class TabletResultsHotelControllerFragment extends Fragment implements
	ISortAndFilterListener, IResultsHotelSelectedListener, IFragmentAvailabilityProvider,
	HotelMapFragmentListener, SupportMapFragmentListener, IBackManageable, IStateProvider<ResultsHotelsState>,
	IAddToBucketListener,
	IResultsHotelReviewsClickedListener, IAcceptingListenersListener, IResultsHotelReviewsBackClickedListener,
	IResultsHotelGalleryClickedListener, IResultsHotelGalleryBackClickedListener, IResultsFilterDoneClickedListener {

	// State
	private static final String STATE_HOTELS_STATE = "STATE_HOTELS_STATE";

	// Frag tags
	private static final String FTAG_HOTEL_LIST = "FTAG_HOTEL_LIST";
	private static final String FTAG_HOTEL_FILTERS = "FTAG_HOTEL_FILTERS";
	private static final String FTAG_HOTEL_FILTERED_COUNT = "FTAG_HOTEL_FILTERED_COUNT";
	private static final String FTAG_HOTEL_MAP = "FTAG_HOTEL_MAP";
	private static final String FTAG_HOTEL_DETAILS = "FTAG_HOTEL_DETAILS";
	private static final String FTAG_HOTEL_SEARCH_DOWNLOAD = "FTAG_HOTEL_SEARCH_DOWNLOAD";
	private static final String FTAG_HOTEL_LOADING_INDICATOR = "FTAG_HOTEL_LOADING_INDICATOR";
	private static final String FTAG_HOTEL_SEARCH_ERROR = "FTAG_HOTEL_SEARCH_ERROR";
	private static final String FTAG_HOTEL_REVIEWS = "FTAG_HOTEL_REVIEWS";
	private static final String FTAG_HOTEL_GALLERY = "FTAG_HOTEL_GALLERY";

	// Settings

	// Containers
	private ViewGroup mRootC;
	private TouchableFrameLayout mHotelListC;
	private TouchableFrameLayout mBgHotelMapC;
	private TouchableFrameLayout mBgHotelMapTouchDelegateC;
	private TouchableFrameLayout mHotelFiltersC;
	private TouchableFrameLayout mHotelFilteredCountC;
	private TouchableFrameLayout mHotelDetailsC;
	private TouchableFrameLayout mHotelReviewsC;
	private TouchableFrameLayout mHotelGalleryC;
	private TouchableFrameLayout mLoadingC;
	private TouchableFrameLayout mSearchErrorC;
	private TouchableFrameLayout mMapDimmer;

	// Fragments
	private HotelMapFragment mMapFragment;
	private ResultsHotelListFragment mHotelListFrag;
	private ResultsHotelsFiltersFragment mHotelFiltersFrag;
	private ResultsHotelsFilterCountFragment mHotelFilteredCountFrag;
	private ResultsHotelDetailsFragment mHotelDetailsFrag;
	private HotelSearchDownloadFragment mHotelSearchDownloadFrag;
	private ResultsListLoadingFragment mLoadingGuiFrag;
	private ResultsListSearchErrorFragment mSearchErrorFrag;
	private ResultsHotelReviewsFragment mHotelReviewsFrag;
	private ResultsHotelGalleryFragment mHotelGalleryFrag;

	// Other
	private StateManager<ResultsHotelsState> mHotelsStateManager = new StateManager<ResultsHotelsState>(getBaseState(), this);
	private GridManager mGrid = new GridManager();

	private boolean mHotelsDeepLink = false;

	private boolean mListHasTouch = false;
	private ISiblingListTouchListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (FragmentBailUtils.shouldBail(getActivity())) {
			return;
		}

		if (savedInstanceState != null) {
			mHotelsStateManager.setDefaultState(ResultsHotelsState.valueOf(savedInstanceState.getString(
				STATE_HOTELS_STATE, getBaseState().name())));
		}
		ResultsHotelsState state = mHotelsStateManager.getState();

		// MAP state is a portrait only, just fall back to LIST_UP on rotation
		if (!getResources().getBoolean(R.bool.portrait) && state == ResultsHotelsState.MAP) {
			mHotelsStateManager.setState(ResultsHotelsState.HOTEL_LIST_UP, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			FragmentManager manager = getChildFragmentManager();
			mMapFragment = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_MAP);
			mHotelListFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_LIST);
			mHotelFiltersFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_FILTERS);
			mHotelFilteredCountFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_FILTERED_COUNT);
			mHotelDetailsFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_DETAILS);
			mHotelSearchDownloadFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_SEARCH_DOWNLOAD);
			mLoadingGuiFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_LOADING_INDICATOR);
			mSearchErrorFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_SEARCH_ERROR);
			mHotelReviewsFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_REVIEWS);
			mHotelGalleryFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_HOTEL_GALLERY);
		}
		View view = inflater.inflate(R.layout.fragment_tablet_results_hotels, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mHotelListC = Ui.findView(view, R.id.column_one_hotel_list);
		mBgHotelMapC = Ui.findView(view, R.id.bg_hotel_map);
		mBgHotelMapC.setTouchListener(mMapTouchListener);
		mBgHotelMapTouchDelegateC = Ui.findView(view, R.id.bg_hotel_map_touch_delegate);
		mHotelFiltersC = Ui.findView(view, R.id.column_two_hotel_filters);
		mHotelFiltersC.setConsumeTouch(true);
		mHotelFilteredCountC = Ui.findView(view, R.id.column_three_hotel_filtered_count);
		mHotelFilteredCountC.setConsumeTouch(true);
		mHotelDetailsC = Ui.findView(view, R.id.hotel_details);
		mHotelReviewsC = Ui.findView(view, R.id.hotel_reviews);
		mHotelGalleryC = Ui.findView(view, R.id.hotel_gallery);
		mLoadingC = Ui.findView(view, R.id.loading_container);
		mSearchErrorC = Ui.findView(view, R.id.column_one_hotel_search_error);
		mMapDimmer = Ui.findView(view, R.id.bg_map_dimmer);
		mMapDimmer.setTouchListener(mMapDimmerTouchListener);

		// Default maps to be invisible (they get ignored by our setVisibilityState function so this is important)
		mBgHotelMapC.setAlpha(0f);

		// Set up our maps touch passthrough. It is important to note that A) the touch receiver is set to be invisible,
		// so that when it gets a touch, it will pass to whatever is behind it. B) It must be the same size as the
		// the view sending it touch events, because no offsets or anything like that are performed. C) It must be
		// behind the view getting the original touch event, otherwise it will create a loop.
		mBgHotelMapTouchDelegateC.setVisibility(View.INVISIBLE);
		mBgHotelMapC.setTouchPassThroughReceiver(mBgHotelMapTouchDelegateC);

		registerStateListener(mHotelsStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsHotelsState>(), false);

		mListener = (ISiblingListTouchListener) getActivity();

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_HOTELS_STATE, mHotelsStateManager.getState().name());
	}

	@Override
	public void onResume() {
		super.onResume();
		mResultsStateHelper.registerWithProvider(this, true);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
		Sp.getBus().register(this);
		Events.register(this);
		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, false);
		}
		Sp.getBus().unregister(this);
		Events.unregister(this);
		mResultsStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);


	}

	@Subscribe
	public void onSimpleDialogCallbackClick(Events.SimpleCallBackDialogOnClick click) {
		if (click.callBackId == SimpleCallbackDialogFragment.CODE_TABLET_NO_NET_CONNECTION_HOTEL_DETAILS) {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
		}
	}

	/*
	 * NEW SEARCH PARAMS
	 */

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		// We only report SpUpdates when params change, so we can safely assume that this means
		// we want to kick off a new search in this case always (unless the params don't support
		// a hotel search)
		//
		// TODO this "dateRangeSupportsHotelSearch" check also occurs in "onStateFinalized", so it is
		// likely not needed in both places..
		if (HotelUtils.dateRangeSupportsHotelSearch(getActivity())) {
			setHotelsState(ResultsHotelsState.LOADING, false);
		}
	}

	private void importSearchParams() {
		Db.getHotelSearch().setSearchResponse(null);
		Db.getHotelSearch().setSearchParams(Sp.getParams().toHotelSearchParams());
	}

	/**
	 * STATE HELPERS
	 */

	private ResultsHotelsState getBaseState() {
		if (isAdded() && !HotelUtils.dateRangeSupportsHotelSearch(getActivity())) {
			return ResultsHotelsState.MAX_HOTEL_STAY;
		}
		else if (Db.getHotelSearch() != null && Db.getHotelSearch().getSearchResponse() != null
			&& Db.getHotelSearch().getSearchResponse().hasErrors()) {
			return ResultsHotelsState.SEARCH_ERROR;
		}
		else if (Db.getHotelSearch() == null || Db.getHotelSearch().getSearchResponse() == null) {
			if (mHotelsDeepLink) {
				return ResultsHotelsState.LOADING_HOTEL_LIST_UP;
			}
			else {
				return ResultsHotelsState.LOADING;
			}
		}
		else if (mHotelsStateManager != null && mHotelsStateManager.getState() == ResultsHotelsState.ZERO_RESULT) {
			return ResultsHotelsState.ZERO_RESULT;
		}
		else {
			return ResultsHotelsState.HOTEL_LIST_DOWN;
		}
	}

	public void setHotelsState(ResultsHotelsState state, boolean animate) {
		mHotelsStateManager.setState(state, animate);
	}

	public void enterViaDeepLink() {
		mHotelsDeepLink = true;
		Db.getHotelSearch().setSearchResponse(null);
		mHotelsStateManager.setDefaultState(getBaseState());
	}

	public ResultsHotelsState getHotelsState() {
		return mHotelsStateManager.getState();
	}

	private void setTouchState(ResultsHotelsState hotelsState) {
		if (hotelsState == ResultsHotelsState.LOADING) {
			mHotelListC.setBlockNewEventsEnabled(true);
		}
		else {
			mHotelListC.setBlockNewEventsEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN || hotelsState == ResultsHotelsState.ROOMS_AND_RATES
			|| hotelsState == ResultsHotelsState.REVIEWS || hotelsState == ResultsHotelsState.GALLERY) {
			mBgHotelMapC.setTouchPassThroughEnabled(true);
		}
		else {
			mBgHotelMapC.setTouchPassThroughEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.LOADING_HOTEL_LIST_UP) {
			mMapDimmer.setConsumeTouch(true);
		}
		else {
			mMapDimmer.setConsumeTouch(false);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			mHotelDetailsC.setBlockNewEventsEnabled(true);
		}
		else {
			mHotelDetailsC.setBlockNewEventsEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
			mHotelDetailsC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelDetailsC.setBlockNewEventsEnabled(true);
		}

		if (hotelsState == ResultsHotelsState.REVIEWS) {
			mHotelReviewsC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelReviewsC.setBlockNewEventsEnabled(true);
		}

		if (hotelsState == ResultsHotelsState.GALLERY) {
			mHotelGalleryC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelGalleryC.setBlockNewEventsEnabled(true);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
			mHotelFiltersC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelFiltersC.setBlockNewEventsEnabled(true);
		}
	}

	public void setListTouchable(boolean touchable) {
		mHotelListC.setBlockNewEventsEnabled(!touchable);
	}

	public ResultsHotelListFragment getListFragment() {
		return mHotelListFrag;
	}

	private void setVisibilityState(ResultsHotelsState hotelsState) {
		mHotelListC.setVisibility(View.VISIBLE);
		mLoadingC.setVisibility(
			hotelsState.showLoading()
				? View.VISIBLE
				: View.INVISIBLE);

		if (hotelsState.isShowMessageState()) {
			mSearchErrorC.setVisibility(View.VISIBLE);
		}
		else {
			mSearchErrorC.setVisibility(View.INVISIBLE);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN || hotelsState == ResultsHotelsState.LOADING || hotelsState.isShowMessageState()) {
			mBgHotelMapC.setAlpha(0f);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelDetailsC.setVisibility(View.INVISIBLE);
			mHotelReviewsC.setVisibility(View.INVISIBLE);
			mHotelGalleryC.setVisibility(View.INVISIBLE);
			mMapDimmer.setVisibility(View.INVISIBLE);
		}
		else {
			mBgHotelMapC.setAlpha(1f);
			if (hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelFiltersC.setVisibility(View.INVISIBLE);
				mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES
				|| hotelsState == ResultsHotelsState.REVIEWS
				|| hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP
				|| hotelsState == ResultsHotelsState.GALLERY
				|| hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| hotelsState == ResultsHotelsState.LOADING_HOTEL_LIST_UP) {
				mMapDimmer.setVisibility(View.VISIBLE);
				mMapDimmer.setAlpha(1f);
			}
			else {
				mMapDimmer.setVisibility(View.INVISIBLE);
				mMapDimmer.setAlpha(0f);
			}

			mHotelDetailsC.setVisibility(
				hotelsState == ResultsHotelsState.ROOMS_AND_RATES
					|| hotelsState == ResultsHotelsState.GALLERY
					? View.VISIBLE
					: View.INVISIBLE);

			mHotelReviewsC.setVisibility(
				hotelsState == ResultsHotelsState.REVIEWS
					? View.VISIBLE
					: View.INVISIBLE);

			mHotelGalleryC.setVisibility(
				hotelsState == ResultsHotelsState.GALLERY
					? View.VISIBLE
					: View.INVISIBLE
			);
		}
	}

	private void setFragmentState(ResultsHotelsState hotelsState) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean loadingGuiAvailable = false;
		boolean searchErrorAvailable = true;
		boolean hotelListAvailable = true;
		boolean hotelMapAvailable = true;
		boolean hotelFiltersAvailable = true;
		boolean hotelFilteredCountAvailable = true;
		boolean hotelRoomsAndRatesAvailable = true;
		boolean hotelReviewsAvailable = false;
		boolean hotelGalleryAvailable = false;

		if (hotelsState == ResultsHotelsState.LOADING) {
			loadingGuiAvailable = true;

			hotelMapAvailable = false;
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}

		if (hotelsState == ResultsHotelsState.LOADING_HOTEL_LIST_UP) {
			loadingGuiAvailable = true;
			hotelMapAvailable = true;

			hotelFiltersAvailable = true;
			hotelFilteredCountAvailable = true;
			hotelRoomsAndRatesAvailable = true;
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}

		if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES || hotelsState == ResultsHotelsState.REVIEWS) {
			hotelReviewsAvailable = true;
			searchErrorAvailable = false;
		}

		if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES || hotelsState == ResultsHotelsState.GALLERY) {
			hotelGalleryAvailable = true;
			searchErrorAvailable = false;
		}

		if (hotelsState == ResultsHotelsState.MAX_HOTEL_STAY || hotelsState == ResultsHotelsState.ZERO_RESULT || hotelsState == ResultsHotelsState.SEARCH_ERROR) {
			loadingGuiAvailable = true;
			hotelMapAvailable = false;
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
			hotelListAvailable = false;
		}

		mHotelListFrag = FragmentAvailabilityUtils.setFragmentAvailability(hotelListAvailable,
			FTAG_HOTEL_LIST, manager, transaction, this, R.id.column_one_hotel_list, false);
		mHotelFiltersFrag = FragmentAvailabilityUtils.setFragmentAvailability(hotelFiltersAvailable,
			FTAG_HOTEL_FILTERS, manager, transaction, this, R.id.column_two_hotel_filters, false);
		mHotelFilteredCountFrag = FragmentAvailabilityUtils.setFragmentAvailability(hotelFilteredCountAvailable,
			FTAG_HOTEL_FILTERED_COUNT, manager, transaction, this, R.id.column_three_hotel_filtered_count, false);
		mMapFragment = FragmentAvailabilityUtils.setFragmentAvailability(hotelMapAvailable,
			FTAG_HOTEL_MAP, manager, transaction, this, R.id.bg_hotel_map, true);
		mHotelDetailsFrag = FragmentAvailabilityUtils.setFragmentAvailability(hotelRoomsAndRatesAvailable,
			FTAG_HOTEL_DETAILS, manager, transaction, this, R.id.hotel_details, false);
		mHotelReviewsFrag = FragmentAvailabilityUtils.setFragmentAvailability(hotelReviewsAvailable,
			FTAG_HOTEL_REVIEWS, manager, transaction, this, R.id.hotel_reviews, false);
		mHotelGalleryFrag = FragmentAvailabilityUtils.setFragmentAvailability(hotelGalleryAvailable,
			FTAG_HOTEL_GALLERY, manager, transaction, this, R.id.hotel_gallery, false);
		mHotelSearchDownloadFrag = FragmentAvailabilityUtils.setFragmentAvailability(true,
			FTAG_HOTEL_SEARCH_DOWNLOAD, manager, transaction, this, 0, false);
		mLoadingGuiFrag = FragmentAvailabilityUtils.setFragmentAvailability(loadingGuiAvailable,
			FTAG_HOTEL_LOADING_INDICATOR, manager, transaction, this, R.id.loading_container, false);
		mSearchErrorFrag = FragmentAvailabilityUtils.setFragmentAvailability(searchErrorAvailable,
			FTAG_HOTEL_SEARCH_ERROR, manager, transaction, this, R.id.column_one_hotel_search_error, false);

		transaction.commit();
	}

	private void setListState(ResultsHotelsState state) {
		if (mHotelListFrag != null) {

			mHotelListFrag.setTopRightTextButtonText(getString(R.string.sort_and_filter));
			mHotelListFrag.setTopSpacePixels(mGrid.getRowHeight(1));

			// Button and locking
			switch (state) {
			case LOADING:
			case ZERO_RESULT:
			case MAX_HOTEL_STAY:
			case SEARCH_ERROR:
			case HOTEL_LIST_DOWN:
				mHotelListFrag.setPercentage(1f, 0);
				mHotelListFrag.setListLockedToTop(false);
				mHotelListFrag.setTopRightTextButtonVisibility(true);
				break;
			case HOTEL_LIST_UP:
			case LOADING_HOTEL_LIST_UP:
				mHotelListFrag.setPercentage(0f, 0);
				mHotelListFrag.setListLockedToTop(false);
				mHotelListFrag.setTopRightTextButtonVisibility(true);
				break;
			case ROOMS_AND_RATES: {
				mHotelListFrag.setListLockedToTop(true);
				mHotelListFrag.setTopRightTextButtonVisibility(true);
				break;
			}
			case HOTEL_LIST_AND_FILTERS: {
				mHotelListFrag.setListLockedToTop(true);
				mHotelListFrag.setTopRightTextButtonVisibility(false);
				break;
			}
			case ADDING_HOTEL_TO_TRIP: {
				mHotelListFrag.setListLockedToTop(true);
				mHotelListFrag.setTopRightTextButtonVisibility(false);
				break;
			}
			}

			// List scroll position
			mHotelListFrag.setListenerEnabled(mListStateHelper, false);
			if (state == ResultsHotelsState.HOTEL_LIST_DOWN || state == ResultsHotelsState.LOADING
				|| state.isShowMessageState()) {
				mHotelListFrag.setPercentage(1f, 0);
			}
			else if (mHotelListFrag.hasList()
				&& mHotelListFrag.getPercentage() > 0) {
				mHotelListFrag.setPercentage(0f, 0);
			}
			mHotelListFrag.setListenerEnabled(mListStateHelper, true);

			// Let's disable Sort & Filter text if ResultsHotelsState.REVIEWS
			mHotelListFrag.setTopRightTextButtonEnabled(state != ResultsHotelsState.REVIEWS);
		}
	}

	/*
	 * FRAGMENT STUFF
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_HOTEL_LIST) {
			frag = mHotelListFrag;
		}
		else if (tag == FTAG_HOTEL_FILTERS) {
			frag = mHotelFiltersFrag;
		}
		else if (tag == FTAG_HOTEL_FILTERED_COUNT) {
			frag = mHotelFilteredCountFrag;
		}
		else if (tag == FTAG_HOTEL_MAP) {
			frag = mMapFragment;
		}
		else if (tag == FTAG_HOTEL_DETAILS) {
			frag = mHotelDetailsFrag;
		}
		else if (tag == FTAG_HOTEL_SEARCH_DOWNLOAD) {
			frag = mHotelSearchDownloadFrag;
		}
		else if (tag == FTAG_HOTEL_LOADING_INDICATOR) {
			frag = mLoadingGuiFrag;
		}
		else if (tag == FTAG_HOTEL_SEARCH_ERROR) {
			frag = mSearchErrorFrag;
		}
		else if (tag == FTAG_HOTEL_REVIEWS) {
			frag = mHotelReviewsFrag;
		}
		else if (tag == FTAG_HOTEL_GALLERY) {
			frag = mHotelGalleryFrag;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_HOTEL_LIST) {
			frag = new ResultsHotelListFragment();
		}
		else if (tag == FTAG_HOTEL_FILTERS) {
			frag = new ResultsHotelsFiltersFragment();
		}
		else if (tag == FTAG_HOTEL_FILTERED_COUNT) {
			frag = new ResultsHotelsFilterCountFragment();
		}
		else if (tag == FTAG_HOTEL_MAP) {
			frag = HotelMapFragment.newInstance();
		}
		else if (tag == FTAG_HOTEL_DETAILS) {
			frag = ResultsHotelDetailsFragment.newInstance();
		}
		else if (tag == FTAG_HOTEL_SEARCH_DOWNLOAD) {
			importSearchParams();
			frag = HotelSearchDownloadFragment.newInstance(Db.getHotelSearch().getSearchParams());
		}
		else if (tag == FTAG_HOTEL_LOADING_INDICATOR) {
			frag = ResultsListLoadingFragment.newInstance(LineOfBusiness.HOTELS);
		}
		else if (tag == FTAG_HOTEL_SEARCH_ERROR) {
			frag = ResultsListSearchErrorFragment.newInstance();
		}
		else if (tag == FTAG_HOTEL_REVIEWS) {
			frag = ResultsHotelReviewsFragment.newInstance();
		}
		else if (tag == FTAG_HOTEL_GALLERY) {
			frag = ResultsHotelGalleryFragment.newInstance();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		switch (tag) {
		case FTAG_HOTEL_MAP:
			HotelMapFragment mapFrag = (HotelMapFragment) frag;
			updateMapFragmentPositioningInfo(mapFrag);
			break;
		case FTAG_HOTEL_LIST:
			ResultsHotelListFragment listFrag = (ResultsHotelListFragment) frag;
			listFrag.setTopSpacePixels(mGrid.getRowHeight(1));
			break;
		}

	}

	private void updateMapFragmentPositioningInfo(HotelMapFragment mapFrag) {
		if (mapFrag != null && mGrid.getTotalWidth() > 0) {
			mapFrag.setResultsViewWidth(mGrid.getColWidth(0));
			mapFrag.setFilterViewWidth(mGrid.getColLeft(2));
			if (mapFrag.isReady()) {
				mapFrag.notifySearchComplete();
			}
		}
	}

	/*
	 * ISortAndFilterListener Functions
	 */

	@Override
	public void onSortAndFilterClicked() {
		ResultsHotelsState state = mHotelsStateManager.getState();
		if (state == ResultsHotelsState.HOTEL_LIST_UP) {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_AND_FILTERS, true);
			OmnitureTracking.trackTabletHotelsSortAndFilterOpen();
		}
		else if (state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
		}
		else if (state == ResultsHotelsState.ROOMS_AND_RATES) {
			mHotelsStateManager.animateThroughStates(true, ResultsHotelsState.HOTEL_LIST_UP,
				ResultsHotelsState.HOTEL_LIST_AND_FILTERS);
		}
	}

	/*
	 * IResultsHotelSelectedListener Functions
	 */

	@Override
	public void onHotelSelected() {
		ResultsHotelsState state = mHotelsStateManager.getState();
		if (state == ResultsHotelsState.ROOMS_AND_RATES) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, false);
		}
		else if (state == ResultsHotelsState.HOTEL_LIST_DOWN) {
			mHotelsStateManager
				.animateThroughStates(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.ROOMS_AND_RATES);
		}
		else {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
		}
		OmnitureTracking.trackPageLoadHotelsInfosite(getActivity());
		AdTracker.trackHotelInfoSite();
	}

	private void updateFragsForRoomsAndRates() {
		Property property = Db.getHotelSearch().getSelectedProperty();
		if (property == null) {
			return;
		}
		Log.d("TabletResultsHotelControllerFragment.updateFragsForRoomsAndRates");
		TimingLogger logger = new TimingLogger("TabletResultsHotelControllerFragment",
			"updateFragsForRoomsAndRates");
		if (mMapFragment != null && mMapFragment.isAdded()) {
			mMapFragment.onHotelSelected(mHotelDetailsFrag == null ? 0 : mHotelDetailsFrag.getTailHeight());
			logger.addSplit("mMapFragment.onHotelSelected()");
		}
		if (mHotelDetailsFrag != null && mHotelDetailsFrag.isAdded()) {
			mHotelDetailsFrag.onHotelSelected();
			logger.addSplit("mHotelDetailsFrag.onHotelSelected()");
		}
		if (mHotelReviewsFrag != null && mHotelReviewsFrag.isAdded()) {
			mHotelReviewsFrag.onHotelSelected();
			logger.addSplit("mHotelReviewsFrag.onHotelSelected()");
		}
		if (mHotelGalleryFrag != null && mHotelGalleryFrag.isAdded()) {
			mHotelGalleryFrag.onHotelSelected();
			logger.addSplit("mHotelGalleryFrag.onHotelSelected()");
		}
		logger.dumpToLog();
	}

	/*
	 * IResultsHotelReviewsClickedListener Functions
	 */

	@Override
	public void onHotelReviewsClicked() {
		setHotelsState(ResultsHotelsState.REVIEWS, true);
		OmnitureTracking.trackPageLoadHotelsDetailsReviews();
	}

	/*
	 * IResultsHotelReviewsBackClickedListener Functions
	 */

	@Override
	public void onHotelReviewsBackClicked() {
		setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
	}

	/*
	 * IResultsHotelGalleryClickedListener Functions
	 */

	@Override
	public void onHotelGalleryClicked() {
		setHotelsState(ResultsHotelsState.GALLERY, true);
	}

	/*
	 * IResultsHotelGalleryBackClickedListener Functions
	 */

	@Override
	public void onHotelGalleryBackClicked() {
		setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
	}

	/*
	 * HotelMapFragmentListener
	 */

	public TouchableFrameLayout.TouchListener mMapTouchListener = new TouchableFrameLayout.TouchListener() {
		@Override
		public void onInterceptTouch(MotionEvent ev) {
			if (getHotelsState() == ResultsHotelsState.HOTEL_LIST_UP && !mGrid.isLandscape()) {
				setHotelsState(ResultsHotelsState.MAP, true);
			}
		}

		@Override
		public void onTouch(MotionEvent ev) {
			// ignore
		}
	};

	/**
	 * MapDimmer FrameLayout is visible when state is set to {@link ResultsHotelsState#ROOMS_AND_RATES} i.e. below the HotelDetailsFragment.
	 * This is a {@link TouchableFrameLayout.TouchListener} that listens to user touches/taps on the MapDimmer view.
	 * <p/>
	 * When user taps on this view when in {@link ResultsHotelsState#ROOMS_AND_RATES} let's close hotel details view and show more of the map.
	 */

	public TouchableFrameLayout.TouchListener mMapDimmerTouchListener = new TouchableFrameLayout.TouchListener() {
		@Override
		public void onInterceptTouch(MotionEvent ev) {
			if (getHotelsState() == ResultsHotelsState.ROOMS_AND_RATES) {
				if (mGrid.isLandscape()) {
					setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
				}
				else {
					setHotelsState(ResultsHotelsState.MAP, true);
				}
			}
		}

		@Override
		public void onTouch(MotionEvent ev) {
			// ignore
		}
	};

	@Override
	public void onMapClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPropertyClicked(Property property) {
		Db.getHotelSearch().setSelectedProperty(property);
		onHotelSelected();
	}

	@Override
	public void onExactLocationClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPropertyBubbleClicked(Property property) {
		if (mMapFragment != null && mMapFragment.isAdded()) {
			mMapFragment.hideBallon(property);
		}
		onPropertyClicked(property);
		OmnitureTracking.trackLinkHotelPinClick();
	}

	@Override
	public void onHotelMapFragmentAttached(HotelMapFragment fragment) {

	}

	/*
	 * SupportMapFragmentListener
	 */

	@Override
	public void onMapLayout() {
		if (mMapFragment != null && isResumed()) {
			mMapFragment.setShowInfoWindow(false);
			updateMapFragmentPositioningInfo(mMapFragment);
		}
	}

	/*
	 * LIST STATE LISTENER
	 */

	private StateListenerHelper<ResultsHotelsListState> mListStateHelper = new StateListenerHelper<ResultsHotelsListState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
			if (shouldWeListenToScroll()) {
				startStateTransition(getHotelsStateFromListState(stateOne), getHotelsStateFromListState(stateTwo));
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo,
											float percentage) {
			if (shouldWeListenToScroll()) {
				updateStateTransition(getHotelsStateFromListState(stateOne), getHotelsStateFromListState(stateTwo),
					percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
			if (shouldWeListenToScroll()) {
				endStateTransition(getHotelsStateFromListState(stateOne), getHotelsStateFromListState(stateTwo));
			}
		}

		@Override
		public void onStateFinalized(ResultsHotelsListState state) {
			if (shouldWeListenToScroll()) {
				ResultsHotelsState hotelsStateFromListState = getHotelsStateFromListState(state);
				if (hotelsStateFromListState != mHotelsStateManager.getState()) {
					setHotelsState(hotelsStateFromListState, false);
				}
			}
		}

		private boolean shouldWeListenToScroll() {
			return mHotelsStateManager.hasState() && (
				mHotelsStateManager.getState() == ResultsHotelsState.HOTEL_LIST_DOWN
					|| mHotelsStateManager.getState() == ResultsHotelsState.HOTEL_LIST_UP);
		}

		private ResultsHotelsState getHotelsStateFromListState(ResultsHotelsListState state) {
			if (state == ResultsHotelsListState.HOTELS_LIST_AT_TOP) {
				return ResultsHotelsState.HOTEL_LIST_UP;
			}
			else if (state == ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM) {
				return getBaseState();
			}
			return null;
		}

	};

	/*
	 * RESULTS STATE LISTENER
	 */

	public StateListenerHelper<ResultsState> getResultsListener() {
		return mResultsStateHelper;
	}

	private StateListenerHelper<ResultsState> mResultsStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				startStateTransition(ResultsHotelsState.HOTEL_LIST_DOWN, ResultsHotelsState.HOTEL_LIST_UP);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				startStateTransition(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.HOTEL_LIST_DOWN);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				mRootC.setVisibility(View.VISIBLE);
				mRootC.setAlpha(0f);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				updateStateTransition(ResultsHotelsState.HOTEL_LIST_DOWN, ResultsHotelsState.HOTEL_LIST_UP, percentage);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				updateStateTransition(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.HOTEL_LIST_DOWN, percentage);
			}
			else if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				mRootC.setAlpha(1f - percentage);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				mRootC.setAlpha(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				endStateTransition(ResultsHotelsState.HOTEL_LIST_DOWN, ResultsHotelsState.HOTEL_LIST_UP);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				endStateTransition(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.HOTEL_LIST_DOWN);
			}
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			// Make sure we are showing ourselves in the ResultsState.OVERVIEW. This can happen
			// When transitioning back to OVERVIEW from flights mode SpUpdate. #3245
			if (state == ResultsState.OVERVIEW) {
				mRootC.setAlpha(1f);
			}

			if (state != ResultsState.HOTELS) {
				setHotelsState(getBaseState(), false);
			}
			else {
				if (mHotelsStateManager.hasState()
					&& mHotelsStateManager.getState() == ResultsHotelsState.HOTEL_LIST_DOWN) {
					setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, false);
				}
				else {
					// The activity is still telling us something, so we better refresh our state.
					setHotelsState(mHotelsStateManager.getState(), false);
				}
			}
		}

	};

	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			if (isLandscape) {
				mGrid.setDimensions(totalWidth, totalHeight);

				//3 rows (AB,top half, bottom half)
				//5 columns - left, spacer,center,spacer,right
				mGrid.setGridSize(3, 5);

				//The top row matches the height of the actionbar
				mGrid.setRowSize(0, getActivity().getActionBar().getHeight());
				mGrid.setRowPercentage(2, getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1));

				int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
				mGrid.setColumnSize(1, spacerSize);
				mGrid.setColumnSize(3, spacerSize);

				//Tell all of the containers where they belong
				mGrid.setContainerToColumn(mLoadingC, 0);
				mGrid.setContainerToColumn(mSearchErrorC, 0);
				mGrid.setContainerToColumn(mHotelListC, 0);
				mGrid.setContainerToColumnSpan(mHotelFiltersC, 1, 2);
				mGrid.setContainerToColumnSpan(mHotelFilteredCountC, 3, 4);
				mGrid.setContainerToColumnSpan(mBgHotelMapC, 0, 4);
				mGrid.setContainerToColumnSpan(mBgHotelMapTouchDelegateC, 0, 4);
				mGrid.setContainerToColumnSpan(mHotelDetailsC, 2, 4);
				mGrid.setContainerToColumnSpan(mHotelReviewsC, 2, 4);

				//All of the views except for the map sit below the action bar
				mGrid.setContainerToRowSpan(mLoadingC, 1, 2);
				mGrid.setContainerToRow(mSearchErrorC, 2);
				mGrid.setContainerToRowSpan(mHotelListC, 1, 2);
				mGrid.setContainerToRowSpan(mHotelFiltersC, 1, 2);
				mGrid.setContainerToRowSpan(mHotelFilteredCountC, 1, 2);
				mGrid.setContainerToRowSpan(mBgHotelMapC, 0, 2);
				mGrid.setContainerToRowSpan(mBgHotelMapTouchDelegateC, 0, 2);
				mGrid.setContainerToRowSpan(mHotelDetailsC, 1, 2);
				mGrid.setContainerToRowSpan(mHotelReviewsC, 1, 2);

				//tell the map where its bounds are
				updateMapFragmentPositioningInfo(mMapFragment);

				if (mHotelListFrag != null) {
					mHotelListFrag.setTopSpacePixels(mGrid.getRowHeight(1));
				}
			}
			else {
				mGrid.setDimensions(totalWidth, totalHeight);

				//3 rows (AB,top half, bottom half)
				//3 columns - left, spacer, right
				mGrid.setGridSize(3, 3);

				//The top row matches the height of the actionbar
				mGrid.setRowSize(0, getActivity().getActionBar().getHeight());
				mGrid.setRowPercentage(2, getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1));

				int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
				mGrid.setColumnSize(1, spacerSize);

				//Tell all of the containers where they belong
				mGrid.setContainerToColumn(mLoadingC, 0);
				mGrid.setContainerToColumn(mSearchErrorC, 0);
				mGrid.setContainerToColumn(mHotelListC, 0);
				mGrid.setContainerToColumn(mHotelFiltersC, 2);
				mGrid.setContainerToColumn(mHotelFilteredCountC, 2);
				mGrid.setContainerToColumnSpan(mBgHotelMapC, 0, 2);
				mGrid.setContainerToColumnSpan(mBgHotelMapTouchDelegateC, 0, 2);
				mGrid.setContainerToColumnSpan(mHotelDetailsC, 0, 2);
				mGrid.setContainerToColumnSpan(mHotelReviewsC, 0, 2);

				//All of the views except for the map sit below the action bar
				mGrid.setContainerToRowSpan(mLoadingC, 1, 2);
				mGrid.setContainerToRow(mSearchErrorC, 2);
				mGrid.setContainerToRowSpan(mHotelListC, 1, 2);
				mGrid.setContainerToRowSpan(mHotelFiltersC, 1, 2);
				mGrid.setContainerToRowSpan(mHotelFilteredCountC, 1, 2);
				mGrid.setContainerToRowSpan(mBgHotelMapC, 0, 2);
				mGrid.setContainerToRowSpan(mBgHotelMapTouchDelegateC, 0, 2);
				mGrid.setContainerToRowSpan(mHotelDetailsC, 1, 2);
				mGrid.setContainerToRowSpan(mHotelReviewsC, 1, 2);

				//tell the map where its bounds are
				updateMapFragmentPositioningInfo(mMapFragment);

				if (mHotelListFrag != null) {
					mHotelListFrag.setTopSpacePixels(mGrid.getRowHeight(1));
				}
			}
		}

	};

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			ResultsHotelsState state = mHotelsStateManager.getState();
			if (state != getBaseState()) {
				if (mHotelsStateManager.isAnimating()) {
					// If we are in the middle of state transition, just reverse it
					setHotelsState(state, true);
					return true;
				}
				else {
					if (state == ResultsHotelsState.HOTEL_LIST_UP) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_DOWN, true);
						return true;
					}
					else if (state == ResultsHotelsState.MAP) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						return true;
					}
					else if (state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						return true;
					}
					else if (state == ResultsHotelsState.ROOMS_AND_RATES) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						Db.getHotelSearch().setSelectedProperty(null);
						return true;
					}
					else if (state == ResultsHotelsState.REVIEWS) {
						setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
						return true;
					}
					else if (state == ResultsHotelsState.GALLERY) {
						setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
						return true;
					}
					else if (state == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
						// We return true here, because we want to block back presses in this state
						return true;
					}
				}
			}
			return false;
		}

	};

	/*
	 * HOTELS STATE PROVIDER
	 */
	private StateListenerCollection<ResultsHotelsState> mHotelsStateListeners = new StateListenerCollection<ResultsHotelsState>();

	@Override
	public void startStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
		mHotelsStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo,
									  float percentage) {
		mHotelsStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
		mHotelsStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsHotelsState state) {
		mHotelsStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsHotelsState> listener, boolean fireFinalizeState) {
		mHotelsStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsHotelsState> listener) {
		mHotelsStateListeners.unRegisterStateListener(listener);
	}

	public void setListenerActive(IStateListener<ResultsHotelsState> listener, boolean active) {
		if (active) {
			mHotelsStateListeners.setListenerActive(listener);
		}
		else {
			mHotelsStateListeners.setListenerInactive(listener);
		}
	}

	/*
	 * HOTELS STATE LISTENER
	 */

	private StateListenerHelper<ResultsHotelsState> mHotelsStateHelper = new StateListenerHelper<ResultsHotelsState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {

			setTouchable(false, stateTwo == ResultsHotelsState.HOTEL_LIST_UP
				|| stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN);

			if ((stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES)
				|| (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP)) {
				// SHOWING ROOMS AND RATES
				mHotelListFrag.setListLockedToTop(true);
				setRoomsAndRatesAnimationVisibilities();
				setRoomsAndRatesAnimationHardwareRendering(true);
			}
			else if ((stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.MAP)
				|| (stateOne == ResultsHotelsState.MAP && stateTwo == ResultsHotelsState.HOTEL_LIST_UP)) {
				mHotelListC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				// SHOWING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				// HIDING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if (stateOne == ResultsHotelsState.ADDING_HOTEL_TO_TRIP
				&& stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				mHotelListFrag.setListLockedToTop(false);

			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				// ADD TO HOTEL
				mHotelListFrag.setListLockedToTop(true);
				setAddToTripAnimationVis(true);
				setAddToTripAnimationHardwareRendering(true);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.REVIEWS) {
				setReviewsAnimationHardwareRendering(true);
				setReviewsAnimationVisibilities(true);
				mHotelDetailsFrag.saveScrollPosition();
			}
			else if (stateOne == ResultsHotelsState.REVIEWS && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				setReviewsAnimationHardwareRendering(true);
				setReviewsAnimationVisibilities(false);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.GALLERY) {
				setGalleryAnimationHardwareRendering(true);
				setGalleryAnimationVisibilities(true);
			}
			else if (stateOne == ResultsHotelsState.GALLERY && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				setGalleryAnimationHardwareRendering(true);
				setGalleryAnimationVisibilities(false);
			}
			else if (stateTwo == ResultsHotelsState.LOADING) {
				mLoadingC.setAlpha(0.0f);
				mLoadingC.setTranslationY(mGrid.getRowTop(2) - mGrid.getRowHeight(0));
			}
			else if (stateOne == ResultsHotelsState.LOADING && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				mLoadingC.setAlpha(1.0f);
			}
			else if (stateTwo == ResultsHotelsState.LOADING_HOTEL_LIST_UP) {
				mLoadingC.setTranslationY(0f);
			}
			else if (stateOne == ResultsHotelsState.LOADING_HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				mLoadingC.setAlpha(1.0f);
				mHotelListFrag.setPercentage(0f, 0);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsState stateOne, ResultsHotelsState stateTwo,
											float percentage) {
			if (stateOne == ResultsHotelsState.HOTEL_LIST_DOWN && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				// ENTERING HOTELS
				mBgHotelMapC.setAlpha(percentage);
				mHotelListFrag.setPercentage(1f - percentage, 0);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				// LEAVING HOTELS
				mBgHotelMapC.setAlpha(1f - percentage);
				mHotelListFrag.setPercentage(percentage, 0);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.MAP) {
				mHotelListC.setTranslationX(percentage * -mHotelListC.getWidth());
			}
			else if (stateOne == ResultsHotelsState.MAP && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				mHotelListC.setTranslationX((1f - percentage) * -mHotelListC.getWidth());
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				// SHOWING ROOMS AND RATES
				setRoomsAndRatesShownPercentage(percentage);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				// HIDING ROOMS AND RATES
				setRoomsAndRatesShownPercentage(1f - percentage);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.REVIEWS) {
				// SHOWING REVIEWS
				setReviewsShownPercentage(percentage);
			}
			else if (stateOne == ResultsHotelsState.REVIEWS && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				// HIDING REVIEWS
				setReviewsShownPercentage(1f - percentage);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.GALLERY) {
				// SHOWING GALLERY
				setGalleryShownPercentage(percentage);
			}
			else if (stateOne == ResultsHotelsState.GALLERY && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				// HIDING GALLERY
				setGalleryShownPercentage(1f - percentage);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				// SHOWING FILTERS
				setHotelsFiltersShownPercentage(percentage);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				// HIDING FILTERS
				setHotelsFiltersShownPercentage(1f - percentage);

			}
			else if (stateOne == ResultsHotelsState.ADDING_HOTEL_TO_TRIP
				&& stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				setRoomsAndRatesShownPercentage(1f - percentage);
				mBgHotelMapC.setAlpha(1f - percentage);
				mHotelListFrag.setPercentage(percentage, 0);
			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				// ADD TO HOTEL
				setAddToTripPercentage(percentage);
			}
			else if (stateTwo == ResultsHotelsState.LOADING) {
				mLoadingC.setAlpha(percentage);
			}
			else if (stateOne == ResultsHotelsState.LOADING && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				mLoadingC.setAlpha(1.0f - percentage);
			}
			else if (stateOne == ResultsHotelsState.LOADING_HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				mLoadingC.setAlpha(1.0f - percentage);
			}

		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				// SHOWING/HIDING ROOMS AND RATES
				setRoomsAndRatesAnimationHardwareRendering(false);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				// SHOWING/HIDING ROOMS AND RATES
				setRoomsAndRatesAnimationHardwareRendering(false);
				mHotelListFrag.clearSelectedProperty();
			}
			else if ((stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.MAP)
				|| (stateOne == ResultsHotelsState.MAP && stateTwo == ResultsHotelsState.HOTEL_LIST_UP)) {
				mHotelListC.setLayerType(View.LAYER_TYPE_NONE, null);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				// SHOWING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
				// HIDING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				// ADD TO HOTEL
				setAddToTripAnimationVis(false);
				setAddToTripAnimationHardwareRendering(false);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.REVIEWS) {
				setReviewsAnimationHardwareRendering(false);
			}
			else if (stateOne == ResultsHotelsState.REVIEWS && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				setReviewsAnimationHardwareRendering(false);
				setReviewsAnimationVisibilities(false);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.GALLERY) {
				setGalleryAnimationHardwareRendering(false);
			}
			else if (stateOne == ResultsHotelsState.GALLERY && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				setGalleryAnimationHardwareRendering(false);
				setGalleryAnimationVisibilities(false);
			}
			else if (stateTwo == ResultsHotelsState.LOADING) {
				mLoadingC.setAlpha(1.0f);
			}
			else if (stateOne == ResultsHotelsState.LOADING && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				mLoadingC.setAlpha(0.0f);
			}
			else if (stateOne == ResultsHotelsState.LOADING_HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				mLoadingC.setAlpha(0.0f);
			}


			setTouchable(true, true);
		}

		@Override
		public void onStateFinalized(ResultsHotelsState state) {
			TimingLogger logger = new TimingLogger("TabletResultsHotelControllerFragment", "onStateFinalized");
			setVisibilityState(state);
			logger.addSplit("setVisibilityState");
			setTouchState(state);
			logger.addSplit("setTouchState");
			setFragmentState(state);
			logger.addSplit("setFragmentState");
			setListState(state);
			logger.addSplit("setListState");

			switch (state) {
			case HOTEL_LIST_DOWN:
				mLoadingC.setAlpha(1.0f);
				mBgHotelMapC.setAlpha(0f);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(0f);
				if (mHotelsDeepLink) {
					mHotelsDeepLink = false;
					OmnitureTracking.trackTabletSearchResultsPageLoad(Sp.getParams());
				}
				break;
			case HOTEL_LIST_UP:
				mBgHotelMapC.setAlpha(1f);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(0f);
				OmnitureTracking.trackTabletHotelListOpen(Db.getHotelSearch().getSearchParams(),
					Db.getHotelSearch().getSearchResponse());
				break;
			case MAP:
				mHotelListC.setTranslationX(-mHotelListC.getWidth());
				break;
			case ROOMS_AND_RATES: {
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(1f);
				setReviewsShownPercentage(0f);
				updateFragsForRoomsAndRates();
				break;
			}
			case REVIEWS: {
				setReviewsShownPercentage(1f);
				break;
			}
			case HOTEL_LIST_AND_FILTERS: {
				setHotelsFiltersShownPercentage(1f);
				setAddToTripPercentage(0f);
				break;
			}
			case ADDING_HOTEL_TO_TRIP: {
				setAddToTripPercentage(1f);
				setHotelsState(ResultsHotelsState.HOTEL_LIST_DOWN, true);
				break;
			}
			case SEARCH_ERROR:
			case ZERO_RESULT:
			case MAX_HOTEL_STAY:
				if (mSearchErrorFrag.isAdded()) {
					mSearchErrorFrag.setState(state);
				}
				break;
			case LOADING:
				mLoadingC.setTranslationY(mGrid.getRowTop(2) - mGrid.getRowHeight(0));
				break;
			case LOADING_HOTEL_LIST_UP:
				mLoadingC.setTranslationY(0f);
				mHotelListFrag.setLastReportedTouchPercentage(0f);
				break;
			}
			logger.addSplit("Switch Statement");

			if (mMapFragment != null && !mHotelsStateManager.isChaining() && state != ResultsHotelsState.ROOMS_AND_RATES
				&& state != ResultsHotelsState.REVIEWS && state != ResultsHotelsState.GALLERY) {
				mMapFragment.setMapPaddingFromResultsHotelsState(state);
			}
			logger.addSplit("mMapFragment.setMapPaddingFromFilterState");


			// Ensure we are downloading the correct data.
			if (Ui.isAdded(mHotelSearchDownloadFrag) && state == ResultsHotelsState.LOADING && HotelUtils.dateRangeSupportsHotelSearch(getActivity())) {
				importSearchParams();
				logger.addSplit("importSearchParams()");
				mHotelSearchDownloadFrag.startOrResumeForParams(Db.getHotelSearch().getSearchParams());
				logger.addSplit("mHotelSearchDownloadFrag.startOrResumeForParams");
			}
			logger.dumpToLog();
		}

		/*
		 * SHOW FILTERS ANIMATION STUFF
		 */
		private void setHotelsFiltersAnimationVisibilities(boolean start) {
			mHotelFiltersC.setVisibility(View.VISIBLE);
			mHotelFilteredCountC.setVisibility(View.VISIBLE);
			mMapDimmer.setVisibility(View.VISIBLE);
		}

		private void setHotelsFiltersAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelFiltersC.setLayerType(layerValue, null);
			mHotelFilteredCountC.setLayerType(layerValue, null);
			mMapDimmer.setLayerType(layerValue, null);
		}

		private void setHotelsFiltersShownPercentage(float percentage) {
			float filtersLeft = -(1f - percentage) * mGrid.getColLeft(2);
			mHotelFiltersC.setTranslationX(filtersLeft);

			if (mGrid.isLandscape()) {
				float filteredCountLeft = mGrid.getColWidth(4) * (1f - percentage);
				mHotelFilteredCountC.setTranslationX(filteredCountLeft);
			}
			else {
				mHotelFilteredCountC.setVisibility(View.GONE);
			}

			mMapDimmer.setAlpha(percentage);
		}

		/*
		 * SHOW ROOMS AND RATES ANIMATION STUFF
		 */

		private void setRoomsAndRatesAnimationVisibilities() {
			mHotelDetailsC.setVisibility(View.VISIBLE);
			mMapDimmer.setAlpha(0f);
			mMapDimmer.setVisibility(View.VISIBLE);
		}

		private void setRoomsAndRatesAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelDetailsC.setLayerType(layerValue, null);
			mMapDimmer.setLayerType(layerValue, null);
		}

		private void setRoomsAndRatesShownPercentage(float percentage) {
			mHotelDetailsC.setTranslationY(-(1f - percentage) * mGrid.getTotalHeight());
			mMapDimmer.setAlpha(percentage);
			translateListForPortraitDetailsMode(percentage);
		}

		/*
		 * SHOW REVIEWS ANIMATION STUFF
		 */

		private void setReviewsAnimationVisibilities(boolean forwards) {
			mHotelDetailsC.setVisibility(View.VISIBLE);
			mHotelReviewsC.setVisibility(View.VISIBLE);
			if (forwards) {
				mHotelReviewsC.setAlpha(0f);
			}
			else {
				mHotelReviewsC.setAlpha(1f);
			}
		}

		private void setReviewsShownPercentage(float percentage) {
			if (percentage < 0.5f) {
				mHotelDetailsFrag.setScrollBetweenSavedAndHeader(percentage * 2.0f);
				mHotelReviewsC.setAlpha(0f);
			}
			if (percentage > 0.6f) {
				float projectedPercentage = (percentage - 0.5f) * 2.0f;
				mHotelReviewsC.setAlpha(projectedPercentage);
			}
			if (percentage == 1.0f) {
				translateListForPortraitDetailsMode(percentage);
			}
		}

		private void translateListForPortraitDetailsMode(float percentage) {
			if (!mGrid.isLandscape()) {
				mHotelListC.setTranslationX(percentage * -mGrid.getColRight(0));
			}
		}

		private void setReviewsAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelReviewsC.setLayerType(layerValue, null);
		}

		/*
		 * SHOW GALLERY ANIMATION STUFF
		 */

		private void setGalleryAnimationVisibilities(boolean forwards) {
			mHotelGalleryC.setVisibility(View.VISIBLE);
		}

		private void setGalleryShownPercentage(float percentage) {
			mHotelGalleryFrag.setAnimationPercentage(percentage);
		}

		private void setGalleryAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelGalleryFrag.setHardwareLayer(layerValue);
		}

		/*
		 * ADD TO TRIP ANIMATION STUFF
		 */
		private void setAddToTripAnimationVis(boolean start) {

		}

		private void setAddToTripAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelDetailsFrag.setTransitionToAddTripHardwareLayer(layerType);
		}

		private void setAddToTripPercentage(float percentage) {
			if (mHotelDetailsFrag != null) {
				mHotelDetailsFrag.setTransitionToAddTripPercentage(percentage);
			}
		}

		/*
		 * Touch
		 */
		private void setTouchable(boolean touchable, boolean includingList) {
			mBgHotelMapC.setTouchPassThroughEnabled(touchable);
			mHotelFiltersC.setBlockNewEventsEnabled(touchable);
			mHotelFilteredCountC.setBlockNewEventsEnabled(touchable);
			mHotelDetailsC.setBlockNewEventsEnabled(touchable);
			if (includingList) {
				mHotelListC.setBlockNewEventsEnabled(touchable);
			}
		}
	};

	/*
	 * EXPEDIA SERVICES FRAG LISTENER
	 */

	@Subscribe
	public void onHotelSearchResponseAvailable(Events.HotelSearchResponseAvailable event) {
		Context context = getActivity();

		HotelSearchResponse response = event.response;

		// If we have a null response, the client should show the SEARCH_ERROR state.
		// There is too much logic surrounding whether the response is null or not
		// already, so the best solution is to add an empty response with an error.
		if (response == null) {

			response = new HotelSearchResponse();
			ServerError serverError = new ServerError();
			serverError.setCode("NULL_RESPONSE");
			response.addError(serverError);
		}
		Db.getHotelSearch().setSearchResponse(response);
		AdImpressionTracking.trackAdClickOrImpression(getActivity(), response.getBeaconUrl(), null);

		boolean isBadResponse = response.hasErrors();
		boolean isZeroResults = response.getPropertiesCount() == 0;

		if (!HotelUtils.dateRangeSupportsHotelSearch(getActivity())) {
			setHotelsState(ResultsHotelsState.MAX_HOTEL_STAY, false);
		}
		else if (isBadResponse) {
			setHotelsState(ResultsHotelsState.SEARCH_ERROR, false);
		}
		else if (isZeroResults) {
			setHotelsState(ResultsHotelsState.ZERO_RESULT, false);
		}
		else {
			handleNewDataAndChangeState(false);
			AdTracker.trackHotelSearch();
		}
	}

	@Subscribe
	public void onHotelOffersResponseAvailable(Events.HotelOffersResponseAvailable event) {
		HotelOffersResponse response = event.response;

		if (response == null || response.hasErrors()) {
			setHotelsState(ResultsHotelsState.SEARCH_ERROR, false);
		}
		else if (response.getProperty() != null) {
			HotelUtils.loadHotelOffersAsSearchResponse(response);
			Property property = response.getProperty();
			Db.getHotelSearch().setSelectedProperty(property);

			handleNewDataAndChangeState(true);
		}
	}

	private void handleNewDataAndChangeState(boolean showDetails) {
		if (mHotelListFrag != null && mHotelListFrag.isAdded()) {
			mHotelListFrag.updateAdapter();
		}
		if (mHotelsStateManager.getState() == ResultsHotelsState.LOADING_HOTEL_LIST_UP) {
			showDetails &= mHotelsDeepLink;
			mHotelsStateManager.setDefaultState(getBaseState());
			ResultsHotelsState state = showDetails ?
				ResultsHotelsState.ROOMS_AND_RATES : ResultsHotelsState.HOTEL_LIST_UP;
			setHotelsState(state, true);
		}
		else {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_DOWN, true);
		}
	}

	/**
	 * IAddToBucketListener
	 */
	@Override
	public void onItemAddedToBucket() {
		OmnitureTracking.trackAddAirAttachHotel();

		// TODO: EVENTUALLY WE WANT TO ANIMATE THIS THING!
		setHotelsState(ResultsHotelsState.ADDING_HOTEL_TO_TRIP, false);
	}

	/*
	IAcceptingListenersListener
	 */
	@Override
	public void acceptingListenersUpdated(Fragment frag, boolean acceptingListener) {
		if (acceptingListener) {
			if (frag == mHotelListFrag) {
				mHotelListFrag.getListView().setOnTouchListener(mListTouchListener);
				mHotelListFrag.registerStateListener(mListStateHelper, false);
			}
		}
		else {
			if (frag == mHotelListFrag) {
				mHotelListFrag.unRegisterStateListener(mListStateHelper);
			}
		}
	}

	public boolean listHasTouch() {
		return mListHasTouch;
	}

	public boolean listIsDisplaced() {
		float per = mHotelListFrag.getListView().getScrollDownPercentage();
		return per != 0f && per != 1f;
	}

	View.OnTouchListener mListTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mListener.isSiblingListBusy(LineOfBusiness.HOTELS)) {
				mListHasTouch = false;
				return true;
			}
			else {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					mListHasTouch = true;
				}
				else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
					mListHasTouch = false;
				}
				return false;
			}
		}
	};

	/**
	 * IResultsFilterDoneClickedListener
	 */
	@Override
	public void onFilterDoneClicked() {
		mMapFragment.reset();
		mMapFragment.notifyFilterChanged();
		AdTracker.trackFilteredHotelSearch();
	}
}
