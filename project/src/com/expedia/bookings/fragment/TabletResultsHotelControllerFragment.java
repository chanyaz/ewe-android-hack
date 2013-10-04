package com.expedia.bookings.fragment;

import java.util.ArrayList;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.expedia.bookings.widget.TouchThroughFrameLayout;
import com.mobiata.android.util.Ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
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

/**
 *  TabletResultsHotelControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to HOTELS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsHotelControllerFragment extends Fragment implements ITabletResultsController,
		ISortAndFilterListener, IResultsHotelSelectedListener, IAddToTripListener, IFragmentAvailabilityProvider {

	public interface IHotelsFruitScrollUpListViewChangeListener {
		public void onHotelsStateChanged(State oldState, State newState, float percentage, View requester);

		public void onHotelsPercentageChanged(State state, float percentage);
	}

	private IFruitScrollUpListViewChangeListener mFruitProxy = new IFruitScrollUpListViewChangeListener() {
		@Override
		public void onStateChanged(State oldState, State newState, float percentage) {
			if (mFruitListener != null) {
				mFruitListener.onHotelsStateChanged(oldState, newState, percentage, mHotelListC);
			}
		}

		@Override
		public void onPercentageChanged(State state, float percentage) {
			if (mFruitListener != null) {
				mFruitListener.onHotelsPercentageChanged(state, percentage);
			}
		}
	};

	private enum HotelsState {
		DEFAULT, DEFAULT_FILTERS, ROOMS_AND_RATES, ROOMS_AND_RATES_FILTERS, ADDING_HOTEL_TO_TRIP
	}

	//State
	private static final String STATE_HOTELS_STATE = "STATE_HOTELS_STATE";
	private static final String STATE_GLOBAL_STATE = "STATE_GLOBAL_STATE";

	//Frag tags
	private static final String FTAG_HOTEL_LIST = "FTAG_HOTEL_LIST";
	private static final String FTAG_HOTEL_FILTERS = "FTAG_HOTEL_FILTERS";
	private static final String FTAG_HOTEL_FILTERED_COUNT = "FTAG_HOTEL_FILTERED_COUNT";
	private static final String FTAG_HOTEL_MAP = "FTAG_HOTEL_MAP";
	private static final String FTAG_HOTEL_ROOMS_AND_RATES = "FTAG_HOTEL_ROOMS_AND_RATES";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mHotelListC;
	private TouchThroughFrameLayout mBgHotelMapC;
	private BlockEventFrameLayout mBgHotelMapTouchDelegateC;
	private BlockEventFrameLayout mHotelFiltersC;
	private BlockEventFrameLayout mHotelFilteredCountC;
	private BlockEventFrameLayout mHotelRoomsAndRatesC;
	private BlockEventFrameLayout mHotelRoomsAndRatesShadeC;

	// Fragments
	private HotelMapFragment mMapFragment;
	private ResultsHotelListFragment mHotelListFrag;
	private ResultsHotelsFiltersFragment mHotelFiltersFrag;
	private ResultsHotelsFilterCountFragment mHotelFilteredCountFrag;
	private ResultsHotelsRoomsAndRates mHotelRoomsAndRatesFrag;

	//Other
	private GlobalResultsState mGlobalState = GlobalResultsState.DEFAULT;
	private HotelsState mHotelsState = HotelsState.DEFAULT;
	private IHotelsFruitScrollUpListViewChangeListener mFruitListener;
	private IAddToTripListener mParentAddToTripListener;
	private ColumnManager mColumnManager = new ColumnManager(3);
	private int mShadeColor = Color.argb(220, 0, 0, 0);
	private boolean mRoomsAndRatesInFront = true;//They start in front

	//Animation
	private ValueAnimator mHotelsStateAnimator;
	private HotelsState mDestinationHotelsState;
	private static final int STATE_CHANGE_ANIMATION_DURATION = 300;

	private ArrayList<IResultsHotelSelectedListener> mHotelSelectedListeners = new ArrayList<IResultsHotelSelectedListener>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mFruitListener = Ui.findFragmentListener(this, IHotelsFruitScrollUpListViewChangeListener.class, true);
		mParentAddToTripListener = Ui.findFragmentListener(this, IAddToTripListener.class);
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
		mHotelRoomsAndRatesShadeC = Ui.findView(view, R.id.hotel_rooms_and_rates_shade);

		//Set shade color
		mHotelRoomsAndRatesShadeC.setBackgroundColor(mShadeColor);

		//Default maps to be invisible (they get ignored by our setVisibilityState function so this is important)
		mBgHotelMapC.setAlpha(0f);

		//Set up our maps touch passthrough. It is important to note that A) the touch receiver is set to be invisible,
		//so that when it gets a touch, it will pass to whatever is behind it. B) It must be the same size as the
		//the view sending it touch events, because no offsets or anything like that are performed. C) It must be
		//behind the view getting the original touch event, otherwise it will create a loop.
		mBgHotelMapTouchDelegateC.setVisibility(View.INVISIBLE);
		mBgHotelMapC.setTouchPassThroughReceiver(mBgHotelMapTouchDelegateC);

		if (savedInstanceState != null) {
			mGlobalState = GlobalResultsState.valueOf(savedInstanceState.getString(STATE_GLOBAL_STATE,
					GlobalResultsState.DEFAULT.name()));
			mHotelsState = HotelsState.valueOf(savedInstanceState.getString(STATE_HOTELS_STATE,
					HotelsState.DEFAULT.name()));
		}
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_HOTELS_STATE, mHotelsState.name());
		outState.putString(STATE_GLOBAL_STATE, mGlobalState.name());
	}

	/**
	 * HOTELS STATE 
	 */

	private void setHotelsState(HotelsState state, boolean animate) {
		if (!animate) {
			if (mHotelsStateAnimator != null && mHotelsStateAnimator.isStarted()) {
				mHotelsStateAnimator.cancel();
			}
			finalizeHotelsState(state);
		}
		else {
			if (mHotelsStateAnimator == null) {
				mDestinationHotelsState = state;
				mHotelsStateAnimator = getTowardsStateAnimator(state);
				if (mHotelsStateAnimator == null) {
					finalizeHotelsState(state);
				}
				else {
					mHotelsStateAnimator.start();
				}
			}
			else if (mDestinationHotelsState != state) {
				mDestinationHotelsState = state;
				mHotelsStateAnimator.reverse();
			}
		}
	}

	private void setHotelsStateZIndex(HotelsState state) {
		//Calling bringToFront() does a full layout pass, so we DONT want to do this when it is un-needed.

		if (state == HotelsState.ADDING_HOTEL_TO_TRIP) {
			if (!mRoomsAndRatesInFront) {
				mHotelRoomsAndRatesShadeC.bringToFront();
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

	private void finalizeHotelsState(HotelsState state) {
		switch (state) {
		case DEFAULT:
		case ROOMS_AND_RATES: {
			mHotelListFrag.setListLockedToTop(false);
			setHotelsFiltersShownPercentage(0f);
			setAddToTripPercentage(0f);
			mHotelListFrag.setSortAndFilterButtonText(getString(R.string.Sort_and_Filter));
			mHotelListFrag.setSortAndFilterButtonEnabled(true);
			break;
		}
		case ROOMS_AND_RATES_FILTERS:
		case DEFAULT_FILTERS: {
			mHotelListFrag.setListLockedToTop(true);
			setHotelsFiltersShownPercentage(1f);
			setAddToTripPercentage(0f);
			mHotelListFrag.setSortAndFilterButtonEnabled(false);
			break;
		}
		case ADDING_HOTEL_TO_TRIP: {
			mHotelListFrag.setListLockedToTop(true);
			setAddToTripPercentage(1f);
			mParentAddToTripListener.beginAddToTrip(mHotelRoomsAndRatesFrag.getSelectedData(),
					mHotelRoomsAndRatesFrag.getDestinationRect(), mShadeColor);
			doAddToTripDownloadStuff();
			break;
		}
		}
		mHotelsState = state;
		mDestinationHotelsState = null;
		mHotelsStateAnimator = null;
		setVisibilityState(mGlobalState, state);
		setTouchState(mGlobalState, state);
		setHotelsStateZIndex(state);

	}

	private ValueAnimator getTowardsStateAnimator(HotelsState state) {
		if (state == HotelsState.ADDING_HOTEL_TO_TRIP) {
			return prepareAddToTripAnimator();
		}
		else if (state == HotelsState.DEFAULT_FILTERS || state == HotelsState.ROOMS_AND_RATES_FILTERS) {
			//Show the filters
			return prepareFiltersAnimator(true);
		}
		else if (mHotelsState == HotelsState.DEFAULT_FILTERS || mHotelsState == HotelsState.ROOMS_AND_RATES_FILTERS) {
			//Filters were showing, now go somewhere else...
			return prepareFiltersAnimator(false);
		}
		else {
			//TODO: OTHER STATE ANIMATORS
			return null;
		}
	}

	/**
	 * ADD TO TRIP ANIMATIONS
	 */

	private ValueAnimator prepareAddToTripAnimator() {
		ValueAnimator addToTripAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(STATE_CHANGE_ANIMATION_DURATION);
		addToTripAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				setAddToTripPercentage((Float) arg0.getAnimatedValue());
			}

		});
		addToTripAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				setAddToTripAnimationHardwareRendering(false);
				finalizeHotelsState(mDestinationHotelsState);
			}
		});

		setHotelsStateZIndex(HotelsState.ADDING_HOTEL_TO_TRIP);
		mHotelRoomsAndRatesShadeC.setVisibility(View.VISIBLE);
		mHotelListFrag.setListLockedToTop(true);
		setAddToTripAnimationHardwareRendering(true);
		return addToTripAnimator;
	}

	private void setAddToTripAnimationHardwareRendering(boolean useHardwareLayer) {
		int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mHotelRoomsAndRatesShadeC.setLayerType(layerType, null);
		mHotelRoomsAndRatesFrag.setTransitionToAddTripHardwareLayer(layerType);
	}

	private void setAddToTripPercentage(float percentage) {
		if (mHotelRoomsAndRatesFrag != null) {
			mHotelRoomsAndRatesFrag.setTransitionToAddTripPercentage(percentage);
		}
		mHotelRoomsAndRatesShadeC.setAlpha(percentage);
	}

	/**
	 * FILTER ANIMATIONS
	 */

	private ValueAnimator prepareFiltersAnimator(boolean showFilters) {
		float startValue = showFilters ? 0f : 1f;
		final float endValue = showFilters ? 1f : 0f;
		ValueAnimator filtersAnimator = ValueAnimator.ofFloat(startValue, endValue).setDuration(
				STATE_CHANGE_ANIMATION_DURATION);
		filtersAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				setHotelsFiltersShownPercentage((Float) arg0.getAnimatedValue());
			}

		});
		filtersAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				setHotelsFiltersAnimationHardwareRendering(false);
				finalizeHotelsState(mDestinationHotelsState);
			}
		});

		mHotelListFrag.setListLockedToTop(true);
		mHotelFiltersC.setVisibility(View.VISIBLE);
		mHotelFilteredCountC.setVisibility(View.VISIBLE);

		setHotelsFiltersAnimationHardwareRendering(true);
		return filtersAnimator;
	}

	private void setHotelsFiltersAnimationHardwareRendering(boolean useHardwareLayer) {
		int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mHotelFiltersC.setLayerType(layerValue, null);
		mHotelFilteredCountC.setLayerType(layerValue, null);
	}

	private void setHotelsFiltersShownPercentage(float percentage) {
		float filtersLeft = -(1f - percentage) * mColumnManager.getColWidth(1);
		mHotelFiltersC.setTranslationX(filtersLeft);

		float filteredCountLeft = mColumnManager.getColWidth(2) * (1f - percentage);
		mHotelFilteredCountC.setTranslationX(filteredCountLeft);
	}

	/**
	 * STATE HELPERS
	 */

	private void setTouchState(GlobalResultsState globalState, HotelsState hotelsState) {
		switch (globalState) {
		case HOTELS: {
			if (hotelsState == HotelsState.DEFAULT) {
				mBgHotelMapC.setTouchPassThroughEnabled(false);
			}
			else {
				mBgHotelMapC.setTouchPassThroughEnabled(true);
			}

			if (hotelsState == HotelsState.ROOMS_AND_RATES) {
				mHotelRoomsAndRatesC.setBlockNewEventsEnabled(false);
			}
			else {
				mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
			}

			if (hotelsState == HotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesShadeC.setBlockNewEventsEnabled(true);
			}
			else {
				mHotelRoomsAndRatesShadeC.setBlockNewEventsEnabled(false);
			}

			if (hotelsState == HotelsState.DEFAULT_FILTERS || hotelsState == HotelsState.ROOMS_AND_RATES_FILTERS) {
				mHotelFiltersC.setBlockNewEventsEnabled(false);
			}
			else {
				mHotelFiltersC.setBlockNewEventsEnabled(true);
			}

			mHotelListC.setBlockNewEventsEnabled(false);
			break;
		}
		case DEFAULT: {
			mBgHotelMapC.setTouchPassThroughEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(false);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
			break;
		}
		default: {
			mBgHotelMapC.setTouchPassThroughEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(true);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState globalState, HotelsState hotelsState) {
		switch (globalState) {
		case HOTELS: {
			if (hotelsState == HotelsState.DEFAULT_FILTERS || hotelsState == HotelsState.ROOMS_AND_RATES_FILTERS) {
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelFiltersC.setVisibility(View.INVISIBLE);
				mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == HotelsState.ROOMS_AND_RATES || hotelsState == HotelsState.ROOMS_AND_RATES_FILTERS
					|| hotelsState == HotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == HotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesShadeC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
			}

			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		case DEFAULT: {
			mHotelListC.setVisibility(View.VISIBLE);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
			break;
		}
		default: {
			mHotelListC.setVisibility(View.INVISIBLE);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
			break;
		}
		}
	}

	private void setFragmentState(GlobalResultsState globalState, HotelsState hotelsState) {
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = manager.beginTransaction();

		boolean hotelListAvailable = true;
		boolean hotelMapAvailable = true;
		boolean hotelFiltersAvailable = true;
		boolean hotelFilteredCountAvailable = true;
		boolean hotelRoomsAndRatesAvailable = true;

		if (globalState != GlobalResultsState.HOTELS) {
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}
		if (globalState != GlobalResultsState.HOTELS && globalState != GlobalResultsState.DEFAULT) {
			hotelMapAvailable = false;
		}

		mHotelListFrag = (ResultsHotelListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelListAvailable, FTAG_HOTEL_LIST, manager,
				transaction, this, R.id.column_one_hotel_list, false);
		mMapFragment = (HotelMapFragment) FragmentAvailabilityUtils.setFragmentAvailability(hotelMapAvailable,
				FTAG_HOTEL_MAP, manager, transaction, this, R.id.bg_hotel_map, false);
		mHotelFiltersFrag = (ResultsHotelsFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelFiltersAvailable,
				FTAG_HOTEL_FILTERS, manager, transaction, this, R.id.column_two_hotel_filters, false);
		mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelFilteredCountAvailable, FTAG_HOTEL_FILTERED_COUNT, manager, transaction, this,
				R.id.column_three_hotel_filtered_count, false);
		mHotelRoomsAndRatesFrag = (ResultsHotelsRoomsAndRates) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelRoomsAndRatesAvailable,
				FTAG_HOTEL_ROOMS_AND_RATES, manager, transaction, this, R.id.hotel_rooms_and_rates, false);

		transaction.commit();

	}

	/**
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
			frag = mHotelRoomsAndRatesFrag;
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
			frag = ResultsHotelsRoomsAndRates.newInstance();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_HOTEL_LIST) {
			((ResultsListFragment) frag).setChangeListener(mFruitProxy);
		}

	}

	/**
	 * ITabletResultsController Functions
	 */

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mGlobalState = state;

		//TODO: Should we reset to default?
		HotelsState tmpHotelsState = state != GlobalResultsState.HOTELS ? HotelsState.DEFAULT : mHotelsState;
		//HotelsState tmpHotelsState = mHotelsState;

		setTouchState(state, tmpHotelsState);
		setVisibilityState(state, tmpHotelsState);
		setFragmentState(state, tmpHotelsState);

		setHotelsState(tmpHotelsState, false);
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mHotelListC.setVisibility(View.VISIBLE);
			if (mHotelsState == HotelsState.ADDING_HOTEL_TO_TRIP) {
				//We are adding a trip - this call wont happen until we have handed off our shade and
				//view to the trip controller, so we set these to invisible so we dont have both on screen
				mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
				mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
			}
			break;
		}
		case HOTELS: {
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		}
	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.HOTELS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.HOTELS)) {
			//Default -> Hotels or Hotels -> Default transition

			//TODO: This should be carefully considered. Basically we are setting a hardware layer on a mapview
			//and we don't know if it is still drawing or not. We want the alpha fade to be buttery smooth, but
			//if the map is still drawing it will be repainting to the GPU constantly causing performance badness.
			//Profiling on my N10 suggests it is still better to set the hardware layer, but it makes me a little nervous.
			mBgHotelMapC.setLayerType(layerType, null);
			mHotelRoomsAndRatesC.setLayerType(layerType, null);

		}

		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//Default -> Flights or Flights -> Default transition
			mHotelListC.setLayerType(layerType, null);
		}

	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mBgHotelMapC != requester) {
			mBgHotelMapC.setTouchPassThroughEnabled(true);
		}
		if (mHotelListC != requester) {
			mHotelListC.setBlockNewEventsEnabled(true);
		}
		if (mHotelFiltersC != requester) {
			mHotelFiltersC.setBlockNewEventsEnabled(true);
		}
		if (mHotelFilteredCountC != requester) {
			mHotelFilteredCountC.setBlockNewEventsEnabled(true);
		}
		if (mHotelRoomsAndRatesC != requester) {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		mHotelListC.setAlpha(percentage);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		mBgHotelMapC.setAlpha(1f - percentage);
		mHotelRoomsAndRatesC.setAlpha(1f - percentage);
	}

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mHotelListC, 0);
		mColumnManager.setContainerToColumn(mHotelFiltersC, 1);
		mColumnManager.setContainerToColumn(mHotelFilteredCountC, 2);
		mColumnManager.setContainerToColumnSpan(mBgHotelMapC, 0, 2);
		mColumnManager.setContainerToColumnSpan(mBgHotelMapTouchDelegateC, 0, 2);
		mColumnManager.setContainerToColumnSpan(mHotelRoomsAndRatesC, 1, 2);
		mColumnManager.setContainerToColumnSpan(mHotelRoomsAndRatesShadeC, 0, 2);

		//since the actionbar is an overlay, we must compensate by setting the root layout to have a top margin
		int actionBarHeight = getActivity().getActionBar().getHeight();
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mRootC.getLayoutParams();
		params.topMargin = actionBarHeight;
		mRootC.setLayoutParams(params);
	}

	//REMOVE: This is just to mimick locking the back button when we are adding the trip...
	private int mBackTapCount = 0;

	@Override
	public boolean handleBackPressed() {
		if (mGlobalState == GlobalResultsState.HOTELS) {
			if (mHotelsStateAnimator != null) {
				//If we are in the middle of state transition, just reverse it
				this.setHotelsState(mHotelsState, true);
				return true;
			}
			else {
				if (mHotelsState == HotelsState.DEFAULT) {
					mHotelListFrag.gotoBottomPosition(STATE_CHANGE_ANIMATION_DURATION);
					return true;
				}
				else if (mHotelsState == HotelsState.DEFAULT_FILTERS) {
					setHotelsState(HotelsState.DEFAULT, true);
					return true;
				}
				else if (mHotelsState == HotelsState.ROOMS_AND_RATES) {
					setHotelsState(HotelsState.DEFAULT, true);
					return true;
				}
				else if (mHotelsState == HotelsState.ROOMS_AND_RATES_FILTERS) {
					setHotelsState(HotelsState.ROOMS_AND_RATES, true);
					return true;
				}
				else if (mHotelsState == HotelsState.ADDING_HOTEL_TO_TRIP) {
					if (mBackTapCount == 0) {
						Ui.showToast(getActivity(),
								"If we are adding your trip, we probably wont allow back pressing...");
						mBackTapCount++;
					}
					else {
						setHotelsState(HotelsState.ROOMS_AND_RATES, true);
						mBackTapCount = 0;
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ISortAndFilterListener Functions
	 */

	@Override
	public void onSortAndFilterClicked() {
		if (mHotelsState == HotelsState.DEFAULT) {
			setHotelsState(HotelsState.DEFAULT_FILTERS, true);
		}
		else if (mHotelsState == HotelsState.DEFAULT_FILTERS) {
			setHotelsState(HotelsState.DEFAULT, true);
		}
		else if (mHotelsState == HotelsState.ROOMS_AND_RATES) {
			setHotelsState(HotelsState.ROOMS_AND_RATES_FILTERS, true);
		}
		else if (mHotelsState == HotelsState.ROOMS_AND_RATES_FILTERS) {
			setHotelsState(HotelsState.ROOMS_AND_RATES, true);
		}
	}

	/**
	 * IResultsHotelSelectedListener Functions
	 */

	@Override
	public void onHotelSelected() {
		mMapFragment.onHotelSelected();
		if (mGlobalState == GlobalResultsState.HOTELS) {
			setHotelsState(HotelsState.ROOMS_AND_RATES, true);

			for (IResultsHotelSelectedListener listener : mHotelSelectedListeners) {
				listener.onHotelSelected();
			}
		}
	}

	/**
	 * IAddToTripListener Functions
	 */

	@Override
	public void beginAddToTrip(Object data, Rect globalCoordinates, int shadeColor) {
		setHotelsState(HotelsState.ADDING_HOTEL_TO_TRIP, true);
	}

	@Override
	public void performTripHandoff() {
		mParentAddToTripListener.performTripHandoff();
		mHotelListFrag.gotoBottomPosition(STATE_CHANGE_ANIMATION_DURATION);
	}

	/**
	 * ADD TO TRIP DOWNLOAD....
	 */
	//NOTE THIS IS JUST A PLACEHOLDER SO THAT WE GET THE FLOW IDEA
	private Runnable mDownloadRunner;

	private void doAddToTripDownloadStuff() {
		if (mDownloadRunner == null) {
			mDownloadRunner = new Runnable() {
				@Override
				public void run() {
					if (getActivity() != null) {
						performTripHandoff();
					}
					mDownloadRunner = null;
				}
			};
			mRootC.postDelayed(mDownloadRunner, 3000);
		}
	}

}
