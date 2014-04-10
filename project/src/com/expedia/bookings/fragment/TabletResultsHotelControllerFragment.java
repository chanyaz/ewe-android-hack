package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.ResultsHotelsListState;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.graphics.PercentageFadeColorDrawable;
import com.expedia.bookings.interfaces.IAddToBucketListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsHotelReviewsClickedListener;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.maps.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsHotelControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to HOTELS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsHotelControllerFragment extends Fragment implements
	ISortAndFilterListener, IResultsHotelSelectedListener, IFragmentAvailabilityProvider,
	HotelMapFragmentListener, SupportMapFragmentListener, IBackManageable, IStateProvider<ResultsHotelsState>,
	ExpediaServicesFragment.ExpediaServicesFragmentListener, IAddToBucketListener,
	IResultsHotelReviewsClickedListener {

	//State
	private static final String STATE_HOTELS_STATE = "STATE_HOTELS_STATE";

	//Frag tags
	private static final String FTAG_HOTEL_LIST = "FTAG_HOTEL_LIST";
	private static final String FTAG_HOTEL_FILTERS = "FTAG_HOTEL_FILTERS";
	private static final String FTAG_HOTEL_FILTERED_COUNT = "FTAG_HOTEL_FILTERED_COUNT";
	private static final String FTAG_HOTEL_MAP = "FTAG_HOTEL_MAP";
	private static final String FTAG_HOTEL_ROOMS_AND_RATES = "FTAG_HOTEL_ROOMS_AND_RATES";
	private static final String FTAG_HOTEL_SEARCH_DOWNLOAD = "FTAG_HOTEL_SEARCH_DOWNLOAD";
	private static final String FTAG_HOTEL_LOADING_INDICATOR = "FTAG_HOTEL_LOADING_INDICATOR";
	private static final String FTAG_HOTEL_SEARCH_ERROR = "FTAG_HOTEL_SEARCH_ERROR";
	private static final String FTAG_HOTEL_REVIEWS = "FTAG_HOTEL_REVIEWS";

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mHotelListC;
	private FrameLayoutTouchController mBgHotelMapC;
	private FrameLayoutTouchController mBgHotelMapTouchDelegateC;
	private FrameLayoutTouchController mHotelFiltersC;
	private FrameLayoutTouchController mHotelFilteredCountC;
	private FrameLayoutTouchController mHotelRoomsAndRatesC;
	private FrameLayoutTouchController mHotelReviewsC;
	private FrameLayoutTouchController mLoadingC;
	private FrameLayoutTouchController mSearchErrorC;

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

	//Other
	private StateManager<ResultsHotelsState> mHotelsStateManager = new StateManager<ResultsHotelsState>(
		ResultsHotelsState.LOADING, this);
	private GridManager mGrid = new GridManager();
	//private int mShadeColor = Color.argb(220, 0, 0, 0);
	private boolean mRoomsAndRatesInFront = true;//They start in front
	private PercentageFadeColorDrawable mBgHotelMapDimmerDrawable;

	private ArrayList<IResultsHotelSelectedListener> mHotelSelectedListeners = new ArrayList<IResultsHotelSelectedListener>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mHotelsStateManager.setDefaultState(ResultsHotelsState.valueOf(savedInstanceState.getString(
				STATE_HOTELS_STATE, getBaseState().name())));
		}

		if ((Db.getHotelSearch() == null || Db.getHotelSearch().getSearchResponse() == null) && !Db
			.loadHotelSearchFromDisk(getActivity())) {
			mHotelsStateManager.setDefaultState(ResultsHotelsState.LOADING);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_hotels, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mHotelListC = Ui.findView(view, R.id.column_one_hotel_list);
		mBgHotelMapC = Ui.findView(view, R.id.bg_hotel_map);
		mBgHotelMapTouchDelegateC = Ui.findView(view, R.id.bg_hotel_map_touch_delegate);
		mHotelFiltersC = Ui.findView(view, R.id.column_two_hotel_filters);
		mHotelFilteredCountC = Ui.findView(view, R.id.column_three_hotel_filtered_count);
		mHotelRoomsAndRatesC = Ui.findView(view, R.id.hotel_rooms_and_rates);
		mHotelReviewsC = Ui.findView(view, R.id.hotel_reviews);
		mLoadingC = Ui.findView(view, R.id.loading_container);
		mSearchErrorC = Ui.findView(view, R.id.column_one_hotel_search_error);

		//Default maps to be invisible (they get ignored by our setVisibilityState function so this is important)
		mBgHotelMapC.setAlpha(0f);
		int dimmedColor = getResources().getColor(R.color.map_dimmer);
		mBgHotelMapDimmerDrawable = new PercentageFadeColorDrawable(Color.TRANSPARENT, dimmedColor);
		mBgHotelMapDimmerDrawable.setPercentage(0);
		mBgHotelMapC.setForeground(mBgHotelMapDimmerDrawable);

		//Set up our maps touch passthrough. It is important to note that A) the touch receiver is set to be invisible,
		//so that when it gets a touch, it will pass to whatever is behind it. B) It must be the same size as the
		//the view sending it touch events, because no offsets or anything like that are performed. C) It must be
		//behind the view getting the original touch event, otherwise it will create a loop.
		mBgHotelMapTouchDelegateC.setVisibility(View.INVISIBLE);
		mBgHotelMapC.setTouchPassThroughReceiver(mBgHotelMapTouchDelegateC);

		registerStateListener(mHotelsStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsHotelsState>(), false);

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
		mResultsStateHelper.registerWithProvider(this, false);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
		Sp.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Sp.getBus().unregister(this);
		mResultsStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
	}

	/*
	 * NEW SEARCH PARAMS
	 */

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		if (mHotelsStateManager.getState() != ResultsHotelsState.LOADING) {
			setHotelsState(ResultsHotelsState.LOADING, false);
		}
		else {
			importSearchParams();
			mHotelSearchDownloadFrag.startOrResumeForParams(Db.getHotelSearch().getSearchParams());
		}
	}

	public void importSearchParams() {
		Db.getHotelSearch().setSearchResponse(null);
		Db.getHotelSearch().setSearchParams(Sp.getParams().toHotelSearchParams());
	}


	/**
	 * STATE HELPERS
	 */

	private ResultsHotelsState getBaseState() {
		if (Db.getHotelSearch() == null || Db.getHotelSearch().getSearchResponse() == null) {
			return ResultsHotelsState.LOADING;
		}
		else {
			return ResultsHotelsState.HOTEL_LIST_DOWN;
		}
	}

	public void setHotelsState(ResultsHotelsState state, boolean animate) {
		mHotelsStateManager.setState(state, animate);
	}

	public ResultsHotelsState getHotelsState(ResultsState state) {
		if (state != ResultsState.HOTELS) {
			return getBaseState();
		}
		else {
			return mHotelsStateManager.getState();
		}
	}

	private void setTouchState(ResultsHotelsState hotelsState) {
		if (hotelsState == ResultsHotelsState.LOADING) {
			mHotelListC.setBlockNewEventsEnabled(true);
		}
		else {
			mHotelListC.setBlockNewEventsEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN || hotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
			mBgHotelMapC.setTouchPassThroughEnabled(true);
		}
		else {
			mBgHotelMapC.setTouchPassThroughEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
		}
		else {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
			|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
			mHotelFiltersC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelFiltersC.setBlockNewEventsEnabled(true);
		}
	}

	private void setVisibilityState(ResultsHotelsState hotelsState) {
		mHotelListC.setVisibility(View.VISIBLE);
		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN || hotelsState == ResultsHotelsState.LOADING) {
			mBgHotelMapC.setAlpha(0f);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			mHotelReviewsC.setVisibility(View.INVISIBLE);
		}
		else {
			mBgHotelMapC.setAlpha(1f);
			if (hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelFiltersC.setVisibility(View.INVISIBLE);
				mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES
				|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS
				|| hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == ResultsHotelsState.REVIEWS) {
				mHotelReviewsC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelReviewsC.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void setFragmentState(ResultsHotelsState hotelsState) {
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = manager.beginTransaction();

		boolean hotelSearchDownloadAvailable = false;
		boolean loadingGuiAvailable = false;
		boolean searchErrorAvailable = false;
		boolean hotelListAvailable = true;
		boolean hotelMapAvailable = true;
		boolean hotelFiltersAvailable = true;
		boolean hotelFilteredCountAvailable = true;
		boolean hotelRoomsAndRatesAvailable = true;
		boolean hotelReviewsAvailable = false;

		if (hotelsState == ResultsHotelsState.LOADING || hotelsState == ResultsHotelsState.SEARCH_ERROR) {
			if (hotelsState == ResultsHotelsState.LOADING) {
				hotelSearchDownloadAvailable = true;
				loadingGuiAvailable = true;
				searchErrorAvailable = false;
			}
			else {
				hotelSearchDownloadAvailable = false;
				loadingGuiAvailable = false;
				searchErrorAvailable = true;
			}

			hotelListAvailable = false;
			hotelMapAvailable = false;
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}
		else if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}

		if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES || hotelsState == ResultsHotelsState.REVIEWS) {
			hotelReviewsAvailable = true;
		}

		//TODO: WE MAY WANT TO REMOVE SOME HEAVIER FRAGMENTS SOMETIMES, ESPECIALLY IF WE ARE IN FLIGHTS MODE OR SOMETHING

		mHotelListFrag = (ResultsHotelListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelListAvailable, FTAG_HOTEL_LIST, manager,
			transaction, this, R.id.column_one_hotel_list, false);
		mHotelFiltersFrag = (ResultsHotelsFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelFiltersAvailable,
			FTAG_HOTEL_FILTERS, manager, transaction, this, R.id.column_two_hotel_filters, false);
		mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelFilteredCountAvailable, FTAG_HOTEL_FILTERED_COUNT, manager, transaction, this,
			R.id.column_three_hotel_filtered_count, false);
		mMapFragment = (HotelMapFragment) FragmentAvailabilityUtils.setFragmentAvailability(hotelMapAvailable,
			FTAG_HOTEL_MAP, manager, transaction, this, R.id.bg_hotel_map, false);
		mHotelDetailsFrag = (ResultsHotelDetailsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelRoomsAndRatesAvailable,
			FTAG_HOTEL_ROOMS_AND_RATES, manager, transaction, this, R.id.hotel_rooms_and_rates, false);
		mHotelReviewsFrag = (ResultsHotelReviewsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelReviewsAvailable,
			FTAG_HOTEL_REVIEWS, manager, transaction, this, R.id.hotel_reviews, false);
		mHotelSearchDownloadFrag = (HotelSearchDownloadFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			hotelSearchDownloadAvailable,
			FTAG_HOTEL_SEARCH_DOWNLOAD, manager, transaction, this, 0, false);
		mLoadingGuiFrag = (ResultsListLoadingFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			loadingGuiAvailable,
			FTAG_HOTEL_LOADING_INDICATOR, manager, transaction, this, R.id.loading_container, false);
		mSearchErrorFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(searchErrorAvailable, FTAG_HOTEL_SEARCH_ERROR, manager, transaction, this,
				R.id.column_one_hotel_search_error, false);
		transaction.commit();
	}

	private void setListState(ResultsHotelsState state) {
		if (mHotelListFrag != null) {

			mHotelListFrag.setTopRightTextButtonText(getString(R.string.Sort_and_Filter));

			//Button and locking
			switch (state) {
			case LOADING:
			case SEARCH_ERROR:
			case HOTEL_LIST_DOWN:
				mHotelListFrag.updateAdapter();
				mHotelListFrag.setPercentage(1f, 0);
				mHotelListFrag.setListLockedToTop(false);
				mHotelListFrag.setTopRightTextButtonEnabled(true);
				break;
			case HOTEL_LIST_UP:
				mHotelListFrag.setPercentage(0f, 0);
				mHotelListFrag.setListLockedToTop(false);
				mHotelListFrag.setTopRightTextButtonEnabled(true);
				break;
			case ROOMS_AND_RATES: {
				mHotelListFrag.setListLockedToTop(true);
				mHotelListFrag.setTopRightTextButtonEnabled(true);
				break;
			}
			case ROOMS_AND_RATES_FILTERS:
			case HOTEL_LIST_AND_FILTERS: {
				mHotelListFrag.setListLockedToTop(true);
				mHotelListFrag.setTopRightTextButtonEnabled(false);
				break;
			}
			case ADDING_HOTEL_TO_TRIP: {
				mHotelListFrag.setListLockedToTop(true);
				mHotelListFrag.setTopRightTextButtonEnabled(false);
				break;
			}
			}

			//List scroll position
			mHotelListFrag.setListenerEnabled(mListStateHelper, false);
			if (state == ResultsHotelsState.HOTEL_LIST_DOWN || state == ResultsHotelsState.LOADING
				|| state == ResultsHotelsState.SEARCH_ERROR) {
				mHotelListFrag.setPercentage(1f, 0);
			}
			else if (mHotelListFrag.hasList()
				&& mHotelListFrag.getPercentage() > 0) {
				mHotelListFrag.setPercentage(0f, 0);
			}
			mHotelListFrag.setListenerEnabled(mListStateHelper, true);
		}
	}

	/*
	 * FRAGMENT STUFF
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
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
		else if (tag == FTAG_HOTEL_ROOMS_AND_RATES) {
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
		else if (tag == FTAG_HOTEL_ROOMS_AND_RATES) {
			frag = ResultsHotelDetailsFragment.newInstance();
		}
		else if (tag == FTAG_HOTEL_SEARCH_DOWNLOAD) {
			frag = HotelSearchDownloadFragment.newInstance(Db.getHotelSearch().getSearchParams());
		}
		else if (tag == FTAG_HOTEL_LOADING_INDICATOR) {
			frag = ResultsListLoadingFragment.newInstance(getString(R.string.loading_hotels), Gravity.CENTER,
				Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		}
		else if (tag == FTAG_HOTEL_SEARCH_ERROR) {
			frag = ResultsListSearchErrorFragment.newInstance(getString(R.string.search_error));
		}
		else if (tag == FTAG_HOTEL_REVIEWS) {
			frag = ResultsHotelReviewsFragment.newInstance();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_HOTEL_LIST) {
			ResultsHotelListFragment listFrag = (ResultsHotelListFragment) frag;
			listFrag.registerStateListener(mListStateHelper, false);
		}
		else if (tag == FTAG_HOTEL_MAP) {
			HotelMapFragment mapFrag = (HotelMapFragment) frag;
			updateMapFragmentPositioningInfo(mapFrag);
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
		}
		else if (state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
		}
		else if (state == ResultsHotelsState.ROOMS_AND_RATES) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES_FILTERS, true);
		}
		else if (state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
		}
	}

	/*
	 * IResultsHotelSelectedListener Functions
	 */

	@Override
	public void onHotelSelected() {
		if (mMapFragment != null && mMapFragment.isAdded()) {
			mMapFragment.onHotelSelected();
		}
		if (mHotelDetailsFrag != null && mHotelDetailsFrag.isAdded()) {
			mHotelDetailsFrag.onHotelSelected();
		}
		for (IResultsHotelSelectedListener listener : mHotelSelectedListeners) {
			listener.onHotelSelected();
		}

		if (mHotelsStateManager.getState() == ResultsHotelsState.HOTEL_LIST_DOWN) {
			mHotelsStateManager
				.animateThroughStates(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.ROOMS_AND_RATES);
		}
		else {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
		}
	}

	/*
	 * IResultsHotelSelectedListener Functions
	 */

	@Override
	public void onHotelReviewsClicked() {
		setHotelsState(ResultsHotelsState.REVIEWS, true);
	}

	/*
	 * HotelMapFragmentListener
	 */

	@Override
	public void onMapClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPropertyClicked(Property property) {
		Db.getHotelSearch().setSelectedProperty(property);
		if (mHotelListFrag != null && mHotelListFrag.isAdded()) {
			mHotelListFrag.onHotelSelected();
		}
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
	}

	@Override
	public void onHotelMapFragmentAttached(HotelMapFragment fragment) {
		fragment.setInitialCameraPosition(CameraUpdateFactory.newLatLngBounds(HotelMapFragment.getAmericaBounds(), 0));
		mMapFragment = fragment;
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
				setHotelsState(getHotelsStateFromListState(state), false);
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
			else if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				mHotelListC.setBlockNewEventsEnabled(true);
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
			else if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				mHotelListC.setBlockNewEventsEnabled(false);
			}
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			if (state == ResultsState.FLIGHTS) {
				mRootC.setVisibility(View.GONE);
				mRootC.setAlpha(0f);
			}
			else {
				mRootC.setVisibility(View.VISIBLE);
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
					//The activity is still telling us something, so we better refresh our state.
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
			mGrid.setDimensions(totalWidth, totalHeight);

			//3 rows (AB,top half, bottom half)
			//5 columns - left, spacer,center,spacer,right
			mGrid.setGridSize(3, 5);

			//The top row matches the height of the actionbar
			mGrid.setRowSize(0, getActivity().getActionBar().getHeight());
			mGrid.setRowPercentage(2, 0.5f);

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
			mGrid.setContainerToColumnSpan(mHotelRoomsAndRatesC, 2, 4);
			mGrid.setContainerToColumnSpan(mHotelReviewsC, 2, 4);

			//All of the views except for the map sit below the action bar
			mGrid.setContainerToRow(mLoadingC, 2);
			mGrid.setContainerToRow(mSearchErrorC, 2);
			mGrid.setContainerToRowSpan(mHotelListC, 1, 2);
			mGrid.setContainerToRowSpan(mHotelFiltersC, 1, 2);
			mGrid.setContainerToRowSpan(mHotelFilteredCountC, 1, 2);
			mGrid.setContainerToRowSpan(mBgHotelMapC, 0, 2);
			mGrid.setContainerToRowSpan(mBgHotelMapTouchDelegateC, 0, 2);
			mGrid.setContainerToRowSpan(mHotelRoomsAndRatesC, 1, 2);
			mGrid.setContainerToRowSpan(mHotelReviewsC, 1, 2);

			//tell the map where its bounds are
			updateMapFragmentPositioningInfo(mMapFragment);
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
					//If we are in the middle of state transition, just reverse it
					setHotelsState(state, true);
					return true;
				}
				else {
					if (state == ResultsHotelsState.HOTEL_LIST_UP) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_DOWN, true);
						return true;
					}
					else if (state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						return true;
					}
					else if (state == ResultsHotelsState.ROOMS_AND_RATES) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						return true;
					}
					else if (state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
						setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
						return true;
					}
					else if (state == ResultsHotelsState.REVIEWS) {
						setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
						return true;
					}
					else if (state == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
						//We return true here, because we want to block back presses in this state
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
	private StateListenerCollection<ResultsHotelsState> mHotelsStateListeners = new StateListenerCollection<ResultsHotelsState>(
		mHotelsStateManager.getState());

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
				//SHOWING ROOMS AND RATES
				mHotelListFrag.setListLockedToTop(true);
				setRoomsAndRatesAnimationVisibilities();
				setRoomsAndRatesAnimationHardwareRendering(true);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if (stateOne == ResultsHotelsState.ADDING_HOTEL_TO_TRIP
				&& stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				mHotelListFrag.setListLockedToTop(false);

			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				mHotelListFrag.setListLockedToTop(true);
				setAddToTripAnimationVis(true);
				setAddToTripAnimationHardwareRendering(true);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsState stateOne, ResultsHotelsState stateTwo,
			float percentage) {
			if (stateOne == ResultsHotelsState.HOTEL_LIST_DOWN && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				//ENTERING HOTELS
				mBgHotelMapC.setAlpha(percentage);
				mHotelListFrag.setPercentage(1f - percentage, 0);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				//LEAVING HOTELS
				mBgHotelMapC.setAlpha(1f - percentage);
				mHotelListFrag.setPercentage(percentage, 0);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				//SHOWING ROOMS AND RATES
				setRoomsAndRatesShownPercentage(percentage);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				//HIDING ROOMS AND RATES
				setRoomsAndRatesShownPercentage(1f - percentage);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				setHotelsFiltersShownPercentage(percentage);

			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				setHotelsFiltersShownPercentage(1f - percentage);

			}
			else if (stateOne == ResultsHotelsState.ADDING_HOTEL_TO_TRIP
				&& stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				setRoomsAndRatesShownPercentage(1f - percentage);
				mBgHotelMapC.setAlpha(1f - percentage);
				mHotelListFrag.setPercentage(percentage, 0);
			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				setAddToTripPercentage(percentage);
			}

		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			if ((stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES)
				|| (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP)) {
				//SHOWING/HIDING ROOMS AND RATES
				setRoomsAndRatesAnimationHardwareRendering(false);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				setAddToTripAnimationVis(false);
				setAddToTripAnimationHardwareRendering(false);
			}

			setTouchable(true, true);

		}

		@Override
		public void onStateFinalized(ResultsHotelsState state) {

			setVisibilityState(state);
			setTouchState(state);
			setHotelsStateZIndex(state);
			setFragmentState(state);
			setListState(state);

			switch (state) {
			case LOADING:
			case HOTEL_LIST_DOWN:
				mBgHotelMapC.setAlpha(0f);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(0f);

				break;
			case HOTEL_LIST_UP:
				mBgHotelMapC.setAlpha(1f);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(0f);
				break;
			case ROOMS_AND_RATES: {
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(1f);
				break;
			}
			case ROOMS_AND_RATES_FILTERS:
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
			}
			if (mMapFragment != null && state != ResultsHotelsState.ROOMS_AND_RATES && state != ResultsHotelsState.REVIEWS) {
				mMapFragment.setMapPaddingFromFilterState(state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS);
			}

			//Ensure we are downloading the correct data.
			if (state == ResultsHotelsState.LOADING && mHotelSearchDownloadFrag != null) {
				importSearchParams();
				mHotelSearchDownloadFrag.startOrResumeForParams(Db.getHotelSearch().getSearchParams());
			}
		}

		/*
		 * SHOW FILTERS ANIMATION STUFF
		 */
		private void setHotelsFiltersAnimationVisibilities(boolean start) {
			mHotelFiltersC.setVisibility(View.VISIBLE);
			mHotelFilteredCountC.setVisibility(View.VISIBLE);
		}

		private void setHotelsFiltersAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelFiltersC.setLayerType(layerValue, null);
			mHotelFilteredCountC.setLayerType(layerValue, null);
		}

		private void setHotelsFiltersShownPercentage(float percentage) {
			float filtersLeft = -(1f - percentage) * mGrid.getColLeft(2);
			mHotelFiltersC.setTranslationX(filtersLeft);

			float filteredCountLeft = mGrid.getColWidth(4) * (1f - percentage);
			mHotelFilteredCountC.setTranslationX(filteredCountLeft);
		}

		/*
		 * SHOW ROOMS AND RATES ANIMATION STUFF
		 */

		private void setRoomsAndRatesAnimationVisibilities() {
			mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
		}

		private void setRoomsAndRatesAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelRoomsAndRatesC.setLayerType(layerValue, null);
		}

		private void setRoomsAndRatesShownPercentage(float percentage) {
			mHotelRoomsAndRatesC.setTranslationY(-(1f - percentage) * mGrid.getTotalHeight());
			mBgHotelMapDimmerDrawable.setPercentage(percentage);
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
		 * HOTEL STATE HELPERS
		 */
		private void setHotelsStateZIndex(ResultsHotelsState state) {
			//Calling bringToFront() does a full layout pass, so we DONT want to do this when it is un-needed.

			if (state == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				if (!mRoomsAndRatesInFront) {
					mHotelRoomsAndRatesC.bringToFront();
					mRoomsAndRatesInFront = true;
				}
			}
			else if (mRoomsAndRatesInFront) {
				mHotelFiltersC.bringToFront();
				mHotelFilteredCountC.bringToFront();
				mHotelListC.bringToFront();
				mRoomsAndRatesInFront = false;
			}
		}

		/*
		 * Touch
		 */
		private void setTouchable(boolean touchable, boolean includingList) {
			mBgHotelMapC.setTouchPassThroughEnabled(touchable);
			mHotelFiltersC.setBlockNewEventsEnabled(touchable);
			mHotelFilteredCountC.setBlockNewEventsEnabled(touchable);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(touchable);
			if (includingList) {
				mHotelListC.setBlockNewEventsEnabled(touchable);
			}
		}
	};

	/*
	EXPEDIA SERVICES FRAG LISTENER
	 */

	@Override
	public void onExpediaServicesDownload(ExpediaServicesFragment.ServiceType type, Response response) {
		if (type == ExpediaServicesFragment.ServiceType.HOTEL_SEARCH) {
			Context context = getActivity();

			Db.getHotelSearch().setSearchResponse((HotelSearchResponse) response);
			Db.saveHotelSearchTimestamp(context);
			Db.kickOffBackgroundHotelSearchSave(context);

			if (response != null && !response.hasErrors()) {
				setHotelsState(ResultsHotelsState.HOTEL_LIST_DOWN, true);
			}
			else {
				setHotelsState(ResultsHotelsState.SEARCH_ERROR, false);
			}
		}
	}

	/**
	 * IAddToBucketListener
	 */
	@Override
	public void onItemAddedToBucket() {
		//TODO: EVENTUALLY WE WANT TO ANIMATE THIS THING!
		setHotelsState(ResultsHotelsState.ADDING_HOTEL_TO_TRIP, false);
	}
}
