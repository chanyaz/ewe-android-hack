package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.enums.ResultsFlightLegState;
import com.expedia.bookings.enums.ResultsFlightsListState;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsFlightLegSelected;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.interfaces.ISiblingListTouchListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FruitList;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;

/**
 * ResultsRecursiveFlightLegsFragment
 * <p/>
 * This fragment allows us to select N flight legs, by providing us the means to select
 * a single flight leg, and attaching another instance of itself to select a future leg,
 * and so on.
 * <p/>
 * This fragment is architected this way in order to both allow N flight legs, AND to work
 * with our StateProvider/Manager/... system. Where a state represents a definitive ui state.
 */
public class ResultsRecursiveFlightLegsFragment extends Fragment implements IStateProvider<ResultsFlightLegState>,
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider, IBackManageable, IResultsFlightLegSelected,
	IResultsFlightSelectedListener, ResultsFlightListFragment.IFlightListHeaderClickListener,
	IAcceptingListenersListener {

	//State
	private static final String INSTANCE_STATE = "INSTANCE_STATE";
	private static final String INSTANCE_LEG_NUMBER = "INSTANCE_LEG_NUMBER";

	//Settings
	private static final float DETAILS_MARGIN_PERCENTAGE = 0.1f;
	private static final float DETAILS_MARGIN_PORTRAIT_PERCENTAGE = 0.08f;

	//Fragment tags
	private static final String FTAG_DETAILS = "FTAG_DETAILS";
	private static final String FTAG_FILTERS = "FTAG_FILTERS";
	private static final String FTAG_LIST = "FTAG_LIST";
	private static final String FTAG_NEXT_LEG = "FTAG_NEXT_LEG";

	//Fragments
	private ResultsFlightListFragment mListFrag;
	private ResultsFlightFiltersFragment mFilterFrag;
	private ResultsFlightDetailsFragment mDetailsFrag;
	private ResultsRecursiveFlightLegsFragment mNextLegFrag;

	//Containers
	private ArrayList<ViewGroup> mContainers = new ArrayList<ViewGroup>();
	private TouchableFrameLayout mDetailsC;
	private TouchableFrameLayout mFiltersC;
	private RelativeLayout mListColumnC;
	private TouchableFrameLayout mListC;
	private TouchableFrameLayout mNextLegC;
	private TouchableFrameLayout mLastLegC;
	private TextView mLastLegHeader;

	//Views
	private FlightLegSummarySectionTablet mLastFlightRow;

	//Other
	private int mLegNumber;
	private GridManager mGrid = new GridManager();
	private StateManager<ResultsFlightLegState> mStateManager;
	private StateListenerCollection<ResultsFlightLegState> mStateListeners;

	private IResultsFlightLegSelected mParentLegSelectedListener;
	private IResultsFlightSelectedListener mParentFlightSelectedListener;

	private Rect mAddToTripAnimRect = new Rect();

	private boolean mListHasTouch = false;
	private ISiblingListTouchListener mListener;

	public static ResultsRecursiveFlightLegsFragment newInstance(int legNumber) {
		ResultsRecursiveFlightLegsFragment frag = new ResultsRecursiveFlightLegsFragment();
		Bundle args = new Bundle();
		args.putInt(INSTANCE_LEG_NUMBER, legNumber);
		frag.setArguments(args);
		frag.initLegNumber(legNumber);
		return frag;
	}

	private void initLegNumber(int legNumber) {
		mLegNumber = legNumber;
		ResultsFlightLegState defaultState = getBaseState();
		mStateManager = new StateManager<ResultsFlightLegState>(defaultState, this);
		mStateListeners = new StateListenerCollection<ResultsFlightLegState>();
	}

	/**
	 * LIFE CYCLE
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			int legNumber = savedInstanceState.getInt(INSTANCE_LEG_NUMBER);
			initLegNumber(legNumber);
			mStateManager.setDefaultState(ResultsFlightLegState.valueOf(savedInstanceState.getString(INSTANCE_STATE)));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			FragmentManager manager = getChildFragmentManager();
			mListFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_LIST);
			mFilterFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_FILTERS);
			mDetailsFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_DETAILS);
			mNextLegFrag = FragmentAvailabilityUtils.getFrag(manager, FTAG_NEXT_LEG);
		}

		View view = inflater.inflate(R.layout.fragment_results_recursive_flight_legs, null, false);

		mDetailsC = Ui.findView(view, R.id.details_container);
		mFiltersC = Ui.findView(view, R.id.filters_container);
		mListColumnC = Ui.findView(view, R.id.list_column_container);
		mListC = Ui.findView(view, R.id.list_container);
		mNextLegC = Ui.findView(view, R.id.next_leg_container);
		mLastLegC = Ui.findView(view, R.id.last_flight_container);
		mLastLegHeader = Ui.findView(mLastLegC, R.id.last_flight_header);
		mLastFlightRow = Ui.findView(view, R.id.last_flight_row);

		mContainers.add(mDetailsC);
		mContainers.add(mFiltersC);
		mContainers.add(mNextLegC);
		mContainers.add(mListColumnC);

		if (!isFirstLeg()) {
			showLastLeg();
		}

		//We just cant have people mashing the list
		mListC.setPreventMashing(true, 200);

		//Always listen to the local State provider
		registerStateListener(new StateListenerLogger<ResultsFlightLegState>(), false);
		registerStateListener(mStateListener, false);
		mListener = (ISiblingListTouchListener) getActivity();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);

		mParentLegSelectedListener = Ui.findFragmentListener(this, IResultsFlightLegSelected.class, false);
		mParentFlightSelectedListener = Ui.findFragmentListener(this, IResultsFlightSelectedListener.class, false);

		if (isFirstLeg()) {
			mResultsFlightsStateListener.registerWithProvider(this, true);
		}
		else {
			mParentLegStateListener.registerWithProvider(this, false);
		}

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

		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
		mParentLegStateListener.unregisterWithProvider(this);
		mResultsFlightsStateListener.unregisterWithProvider(this);

		mParentLegSelectedListener = null;
		mParentFlightSelectedListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(INSTANCE_LEG_NUMBER, mLegNumber);
		outState.putString(INSTANCE_STATE, mStateManager.getState().name());
	}

	/**
	 * General  methods
	 */

	public void resetQuery() {
		if (mListFrag != null && mListFrag.isAdded()) {
			mListFrag.resetQuery();
		}
		else {
			Db.getFlightSearch().setSelectedLeg(mLegNumber, null);
			Db.getFlightSearch().clearQuery(mLegNumber);
		}
		if (mFilterFrag != null && mFilterFrag.isAdded()) {
			mFilterFrag.onFilterChanged();
		}
		if (mDetailsFrag != null && mDetailsFrag.isAdded()) {
			//TODO: Clear details state
		}
		if (mNextLegFrag != null && mNextLegFrag.isAdded()) {
			mNextLegFrag.resetQuery();
		}
	}

	public ResultsFlightLegState getBaseState() {
		return isFirstLeg() ? ResultsFlightLegState.LIST_DOWN : ResultsFlightLegState.FILTERS;
	}

	public boolean hasValidDataForDetails() {
		return Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedLegs() != null
			&& Db.getFlightSearch().getSelectedLegs().length > mLegNumber
			&& Db.getFlightSearch().getSelectedLegs()[mLegNumber] != null;
	}

	public boolean isLastLeg() {
		return Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedLegs() != null
			&& Db.getFlightSearch().getSelectedLegs().length == (mLegNumber + 1);
	}

	public boolean isFirstLeg() {
		return mLegNumber == 0;
	}

	public void setListTouchable(boolean touchable) {
		if (mListC != null) {
			mListC.setBlockNewEventsEnabled(!touchable);
		}
	}

	/*
	 * IResultsFlightSelectedListener
	 */

	@Override
	public void onFlightSelected(int legNumber) {
		Log.d("onFlightSelected mLegNumber:" + mLegNumber + " legNumber:" + legNumber);
		if (legNumber == mLegNumber) {
			if (mStateManager.getState() == ResultsFlightLegState.LIST_DOWN) {
				mStateManager.animateThroughStates(ResultsFlightLegState.FILTERS, ResultsFlightLegState.DETAILS);
			}
			else {
				setState(ResultsFlightLegState.DETAILS, true);
			}
			if (mNextLegFrag != null) {
				mNextLegFrag.resetQuery();
			}
		}
		if (mParentFlightSelectedListener != null) {
			mParentFlightSelectedListener.onFlightSelected(legNumber);
		}
		OmnitureTracking.trackPageLoadFlightSearchResultsDetails(legNumber);
	}

	/*
	 * IResultsFlightSelectedListener
	 */

	@Override
	public void onTripAdded(int legNumber) {
		Log.d("onTripAdded mLegNumber:" + mLegNumber + " legNumber:" + legNumber);
		if (legNumber == mLegNumber) {
			if (isLastLeg()) {
				AdTracker.trackTabletFlightViewContent();
				// Set the Db properly
				Db.getTripBucket().clearFlight();
				Db.getTripBucket().add(Db.getFlightSearch());
				Db.saveTripBucket(getActivity());

				// Reset the query so we can re-select a new flight for trip bucket
				resetQuery();

				// Change state
				setState(ResultsFlightLegState.ADDING_TO_TRIP, false);
			}
			else {
				if (mNextLegFrag != null) {
					mNextLegFrag.resetQuery();
					mNextLegFrag.setState(ResultsFlightLegState.FILTERS, false);
				}
				setState(ResultsFlightLegState.LATER_LEG, true);
				AdTracker.trackPageLoadFlightSearchResults(mLegNumber + 1);
				OmnitureTracking.trackPageLoadFlightSearchResults(mLegNumber + 1);
			}
		}
		if (mParentLegSelectedListener != null) {
			mParentLegSelectedListener.onTripAdded(legNumber);
		}
	}

	/**
	 * LIST STATE LISTENER
	 */

	private StateListenerHelper<ResultsFlightsListState> mListStateListener = new StateListenerHelper<ResultsFlightsListState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo) {
			startStateTransition(translate(stateOne), translate(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo,
			float percentage) {
			updateStateTransition(translate(stateOne), translate(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo) {
			endStateTransition(translate(stateOne), translate(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsFlightsListState state) {
			setState(translate(state), false);
		}

		private ResultsFlightLegState translate(ResultsFlightsListState state) {
			if (state == ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM) {
				if (mLegNumber == 0) {
					return ResultsFlightLegState.LIST_DOWN;
				}
				else {
					return ResultsFlightLegState.FILTERS;
				}
			}
			else {
				if (mStateManager.getState() == ResultsFlightLegState.LIST_DOWN) {
					return ResultsFlightLegState.FILTERS;
				}
				else {
					return mStateManager.getState();
				}
			}
		}
	};

	/**
	 * LISTENER FOR THIS CHILD FRAGMENT PROVIDER STATE - ResultsFlightLegState
	 */

	private StateListenerHelper<ResultsFlightLegState> mNextLegStateListener = new StateListenerHelper<ResultsFlightLegState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (getState() == ResultsFlightLegState.LATER_LEG && stateOne == ResultsFlightLegState.DETAILS
				&& stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				startStateTransition(ResultsFlightLegState.LATER_LEG, ResultsFlightLegState.ADDING_TO_TRIP);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
			float percentage) {
			if (getState() == ResultsFlightLegState.LATER_LEG && stateOne == ResultsFlightLegState.DETAILS
				&& stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				updateStateTransition(ResultsFlightLegState.LATER_LEG, ResultsFlightLegState.ADDING_TO_TRIP,
					percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (getState() == ResultsFlightLegState.LATER_LEG && stateOne == ResultsFlightLegState.DETAILS
				&& stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				endStateTransition(ResultsFlightLegState.LATER_LEG, ResultsFlightLegState.ADDING_TO_TRIP);
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightLegState state) {
			if (getState() == ResultsFlightLegState.LATER_LEG && state == ResultsFlightLegState.ADDING_TO_TRIP) {
				setState(ResultsFlightLegState.ADDING_TO_TRIP, false);
			}
		}
	};

	/**
	 * LISTENER FOR PARENT LEG - ResultsFlightLegState
	 */

	private StateListenerHelper<ResultsFlightLegState> mParentLegStateListener = new StateListenerHelper<ResultsFlightLegState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
			float percentage) {

		}

		@Override
		public void onStateTransitionEnd(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsFlightLegState state) {
			if (state == ResultsFlightLegState.LIST_DOWN || state == ResultsFlightLegState.FILTERS
				|| state == ResultsFlightLegState.DETAILS) {
				setState(getBaseState(), false);
			}
			else if (state == ResultsFlightLegState.LATER_LEG) {
				if (!mStateManager.hasState()) {
					setState(getState(), false);
				}
			}
		}
	};

	/**
	 * PARENT FLIGHT STATE LISTENER
	 * <p/>
	 * NOTE: This is only listening if leg == 0
	 */

	private StateListenerHelper<ResultsFlightsState> mResultsFlightsStateListener = new StateListenerHelper<ResultsFlightsState>() {

		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				startStateTransition(ResultsFlightLegState.ADDING_TO_TRIP, ResultsFlightLegState.LIST_DOWN);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				updateStateTransition(ResultsFlightLegState.ADDING_TO_TRIP, ResultsFlightLegState.LIST_DOWN,
					percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				endStateTransition(ResultsFlightLegState.ADDING_TO_TRIP, ResultsFlightLegState.LIST_DOWN);
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {

			ResultsFlightsState flightsState = state;
			ResultsFlightLegState legState = getState();

			if (flightsState == ResultsFlightsState.CHOOSING_FLIGHT) {
				if (legState == ResultsFlightLegState.LIST_DOWN) {
					setState(ResultsFlightLegState.FILTERS, false);
				}
				else if (!mStateManager.hasState()) {
					setState(legState, false);
				}
			}
			else if (flightsState == ResultsFlightsState.LOADING
				|| flightsState == ResultsFlightsState.SEARCH_ERROR
				|| flightsState == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				setState(getBaseState(), false);
			}

			// The FruitList can get into bad state when transitioning back to the LOADING state from the
			// CHOOSING_FLIGHT state. We update the book-keeping in FruitList here as a fail-safe.
			if (flightsState == ResultsFlightsState.LOADING) {
				mListFrag.setLastReportedTouchPercentage(1f);
			}
		}
	};

	/**
	 * LISTENER FOR THIS OWN FRAGMENTS PROVIDER STATE - ResultsFlightLegState
	 */

	private StateListenerHelper<ResultsFlightLegState> mStateListener = new StateListenerHelper<ResultsFlightLegState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (stateOne == ResultsFlightLegState.LIST_DOWN && stateTwo == ResultsFlightLegState.FILTERS) {
				showFiltersAnimPrep(0f);
			}
			else if (stateOne == ResultsFlightLegState.FILTERS && stateTwo == ResultsFlightLegState.LIST_DOWN) {
				showFiltersAnimPrep(1f);
			}
			else if (stateOne == ResultsFlightLegState.FILTERS && stateTwo == ResultsFlightLegState.DETAILS) {
				showDetailsAnimPrep(0f);
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.FILTERS) {
				showDetailsAnimPrep(1f);
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.DETAILS) {
				// We want the bind to happen as early as possible when viewing different flights on details mode
				mDetailsFrag.bindWithDb();
				mDetailsFrag.scrollToTop();
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.LATER_LEG) {
				showNextLegAnimPrep(0f);
			}
			else if (stateOne == ResultsFlightLegState.LATER_LEG && stateTwo == ResultsFlightLegState.DETAILS) {
				showNextLegAnimPrep(1f);
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				showAddToTripAnimPrep();
			}
			else if (stateOne == ResultsFlightLegState.ADDING_TO_TRIP && stateTwo == ResultsFlightLegState.LIST_DOWN) {
				showAddingBackToDownAnimPrep(0f);
			}
			else if (stateOne == ResultsFlightLegState.LATER_LEG && stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				showAddToTripAnimPrep();
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
			float percentage) {
			if (stateOne == ResultsFlightLegState.LIST_DOWN && stateTwo == ResultsFlightLegState.FILTERS) {
				showFiltersPercentage(percentage);
			}
			else if (stateOne == ResultsFlightLegState.FILTERS && stateTwo == ResultsFlightLegState.LIST_DOWN) {
				showFiltersPercentage(1f - percentage);
			}
			else if (stateOne == ResultsFlightLegState.FILTERS && stateTwo == ResultsFlightLegState.DETAILS) {
				showDetailsAnimUpdate(percentage);
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.FILTERS) {
				showDetailsAnimUpdate(1f - percentage);
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.LATER_LEG) {
				showNextLegPercentage(percentage);
			}
			else if (stateOne == ResultsFlightLegState.LATER_LEG && stateTwo == ResultsFlightLegState.DETAILS) {
				showNextLegPercentage(1f - percentage);
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				showAddToTripPercentage(percentage);
			}
			else if (stateOne == ResultsFlightLegState.ADDING_TO_TRIP && stateTwo == ResultsFlightLegState.LIST_DOWN) {
				showAddingBackToDownPercentage(percentage);
			}
			else if (stateOne == ResultsFlightLegState.LATER_LEG && stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				showAddToTripPercentage(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
			if (stateOne == ResultsFlightLegState.LIST_DOWN && stateTwo == ResultsFlightLegState.FILTERS) {
				showFiltersAnimCleanUp();
			}
			else if (stateOne == ResultsFlightLegState.FILTERS && stateTwo == ResultsFlightLegState.LIST_DOWN) {
				showFiltersAnimCleanUp();
			}
			else if (stateOne == ResultsFlightLegState.FILTERS && stateTwo == ResultsFlightLegState.DETAILS) {
				showDetailsAnimCleanUp();
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.FILTERS) {
				showDetailsAnimCleanUp();
				clearSelection();
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.LATER_LEG) {
				showNextLegAnimCleanup();
			}
			else if (stateOne == ResultsFlightLegState.LATER_LEG && stateTwo == ResultsFlightLegState.DETAILS) {
				showNextLegAnimCleanup();
			}
			else if (stateOne == ResultsFlightLegState.DETAILS && stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				showAddToTripAnimCleanUp();
			}
			else if (stateOne == ResultsFlightLegState.ADDING_TO_TRIP && stateTwo == ResultsFlightLegState.LIST_DOWN) {
				showAddingBackToDownAnimCleanUp();
			}
			else if (stateOne == ResultsFlightLegState.LATER_LEG && stateTwo == ResultsFlightLegState.ADDING_TO_TRIP) {
				showAddToTripAnimCleanUp();
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightLegState state) {
			updateVisibilitiesForState(state);
			updateFragmentsForState(state);
			updateListForState(state);

			//List and filters state
			if (state == ResultsFlightLegState.LIST_DOWN) {
				mFiltersC.setAlpha(0f);
				if (mListFrag != null && mListFrag.hasList()) {
					mFiltersC.setTranslationY(mListFrag.getMaxDistanceFromTop());
				}
			}
			else {
				mFiltersC.setAlpha(1f);
				mFiltersC.setTranslationY(0f);
			}

			if (mListFrag != null && mListFrag.hasList()) {
				mListFrag.setListHeaderExpansionPercentage(state.shouldShowExpandedHeader() ? 1f : 0f);
			}

			//Details state
			if (state == ResultsFlightLegState.DETAILS) {
				showDetailsAnimUpdate(1f);
				mDetailsFrag.bindWithDb();
			}
			else {
				showDetailsAnimUpdate(0f);
			}

			//Next Leg State
			if (state == ResultsFlightLegState.LATER_LEG) {
				showNextLegPercentage(1f);
				if (mNextLegFrag.hasValidDataForDetails()) {
					mNextLegFrag.setState(ResultsFlightLegState.DETAILS, false);
				}
				else {
					mNextLegFrag.setState(ResultsFlightLegState.FILTERS, false);
				}
			}
			else if (mNextLegFrag != null && state != ResultsFlightLegState.ADDING_TO_TRIP) {
				//If we are showing, the next leg should always be in the filters state.
				mNextLegFrag.setState(ResultsFlightLegState.FILTERS, false);
			}

			//Last leg state
			if (mLegNumber > 0 && mLastFlightRow != null) {
				int lastFlightIndex = mLegNumber - 1;
				if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedLegs() != null
					&& Db.getFlightSearch().getSelectedLegs().length > lastFlightIndex
					&& Db.getFlightSearch().getSelectedLegs()[lastFlightIndex] != null
					&& Db.getFlightSearch().getSelectedLegs()[lastFlightIndex].getFlightTrip() != null) {
					mLastFlightRow.bind(Db.getFlightSearch(), lastFlightIndex);
				}
			}
		}
	};


	/**
	 * State Helpers
	 */

	public void setState(ResultsFlightLegState state, boolean animate) {
		//If we are transitioning we typically dont want to allow state changes, but
		//if we are going back to the original state, we allow animation reversals
		if (!mStateListeners.isTransitioning() || state == mStateManager.getState()) {
			mStateManager.setState(state, animate);
		}
		else {
			Log.e("setState may not be called while we are transitioning");
		}
	}

	public ResultsFlightLegState getState() {
		return mStateManager.getState();
	}

	/*
	SHOW & HIDE DETAILS ANIMATION HELPERS
	 */
	protected void showDetailsAnimPrep(float startPercentage) {
		if (mDetailsFrag != null) {
			if (hasValidDataForDetails()) {
				mDetailsFrag.bindWithDb();
				mDetailsFrag.scrollToTop();
			}

			int slideInDistance = mGrid.getColSpanWidth(1, 4);
			mDetailsFrag.setDetailsSlideInAnimationState(startPercentage, slideInDistance, true);
			mDetailsFrag.prepareSlideInAnimation();
			mDetailsFrag.setSlideInAnimationLayer(View.LAYER_TYPE_HARDWARE);
		}

		mFiltersC.setVisibility(View.VISIBLE);
		mListColumnC.setVisibility(View.VISIBLE);
		mDetailsC.setVisibility(View.VISIBLE);

		mFiltersC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mListColumnC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	protected void showDetailsAnimUpdate(float percentage) {
		if (percentage == 0) {
			mFiltersC.setTranslationX(0f);
			mListColumnC.setTranslationX(0f);
		}
		else {
			// for landscape we shift the the columns over by one, for portrait we need to shift by two
			int translateMultiplier = mGrid.isLandscape() ? -1 : -2;
			mFiltersC.setTranslationX(percentage * translateMultiplier * mGrid.getColRight(0));
			mListColumnC.setTranslationX(percentage * translateMultiplier * mGrid.getColLeft(2));
			if (mDetailsFrag != null) {
				int slideInDistance = mGrid.getColSpanWidth(1, 4);
				mDetailsFrag.setDetailsSlideInAnimationState(percentage, slideInDistance, true);
			}
		}
	}

	protected void showDetailsAnimCleanUp() {
		if (mDetailsFrag != null) {
			mDetailsFrag.finalizeSlideInPercentage();
			mDetailsFrag.setSlideInAnimationLayer(View.LAYER_TYPE_NONE);
		}
		mFiltersC.setLayerType(View.LAYER_TYPE_NONE, null);
		mListColumnC.setLayerType(View.LAYER_TYPE_NONE, null);
	}

	/*
	SHOW & HIDE NEXT LEG ANIMATION HELPERS
	 */

	protected Rect getColumnHeaderRect() {
		if (mLastFlightRow != null) {
			return ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mLastFlightRow);
		}
		return null;
	}

	protected void hideLastLeg() {
		if (mLastLegC != null) {
			mLastLegC.setVisibility(View.INVISIBLE);
		}
	}

	protected void showLastLeg() {
		if (mLastLegC != null) {
			mLastLegC.setVisibility(View.VISIBLE);
			String city = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCity();
			mLastLegHeader.setText(getString(R.string.your_selected_flight_to_x_TEMPLATE, city));
		}
	}

	protected void showNextLegAnimPrep(float startPercentage) {
		if (mNextLegFrag != null) {
			mNextLegFrag.hideLastLeg();
		}

		int listVis = mGrid.isLandscape() ? View.VISIBLE : View.INVISIBLE;
		mListColumnC.setVisibility(listVis);
		mDetailsC.setVisibility(View.VISIBLE);
		mNextLegC.setAlpha(startPercentage);
		mNextLegC.setVisibility(View.VISIBLE);

		//Rendering
		mListColumnC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mNextLegC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		if (mDetailsFrag != null) {
			mDetailsFrag.setDepartureTripSelectedAnimationLayer(View.LAYER_TYPE_HARDWARE);
			if (mNextLegFrag != null) {
				Rect animateTowardsRect = mNextLegFrag.getColumnHeaderRect();
				if (animateTowardsRect != null) {
					mDetailsFrag.prepareDepartureFlightSelectedAnimation(animateTowardsRect, false);
				}
			}
		}
	}

	protected void showNextLegPercentage(float percentage) {
		float nextLegTransX = (1f - percentage) * mGrid.getColSpanWidth(0, 1);
		float listTransX = (int) (-mGrid.getColLeft(2) + percentage
			* -mGrid.getColSpanWidth(0, 1));

		mNextLegC.setTranslationX(nextLegTransX);
		mNextLegC.setAlpha(percentage);
		mListColumnC.setTranslationX(listTransX);

		if (mDetailsFrag != null) {
			mDetailsFrag.setDepartureTripSelectedAnimationState(percentage, false);
		}
	}

	protected void showNextLegAnimCleanup() {
		mListColumnC.setLayerType(View.LAYER_TYPE_NONE, null);
		mNextLegC.setLayerType(View.LAYER_TYPE_NONE, null);
		if (mDetailsFrag != null) {
			mDetailsFrag.finalizeDepartureFlightSelectedAnimation();
			mDetailsFrag.setDepartureTripSelectedAnimationLayer(View.LAYER_TYPE_NONE);
		}
		if (mNextLegFrag != null) {
			mNextLegFrag.showLastLeg();
		}
	}

	/*
	LIST UP AND DOWN ANIM HELPERS
	 */

	protected void showFiltersAnimPrep(float startPercentage) {
		mFiltersC.setAlpha(startPercentage);
		mFiltersC.setVisibility(View.VISIBLE);
		mFiltersC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mListFrag.setListLockedToTop(false);
		mListFrag.setPercentage(1f - startPercentage, 0);
	}

	protected void showFiltersPercentage(float percentage) {
		mFiltersC.setAlpha(percentage);
		float filterPaneTopTranslation = (1f - percentage) * mListFrag.getMaxDistanceFromTop();
		mFiltersC.setTranslationY(filterPaneTopTranslation);
		mListFrag.setPercentage(1f - percentage, 0);
		mListFrag.setListHeaderExpansionPercentage(percentage);
	}

	protected void showFiltersAnimCleanUp() {
		mFiltersC.setLayerType(View.LAYER_TYPE_NONE, null);
	}

	/*
	LIST UP AND DOWN ANIM HELPERS
	 */

	protected void showAddToTripAnimPrep() {
		if (isLastLeg()) {
			mDetailsFrag.setAddToTripFromDetailsAnimationLayer(View.LAYER_TYPE_HARDWARE);
			mDetailsFrag.prepareAddToTripFromDetailsAnimation(mAddToTripAnimRect);
		}
		if (isFirstLeg()) {
			mListFrag.setListHeaderExpansionPercentage(0f);
		}
	}

	protected void showAddToTripPercentage(float percentage) {
		if (isLastLeg()) {
			mDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
			if (mGrid.isLandscape()) {
				float listTransX = (int) (-mGrid.getColLeft(2) + percentage
					* -mGrid.getColSpanWidth(0, 1));
				mListColumnC.setTranslationX(listTransX);
			}
			else {
				mListColumnC.setVisibility(View.INVISIBLE);
			}
		}
	}

	protected void showAddToTripAnimCleanUp() {
		if (isLastLeg()) {
			mDetailsFrag.setAddToTripFromDetailsAnimationLayer(View.LAYER_TYPE_NONE);
			mDetailsFrag.finalizeAddToTripFromDetailsAnimation();
		}
	}

	/*
	ADD_TO_TRIP back to OVERVIEW Anim helpers
	 */

	protected void showAddingBackToDownAnimPrep(float startPercentage) {
		if (isFirstLeg()) {
			mListColumnC.setTranslationX((1f - startPercentage) * -mGrid.getColSpanWidth(0, 3));
			mListColumnC.setTranslationY(0);
			mListColumnC.setVisibility(View.VISIBLE);
			mListColumnC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			mListFrag.setListLockedToTop(false);
			mListFrag.setPercentage(1f, 0);
		}
	}

	protected void showAddingBackToDownPercentage(float percentage) {
		if (isFirstLeg()) {
			mListColumnC.setTranslationX((1f - percentage) * -mGrid.getColSpanWidth(0, 3));
		}
	}

	protected void showAddingBackToDownAnimCleanUp() {
		if (isFirstLeg()) {
			mListColumnC.setLayerType(View.LAYER_TYPE_NONE, null);
		}
	}

	/*
	FINALIZE HELPERS
	 */

	protected void updateListForState(ResultsFlightLegState state) {
		if (mListFrag != null) {
			if (isFirstLeg()) {
				//We only care about list state for the first leg (as it is this list we use for the overview -> flights transition)

				//Dont let these changes throw us into a loop
				mListFrag.setListenerEnabled(mListStateListener, false);
				if (state != ResultsFlightLegState.LIST_DOWN && state != ResultsFlightLegState.FILTERS
					&& state != ResultsFlightLegState.ADDING_TO_TRIP) {
					if (mListFrag.hasList() && mListFrag.getPercentage() > 0) {
						mListFrag.setPercentage(0f, 0);
					}
					mListFrag.setListLockedToTop(true);
				}
				else {
					mListFrag.setListLockedToTop(false);

					if (mListFrag.hasList()) {
						if ((state == ResultsFlightLegState.ADDING_TO_TRIP || state == ResultsFlightLegState.LIST_DOWN)
							&& mListFrag.getPercentage() < 1) {
							mListFrag.setPercentage(1f, 0);
						}
						else if (state == ResultsFlightLegState.FILTERS && mListFrag.getPercentage() > 0) {
							mListFrag.setPercentage(0f, 0);
						}
					}
				}
				mListFrag.setListenerEnabled(mListStateListener, true);
			}
			else {
				//Other legs are always locked to the top.
				mListFrag.setListLockedToTop(true);
				if (mListFrag.hasList() && mListFrag.getPercentage() > 0) {
					mListFrag.setPercentage(0f, 0);
				}
				if (mListFrag.hasList()) {
					mListFrag.setListHeaderExpansionPercentage(1f);
				}

				//This is the previous leg row that should always be visible if we aren't leg == 0
				showLastLeg();
			}

			// Sort and Filter button
			mListFrag.setTopRightTextButtonVisibility(state == ResultsFlightLegState.DETAILS);
		}
	}

	protected void updateVisibilitiesForState(ResultsFlightLegState state) {
		ArrayList<ViewGroup> visibleViews = new ArrayList<ViewGroup>();
		switch (state) {
		case LIST_DOWN:
			visibleViews.add(mListColumnC);
			break;
		case FILTERS:
			visibleViews.add(mListColumnC);
			visibleViews.add(mFiltersC);
			break;
		case DETAILS:
			visibleViews.add(mListColumnC);
			visibleViews.add(mFiltersC);
			visibleViews.add(mDetailsC);
			break;
		case LATER_LEG:
			visibleViews.add(mNextLegC);
			break;
		case ADDING_TO_TRIP:
			if (!isLastLeg()) {
				visibleViews.add(mNextLegC);
			}
			if (isFirstLeg()) {
				visibleViews.add(mListColumnC);
			}
			break;
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

	protected void updateFragmentsForState(ResultsFlightLegState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean listAvail = false;
		boolean filterAvail = false;
		boolean detailsAvail = false;
		boolean nextLegAvail = false;

		switch (state) {
		case LIST_DOWN:
			listAvail = true;
			filterAvail = true;
			detailsAvail = true;
			break;
		case FILTERS:
			listAvail = true;
			filterAvail = true;
			detailsAvail = true;
			break;
		case DETAILS:
			listAvail = true;
			filterAvail = true;
			detailsAvail = true;
			nextLegAvail = !isLastLeg();
			break;
		case LATER_LEG:
			listAvail = true;
			filterAvail = true;
			detailsAvail = true;
			nextLegAvail = true;
			break;
		case ADDING_TO_TRIP:
			if (isLastLeg()) {
				listAvail = true;
				filterAvail = true;
				detailsAvail = true;
			}
			else {
				nextLegAvail = true;
				if (isFirstLeg()) {
					listAvail = true;
				}
			}
			break;
		}

		mListFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(listAvail, FTAG_LIST, manager, transaction, this, R.id.list_container, true);
		mFilterFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(filterAvail, FTAG_FILTERS, manager, transaction, this, R.id.filters_container,
				false);
		mDetailsFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(detailsAvail, FTAG_DETAILS, manager, transaction, this, R.id.details_container,
				true);
		mNextLegFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(nextLegAvail, FTAG_NEXT_LEG, manager, transaction, this, R.id.next_leg_container,
				false);

		Log.d("updateFragmentsForState - leg:" + mLegNumber + " state:" + state + " list:" + listAvail + " filters:"
			+ filterAvail + " details:" + detailsAvail + " nextLeg:" + nextLegAvail);

		transaction.commit();
	}

	/**
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		switch (tag) {
		case FTAG_DETAILS:
			return mDetailsFrag;
		case FTAG_FILTERS:
			return mFilterFrag;
		case FTAG_LIST:
			return mListFrag;
		case FTAG_NEXT_LEG:
			return mNextLegFrag;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		switch (tag) {
		case FTAG_DETAILS:
			return ResultsFlightDetailsFragment.newInstance(mLegNumber);
		case FTAG_FILTERS:
			return ResultsFlightFiltersFragment.newInstance(mLegNumber);
		case FTAG_LIST:
			return ResultsFlightListFragment.getInstance(mLegNumber);
		case FTAG_NEXT_LEG:
			return ResultsRecursiveFlightLegsFragment.newInstance(mLegNumber + 1);
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		switch (tag) {
		case FTAG_DETAILS:
			updateDetailsFragSizes((ResultsFlightDetailsFragment) frag);
			break;
		case FTAG_FILTERS:
			((ResultsFlightFiltersFragment) frag).bindAll();
			break;
		case FTAG_LIST:
			ResultsFlightListFragment listFrag = (ResultsFlightListFragment) frag;
			listFrag.setTopRightTextButtonText(getString(R.string.Sort_and_Filter));
			listFrag.setTopSpacePixels(mGrid.getRowHeight(1));
			break;
		}
	}

	/**
	 * Fragment Helpers
	 */

	public void clearSelection() {
		if (Ui.isAdded(mListFrag)) {
			mListFrag.clearSelection();
		}
	}

	private void updateDetailsFragSizes(ResultsFlightDetailsFragment frag) {
		if (frag != null && mGrid.getTotalWidth() > 0) {
			int actionbarHeight = getActivity().getActionBar().getHeight();

			int leftCol, rightCol;
			boolean isLandscape = mGrid.isLandscape();
			if (isLandscape) {
				leftCol = 2;
				rightCol = 4;
			}
			else {
				leftCol = 0;
				rightCol = 2;
			}

			Rect position = new Rect();
			position.left = mGrid.getColLeft(leftCol);
			position.right = mGrid.getColRight(rightCol);
			position.top = 0;
			position.bottom = mGrid.getTotalHeight() - actionbarHeight;
			float marginPercentage = isLandscape ? DETAILS_MARGIN_PERCENTAGE : DETAILS_MARGIN_PORTRAIT_PERCENTAGE;
			frag.setDefaultDetailsPositionAndDimensions(position, marginPercentage);
		}

		if (frag != null && mListFrag != null && mListFrag.hasList()) {
			FruitList list = mListFrag.getListView();
			frag.setDefaultRowDimensions(mGrid.getColWidth(1), list.getRowHeight(false));
		}
	}

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
				mGrid.setContainerToColumn(mFiltersC, 0);
				mGrid.setContainerToColumn(mListColumnC, 2);
				mGrid.setContainerToColumnSpan(mDetailsC, 0, 4);

				//Vertical alignment

				//Most content sits in rows 1 and 2 (below the actionbar)
				mGrid.setContainerToRowSpan(mFiltersC, 1, 2);
				mGrid.setContainerToRowSpan(mListColumnC, 1, 2);
				mGrid.setContainerToRowSpan(mDetailsC, 1, 2);

				//Frag stuff
				updateDetailsFragSizes(mDetailsFrag);
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
				mGrid.setContainerToColumn(mFiltersC, 0);
				mGrid.setContainerToColumn(mListColumnC, 2);
				mGrid.setContainerToColumnSpan(mDetailsC, 0, 2);

				//Vertical alignment

				//Most content sits in rows 1 and 2 (below the actionbar)
				mGrid.setContainerToRowSpan(mFiltersC, 1, 2);
				mGrid.setContainerToRowSpan(mListColumnC, 1, 2);
				mGrid.setContainerToRowSpan(mDetailsC, 1, 2);

				//Frag stuff
				updateDetailsFragSizes(mDetailsFrag);
			}

			if (mListFrag != null) {
				mListFrag.setTopSpacePixels(mGrid.getRowHeight(1));
			}
			// Race condition band-aid. the 0th-leg will invoke showDetailsAnimUpdate(1f)
			// on rotation (if it is in details state), but this method depends upon mGrid
			// being initialized.
			if (getState() == ResultsFlightLegState.DETAILS) {
				showDetailsAnimUpdate(1f);
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
			ResultsFlightLegState state = mStateManager.getState();
			if (mStateManager.isAnimating()) {
				setState(state, true);
				return true;
			}
			else {
				if (state == ResultsFlightLegState.LATER_LEG) {
					if (mNextLegFrag != null && mNextLegFrag.getState() == ResultsFlightLegState.FILTERS) {
						setState(ResultsFlightLegState.DETAILS, true);
						return true;
					}
					return false;
				}
				else if (state == ResultsFlightLegState.DETAILS) {
					setState(ResultsFlightLegState.FILTERS, true);
					return true;
				}
				else if (state == ResultsFlightLegState.FILTERS) {
					if (mLegNumber == 0) {
						setState(ResultsFlightLegState.LIST_DOWN, true);
						return true;
					}
				}
			}
			return false;
		}
	};

	/**
	 * ResultsFlightLegState STATE PROVIDER
	 */

	@Override
	public void startStateTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
		float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsFlightLegState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsFlightLegState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsFlightLegState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	/*
	 * FLIGHT LIST FRAGMENT LISTENER
	 */
	@Override
	public void onTopRightClicked() {
		if (getState() == ResultsFlightLegState.DETAILS) {
			setState(ResultsFlightLegState.FILTERS, true);
		}
	}

	@Override
	public void acceptingListenersUpdated(Fragment frag, boolean acceptingListener) {
		if (acceptingListener) {

			if (frag == mNextLegFrag) {
				mNextLegFrag.registerStateListener(mNextLegStateListener, false);
			}
			else if (frag == mListFrag && mLegNumber == 0) {
				mListFrag.getListView().setOnTouchListener(mListTouchListener);
				mListFrag.registerStateListener(mListStateListener, false);
			}
		}
		else {
			if (frag == mNextLegFrag) {
				mNextLegFrag.unRegisterStateListener(mNextLegStateListener);
			}
			else if (frag == mListFrag && mLegNumber == 0) {
				mListFrag.unRegisterStateListener(mListStateListener);
			}
		}
	}

	public boolean listHasTouch() {
		return mListHasTouch;
	}

	public boolean listIsDisplaced() {
		if (mListFrag != null && mListFrag.getListView() != null) {
			float per = mListFrag.getListView().getScrollDownPercentage();
			return per != 0f && per != 1f;
		}
		return false;
	}

	View.OnTouchListener mListTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mListener.isSiblingListBusy(LineOfBusiness.FLIGHTS)) {
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
}
