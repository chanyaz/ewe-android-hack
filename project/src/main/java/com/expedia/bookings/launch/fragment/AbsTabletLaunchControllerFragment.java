package com.expedia.bookings.launch.fragment;

import android.app.ActionBar;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.fragment.FusedLocationProviderFragment;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment;
import com.expedia.bookings.fragment.TabletWaypointFragment;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ExpediaNetUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public abstract class AbsTabletLaunchControllerFragment extends MeasurableFragment
	implements IBackManageable, IStateProvider<LaunchState>,
	TabletWaypointFragment.ITabletWaypointFragmentListener,
	 GooglePlayServicesDialog.GooglePlayServicesConnectionSuccessListener {

	protected static final String STATE_LAUNCH_STATE = "STATE_LAUNCH_STATE";

	protected static final String FRAG_TAG_MAP = "FRAG_TAG_MAP";
	protected static final String FRAG_TAG_TILES = "FRAG_TAG_TILES";
	protected static final String FRAG_TAG_WAYPOINT = "FRAG_TAG_WAYPOINT";
	protected static final String FRAG_TAG_PIN = "FRAG_TAG_PIN";

	// Containers
	protected ViewGroup mRootC;
	protected ViewGroup mSearchBarC;
	protected ViewGroup mWaypointC;
	protected ViewGroup mTilesC;
	protected TouchableFrameLayout mNoConnectivityContainer;

	// Fragments
	protected TabletLaunchMapFragment mMapFragment;
	protected TabletWaypointFragment mWaypointFragment;
	protected TabletLaunchPinDetailFragment mPinFragment;
	// Invisible Fragment that handles FusedLocationProvider
	protected FusedLocationProviderFragment mLocationFragment;

	protected TextView mAbText1;
	protected TextView mAbText2;

	//vars
	protected StateManager<LaunchState> mStateManager = new StateManager<>(LaunchState.CHECKING_GOOGLE_PLAY_SERVICES,
		this);

	/*
	 * All subclass need to implement this
	 */
	protected abstract void setFragmentState(LaunchState state);


	/*
	 * Fragment Lifecycle
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_launch, null, false);
		mWaypointC = Ui.findView(mRootC, R.id.waypoint_container);
		mTilesC = Ui.findView(mRootC, R.id.tiles_container);
		mNoConnectivityContainer = Ui.findView(mRootC, R.id.no_connectivity_container);
		mNoConnectivityContainer.setBlockNewEventsEnabled(true);
		mSearchBarC = Ui.findView(mRootC, R.id.fake_search_bar_container);
		mSearchBarC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setLaunchState(LaunchState.WAYPOINT, true);
				OmnitureTracking.trackTabletDestinationSearchPageLoad();
			}
		});
		mSearchBarC.setVisibility(View.INVISIBLE);

		if (savedInstanceState != null) {
			FragmentManager fm = getChildFragmentManager();

			mMapFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_MAP);
			mWaypointFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_WAYPOINT);
			mPinFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_PIN);

			String savedState = savedInstanceState
				.getString(STATE_LAUNCH_STATE, LaunchState.CHECKING_GOOGLE_PLAY_SERVICES.name());
			mStateManager.setDefaultState(LaunchState.valueOf(savedState));
		}

		ActionBar ab = getActivity().getActionBar();
		ab.setCustomView(R.layout.actionbar_tablet_title);

		mAbText1 = Ui.findView(ab.getCustomView(), R.id.text1);
		mAbText1.setText(Ui.obtainThemeResID(getActivity(), R.attr.skin_tablet_ab_launch_text1_base));
		mAbText2 = Ui.findView(ab.getCustomView(), R.id.text2);
		mAbText2.setText(R.string.Destination);
		mAbText2.setAlpha(0f);
		registerStateListener(mCheckedForServicesListener, false);
		registerStateListener(mCurrentLocationStateListener, false);
		registerStateListener(mWaypointStateListener, false);
		registerStateListener(mNoConnectivityStateListener, false);
		registerStateListener(new StateListenerLogger<LaunchState>(), false);
		mLocationFragment = FusedLocationProviderFragment.getInstance(this);

		return mRootC;
	}


	@Override
	public void onStart() {
		super.onStart();
		setLaunchState(mStateManager.getState(), false);
	}

	@Override
	public void onResume() {
		super.onResume();

		Events.register(this);
		mBackManager.registerWithParent(this);

		// Because we enable/disable suggestion list type in a global manner, we must reset our
		// desired behavior in onResume otherwise we could go back and forth with
		// TODO shove special casing into SuggestionsAdapter (so we can modify instances and have
		// TODO more sane management of such configuration)
		SuggestionProvider.enableCurrentLocation(true);
		SuggestionProvider.setShowNearbyAiports(false);

		if (getActivity() != null) {
			GooglePlayServicesDialog gpsd = new GooglePlayServicesDialog(getActivity(), this);
			gpsd.startChecking();
		}

		checkConnectivityAndDisplayMessage();
	}

	private void checkConnectivityAndDisplayMessage() {
		if (!ExpediaNetUtils.isOnline(getActivity())) {
			setLaunchState(LaunchState.NO_CONNECTIVITY, false);
		}
		else {
			if (getLaunchState() == LaunchState.NO_CONNECTIVITY) {
				// We only want to mess around if we we're in a bad state
				setLaunchState(LaunchState.OVERVIEW, false);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		Events.unregister(this);
		mBackManager.unregisterWithParent(this);
		if (mLocationFragment != null) {
			mLocationFragment.stop();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LAUNCH_STATE, mStateManager.getState().name());
	}

	/*
	 * TabletLaunchControllerFragment specific
	 */

	public LaunchState getLaunchState() {
		return mStateManager.getState();
	}

	public void setLaunchState(LaunchState state, boolean animate) {
		if (mPinFragment != null && mMapFragment != null) {
			mPinFragment.setOriginRect(mMapFragment.getClickedPinRect());
		}
		mStateManager.setState(state, animate);
	}

	public boolean shouldDisplayMenu() {
		return mStateManager.getState() == LaunchState.OVERVIEW && !mStateManager.isAnimating();
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
			if (mStateManager.isAnimating()) {
				//If we are in the middle of state transition, just reverse it
				setLaunchState(mStateManager.getState(), true);
				return true;
			}
			else if (getLaunchState() == LaunchState.CHECKING_GOOGLE_PLAY_SERVICES) {
				// Just back out of the app
				return false;
			}
			else if (getLaunchState() == LaunchState.NO_CONNECTIVITY) {
				// Just back out of the app
				return false;
			}
			else if (getLaunchState() != LaunchState.OVERVIEW) {
				setLaunchState(LaunchState.OVERVIEW, true);
				return true;
			}
			return false;
		}
	};

	/*
	 * IStateProvider<LaunchState>
	 */

	private StateListenerCollection<LaunchState> mStateListeners = new StateListenerCollection<>();

	@Override
	public void startStateTransition(LaunchState stateOne, LaunchState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(LaunchState stateOne, LaunchState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(LaunchState stateOne, LaunchState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(LaunchState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<LaunchState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<LaunchState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	private SingleStateListener<LaunchState> mCheckedForServicesListener = new SingleStateListener<>(
		LaunchState.GETTING_CURRENT_LOCATION, LaunchState.OVERVIEW, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			// ignore
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			// ignore
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			// ignore
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			final boolean isOverview = !isReversed ? true : false;
			mSearchBarC.setVisibility(isOverview ? View.VISIBLE : View.INVISIBLE);
		}
	}
	);

	private SingleStateListener<LaunchState> mCurrentLocationStateListener = new SingleStateListener<>(
		LaunchState.CHECKING_GOOGLE_PLAY_SERVICES, LaunchState.GETTING_CURRENT_LOCATION, true,
		new ISingleStateListener() {
			@Override
			public void onStateTransitionStart(boolean isReversed) {
				// ignore
			}

			@Override
			public void onStateTransitionUpdate(boolean isReversed, float percentage) {
				// ignore
			}

			@Override
			public void onStateTransitionEnd(boolean isReversed) {
				// ignore
			}

			@Override
			public void onStateFinalized(boolean isReversed) {
				if (getActivity() != null && !getActivity().isFinishing()) {
					if (!isReversed) {
						//Get the current Location and update the tiles
						mLocationFragment.find(mFindCurrentLocation);
						setLaunchState(LaunchState.OVERVIEW, false);
					}
				}
			}
		}
	);

	private SingleStateListener<LaunchState> mWaypointStateListener = new SingleStateListener<>(
		LaunchState.OVERVIEW, LaunchState.WAYPOINT, true, new ISingleStateListener() {
		@Override
		public void onStateTransitionStart(boolean isReversed) {
			if (!isReversed) {
				getActivity().getActionBar().hide();
			}
			else {
				getActivity().getActionBar().show();
			}

			mSearchBarC.setVisibility(View.INVISIBLE);
			mWaypointC.setVisibility(View.VISIBLE);
			mTilesC.setVisibility(View.VISIBLE);
			mWaypointC.getBackground().setAlpha(isReversed ? 255 : 0);
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			mTilesC.setAlpha(1f - percentage);
			mWaypointC.getBackground().setAlpha((int) (255f * percentage));
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			if (!isReversed) {
				getActivity().getActionBar().hide();
				mSearchBarC.setVisibility(View.INVISIBLE);
				mTilesC.setVisibility(View.INVISIBLE);
				mWaypointC.setVisibility(View.VISIBLE);
			}
			else {
				getActivity().getActionBar().show();
				mTilesC.setVisibility(View.VISIBLE);
				mSearchBarC.setVisibility(View.VISIBLE);
				mWaypointC.setVisibility(View.INVISIBLE);
			}

			mWaypointC.getBackground().setAlpha(isReversed ? 0 : 255);
		}
	}
	);

	/*
	 * TabletWaypointFragment.ITabletWaypointFragmentListener
	 */

	@Override
	public Rect getAnimOrigin() {
		if (mSearchBarC != null) {
			return ScreenPositionUtils.getGlobalScreenPosition(mSearchBarC);
		}
		return new Rect();
	}


	/*
	 * GooglePlayServicesDialog.GooglePlayServicesConnectionSuccessListener
	 */
	@Override
	public void onGooglePlayServicesConnectionSuccess() {
		if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
			// We check in every onResume
			// But we only care to change to OVERVIEW if we are still in the checking state
			if (getLaunchState() == LaunchState.CHECKING_GOOGLE_PLAY_SERVICES
				|| getLaunchState() == LaunchState.OVERVIEW) {
				setLaunchState(LaunchState.GETTING_CURRENT_LOCATION, false);
			}
		}
	}

	protected void doSearch() {
		HotelSearch hotelSearch = Db.getHotelSearch();
		FlightSearch flightSearch = Db.getFlightSearch();

		// Search results filters
		HotelFilter filter = Db.getFilter();
		filter.reset();
		filter.notifyFilterChanged();

		// Start the search
		Log.i("Starting search with params: " + Sp.getParams());
		hotelSearch.setSearchResponse(null);
		flightSearch.setSearchResponse(null);

		startActivity(TabletResultsActivity.createIntent(getActivity()));
	}

	/*
	 * No internet dialog
	 */
	private static final String FRAG_TAG_INTERNET_DEAD = "FRAG_TAG_INTERNET_DEAD";

	@Subscribe
	public void showNoInternetDialog(Events.ShowNoInternetDialog e) {
		String msg = getString(R.string.error_no_internet);
		String okBtn = getString(R.string.ok);
		SimpleCallbackDialogFragment frag = SimpleCallbackDialogFragment.newInstance(null, msg, okBtn, e.callBackId);
		frag.setCancelable(false);
		frag.show(getActivity().getSupportFragmentManager(), FRAG_TAG_INTERNET_DEAD);
	}

	FusedLocationProviderFragment.FusedLocationProviderListener mFindCurrentLocation = new FusedLocationProviderFragment.FusedLocationProviderListener() {
		@Override
		public void onFound(Location currentLocation) {
			LaunchDb.generateNearByCollection(getActivity(), currentLocation);
		}

		@Override
		public void onError() {

		}
	};
	private StateListenerHelper<LaunchState> mNoConnectivityStateListener = new StateListenerHelper<LaunchState>() {
		@Override
		public void onStateTransitionStart(LaunchState stateOne, LaunchState stateTwo) {
			// ignore
		}

		@Override
		public void onStateTransitionUpdate(LaunchState stateOne, LaunchState stateTwo, float p) {
			// ignore
		}

		@Override
		public void onStateTransitionEnd(LaunchState stateOne, LaunchState stateTwo) {
			// ignore
		}

		@Override
		public void onStateFinalized(LaunchState state) {
			setFragmentState(state);

			if (state == LaunchState.NO_CONNECTIVITY) {
				mNoConnectivityContainer.setVisibility(View.VISIBLE);
			}
			else {
				mNoConnectivityContainer.setVisibility(View.GONE);
			}
		}
	};

}
