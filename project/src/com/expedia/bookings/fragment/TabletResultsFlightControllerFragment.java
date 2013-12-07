package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsFlightLegSelected;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerHandHolder;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.mobiata.android.util.Ui;

/**
 *  TabletResultsFlightControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to FLIGHTS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsFlightControllerFragment extends Fragment implements IResultsFlightSelectedListener,
		IResultsFlightLegSelected, IAddToTripListener, IFragmentAvailabilityProvider, IBackManageable,
		IStateProvider<ResultsFlightsState> {

	public interface IFlightsFruitScrollUpListViewChangeListener {

		public void onFlightsStateChanged(State oldState, State newState, float percentage, View requester);

		public void onFlightsPercentageChanged(State state, float percentage);

	}

	private IFruitScrollUpListViewChangeListener mFruitProxy = new IFruitScrollUpListViewChangeListener() {

		@Override
		public void onStateChanged(State oldState, State newState, float percentage) {
			if (mListener != null) {
				mListener.onFlightsStateChanged(oldState, newState, percentage, mFlightOneListC);
			}
		}

		@Override
		public void onPercentageChanged(State state, float percentage) {
			if (mListener != null) {
				mListener.onFlightsPercentageChanged(state, percentage);
			}

		}
	};

	//State
	private static final String STATE_FLIGHTS_STATE = "STATE_HOTELS_STATE";
	private static final String STATE_GLOBAL_STATE = "STATE_GLOBAL_STATE";

	//Frag tags
	private static final String FTAG_FLIGHT_MAP = "FTAG_FLIGHT_MAP";
	private static final String FTAG_FLIGHT_ADD_TO_TRIP = "FTAG_FLIGHT_ADD_TO_TRIP";
	private static final String FTAG_FLIGHT_ONE_FILTERS = "FTAG_FLIGHT_ONE_FILTERS";
	private static final String FTAG_FLIGHT_ONE_LIST = "FTAG_FLIGHT_ONE_LIST";
	private static final String FTAG_FLIGHT_TWO_FILTERS = "FTAG_FLIGHT_TWO_FILTERS";
	private static final String FTAG_FLIGHT_TWO_LIST = "FTAG_FLIGHT_TWO_LIST";
	private static final String FTAG_FLIGHT_ONE_DETAILS = "FTAG_FLIGHT_ONE_DETAILS";
	private static final String FTAG_FLIGHT_TWO_DETAILS = "FTAG_FLIGHT_TWO_DETAILS";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mFlightMapC;
	private BlockEventFrameLayout mAddToTripC;

	private BlockEventFrameLayout mFlightOneListC;
	private BlockEventFrameLayout mFlightOneFiltersC;
	private BlockEventFrameLayout mFlightOneDetailsC;

	private RelativeLayout mFlightTwoListColumnC;
	private BlockEventFrameLayout mFlightTwoFlightOneHeaderC;
	private BlockEventFrameLayout mFlightTwoListC;
	private BlockEventFrameLayout mFlightTwoFiltersC;
	private BlockEventFrameLayout mFlightTwoDetailsC;

	private ArrayList<ViewGroup> mContainers = new ArrayList<ViewGroup>();

	//Views
	private FlightLegSummarySectionTablet mFlightOneSelectedRow;

	//Fragments
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsFlightAddToTrip mAddToTripFrag;
	private ResultsFlightListFragment mFlightOneListFrag;
	private ResultsFlightFiltersFragment mFlightOneFilterFrag;
	private ResultsFlightDetailsFragment mFlightOneDetailsFrag;
	private ResultsFlightListFragment mFlightTwoListFrag;
	private ResultsFlightFiltersFragment mFlightTwoFilterFrag;
	private ResultsFlightDetailsFragment mFlightTwoDetailsFrag;

	//Other
	private ResultsState mGlobalState;
	private IFlightsFruitScrollUpListViewChangeListener mListener;
	private GridManager mGrid = new GridManager();
	private float mFlightDetailsMarginPercentage = 0.1f;
	private boolean mOneWayFlight = true;
	private IAddToTripListener mParentAddToTripListener;

	private StateManager<ResultsFlightsState> mFlightsStateManager = new StateManager<ResultsFlightsState>(
			ResultsFlightsState.FLIGHT_ONE_FILTERS, this);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedLegs() != null
				&& Db.getFlightSearch().getSelectedLegs().length > 1) {
			mOneWayFlight = false;
		}

		mListener = Ui.findFragmentListener(this, IFlightsFruitScrollUpListViewChangeListener.class, true);
		mParentAddToTripListener = Ui.findFragmentListener(this, IAddToTripListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_flights, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mFlightMapC = Ui.findView(view, R.id.bg_flight_map);
		mAddToTripC = Ui.findView(view, R.id.flights_add_to_trip);

		mFlightOneFiltersC = Ui.findView(view, R.id.flight_one_filters);
		mFlightOneListC = Ui.findView(view, R.id.flight_one_list);
		mFlightOneDetailsC = Ui.findView(view, R.id.flight_one_details);
		mFlightTwoListColumnC = Ui.findView(view, R.id.flight_two_list_and_header_container);
		mFlightTwoFlightOneHeaderC = Ui.findView(view, R.id.flight_two_header_with_flight_one_info);
		mFlightTwoListC = Ui.findView(view, R.id.flight_two_list);
		mFlightTwoFiltersC = Ui.findView(view, R.id.flight_two_filters);
		mFlightTwoDetailsC = Ui.findView(view, R.id.flight_two_details);

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

		mFlightOneSelectedRow = Ui.findView(view, R.id.flight_one_row);

		if (savedInstanceState != null) {
			mGlobalState = ResultsState.valueOf(savedInstanceState.getString(STATE_GLOBAL_STATE,
					ResultsState.OVERVIEW.name()));
			mFlightsStateManager.setDefaultState(ResultsFlightsState.valueOf(savedInstanceState.getString(
					STATE_FLIGHTS_STATE,
					ResultsFlightsState.FLIGHT_ONE_FILTERS.name())));
		}

		registerStateListener(mFlightsStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsFlightsState>(), false);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_FLIGHTS_STATE, mFlightsStateManager.getState().name());
		outState.putString(STATE_GLOBAL_STATE, mGlobalState.name());
	}

	@Override
	public void onResume() {
		super.onResume();
		mStateHelper.registerWithProvider(this);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
	}

	private Rect getAddTripRect() {
		return mAddToTripFrag.getRowRect();
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

		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_FLIGHT_MAP) {
			updateMapFragSizes((ResultsFlightMapFragment) frag);
		}
		else if (tag == FTAG_FLIGHT_ONE_LIST) {
			((ResultsListFragment) frag).setChangeListener(mFruitProxy);
			((ResultsListFragment) frag).setTopRightTextButtonText(getString(R.string.Done));
		}
		else if (tag == FTAG_FLIGHT_TWO_LIST) {
			((ResultsListFragment) frag).gotoTopPosition(0);
			((ResultsListFragment) frag).setListLockedToTop(true);
		}
		else if (tag == FTAG_FLIGHT_ONE_DETAILS) {
			updateDetailsFragSizes((ResultsFlightDetailsFragment) frag);
		}
		else if (tag == FTAG_FLIGHT_TWO_DETAILS) {
			updateDetailsFragSizes((ResultsFlightDetailsFragment) frag);
		}
	}

	private void updateDetailsFragSizes(ResultsFlightDetailsFragment frag) {
		if (frag != null && mGrid.getTotalWidth() > 0) {
			int actionbarHeight = getActivity().getActionBar().getHeight();
			int leftCol = 1;
			int rightCol = 2;
			if (!mGrid.isLandscape()) {
				leftCol = 0;
				rightCol = 1;
			}

			Rect position = new Rect();
			position.left = mGrid.getColLeft(leftCol);
			position.right = mGrid.getColRight(rightCol);
			position.top = 0;
			position.bottom = mGrid.getTotalHeight() - actionbarHeight;
			frag.setDefaultDetailsPositionAndDimensions(position, mFlightDetailsMarginPercentage);
		}
		if (frag != null && mFlightOneListFrag != null && mFlightOneListFrag.getTopSpaceListView() != null
				&& mFlightOneListFrag.getTopSpaceListView().getRowHeight(false) > 0) {
			frag.setDetaultRowDimensions(mGrid.getColWidth(1), mFlightOneListFrag.getTopSpaceListView()
					.getRowHeight(false));
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
		if (mGlobalState == ResultsState.FLIGHTS) {
			if (legNumber == 0) {
				//TODO: IF MULTILEG FLIGHT BIND THE FLIGHT TO THE ROW HEADER
				setFlightsState(ResultsFlightsState.FLIGHT_ONE_DETAILS,
						mFlightsStateManager.getState() != ResultsFlightsState.FLIGHT_ONE_DETAILS);
				// Make sure to reset the query, as the flights present in the second leg depend upon the flight
				// selected from the first leg. Frag is null for one-way flights.
				if (mFlightTwoListFrag != null) {
					mFlightTwoListFrag.resetQuery();
				}
				if (mFlightTwoFilterFrag != null) {
					mFlightTwoFilterFrag.onFilterChanged();
				}

				mFlightOneDetailsFrag.bindWithDb();
			}
			else if (legNumber == 1) {
				setFlightsState(ResultsFlightsState.FLIGHT_TWO_DETAILS,
						mFlightsStateManager.getState() != ResultsFlightsState.FLIGHT_TWO_DETAILS);

				mFlightTwoDetailsFrag.bindWithDb();
			}
		}
	}

	/*
	 * IResultsFlightLegSelected
	 */

	@Override
	public void onTripAdded(int legNumber) {
		if (mGlobalState == ResultsState.FLIGHTS) {
			boolean lastLegToSelect = mOneWayFlight || legNumber == 1;
			if (lastLegToSelect) {
				setFlightsState(ResultsFlightsState.ADDING_FLIGHT_TO_TRIP, true);
			}
			else {
				setFlightsState(ResultsFlightsState.FLIGHT_TWO_FILTERS, true);
			}
		}
	}

	/*
	 * IAddToTripListener Functions
	 */

	@Override
	public void beginAddToTrip(Object data, Rect globalCoordinates, int shadeColor) {
		//TODO: Block touches during this transition...
		mParentAddToTripListener.beginAddToTrip(data, globalCoordinates, shadeColor);
	}

	@Override
	public void performTripHandoff() {
		//FAKE IT TO MAKE IT
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f)
				.setDuration(StateManager.STATE_CHANGE_ANIMATION_DURATION);
		animator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				mListener.onFlightsPercentageChanged(State.TRANSIENT, (Float) arg0.getAnimatedValue());
				//Fade in the flight list, this will look dumb and needs to be re-thought
				mFlightOneListC.setAlpha((Float) arg0.getAnimatedValue());
			}

		});
		animator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator arg0) {
				if (getActivity() != null) {
					mListener.onFlightsStateChanged(State.TRANSIENT, State.LIST_CONTENT_AT_BOTTOM, 0f, mFlightOneListC);
					mFlightOneListFrag.getTopSpaceListView().setListenersEnabled(true);
				}
			}
		});

		mListener.onFlightsStateChanged(State.LIST_CONTENT_AT_TOP, State.TRANSIENT, 0f, mFlightOneListC);
		animator.start();
		mFlightOneListFrag.getTopSpaceListView().setListenersEnabled(false);
		mFlightOneListC.setTranslationX(0);
		mFlightOneListFrag.gotoBottomPosition(0);
		mFlightOneListC.setAlpha(0f);
		mFlightOneListC.setVisibility(View.VISIBLE);
		mFlightOneDetailsC.setVisibility(View.INVISIBLE);
		mFlightTwoDetailsC.setVisibility(View.INVISIBLE);

		//Tell the trip overview to do its thing...
		mParentAddToTripListener.performTripHandoff();
	}

	/*
	 * STATE HELPER METHODS
	 */

	public void setFlightsState(ResultsFlightsState state, boolean animate) {
		mFlightsStateManager.setState(state, animate);
	}

	public ResultsFlightsState getFlightsState(ResultsState state) {
		return state != ResultsState.FLIGHTS ? mFlightsStateManager.getDefaultState() : mFlightsStateManager.getState();
	}

	private void setTouchState(ResultsState globalState, ResultsFlightsState flightsState) {
		ArrayList<ViewGroup> touchableViews = new ArrayList<ViewGroup>();
		switch (globalState) {
		case FLIGHTS: {
			switch (flightsState) {
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
			break;
		}
		case OVERVIEW: {
			touchableViews.add(mFlightOneListC);
			break;
		}
		default: {
			break;
		}
		}

		for (ViewGroup vg : mContainers) {
			if (vg instanceof BlockEventFrameLayout) {
				if (touchableViews.contains(vg)) {
					((BlockEventFrameLayout) vg).setBlockNewEventsEnabled(false);
				}
				else {
					((BlockEventFrameLayout) vg).setBlockNewEventsEnabled(true);
				}
			}
		}
	}

	private void setVisibilityState(ResultsState globalState, ResultsFlightsState flightsState) {
		ArrayList<ViewGroup> visibleViews = new ArrayList<ViewGroup>();
		switch (globalState) {
		case FLIGHTS: {
			visibleViews.add(mFlightMapC);
			switch (flightsState) {
			case FLIGHT_ONE_FILTERS: {
				visibleViews.add(mFlightOneFiltersC);
				visibleViews.add(mFlightOneListC);
				break;
			}
			case FLIGHT_ONE_DETAILS: {
				visibleViews.add(mFlightOneDetailsC);
				visibleViews.add(mFlightOneListC);
				break;
			}
			case FLIGHT_TWO_FILTERS: {
				visibleViews.add(mFlightTwoFiltersC);
				visibleViews.add(mFlightTwoListColumnC);
				visibleViews.add(mFlightTwoFlightOneHeaderC);
				visibleViews.add(mFlightTwoListC);
				break;
			}
			case FLIGHT_TWO_DETAILS: {
				visibleViews.add(mFlightTwoDetailsC);
				visibleViews.add(mFlightTwoListC);
				visibleViews.add(mFlightTwoListColumnC);
				visibleViews.add(mFlightTwoFlightOneHeaderC);
				break;
			}
			case ADDING_FLIGHT_TO_TRIP: {
				visibleViews.add(mAddToTripC);
				break;
			}
			}
			break;
		}
		case OVERVIEW: {
			visibleViews.add(mFlightOneListC);
			break;
		}
		default: {
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

	private void setFragmentState(ResultsState state, ResultsFlightsState flightsState) {

		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction

		FragmentTransaction transaction = manager.beginTransaction();

		boolean flightOneListAvailable = true;
		boolean flightMapAvailable = true;
		boolean flightAddToTripAvailable = true;
		boolean flightOneFiltersAvailable = true;
		boolean flightTwoListAvailable = true;
		boolean flightTwoFiltersAvailabe = true;
		boolean flightOneDetailsAvailable = true;
		boolean flightTwoDetailsAvailable = true;

		if (state != ResultsState.FLIGHTS && state != ResultsState.OVERVIEW) {
			flightMapAvailable = false;
			flightOneFiltersAvailable = false;
		}
		if (state != ResultsState.FLIGHTS) {
			flightTwoListAvailable = false;
			flightTwoFiltersAvailabe = false;
			flightOneDetailsAvailable = false;
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
		mFlightOneListFrag = (ResultsFlightListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightOneListAvailable,
				FTAG_FLIGHT_ONE_LIST, manager, transaction, this, R.id.flight_one_list, false);
		mFlightOneFilterFrag = (ResultsFlightFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightOneFiltersAvailable,
				FTAG_FLIGHT_ONE_FILTERS, manager, transaction, this, R.id.flight_one_filters, false);
		mFlightOneDetailsFrag = (ResultsFlightDetailsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightOneDetailsAvailable,
				FTAG_FLIGHT_ONE_DETAILS, manager, transaction, this, R.id.flight_one_details, true);
		mFlightTwoListFrag = (ResultsFlightListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightTwoListAvailable,
				FTAG_FLIGHT_TWO_LIST, manager, transaction, this, R.id.flight_two_list, false);
		mFlightTwoFilterFrag = (ResultsFlightFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightTwoFiltersAvailabe,
				FTAG_FLIGHT_TWO_FILTERS, manager, transaction, this, R.id.flight_two_filters, false);
		mFlightTwoDetailsFrag = (ResultsFlightDetailsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				flightTwoDetailsAvailable,
				FTAG_FLIGHT_TWO_DETAILS, manager, transaction, this, R.id.flight_two_details, true);

		transaction.commit();

	}

	/*
	 * RESULTS STATE LISTENER
	 */

	private StateListenerHelper<ResultsState> mStateHelper = new StateListenerHandHolder<ResultsState>() {

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				mFlightOneFiltersC.setAlpha(1f - percentage);
				mFlightMapC.setAlpha(1f - percentage);
				float filterPaneTopTranslation = percentage
						* mFlightOneListFrag.getTopSpaceListView().getHeaderSpacerHeight();
				mFlightOneFiltersC.setTranslationY(filterPaneTopTranslation);
			}

			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				int colOneDist = mGrid.getTotalWidth() - mGrid.getColLeft(1);
				int colTwoDist = mGrid.getTotalWidth() - mGrid.getColLeft(2);

				mFlightMapC.setTranslationX(colTwoDist * (1f - percentage));
				mFlightOneListC.setTranslationX(colOneDist * (1f - percentage));
			}
		}

		@Override
		public void setHardwareLayerForTransition(ResultsState stateOne, ResultsState stateTwo, int layerType) {
			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.HOTELS)
					&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.HOTELS)) {
				//Default -> Hotels or Hotels -> Default transition

				mFlightOneListC.setLayerType(layerType, null);

			}

			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.FLIGHTS)
					&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.FLIGHTS)) {
				//Default -> Flights or Flights -> Default transition

				mFlightOneFiltersC.setLayerType(layerType, null);
			}

		}

		@Override
		public void setVisibilityForTransition(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW || stateTwo == ResultsState.OVERVIEW) {
				mFlightOneListC.setVisibility(View.VISIBLE);
			}

			if (stateOne == ResultsState.FLIGHTS || stateTwo == ResultsState.FLIGHTS) {
				mFlightMapC.setVisibility(View.VISIBLE);
				mFlightOneFiltersC.setVisibility(View.VISIBLE);
				mFlightOneListC.setVisibility(View.VISIBLE);
			}

		}

		@Override
		public void setTouchabilityForTransition(ResultsState stateOne, ResultsState stateTwo) {
			for (ViewGroup vg : mContainers) {
				if (vg instanceof BlockEventFrameLayout) {
					((BlockEventFrameLayout) vg).setBlockNewEventsEnabled(true);
				}
			}
		}

		@Override
		public void setTouchabilityForState(ResultsState state) {
			setTouchState(state, getFlightsState(state));
		}

		@Override
		public void setVisibilityForState(ResultsState state) {
			setVisibilityState(state, getFlightsState(state));
		}

		@Override
		public void setFragmentsForState(ResultsState state) {
			setFragmentState(state, getFlightsState(state));
		}

		@Override
		public void setMiscForState(ResultsState state) {
			mGlobalState = state;
			setFlightsState(getFlightsState(state), false);
		}
	};

	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);

			if (isLandscape) {
				mGrid.setGridSize(1, 3);

				mGrid.setContainerToColumnSpan(mFlightMapC, 0, 2);

				mGrid.setContainerToColumn(mFlightOneFiltersC, 0);
				mGrid.setContainerToColumn(mFlightOneListC, 1);
				mGrid.setContainerToColumnSpan(mFlightOneDetailsC, 0, 2);

				mGrid.setContainerToColumn(mFlightTwoFiltersC, 0);
				mGrid.setContainerToColumn(mFlightTwoListColumnC, 1);
				mGrid.setContainerToColumnSpan(mFlightTwoDetailsC, 0, 2);
			}
			else {
				mGrid.setGridSize(2, 2);

				mGrid.setContainerToColumnSpan(mFlightMapC, 0, 1);

				mGrid.setContainerToColumn(mFlightOneFiltersC, 0);
				mGrid.setContainerToColumn(mFlightOneListC, 1);
				mGrid.setContainerToColumnSpan(mFlightOneDetailsC, 0, 1);

				mGrid.setContainerToColumn(mFlightTwoFiltersC, 0);
				mGrid.setContainerToColumn(mFlightTwoListColumnC, 1);
				mGrid.setContainerToColumnSpan(mFlightTwoDetailsC, 0, 1);
			}

			updateDetailsFragSizes(mFlightOneDetailsFrag);
			updateDetailsFragSizes(mFlightTwoDetailsFrag);
			updateMapFragSizes(mFlightMapFrag);

			//since the actionbar is an overlay, we must compensate by setting the root layout to have a top margin
			int actionBarHeight = getActivity().getActionBar().getHeight();
			FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mRootC.getLayoutParams();
			params.topMargin = actionBarHeight;
			mRootC.setLayoutParams(params);
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
			if (mGlobalState == ResultsState.FLIGHTS) {
				ResultsFlightsState state = mFlightsStateManager.getState();
				if (mFlightsStateManager.isAnimating()) {
					//If we are in the middle of state transition, just reverse it
					setFlightsState(state, true);
					return true;
				}
				else {
					if (state == ResultsFlightsState.FLIGHT_ONE_FILTERS) {
						mFlightOneListFrag.gotoBottomPosition();
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
			}
			return false;
		}

	};

	/*
	 * FLIGHTS STATE PROVIDER
	 */
	private ArrayList<IStateListener<ResultsFlightsState>> mStateChangeListeners = new ArrayList<IStateListener<ResultsFlightsState>>();

	@Override
	public void startStateTransition(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
		for (IStateListener<ResultsFlightsState> listener : mStateChangeListeners) {
			listener.onStateTransitionStart(stateOne, stateTwo);
		}
	}

	@Override
	public void updateStateTransition(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
		for (IStateListener<ResultsFlightsState> listener : mStateChangeListeners) {
			listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
		}

	}

	@Override
	public void endStateTransition(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
		for (IStateListener<ResultsFlightsState> listener : mStateChangeListeners) {
			listener.onStateTransitionEnd(stateOne, stateTwo);
		}
	}

	@Override
	public void finalizeState(ResultsFlightsState state) {
		for (IStateListener<ResultsFlightsState> listener : mStateChangeListeners) {
			listener.onStateFinalized(state);
		}
	}

	@Override
	public void registerStateListener(IStateListener<ResultsFlightsState> listener, boolean fireFinalizeState) {
		mStateChangeListeners.add(listener);
		if (fireFinalizeState) {
			listener.onStateFinalized(getFlightsState(mGlobalState));
		}
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsFlightsState> listener) {
		mStateChangeListeners.remove(listener);

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
			if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS)) {
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
			else if (((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS))
					|| ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS))) {
				//WE ARE GOING FROM SHOWING THE FLIGHT LIST TO SHWOING FLIGHT DETAILS

				//Vars - because we want re-use as much as possible, and this animation can happen on any leg of the flight, we set temp vars
				//to use while animating
				if ((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
						&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS)) {
					mTransitionFiltersC = mFlightOneFiltersC;
					mTransitionListC = mFlightOneListC;
					mTransitionDetailsC = mFlightOneDetailsC;
					mTransitionDetailsFrag = mFlightOneDetailsFrag;
					mFlightOneDetailsFrag.prepareSlideInAnimation();
				}
				else if ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
						&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS)) {
					mTransitionFiltersC = mFlightTwoFiltersC;
					mTransitionListC = mFlightTwoListColumnC;
					mTransitionDetailsC = mFlightTwoDetailsC;
					mTransitionDetailsFrag = mFlightTwoDetailsFrag;
					mFlightTwoDetailsFrag.prepareSlideInAnimation();
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
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				//WE ARE GOING FROM SHOWING THE FLIGHT DETAILS TO SHOWING ADD_TO_TRIP

				Rect addToTripDestination = getAddTripRect();
				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneListC.setVisibility(View.VISIBLE);
					mFlightOneDetailsC.setVisibility(View.VISIBLE);
					mFlightOneDetailsFrag.prepareAddToTripFromDetailsAnimation(addToTripDestination);
				}
				else {
					if (mGrid.isLandscape()) {
						Rect departureFlightLocation = ScreenPositionUtils
								.getGlobalScreenPosition(mFlightTwoFlightOneHeaderC);
						mFlightOneDetailsFrag.prepareAddToTripFromDepartureAnimation(departureFlightLocation,
								addToTripDestination);
						mFlightOneDetailsC.setVisibility(View.VISIBLE);
					}

					mFlightTwoListColumnC.setVisibility(View.VISIBLE);
					mFlightTwoListC.setVisibility(View.VISIBLE);
					mFlightTwoDetailsC.setVisibility(View.VISIBLE);

					mFlightTwoDetailsFrag.prepareAddToTripFromDetailsAnimation(addToTripDestination);
				}
			}

		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
				float percentage) {
			if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS)) {
				//Between flight details and the next flight leg list/filters
				boolean forward = stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS;
				percentage = forward ? percentage : 1f - percentage;

				int flightOneListTranslationX = (int) (-mGrid.getColWidth(1) + percentage
						* -mGrid.getColWidth(1));
				int flightTwoTranslationX = (int) ((1f - percentage) * (mGrid.getColWidth(1) / 2f + mGrid
						.getColLeft(1)));

				if (!mGrid.isLandscape()) {
					flightOneListTranslationX = -mGrid.getColRight(1);
				}

				mFlightOneListC.setTranslationX(flightOneListTranslationX);
				mFlightTwoFiltersC.setTranslationX(flightTwoTranslationX);
				mFlightTwoListColumnC.setTranslationX(flightTwoTranslationX);
				mFlightTwoFiltersC.setAlpha(percentage);
				mFlightTwoListColumnC.setAlpha(percentage);
				mFlightOneDetailsFrag.setDepartureTripSelectedAnimationState(percentage);
			}
			else if (((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS))
					|| ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS))) {

				boolean forward = stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS
						|| stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS;
				percentage = forward ? percentage : 1f - percentage;

				//Between filters and details
				if (mTransitionFiltersC != null) {

					if (mGrid.isLandscape()) {
						mTransitionFiltersC.setTranslationX(percentage * -mGrid.getColWidth(0));
						mTransitionListC.setTranslationX(percentage * -mGrid.getColWidth(0));
					}
					else {
						mTransitionFiltersC.setTranslationX(percentage * -mGrid.getColRight(0));
						mTransitionListC.setTranslationX(percentage * -mGrid.getColRight(1));
					}

					if (mTransitionDetailsFrag != null) {
						int detailsTranslateDistance = mGrid.getColWidth(1) + mGrid.getColWidth(2);
						mTransitionDetailsFrag.setDetailsSlideInAnimationState(percentage, detailsTranslateDistance,
								true);
					}
				}
			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				//Tell fragments about our transition
				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
				}
				else {
					if (mGrid.isLandscape()) {
						mFlightOneDetailsFrag.setAddToTripFromDepartureAnimationState(percentage);
					}
					mFlightTwoDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
				}

				//Move flight list out of view
				float flightListTranslationX = -mGrid.getColWidth(0) + -percentage
						* mGrid.getColWidth(0);

				if (!mGrid.isLandscape()) {
					flightListTranslationX = -mGrid.getColRight(1);
				}

				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneListC.setTranslationX(flightListTranslationX);
				}
				else {
					mFlightTwoListColumnC.setTranslationX(flightListTranslationX);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (((stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_ONE_DETAILS))
					|| ((stateOne == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& (stateTwo == ResultsFlightsState.FLIGHT_TWO_FILTERS || stateTwo == ResultsFlightsState.FLIGHT_TWO_DETAILS))) {
				mTransitionFiltersC = null;
				mTransitionListC = null;
				mTransitionDetailsC = null;
				mTransitionDetailsFrag = null;
			}
			else if ((stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS || stateOne == ResultsFlightsState.FLIGHT_TWO_DETAILS)
					&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				if (stateOne == ResultsFlightsState.FLIGHT_ONE_DETAILS) {
					mFlightOneDetailsFrag.finalizeAddToTripFromDetailsAnimation();
				}
				else {
					mFlightOneDetailsFrag.finalizeAddToTripFromDepartureAnimation();
					mFlightTwoDetailsFrag.finalizeAddToTripFromDetailsAnimation();
				}
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			if (mFlightOneListFrag != null) {
				mFlightOneListFrag.setListLockedToTop(state != ResultsFlightsState.FLIGHT_ONE_FILTERS);
			}

			switch (state) {
			case FLIGHT_ONE_FILTERS: {
				positionForFilters(mFlightOneFiltersC, mFlightOneListC, 0);
				break;
			}
			case FLIGHT_ONE_DETAILS: {
				positionForDetails(mFlightOneFiltersC, mFlightOneListC, mFlightOneDetailsFrag);
				break;
			}
			case FLIGHT_TWO_FILTERS: {
				positionForFilters(mFlightTwoFiltersC, mFlightTwoListColumnC, 1);
				mFlightTwoFlightOneHeaderC.setVisibility(View.VISIBLE);
				break;
			}
			case FLIGHT_TWO_DETAILS: {
				positionForDetails(mFlightTwoFiltersC, mFlightTwoListColumnC, mFlightTwoDetailsFrag);
				break;
			}
			case ADDING_FLIGHT_TO_TRIP: {
				mAddToTripFrag.beginOrResumeAddToTrip();
				break;
			}
			}
			setTouchState(mGlobalState, state);
			setVisibilityState(mGlobalState, state);
		}

		private void positionForFilters(ViewGroup filtersC, ViewGroup listC, int legPos) {
			filtersC.setTranslationX(0f);
			listC.setTranslationX(0f);

			// Clear the checked/blue/highlight item when returning to filters
			ListView listView = Ui.findView(listC, android.R.id.list);
			if (listView != null) {
				int checkedPos = listView.getCheckedItemPosition();
				if (checkedPos != ListView.INVALID_POSITION) {
					listView.setItemChecked(checkedPos, false);
				}
			}

			if (legPos == 1) {
				mFlightOneSelectedRow.bind(Db.getFlightSearch(), 0);
			}
		}

		private void positionForDetails(ViewGroup filtersC, ViewGroup listC, ResultsFlightDetailsFragment detailsFrag) {
			if (mGrid.isLandscape()) {
				filtersC.setTranslationX(-mGrid.getColWidth(0));
				listC.setTranslationX(-mGrid.getColLeft(1));
				int detailsTranslateDistance = mGrid.getColWidth(1) + mGrid.getColWidth(2);
				detailsFrag.setDetailsSlideInAnimationState(1f, detailsTranslateDistance, true);
			}
			else {
				filtersC.setTranslationX(-mGrid.getColWidth(0));
				listC.setTranslationX(-mGrid.getColRight(1));
				int detailsTranslateDistance = mGrid.getColWidth(0) + mGrid.getColWidth(1);
				detailsFrag.setDetailsSlideInAnimationState(1f, detailsTranslateDistance, true);
			}
		}

	};

}
