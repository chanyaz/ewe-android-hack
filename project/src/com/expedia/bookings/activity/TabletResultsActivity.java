package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsLoadingState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment;
import com.expedia.bookings.fragment.ResultsLoadingFragment;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

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
public class TabletResultsActivity extends SherlockFragmentActivity implements IBackButtonLockListener,
	IAddToTripListener, IFragmentAvailabilityProvider, IStateProvider<ResultsState>, IMeasurementProvider,
	IBackManageable {

	//State
	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";

	//Tags
	private static final String FTAG_FLIGHTS_CONTROLLER = "FTAG_FLIGHTS_CONTROLLER";
	private static final String FTAG_HOTELS_CONTROLLER = "FTAG_HOTELS_CONTROLLER";
	private static final String FTAG_BACKGROUND_IMAGE = "FTAG_BACKGROUND_IMAGE";
	private static final String FTAG_LOADING = "FTAG_LOADING";

	//Containers..
	private ViewGroup mRootC;
	private FrameLayoutTouchController mBgDestImageC;
	private FrameLayoutTouchController mLoadingC;

	//Fragments
	private ResultsBackgroundImageFragment mBackgroundImageFrag;
	private TabletResultsFlightControllerFragment mFlightsController;
	private TabletResultsHotelControllerFragment mHotelsController;
	private ResultsLoadingFragment mLoadingFrag;

	//Other
	private GridManager mGrid = new GridManager();
	private ResultsState mState = ResultsState.OVERVIEW;
	private boolean mPreDrawInitComplete = false;
	private boolean mBackButtonLocked = false;

	private HockeyPuck mHockeyPuck;

	private ArrayList<IAddToTripListener> mAddToTripListeners = new ArrayList<IAddToTripListener>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_results);

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);
		mBgDestImageC = Ui.findView(this, R.id.bg_dest_image_overlay);
		mBgDestImageC.setBlockNewEventsEnabled(true);
		mBgDestImageC.setVisibility(View.VISIBLE);
		mLoadingC = Ui.findView(this, R.id.loading_frag_container);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_STATE)) {
			String stateName = savedInstanceState.getString(STATE_CURRENT_STATE);
			mState = ResultsState.valueOf(stateName);
			mResultsStateListeners.finalizeState(mState);//Note at this point there are no listeners, so no worries.
		}

		//Add default fragments
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		mBackgroundImageFrag = (ResultsBackgroundImageFragment) FragmentAvailabilityUtils.setFragmentAvailability(true,
			FTAG_BACKGROUND_IMAGE,
			manager, transaction, this, R.id.bg_dest_image_overlay, false);
		mFlightsController = (TabletResultsFlightControllerFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_FLIGHTS_CONTROLLER, manager, transaction, this,
			R.id.full_width_flights_controller_container, false);
		mHotelsController = (TabletResultsHotelControllerFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_HOTELS_CONTROLLER, manager, transaction, this,
			R.id.full_width_hotels_controller_container, false);
		mLoadingFrag = (ResultsLoadingFragment) FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_LOADING, manager, transaction, this,
			R.id.loading_frag_container, false);
		transaction.commit();
		manager.executePendingTransactions();//These must be finished before we continue..

		//We load up the default backgrounds so they are ready to go later if/when we need them
		//this is important, as we need to load images before our memory load gets too heavy
		if (savedInstanceState == null || !Db.getBackgroundImageCache(this).isDefaultInCache()) {
			Db.getBackgroundImageCache(this).loadDefaultsInThread(this);
		}

		//Set up the loading fragment to listen for hotel/flight events.
		if (mLoadingFrag != null) {
			if (mHotelsController != null) {
				mHotelsController.registerStateListener(mLoadingFrag.getHotelsStateListener(), true);
			}
			if (mFlightsController != null) {
				mFlightsController.registerStateListener(mLoadingFrag.getFlightsStateListener(), true);
			}
		}

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);

		//TODO: This is just for logging so it can be removed if we want to turn off state logging.
		registerStateListener(new StateListenerLogger<ResultsState>(), true);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_CURRENT_STATE, mState.name());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();

		// TODO: make background image dynamic, we'll probably want it in its own fragment

		mRootC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (!mPreDrawInitComplete) {
					updateContentSize(mRootC.getWidth(), mRootC.getHeight());
					finalizeState(mState);
					mPreDrawInitComplete = true;
				}

				mRootC.getViewTreeObserver().removeOnPreDrawListener(this);

				return true;
			}
		});

		mHockeyPuck.onResume();

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


			//TODO: REMOVE THIS, IT IS ONLY USEFUL FOR BUILDING THE LOADING STUFF
			menu.add(100, ResultsLoadingState.ALL.ordinal() + 1, ResultsLoadingState.ALL.ordinal() + 1,
				ResultsLoadingState.ALL.name());
			menu.add(100, ResultsLoadingState.HOTELS.ordinal() + 1, ResultsLoadingState.HOTELS.ordinal() + 1,
				ResultsLoadingState.HOTELS.name());
			menu.add(100, ResultsLoadingState.FLIGHTS.ordinal() + 1, ResultsLoadingState.FLIGHTS.ordinal() + 1,
				ResultsLoadingState.FLIGHTS.name());
			menu.add(100, ResultsLoadingState.NONE.ordinal() + 1, ResultsLoadingState.NONE.ordinal() + 1,
				ResultsLoadingState.NONE.name());


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
				finalizeState(ResultsState.OVERVIEW);
				return true;
			}
			else if (groupId == ResultsState.HOTELS.ordinal()) {
				Log.d("JumpTo: HOTELS - state:" + ResultsHotelsState.values()[id].name());
				if (mState != ResultsState.HOTELS) {
					finalizeState(ResultsState.HOTELS);
				}
				mHotelsController.setHotelsState(ResultsHotelsState.values()[id], false);
				return true;
			}
			else if (groupId == ResultsState.FLIGHTS.ordinal()) {
				Log.d("JumpTo: FLIGHTS - state:" + ResultsFlightsState.values()[id].name());
				if (mState != ResultsState.FLIGHTS) {
					finalizeState(ResultsState.FLIGHTS);
				}
				mFlightsController.setFlightsState(ResultsFlightsState.values()[id], false);
				return true;
			}

			//TODO: REMOVE THE BELOW CODE, IT IS NOT USEFUL EXCEPT FOR WHILE BUILDING THE LOADING STUFF
			if (item.getGroupId() == 100 && mLoadingFrag != null) {
				boolean anim = true;
				if (id == ResultsLoadingState.ALL.ordinal()) {
					mLoadingFrag.setState(ResultsLoadingState.ALL, anim);
				}
				else if (id == ResultsLoadingState.HOTELS.ordinal()) {
					mLoadingFrag.setState(ResultsLoadingState.HOTELS, anim);
				}
				else if (id == ResultsLoadingState.FLIGHTS.ordinal()) {
					mLoadingFrag.setState(ResultsLoadingState.FLIGHTS, anim);
				}
				else if (id == ResultsLoadingState.NONE.ordinal()) {
					mLoadingFrag.setState(ResultsLoadingState.NONE, anim);
				}
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

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHTS_CONTROLLER) {
			frag = mFlightsController;
		}
		else if (tag == FTAG_HOTELS_CONTROLLER) {
			frag = mHotelsController;
		}
		else if (tag == FTAG_BACKGROUND_IMAGE) {
			frag = mBackgroundImageFrag;
		}
		else if (tag == FTAG_LOADING) {
			frag = mLoadingFrag;
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
		else if (tag == FTAG_BACKGROUND_IMAGE) {
			String destination = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
			frag = ResultsBackgroundImageFragment.newInstance(destination, false);
		}
		else if (tag == FTAG_LOADING) {
			frag = ResultsLoadingFragment.newInstance();
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

	/**
	 * IAddToTripListener Stuff
	 */

	@Override
	public void beginAddToTrip(Object data, Rect globalCoordinates, int shadeColor) {
		for (IAddToTripListener listener : mAddToTripListeners) {
			listener.beginAddToTrip(data, globalCoordinates, shadeColor);
		}

	}

	@Override
	public void performTripHandoff() {
		for (IAddToTripListener listener : mAddToTripListeners) {
			listener.performTripHandoff();
		}
	}

	/*
	 * State management
	 */

	private StateListenerCollection<ResultsState> mResultsStateListeners = new StateListenerCollection<ResultsState>(
		mState);

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

		setListenerState(state);

		mState = state;
		mResultsStateListeners.finalizeState(state);

	}

	private void setListenerState(ResultsState state) {
		if (state == ResultsState.HOTELS) {
			mFlightsController.unRegisterStateListener(mFlightsStateHelper);
			mHotelsController.registerStateListener(mHotelsStateHelper, false);
		}
		else if (state == ResultsState.FLIGHTS) {
			mFlightsController.registerStateListener(mFlightsStateHelper, false);
			mHotelsController.unRegisterStateListener(mHotelsStateHelper);
		}
		else {
			mFlightsController.registerStateListener(mFlightsStateHelper, false);
			mHotelsController.registerStateListener(mHotelsStateHelper, false);
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
			mGrid.setGridSize(2, 3);
			mGrid.setDimensions(totalWidth, totalHeight);

			mGrid.setContainerToColumnSpan(mLoadingC, 0, 1);
			mGrid.setContainerToRow(mLoadingC, 1);

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
			finalizeState(getResultsStateFromHotels(state));

			mResultsStateListeners.setListenerActive(mHotelsController.getResultsListener());
		}

		private ResultsState getResultsStateFromHotels(ResultsHotelsState state) {
			if (state == ResultsHotelsState.LOADING || state == ResultsHotelsState.HOTEL_LIST_DOWN) {
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
			finalizeState(getResultsStateFromFlights(state));

			mResultsStateListeners.setListenerActive(mFlightsController.getResultsListener());
		}

		private ResultsState getResultsStateFromFlights(ResultsFlightsState state) {
			if (state == ResultsFlightsState.LOADING || state == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				return ResultsState.OVERVIEW;
			}
			else {
				return ResultsState.FLIGHTS;
			}
		}
	};
}
