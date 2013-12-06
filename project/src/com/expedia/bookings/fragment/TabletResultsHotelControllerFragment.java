package com.expedia.bookings.fragment;

import java.util.ArrayList;

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
import com.expedia.bookings.enums.ResultsHotelsListState;
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
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.maps.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
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
	private ResultsHotelDetailsFragment mHotelDetailsFrag;

	//Other
	//private ResultsState mGlobalState = ResultsState.OVERVIEW;
	private StateManager<ResultsHotelsState> mHotelsStateManager = new StateManager<ResultsHotelsState>(
			ResultsHotelsState.HOTEL_LIST_DOWN, this);
	private IAddToTripListener mParentAddToTripListener;
	private GridManager mGrid = new GridManager();
	private int mShadeColor = Color.argb(220, 0, 0, 0);
	private boolean mRoomsAndRatesInFront = true;//They start in front

	private ArrayList<IResultsHotelSelectedListener> mHotelSelectedListeners = new ArrayList<IResultsHotelSelectedListener>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

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
			mHotelsStateManager.setDefaultState(ResultsHotelsState.valueOf(savedInstanceState.getString(
					STATE_HOTELS_STATE,
					ResultsHotelsState.HOTEL_LIST_DOWN.name())));
		}

		registerStateListener(mHotelsStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsHotelsState>(), false);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_HOTELS_STATE, mHotelsStateManager.getState().name());
	}

	@Override
	public void onResume() {
		super.onResume();
		mResultsStateHelper.registerWithProvider(this);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mResultsStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
	}

	/**
	 * STATE HELPERS
	 */

	public void setHotelsState(ResultsHotelsState state, boolean animate) {
		mHotelsStateManager.setState(state, animate);
	}

	public ResultsHotelsState getHotelsState(ResultsState state) {
		return state != ResultsState.HOTELS ? ResultsHotelsState.HOTEL_LIST_DOWN : mHotelsStateManager
				.getState();
	}

	private void setTouchState(ResultsHotelsState hotelsState) {
		mHotelListC.setBlockNewEventsEnabled(false);

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			mBgHotelMapC.setTouchPassThroughEnabled(true);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
		}
		else {
			mBgHotelMapC.setTouchPassThroughEnabled(false);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES) {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(true);
		}

		if (hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
			mHotelRoomsAndRatesShadeC.setBlockNewEventsEnabled(true);
		}
		else {
			mHotelRoomsAndRatesShadeC.setBlockNewEventsEnabled(false);
		}

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
				|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
			mHotelFiltersC.setBlockNewEventsEnabled(false);
		}
		else {
			mHotelFiltersC.setBlockNewEventsEnabled(true);
		}
	}

	private void setVisibilityState(ResultsHotelsState hotelsState) {
		mHotelListC.setVisibility(View.VISIBLE);
		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			mHotelListC.setVisibility(View.VISIBLE);
			mHotelFiltersC.setVisibility(View.INVISIBLE);
			mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
		}
		else {
			if (hotelsState == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelFiltersC.setVisibility(View.INVISIBLE);
				mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == ResultsHotelsState.ROOMS_AND_RATES
					|| hotelsState == ResultsHotelsState.ROOMS_AND_RATES_FILTERS
					|| hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesC.setVisibility(View.INVISIBLE);
			}

			if (hotelsState == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				mHotelRoomsAndRatesShadeC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelRoomsAndRatesShadeC.setVisibility(View.INVISIBLE);
			}
			mHotelListC.setVisibility(View.VISIBLE);
		}
	}

	private void setFragmentState(ResultsHotelsState hotelsState) {
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

		if (hotelsState == ResultsHotelsState.HOTEL_LIST_DOWN) {
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
			hotelRoomsAndRatesAvailable = false;
		}

		//TODO: WE MAY WANT TO REMOVE SOME HEAVIER FRAGMENTS SOMETIMES, ESPECIALLY IF WE ARE IN FLIGHTS MODE OR SOMETHING

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
		mHotelDetailsFrag = (ResultsHotelDetailsFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				hotelRoomsAndRatesAvailable,
				FTAG_HOTEL_ROOMS_AND_RATES, manager, transaction, this, R.id.hotel_rooms_and_rates, false);

		transaction.commit();
	}

	/*
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
			frag = mHotelDetailsFrag;
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
			frag = ResultsHotelDetailsFragment.newInstance();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_HOTEL_LIST) {
			ResultsHotelListFragment listFrag = (ResultsHotelListFragment) frag;
			listFrag.registerStateListener(mListStateHelper, false);
		}
		else if (tag == FTAG_HOTEL_MAP) {
			HotelMapFragment mapFrag = (HotelMapFragment) frag;
			updateMapFragmentPositioningInfo(mapFrag);
		}
	}

	private void updateMapFragmentPositioningInfo(HotelMapFragment mapFrag) {
		if (mapFrag != null && mGrid.getTotalWidth() > 0) {
			mapFrag.setResultsViewWidth(mGrid.getColWidth(0));
			mapFrag.setFilterViewWidth(mGrid.getColLeft(2));
			if (mapFrag.isReady()) {
				mapFrag.notifySearchComplete();
			}
		}
	}

	/*
	 * ISortAndFilterListener Functions
	 */

	@Override
	public void onSortAndFilterClicked() {
		ResultsHotelsState state = mHotelsStateManager.getState();
		if (state == ResultsHotelsState.HOTEL_LIST_UP) {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_AND_FILTERS, true);
		}
		else if (state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
			setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
		}
		else if (state == ResultsHotelsState.ROOMS_AND_RATES) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES_FILTERS, true);
		}
		else if (state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
			setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
		}
	}

	/*
	 * IResultsHotelSelectedListener Functions
	 */

	@Override
	public void onHotelSelected() {
		mMapFragment.onHotelSelected();
		mHotelDetailsFrag.onHotelSelected();
		setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
		for (IResultsHotelSelectedListener listener : mHotelSelectedListeners) {
			listener.onHotelSelected();
		}
	}

	/*
	 * IAddToTripListener Functions
	 */

	@Override
	public void beginAddToTrip(Object data, Rect globalCoordinates, int shadeColor) {
		setHotelsState(ResultsHotelsState.ADDING_HOTEL_TO_TRIP, true);
	}

	@Override
	public void performTripHandoff() {
		mParentAddToTripListener.performTripHandoff();
		mHotelListFrag.gotoBottomPosition(StateManager.STATE_CHANGE_ANIMATION_DURATION);
	}

	/*
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
	 * LIST STATE LISTENER
	 */

	private StateListenerHelper<ResultsHotelsListState> mListStateHelper = new StateListenerHelper<ResultsHotelsListState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
			startStateTransition(getHotelsStateFromListState(stateOne), getHotelsStateFromListState(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo,
				float percentage) {
			updateStateTransition(getHotelsStateFromListState(stateOne), getHotelsStateFromListState(stateTwo),
					percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsListState stateOne, ResultsHotelsListState stateTwo) {
			endStateTransition(getHotelsStateFromListState(stateOne), getHotelsStateFromListState(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsHotelsListState state) {
			finalizeState(getHotelsStateFromListState(state));
		}

		private ResultsHotelsState getHotelsStateFromListState(ResultsHotelsListState state) {
			if (state == ResultsHotelsListState.HOTELS_LIST_AT_TOP) {
				return ResultsHotelsState.HOTEL_LIST_UP;
			}
			else if (state == ResultsHotelsListState.HOTELS_LIST_AT_BOTTOM) {
				return ResultsHotelsState.HOTEL_LIST_DOWN;
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
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				startStateTransition(ResultsHotelsState.HOTEL_LIST_DOWN, ResultsHotelsState.HOTEL_LIST_UP);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				startStateTransition(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.HOTEL_LIST_DOWN);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				updateStateTransition(ResultsHotelsState.HOTEL_LIST_DOWN, ResultsHotelsState.HOTEL_LIST_UP, percentage);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				updateStateTransition(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.HOTEL_LIST_DOWN, percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				endStateTransition(ResultsHotelsState.HOTEL_LIST_DOWN, ResultsHotelsState.HOTEL_LIST_UP);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				endStateTransition(ResultsHotelsState.HOTEL_LIST_UP, ResultsHotelsState.HOTEL_LIST_DOWN);
			}
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			if (state != ResultsState.HOTELS) {
				setHotelsState(ResultsHotelsState.HOTEL_LIST_DOWN, false);
			}
			else {
				setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, false);
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

			if (mGrid.isLandscape()) {
				mGrid.setGridSize(1, 3);

				//Tell all of the containers where they belong
				mGrid.setContainerToColumn(mHotelListC, 0);
				mGrid.setContainerToColumn(mHotelFiltersC, 1);
				mGrid.setContainerToColumn(mHotelFilteredCountC, 2);
				mGrid.setContainerToColumnSpan(mBgHotelMapC, 0, 2);
				mGrid.setContainerToColumnSpan(mBgHotelMapTouchDelegateC, 0, 2);
				mGrid.setContainerToColumnSpan(mHotelRoomsAndRatesC, 1, 2);
				mGrid.setContainerToColumnSpan(mHotelRoomsAndRatesShadeC, 0, 2);
			}
			else {
				mGrid.setGridSize(2, 2);

				mGrid.setContainerToColumn(mHotelListC, 0);
				mGrid.setContainerToColumn(mHotelFiltersC, 1);
				mGrid.setContainerToColumn(mHotelFilteredCountC, 0);
				mGrid.setContainerToColumnSpan(mBgHotelMapC, 0, 1);
				mGrid.setContainerToColumnSpan(mBgHotelMapTouchDelegateC, 0, 1);
				mGrid.setContainerToColumnSpan(mHotelRoomsAndRatesC, 0, 1);
				mGrid.setContainerToColumnSpan(mHotelRoomsAndRatesShadeC, 0, 1);
			}
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
			ResultsHotelsState state = mHotelsStateManager.getState();
			if (state != ResultsHotelsState.HOTEL_LIST_DOWN) {
				if (mHotelsStateManager.isAnimating()) {
					//If we are in the middle of state transition, just reverse it
					setHotelsState(state, true);
					return true;
				}
				else {
					if (state == ResultsHotelsState.HOTEL_LIST_UP) {
						mHotelListFrag.gotoBottomPosition(StateManager.STATE_CHANGE_ANIMATION_DURATION);
						return true;
					}
					else if (state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						return true;
					}
					else if (state == ResultsHotelsState.ROOMS_AND_RATES) {
						setHotelsState(ResultsHotelsState.HOTEL_LIST_UP, true);
						return true;
					}
					else if (state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
						setHotelsState(ResultsHotelsState.ROOMS_AND_RATES, true);
						return true;
					}
					else if (state == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
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
	private StateListenerCollection<ResultsHotelsState> mHotelsStateListeners = new StateListenerCollection<ResultsHotelsState>(
			mHotelsStateManager.getState());

	@Override
	public void startStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
		mHotelsStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo,
			float percentage) {
		mHotelsStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
		mHotelsStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsHotelsState state) {
		mHotelsStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsHotelsState> listener, boolean fireFinalizeState) {
		mHotelsStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsHotelsState> listener) {
		mHotelsStateListeners.unRegisterStateListener(listener);
	}

	/*
	 * HOTELS STATE LISTENER
	 */

	private StateListenerHelper<ResultsHotelsState> mHotelsStateHelper = new StateListenerHelper<ResultsHotelsState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {

			setTouchable(false, stateTwo == ResultsHotelsState.HOTEL_LIST_UP
					|| stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN);

			if ((stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES)
					|| (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP)) {
				//SHOWING ROOMS AND RATES
				mHotelListFrag.setListLockedToTop(true);
				setRoomsAndRatesAnimationVisibilities();
				setRoomsAndRatesAnimationHardwareRendering(true);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersAnimationVisibilities(true);
				setHotelsFiltersAnimationHardwareRendering(true);
			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				mHotelListFrag.setListLockedToTop(true);
				setAddToTripAnimationVis(true);
				setAddToTripAnimationHardwareRendering(true);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsState stateOne, ResultsHotelsState stateTwo, float percentage) {
			if (stateOne == ResultsHotelsState.HOTEL_LIST_DOWN && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				//ENTERING HOTELS
				mBgHotelMapC.setAlpha(percentage);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
				//LEAVING HOTELS
				mBgHotelMapC.setAlpha(1f - percentage);
			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES) {
				//SHOWING ROOMS AND RATES
				setRoomsAndRatesShownPercentage(percentage);
			}
			else if (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
				//HIDING ROOMS AND RATES
				setRoomsAndRatesShownPercentage(1f - percentage);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				setHotelsFiltersShownPercentage(percentage);

			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				setHotelsFiltersShownPercentage(1f - percentage);

			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				setAddToTripPercentage(percentage);
			}

		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			if ((stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.ROOMS_AND_RATES)
					|| (stateOne == ResultsHotelsState.ROOMS_AND_RATES && stateTwo == ResultsHotelsState.HOTEL_LIST_UP)) {
				//SHOWING/HIDING ROOMS AND RATES
				setRoomsAndRatesAnimationHardwareRendering(false);
			}
			else if (stateTwo == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| stateTwo == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//SHOWING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if (stateOne == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
					|| stateOne == ResultsHotelsState.ROOMS_AND_RATES_FILTERS) {
				//HIDING FILTERS
				setHotelsFiltersAnimationVisibilities(false);
				setHotelsFiltersAnimationHardwareRendering(false);

			}
			else if (stateTwo == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
				//ADD TO HOTEL
				setAddToTripAnimationVis(false);
				setAddToTripAnimationHardwareRendering(false);
			}

			setTouchable(true, true);

		}

		@Override
		public void onStateFinalized(ResultsHotelsState state) {

			setVisibilityState(state);
			setTouchState(state);
			setHotelsStateZIndex(state);
			setFragmentState(state);

			switch (state) {
			case HOTEL_LIST_DOWN:
				mBgHotelMapC.setAlpha(0f);
				mHotelListFrag.setListLockedToTop(false);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(0f);
				mHotelListFrag.setTopRightTextButtonText(getString(R.string.Sort_and_Filter));
				mHotelListFrag.setTopRightTextButtonEnabled(false);
				break;
			case HOTEL_LIST_UP:
				mBgHotelMapC.setAlpha(1f);
				mHotelListFrag.setListLockedToTop(false);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(0f);
				mHotelListFrag.setTopRightTextButtonText(getString(R.string.Sort_and_Filter));
				mHotelListFrag.setTopRightTextButtonEnabled(true);
				break;
			case ROOMS_AND_RATES: {
				mHotelListFrag.setListLockedToTop(false);
				setHotelsFiltersShownPercentage(0f);
				setAddToTripPercentage(0f);
				setRoomsAndRatesShownPercentage(1f);
				mHotelListFrag.setTopRightTextButtonText(getString(R.string.Sort_and_Filter));
				mHotelListFrag.setTopRightTextButtonEnabled(true);
				break;
			}
			case ROOMS_AND_RATES_FILTERS:
			case HOTEL_LIST_AND_FILTERS: {
				mHotelListFrag.setListLockedToTop(true);
				setHotelsFiltersShownPercentage(1f);
				setAddToTripPercentage(0f);
				mHotelListFrag.setTopRightTextButtonEnabled(false);
				break;
			}
			case ADDING_HOTEL_TO_TRIP: {
				mHotelListFrag.setListLockedToTop(true);
				setAddToTripPercentage(1f);
				mParentAddToTripListener.beginAddToTrip(mHotelDetailsFrag.getSelectedData(),
						mHotelDetailsFrag.getDestinationRect(), mShadeColor);
				doAddToTripDownloadStuff();
				break;
			}
			}

			if (mMapFragment != null && state != ResultsHotelsState.ROOMS_AND_RATES) {
				mMapFragment.setMapPaddingFromFilterState(state == ResultsHotelsState.HOTEL_LIST_AND_FILTERS
						|| state == ResultsHotelsState.ROOMS_AND_RATES_FILTERS);
			}
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
			float filtersLeft = -(1f - percentage) * mGrid.getColWidth(1);
			mHotelFiltersC.setTranslationX(filtersLeft);

			float filteredCountLeft = mGrid.getColWidth(2) * (1f - percentage);
			mHotelFilteredCountC.setTranslationX(filteredCountLeft);
		}

		/*
		 * SHOW ROOMS AND RATES ANIMATION STUFF
		 */

		private void setRoomsAndRatesAnimationVisibilities() {
			mHotelRoomsAndRatesC.setVisibility(View.VISIBLE);
		}

		private void setRoomsAndRatesAnimationHardwareRendering(boolean useHardwareLayer) {
			int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mHotelRoomsAndRatesC.setLayerType(layerValue, null);
			if (!mGrid.isLandscape()) {
				mHotelListC.setLayerType(layerValue, null);
			}
		}

		private void setRoomsAndRatesShownPercentage(float percentage) {
			if (mGrid.isLandscape()) {
				mHotelRoomsAndRatesC.setTranslationY(-(1f - percentage) * mGrid.getTotalHeight());
			}
			else {
				float roomsAndRatesTransX = (1f - percentage) * mGrid.getTotalWidth();
				float hotelListTransX = percentage * -mGrid.getColWidth(0);
				mHotelRoomsAndRatesC.setTranslationX(roomsAndRatesTransX);
				mHotelListC.setTranslationX(hotelListTransX);
			}
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
			mHotelDetailsFrag.setTransitionToAddTripHardwareLayer(layerType);
		}

		private void setAddToTripPercentage(float percentage) {
			if (mHotelDetailsFrag != null) {
				mHotelDetailsFrag.setTransitionToAddTripPercentage(percentage);
			}
			mHotelRoomsAndRatesShadeC.setAlpha(percentage);
		}

		/*
		 * HOTEL STATE HELPERS
		 */
		private void setHotelsStateZIndex(ResultsHotelsState state) {
			//Calling bringToFront() does a full layout pass, so we DONT want to do this when it is un-needed.

			if (state == ResultsHotelsState.ADDING_HOTEL_TO_TRIP) {
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

		/*
		 * Touch
		 */
		private void setTouchable(boolean touchable, boolean includingList) {
			mBgHotelMapC.setTouchPassThroughEnabled(touchable);
			mHotelFiltersC.setBlockNewEventsEnabled(touchable);
			mHotelFilteredCountC.setBlockNewEventsEnabled(touchable);
			mHotelRoomsAndRatesC.setBlockNewEventsEnabled(touchable);
			if (includingList) {
				mHotelListC.setBlockNewEventsEnabled(touchable);
			}
		}
	};
}
