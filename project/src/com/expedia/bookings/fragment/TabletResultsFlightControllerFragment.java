package com.expedia.bookings.fragment;

import java.util.ArrayList;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.mobiata.android.util.Ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 *  TabletResultsFlightControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to FLIGHTS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsFlightControllerFragment extends Fragment implements ITabletResultsController {

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

	private enum FlightsState {
		FLIGHT_ONE_FILTERS, FLIGHT_ONE_DETAILS, FLIGHT_TWO_FILTERS, FLIGHT_TWO_DETAILS, ADDING_FLIGHT_TO_TRIP
	}

	//State
	private static final String STATE_FLIGHTS_STATE = "STATE_HOTELS_STATE";
	private static final String STATE_GLOBAL_STATE = "STATE_GLOBAL_STATE";

	//Tags
	private static final String FRAG_TAG_FLIGHT_MAP = "FRAG_TAG_FLIGHT_MAP";
	private static final String FRAG_TAG_FLIGHT_FILTERS = "FRAG_TAG_FLIGHT_FILTERS";
	private static final String FRAG_TAG_FLIGHT_LIST = "FRAG_TAG_FLIGHT_LIST";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mFlightMapC;

	private BlockEventFrameLayout mFlightOneListC;
	private BlockEventFrameLayout mFlightOneFiltersC;
	private BlockEventFrameLayout mFlightOneDetailsC;

	private RelativeLayout mFlightTwoListColumnC;
	private BlockEventFrameLayout mFlightTwoFlightOneHeaderC;
	private BlockEventFrameLayout mFlightTwoListC;
	private BlockEventFrameLayout mFlightTwoFiltersC;
	private BlockEventFrameLayout mFlightTwoDetailsC;

	private ArrayList<ViewGroup> mContainers = new ArrayList<ViewGroup>();

	//Fragments
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsFlightListFragment mFlightOneListFrag;
	private ResultsFlightFiltersFragment mFlightOneFilterFrag;

	//Other
	private GlobalResultsState mGlobalState;
	private FlightsState mFlightsState = FlightsState.FLIGHT_ONE_FILTERS;
	private IFlightsFruitScrollUpListViewChangeListener mListener;
	private ColumnManager mColumnManager = new ColumnManager(3);

	//Animation
	private ValueAnimator mFlightsStateAnimator;
	private FlightsState mDestinationFlightsState;
	private static final int STATE_CHANGE_ANIMATION_DURATION = 300;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IFlightsFruitScrollUpListViewChangeListener.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_flights, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mFlightMapC = Ui.findView(view, R.id.bg_flight_map);

		mFlightOneFiltersC = Ui.findView(view, R.id.flight_one_filters);
		mFlightOneListC = Ui.findView(view, R.id.flight_one_list);
		mFlightOneDetailsC = Ui.findView(view, R.id.flight_one_details);
		mFlightTwoListColumnC = Ui.findView(view, R.id.flight_two_list_and_header_container);
		mFlightTwoFlightOneHeaderC = Ui.findView(view, R.id.flight_two_header_with_flight_one_info);
		mFlightTwoListC = Ui.findView(view, R.id.flight_two_list);
		mFlightTwoFiltersC = Ui.findView(view, R.id.flight_two_filters);
		mFlightTwoDetailsC = Ui.findView(view, R.id.flight_two_details);

		mContainers.add(mFlightMapC);
		mContainers.add(mFlightOneFiltersC);
		mContainers.add(mFlightOneListC);
		mContainers.add(mFlightOneDetailsC);
		mContainers.add(mFlightTwoListColumnC);
		mContainers.add(mFlightTwoFlightOneHeaderC);
		mContainers.add(mFlightTwoListC);
		mContainers.add(mFlightTwoFiltersC);
		mContainers.add(mFlightTwoDetailsC);

		if (savedInstanceState != null) {
			mGlobalState = GlobalResultsState.valueOf(savedInstanceState.getString(STATE_GLOBAL_STATE,
					GlobalResultsState.DEFAULT.name()));
			mFlightsState = FlightsState.valueOf(savedInstanceState.getString(STATE_FLIGHTS_STATE,
					FlightsState.FLIGHT_ONE_FILTERS.name()));
		}

		mFlightOneFiltersC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setFlightsState(FlightsState.FLIGHT_ONE_DETAILS, true);
			}

		});

		mFlightOneDetailsC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setFlightsState(FlightsState.FLIGHT_TWO_FILTERS, true);
			}

		});

		mFlightTwoFiltersC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setFlightsState(FlightsState.FLIGHT_TWO_DETAILS, true);
			}

		});

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_FLIGHTS_STATE, mFlightsState.name());
		outState.putString(STATE_GLOBAL_STATE, mGlobalState.name());
	}

	private void setFlightsState(FlightsState state, boolean animate) {
		if (!animate) {
			if (mFlightsStateAnimator != null && mFlightsStateAnimator.isStarted()) {
				mFlightsStateAnimator.cancel();
			}
			finalizeFlightsState(state);
		}
		else {
			if (mFlightsStateAnimator == null) {
				mDestinationFlightsState = state;
				mFlightsStateAnimator = getTowardsStateAnimator(state);
				if (mFlightsStateAnimator == null) {
					finalizeFlightsState(state);
				}
				else {
					mFlightsStateAnimator.start();
				}
			}
			else if (mDestinationFlightsState != state) {
				mDestinationFlightsState = state;
				mFlightsStateAnimator.reverse();
			}
		}
	}

	private void finalizeFlightsState(FlightsState state) {
		if (mFlightOneListFrag != null) {
			mFlightOneListFrag.setListLockedToTop(state != FlightsState.FLIGHT_ONE_FILTERS);
		}
		switch (state) {
		case FLIGHT_ONE_FILTERS: {
			setTransitionToFlightDetailsPercentage(mFlightOneFiltersC, mFlightOneListC, mFlightOneDetailsC, 0f);
			break;
		}
		case FLIGHT_ONE_DETAILS: {
			setTransitionToFlightDetailsPercentage(mFlightOneFiltersC, mFlightOneListC, mFlightOneDetailsC, 1f);
			setBetweenFlightsAnimationPercentage(0f);
			break;
		}
		case FLIGHT_TWO_FILTERS: {
			setBetweenFlightsAnimationPercentage(1f);
			setTransitionToFlightDetailsPercentage(mFlightTwoFiltersC, this.mFlightTwoListColumnC, mFlightTwoDetailsC,
					0f);
			break;
		}
		case FLIGHT_TWO_DETAILS: {
			setTransitionToFlightDetailsPercentage(mFlightTwoFiltersC, this.mFlightTwoListColumnC, mFlightTwoDetailsC,
					1f);
			break;
		}
		case ADDING_FLIGHT_TO_TRIP: {
			break;
		}
		}
		mFlightsState = state;
		mDestinationFlightsState = null;
		mFlightsStateAnimator = null;
		setVisibilityState(mGlobalState, state);
		setTouchState(mGlobalState, state);
	}

	private ValueAnimator getTowardsStateAnimator(FlightsState state) {
		if (state == FlightsState.FLIGHT_ONE_FILTERS) {
			return prepareTransitionToFlightDetailsAnimator(false, false);
		}
		else if (state == FlightsState.FLIGHT_ONE_DETAILS) {
			if (mFlightsState == FlightsState.FLIGHT_TWO_FILTERS) {
				return prepareTransitionBetweenFlights(false);
			}
			else {
				return prepareTransitionToFlightDetailsAnimator(true, false);
			}
		}
		else if (state == FlightsState.FLIGHT_TWO_FILTERS) {
			if (mFlightsState == FlightsState.FLIGHT_TWO_DETAILS) {
				return prepareTransitionToFlightDetailsAnimator(false, true);
			}
			else {
				return prepareTransitionBetweenFlights(true);
			}
		}
		else if (state == FlightsState.FLIGHT_TWO_DETAILS) {
			if (mFlightsState == FlightsState.ADDING_FLIGHT_TO_TRIP) {
				//TODO: Backwards from adding trip...
			}
			else {
				return prepareTransitionToFlightDetailsAnimator(true, true);
			}
		}
		return null;
	}

	/*
	 * GO BETWEEN FIRST AND SECOND FLIGHT ANIMATION STUFF
	 */

	private ValueAnimator prepareTransitionBetweenFlights(boolean forward) {
		float startValue = forward ? 0f : 1f;
		float endValue = forward ? 1f : 0f;

		ValueAnimator betweenFlightsAnimation = ValueAnimator.ofFloat(startValue, endValue).setDuration(
				STATE_CHANGE_ANIMATION_DURATION);
		betweenFlightsAnimation.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				setBetweenFlightsAnimationPercentage((Float) arg0.getAnimatedValue());
			}

		});
		betweenFlightsAnimation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				finalizeFlightsState(mDestinationFlightsState);
			}
		});

		mFlightOneListC.setVisibility(View.VISIBLE);
		mFlightOneDetailsC.setVisibility(View.VISIBLE);
		mFlightTwoFiltersC.setVisibility(View.VISIBLE);
		mFlightTwoListColumnC.setVisibility(View.VISIBLE);
		mFlightTwoListC.setVisibility(View.VISIBLE);
		mFlightTwoFlightOneHeaderC.setVisibility(View.VISIBLE);

		return betweenFlightsAnimation;
	}

	private void setTransitionBetweenFlightsHardwareRendering(boolean useHardwareLayer) {
		int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

		mFlightOneListC.setLayerType(layerType, null);
		mFlightTwoFiltersC.setLayerType(layerType, null);
		mFlightTwoListColumnC.setLayerType(layerType, null);

		//This will need some special consideration when we start flying around and stuff...
		mFlightOneDetailsC.setLayerType(layerType, null);
	}

	private void setBetweenFlightsAnimationPercentage(float percentage) {

		int flightOneListTranslationX = (int) (-mColumnManager.getColWidth(1) + percentage
				* -mColumnManager.getColWidth(1));
		int flightTwoTranslationX = (int) ((1f - percentage) * (mColumnManager.getColWidth(1) / 2f + mColumnManager
				.getColLeft(1)));

		mFlightOneListC.setTranslationX(flightOneListTranslationX);

		mFlightTwoFiltersC.setTranslationX(flightTwoTranslationX);
		mFlightTwoListColumnC.setTranslationX(flightTwoTranslationX);

		mFlightTwoFiltersC.setAlpha(percentage);
		mFlightTwoListColumnC.setAlpha(percentage);

		//TODO: THIS WONT ACTUALLY BE HAPPENING, IT DOES SOME SHRINKING AND FLYING AROUND AND STUFF.
		mFlightOneDetailsC.setAlpha(1f - percentage);
	}

	/*
	 * FLIGHT DETAILS ANIMATION STUFF
	 */

	private ValueAnimator prepareTransitionToFlightDetailsAnimator(boolean forward, boolean returnFlight) {
		float startValue = forward ? 0f : 1f;
		float endValue = forward ? 1f : 0f;

		final ViewGroup filters;
		final ViewGroup list;
		final ViewGroup details;
		if (!returnFlight) {
			filters = mFlightOneFiltersC;
			list = mFlightOneListC;
			details = mFlightOneDetailsC;
		}
		else {
			filters = mFlightTwoFiltersC;
			list = mFlightTwoListColumnC;
			details = mFlightTwoDetailsC;
		}

		ValueAnimator flightDetailsAnimator = ValueAnimator.ofFloat(startValue, endValue).setDuration(
				STATE_CHANGE_ANIMATION_DURATION);
		flightDetailsAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				setTransitionToFlightDetailsPercentage(filters, list, details, (Float) arg0.getAnimatedValue());
			}

		});
		flightDetailsAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				setTransitionToFlightDetailsHardwareRendering(false, filters, details, list);
				finalizeFlightsState(mDestinationFlightsState);
			}
		});

		filters.setVisibility(View.VISIBLE);
		details.setVisibility(View.VISIBLE);
		list.setVisibility(View.VISIBLE);
		setTransitionToFlightDetailsHardwareRendering(true, filters, details, list);
		return flightDetailsAnimator;

	}

	private void setTransitionToFlightDetailsHardwareRendering(boolean useHardwareLayer, ViewGroup filtersC,
			ViewGroup listC, ViewGroup detailsC) {
		int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		filtersC.setLayerType(layerType, null);
		listC.setLayerType(layerType, null);
		detailsC.setLayerType(layerType, null);
	}

	private void setTransitionToFlightDetailsPercentage(ViewGroup filtersC, ViewGroup listC, ViewGroup detailsC,
			float percentage) {
		filtersC.setTranslationX(percentage * -mColumnManager.getColWidth(0));
		listC.setTranslationX(percentage * -mColumnManager.getColWidth(0));

		int detailsTranslateDistance = mColumnManager.getColWidth(1) + mColumnManager.getColWidth(2);
		detailsC.setTranslationX(detailsTranslateDistance * -(1f - percentage));
	}

	private void setTouchState(GlobalResultsState globalState, FlightsState flightsState) {
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
		case DEFAULT: {
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

	private void setVisibilityState(GlobalResultsState globalState, FlightsState flightsState) {
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
				break;
			}
			}
			break;
		}
		case DEFAULT: {
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

	private void setFragmentState(GlobalResultsState state, FlightsState flightsState) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getChildFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

		boolean flightListAvailable = true;
		boolean flightMapAvailable = true;
		boolean flightFiltersAvailable = true;

		if (state != GlobalResultsState.HOTELS) {
			flightMapAvailable = false;
			flightFiltersAvailable = false;
		}

		//Flight list
		setFlightListFragmentAvailability(flightListAvailable, transaction);

		//Flight map
		setFlightMapFragmentAvailability(flightMapAvailable, transaction);

		//Flight filters
		setFlightFilterFragmentAvailability(flightFiltersAvailable, transaction);

		transaction.commit();

	}

	private FragmentTransaction setFlightListFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mFlightOneListFrag == null || !mFlightOneListFrag.isAdded()) {

				if (mFlightOneListFrag == null) {
					mFlightOneListFrag = (ResultsFlightListFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_FLIGHT_LIST);
				}
				if (mFlightOneListFrag == null) {
					mFlightOneListFrag = new ResultsFlightListFragment();
				}
				if (!mFlightOneListFrag.isAdded()) {
					transaction.add(R.id.flight_one_list, mFlightOneListFrag, FRAG_TAG_FLIGHT_LIST);
				}

				mFlightOneListFrag.setChangeListener(mFruitProxy);
				mFlightOneListFrag.setSortAndFilterButtonText(getString(R.string.done));
			}
		}
		else {
			if (mFlightOneListFrag == null) {
				mFlightOneListFrag = (ResultsFlightListFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_FLIGHT_LIST);
			}
			if (mFlightOneListFrag != null) {
				transaction.remove(mFlightOneListFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightMapFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightMapFrag == null || !mFlightMapFrag.isAdded()) {
				if (mFlightMapFrag == null) {
					mFlightMapFrag = (ResultsFlightMapFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_FLIGHT_MAP);
				}
				if (mFlightMapFrag == null) {
					mFlightMapFrag = ResultsFlightMapFragment.newInstance();
				}
				if (!mFlightMapFrag.isAdded()) {
					transaction.add(R.id.bg_flight_map, mFlightMapFrag, FRAG_TAG_FLIGHT_MAP);
				}
			}
		}
		else {
			if (mFlightMapFrag == null) {
				mFlightMapFrag = (ResultsFlightMapFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_FLIGHT_MAP);
			}
			if (mFlightMapFrag != null) {
				transaction.remove(mFlightMapFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightFilterFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightOneFilterFrag == null || !mFlightOneFilterFrag.isAdded()) {
				if (mFlightOneFilterFrag == null) {
					mFlightOneFilterFrag = (ResultsFlightFiltersFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_FLIGHT_FILTERS);
				}
				if (mFlightOneFilterFrag == null) {
					mFlightOneFilterFrag = ResultsFlightFiltersFragment.newInstance();
				}
				if (!mFlightOneFilterFrag.isAdded()) {
					transaction.add(R.id.flight_one_filters, mFlightOneFilterFrag, FRAG_TAG_FLIGHT_FILTERS);
				}
			}
		}
		else {
			if (mFlightOneFilterFrag == null) {
				mFlightOneFilterFrag = (ResultsFlightFiltersFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_FLIGHT_FILTERS);
			}
			if (mFlightOneFilterFrag != null) {
				transaction.remove(mFlightOneFilterFrag);
			}
		}
		return transaction;
	}

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mGlobalState = state;

		FlightsState tmpFlightsState = state != GlobalResultsState.FLIGHTS ? FlightsState.FLIGHT_ONE_FILTERS
				: mFlightsState;

		setTouchState(state, tmpFlightsState);
		setVisibilityState(state, tmpFlightsState);
		setFragmentState(state, tmpFlightsState);

		setFlightsState(tmpFlightsState, false);
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		if (state == GlobalResultsState.DEFAULT) {
			mFlightOneListC.setVisibility(View.VISIBLE);
		}
		else if (state == GlobalResultsState.FLIGHTS) {
			mFlightMapC.setVisibility(View.VISIBLE);
			mFlightOneFiltersC.setVisibility(View.VISIBLE);
			mFlightOneListC.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.HOTELS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.HOTELS)) {
			//Default -> Hotels or Hotels -> Default transition

			mFlightOneListC.setLayerType(layerType, null);

		}

		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//Default -> Flights or Flights -> Default transition

			mFlightMapC.setLayerType(layerType, null);
			mFlightOneFiltersC.setLayerType(layerType, null);
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		for (ViewGroup vg : mContainers) {
			if (vg instanceof BlockEventFrameLayout && vg != requester) {
				((BlockEventFrameLayout) vg).setBlockNewEventsEnabled(true);
			}
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		mFlightOneFiltersC.setAlpha(1f - percentage);
		mFlightMapC.setAlpha(1f - percentage);
		float filterPaneTopTranslation = percentage * mFlightOneListFrag.getTopSpaceListView().getHeaderSpacerHeight();
		mFlightOneFiltersC.setTranslationY(filterPaneTopTranslation);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		int colOneDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(1);
		int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);

		mFlightMapC.setTranslationX(colTwoDist * (1f - percentage));
		mFlightOneListC.setTranslationX(colOneDist * (1f - percentage));
	}

	@Override
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumnSpan(mFlightMapC, 0, 2);

		mColumnManager.setContainerToColumn(mFlightOneFiltersC, 0);
		mColumnManager.setContainerToColumn(mFlightOneListC, 1);
		mColumnManager.setContainerToColumnSpan(mFlightOneDetailsC, 1, 2);

		mColumnManager.setContainerToColumn(mFlightTwoFiltersC, 0);
		mColumnManager.setContainerToColumn(mFlightTwoListColumnC, 1);
		mColumnManager.setContainerToColumnSpan(mFlightTwoDetailsC, 1, 2);

	}

	@Override
	public boolean handleBackPressed() {
		if (mGlobalState == GlobalResultsState.FLIGHTS) {
			if (mFlightsStateAnimator != null) {
				//If we are in the middle of state transition, just reverse it
				this.setFlightsState(mFlightsState, true);
				return true;
			}
			else {
				if (mFlightsState == FlightsState.FLIGHT_ONE_FILTERS) {
					mFlightOneListFrag.gotoBottomPosition();
					return true;
				}
				else if (mFlightsState == FlightsState.FLIGHT_ONE_DETAILS) {
					setFlightsState(FlightsState.FLIGHT_ONE_FILTERS, true);
					return true;
				}
				else if (mFlightsState == FlightsState.FLIGHT_TWO_FILTERS) {
					setFlightsState(FlightsState.FLIGHT_ONE_DETAILS, true);
					return true;
				}
				else if (mFlightsState == FlightsState.FLIGHT_TWO_DETAILS) {
					setFlightsState(FlightsState.FLIGHT_TWO_FILTERS, true);
					return true;
				}
			}
		}
		return false;
	}
}
