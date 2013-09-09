package com.expedia.bookings.fragment;

import java.util.ArrayList;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.expedia.bookings.widget.TouchThroughFrameLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.mobiata.android.Log;
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
import android.view.ViewGroup;

/**
 *  TabletResultsHotelControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to HOTELS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsHotelControllerFragment extends Fragment implements SupportMapFragmentListener,
		ITabletResultsController, ISortAndFilterListener, IResultsHotelSelectedListener {

	public interface IHotelsFruitScrollUpListViewChangeListener {
		public void onHotelsStateChanged(State oldState, State newState, float percentage, View requester);

		public void onHotelsPercentageChanged(State state, float percentage);
	}

	private IFruitScrollUpListViewChangeListener mFruitProxy = new IFruitScrollUpListViewChangeListener() {
		@Override
		public void onStateChanged(State oldState, State newState, float percentage) {
			if (mListener != null) {
				mListener.onHotelsStateChanged(oldState, newState, percentage, mHotelListC);
			}
		}

		@Override
		public void onPercentageChanged(State state, float percentage) {
			if (mListener != null) {
				mListener.onHotelsPercentageChanged(state, percentage);
			}
		}
	};

	private enum HotelsState {
		DEFAULT, DEFAULT_FILTERS, ROOMS_AND_RATES, ROOMS_AND_RATES_FILTERS
	}

	//State
	private static final String STATE_HOTELS_STATE = "STATE_HOTELS_STATE";
	private static final String STATE_GLOBAL_STATE = "STATE_GLOBAL_STATE";

	//Tags
	private static final String FRAG_TAG_HOTEL_LIST = "FRAG_TAG_HOTEL_LIST";
	private static final String FRAG_TAG_HOTEL_FILTERS = "FRAG_TAG_HOTEL_FILTERS";
	private static final String FRAG_TAG_HOTEL_FILTERED_COUNT = "FRAG_TAG_HOTEL_FILTERED_COUNT";
	private static final String FRAG_TAG_HOTEL_MAP = "FRAG_TAG_HOTEL_MAP";
	private static final String FRAG_TAG_HOTEL_ROOMS_AND_RATES = "FRAG_TAG_HOTEL_ROOMS_AND_RATES";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mHotelListC;
	private TouchThroughFrameLayout mBgHotelMapC;
	private BlockEventFrameLayout mBgHotelMapTouchDelegateC;
	private BlockEventFrameLayout mHotelFiltersC;
	private BlockEventFrameLayout mHotelFilteredCountC;
	private BlockEventFrameLayout mHotelRoomsAndRatesC;

	//Fragments
	private SupportMapFragment mMapFragment;
	private ResultsHotelListFragment mHotelListFrag;
	private ResultsHotelsFiltersFragment mHotelFiltersFrag;
	private ResultsHotelsFilterCountFragment mHotelFilteredCountFrag;
	private ResultsHotelsRoomsAndRates mHotelRoomsAndRatesFrag;

	//Other
	private GlobalResultsState mGlobalState = GlobalResultsState.DEFAULT;
	private HotelsState mHotelsState = HotelsState.DEFAULT;
	private IHotelsFruitScrollUpListViewChangeListener mListener;
	private ColumnManager mColumnManager = new ColumnManager(3);

	//Animation
	private ValueAnimator mHotelsStateAnimator;
	private HotelsState mDestinationHotelsState;
	private static final int STATE_CHANGE_ANIMATION_DURATION = 200;

	private ArrayList<IResultsHotelSelectedListener> mHotelSelectedListeners = new ArrayList<IResultsHotelSelectedListener>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IHotelsFruitScrollUpListViewChangeListener.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_hotels, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mHotelListC = Ui.findView(view, R.id.column_one_hotel_list);
		mBgHotelMapC = Ui.findView(view, R.id.bg_hotel_map);
		mBgHotelMapTouchDelegateC = Ui.findView(view, R.id.bg_hotel_map_touch_delegate);
		mHotelFiltersC = Ui.findView(view, R.id.column_one_hotel_filters);
		mHotelFilteredCountC = Ui.findView(view, R.id.column_three_hotel_filtered_count);
		mHotelRoomsAndRatesC = Ui.findView(view, R.id.column_two_hotel_rooms_and_rates);

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

	private ValueAnimator getTowardsStateAnimator(HotelsState state) {
		if (state == HotelsState.DEFAULT_FILTERS || state == HotelsState.ROOMS_AND_RATES_FILTERS) {
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

	private void finalizeHotelsState(HotelsState state) {
		switch (state) {
		case DEFAULT:
		case ROOMS_AND_RATES: {
			mHotelListFrag.setListLockedToTop(false);
			setHotelsFiltersShownPercentage(0f);
			mHotelListFrag.setSortAndFilterButtonText(getString(R.string.sort_and_filter));
			break;
		}
		case ROOMS_AND_RATES_FILTERS:
		case DEFAULT_FILTERS: {
			mHotelListFrag.setListLockedToTop(true);
			setHotelsFiltersShownPercentage(1f);
			mHotelListFrag.setSortAndFilterButtonText(getString(R.string.done));
			break;
		}
		}
		mHotelsState = state;
		mDestinationHotelsState = null;
		mHotelsStateAnimator = null;
		setVisibilityState(mGlobalState, state);
		setTouchState(mGlobalState, state);
	}

	private void setHotelsFiltersAnimationHardwareRendering(boolean useHardwareLayer) {
		int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mHotelListC.setLayerType(layerValue, null);
		mHotelFiltersC.setLayerType(layerValue, null);
		mHotelFilteredCountC.setLayerType(layerValue, null);
	}

	private void setHotelsFiltersShownPercentage(float percentage) {
		mHotelListC.setTranslationX(percentage * mColumnManager.getColLeft(1));

		float filtersLeft = mColumnManager.getColLeft(0) - ((1f - percentage) * mColumnManager.getColWidth(0));
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

			if (hotelsState == HotelsState.ROOMS_AND_RATES || hotelsState == HotelsState.ROOMS_AND_RATES_FILTERS) {
				mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			}

			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		case DEFAULT: {
			mHotelListC.setVisibility(View.VISIBLE);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			break;
		}
		default: {
			mHotelListC.setVisibility(View.INVISIBLE);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			break;
		}
		}
	}

	private void setFragmentState(GlobalResultsState globalState, HotelsState hotelsState) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getChildFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

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

		//Hotel list
		setHotelListFragmentAvailability(hotelListAvailable, transaction);

		//Hotel Map
		setHotelsMapFragmentAvailability(hotelMapAvailable, transaction);

		//Hotel filters
		setHotelFiltersFragmentAvailability(hotelFiltersAvailable, transaction);

		//Hotel Filtered count fragment
		setHotelFilteredCountFragmentAvailability(hotelFilteredCountAvailable, transaction);

		//Rooms and rates
		setHotelRoomsAndRatesFragmentAvailability(hotelRoomsAndRatesAvailable, transaction);

		transaction.commit();

	}

	/**
	 * FRAGMENT HELPERS
	 */

	private FragmentTransaction setHotelListFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mHotelListFrag == null || !mHotelListFrag.isAdded()) {
				if (mHotelListFrag == null) {
					mHotelListFrag = (ResultsHotelListFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_HOTEL_LIST);
				}
				if (mHotelListFrag == null) {
					mHotelListFrag = new ResultsHotelListFragment();
				}
				if (!mHotelListFrag.isAdded()) {
					transaction.add(R.id.column_one_hotel_list, mHotelListFrag, FRAG_TAG_HOTEL_LIST);
				}
				mHotelListFrag.setChangeListener(mFruitProxy);
			}
		}
		else {
			if (mHotelListFrag == null) {
				mHotelListFrag = (ResultsHotelListFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_HOTEL_LIST);
			}
			if (mHotelListFrag != null) {
				transaction.remove(mHotelListFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelFiltersFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mHotelFiltersFrag == null || !mHotelFiltersFrag.isAdded()) {
				if (mHotelFiltersFrag == null) {
					mHotelFiltersFrag = (ResultsHotelsFiltersFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_HOTEL_FILTERS);
				}
				if (mHotelFiltersFrag == null) {
					mHotelFiltersFrag = new ResultsHotelsFiltersFragment();
				}
				if (!mHotelFiltersFrag.isAdded()) {
					transaction.add(R.id.column_one_hotel_filters, mHotelFiltersFrag, FRAG_TAG_HOTEL_FILTERS);
				}
			}
		}
		else {
			if (mHotelFiltersFrag == null) {
				mHotelFiltersFrag = (ResultsHotelsFiltersFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_HOTEL_FILTERS);
			}
			if (mHotelFiltersFrag != null) {
				transaction.remove(mHotelFiltersFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelFilteredCountFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mHotelFilteredCountFrag == null || !mHotelFilteredCountFrag.isAdded()) {
				if (mHotelFilteredCountFrag == null) {
					mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) getChildFragmentManager()
							.findFragmentByTag(
									FRAG_TAG_HOTEL_FILTERED_COUNT);
				}
				if (mHotelFilteredCountFrag == null) {
					mHotelFilteredCountFrag = new ResultsHotelsFilterCountFragment();
				}
				if (!mHotelFilteredCountFrag.isAdded()) {
					transaction.add(R.id.column_three_hotel_filtered_count, mHotelFilteredCountFrag,
							FRAG_TAG_HOTEL_FILTERED_COUNT);
				}
			}
		}
		else {
			if (mHotelFilteredCountFrag == null) {
				mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) getChildFragmentManager()
						.findFragmentByTag(
								FRAG_TAG_HOTEL_FILTERED_COUNT);
			}
			if (mHotelFilteredCountFrag != null) {
				transaction.remove(mHotelFilteredCountFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelRoomsAndRatesFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mHotelRoomsAndRatesFrag == null || !mHotelRoomsAndRatesFrag.isAdded()) {
				if (mHotelRoomsAndRatesFrag == null) {
					mHotelRoomsAndRatesFrag = (ResultsHotelsRoomsAndRates) getChildFragmentManager()
							.findFragmentByTag(
									FRAG_TAG_HOTEL_ROOMS_AND_RATES);
				}
				if (mHotelRoomsAndRatesFrag == null) {
					mHotelRoomsAndRatesFrag = ResultsHotelsRoomsAndRates.newInstance();
				}
				if (!mHotelRoomsAndRatesFrag.isAdded()) {
					transaction.add(R.id.column_two_hotel_rooms_and_rates, mHotelRoomsAndRatesFrag,
							FRAG_TAG_HOTEL_ROOMS_AND_RATES);
				}
			}
		}
		else {
			if (mHotelRoomsAndRatesFrag == null) {
				mHotelRoomsAndRatesFrag = (ResultsHotelsRoomsAndRates) getChildFragmentManager()
						.findFragmentByTag(
								FRAG_TAG_HOTEL_ROOMS_AND_RATES);
			}
			if (mHotelRoomsAndRatesFrag != null) {
				transaction.remove(mHotelRoomsAndRatesFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelsMapFragmentAvailability(boolean available, FragmentTransaction transaction) {
		//More initialization in onMapLayout
		if (available) {
			if (mMapFragment == null || !mMapFragment.isAdded()) {

				if (mMapFragment == null) {
					mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAG_TAG_HOTEL_MAP);
				}
				if (mMapFragment == null) {
					mMapFragment = SupportMapFragment.newInstance();
				}
				if (!mMapFragment.isAdded()) {
					transaction.add(R.id.bg_hotel_map, mMapFragment, FRAG_TAG_HOTEL_MAP);
				}
			}
		}
		else {
			if (mMapFragment == null) {
				mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAG_TAG_HOTEL_MAP);
			}
			if (mMapFragment != null) {
				transaction.remove(mMapFragment);
			}
		}
		return transaction;
	}

	/**
	 * MAP LISTENER, WE SHOULD SET THESE COORDINATES TO WHEREVER THE HOTEL SEARCH IS ....
	 */
	@Override
	public void onMapLayout() {
		if (mMapFragment != null) {
			mMapFragment.setInitialCameraPosition(CameraUpdateFactory.newLatLngBounds(
					SupportMapFragment.getAmericaBounds(),
					0));
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
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mHotelListC, 0);
		mColumnManager.setContainerToColumn(mHotelFiltersC, 0);
		mColumnManager.setContainerToColumn(mHotelFilteredCountC, 2);
		mColumnManager.setContainerToColumnSpan(mBgHotelMapC, 0, 2);
		mColumnManager.setContainerToColumnSpan(mBgHotelMapTouchDelegateC, 0, 2);
		mColumnManager.setContainerToColumnSpan(mHotelRoomsAndRatesC, 1, 2);

	}

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
					mHotelListFrag.gotoBottomPosition();
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
		if (mGlobalState == GlobalResultsState.HOTELS) {
			setHotelsState(HotelsState.ROOMS_AND_RATES, true);
			
			for (IResultsHotelSelectedListener listener : mHotelSelectedListeners) {
				listener.onHotelSelected();
			}
		}
	}

}
