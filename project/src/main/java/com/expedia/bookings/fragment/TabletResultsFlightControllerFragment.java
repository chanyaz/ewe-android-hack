package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Locale;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.pos.PointOfSale;
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
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsFlightControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to FLIGHTS results
 */
public class TabletResultsFlightControllerFragment extends Fragment implements
	IFragmentAvailabilityProvider, IBackManageable,
	IStateProvider<ResultsFlightsState>, IAcceptingListenersListener {

	// State
	private static final String STATE_FLIGHTS_STATE = "STATE_FLIGHTS_STATE";

	// Frag tags
	private static final String FTAG_FLIGHT_MAP = "FTAG_FLIGHT_MAP";
	private static final String FTAG_FLIGHT_SEARCH_DOWNLOAD = "FTAG_FLIGHT_SEARCH_DOWNLOAD";
	private static final String FTAG_FLIGHT_SEARCH_ERROR = "FTAG_FLIGHT_SEARCH_ERROR";
	private static final String FTAG_FLIGHT_LOADING_INDICATOR = "FTAG_FLIGHT_LOADING_INDICATOR";
	private static final String FTAG_FLIGHT_LEGS_CHOOSER = "FTAG_FLIGHT_LEGS_CHOOSER";
	private static final String FTAG_FLIGHT_INFANT_CHOOSER = "FTAG_FLIGHT_INFANT_CHOOSER";

	// Containers
	private ViewGroup mRootC;
	private TouchableFrameLayout mFlightMapC;
	private TouchableFrameLayout mFlightLegsC;
	private TouchableFrameLayout mLoadingC;
	private TouchableFrameLayout mSearchErrorC;
	private FrameLayout mRouteDescriptionC;

	private ArrayList<View> mVisibilityControlledViews = new ArrayList<View>();

	// Fragments
	private ResultsFlightMapFragment mFlightMapFrag;
	private FlightSearchDownloadFragment mFlightSearchDownloadFrag;
	private ResultsListLoadingFragment mLoadingGuiFrag;
	private ResultsRecursiveFlightLegsFragment mFlightLegsFrag;
	private ResultsListSearchErrorFragment mSearchErrorFrag;
	private FlightInfantChooserDialogFragment mInfantFrag;

	// Other
	private GridManager mGrid = new GridManager();
	private StateManager<ResultsFlightsState> mFlightsStateManager = new StateManager<ResultsFlightsState>(
		ResultsFlightsState.LOADING, this);

	private boolean mCouldShowInfantPrompt = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (FragmentBailUtils.shouldBail(getActivity())) {
			return;
		}

		if (savedInstanceState != null) {
			mFlightsStateManager.setDefaultState(ResultsFlightsState.valueOf(savedInstanceState.getString(
				STATE_FLIGHTS_STATE, getBaseState().name())));
		}
		else {
			mFlightsStateManager.setDefaultState(getBaseState());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			FragmentManager manager = getChildFragmentManager();
			mFlightMapFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_MAP);
			mFlightSearchDownloadFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_SEARCH_DOWNLOAD);
			mLoadingGuiFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_LOADING_INDICATOR);
			mFlightLegsFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_LEGS_CHOOSER);
			mSearchErrorFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHT_SEARCH_ERROR);
		}

		View view = inflater.inflate(R.layout.fragment_tablet_results_flights, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mFlightMapC = Ui.findView(view, R.id.bg_flight_map);
		mLoadingC = Ui.findView(view, R.id.loading_container);
		mFlightLegsC = Ui.findView(view, R.id.flight_leg_container);
		mSearchErrorC = Ui.findView(view, R.id.search_error_container);
		mRouteDescriptionC = Ui.findView(view, R.id.route_desc_container);

		mVisibilityControlledViews.add(mFlightMapC);
		mVisibilityControlledViews.add(mLoadingC);
		mVisibilityControlledViews.add(mFlightLegsC);
		mVisibilityControlledViews.add(mSearchErrorC);

		registerStateListener(mFlightsStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsFlightsState>(), false);

		// TODO: This should not be here. We are consuming GPU memory needlessly most of the time.
		// These views should be moved to hardware layers in onStateTransitionStart for relevant transitions and moved off
		// of hardware layers in onStateTransitionEnd.
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

	public void clearSelection() {
		if (Ui.isAdded(mFlightLegsFrag)) {
			mFlightLegsFrag.clearSelection();
		}
	}

	/*
	 * Returns the base/default flights state at any point in time.
	 * Use it to either reset the state or to get the state to begin with.
	 */
	public ResultsFlightsState getBaseState() {
		if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			return ResultsFlightsState.NO_FLIGHTS_DROPDOWN_POS;
		}
		else if (!PointOfSale.getPointOfSale().supports(LineOfBusiness.FLIGHTS)) {
			return ResultsFlightsState.NO_FLIGHTS_POS;
		}
		else if (TextUtils.isEmpty(Sp.getParams().getOriginAirportCode()) || isOriginDestinationSame()) {
			return ResultsFlightsState.MISSING_ORIGIN;
		}
		else if (Sp.getParams().getStartDate() == null) {
			return ResultsFlightsState.MISSING_STARTDATE;
		}
		else if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null && Db
			.getFlightSearch().getSearchResponse().hasErrors()) {
			return ResultsFlightsState.SEARCH_ERROR;
		}
		else if (mFlightsStateManager != null && mFlightsStateManager.getState() == ResultsFlightsState.ZERO_RESULT) {
			return ResultsFlightsState.ZERO_RESULT;
		}
		else if (mFlightsStateManager != null && mFlightsStateManager.getState() == ResultsFlightsState.INVALID_START_DATE) {
			return ResultsFlightsState.INVALID_START_DATE;
		}
		else if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null) {
			return ResultsFlightsState.FLIGHT_LIST_DOWN;
		}
		else {
			return ResultsFlightsState.LOADING;
		}

	}

	/*
	 * Helper method to check if destination and origin airport are the same.
	 */
	public boolean isOriginDestinationSame() {
		Location destinationLoc = Sp.getParams().getDestinationLocation(true);
		if (destinationLoc != null) {
			return Sp.getParams().getOriginAirportCode().equals(destinationLoc.getDestinationId());
		}
		return false;
	}

	/*
	 * Helper method to check if it's time to start the flight search.
	 */
	public boolean readyToSearch() {
		return Sp.getParams().hasEnoughInfoForFlightsSearch() && PointOfSale.getPointOfSale().isFlightSearchEnabledTablet();
	}
	/*
	 * NEW SEARCH PARAMS
	 */

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		if (mFlightsStateManager.getState() != ResultsFlightsState.LOADING && readyToSearch()) {
			setFlightsState(ResultsFlightsState.LOADING, false);
		}
		else {
			mFlightsStateManager.setState(getBaseState(), true);
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
		else if (tag == FTAG_FLIGHT_SEARCH_DOWNLOAD) {
			frag = FlightSearchDownloadFragment.newInstance(Sp.getParams().toFlightSearchParams());
		}
		else if (tag == FTAG_FLIGHT_LOADING_INDICATOR) {
			frag = ResultsListLoadingFragment.newInstance(LineOfBusiness.FLIGHTS);
		}
		else if (tag == FTAG_FLIGHT_LEGS_CHOOSER) {
			frag = ResultsRecursiveFlightLegsFragment.newInstance(0);
		}
		else if (tag == FTAG_FLIGHT_SEARCH_ERROR) {
			frag = ResultsListSearchErrorFragment.newInstance();
		}

		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		switch (tag) {
		case FTAG_FLIGHT_MAP:
			updateMapFragSizes((ResultsFlightMapFragment) frag);
			break;
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
		case MISSING_ORIGIN:
		case NO_FLIGHTS_DROPDOWN_POS:
			touchableViews.add(mSearchErrorC);
			break;
		case LOADING:
			touchableViews.add(mFlightLegsC);
			break;
		}

		for (View vg : mVisibilityControlledViews) {
			if (vg instanceof TouchableFrameLayout) {
				if (touchableViews.contains(vg)) {
					((TouchableFrameLayout) vg).setBlockNewEventsEnabled(false);
				}
				else {
					((TouchableFrameLayout) vg).setBlockNewEventsEnabled(true);
				}
			}
		}
	}

	public void setListTouchable(boolean touchable) {
		if (mFlightLegsFrag != null) {
			mFlightLegsFrag.setListTouchable(touchable);
		}
	}

	private void setVisibilityState(ResultsFlightsState flightsState) {
		ArrayList<View> visibleViews = new ArrayList<View>();

		if (flightsState.isShowMessageState()) {
			visibleViews.add(mSearchErrorC);
			visibleViews.add(mLoadingC);
		}

		switch (flightsState) {
		case LOADING: {
			visibleViews.add(mLoadingC);
			visibleViews.add(mFlightLegsC);
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
			visibleViews.add(mFlightMapC);
			visibleViews.add(mFlightLegsC);
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
		FragmentTransaction transaction = manager.beginTransaction();

		boolean loadingAvailable = false;
		boolean searchErrorAvailable = true;
		boolean flightMapAvailable = true;

		if (flightsState == ResultsFlightsState.LOADING) {
			loadingAvailable = true;
			flightMapAvailable = false;
		}

		if (flightsState.isShowMessageState()) {
			loadingAvailable = false;
			flightMapAvailable = false;
		}

		mFlightMapFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			flightMapAvailable, FTAG_FLIGHT_MAP,
			manager, transaction, this, R.id.bg_flight_map, false);
		mFlightSearchDownloadFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_FLIGHT_SEARCH_DOWNLOAD, manager, transaction, this, 0, true);
		mLoadingGuiFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			loadingAvailable,
			FTAG_FLIGHT_LOADING_INDICATOR, manager, transaction, this, R.id.loading_container, true);
		mFlightLegsFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
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

			if (stateTwo == ResultsState.OVERVIEW) {
				mRootC.setVisibility(View.VISIBLE);
				mRootC.setAlpha(1f);
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
			// This is done because flights controller sits on top of hotels
			// controller in the ResultsActivity.
			if (state == ResultsState.HOTELS) {
				mRootC.setVisibility(View.GONE);
				mRootC.setAlpha(0f);
			}
			else {
				mRootC.setVisibility(View.VISIBLE);
				mRootC.setAlpha(1f);
			}

			if (state != ResultsState.FLIGHTS) {
				setFlightsState(getBaseState(), false);
			}
			else {
				// We are in flights mode
				if (mFlightsStateManager.hasState()
					&& mFlightsStateManager.getState() == ResultsFlightsState.FLIGHT_LIST_DOWN) {
					// If we have a state, and that state is DOWN, lets go up
					setFlightsState(ResultsFlightsState.CHOOSING_FLIGHT, false);
				}
				else {
					// The activity is still telling us something, so we better refresh our state.
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
			if (isLandscape) {
				mGrid.setDimensions(totalWidth, totalHeight);

				mGrid.setGridSize(3, 5);

				//The top row matches the height of the actionbar
				mGrid.setRowSize(0, getActivity().getActionBar().getHeight());

				//The bottom row
				mGrid.setRowPercentage(2, getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1));

				//These columns are just the spacers between content columns
				int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
				mGrid.setColumnSize(1, spacerSize);
				mGrid.setColumnSize(3, spacerSize);

				//Horizontal alignment
				mGrid.setContainerToColumnSpan(mFlightMapC, 0, 4);
				mGrid.setContainerToColumn(mLoadingC, 2);
				mGrid.setContainerToColumn(mSearchErrorC, 2);
				mGrid.setContainerToColumnSpan(mFlightLegsC, 0, 4);
				mGrid.setContainerToColumn(mRouteDescriptionC, 4);

				//Special cases
				mGrid.setContainerToRowSpan(mFlightMapC, 0, 2);
				mGrid.setContainerToRow(mLoadingC, 2);
				mGrid.setContainerToRow(mSearchErrorC, 2);
				mGrid.setContainerToRow(mRouteDescriptionC, 2);

				//Frag stuff
				updateMapFragSizes(mFlightMapFrag);
			}
			else {
				mGrid.setDimensions(totalWidth, totalHeight);

				mGrid.setGridSize(3, 3);

				//The top row matches the height of the actionbar
				mGrid.setRowSize(0, getActivity().getActionBar().getHeight());

				//The bottom row
				mGrid.setRowPercentage(2, getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1));

				//These columns are just the spacers between content columns
				int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
				mGrid.setColumnSize(1, spacerSize);

				//Horizontal alignment
				mGrid.setContainerToColumnSpan(mFlightMapC, 0, 2);
				mGrid.setContainerToColumn(mLoadingC, 2);
				mGrid.setContainerToColumn(mSearchErrorC, 2);
				mGrid.setContainerToColumnSpan(mFlightLegsC, 0, 2);

				//Special cases
				mGrid.setContainerToRowSpan(mFlightMapC, 0, 2);
				mGrid.setContainerToRow(mLoadingC, 2);
				mGrid.setContainerToRow(mSearchErrorC, 2);

				//Frag stuff
				updateMapFragSizes(mFlightMapFrag);
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
				// If we are in the middle of state transition, just reverse it
				setFlightsState(state, true);
				return true;
			}
			else {
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
	private StateListenerCollection<ResultsFlightsState> mFlightsStateListeners = new StateListenerCollection<ResultsFlightsState>();

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

		// If we have arrived at the ADDING_FLIGHT_TO_TRIP state, we want to immediately move to the list down state.
		// We do this here so that finalize has been called on all listeners before moving on.
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
				mLoadingC.setAlpha(1.0f);
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
			else if (stateOne == ResultsFlightsState.LOADING && stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mLoadingC.setAlpha(1.0f - percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.CHOOSING_FLIGHT
				|| stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mCouldShowInfantPrompt = true;
			}
			else if (stateOne == ResultsFlightsState.LOADING && stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mLoadingC.setAlpha(0.0f);
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			setVisibilityState(state);
			setTouchState(state);
			setFragmentState(state);

			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mLoadingC.setAlpha(1.0f);
			}

			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN
				|| state.isShowMessageState()) {
				mFlightMapC.setAlpha(0f);
			}
			else {
				mFlightMapC.setAlpha(1f);
			}

			if (!state.isFlightListState()) {
				mFlightMapC.setVisibility(View.GONE);
			}

			// Make sure we are loading using the most recent params
			if (Ui.isAdded(mFlightSearchDownloadFrag) && state == ResultsFlightsState.LOADING && readyToSearch()) {
				importSearchParams();
				mFlightSearchDownloadFrag.startOrResumeForParams(Db.getFlightSearch().getSearchParams());
			}

			if (mFlightLegsFrag != null && state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				if (mFlightLegsFrag.getState() != ResultsFlightLegState.LIST_DOWN) {
					mFlightLegsFrag.setState(ResultsFlightLegState.LIST_DOWN, false);
				}
			}

			if (state.isShowMessageState()) {
				if (mSearchErrorFrag.isAdded()) {
					mSearchErrorFrag.setState(state);
				}
				else {
					mSearchErrorFrag.setDefaultState(state);
				}
			}

			if (state == ResultsFlightsState.CHOOSING_FLIGHT) {
				if (mCouldShowInfantPrompt) {
					mCouldShowInfantPrompt = false;
					popInfantPromptIfNeeded();
				}
				if (mFlightLegsFrag.isFirstLeg()) {
					OmnitureTracking.trackPageLoadFlightSearchResults(0);
					AdTracker.trackPageLoadFlightSearchResults(0);
				}
			}
		}
	};

	// Route description

	private void bindRouteDescriptionText(boolean forward) {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		if (params != null && params.isFilled()) {
			String firstCity = forward ? params.getDepartureLocation().getCity() : params.getArrivalLocation().getCity();
			String secondCity = forward ? params.getArrivalLocation().getCity() : params.getDepartureLocation().getCity();

			if (TextUtils.isEmpty(firstCity) || TextUtils.isEmpty(secondCity)) {
				return;
			}

			String routeDescription = getString(R.string.flight_cities_TEMPLATE, firstCity, secondCity).toUpperCase(Locale.getDefault());

			SpannableStringBuilder ssb = new SpannableStringBuilder(routeDescription);

			int endOfOriginText = firstCity.length();
			int startOfDestinationText = routeDescription.length() - secondCity.length();

			ssb.setSpan(FontCache.getSpan(FontCache.Font.ROBOTO_LIGHT), endOfOriginText, startOfDestinationText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			TextView tv = Ui.findView(mRouteDescriptionC, R.id.route_description_text);
			int strokeBorder = (int) getResources().getDimension(R.dimen.tablet_flight_route_desc_stroke_size);
			tv.setStrokeColor(Color.parseColor("#687887"));
			tv.setStrokeWidth(strokeBorder);
			tv.setText(ssb, android.widget.TextView.BufferType.SPANNABLE);
		}
	}

	// Infants

	private void popInfantPromptIfNeeded() {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		if (params.hasInfants() && !params.hasMoreInfantsThanAvailableLaps()) {
			mInfantFrag = Ui.findSupportFragment(this, FTAG_FLIGHT_INFANT_CHOOSER);
			if (mInfantFrag == null) {
				mInfantFrag = FlightInfantChooserDialogFragment.newInstance();
			}
			if (!mInfantFrag.isAdded()) {
				mInfantFrag.show(getFragmentManager(), "infantChooser");
			}
			OmnitureTracking.trackFlightInfantDialog();
		}
	}

	@Subscribe
	public void onSimpleDialogCallbackClick(Events.SimpleCallBackDialogOnClick click) {
		if (click.callBackId == SimpleCallbackDialogFragment.CODE_TABLET_FLIGHTS_INFANT_CHOOSER) {

			boolean newLapPref = !Db.getFlightSearch().getSearchParams().getInfantSeatingInLap();
			Sp.getParams().setInfantsInLaps(newLapPref);
			Sp.reportSpUpdate();
			OmnitureTracking.trackTabletSearchResultsPageLoad(Sp.getParams());
		}
	}

	@Subscribe
	public void onSimpleCallbackCancel(Events.SimpleCallBackDialogOnCancel cancel) {
		if (cancel.callBackId == SimpleCallbackDialogFragment.CODE_TABLET_FLIGHTS_INFANT_CHOOSER) {
			popInfantPromptIfNeeded();
		}
	}

	@Subscribe
	public void onFlightSearchResponseAvailable(Events.FlightSearchResponseAvailable event) {
		FlightSearchResponse flightResponse = event.response;

		if (flightResponse != null) {
			Db.addAirlineNames(flightResponse.getAirlineNames());
		}
		else {
			// If we have a null response, the client should show the SEARCH_ERROR state.
			// There is too much logic surrounding whether the response is null or not
			// already, so the best solution is to add an empty response with an error.
			flightResponse = new FlightSearchResponse();
			ServerError serverError = new ServerError();
			serverError.setCode("NULL_RESPONSE");
			flightResponse.addError(serverError);
		}

		if (!FlightUtils.dateRangeSupportsFlightSearch(getActivity())) {
			setFlightsState(ResultsFlightsState.INVALID_START_DATE, false);
			return;
		}
		Db.getFlightSearch().setSearchResponse(flightResponse);

		boolean isBadResponse = flightResponse.hasErrors();
		boolean isZeroResults = flightResponse.getTripCount() == 0;

		if (isBadResponse) {
			setFlightsState(ResultsFlightsState.SEARCH_ERROR, false);
		}
		else if (isZeroResults) {
			setFlightsState(ResultsFlightsState.ZERO_RESULT, false);
		}
		else {
			mFlightLegsFrag.resetQuery();
			setFlightsState(ResultsFlightsState.FLIGHT_LIST_DOWN, true);
			bindRouteDescriptionText(true);
			AdTracker.trackFlightSearch();
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
					bindRouteDescriptionText(false);
				}
			}
			else if (state == ResultsFlightLegState.LIST_DOWN || state == ResultsFlightLegState.FILTERS
				|| state == ResultsFlightLegState.DETAILS) {
				if (mFlightMapFrag != null && mFlightMapFrag.isAdded() && mFlightMapFrag.isMapGenerated()) {
					mFlightMapFrag.forward();
					bindRouteDescriptionText(true);
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

	public boolean listHasTouch() {
		return mFlightLegsFrag.listHasTouch();
	}

	public boolean listIsDisplaced() {
		return mFlightLegsFrag.listIsDisplaced();
	}
}
