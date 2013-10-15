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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerHandHolder;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
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
		IAddToTripListener, IFragmentAvailabilityProvider, IBackManageable {

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
	private FlightsState mFlightsState = FlightsState.FLIGHT_ONE_FILTERS;
	private IFlightsFruitScrollUpListViewChangeListener mListener;
	private ColumnManager mColumnManager = new ColumnManager(3);
	private int mGlobalHeight = 0;
	private float mFlightDetailsMarginPercentage = 0.1f;
	private boolean mOneWayFlight = true;
	private IAddToTripListener mParentAddToTripListener;

	//Animation
	private ValueAnimator mFlightsStateAnimator;
	private FlightsState mDestinationFlightsState;
	private static final int STATE_CHANGE_ANIMATION_DURATION = 300;

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
					ResultsState.DEFAULT.name()));
			mFlightsState = FlightsState.valueOf(savedInstanceState.getString(STATE_FLIGHTS_STATE,
					FlightsState.FLIGHT_ONE_FILTERS.name()));
		}

		mFlightOneDetailsC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mOneWayFlight) {
					setFlightsState(FlightsState.ADDING_FLIGHT_TO_TRIP, true);
				}
				else {
					setFlightsState(FlightsState.FLIGHT_TWO_FILTERS, true);
				}
			}

		});

		mFlightTwoDetailsC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setFlightsState(FlightsState.ADDING_FLIGHT_TO_TRIP, true);
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
			setTransitionToFlightDetailsPercentage(mFlightOneFiltersC, mFlightOneListC, mFlightOneDetailsFrag, 0f);
			break;
		}
		case FLIGHT_ONE_DETAILS: {
			setTransitionToFlightDetailsPercentage(mFlightOneFiltersC, mFlightOneListC, mFlightOneDetailsFrag, 1f);
			setBetweenFlightsAnimationPercentage(0f);
			break;
		}
		case FLIGHT_TWO_FILTERS: {
			setBetweenFlightsAnimationPercentage(1f);
			setTransitionToFlightDetailsPercentage(mFlightTwoFiltersC, this.mFlightTwoListColumnC,
					mFlightTwoDetailsFrag,
					0f);
			mFlightTwoFlightOneHeaderC.setVisibility(View.VISIBLE);
			break;
		}
		case FLIGHT_TWO_DETAILS: {
			setTransitionToFlightDetailsPercentage(mFlightTwoFiltersC, this.mFlightTwoListColumnC,
					mFlightTwoDetailsFrag,
					1f);
			break;
		}
		case ADDING_FLIGHT_TO_TRIP: {
			setAddTripAnimationPercentage(1f);
			mAddToTripFrag.beginOrResumeAddToTrip();
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
			else if (mFlightsState == FlightsState.ADDING_FLIGHT_TO_TRIP) {
				return null;//prepareTransitionToFlightDetailsAnimator(false, false);
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
				return prepareTransitionToAddTripAnimator(false);
			}
			else {
				return prepareTransitionToFlightDetailsAnimator(true, true);
			}
		}
		else if (state == FlightsState.ADDING_FLIGHT_TO_TRIP) {
			return prepareTransitionToAddTripAnimator(true);
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
				if (getActivity() != null) {
					mFlightOneDetailsFrag.finalizeDepartureFlightSelectedAnimation();
					finalizeFlightsState(mDestinationFlightsState);
				}
			}
		});

		//Tell the details where we will be flying the selected row
		Rect destinationSummaryLocation = ScreenPositionUtils
				.getGlobalScreenPositionWithoutTranslations(mFlightTwoFlightOneHeaderC);
		mFlightOneDetailsFrag.prepareDepartureFlightSelectedAnimation(destinationSummaryLocation);

		mFlightOneListC.setVisibility(View.VISIBLE);
		mFlightOneDetailsC.setVisibility(View.VISIBLE);
		mFlightTwoFiltersC.setVisibility(View.VISIBLE);
		mFlightTwoListColumnC.setVisibility(View.VISIBLE);
		mFlightTwoListC.setVisibility(View.VISIBLE);
		mFlightTwoFlightOneHeaderC.setVisibility(View.INVISIBLE);

		return betweenFlightsAnimation;
	}

	private void setTransitionBetweenFlightsHardwareRendering(boolean useHardwareLayer) {
		int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

		mFlightOneListC.setLayerType(layerType, null);
		mFlightTwoFiltersC.setLayerType(layerType, null);
		mFlightTwoListColumnC.setLayerType(layerType, null);

		if (mFlightOneDetailsFrag != null) {
			mFlightOneDetailsFrag.setDepartureTripSelectedAnimationLayer(layerType);
		}
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

		mFlightOneDetailsFrag.setDepartureTripSelectedAnimationState(percentage);
	}

	/*
	 * FLIGHT DETAILS ANIMATION STUFF
	 */

	private ValueAnimator prepareTransitionToFlightDetailsAnimator(boolean forward, boolean returnFlight) {
		float startValue = forward ? 0f : 1f;
		float endValue = forward ? 1f : 0f;

		final ViewGroup filters;
		final ViewGroup list;
		final ViewGroup detailsC;
		final ResultsFlightDetailsFragment details;
		if (!returnFlight) {
			filters = mFlightOneFiltersC;
			list = mFlightOneListC;
			detailsC = mFlightOneDetailsC;
			details = mFlightOneDetailsFrag;
		}
		else {
			filters = mFlightTwoFiltersC;
			list = mFlightTwoListColumnC;
			detailsC = mFlightTwoDetailsC;
			details = mFlightTwoDetailsFrag;
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
				if (getActivity() != null) {
					setTransitionToFlightDetailsHardwareRendering(false, filters, list, details);
					finalizeFlightsState(mDestinationFlightsState);
				}
			}
		});

		if (!returnFlight) {
			mFlightOneDetailsFrag.prepareSlideInAnimation();
		}
		else {
			mFlightTwoDetailsFrag.prepareSlideInAnimation();
		}

		filters.setVisibility(View.VISIBLE);
		detailsC.setVisibility(View.VISIBLE);
		list.setVisibility(View.VISIBLE);
		setTransitionToFlightDetailsHardwareRendering(true, filters, list, details);
		return flightDetailsAnimator;

	}

	private void setTransitionToFlightDetailsHardwareRendering(boolean useHardwareLayer, ViewGroup filtersC,
			ViewGroup listC, ResultsFlightDetailsFragment details) {
		int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		filtersC.setLayerType(layerType, null);
		listC.setLayerType(layerType, null);

		if (details != null) {
			details.setSlideInAnimationLayer(layerType);
		}
	}

	private void setTransitionToFlightDetailsPercentage(ViewGroup filtersC, ViewGroup listC,
			ResultsFlightDetailsFragment details,
			float percentage) {
		filtersC.setTranslationX(percentage * -mColumnManager.getColWidth(0));
		listC.setTranslationX(percentage * -mColumnManager.getColWidth(0));

		if (details != null) {
			int detailsTranslateDistance = mColumnManager.getColWidth(1) + mColumnManager.getColWidth(2);
			details.setDetailsSlideInAnimationState(percentage, detailsTranslateDistance, true);
		}
	}

	/*
	 * GO TO ADD TRIP ANIMATION STUFF
	 */

	private ValueAnimator prepareTransitionToAddTripAnimator(boolean forward) {
		float startValue = forward ? 0f : 1f;
		float endValue = forward ? 1f : 0f;

		ValueAnimator addTripAnimation = ValueAnimator.ofFloat(startValue, endValue).setDuration(
				STATE_CHANGE_ANIMATION_DURATION);
		addTripAnimation.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				setAddTripAnimationPercentage((Float) arg0.getAnimatedValue());
			}

		});
		addTripAnimation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				if (getActivity() != null) {
					if (mOneWayFlight) {
						mFlightOneDetailsFrag.finalizeAddToTripFromDetailsAnimation();
					}
					else {
						mFlightOneDetailsFrag.finalizeAddToTripFromDepartureAnimation();
						mFlightTwoDetailsFrag.finalizeAddToTripFromDetailsAnimation();
					}
					finalizeFlightsState(mDestinationFlightsState);
				}
			}
		});

		Rect addToTripDestination = getAddTripRect();
		if (mOneWayFlight) {
			mFlightOneListC.setVisibility(View.VISIBLE);
			mFlightOneDetailsC.setVisibility(View.VISIBLE);
			mFlightOneDetailsFrag.prepareAddToTripFromDetailsAnimation(addToTripDestination);
		}
		else {

			Rect departureFlightLocation = ScreenPositionUtils.getGlobalScreenPosition(mFlightTwoFlightOneHeaderC);

			mFlightOneDetailsFrag.prepareAddToTripFromDepartureAnimation(departureFlightLocation, addToTripDestination);
			mFlightOneDetailsC.setVisibility(View.VISIBLE);

			mFlightTwoListColumnC.setVisibility(View.VISIBLE);
			mFlightTwoListC.setVisibility(View.VISIBLE);
			mFlightTwoDetailsC.setVisibility(View.VISIBLE);

			mFlightTwoDetailsFrag.prepareAddToTripFromDetailsAnimation(addToTripDestination);
		}

		return addTripAnimation;
	}

	private Rect getAddTripRect() {
		return mAddToTripFrag.getRowRect();
	}

	private void setAddTripHardwareRendering(boolean useHardwareLayer) {
		int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

		//	TODO: set hardware layers
	}

	private void setAddTripAnimationPercentage(float percentage) {
		//Tell fragments about our transition
		if (mOneWayFlight) {
			mFlightOneDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
		}
		else {
			mFlightOneDetailsFrag.setAddToTripFromDepartureAnimationState(percentage);
			mFlightTwoDetailsFrag.setAddToTripFromDetailsAnimationState(percentage);
		}

		//Move flight list out of view
		float flightListTranslationX = -mColumnManager.getColWidth(0) + -percentage * mColumnManager.getColWidth(0);
		if (mOneWayFlight) {
			mFlightOneListC.setTranslationX(flightListTranslationX);
		}
		else {
			mFlightTwoListColumnC.setTranslationX(flightListTranslationX);
		}
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
		if (tag == FTAG_FLIGHT_ONE_LIST) {
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
		if (frag != null && mColumnManager.getTotalWidth() > 0) {
			int actionbarHeight = getActivity().getActionBar().getHeight();
			Rect position = new Rect();
			position.left = mColumnManager.getColLeft(1);
			position.right = mColumnManager.getColRight(2);
			position.top = 0;
			position.bottom = mGlobalHeight - actionbarHeight;
			frag.setDefaultDetailsPositionAndDimensions(position, mFlightDetailsMarginPercentage);
		}
		if (frag != null && mFlightOneListFrag != null && mFlightOneListFrag.getTopSpaceListView() != null
				&& mFlightOneListFrag.getTopSpaceListView().getRowHeight(false) > 0) {
			frag.setDetaultRowDimensions(mColumnManager.getColWidth(1), mFlightOneListFrag.getTopSpaceListView()
					.getRowHeight(false));
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
				setFlightsState(FlightsState.FLIGHT_ONE_DETAILS, mFlightsState != FlightsState.FLIGHT_ONE_DETAILS);
				// Make sure to reset the query, as the flights present in the second leg depend upon the flight
				// selected from the first leg. Frag is null for one-way flights.
				if (mFlightTwoListFrag != null) {
					mFlightTwoListFrag.resetQuery();
				}
				if (mFlightTwoFilterFrag != null) {
					mFlightTwoFilterFrag.onFilterChanged();
				}
			}
			else if (legNumber == 1) {
				setFlightsState(FlightsState.FLIGHT_TWO_DETAILS, mFlightsState != FlightsState.FLIGHT_TWO_DETAILS);
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
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(STATE_CHANGE_ANIMATION_DURATION);
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

	private FlightsState getFlightsState(ResultsState state) {
		return state != ResultsState.FLIGHTS ? FlightsState.FLIGHT_ONE_FILTERS : mFlightsState;
	}

	private void setTouchState(ResultsState globalState, FlightsState flightsState) {
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

	private void setVisibilityState(ResultsState globalState, FlightsState flightsState) {
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

	private void setFragmentState(ResultsState state, FlightsState flightsState) {

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

		if (state != ResultsState.FLIGHTS && state != ResultsState.DEFAULT) {
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
		public void onStateTransitionPercentageChange(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.DEFAULT && stateTwo == ResultsState.FLIGHTS) {
				mFlightOneFiltersC.setAlpha(1f - percentage);
				mFlightMapC.setAlpha(1f - percentage);
				float filterPaneTopTranslation = percentage
						* mFlightOneListFrag.getTopSpaceListView().getHeaderSpacerHeight();
				mFlightOneFiltersC.setTranslationY(filterPaneTopTranslation);
			}

			if (stateOne == ResultsState.DEFAULT && stateTwo == ResultsState.HOTELS) {
				int colOneDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(1);
				int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);

				mFlightMapC.setTranslationX(colTwoDist * (1f - percentage));
				mFlightOneListC.setTranslationX(colOneDist * (1f - percentage));
			}
		}

		@Override
		public void setHardwareLayerForTransition(ResultsState stateOne, ResultsState stateTwo, int layerType) {
			if ((stateOne == ResultsState.DEFAULT || stateOne == ResultsState.HOTELS)
					&& (stateTwo == ResultsState.DEFAULT || stateTwo == ResultsState.HOTELS)) {
				//Default -> Hotels or Hotels -> Default transition

				mFlightOneListC.setLayerType(layerType, null);

			}

			if ((stateOne == ResultsState.DEFAULT || stateOne == ResultsState.FLIGHTS)
					&& (stateTwo == ResultsState.DEFAULT || stateTwo == ResultsState.FLIGHTS)) {
				//Default -> Flights or Flights -> Default transition

				mFlightOneFiltersC.setLayerType(layerType, null);
			}

		}

		@Override
		public void setVisibilityForTransition(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.DEFAULT || stateTwo == ResultsState.DEFAULT) {
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
		public void onContentSizeUpdated(int totalWidth, int totalHeight) {
			mColumnManager.setTotalWidth(totalWidth);
			mGlobalHeight = totalHeight;

			mColumnManager.setContainerToColumnSpan(mFlightMapC, 0, 2);

			mColumnManager.setContainerToColumn(mFlightOneFiltersC, 0);
			mColumnManager.setContainerToColumn(mFlightOneListC, 1);
			mColumnManager.setContainerToColumnSpan(mFlightOneDetailsC, 0, 2);

			mColumnManager.setContainerToColumn(mFlightTwoFiltersC, 0);
			mColumnManager.setContainerToColumn(mFlightTwoListColumnC, 1);
			mColumnManager.setContainerToColumnSpan(mFlightTwoDetailsC, 0, 2);

			updateDetailsFragSizes(mFlightOneDetailsFrag);
			updateDetailsFragSizes(mFlightTwoDetailsFrag);

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
				if (mFlightsStateAnimator != null) {
					//If we are in the middle of state transition, just reverse it
					setFlightsState(mFlightsState, true);
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
					else if (mFlightsState == FlightsState.ADDING_FLIGHT_TO_TRIP) {
						return true;
					}
				}
			}
			return false;
		}

	};

}
