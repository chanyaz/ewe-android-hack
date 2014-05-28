package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment;
import com.expedia.bookings.fragment.ResultsTripBucketFragment;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment;
import com.expedia.bookings.fragment.TabletResultsSearchControllerFragment;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.ITripBucketBookClickListener;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsActivity: The results activity designed for tablet results 2013
 * <p/>
 * This activity was designed keep track of global results state e.g. Are we in flights/hotels/overview mode?
 * Furthermore is houses (and sets up plumbing between) our various ITabletResultsControllers.
 * <p/>
 * The ITabletResultsControllers control whole UI flows. So anything to do with hotels, is housed within
 * the ITabletResultsController instance fragment, which is in control over everything on screen when our
 * GlobalResultsState is set to HOTEL.
 * <p/>
 * At the time of this writting (9/5/2013) this is also in control of background images, but hopefully this
 * will be offloaded to elsewhere in the app eventually (if for nothing other than performance/ load time reasons).
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsActivity extends FragmentActivity implements IBackButtonLockListener,
	IFragmentAvailabilityProvider, IStateProvider<ResultsState>, IMeasurementProvider,
	IBackManageable, IAcceptingListenersListener, ITripBucketBookClickListener {

	//State
	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";

	//Tags
	private static final String FTAG_FLIGHTS_CONTROLLER = "FTAG_FLIGHTS_CONTROLLER";
	private static final String FTAG_HOTELS_CONTROLLER = "FTAG_HOTELS_CONTROLLER";
	private static final String FTAG_SEARCH_CONTROLLER = "FTAG_SEARCH_CONTROLLER";
	private static final String FTAG_BACKGROUND_IMAGE = "FTAG_BACKGROUND_IMAGE";
	private static final String FTAG_BUCKET = "FTAG_BUCKET";

	//Containers..
	private ViewGroup mRootC;
	private FrameLayoutTouchController mBgDestImageC;
	private FrameLayoutTouchController mTripBucketC;
	private LinearLayout mMissingFlightInfoC;
	private ViewGroup mFlightsC;

	//Fragments
	private ResultsBackgroundImageFragment mBackgroundImageFrag;
	private TabletResultsFlightControllerFragment mFlightsController;
	private TabletResultsHotelControllerFragment mHotelsController;
	private TabletResultsSearchControllerFragment mSearchController;
	private ResultsTripBucketFragment mTripBucketFrag;

	//Other
	private GridManager mGrid = new GridManager();
	private StateManager<ResultsState> mStateManager = new StateManager<ResultsState>(ResultsState.OVERVIEW, this);
	private boolean mBackButtonLocked = false;
	private Interpolator mCenterColumnUpDownInterpolator = new AccelerateInterpolator(1.2f);

	private HockeyPuck mHockeyPuck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			Sp.saveOrLoadForTesting(this);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_results);

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);
		mBgDestImageC = Ui.findView(this, R.id.bg_dest_image_overlay);
		mBgDestImageC.setBlockNewEventsEnabled(true);
		mBgDestImageC.setVisibility(View.VISIBLE);
		mTripBucketC = Ui.findView(this, R.id.trip_bucket_container);
		mMissingFlightInfoC = Ui.findView(this, R.id.missing_flight_info_container);
		mFlightsC = Ui.findView(this, R.id.full_width_flights_controller_container);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_STATE)) {
			String stateName = savedInstanceState.getString(STATE_CURRENT_STATE);
			mStateManager.setDefaultState(ResultsState.valueOf(stateName));

			//If the flights fragment was attached before, we want the local reference to be accurate
			FragmentManager manager = getSupportFragmentManager();
			mFlightsController = FragmentAvailabilityUtils.getFrag(manager, FTAG_FLIGHTS_CONTROLLER);
		}

		//Add default fragments
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		mBackgroundImageFrag = FragmentAvailabilityUtils.setFragmentAvailability(true,
			FTAG_BACKGROUND_IMAGE,
			manager, transaction, this, R.id.bg_dest_image_overlay, false);
		mHotelsController = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_HOTELS_CONTROLLER, manager, transaction, this,
			R.id.full_width_hotels_controller_container, false);
		mSearchController = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_SEARCH_CONTROLLER, manager, transaction, this,
			R.id.full_width_search_controller_container, false);
		mTripBucketFrag = FragmentAvailabilityUtils.setFragmentAvailability(true,
			FTAG_BUCKET, manager, transaction, this,
			R.id.trip_bucket_container, false);

		transaction.commit();
		manager.executePendingTransactions();//These must be finished before we continue..

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);

		//TODO: This is just for logging so it can be removed if we want to turn off state logging.
		registerStateListener(new StateListenerLogger<ResultsState>(), false);
		registerStateListener(mStateListener, false);

		//We want the up button
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_CURRENT_STATE, mStateManager.getState().name());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();

		mRootC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mRootC.getWidth() > 0 && mRootC.getHeight() > 0) {
					updateContentSize(mRootC.getWidth(), mRootC.getHeight());
					setState(mStateManager.getState(), false);
					mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
				}

				return true;
			}
		});

		Sp.getBus().register(this);

		mHockeyPuck.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		Sp.getBus().unregister(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean retVal = super.onCreateOptionsMenu(menu);

		DebugMenu.onCreateOptionsMenu(this, menu);

		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onCreateOptionsMenu(menu);
		}

		//We allow debug users to jump between states
		if (!AndroidUtils.isRelease(this)) {
			//We use ordinal() + 1 for all ids and groups because 0 == Menu.NONE
			SubMenu subMen = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Results State");
			subMen.add(ResultsState.OVERVIEW.ordinal() + 1, ResultsState.OVERVIEW.ordinal() + 1,
				ResultsState.OVERVIEW.ordinal() + 1, ResultsState.OVERVIEW.name());

			SubMenu hotelSubMen = subMen.addSubMenu(Menu.NONE, Menu.NONE, 1, ResultsState.HOTELS.name());
			SubMenu flightSubMen = subMen.addSubMenu(Menu.NONE, Menu.NONE, 2, ResultsState.FLIGHTS.name());
			for (ResultsHotelsState hotelState : ResultsHotelsState.values()) {
				hotelSubMen.add(ResultsState.HOTELS.ordinal() + 1, hotelState.ordinal() + 1, hotelState.ordinal() + 1,
					hotelState.name());
			}
			for (ResultsFlightsState flightState : ResultsFlightsState.values()) {
				flightSubMen.add(ResultsState.FLIGHTS.ordinal() + 1, flightState.ordinal() + 1,
					flightState.ordinal() + 1,
					flightState.name());
			}

			return true;
		}

		return retVal;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onPrepareOptionsMenu(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		if (!AndroidUtils.isRelease(this) && mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		//We allow debug users to jump between states
		if (!AndroidUtils.isRelease(this)) {

			//All of our groups/ids are .ordinal() + 1 so we subtract here to make things easier
			int groupId = item.getGroupId() - 1;
			int id = item.getItemId() - 1;

			if (groupId == ResultsState.OVERVIEW.ordinal() && id == ResultsState.OVERVIEW.ordinal()) {
				Log.d("JumpTo: OVERVIEW");
				setState(ResultsState.OVERVIEW, false);
				return true;
			}
			else if (groupId == ResultsState.HOTELS.ordinal()) {
				Log.d("JumpTo: HOTELS - state:" + ResultsHotelsState.values()[id].name());
				if (getState() != ResultsState.HOTELS) {
					setState(ResultsState.HOTELS, false);
				}
				mHotelsController.setHotelsState(ResultsHotelsState.values()[id], false);
				return true;
			}
			else if (groupId == ResultsState.FLIGHTS.ordinal()) {
				Log.d("JumpTo: FLIGHTS - state:" + ResultsFlightsState.values()[id].name());
				if (getState() != ResultsState.FLIGHTS) {
					setState(ResultsState.FLIGHTS, false);
				}
				mFlightsController.setFlightsState(ResultsFlightsState.values()[id], false);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (!mBackButtonLocked) {
			if (!mBackManager.doOnBackPressed()) {
				super.onBackPressed();
			}
		}
	}


	/*
	 Search Params, and the flights fragment
	 */
	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		if (mFlightsController == null && Sp.getParams().hasEnoughInfoForFlightsSearch()) {
			//Now we have enough params for a flight search, lets attach the flights controller
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			mFlightsController = FragmentAvailabilityUtils.setFragmentAvailability(
				true,
				FTAG_FLIGHTS_CONTROLLER, manager, transaction, this,
				R.id.full_width_flights_controller_container, false);
			transaction.commit();
			manager.executePendingTransactions();//These must be finished before we continue..

			//Register our listeners now that flights is attached
			setListenerState(getState());
		}
		else if (mFlightsController != null && getState() != ResultsState.FLIGHTS && !Sp.getParams()
			.hasEnoughInfoForFlightsSearch()) {

			//We dont have enough info for a flight search, so we detach our flights controller
			FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			mFlightsController = FragmentAvailabilityUtils.setFragmentAvailability(
				false,
				FTAG_FLIGHTS_CONTROLLER, manager, transaction, this,
				R.id.full_width_flights_controller_container, false);
			transaction.commit();
			manager.executePendingTransactions();//These must be finished before we continue..
		}


		if (mFlightsController == null) {
			//Show the missing info pane
			mMissingFlightInfoC.setVisibility(View.VISIBLE);
		}
		else {
			//Hide the missing info pane
			mMissingFlightInfoC.setVisibility(View.GONE);
		}
	}

	/**
	 * FRAGMENT AVAILABILITY
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHTS_CONTROLLER) {
			frag = mFlightsController;
		}
		else if (tag == FTAG_HOTELS_CONTROLLER) {
			frag = mHotelsController;
		}
		else if (tag == FTAG_SEARCH_CONTROLLER) {
			frag = mSearchController;
		}
		else if (tag == FTAG_BACKGROUND_IMAGE) {
			frag = mBackgroundImageFrag;
		}
		else if (tag == FTAG_BUCKET) {
			frag = mTripBucketFrag;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHTS_CONTROLLER) {
			frag = new TabletResultsFlightControllerFragment();
		}
		else if (tag == FTAG_HOTELS_CONTROLLER) {
			frag = new TabletResultsHotelControllerFragment();
		}
		else if (tag == FTAG_SEARCH_CONTROLLER) {
			frag = new TabletResultsSearchControllerFragment();
		}
		else if (tag == FTAG_BACKGROUND_IMAGE) {
			String destination = Sp.getParams().getDestination().getAirportCode();
			frag = ResultsBackgroundImageFragment.newInstance(destination, false);
		}
		else if (tag == FTAG_BUCKET) {
			frag = new ResultsTripBucketFragment();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {

	}

	@Override
	public void setBackButtonLockState(boolean locked) {
		mBackButtonLocked = locked;
	}

	/*
	 * State management
	 */

	public void setState(ResultsState state, boolean animate) {
		mStateManager.setState(state, animate);
	}

	public ResultsState getState() {
		return mStateManager.getState();
	}

	private StateListenerHelper<ResultsState> mStateListener = new StateListenerHelper<ResultsState>() {
		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {

			if (stateTwo == ResultsState.OVERVIEW) {
				//We change the visibility of some trip bucket items, this resets things
				mTripBucketFrag.bindToDb();
			}

			setEnteringProductHardwareLayers(View.LAYER_TYPE_HARDWARE,
				stateOne == ResultsState.HOTELS || stateTwo == ResultsState.HOTELS);

		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo != ResultsState.OVERVIEW) {
				setEnteringProductPercentage(percentage,
					stateOne == ResultsState.HOTELS || stateTwo == ResultsState.HOTELS);
			}
			else if (stateOne != ResultsState.OVERVIEW && stateTwo == ResultsState.OVERVIEW) {
				setEnteringProductPercentage(1f - percentage,
					stateOne == ResultsState.HOTELS || stateTwo == ResultsState.HOTELS);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			setEnteringProductHardwareLayers(View.LAYER_TYPE_NONE,
				stateOne == ResultsState.HOTELS || stateTwo == ResultsState.HOTELS);
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			setListenerState(state);

			if (mTripBucketFrag != null) {
				mTripBucketFrag.bindToDb();
				if (state == ResultsState.HOTELS || state == ResultsState.FLIGHTS) {
					//This makes items in the bucket invisible
					mTripBucketFrag.setBucketPreparedForAdd();
				}
			}

			if (state == ResultsState.OVERVIEW) {
				setEnteringProductPercentage(0f, false);
			}
			else {
				setEnteringProductPercentage(1f, state == ResultsState.HOTELS);
			}
		}
	};

	/**
	 * TRANSITIONS
	 */

	private void setEnteringProductHardwareLayers(int layerType, boolean enteringHotels) {
		mTripBucketC.setLayerType(layerType, null);
		mMissingFlightInfoC.setLayerType(layerType, null);
		if (enteringHotels) {
			mFlightsC.setLayerType(layerType, null);
		}
	}

	private void setEnteringProductPercentage(float percentage, boolean enteringHotels) {
		mTripBucketC.setTranslationY(percentage * mTripBucketC.getHeight());
		mMissingFlightInfoC.setTranslationY(
			mCenterColumnUpDownInterpolator.getInterpolation(percentage) * mGrid.getRowHeight(1));
		if (enteringHotels) {
			mFlightsC
				.setTranslationY(mCenterColumnUpDownInterpolator.getInterpolation(percentage) * mGrid.getRowHeight(1));
		}
		else if (mFlightsC.getTranslationY() != 0) {
			mFlightsC.setTranslationY(0f);
		}
	}


	/**
	 * STATE PROVIDER
	 */

	private StateListenerCollection<ResultsState> mResultsStateListeners = new StateListenerCollection<ResultsState>(
		mStateManager.getDefaultState());

	@Override
	public void startStateTransition(ResultsState stateOne, ResultsState stateTwo) {
		mResultsStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsState stateOne, ResultsState stateTwo, float percentage) {
		mResultsStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsState stateOne, ResultsState stateTwo) {
		mResultsStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsState state) {
		mResultsStateListeners.finalizeState(state);
	}

	private void setListenerState(ResultsState state) {
		//If we are in flights mode, we don't care what the hotels controller says
		if (mHotelsController != null) {
			if (state == ResultsState.FLIGHTS) {
				mHotelsController.setListenerActive(mHotelsStateHelper, false);
			}
			else {
				mHotelsController.setListenerActive(mHotelsStateHelper, true);
			}
		}

		//If we are in hotels mode, we don't care what the flights controller says
		if (mFlightsController != null) {
			if (state == ResultsState.HOTELS) {
				mFlightsController.setListenerActive(mFlightsStateHelper, false);
			}
			else {
				mFlightsController.setListenerActive(mFlightsStateHelper, true);
			}
		}
	}

	@Override
	public void registerStateListener(IStateListener<ResultsState> listener, boolean fireFinalizeState) {
		mResultsStateListeners.registerStateListener(listener, fireFinalizeState);

	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsState> listener) {
		mResultsStateListeners.unRegisterStateListener(listener);
	}

	/*
	 * IMeasurementProvider
	 */

	private int mLastReportedWidth = -1;
	private int mLastReportedHeight = -1;
	private ArrayList<IMeasurementListener> mMeasurementListeners = new ArrayList<IMeasurementListener>();

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {

		if (totalWidth != mLastReportedWidth || totalHeight != mLastReportedHeight) {
			boolean isLandscape = totalWidth > totalHeight;

			mLastReportedWidth = totalWidth;
			mLastReportedHeight = totalHeight;

			//Setup grid manager
			mGrid.setGridSize(2, 5);
			mGrid.setDimensions(totalWidth, totalHeight);

			int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
			mGrid.setColumnSize(1, spacerSize);
			mGrid.setColumnSize(3, spacerSize);

			mGrid.setContainerToColumn(mTripBucketC, 4);
			mGrid.setContainerToRow(mTripBucketC, 1);
			mGrid.setContainerToColumn(mMissingFlightInfoC, 2);
			mGrid.setContainerToRow(mMissingFlightInfoC, 1);

			for (IMeasurementListener listener : mMeasurementListeners) {
				listener.onContentSizeUpdated(totalWidth, totalHeight, isLandscape);
			}
		}
	}

	@Override
	public void registerMeasurementListener(IMeasurementListener listener, boolean fireListener) {
		mMeasurementListeners.add(listener);
		if (fireListener && mLastReportedWidth >= 0 && mLastReportedHeight >= 0) {
			listener.onContentSizeUpdated(mLastReportedWidth, mLastReportedHeight,
				mLastReportedWidth > mLastReportedHeight);
		}
	}

	@Override
	public void unRegisterMeasurementListener(IMeasurementListener listener) {
		mMeasurementListeners.remove(listener);
	}

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
			//Our children may do something on back pressed, but if we are left in charge we do nothing
			return false;
		}

	};

	/*
	 * HOTELS STATE LISTENER
	 */

	private StateListenerHelper<ResultsHotelsState> mHotelsStateHelper = new StateListenerHelper<ResultsHotelsState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			Log.d("ResultsHotelsState - onStateTransitionStart - stateOne:" + stateOne + " stateTwo:" + stateTwo);
			mResultsStateListeners.setListenerInactive(mHotelsController.getResultsListener());

			//DO WORK
			startStateTransition(getResultsStateFromHotels(stateOne), getResultsStateFromHotels(stateTwo));

			if (stateTwo == ResultsHotelsState.GALLERY) {
				getActionBar().hide();
				mSearchController.hideSearchBtns();
			}
			if (stateOne == ResultsHotelsState.GALLERY) {
				getActionBar().show();
				mSearchController.showSearchBtns();
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsState stateOne, ResultsHotelsState stateTwo,
			float percentage) {
			Log.d("ResultsHotelsState - onStateTransitionUpdate - stateOne:" + stateOne + " stateTwo:" + stateTwo
				+ " percentage:" + percentage);
			updateStateTransition(getResultsStateFromHotels(stateOne), getResultsStateFromHotels(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {
			Log.d("ResultsHotelsState - onStateTransitionEnd - stateOne:" + stateOne + " stateTwo:" + stateTwo);

			//DO WORK
			endStateTransition(getResultsStateFromHotels(stateOne), getResultsStateFromHotels(stateTwo));

			mResultsStateListeners.setListenerActive(mHotelsController.getResultsListener());
		}

		@Override
		public void onStateFinalized(ResultsHotelsState state) {
			Log.d("ResultsHotelsState - onStateFinalized - state:" + state);
			mResultsStateListeners.setListenerInactive(mHotelsController.getResultsListener());

			//DO WORK
			setState(getResultsStateFromHotels(state), false);

			mResultsStateListeners.setListenerActive(mHotelsController.getResultsListener());

			if (state == ResultsHotelsState.GALLERY) {
				getActionBar().hide();
				mSearchController.hideSearchBtns();
			}
		}

		private ResultsState getResultsStateFromHotels(ResultsHotelsState state) {
			if (state == ResultsHotelsState.LOADING || state == ResultsHotelsState.HOTEL_LIST_DOWN
				|| state == ResultsHotelsState.SEARCH_ERROR) {
				return ResultsState.OVERVIEW;
			}
			else {
				return ResultsState.HOTELS;
			}
		}

	};

	/*
	 * FLIGHTS STATE LISTENER
	 */

	private StateListenerHelper<ResultsFlightsState> mFlightsStateHelper = new StateListenerHelper<ResultsFlightsState>() {

		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			Log.d("ResultsFlightsState - onStateTransitionStart - stateOne:" + stateOne + " stateTwo:" + stateTwo);
			mResultsStateListeners.setListenerInactive(mFlightsController.getResultsListener());

			//DO WORK
			startStateTransition(getResultsStateFromFlights(stateOne), getResultsStateFromFlights(stateTwo));

			if (mFlightsController != null && mTripBucketFrag != null
				&& stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightsController
					.setAnimateToBucketRect(mTripBucketFrag.getAddToTripBucketDestinationRect(LineOfBusiness.FLIGHTS));
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
			Log.d("ResultsFlightsState - onStateTransitionUpdate - stateOne:" + stateOne + " stateTwo:" + stateTwo
				+ " percentage:" + percentage);
			updateStateTransition(getResultsStateFromFlights(stateOne), getResultsStateFromFlights(stateTwo),
				percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			Log.d("ResultsFlightsState - onStateTransitionEnd - stateOne:" + stateOne + " stateTwo:" + stateTwo);

			//DO WORK
			endStateTransition(getResultsStateFromFlights(stateOne), getResultsStateFromFlights(stateTwo));

			mResultsStateListeners.setListenerActive(mFlightsController.getResultsListener());
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			Log.d("ResultsFlightsState - onStateFinalized - state:" + state);

			mResultsStateListeners.setListenerInactive(mFlightsController.getResultsListener());

			//DO WORK
			setState(getResultsStateFromFlights(state), false);

			mResultsStateListeners.setListenerActive(mFlightsController.getResultsListener());
		}

		private ResultsState getResultsStateFromFlights(ResultsFlightsState state) {
			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN ||
				state == ResultsFlightsState.SEARCH_ERROR) {
				return ResultsState.OVERVIEW;
			}
			else {
				return ResultsState.FLIGHTS;
			}
		}
	};

	/*
	 * ITripBucketBookClickListener
	 */

	@Override
	public void onTripBucketBookClicked(LineOfBusiness lob) {
		startActivity(TabletCheckoutActivity.createIntent(this, lob));
	}

	/*
	IAcceptingListenersListener
	 */

	@Override
	public void acceptingListenersUpdated(Fragment frag, boolean acceptingListener) {
		if (acceptingListener) {
			if (frag == mFlightsController) {
				mFlightsController.registerStateListener(mFlightsStateHelper, false);
			}
			else if (frag == mHotelsController) {
				mHotelsController.registerStateListener(mHotelsStateHelper, false);
			}
		}
		else {
			if (frag == mFlightsController) {
				mFlightsController.unRegisterStateListener(mFlightsStateHelper);
			}
			else if (frag == mHotelsController) {
				mHotelsController.unRegisterStateListener(mHotelsStateHelper);
			}
		}
	}
}
