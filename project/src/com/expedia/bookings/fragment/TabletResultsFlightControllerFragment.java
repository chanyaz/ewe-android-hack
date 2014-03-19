package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.ResultsFlightsListState;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.ResultsFlightListFragment.IDoneClickedListener;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsFlightLegSelected;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.FruitList;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsFlightControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to FLIGHTS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsFlightControllerFragment extends Fragment implements IResultsFlightSelectedListener,
	IResultsFlightLegSelected, IFragmentAvailabilityProvider, IBackManageable,
	IStateProvider<ResultsFlightsState>, IDoneClickedListener, ExpediaServicesFragment.ExpediaServicesFragmentListener,
	ResultsFlightHistogramFragment.HistogramFragmentListener {

	//State
	private static final String STATE_FLIGHTS_STATE = "STATE_FLIGHTS_STATE";

	//Frag tags
	private static final String FTAG_FLIGHT_MAP = "FTAG_FLIGHT_MAP";
	private static final String FTAG_FLIGHT_ADD_TO_TRIP = "FTAG_FLIGHT_ADD_TO_TRIP";
	private static final String FTAG_FLIGHT_HISTOGRAM = "FTAG_FLIGHT_HISTOGRAM";
	private static final String FTAG_FLIGHT_ONE_FILTERS = "FTAG_FLIGHT_ONE_FILTERS";
	private static final String FTAG_FLIGHT_ONE_LIST = "FTAG_FLIGHT_ONE_LIST";
	private static final String FTAG_FLIGHT_TWO_FILTERS = "FTAG_FLIGHT_TWO_FILTERS";
	private static final String FTAG_FLIGHT_TWO_LIST = "FTAG_FLIGHT_TWO_LIST";
	private static final String FTAG_FLIGHT_ONE_DETAILS = "FTAG_FLIGHT_ONE_DETAILS";
	private static final String FTAG_FLIGHT_TWO_DETAILS = "FTAG_FLIGHT_TWO_DETAILS";
	private static final String FTAG_FLIGHT_SEARCH_DOWNLOAD = "FTAG_FLIGHT_SEARCH_DOWNLOAD";
	private static final String FTAG_FLIGHT_LOADING_INDICATOR = "FTAG_FLIGHT_LOADING_INDICATOR";

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mFlightMapC;
	private FrameLayoutTouchController mAddToTripC;

	private FrameLayoutTouchController mFlightHistogramC;

	private FrameLayoutTouchController mFlightOneListC;
	private FrameLayoutTouchController mFlightOneFiltersC;
	private FrameLayoutTouchController mFlightOneDetailsC;

	private RelativeLayout mFlightTwoListColumnC;
	private FrameLayoutTouchController mFlightTwoFlightOneHeaderC;
	private FrameLayoutTouchController mFlightTwoListC;
	private FrameLayoutTouchController mFlightTwoFiltersC;
	private FrameLayoutTouchController mFlightTwoDetailsC;
	private FrameLayoutTouchController mLoadingC;

	private ArrayList<ViewGroup> mContainers = new ArrayList<ViewGroup>();

	//Views
	private FlightLegSummarySectionTablet mFlightOneSelectedRow;

	//Fragments
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsFlightAddToTrip mAddToTripFrag;
	private ResultsFlightHistogramFragment mFlightHistogramFrag;
	private ResultsFlightListFragment mFlightOneListFrag;
	private ResultsFlightFiltersFragment mFlightOneFilterFrag;
	private ResultsFlightDetailsFragment mFlightOneDetailsFrag;
	private ResultsFlightListFragment mFlightTwoListFrag;
	private ResultsFlightFiltersFragment mFlightTwoFilterFrag;
	private ResultsFlightDetailsFragment mFlightTwoDetailsFrag;
	private FlightSearchDownloadFragment mFlightSearchDownloadFrag;
	private ResultsListLoadingFragment mLoadingGuiFrag;

	//Other
	private GridManager mGrid = new GridManager();
	private float mFlightDetailsMarginPercentage = 0.1f;
	private boolean mOneWayFlight = true;

	private StateManager<ResultsFlightsState> mFlightsStateManager = new StateManager<ResultsFlightsState>(
		ResultsFlightsState.LOADING, this);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			mOneWayFlight = !Db.getFlightSearch().getSearchParams().isRoundTrip();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mFlightsStateManager.setDefaultState(ResultsFlightsState.valueOf(savedInstanceState.getString(
				STATE_FLIGHTS_STATE,
				getBaseState().name())));
		}

		if (Db.getFlightSearch() == null || Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(getActivity())) {
				mFlightsStateManager.setDefaultState(ResultsFlightsState.LOADING);
			}
			else {
				Db.loadFlightSearchParamsFromDisk(getActivity());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_flights, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mFlightMapC = Ui.findView(view, R.id.bg_flight_map);
		mAddToTripC = Ui.findView(view, R.id.flights_add_to_trip);

		mFlightHistogramC = Ui.findView(view, R.id.flight_histogram_container);

		mFlightOneFiltersC = Ui.findView(view, R.id.flight_one_filters);
		mFlightOneListC = Ui.findView(view, R.id.flight_one_list);
		mFlightOneDetailsC = Ui.findView(view, R.id.flight_one_details);
		mFlightTwoListColumnC = Ui.findView(view, R.id.flight_two_list_and_header_container);
		mFlightTwoFlightOneHeaderC = Ui.findView(view, R.id.flight_two_header_with_flight_one_info);
		mFlightTwoListC = Ui.findView(view, R.id.flight_two_list);
		mFlightTwoFiltersC = Ui.findView(view, R.id.flight_two_filters);
		mFlightTwoDetailsC = Ui.findView(view, R.id.flight_two_details);
		mLoadingC = Ui.findView(view, R.id.loading_container);

		mContainers.add(mFlightMapC);
		mContainers.add(mAddToTripC);
		mContainers.add(mFlightOneFiltersC);
		mContainers.add(mFlightOneListC);
		mContainers.add(mFlightOneDetailsC);
		mContainers.add(mFlightTwoListColumnC);
		mContainers.add(mFlightTwoFlightOneHeaderC);
		mContainers.add(mFlightTwoListC);
		mContainers.add(mFlightTwoFiltersC);
		mContainers.add(mFlightTwoDetailsC);
		mContainers.add(mLoadingC);

		mFlightOneSelectedRow = Ui.findView(view, R.id.flight_one_row);

		registerStateListener(new StateListenerLogger<ResultsFlightsState>(), false);
		registerStateListener(mFlightsStateHelper, false);

		mFlightMapC.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_FLIGHTS_STATE, mFlightsStateManager.getState().name());
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

	private Rect getAddTripRect() {
		return mAddToTripFrag.getRowRect();
	}

	// Base state is semi-complicated at this point. For now, it'll work like this:
	// 1. If we have actual FlightSearch data, let's show that.
	// 2. If we don't have FlightSearch data but do have flight histogram data, let's show that
	// 3. Otherwise, let's just show the loading state
	private ResultsFlightsState getBaseState() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null) {
			return ResultsFlightsState.FLIGHT_LIST_DOWN;
		}
		else if (Db.getFlightSearchHistogramResponse() != null) {
			return ResultsFlightsState.FLIGHT_HISTOGRAM;
		}
		else {
			return ResultsFlightsState.LOADING;
		}
	}

	/*
	 * NEW SEARCH PARAMS
	 */

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		if (mFlightsStateManager.getState() != ResultsFlightsState.LOADING) {
			setFlightsState(ResultsFlightsState.LOADING, false);
		}
		else {
			importSearchParams();
			mFlightSearchDownloadFrag.startOrResumeForParams(Db.getFlightSearch().getSearchParams());
		}
	}

	public void importSearchParams() {
		Db.getFlightSearch().setSearchResponse(null);
		Db.setFlightSearchHistogramResponse(null);
		Db.getFlightSearch().setSearchParams(Sp.getParams().toFlightSearchParams());
	}

	/*
	 * FRAGMENT HELPERS
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHT_MAP) {
			frag = this.mFlightMapFrag;
		}
		else if (tag == FTAG_FLIGHT_ADD_TO_TRIP) {
			frag = this.mAddToTripFrag;
		}
		else if (tag == FTAG_FLIGHT_HISTOGRAM) {
			frag = mFlightHistogramFrag;
		}
		else if (tag == FTAG_FLIGHT_ONE_FILTERS) {
			frag = this.mFlightOneFilterFrag;
		}
		else if (tag == FTAG_FLIGHT_ONE_LIST) {
			frag = this.mFlightOneListFrag;
		}
		else if (tag == FTAG_FLIGHT_TWO_FILTERS) {
			frag = this.mFlightTwoFilterFrag;
		}
		else if (tag == FTAG_FLIGHT_TWO_LIST) {
			frag = this.mFlightTwoListFrag;
		}
		else if (tag == FTAG_FLIGHT_ONE_DETAILS) {
			frag = this.mFlightOneDetailsFrag;
		}
		else if (tag == FTAG_FLIGHT_TWO_DETAILS) {
			frag = this.mFlightTwoDetailsFrag;
		}
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = mFlightSearchDownloadFrag;
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = mLoadingGuiFrag;
		}

		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHT_MAP) {
			frag = ResultsFlightMapFragment.newInstance();
		}
		else if (tag == FTAG_FLIGHT_ADD_TO_TRIP) {
			frag = ResultsFlightAddToTrip.newInstance();
		}
		else if (tag == FTAG_FLIGHT_HISTOGRAM) {
			frag = new ResultsFlightHistogramFragment();
		}
		else if (tag == FTAG_FLIGHT_ONE_FILTERS) {
			frag = ResultsFlightFiltersFragment.newInstance(0);
		}
		else if (tag == FTAG_FLIGHT_ONE_LIST) {
			frag = ResultsFlightListFragment.getInstance(0);
		}
		else if (tag == FTAG_FLIGHT_TWO_FILTERS) {
			frag = ResultsFlightFiltersFragment.newInstance(1);
		}
		else if (tag == FTAG_FLIGHT_TWO_LIST) {
			frag = ResultsFlightListFragment.getInstance(1);
		}
		else if (tag == FTAG_FLIGHT_ONE_DETAILS) {
			frag = ResultsFlightDetailsFragment.newInstance(0);
		}
		else if (tag == FTAG_FLIGHT_TWO_DETAILS) {
			frag = ResultsFlightDetailsFragment.newInstance(1);
		}
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = FlightSearchDownloadFragment.newInstance(Sp.getParams().toFlightSearchParams());
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = ResultsListLoadingFragment.newInstance(getString(R.string.loading_flights));
		}

		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {

		if (tag == FTAG_FLIGHT_MAP) {
			updateMapFragSizes((ResultsFlightMapFragment) frag);
		}
		else if (tag == FTAG_FLIGHT_ONE_LIST) {
			ResultsFlightListFragment listFrag = (ResultsFlightListFragment) frag;
			listFrag.registerStateListener(mListStateHelper, false);
			listFrag.setTopRightTextButtonText(getString(R.string.Done));
		}
		else if (tag == FTAG_FLIGHT_TWO_LIST) {
			((ResultsListFragment) frag).setPercentage(0f, 0);
			((ResultsListFragment) frag).setListLockedToTop(true);
		}
		else if (tag == FTAG_FLIGHT_ONE_DETAILS) {
			updateDetailsFragSizes((ResultsFlightDetailsFragment) frag);
		}
		else if (tag == FTAG_FLIGHT_TWO_DETAILS) {
			updateDetailsFragSizes((ResultsFlightDetailsFragment) frag);
		}
		else if (tag == FTAG_FLIGHT_ONE_FILTERS) {
			((ResultsFlightFiltersFragment) frag).bindAll();
		}
		else if (tag == FTAG_FLIGHT_TWO_FILTERS) {
			((ResultsFlightFiltersFragment) frag).bindAll();
		}
		else if (tag == FTAG_FLIGHT_HISTOGRAM) {
			ResultsFlightHistogramFragment histFrag = (ResultsFlightHistogramFragment) frag;
			histFrag.setHistogramData(Db.getFlightSearchHistogramResponse());
			histFrag.setColWidth(mGrid.getColWidth(2));

			if (mFlightSearchDownloadFrag != null) {
				histFrag.setShowProgressBar(mFlightSearchDownloadFrag.isDownloadingFlightSearch());
			}
		}
	}

	private void updateDetailsFragSizes(ResultsFlightDetailsFragment frag) {
		if (frag != null && mGrid.getTotalWidth() > 0) {
			int actionbarHeight = getActivity().getActionBar().getHeight();
			int leftCol = 2;
			int rightCol = 4;

			Rect position = new Rect();
			position.left = mGrid.getColLeft(leftCol);
			position.right = mGrid.getColRight(rightCol);
			position.top = 0;
			position.bottom = mGrid.getTotalHeight() - actionbarHeight;
			frag.setDefaultDetailsPositionAndDimensions(position, mFlightDetailsMarginPercentage);
		}

		if (frag != null && mFlightOneListFrag != null && mFlightOneListFrag.hasList()) {
			FruitList list = (FruitList) mFlightOneListFrag.getListView();
			frag.setDetaultRowDimensions(mGrid.getColWidth(1), list.getRowHeight(false));
		}
	}

	private void updateMapFragSizes(ResultsFlightMapFragment frag) {
		if (frag != null && mGrid.isLandscape()) {
			int padding = getResources().getDimensionPixelSize(R.dimen.tablet_results_flight_map_padding);
			frag.setPadding(mGrid.getColLeft(2) + padding, padding, padding, padding);
		}
	}

	/*
	 * IResultsFlightSelectedListener
	 */

	@Override
	public void onFlightSelected(int legNumber) {
		if (legNumber == 0) {
			if (mFlightsStateManager.getState() == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightsStateManager.animateThroughStates(ResultsFlightsState.FLIGHT_ONE_FILTERS,
					ResultsFlightsState.FLIGHT_ONE_DETAILS);
			}
			else {
				setFlightsState(ResultsFlightsState.FLIGHT_ONE_DETAILS, true);
			}

			// Make sure to reset the query, as the flights present in the second leg depend upon the flight
			// selected from the first leg. Frag is null for one-way flights.
			if (mFlightTwoListFrag != null) {
				mFlightTwoListFrag.resetQuery();
			}
			if (mFlightTwoFilterFrag != null) {
				mFlightTwoFilterFrag.onFilterChanged();
			}

			if (mFlightOneDetailsFrag != null && mFlightOneDetailsFrag.isAdded()) {
				mFlightOneDetailsFrag.bindWithDb();
			}
		}
		else if (legNumber == 1) {
			setFlightsState(ResultsFlightsState.FLIGHT_TWO_DETAILS,
				mFlightsStateManager.getState() != ResultsFlightsState.FLIGHT_TWO_DETAILS);

			if (mFlightTwoDetailsFrag != null && mFlightTwoDetailsFrag.isAdded()) {
				mFlightTwoDetailsFrag.bindWithDb();
			}
		}
	}

	/*
	 * IResultsFlightLegSelected
	 */

	@Override
	public void onTripAdded(int legNumber) {
		if (mFlightsStateManager.getState() != ResultsFlightsState.FLIGHT_LIST_DOWN) {
			boolean lastLegToSelect = mOneWayFlight || legNumber == 1;
			if (lastLegToSelect) {
				Db.getTripBucket().clearFlight();
				Db.getTripBucket().add(Db.getFlightSearch().getSearchState());
				Db.saveTripBucket(getActivity());
				setFlightsState(ResultsFlightsState.ADDING_FLIGHT_TO_TRIP, true);
			}
			else {
				setFlightsState(ResultsFlightsState.FLIGHT_TWO_FILTERS, true);
			}
		}
	}

	/*
	 * STATE HELPER METHODS
	 */

	public void setFlightsState(ResultsFlightsState state, boolean animate) {
		mFlightsStateManager.setState(state, animate);
	}

	private void setTouchState(ResultsFlightsState flightsState) {
		ArrayList<ViewGroup> touchableViews = new ArrayList<ViewGroup>();
		switch (flightsState) {
		case FLIGHT_HISTOGRAM: {
			touchableViews.add(mFlightHistogramC);
			break;
		}
		case FLIGHT_LIST_DOWN: {
			touchableViews.add(mFlightOneListC);
			break;
		}
		case FLIGHT_ONE_FILTERS: {
			touchableViews.add(mFlightOneFiltersC);
			touchableViews.add(mFlightOneListC);
			break;
		}
		case FLIGHT_ONE_DETAILS: {
			touchableViews.add(mFlightOneDetailsC);
			touchableViews.add(mFlightOneListC);
			break;
		}
		case FLIGHT_TWO_FILTERS: {
			touchableViews.add(mFlightTwoFiltersC);
			touchableViews.add(mFlightTwoListC);
			break;
		}
		case FLIGHT_TWO_DETAILS: {
			touchableViews.add(mFlightTwoDetailsC);
			touchableViews.add(mFlightTwoListC);
			break;
		}
		case ADDING_FLIGHT_TO_TRIP: {
			break;
		}
		}

		for (ViewGroup vg : mContainers) {
			if (vg instanceof FrameLayoutTouchController) {
				if (touchableViews.contains(vg)) {
					((FrameLayoutTouchController) vg).setBlockNewEventsEnabled(false);
				}
				else {
					((FrameLayoutTouchController) vg).setBlockNewEventsEnabled(true);
				}
			}
		}
	}

	private void setVisibilityState(ResultsFlightsState flightsState) {
		ArrayList<ViewGroup> visibleViews = new ArrayList<ViewGroup>();

		switch (flightsState) {
		case LOADING:
			visibleViews.add(mLoadingC);
		case FLIGHT_HISTOGRAM:
		case FLIGHT_LIST_DOWN: {
			visibleViews.add(mFlightHistogramC);
			visibleViews.add(mFlightOneListC);
			break;
		}
		case FLIGHT_ONE_FILTERS: {
			visibleViews.add(mFlightOneFiltersC);
			visibleViews.add(mFlightOneListC);
			visibleViews.add(mFlightMapC);
			break;
		}
		case FLIGHT_ONE_DETAILS: {
			visibleViews.add(mFlightOneDetailsC);
			visibleViews.add(mFlightOneListC);
			visibleViews.add(mFlightMapC);
			break;
		}
		case FLIGHT_TWO_FILTERS: {
			visibleViews.add(mFlightTwoFiltersC);
			visibleViews.add(mFlightTwoListColumnC);
			visibleViews.add(mFlightTwoFlightOneHeaderC);
			visibleViews.add(mFlightTwoListC);
			visibleViews.add(mFlightMapC);
			mFlightTwoFiltersC.setAlpha(1f);
			mFlightTwoListColumnC.setAlpha(1f);
			break;
		}
		case FLIGHT_TWO_DETAILS: {
			visibleViews.add(mFlightTwoDetailsC);
			visibleViews.add(mFlightTwoListC);
			visibleViews.add(mFlightTwoListColumnC);
			visibleViews.add(mFlightTwoFlightOneHeaderC);
			visibleViews.add(mFlightMapC);
			mFlightTwoListColumnC.setAlpha(1f);
			break;
		}
		case ADDING_FLIGHT_TO_TRIP: {
			visibleViews.add(mAddToTripC);
			visibleViews.add(mFlightMapC);
			break;
		}
		}

		for (ViewGroup vg : mContainers) {
			if (visibleViews.contains(vg)) {
				vg.setVisibility(View.VISIBLE);
			}
			else {
				vg.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void setFragmentState(ResultsFlightsState flightsState) {

		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction

		FragmentTransaction transaction = manager.beginTransaction();

		boolean loadingAvailable = false;
		boolean flightSearchDownloadAvailable = false;
		boolean flightHistogramAvailable = false;
		boolean flightOneListAvailable = true;
		boolean flightMapAvailable = true;
		boolean flightAddToTripAvailable = true;
		boolean flightOneFiltersAvailable = true;
		boolean flightTwoListAvailable = true;
		boolean flightTwoFiltersAvailabe = true;
		boolean flightOneDetailsAvailable = true;
		boolean flightTwoDetailsAvailable = true;

		if (flightsState == ResultsFlightsState.LOADING) {
			// This case kicks off the downloads
			flightSearchDownloadAvailable = true;

			loadingAvailable = true;

			flightMapAvailable = false;
			flightAddToTripAvailable = false;
			flightOneListAvailable = false;
			flightOneFiltersAvailable = false;
			flightTwoListAvailable = false;
			flightTwoFiltersAvailabe = false;
			flightOneDetailsAvailable = false;
			flightTwoDetailsAvailable = false;
		}
		else if (flightsState == ResultsFlightsState.FLIGHT_HISTOGRAM) {
			flightSearchDownloadAvailable = true;
			flightHistogramAvailable = true;

			flightMapAvailable = false;
			flightAddToTripAvailable = false;
			flightOneListAvailable = true;
			flightOneFiltersAvailable = false;
			flightTwoListAvailable = false;
			flightTwoFiltersAvailabe = false;
			flightOneDetailsAvailable = false;
			flightTwoDetailsAvailable = false;
		}
		else if (flightsState == ResultsFlightsState.FLIGHT_LIST_DOWN) {
			flightOneListAvailable = true;

			flightHistogramAvailable = true;
			flightTwoListAvailable = false;
			flightTwoFiltersAvailabe = false;
			flightTwoDetailsAvailable = false;
		}

		if (mOneWayFlight) {
			flightTwoListAvailable = false;
			flightTwoFiltersAvailabe = false;
			flightTwoDetailsAvailable = false;
		}

		mFlightMapFrag = (ResultsFlightMapFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightMapAvailable, FTAG_FLIGHT_MAP,
			manager, transaction, this, R.id.bg_flight_map, false);
		mAddToTripFrag = (ResultsFlightAddToTrip) FragmentAvailabilityUtils.setFragmentAvailability(
			flightAddToTripAvailable,
			FTAG_FLIGHT_ADD_TO_TRIP, manager, transaction, this, R.id.flights_add_to_trip, false);
		mFlightHistogramFrag = FragmentAvailabilityUtils.setFragmentAvailability(flightHistogramAvailable,
			FTAG_FLIGHT_HISTOGRAM, manager, transaction, this, R.id.flight_histogram_container, true);
		mFlightOneListFrag = (ResultsFlightListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightOneListAvailable,
			FTAG_FLIGHT_ONE_LIST, manager, transaction, this, R.id.flight_one_list, false);
		mFlightOneFilterFrag = (ResultsFlightFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightOneFiltersAvailable,
			FTAG_FLIGHT_ONE_FILTERS, manager, transaction, this, R.id.flight_one_filters, true);
		mFlightOneDetailsFrag = (ResultsFlightDetailsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightOneDetailsAvailable,
			FTAG_FLIGHT_ONE_DETAILS, manager, transaction, this, R.id.flight_one_details, true);
		mFlightTwoListFrag = (ResultsFlightListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightTwoListAvailable,
			FTAG_FLIGHT_TWO_LIST, manager, transaction, this, R.id.flight_two_list, false);
		mFlightTwoFilterFrag = (ResultsFlightFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightTwoFiltersAvailabe,
			FTAG_FLIGHT_TWO_FILTERS, manager, transaction, this, R.id.flight_two_filters, true);
		mFlightTwoDetailsFrag = (ResultsFlightDetailsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightTwoDetailsAvailable,
			FTAG_FLIGHT_TWO_DETAILS, manager, transaction, this, R.id.flight_two_details, true);
		mFlightSearchDownloadFrag = (FlightSearchDownloadFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightSearchDownloadAvailable,
			FTAG_FLIGHT_SEARCH_DOWNLOAD, manager, transaction, this, 0, true);
		mLoadingGuiFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			loadingAvailable,
			FTAG_FLIGHT_LOADING_INDICATOR, manager, transaction, this, R.id.loading_container, true);
		transaction.commit();

	}

	private void setFirstFlightListState(ResultsFlightsState state) {
		if (mFlightOneListFrag != null) {
			//lock
			mFlightOneListFrag.setListLockedToTop(
				state != ResultsFlightsState.LOADING && state != ResultsFlightsState.FLIGHT_LIST_DOWN
					&& state != ResultsFlightsState.FLIGHT_ONE_FILTERS
					&& state != ResultsFlightsState.FLIGHT_HISTOGRAM);

			if (state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightOneListFrag.resetQuery();
			}

			//List scroll position
			mFlightOneListFrag.unRegisterStateListener(mListStateHelper);
			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN
				|| state == ResultsFlightsState.FLIGHT_HISTOGRAM) {
				mFlightOneListFrag.setPercentage(1f, 0);
			}
			else if (mFlightOneListFrag.hasList()
				&& mFlightOneListFrag.getPercentage() > 0) {
				mFlightOneListFrag.setPercentage(0f, 0);
			}
			mFlightOneListFrag.registerStateListener(mListStateHelper, false);
		}
	}

	/*
	 * FLIGHT LIST FRAGMENT LISTENER
	 */
	@Override
	public void onDoneClicked() {
		if (mFlightsStateManager.getState() == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
			//Go through the states.
			mFlightsStateManager.animateThroughStates(ResultsFlightsState.FLIGHT_ONE_FILTERS,
				ResultsFlightsState.FLIGHT_LIST_DOWN);
		}
		else if (mFlightsStateManager.getState() == ResultsFlightsState.FLIGHT_ONE_FILTERS) {
			//Animate the list down
			setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
		}
		else {
			//We are done but we don't know how to get back, so we just go back without animation.
			setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, false);
		}
	}

	@Override
	public void onStickyHeaderClicked() {
		if (mFlightsStateManager.getState() == ResultsFlightsState.FLIGHT_LIST_DOWN) {
			setFlightsState(ResultsFlightsState.FLIGHT_HISTOGRAM, true);
		}
	}

	/*
	 * LIST STATE LISTENER
	 */

	private StateListenerHelper<ResultsFlightsListState> mListStateHelper = new StateListenerHelper<ResultsFlightsListState>() {

		@Override
		public void onStateTransitionStart(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo) {
			if (getFlightsListActionsEnabled()) {
				startStateTransition(getFlightsStateFromListState(stateOne), getFlightsStateFromListState(stateTwo));
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo,
			float percentage) {
			if (getFlightsListActionsEnabled()) {
				updateStateTransition(getFlightsStateFromListState(stateOne), getFlightsStateFromListState(stateTwo),
					percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo) {
			if (getFlightsListActionsEnabled()) {
				endStateTransition(getFlightsStateFromListState(stateOne), getFlightsStateFromListState(stateTwo));
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsListState state) {
			if (getFlightsListActionsEnabled()) {
				setFlightsState(getFlightsStateFromListState(state), false);
			}
		}

		private boolean getFlightsListActionsEnabled() {
			ResultsFlightsState state = mFlightsStateManager.getState();
			if (mFlightsStateManager.hasState() && (state == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| state == ResultsFlightsState.FLIGHT_LIST_DOWN)) {
				return true;
			}
			return false;
		}

		private ResultsFlightsState getFlightsStateFromListState(ResultsFlightsListState state) {
			if (state == ResultsFlightsListState.FLIGHTS_LIST_AT_TOP) {
				return ResultsFlightsState.FLIGHT_ONE_FILTERS;
			}
			else if (state == ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM) {
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
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				startStateTransition(ResultsFlightsState.FLIGHT_LIST_DOWN, ResultsFlightsState.FLIGHT_ONE_FILTERS);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				startStateTransition(ResultsFlightsState.FLIGHT_ONE_FILTERS, ResultsFlightsState.FLIGHT_LIST_DOWN);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				updateStateTransition(ResultsFlightsState.FLIGHT_LIST_DOWN, ResultsFlightsState.FLIGHT_ONE_FILTERS,
					percentage);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				updateStateTransition(ResultsFlightsState.FLIGHT_ONE_FILTERS, ResultsFlightsState.FLIGHT_LIST_DOWN,
					percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				endStateTransition(ResultsFlightsState.FLIGHT_LIST_DOWN, ResultsFlightsState.FLIGHT_ONE_FILTERS);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				endStateTransition(ResultsFlightsState.FLIGHT_ONE_FILTERS, ResultsFlightsState.FLIGHT_LIST_DOWN);
			}
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			if (state != ResultsState.FLIGHTS) {
				setFlightsState(getBaseState(), false);
			}
			else {
				//We are in flights mode
				if (mFlightsStateManager.hasState()
					&& mFlightsStateManager.getState() == ResultsFlightsState.FLIGHT_LIST_DOWN) {
					//If we have a state, and that state is DOWN, lets go up
					setFlightsState(ResultsFlightsState.FLIGHT_ONE_FILTERS, false);
				}
				else {
					//The activity is still telling us something, so we better refresh our state.
					setFlightsState(mFlightsStateManager.getState(), false);
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


			mGrid.setGridSize(3, 5);

			//The top row matches the height of the actionbar
			mGrid.setRowSize(0, getActivity().getActionBar().getHeight());

			//The bottom row
			mGrid.setRowPercentage(2, .50f);

			//These columns are just the spacers between content columns
			int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
			mGrid.setColumnSize(1, spacerSize);
			mGrid.setColumnSize(3, spacerSize);

			//Horizontal alignment
			mGrid.setContainerToColumnSpan(mFlightMapC, 0, 4);
			mGrid.setContainerToColumn(mFlightOneFiltersC, 0);
			mGrid.setContainerToColumn(mFlightOneListC, 2);
			mGrid.setContainerToColumn(mFlightHistogramC, 2);
			mGrid.setContainerToColumnSpan(mFlightOneDetailsC, 0, 4);
			mGrid.setContainerToColumn(mFlightTwoFiltersC, 0);
			mGrid.setContainerToColumn(mFlightTwoListColumnC, 2);
			mGrid.setContainerToColumnSpan(mFlightTwoDetailsC, 0, 4);
			mGrid.setContainerToColumn(mLoadingC, 2);

			//Vertical alignment

			//Most content sits in rows 1 and 2 (below the actionbar)
			mGrid.setContainerToRowSpan(mFlightOneFiltersC, 1, 2);
			mGrid.setContainerToRowSpan(mFlightOneListC, 1, 2);
			mGrid.setContainerToRowSpan(mFlightOneDetailsC, 1, 2);
			mGrid.setContainerToRowSpan(mFlightTwoFiltersC, 1, 2);
			mGrid.setContainerToRowSpan(mFlightTwoListColumnC, 1, 2);
			mGrid.setContainerToRowSpan(mFlightTwoDetailsC, 1, 2);

			//Special cases
			mGrid.setContainerToRowSpan(mFlightMapC, 0, 2);
			mGrid.setContainerToRow(mFlightHistogramC, 2);
			mGrid.setContainerToRow(mLoadingC, 2);

			//Frag stuff
			updateDetailsFragSizes(mFlightOneDetailsFrag);
			updateDetailsFragSizes(mFlightTwoDetailsFrag);
			updateMapFragSizes(mFlightMapFrag);

			//If we are already initialized, we should reset our state so things get positioned properly.
			if (mFlightOneListFrag != null) {
				setFlightsState(mFlightsStateManager.getState(), false);
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
			ResultsFlightsState state = mFlightsStateManager.getState();
			if (mFlightsStateManager.isAnimating()) {
				//If we are in the middle of state transition, just reverse it
				setFlightsState(state, true);
				return true;
			}
			else {

				// TODO add Histogram state in here?

				if (state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
					return false;
				}
				else if (state == ResultsFlightsState.FLIGHT_ONE_FILTERS) {
					setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
					return true;
				}
				else if (state == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					setFlightsState(ResultsFlightsState.FLIGHT_ONE_FILTERS, true);
					return true;
				}
				else if (state == ResultsFlightsState.FLIGHT_TWO_FILTERS) {
					setFlightsState(ResultsFlightsState.FLIGHT_ONE_DETAILS, true);
					return true;
				}
				else if (state == ResultsFlightsState.FLIGHT_TWO_DETAILS) {
					setFlightsState(ResultsFlightsState.FLIGHT_TWO_FILTERS, true);
					return true;
				}
				else if (state == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
					return true;
				}
			}

			return false;
		}

	};

	/*
	 * FLIGHTS STATE PROVIDER
	 */
	private StateListenerCollection<ResultsFlightsState> mFlightsStateListeners = new StateListenerCollection<ResultsFlightsState>(
		mFlightsStateManager.getState());

	@Override
	public void startStateTransition(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
		mFlightsStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
		float percentage) {
		mFlightsStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
		mFlightsStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsFlightsState state) {
		mFlightsStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsFlightsState> listener, boolean fireFinalizeState) {
		mFlightsStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsFlightsState> listener) {
		mFlightsStateListeners.unRegisterStateListener(listener);
	}

	/*
	 * FLIGHTS STATE LISTENER
	 */

	private StateListenerHelper<ResultsFlightsState> mFlightsStateHelper = new StateListenerHelper<ResultsFlightsState>() {

		//Vars for animating between filters and details.
		private ViewGroup mTransitionFiltersC;
		private ViewGroup mTransitionListC;
		private ViewGroup mTransitionDetailsC;
		private ResultsFlightDetailsFragment mTransitionDetailsFrag;

		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			int layerType = View.LAYER_TYPE_HARDWARE;
			if ((stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS)
				|| (stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN)) {

				mFlightMapC.setVisibility(View.VISIBLE);
				mFlightOneFiltersC.setVisibility(View.VISIBLE);
				mFlightOneListC.setVisibility(View.VISIBLE);
			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS)) {
				//WE ARE GOING FROM FLIGHT DETAILS TO THE NEXT LEG

				//Visibility
				mFlightOneListC.setVisibility(View.VISIBLE);
				mFlightOneDetailsC.setVisibility(View.VISIBLE);
				mFlightTwoFiltersC.setVisibility(View.VISIBLE);
				mFlightTwoListColumnC.setVisibility(View.VISIBLE);
				mFlightTwoListC.setVisibility(View.VISIBLE);
				mFlightTwoFlightOneHeaderC.setVisibility(View.INVISIBLE);

				//Rendering
				mFlightOneListC.setLayerType(layerType, null);
				mFlightTwoFiltersC.setLayerType(layerType, null);
				mFlightTwoListColumnC.setLayerType(layerType, null);
				if (mFlightOneDetailsFrag != null) {
					mFlightOneDetailsFrag.setDepartureTripSelectedAnimationLayer(layerType);
				}

				//Misc
				Rect destinationSummaryLocation = ScreenPositionUtils
					.getGlobalScreenPositionWithoutTranslations(mFlightTwoFlightOneHeaderC);
				mFlightOneDetailsFrag.prepareDepartureFlightSelectedAnimation(destinationSummaryLocation);
			}
			else if (((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS))
				|| ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS
				|| stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS))) {
				//WE ARE GOING FROM SHOWING THE FLIGHT LIST TO SHWOING FLIGHT DETAILS

				//Vars - because we want re-use as much as possible, and this animation can happen on any leg of the flight, we set temp vars
				//to use while animating
				if ((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
					|| stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS
					|| stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS)) {
					mTransitionFiltersC = mFlightOneFiltersC;
					mTransitionListC = mFlightOneListC;
					mTransitionDetailsC = mFlightOneDetailsC;
					mTransitionDetailsFrag = mFlightOneDetailsFrag;
				}
				else if ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS
					|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS
					|| stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS)) {
					mTransitionFiltersC = mFlightTwoFiltersC;
					mTransitionListC = mFlightTwoListColumnC;
					mTransitionDetailsC = mFlightTwoDetailsC;
					mTransitionDetailsFrag = mFlightTwoDetailsFrag;
				}

				if (mTransitionDetailsFrag != null) {
					if (stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
						|| stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS) {
						//This is here to prevent a flicker when the container becomes visible
						int detailsTranslateDistance = mGrid.getColWidth(1) + mGrid.getColWidth(2);
						mTransitionDetailsFrag.setDetailsSlideInAnimationState(0f, detailsTranslateDistance, true);
					}
					mTransitionDetailsFrag.prepareSlideInAnimation();
				}

				//Visibility
				mTransitionFiltersC.setVisibility(View.VISIBLE);
				mTransitionDetailsC.setVisibility(View.VISIBLE);
				mTransitionListC.setVisibility(View.VISIBLE);

				//Rendering
				mTransitionFiltersC.setLayerType(layerType, null);
				mTransitionListC.setLayerType(layerType, null);
				if (mTransitionDetailsFrag != null) {
					mTransitionDetailsFrag.setSlideInAnimationLayer(layerType);
				}

			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				//WE ARE GOING FROM SHOWING THE FLIGHT DETAILS TO SHOWING ADD_TO_TRIP

				Rect addToTripDestination = getAddTripRect();
				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneListC.setVisibility(View.VISIBLE);
					mFlightOneDetailsC.setVisibility(View.VISIBLE);
					mFlightOneDetailsFrag.prepareAddToTripFromDetailsAnimation(addToTripDestination);
				}
				else {

					Rect departureFlightLocation = ScreenPositionUtils
						.getGlobalScreenPosition(mFlightTwoFlightOneHeaderC);
					mFlightOneDetailsFrag.prepareAddToTripFromDepartureAnimation(departureFlightLocation,
						addToTripDestination);
					mFlightOneDetailsC.setVisibility(View.VISIBLE);


					mFlightTwoListColumnC.setVisibility(View.VISIBLE);
					mFlightTwoListC.setVisibility(View.VISIBLE);
					mFlightTwoDetailsC.setVisibility(View.VISIBLE);

					mFlightTwoDetailsFrag.prepareAddToTripFromDetailsAnimation(addToTripDestination);
				}
			}
			else if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightOneListFrag.setListLockedToTop(false);
				mFlightOneListFrag.setPercentage(1f, 0);
				mFlightOneListC.setAlpha(0);
				mFlightOneListC.setVisibility(View.VISIBLE);
				positionForFilters(mFlightOneFiltersC, mFlightOneListC);
			}
			else if (isHistogramAndListCardFlipTransition(stateOne, stateTwo)) {
				mFlightHistogramC.setLayerType(layerType, null);
				mFlightOneListC.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
			if ((stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS)
				|| (stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN)) {
				float perc = stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN ? percentage : 1f - percentage;
				mFlightOneFiltersC.setAlpha(perc);
				mFlightMapC.setAlpha(perc);
				float filterPaneTopTranslation = (1f - perc)
					* mFlightOneListFrag.getMaxDistanceFromTop();
				mFlightOneFiltersC.setTranslationY(filterPaneTopTranslation);
				mFlightOneListFrag.setPercentage(1f - perc, 0);
			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS)) {
				//Between flight details and the next flight leg list/filters
				boolean forward = stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS;
				percentage = forward ? percentage : 1f - percentage;

				int flightOneListTranslationX = (int) (-mGrid.getColLeft(2) + percentage
					* -mGrid.getColSpanWidth(0, 1));

				int flightTwoTranslationX = (int) ((1f - percentage) * (mGrid.getColWidth(2) / 2f + mGrid
					.getColLeft(2)));

				mFlightOneListC.setTranslationX(flightOneListTranslationX);
				mFlightTwoFiltersC.setTranslationX(flightTwoTranslationX);
				mFlightTwoListColumnC.setTranslationX(flightTwoTranslationX);
				mFlightTwoFiltersC.setAlpha(percentage);
				mFlightTwoListColumnC.setAlpha(percentage);
				mFlightOneDetailsFrag.setDepartureTripSelectedAnimationState(percentage);
			}
			else if (((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS))
				|| ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS
				|| stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS))) {

				boolean forward = stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
					|| stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS;
				percentage = forward ? percentage : 1f - percentage;

				//Between filters and details
				if (mTransitionFiltersC != null) {


					mTransitionFiltersC.setTranslationX(percentage * -mGrid.getColSpanWidth(0, 1));
					mTransitionListC.setTranslationX(percentage * -mGrid.getColSpanWidth(0, 1));


					if (mTransitionDetailsFrag != null) {
						int detailsTranslateDistance = mGrid.getColSpanWidth(1, 4);
						mTransitionDetailsFrag.setDetailsSlideInAnimationState(percentage, detailsTranslateDistance,
							true);
					}
				}
			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				//Tell fragments about our transition
				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
				}
				else {
					mFlightOneDetailsFrag.setAddToTripFromDepartureAnimationState(percentage);
					mFlightTwoDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
				}

				//Move flight list out of view
				float flightListTranslationX = -mGrid.getColWidth(0) + -percentage
					* mGrid.getColWidth(0);

				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneListC.setTranslationX(flightListTranslationX);
				}
				else {
					mFlightTwoListColumnC.setTranslationX(flightListTranslationX);
				}
			}
			else if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightOneListC.setAlpha(percentage);
				mFlightMapC.setAlpha(1f - percentage);
			}
			else if (isHistogramAndListCardFlipTransition(stateOne, stateTwo)) {
				boolean forward = stateOne == ResultsFlightsState.FLIGHT_HISTOGRAM;
				ViewGroup outC;
				ViewGroup inC;

				if (forward) {
					outC = mFlightHistogramC;
					inC = mFlightOneListC;
				}
				else {
					outC = mFlightOneListC;
					inC = mFlightHistogramC;
				}

				if (percentage < .5f) {
					outC.setAlpha(1f);
					inC.setAlpha(0f);
				}
				else {
					outC.setAlpha(0f);
					inC.setAlpha(1f);
				}

				float outRotateY;
				float inRotateY;

				if (forward) {
					outRotateY = percentage * -180;
					inRotateY = (1f - percentage) * 180;
				}
				else {
					outRotateY = percentage * 180;
					inRotateY = (1f - percentage) * -180;
				}

				outC.setRotationY(outRotateY);
				inC.setRotationY(inRotateY);

			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			int layerType = View.LAYER_TYPE_NONE;
			if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS)) {
				//Rendering
				mFlightOneListC.setLayerType(layerType, null);
				mFlightTwoFiltersC.setLayerType(layerType, null);
				mFlightTwoListColumnC.setLayerType(layerType, null);
				if (mFlightOneDetailsFrag != null) {
					mFlightOneDetailsFrag.setDepartureTripSelectedAnimationLayer(layerType);
				}
			}
			else if (((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS
				|| stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS))
				|| ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
				&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS
				|| stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS))) {

				//Rendering
				mTransitionFiltersC.setLayerType(layerType, null);
				mTransitionListC.setLayerType(layerType, null);
				if (mTransitionDetailsFrag != null) {
					mTransitionDetailsFrag.setSlideInAnimationLayer(layerType);
				}

				mTransitionFiltersC = null;
				mTransitionListC = null;
				mTransitionDetailsC = null;
				mTransitionDetailsFrag = null;
			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS
				|| stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {

				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneDetailsFrag.finalizeAddToTripFromDepartureAnimation();
				}
				else {
					mFlightOneDetailsFrag.finalizeAddToTripFromDepartureAnimation();
					mFlightTwoDetailsFrag.finalizeAddToTripFromDetailsAnimation();
				}
			}
			else if (isHistogramAndListCardFlipTransition(stateOne, stateTwo)) {
				mFlightHistogramC.setLayerType(layerType, null);
				mFlightOneListC.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			setFragmentState(state);
			setTouchState(state);
			setVisibilityState(state);
			setFirstFlightListState(state);

			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN
				|| state == ResultsFlightsState.FLIGHT_HISTOGRAM) {
				mFlightOneFiltersC.setAlpha(0f);
				mFlightMapC.setAlpha(0f);
				if (mFlightOneListFrag != null && mFlightOneListFrag.hasList()) {
					mFlightOneFiltersC
						.setTranslationY(mFlightOneListFrag.getMaxDistanceFromTop());
					mFlightOneListFrag.setPercentage(1f, 0);
				}
			}
			else {
				mFlightOneFiltersC.setAlpha(1f);
				mFlightMapC.setAlpha(1f);
				mFlightOneFiltersC.setTranslationY(0f);
			}

			switch (state) {
			case LOADING:
			case FLIGHT_HISTOGRAM:
			case FLIGHT_LIST_DOWN:
				positionForFilters(mFlightOneFiltersC, mFlightOneListC);
				bindDataForFilters(mFlightOneListC, 0);
				break;
			case FLIGHT_ONE_FILTERS: {
				positionForFilters(mFlightOneFiltersC, mFlightOneListC);
				bindDataForFilters(mFlightOneListC, 0);
				break;
			}
			case FLIGHT_ONE_DETAILS: {
				positionForDetails(mFlightOneFiltersC, mFlightOneListC, mFlightOneDetailsFrag);
				bindDataForDetails(mFlightOneDetailsFrag, 0);
				break;
			}
			case FLIGHT_TWO_FILTERS: {
				positionForFilters(mFlightTwoFiltersC, mFlightTwoListColumnC);
				bindDataForFilters(mFlightTwoListColumnC, 1);
				break;
			}
			case FLIGHT_TWO_DETAILS: {
				positionForDetails(mFlightTwoFiltersC, mFlightTwoListColumnC, mFlightTwoDetailsFrag);
				bindDataForDetails(mFlightTwoDetailsFrag, 1);
				break;
			}
			case ADDING_FLIGHT_TO_TRIP: {
				setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
				break;
			}
			}

			// Some histogram/list card flip animation cleanup
			if (state == ResultsFlightsState.FLIGHT_HISTOGRAM) {
				mFlightHistogramC.setAlpha(1f);
				mFlightOneListC.setAlpha(0f);
				mFlightHistogramC.setRotationY(0f);
				mFlightHistogramC.setTouchPassThroughEnabled(false);
			}

			if (state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightOneListC.setAlpha(1f);
				mFlightHistogramC.setAlpha(0f);
				mFlightOneListC.setRotationY(0f);
				mFlightHistogramC.setTouchPassThroughEnabled(true);
				mFlightHistogramC.setTouchPassThroughReceiver(mFlightOneListC);
			}

			// Switch the flight line around
			switch (state) {
			case FLIGHT_LIST_DOWN:
			case FLIGHT_ONE_FILTERS:
			case FLIGHT_ONE_DETAILS:
				if (mFlightMapFrag.isAdded() && mFlightMapFrag.isMapGenerated()) {
					mFlightMapFrag.forward();
				}
				break;
			case FLIGHT_TWO_FILTERS:
			case FLIGHT_TWO_DETAILS:
				if (mFlightMapFrag.isAdded() && mFlightMapFrag.isMapGenerated()) {
					mFlightMapFrag.backward();
				}
				break;
			}

			//Make sure we are loading using the most recent params
			if (mFlightSearchDownloadFrag != null && state == ResultsFlightsState.LOADING) {
				importSearchParams();
				mFlightSearchDownloadFrag.startOrResumeForParams(Db.getFlightSearch().getSearchParams());
			}

			//The histogram spinner should show if we dont have flight resutls
			if (mFlightHistogramFrag != null && state == ResultsFlightsState.FLIGHT_HISTOGRAM) {
				mFlightHistogramFrag.setShowProgressBar(Db.getFlightSearch().getSearchResponse() == null);
			}
		}

		private boolean isHistogramAndListCardFlipTransition(ResultsFlightsState one, ResultsFlightsState two) {
			return (one == ResultsFlightsState.FLIGHT_HISTOGRAM && two == ResultsFlightsState.FLIGHT_LIST_DOWN) ||
				(one == ResultsFlightsState.FLIGHT_LIST_DOWN && two == ResultsFlightsState.FLIGHT_HISTOGRAM);
		}

		private void positionForFilters(ViewGroup filtersC, ViewGroup listC) {
			filtersC.setTranslationX(0f);
			listC.setTranslationX(0f);
		}

		private void bindDataForFilters(ViewGroup listC, int legPos) {
			// Clear the checked/blue/highlight item when returning to filters
			ListView listView = Ui.findView(listC, android.R.id.list);
			if (listView != null) {
				int checkedPos = listView.getCheckedItemPosition();
				if (checkedPos != ListView.INVALID_POSITION) {
					listView.setItemChecked(checkedPos, false);
				}
			}

			if (legPos == 1) {
				if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedLegs() != null
					&& Db.getFlightSearch().getSelectedLegs().length > 0
					&& Db.getFlightSearch().getSelectedLegs()[0] != null
					&& Db.getFlightSearch().getSelectedLegs()[0].getFlightTrip() != null) {
					mFlightOneSelectedRow.bind(Db.getFlightSearch(), 0);
				}
			}
		}

		private void positionForDetails(ViewGroup filtersC, ViewGroup listC, ResultsFlightDetailsFragment detailsFrag) {
			filtersC.setTranslationX(-mGrid.getColWidth(0));
			listC.setTranslationX(-mGrid.getColLeft(2));
			int detailsTranslateDistance = mGrid.getColSpanWidth(1, 4);
			detailsFrag.setDetailsSlideInAnimationState(1f, detailsTranslateDistance, true);
		}

		private void bindDataForDetails(ResultsFlightDetailsFragment detailsFrag, int pos) {
			detailsFrag.bindWithDb();

			// When going backwards, we must un-select the selected inbound leg, otherwise if
			// we query the trips again, we will return a limited set of results for the outbound
			// leg (based upon which trips work with that selected inbound flight).
			if (pos == 0 && Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				Db.getFlightSearch().setSelectedLeg(1, null);
			}
		}
	};

	/*
	EXPEDIA SERVICES FRAG LISTENER
	 */

	@Override
	public void onExpediaServicesDownload(ExpediaServicesFragment.ServiceType type, Response response) {
		if (type == ExpediaServicesFragment.ServiceType.FLIGHT_SEARCH) {
			Db.getFlightSearch().setSearchResponse((FlightSearchResponse) response);
			if (response != null) {
				Db.kickOffBackgroundFlightSearchSave(getActivity());
				Db.addAirlineNames(((FlightSearchResponse) response).getAirlineNames());
			}

			if (response != null && !response.hasErrors()) {
				mOneWayFlight = !Db.getFlightSearch().getSearchParams().isRoundTrip();
				setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
			}
			else {
				//TODO: Better Error Handling
				Ui.showToast(getActivity(), "FAIL FAIL FAIL - FLIGHT SEARCH ERROR");
			}

			if (mFlightHistogramFrag != null) {
				mFlightHistogramFrag.setShowProgressBar(false);
			}
		}
		else if (type == ExpediaServicesFragment.ServiceType.FLIGHT_GDE_SEARCH) {
			if (response != null && !response.hasErrors()) {
				Db.setFlightSearchHistogramResponse((FlightSearchHistogramResponse) response);
				if (Db.getFlightSearch() == null || (Db.getFlightSearch() != null &&
					Db.getFlightSearch().getSearchResponse() == null)) {
					setFlightsState(ResultsFlightsState.FLIGHT_HISTOGRAM, true);
				}
			}
			else {
				//TODO: Better Error Handling
				Ui.showToast(getActivity(), "FAIL FAIL FAIL - GDE DATA DOWNLOAD!");
			}
		}
	}

	/*
	HISTOGRAM FRAG LISTENER
	 */

	@Override
	public void onHeaderClick() {
		// If we have flight search data in the Db then hop over to the actual flight results,
		if (Db.getFlightSearch() != null & Db.getFlightSearch().getSearchResponse() != null) {
			setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
		}
	}

}
