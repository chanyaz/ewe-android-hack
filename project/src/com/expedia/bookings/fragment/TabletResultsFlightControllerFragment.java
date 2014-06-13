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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.ResultsFlightLegState;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsFlightControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to FLIGHTS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsFlightControllerFragment extends Fragment implements
	IFragmentAvailabilityProvider, IBackManageable,
	IStateProvider<ResultsFlightsState>, ExpediaServicesFragment.ExpediaServicesFragmentListener,
	IAcceptingListenersListener {

	//State
	private static final String STATE_FLIGHTS_STATE = "STATE_FLIGHTS_STATE";

	//Frag tags
	private static final String FTAG_FLIGHT_MAP = "FTAG_FLIGHT_MAP";
	private static final String FTAG_FLIGHT_ADD_TO_TRIP = "FTAG_FLIGHT_ADD_TO_TRIP";
	private static final String FTAG_FLIGHT_SEARCH_DOWNLOAD = "FTAG_FLIGHT_SEARCH_DOWNLOAD";
	private static final String FTAG_FLIGHT_SEARCH_ERROR = "FTAG_FLIGHT_SEARCH_ERROR";
	private static final String FTAG_FLIGHT_LOADING_INDICATOR = "FTAG_FLIGHT_LOADING_INDICATOR";
	private static final String FTAG_FLIGHT_LEGS_CHOOSER = "FTAG_FLIGHT_LEGS_CHOOSER";
	private static final String FTAG_FLIGHT_INFANT_CHOOSER = "FTAG_FLIGHT_INFANT_CHOOSER";

	//Settings
	private static final long PARAM_UPDATE_COOLDOWN_MS = 500;

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mFlightMapC;
	private FrameLayoutTouchController mAddToTripC;
	private FrameLayoutTouchController mFlightLegsC;
	private FrameLayoutTouchController mLoadingC;
	private FrameLayoutTouchController mSearchErrorC;

	private ArrayList<View> mVisibilityControlledViews = new ArrayList<View>();

	//Fragments
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsFlightAddToTrip mAddToTripFrag;
	private FlightSearchDownloadFragment mFlightSearchDownloadFrag;
	private ResultsListLoadingFragment mLoadingGuiFrag;
	private ResultsRecursiveFlightLegsFragment mFlightLegsFrag;
	private ResultsListSearchErrorFragment mSearchErrorFrag;
	private InfantChooserDialogFragment mInfantFrag;
	private Runnable mSearchParamUpdateRunner;

	//Other
	private View mAddToTripShadeView;
	private GridManager mGrid = new GridManager();
	private StateManager<ResultsFlightsState> mFlightsStateManager = new StateManager<ResultsFlightsState>(
		ResultsFlightsState.LOADING, this);

	//When we are downloading new data, we set this to true, so that we remember to resetQuery on our legs chooser.
	private boolean mNeedsQueryReset = true;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mFlightsStateManager.setDefaultState(ResultsFlightsState.valueOf(savedInstanceState.getString(
				STATE_FLIGHTS_STATE,
				getBaseState().name())));
		}

		//If we are in a state such that we think we should have data, lets try to load it.
		ResultsFlightsState state = mFlightsStateManager.getState();
		if (state != ResultsFlightsState.LOADING && state != ResultsFlightsState.SEARCH_ERROR) {
			if (Db.getFlightSearch() == null || Db.getFlightSearch().getSearchResponse() == null) {
				if (!Db.loadCachedFlightData(getActivity())) {
					mFlightsStateManager.setDefaultState(ResultsFlightsState.LOADING);
				}
				else {
					Db.loadFlightSearchParamsFromDisk(getActivity());
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			FragmentManager manager = getChildFragmentManager();
			mFlightMapFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_MAP);
			mAddToTripFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_ADD_TO_TRIP);
			mFlightSearchDownloadFrag = FragmentAvailabilityUtils
				.getFrag(manager, FTAG_FLIGHT_SEARCH_DOWNLOAD);
			mLoadingGuiFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_LOADING_INDICATOR);
			mFlightLegsFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_LEGS_CHOOSER);
			mSearchErrorFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_SEARCH_ERROR);
		}

		View view = inflater.inflate(R.layout.fragment_tablet_results_flights, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mFlightMapC = Ui.findView(view, R.id.bg_flight_map);
		mAddToTripC = Ui.findView(view, R.id.flights_add_to_trip);
		mLoadingC = Ui.findView(view, R.id.loading_container);
		mFlightLegsC = Ui.findView(view, R.id.flight_leg_container);
		mSearchErrorC = Ui.findView(view, R.id.search_error_container);
		mAddToTripShadeView = Ui.findView(view, R.id.flights_add_to_trip_shade);

		mVisibilityControlledViews.add(mFlightMapC);
		mVisibilityControlledViews.add(mAddToTripC);
		mVisibilityControlledViews.add(mLoadingC);
		mVisibilityControlledViews.add(mFlightLegsC);
		mVisibilityControlledViews.add(mSearchErrorC);
		mVisibilityControlledViews.add(mAddToTripShadeView);

		registerStateListener(new StateListenerLogger<ResultsFlightsState>(), false);
		registerStateListener(mFlightsStateHelper, false);

		//TODO: This should not be here. We are consuming GPU memory needlessly most of the time.
		//These views should be moved to hardware layers in onStateTransitionStart for relevant transitions and moved off
		//of hardware layers in onStateTransitionEnd.
		mFlightMapC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mAddToTripShadeView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

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
		mBackManager.unregisterWithParent(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mResultsStateHelper.unregisterWithProvider(this);
	}

	public Rect getAddTripRect() {
		if(mAddToTripFrag != null){
			return mAddToTripFrag.getCenteredAddToTripRect();
		}
		return new Rect();
	}

	public void setAnimateToBucketRect(Rect globalRect) {
		if (mAddToTripFrag != null) {
			mAddToTripFrag.setGlobalDestinationRect(globalRect);
		}
	}

	// Base state is semi-complicated at this point. For now, it'll work like this:
	// 1. If we have actual FlightSearch data, let's show that.
	// 2. If we don't have FlightSearch data but do have flight histogram data, let's show that
	// 3. Otherwise, let's just show the loading state
	private ResultsFlightsState getBaseState() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null && Db
			.getFlightSearch().getSearchResponse().hasErrors()) {
			return ResultsFlightsState.SEARCH_ERROR;
		}
		else if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null) {
			return ResultsFlightsState.FLIGHT_LIST_DOWN;
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
			if (mFlightSearchDownloadFrag != null) {
				//We dont care if our last search finished, we are waiting for our cooldown period before we want to
				//commit to doing a full search.
				mFlightSearchDownloadFrag.ignoreNextDownload();
			}
			if (mSearchParamUpdateRunner != null) {
				mRootC.removeCallbacks(mSearchParamUpdateRunner);
			}
			mSearchParamUpdateRunner = new SearchParamUpdateRunner();
			mRootC.postDelayed(mSearchParamUpdateRunner, PARAM_UPDATE_COOLDOWN_MS);
		}
	}


	private class SearchParamUpdateRunner implements Runnable {
		@Override
		public void run() {
			if (mSearchParamUpdateRunner == this && getActivity() != null
				&& mFlightsStateManager.getState() == ResultsFlightsState.LOADING
				&& mFlightSearchDownloadFrag != null) {
				importSearchParams();
				mFlightSearchDownloadFrag.startOrResumeForParams(Db.getFlightSearch().getSearchParams());
			}
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
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHT_MAP) {
			frag = this.mFlightMapFrag;
		}
		else if (tag == FTAG_FLIGHT_ADD_TO_TRIP) {
			frag = this.mAddToTripFrag;
		}
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = mFlightSearchDownloadFrag;
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = mLoadingGuiFrag;
		}
		else if (tag == FTAG_FLIGHT_LEGS_CHOOSER) {
			frag = mFlightLegsFrag;
		}
		else if (tag == FTAG_FLIGHT_SEARCH_ERROR) {
			frag = mSearchErrorFrag;
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
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = FlightSearchDownloadFragment.newInstance(Sp.getParams().toFlightSearchParams());
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = ResultsListLoadingFragment.newInstance(getString(R.string.loading_flights), Gravity.CENTER,
				Gravity.LEFT);
		}
		else if (tag == FTAG_FLIGHT_LEGS_CHOOSER) {
			frag = ResultsRecursiveFlightLegsFragment.newInstance(0);
		}
		else if (tag == FTAG_FLIGHT_SEARCH_ERROR) {
			frag = ResultsListSearchErrorFragment.newInstance(getString(R.string.tablet_search_results_flights_unavailable), R.drawable.ic_tablet_sold_out_flight);
		}

		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_FLIGHT_MAP) {
			updateMapFragSizes((ResultsFlightMapFragment) frag);
		}
	}

	private void updateMapFragSizes(ResultsFlightMapFragment frag) {
		if (frag != null) {
			int padding = getResources().getDimensionPixelSize(R.dimen.tablet_results_flight_map_padding);
			frag.setPadding(mGrid.getColLeft(4) + padding, padding, padding, padding);
		}
	}

	/*
	 * STATE HELPER METHODS
	 */

	public void setFlightsState(ResultsFlightsState state, boolean animate) {
		mFlightsStateManager.setState(state, animate);
	}

	public ResultsFlightsState getState() {
		return mFlightsStateManager.getState();
	}

	private void setTouchState(ResultsFlightsState flightsState) {
		ArrayList<ViewGroup> touchableViews = new ArrayList<ViewGroup>();
		switch (flightsState) {
		case FLIGHT_LIST_DOWN: {
			touchableViews.add(mFlightLegsC);
			break;
		}
		case CHOOSING_FLIGHT: {
			touchableViews.add(mFlightLegsC);
			break;
		}
		case ADDING_FLIGHT_TO_TRIP: {
			break;
		}
		}

		for (View vg : mVisibilityControlledViews) {
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
		ArrayList<View> visibleViews = new ArrayList<View>();

		switch (flightsState) {
		case SEARCH_ERROR: {
			visibleViews.add(mSearchErrorC);
			break;
		}
		case LOADING: {
			visibleViews.add(mLoadingC);
			break;
		}
		case FLIGHT_LIST_DOWN: {
			visibleViews.add(mFlightLegsC);
			break;
		}
		case CHOOSING_FLIGHT: {
			visibleViews.add(mFlightMapC);
			visibleViews.add(mFlightLegsC);
			break;
		}
		case ADDING_FLIGHT_TO_TRIP: {
			visibleViews.add(mAddToTripC);
			visibleViews.add(mFlightMapC);
			visibleViews.add(mFlightLegsC);
			visibleViews.add(mAddToTripShadeView);
			break;
		}
		}

		for (View vg : mVisibilityControlledViews) {
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
		boolean searchErrorAvailable = false;
		boolean flightSearchDownloadAvailable = false;
		boolean flightMapAvailable = true;
		boolean flightAddToTripAvailable = true;
		boolean flightLegsFragAvailable = true;

		if (flightsState == ResultsFlightsState.LOADING || flightsState == ResultsFlightsState.SEARCH_ERROR) {
			// This case kicks off the downloads
			if (flightsState == ResultsFlightsState.LOADING) {
				flightSearchDownloadAvailable = true;
				loadingAvailable = true;
				searchErrorAvailable = false;
			}
			else {
				flightSearchDownloadAvailable = false;
				loadingAvailable = false;
				searchErrorAvailable = true;
			}

			flightLegsFragAvailable = false;
			flightMapAvailable = false;
			flightAddToTripAvailable = false;
		}

		mFlightMapFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightMapAvailable, FTAG_FLIGHT_MAP,
			manager, transaction, this, R.id.bg_flight_map, false);
		mAddToTripFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightAddToTripAvailable,
			FTAG_FLIGHT_ADD_TO_TRIP, manager, transaction, this, R.id.flights_add_to_trip, false);
		mFlightSearchDownloadFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightSearchDownloadAvailable,
			FTAG_FLIGHT_SEARCH_DOWNLOAD, manager, transaction, this, 0, true);
		mLoadingGuiFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			loadingAvailable,
			FTAG_FLIGHT_LOADING_INDICATOR, manager, transaction, this, R.id.loading_container, true);
		mFlightLegsFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightLegsFragAvailable,
			FTAG_FLIGHT_LEGS_CHOOSER, manager, transaction, this, R.id.flight_leg_container, false);
		mSearchErrorFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(searchErrorAvailable, FTAG_FLIGHT_SEARCH_ERROR, manager, transaction, this,
				R.id.search_error_container, false);
		transaction.commit();

	}

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
				startStateTransition(ResultsFlightsState.FLIGHT_LIST_DOWN, ResultsFlightsState.CHOOSING_FLIGHT);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				startStateTransition(ResultsFlightsState.CHOOSING_FLIGHT, ResultsFlightsState.FLIGHT_LIST_DOWN);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				updateStateTransition(ResultsFlightsState.FLIGHT_LIST_DOWN, ResultsFlightsState.CHOOSING_FLIGHT,
					percentage);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				updateStateTransition(ResultsFlightsState.CHOOSING_FLIGHT, ResultsFlightsState.FLIGHT_LIST_DOWN,
					percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				endStateTransition(ResultsFlightsState.FLIGHT_LIST_DOWN, ResultsFlightsState.CHOOSING_FLIGHT);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				endStateTransition(ResultsFlightsState.CHOOSING_FLIGHT, ResultsFlightsState.FLIGHT_LIST_DOWN);
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
					setFlightsState(ResultsFlightsState.CHOOSING_FLIGHT, false);
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
			mGrid.setContainerToColumn(mLoadingC, 2);
			mGrid.setContainerToColumn(mSearchErrorC, 2);
			mGrid.setContainerToColumnSpan(mFlightLegsC, 0, 4);

			//Special cases
			mGrid.setContainerToRowSpan(mFlightMapC, 0, 2);
			mGrid.setContainerToRow(mLoadingC, 2);
			mGrid.setContainerToRow(mSearchErrorC, 2);

			//Frag stuff
			updateMapFragSizes(mFlightMapFrag);
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
				if (state == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
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

		//If we have arrived at the ADDING_FLIGHT_TO_TRIP state, we want to immediately move to the list down state.
		//We do this here so that finalize has been called on all listeners before moving on.
		if (state == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
			setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
		}
	}

	@Override
	public void registerStateListener(IStateListener<ResultsFlightsState> listener, boolean fireFinalizeState) {
		mFlightsStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsFlightsState> listener) {
		mFlightsStateListeners.unRegisterStateListener(listener);
	}

	public void setListenerActive(IStateListener<ResultsFlightsState> listener, boolean active) {
		if (active) {
			mFlightsStateListeners.setListenerActive(listener);
		}
		else {
			mFlightsStateListeners.setListenerInactive(listener);
		}
	}

	/*
	 * FLIGHTS STATE LISTENER
	 */

	private StateListenerHelper<ResultsFlightsState> mFlightsStateHelper = new StateListenerHelper<ResultsFlightsState>() {

		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {

			if ((stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.CHOOSING_FLIGHT)
				|| (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN)) {
				mFlightMapC.setVisibility(View.VISIBLE);
			}
			else if (stateOne == ResultsFlightsState.LOADING && stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {

				if (mLoadingGuiFrag != null) {
					//init the animation
					mLoadingGuiFrag.initGrowToRowsAnimation();
				}
			}
			else if (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				mAddToTripC.setAlpha(0f);
				mAddToTripC.setVisibility(View.VISIBLE);
				mAddToTripShadeView.setAlpha(0f);
				mAddToTripShadeView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
			if ((stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.CHOOSING_FLIGHT)
				|| (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN)) {
				float perc = stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN ? percentage : 1f - percentage;
				mFlightMapC.setAlpha(perc);
			}
			else if (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				mAddToTripC.setAlpha(percentage);
				mAddToTripShadeView.setAlpha(percentage);
			}
			else if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightMapC.setAlpha(1f - percentage);
				mAddToTripShadeView.setAlpha(1f - percentage);
			}
			else if (stateOne == ResultsFlightsState.LOADING && stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mLoadingGuiFrag.setGrowToRowsAnimPercentage(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.CHOOSING_FLIGHT) {
				popInfantPromptIfNeeded();
			}
			else if (stateOne == ResultsFlightsState.LOADING && stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				//The loading fragment is about to be removed in onFinalize, but lets reset it beforehand regardless.
				mLoadingC.setVisibility(View.INVISIBLE);
				mLoadingGuiFrag.cleanUpGrowToRowsAnim();
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			setFragmentState(state);
			setTouchState(state);
			setVisibilityState(state);

			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN
				|| state == ResultsFlightsState.SEARCH_ERROR) {
				mFlightMapC.setAlpha(0f);
			}
			else {
				mFlightMapC.setAlpha(1f);
			}

			//Make sure we are loading using the most recent params
			if (mFlightSearchDownloadFrag != null && state == ResultsFlightsState.LOADING) {
				importSearchParams();
				mFlightSearchDownloadFrag.startOrResumeForParams(Db.getFlightSearch().getSearchParams());
			}

			if (mFlightLegsFrag != null && state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				if (mNeedsQueryReset || mFlightLegsFrag.getState() != ResultsFlightLegState.LIST_DOWN) {
					mFlightLegsFrag.resetQuery();
					mFlightLegsFrag.setState(ResultsFlightLegState.LIST_DOWN, false);
					mNeedsQueryReset = false;
				}
			}

			if (mFlightLegsFrag != null && state != ResultsFlightsState.LOADING
				&& state != ResultsFlightsState.SEARCH_ERROR) {
				mFlightLegsFrag.setAddToTripRect(getAddTripRect());
			}

			if (state == ResultsFlightsState.LOADING) {
				mNeedsQueryReset = true;
			}

			if (state == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				mAddToTripC.setAlpha(1f);
			}
			else {
				mAddToTripC.setAlpha(0f);
			}
		}
	};

	// Infants

	private void popInfantPromptIfNeeded() {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		if (params.hasInfants() && !params.hasMoreInfantsThanAvailableLaps()) {
			mInfantFrag = Ui.findSupportFragment(this, FTAG_FLIGHT_INFANT_CHOOSER);
			if (mInfantFrag == null) {
				mInfantFrag = InfantChooserDialogFragment.newInstance();
			}
			if (!mInfantFrag.isAdded()) {
				mInfantFrag.show(getFragmentManager(), "infantChooser");
			}
		}
	}

	@Subscribe
	public void onSimpleDialogCallbackClick(Events.SimpleCallBackDialogOnClick click) {
		if (click.callBackId == SimpleCallbackDialogFragment.CODE_TABLET_FLIGHTS_INFANT_CHOOSER) {

			boolean newLapPref = !Db.getFlightSearch().getSearchParams().getInfantSeatingInLap();
			Sp.getParams().setInfantsInLaps(newLapPref);
			Sp.reportSpUpdate();
		}
	}

	@Subscribe
	public void onSimpleCallbackCancel(Events.SimpleCallBackDialogOnCancel cancel) {
		if (cancel.callBackId == SimpleCallbackDialogFragment.CODE_TABLET_FLIGHTS_INFANT_CHOOSER) {
			popInfantPromptIfNeeded();
		}
	}

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
				//We need the legs fragment to start drawing so we can animate it in
				FragmentManager manager = getChildFragmentManager();
				FragmentTransaction transaction = manager.beginTransaction();
				mFlightLegsFrag = FragmentAvailabilityUtils.setFragmentAvailability(
					true,
					FTAG_FLIGHT_LEGS_CHOOSER, manager, transaction, TabletResultsFlightControllerFragment.this,
					R.id.flight_leg_container, false);
				transaction.commit();
				manager.executePendingTransactions();
				mFlightLegsC.setVisibility(View.VISIBLE);

				setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
			}
			else if (!mFlightSearchDownloadFrag.isDownloadingFlightSearch()) {
				//If we aren't downloading, and we dont have a valid response, we move to the error state
				setFlightsState(ResultsFlightsState.SEARCH_ERROR, false);
			}
		}
	}

	/*
	LEG STATE LISTENER
	 */

	private StateListenerHelper<ResultsFlightLegState> mLegStateListener = new StateListenerHelper<ResultsFlightLegState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (validTransition(stateOne, stateTwo)) {
				startStateTransition(translate(stateOne), translate(stateTwo));
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
			float percentage) {
			if (validTransition(stateOne, stateTwo)) {
				updateStateTransition(translate(stateOne), translate(stateTwo), percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (validTransition(stateOne, stateTwo)) {
				endStateTransition(translate(stateOne), translate(stateTwo));
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightLegState state) {
			if (mFlightsStateManager.getState() != translate(state)) {
				setFlightsState(translate(state), false);
			}
			if (state == ResultsFlightLegState.LATER_LEG) {
				if (mFlightMapFrag != null && mFlightMapFrag.isAdded() && mFlightMapFrag.isMapGenerated()) {
					mFlightMapFrag.backward();
				}
			}
			else if (state == ResultsFlightLegState.LIST_DOWN || state == ResultsFlightLegState.FILTERS
				|| state == ResultsFlightLegState.DETAILS) {
				if (mFlightMapFrag != null && mFlightMapFrag.isAdded() && mFlightMapFrag.isMapGenerated()) {
					mFlightMapFrag.forward();
				}
			}
		}

		private boolean validTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (stateOne == ResultsFlightLegState.ADDING_TO_TRIP) {
				return false;
			}
			if (stateOne == stateTwo) {
				return false;
			}
			return true;
		}

		private ResultsFlightsState translate(ResultsFlightLegState state) {
			if (state == ResultsFlightLegState.LIST_DOWN) {
				return getBaseState();
			}
			else if (state == ResultsFlightLegState.ADDING_TO_TRIP) {
				return ResultsFlightsState.ADDING_FLIGHT_TO_TRIP;
			}
			else {
				return ResultsFlightsState.CHOOSING_FLIGHT;
			}
		}
	};

	/*
	IAcceptingListenersListener
	 */

	@Override
	public void acceptingListenersUpdated(Fragment frag, boolean acceptingListener) {
		if (acceptingListener) {
			if (frag == mFlightLegsFrag) {
				mFlightLegsFrag.registerStateListener(mLegStateListener, false);
			}
		}
		else {
			if (frag == mFlightLegsFrag) {
				mFlightLegsFrag.unRegisterStateListener(mLegStateListener);
			}
		}
	}
}
