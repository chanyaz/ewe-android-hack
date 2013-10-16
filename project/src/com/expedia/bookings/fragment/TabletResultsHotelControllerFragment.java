package com.expedia.bookings.fragment;

import java.util.ArrayList;

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

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsHotelSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.maps.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.expedia.bookings.widget.TouchThroughFrameLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.mobiata.android.util.Ui;

/**
 *  TabletResultsHotelControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to HOTELS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsHotelControllerFragment extends Fragment implements
		ISortAndFilterListener, IResultsHotelSelectedListener, IAddToTripListener, IFragmentAvailabilityProvider,
		HotelMapFragmentListener, SupportMapFragmentListener, IBackManageable, IStateProvider<ResultsHotelsState> {

	public interface IHotelsFruitScrollUpListViewChangeListener {
		public void onHotelsStateChanged(State oldState, State newState, float percentage, View requester);

		public void onHotelsPercentageChanged(State state, float percentage);
	}

	private IFruitScrollUpListViewChangeListener mFruitProxy = new IFruitScrollUpListViewChangeListener() {
		@Override
		public void onStateChanged(State oldState, State newState, float percentage) {
			if(mFruitListener != null) {
				mFruitListener.onHotelsStateChanged(oldState, newState, percentage, mHotelListC);
			}
		}

		@Override
		public void onPercentageChanged(State state, float percentage) {
			if(mFruitListener != null) {
				mFruitListener.onHotelsPercentageChanged(state, percentage);
			}
		}
	};

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
	private ResultsState mGlobalState = ResultsState.DEFAULT;
	private ResultsHotelsState mHotelsState = ResultsHotelsState.DEFAULT;
	private IHotelsFruitScrollUpListViewChangeListener mFruitListener;
	private IAddToTripListener mParentAddToTripListener;
	private ColumnManager mColumnManager = new ColumnManager(3);
	private int mShadeColor = Color.argb(220, 0, 0, 0);
	private boolean mRoomsAndRatesInFront = true;//They start in front

	//Animation
	private ValueAnimator mHotelsStateAnimator;
	private ResultsHotelsState mDestinationHotelsState;
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

		if(savedInstanceState != null) {
			mGlobalState = ResultsState.valueOf(savedInstanceState.getString(STATE_GLOBAL_STATE,
					ResultsState.DEFAULT.name()));
			mHotelsState = ResultsHotelsState.valueOf(savedInstanceState.getString(STATE_HOTELS_STATE,
					ResultsHotelsState.DEFAULT.name()));
		}

		registerStateListener(mHotelsStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsHotelsState>(), false);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_HOTELS_STATE, mHotelsState.name());
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

	/**
	 * HOTELS STATE
	 */

	private void setHotelsState(ResultsHotelsState state, boolean animate) {
		if(!animate) {
			if(mHotelsStateAnimator != null && mHotelsStateAnimator.isStarted()) {
				mHotelsStateAnimator.cancel();
			}
			finalizeState(state);
		}
		else {
			if(mHotelsStateAnimator == null) {
				mDestinationHotelsState = state;
				mHotelsStateAnimator = getTowardsStateAnimator(state);
				if(mHotelsStateAnimator == null) {
					finalizeState(state);
				}
				else {
					mHotelsStateAnimator.start();
				}
			}
			else if(mDestinationHotelsState != state) {
				mDestinationHotelsState = state;
				mHotelsStateAnimator.reverse();
			}
		}
	}

	private ValueAnimator getTowardsStateAnimator(final ResultsHotelsState state) {
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(STATE_CHANGE_ANIMATION_DURATION);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				updateStateTransition(mHotelsState, state, (Float) arg0.getAnimatedValue());
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				if(getActivity() != null) {
					startStateTransition(mHotelsState, state);
				}
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				if(getActivity() != null) {
					endStateTransition(mHotelsState, state);
					finalizeState(mDestinationHotelsState);
				}
			}
		});

		return animator;
	}

	/**
	 * STATE HELPERS
	 */

	private ResultsHotelsState getHotelsState(ResultsState state) {
		return state != ResultsState.HOTELS ? ResultsHotelsState.DEFAULT : mHotelsState;
	}

	private void setTouchState(ResultsState globalState, ResultsHotelsState hotelsState) {
		switch (globalState) {
		case HOTELS: {
			if(hotelsState == ResultsHotelsState.DEFAULT) {
				mBgHotelMapC.setTouchPassThroughEnabled(false);
			}
			else {
				mBgHotelMapC.setTouchPassThroughEnabled(true);
			}

			if(hotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
				mHotelRoomsAndRatesC.setBlockNewEventsEnabled(false);
			}
			else {
				mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
			}

			if(hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesShadeC.setBlockNewEventsEnabled(true);
			}
			else {
				mHotelRoomsAndRatesShadeC.setBlockNewEventsEnabled(false);
			}

			if(hotelsState == ResultsHotelsState.DEFAULT_FILTERS
					|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
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

	private void setVisibilityState(ResultsState globalState, ResultsHotelsState hotelsState) {
		switch (globalState) {
		case HOTELS: {
			if(hotelsState == ResultsHotelsState.DEFAULT_FILTERS
					|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelFiltersC.setVisibility(View.INVISIBLE);
				mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			}

			if(hotelsState == ResultsHotelsState.ROOMS_AND_RATES
					|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS
					|| hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			}

			if(hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
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

	private void setFragmentState(ResultsState globalState, ResultsHotelsState hotelsState) {
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

		if(globalState != ResultsState.HOTELS) {
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}
		if(globalState != ResultsState.HOTELS && globalState != ResultsState.DEFAULT) {
			hotelMapAvailable = false;
		}

		mHotelListFrag = (ResultsHotelListFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelListAvailable, FTAG_HOTEL_LIST, manager,
				transaction, this, R.id.column_one_hotel_list, false);
		mHotelFiltersFrag = (ResultsHotelsFiltersFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelFiltersAvailable,
				FTAG_HOTEL_FILTERS, manager, transaction, this, R.id.column_two_hotel_filters, false);
		mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelFilteredCountAvailable, FTAG_HOTEL_FILTERED_COUNT, manager, transaction, this,
				R.id.column_three_hotel_filtered_count, false);
		mMapFragment = (HotelMapFragment) FragmentAvailabilityUtils.setFragmentAvailability(hotelMapAvailable,
				FTAG_HOTEL_MAP, manager, transaction, this, R.id.bg_hotel_map, false);
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
		if(tag == FTAG_HOTEL_LIST) {
			frag = mHotelListFrag;
		}
		else if(tag == FTAG_HOTEL_FILTERS) {
			frag = mHotelFiltersFrag;
		}
		else if(tag == FTAG_HOTEL_FILTERED_COUNT) {
			frag = mHotelFilteredCountFrag;
		}
		else if(tag == FTAG_HOTEL_MAP) {
			frag = mMapFragment;
		}
		else if(tag == FTAG_HOTEL_ROOMS_AND_RATES) {
			frag = mHotelRoomsAndRatesFrag;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if(tag == FTAG_HOTEL_LIST) {
			frag = new ResultsHotelListFragment();
		}
		else if(tag == FTAG_HOTEL_FILTERS) {
			frag = new ResultsHotelsFiltersFragment();
		}
		else if(tag == FTAG_HOTEL_FILTERED_COUNT) {
			frag = new ResultsHotelsFilterCountFragment();
		}
		else if(tag == FTAG_HOTEL_MAP) {
			frag = HotelMapFragment.newInstance();
		}
		else if(tag == FTAG_HOTEL_ROOMS_AND_RATES) {
			frag = ResultsHotelsRoomsAndRates.newInstance();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if(tag == FTAG_HOTEL_LIST) {
			ResultsHotelListFragment listFrag = (ResultsHotelListFragment) frag;
			listFrag.setChangeListener(mFruitProxy);
		}
		else if(tag == FTAG_HOTEL_MAP) {
			HotelMapFragment mapFrag = (HotelMapFragment) frag;
			updateMapFragmentPositioningInfo(mapFrag);
		}
	}

	private void updateMapFragmentPositioningInfo(HotelMapFragment mapFrag) {
		if(mapFrag != null && mColumnManager.getTotalWidth() > 0) {
			mapFrag.setResultsViewWidth(mColumnManager.getColWidth(0));
			mapFrag.setFilterViewWidth(mColumnManager.getColLeft(2));
			if(mapFrag.isReady()) {
				mapFrag.notifySearchComplete();
			}
		}
	}

	/**
	 * ISortAndFilterListener Functions
	 */

	@Override
	public void onSortAndFilterClicked() {
		if(mHotelsState == ResultsHotelsState.DEFAULT) {
			setHotelsState(ResultsHotelsState.DEFAULT_FILTERS, true);
		}
		else if(mHotelsState == ResultsHotelsState.DEFAULT_FILTERS) {
			setHotelsState(ResultsHotelsState.DEFAULT, true);
		}
		else if(mHotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES_FILTERS, true);
		}
		else if(mHotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
		}
	}

	/**
	 * IResultsHotelSelectedListener Functions
	 */

	@Override
	public void onHotelSelected() {
		mMapFragment.onHotelSelected();
		if(mGlobalState == ResultsState.HOTELS) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, false);//we dont animate because there is not animation for this...

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
		setHotelsState(ResultsHotelsState.ADDING_HOTEL_TO_TRIP, true);
	}

	@Override
	public void performTripHandoff() {
		mParentAddToTripListener.performTripHandoff();
		mHotelListFrag.gotoBottomPosition(STATE_CHANGE_ANIMATION_DURATION);
	}

	/*
	 * ADD TO TRIP DOWNLOAD....
	 */
	//NOTE THIS IS JUST A PLACEHOLDER SO THAT WE GET THE FLOW IDEA
	private Runnable mDownloadRunner;

	private void doAddToTripDownloadStuff() {
		if(mDownloadRunner == null) {
			mDownloadRunner = new Runnable() {
				@Override
				public void run() {
					if(getActivity() != null) {
						performTripHandoff();
					}
					mDownloadRunner = null;
				}
			};
			mRootC.postDelayed(mDownloadRunner, 3000);
		}
	}

	/*
	 * HotelMapFragmentListener
	 */

	@Override
	public void onMapClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPropertyClicked(Property property) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onExactLocationClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPropertyBubbleClicked(Property property) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHotelMapFragmentAttached(HotelMapFragment fragment) {
		fragment.setInitialCameraPosition(CameraUpdateFactory.newLatLngBounds(HotelMapFragment.getAmericaBounds(), 0));
		this.mMapFragment = fragment;
	}

	/*
	 * SupportMapFragmentListener
	 */

	@Override
	public void onMapLayout() {
		mMapFragment.setShowInfoWindow(false);
		updateMapFragmentPositioningInfo(mMapFragment);
	}

	/*
	 * RESULTS STATE LISTENER
	 */

	private StateListenerHelper mStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			//Touch
			setTouchable(false);

			//Visibilities
			if(stateOne == ResultsState.DEFAULT || stateTwo == ResultsState.DEFAULT) {

				mHotelListC.setVisibility(View.VISIBLE);
				if(mHotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
					//We are adding a trip - this call wont happen until we have handed off our shade and
					//view to the trip controller, so we set these to invisible so we dont have both on screen
					mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
					mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
				}

			}

			if(stateOne == ResultsState.HOTELS || stateTwo == ResultsState.HOTELS) {
				mHotelListC.setVisibility(View.VISIBLE);
			}

			//Layer types
			int layerType = View.LAYER_TYPE_HARDWARE;
			if((stateOne == ResultsState.DEFAULT || stateOne == ResultsState.HOTELS)
					&& (stateTwo == ResultsState.DEFAULT || stateTwo == ResultsState.HOTELS)) {
				//Default -> Hotels or Hotels -> Default transition
				mHotelRoomsAndRatesC.setLayerType(layerType, null);
			}
			if((stateOne == ResultsState.DEFAULT || stateOne == ResultsState.FLIGHTS)
					&& (stateTwo == ResultsState.DEFAULT || stateTwo == ResultsState.FLIGHTS)) {
				//Default -> Flights or Flights -> Default transition
				mHotelListC.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if(stateOne == ResultsState.DEFAULT && stateTwo == ResultsState.FLIGHTS) {
				mHotelListC.setAlpha(percentage);
			}

			if(stateOne == ResultsState.DEFAULT && stateTwo == ResultsState.HOTELS) {
				mBgHotelMapC.setAlpha(1f - percentage);
				mHotelRoomsAndRatesC.setAlpha(1f - percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			//Touch
			setTouchable(true);

			//Layer types
			int layerType = View.LAYER_TYPE_SOFTWARE;
			if((stateOne == ResultsState.DEFAULT || stateOne == ResultsState.HOTELS)
					&& (stateTwo == ResultsState.DEFAULT || stateTwo == ResultsState.HOTELS)) {
				//Default -> Hotels or Hotels -> Default transition
				mHotelRoomsAndRatesC.setLayerType(layerType, null);
			}
			if((stateOne == ResultsState.DEFAULT || stateOne == ResultsState.FLIGHTS)
					&& (stateTwo == ResultsState.DEFAULT || stateTwo == ResultsState.FLIGHTS)) {
				//Default -> Flights or Flights -> Default transition
				mHotelListC.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateFinalized(ResultsState state) {
			mGlobalState = state;

			//TODO: Should we reset to default?
			ResultsHotelsState tmpHotelsState = state != ResultsState.HOTELS ? ResultsHotelsState.DEFAULT
					: mHotelsState;
			//HotelsState tmpHotelsState = mHotelsState;

			setTouchState(state, tmpHotelsState);
			setVisibilityState(state, tmpHotelsState);
			setFragmentState(state, tmpHotelsState);

			setHotelsState(tmpHotelsState, false);
		}

		private void setTouchable(boolean touchable) {
			mBgHotelMapC.setTouchPassThroughEnabled(touchable);
			mHotelListC.setBlockNewEventsEnabled(touchable);
			mHotelFiltersC.setBlockNewEventsEnabled(touchable);
			mHotelFilteredCountC.setBlockNewEventsEnabled(touchable);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(touchable);
		}

	};

	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight) {
			mColumnManager.setTotalWidth(totalWidth);

			//Tell all of the containers where they belong
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

			//tell the map where its bounds are
			updateMapFragmentPositioningInfo(mMapFragment);
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
			if(mGlobalState == ResultsState.HOTELS) {
				if(mHotelsStateAnimator != null) {
					//If we are in the middle of state transition, just reverse it
					setHotelsState(mHotelsState, true);
					return true;
				}
				else {
					if(mHotelsState == ResultsHotelsState.DEFAULT) {
						mHotelListFrag.gotoBottomPosition(STATE_CHANGE_ANIMATION_DURATION);
						return true;
					}
					else if(mHotelsState == ResultsHotelsState.DEFAULT_FILTERS) {
						setHotelsState(ResultsHotelsState.DEFAULT, true);
						return true;
					}
					else if(mHotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
						setHotelsState(ResultsHotelsState.DEFAULT, false);
						return true;
					}
					else if(mHotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
						setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
						return true;
					}
					else if(mHotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
						//We return true here, because we want to block back presses in this state
						return true;
					}
				}
			}
			return false;
		}

	};

	/*
	 * HOTELS STATE PROVIDER
	 */
	private ArrayList<IStateListener<ResultsHotelsState>> mStateChangeListeners = new ArrayList<IStateListener<ResultsHotelsState>>();

	@Override
	public void startStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
		for (IStateListener<ResultsHotelsState> listener : mStateChangeListeners) {
			listener.onStateTransitionStart(stateOne, stateTwo);
		}
	}

	@Override
	public void updateStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo,
			float percentage) {
		for (IStateListener<ResultsHotelsState> listener : mStateChangeListeners) {
			listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
		}

	}

	@Override
	public void endStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
		for (IStateListener<ResultsHotelsState> listener : mStateChangeListeners) {
			listener.onStateTransitionEnd(stateOne, stateTwo);
		}
	}

	@Override
	public void finalizeState(ResultsHotelsState state) {
		for (IStateListener<ResultsHotelsState> listener : mStateChangeListeners) {
			listener.onStateFinalized(state);
		}
	}

	@Override
	public void registerStateListener(IStateListener<ResultsHotelsState> listener, boolean fireFinalizeState) {
		mStateChangeListeners.add(listener);
		if(fireFinalizeState) {
			listener.onStateFinalized(getHotelsState(mGlobalState));
		}
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsHotelsState> listener) {
		mStateChangeListeners.remove(listener);

	}

	/*
	 * HOTELS STATE LISTENER
	 */

	private StateListenerHelper<ResultsHotelsState> mHotelsStateHelper = new StateListenerHelper<ResultsHotelsState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			if(stateTwo == ResultsHotelsState.DEFAULT_FILTERS || stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if(stateOne == ResultsHotelsState.DEFAULT_FILTERS
					|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if(stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				mHotelListFrag.setListLockedToTop(true);
				setAddToTripAnimationVis(true);
				setAddToTripAnimationHardwareRendering(true);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsState stateOne, ResultsHotelsState stateTwo, float percentage) {
			if(stateTwo == ResultsHotelsState.DEFAULT_FILTERS || stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				setHotelsFiltersShownPercentage(percentage);

			}
			else if(stateOne == ResultsHotelsState.DEFAULT_FILTERS
					|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				setHotelsFiltersShownPercentage(1f - percentage);

			}
			else if(stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				setAddToTripPercentage(percentage);
			}

		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			if(stateTwo == ResultsHotelsState.DEFAULT_FILTERS || stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if(stateOne == ResultsHotelsState.DEFAULT_FILTERS
					|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if(stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				setAddToTripAnimationVis(false);
				setAddToTripAnimationHardwareRendering(false);
			}

		}

		@Override
		public void onStateFinalized(ResultsHotelsState state) {
			switch (state) {
			case DEFAULT:
			case ROOMS_AND_RATES: {
				mHotelListFrag.setListLockedToTop(false);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				mHotelListFrag.setTopRightTextButtonText(getString(R.string.Sort_and_Filter));
				mHotelListFrag.setTopRightTextButtonEnabled(true);
				break;
			}
			case ROOMS_AND_RATES_FILTERS:
			case DEFAULT_FILTERS: {
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersShownPercentage(1f);
				setAddToTripPercentage(0f);
				mHotelListFrag.setTopRightTextButtonEnabled(false);
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

			if(mMapFragment != null) {
				mMapFragment.setMapPaddingFromFilterState(state == ResultsHotelsState.DEFAULT_FILTERS
						|| state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS);
			}

			mHotelsState = state;
			mDestinationHotelsState = null;
			mHotelsStateAnimator = null;
			setVisibilityState(mGlobalState, state);
			setTouchState(mGlobalState, state);
			setHotelsStateZIndex(state);

		}

		/*
		 * SHOW FILTERS ANIMATION STUFF
		 */
		private void setHotelsFiltersAnimationVisibilities(boolean start) {
			mHotelFiltersC.setVisibility(View.VISIBLE);
			mHotelFilteredCountC.setVisibility(View.VISIBLE);
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

		/*
		 * ADD TO TRIP ANIMATION STUFF
		 */
		private void setAddToTripAnimationVis(boolean start) {
			mHotelRoomsAndRatesShadeC.setVisibility(View.VISIBLE);
		}

		private void setAddToTripAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerType = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelRoomsAndRatesShadeC.setLayerType(layerType, null);
			mHotelRoomsAndRatesFrag.setTransitionToAddTripHardwareLayer(layerType);
		}

		private void setAddToTripPercentage(float percentage) {
			if(mHotelRoomsAndRatesFrag != null) {
				mHotelRoomsAndRatesFrag.setTransitionToAddTripPercentage(percentage);
			}
			mHotelRoomsAndRatesShadeC.setAlpha(percentage);
		}

		/*
		 * HOTEL STATE HELPERS
		 */
		private void setHotelsStateZIndex(ResultsHotelsState state) {
			//Calling bringToFront() does a full layout pass, so we DONT want to do this when it is un-needed.

			if(state == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				if(!mRoomsAndRatesInFront) {
					mHotelRoomsAndRatesShadeC.bringToFront();
					mHotelRoomsAndRatesC.bringToFront();
					mRoomsAndRatesInFront = true;
				}
			}
			else if(mRoomsAndRatesInFront) {
				mHotelFiltersC.bringToFront();
				mHotelFilteredCountC.bringToFront();
				mHotelListC.bringToFront();
				mRoomsAndRatesInFront = false;
			}
		}
	};
}
