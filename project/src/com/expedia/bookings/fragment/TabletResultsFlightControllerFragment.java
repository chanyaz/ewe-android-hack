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
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsFlightControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to FLIGHTS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsFlightControllerFragment extends Fragment implements
	IFragmentAvailabilityProvider, IBackManageable,
	IStateProvider<ResultsFlightsState>, IDoneClickedListener, ExpediaServicesFragment.ExpediaServicesFragmentListener,
	ResultsFlightHistogramFragment.HistogramFragmentListener {

	//State
	private static final String STATE_FLIGHTS_STATE = "STATE_FLIGHTS_STATE";

	//Frag tags
	private static final String FTAG_FLIGHT_MAP = "FTAG_FLIGHT_MAP";
	private static final String FTAG_FLIGHT_ADD_TO_TRIP = "FTAG_FLIGHT_ADD_TO_TRIP";
	private static final String FTAG_FLIGHT_HISTOGRAM = "FTAG_FLIGHT_HISTOGRAM";
	private static final String FTAG_FLIGHT_SEARCH_DOWNLOAD = "FTAG_FLIGHT_SEARCH_DOWNLOAD";
	private static final String FTAG_FLIGHT_LOADING_INDICATOR = "FTAG_FLIGHT_LOADING_INDICATOR";
	private static final String FTAG_FLIGHT_LEGS_CHOOSER = "FTAG_FLIGHT_LEGS_CHOOSER";

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mFlightMapC;
	private FrameLayoutTouchController mAddToTripC;
	private FrameLayoutTouchController mFlightHistogramC;
	private FrameLayoutTouchController mFlightLegsC;
	private FrameLayoutTouchController mLoadingC;

	private ArrayList<ViewGroup> mContainers = new ArrayList<ViewGroup>();

	//Fragments
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsFlightAddToTrip mAddToTripFrag;
	private ResultsFlightHistogramFragment mFlightHistogramFrag;
	private FlightSearchDownloadFragment mFlightSearchDownloadFrag;
	private ResultsListLoadingFragment mLoadingGuiFrag;
	private ResultsRecursiveFlightLegsFragment mFlightLegsFrag;

	//Other
	private GridManager mGrid = new GridManager();
	private StateManager<ResultsFlightsState> mFlightsStateManager = new StateManager<ResultsFlightsState>(
		ResultsFlightsState.LOADING, this);

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
		mLoadingC = Ui.findView(view, R.id.loading_container);
		mFlightLegsC = Ui.findView(view, R.id.flight_leg_container);

		mContainers.add(mFlightMapC);
		mContainers.add(mAddToTripC);
		mContainers.add(mLoadingC);
		mContainers.add(mFlightHistogramC);
		mContainers.add(mFlightLegsC);

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
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = mFlightSearchDownloadFrag;
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = mLoadingGuiFrag;
		}
		else if (tag == FTAG_FLIGHT_LEGS_CHOOSER) {
			frag = mFlightLegsFrag;
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
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = FlightSearchDownloadFragment.newInstance(Sp.getParams().toFlightSearchParams());
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = ResultsListLoadingFragment.newInstance(getString(R.string.loading_flights));
		}
		else if (tag == FTAG_FLIGHT_LEGS_CHOOSER) {
			frag = ResultsRecursiveFlightLegsFragment.newInstance(0);
		}

		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {

		if (tag == FTAG_FLIGHT_MAP) {
			updateMapFragSizes((ResultsFlightMapFragment) frag);
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

	private void updateMapFragSizes(ResultsFlightMapFragment frag) {
		if (frag != null && mGrid.isLandscape()) {
			int padding = getResources().getDimensionPixelSize(R.dimen.tablet_results_flight_map_padding);
			frag.setPadding(mGrid.getColLeft(2) + padding, padding, padding, padding);
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
			//visibleViews.add(mFlightHistogramC);
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
		boolean flightMapAvailable = true;
		boolean flightAddToTripAvailable = true;
		boolean flightLegsFragAvailable = true;

		if (flightsState == ResultsFlightsState.LOADING) {
			// This case kicks off the downloads
			flightSearchDownloadAvailable = true;
			loadingAvailable = true;
			flightLegsFragAvailable = false;
			flightMapAvailable = false;
			flightAddToTripAvailable = false;
		}
		else if (flightsState == ResultsFlightsState.FLIGHT_HISTOGRAM) {
			flightSearchDownloadAvailable = true;
			flightHistogramAvailable = true;
			flightMapAvailable = false;
			flightAddToTripAvailable = false;
		}
		else if (flightsState == ResultsFlightsState.FLIGHT_LIST_DOWN) {
			flightHistogramAvailable = true;
		}

		mFlightMapFrag = (ResultsFlightMapFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightMapAvailable, FTAG_FLIGHT_MAP,
			manager, transaction, this, R.id.bg_flight_map, false);
		mAddToTripFrag = (ResultsFlightAddToTrip) FragmentAvailabilityUtils.setFragmentAvailability(
			flightAddToTripAvailable,
			FTAG_FLIGHT_ADD_TO_TRIP, manager, transaction, this, R.id.flights_add_to_trip, false);
		mFlightHistogramFrag = FragmentAvailabilityUtils.setFragmentAvailability(flightHistogramAvailable,
			FTAG_FLIGHT_HISTOGRAM, manager, transaction, this, R.id.flight_histogram_container, true);
		mFlightSearchDownloadFrag = (FlightSearchDownloadFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			flightSearchDownloadAvailable,
			FTAG_FLIGHT_SEARCH_DOWNLOAD, manager, transaction, this, 0, true);
		mLoadingGuiFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			loadingAvailable,
			FTAG_FLIGHT_LOADING_INDICATOR, manager, transaction, this, R.id.loading_container, true);
		mFlightLegsFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightLegsFragAvailable,
			FTAG_FLIGHT_LEGS_CHOOSER, manager, transaction, this, R.id.flight_leg_container, false);
		transaction.commit();

	}

	/*
	 * FLIGHT LIST FRAGMENT LISTENER
	 */
	@Override
	public void onDoneClicked() {
		if (mFlightsStateManager.getState() == ResultsFlightsState.CHOOSING_FLIGHT) {
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
			if (mFlightsStateManager.hasState() && (state == ResultsFlightsState.CHOOSING_FLIGHT
				|| state == ResultsFlightsState.FLIGHT_LIST_DOWN)) {
				return true;
			}
			return false;
		}

		private ResultsFlightsState getFlightsStateFromListState(ResultsFlightsListState state) {
			if (state == ResultsFlightsListState.FLIGHTS_LIST_AT_TOP) {
				return ResultsFlightsState.CHOOSING_FLIGHT;
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
			mGrid.setContainerToColumn(mFlightHistogramC, 2);
			mGrid.setContainerToColumn(mLoadingC, 2);
			mGrid.setContainerToColumnSpan(mFlightLegsC, 0, 4);

			//Special cases
			mGrid.setContainerToRowSpan(mFlightMapC, 0, 2);
			mGrid.setContainerToRow(mFlightHistogramC, 2);
			mGrid.setContainerToRow(mLoadingC, 2);

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

				if (state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
					return false;
				}
				else if (state == ResultsFlightsState.CHOOSING_FLIGHT) {
					setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
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

		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			int layerType = View.LAYER_TYPE_HARDWARE;
			if ((stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.CHOOSING_FLIGHT)
				|| (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN)) {
				mFlightMapC.setVisibility(View.VISIBLE);
			}
			else if (isHistogramAndListCardFlipTransition(stateOne, stateTwo)) {
				mFlightHistogramC.setLayerType(layerType, null);
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
			else if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightMapC.setAlpha(1f - percentage);
			}
			else if (isHistogramAndListCardFlipTransition(stateOne, stateTwo)) {
				boolean forward = stateOne == ResultsFlightsState.FLIGHT_HISTOGRAM;
				ViewGroup outC;
				ViewGroup inC;

				if (forward) {
					outC = mFlightHistogramC;
					inC = mFlightHistogramC;
				}
				else {
					outC = mFlightHistogramC;
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
			if (isHistogramAndListCardFlipTransition(stateOne, stateTwo)) {
				mFlightHistogramC.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			setFragmentState(state);
			setTouchState(state);
			setVisibilityState(state);

			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN
				|| state == ResultsFlightsState.FLIGHT_HISTOGRAM) {

				mFlightMapC.setAlpha(0f);
			}
			else {
				mFlightMapC.setAlpha(1f);
			}

			switch (state) {
			case ADDING_FLIGHT_TO_TRIP: {
				setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
				break;
			}
			}

			// Some histogram/list card flip animation cleanup
			if (state == ResultsFlightsState.FLIGHT_HISTOGRAM) {
				mFlightHistogramC.setAlpha(1f);
				mFlightHistogramC.setRotationY(0f);
				mFlightHistogramC.setTouchPassThroughEnabled(false);
			}

			if (state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightHistogramC.setAlpha(0f);
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
