package com.expedia.bookings.fragment;

import android.app.ActionBar;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.graphics.SvgDrawable;
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
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.squareup.otto.Subscribe;

/**
 * Created by dmelton on 6/6/14.
 */
public class TabletLaunchControllerFragment extends MeasurableFragment
	implements IBackManageable, IStateProvider<LaunchState>,
	TabletWaypointFragment.ITabletWaypointFragmentListener,
	IFragmentAvailabilityProvider, GooglePlayServicesDialog.GooglePlayServicesConnectionSuccessListener {

	private static final String STATE_LAUNCH_STATE = "STATE_LAUNCH_STATE";

	private static final String FRAG_TAG_MAP = "FRAG_TAG_MAP";
	private static final String FRAG_TAG_TILES = "FRAG_TAG_TILES";
	private static final String FRAG_TAG_WAYPOINT = "FRAG_TAG_WAYPOINT";
	private static final String FRAG_TAG_PIN = "FRAG_TAG_PIN";

	// Containers
	private ViewGroup mRootC;
	private ViewGroup mSearchBarC;
	private ViewGroup mWaypointC;
	private TouchableFrameLayout mPinDetailC;
	private ViewGroup mTilesC;
	private TouchableFrameLayout mNoConnectivityContainer;

	// Fragments
	private TabletLaunchMapFragment mMapFragment;
	private TabletLaunchDestinationTilesFragment mTilesFragment;
	private TabletWaypointFragment mWaypointFragment;
	private TabletLaunchPinDetailFragment mPinFragment;
	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment mLocationFragment;
	private View mGlobeBackground;

	private TextView mAbText1;
	private TextView mAbText2;

	//vars
	private StateManager<LaunchState> mStateManager = new StateManager<>(LaunchState.CHECKING_GOOGLE_PLAY_SERVICES, this);

	/*
	 * Fragment Lifecycle
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_launch, null, false);

		mWaypointC = Ui.findView(mRootC, R.id.waypoint_container);
		mSearchBarC = Ui.findView(mRootC, R.id.fake_search_bar_container);
		mPinDetailC = Ui.findView(mRootC, R.id.pin_detail_container);
		mTilesC = Ui.findView(mRootC, R.id.tiles_container);
		mGlobeBackground = Ui.findView(mRootC, R.id.globe_background);
		mNoConnectivityContainer = Ui.findView(mRootC, R.id.no_connectivity_container);
		mNoConnectivityContainer.setBlockNewEventsEnabled(true);

		if (savedInstanceState != null) {
			FragmentManager fm = getChildFragmentManager();

			mMapFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_MAP);
			mTilesFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_TILES);
			mWaypointFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_WAYPOINT);
			mPinFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_PIN);

			String savedState = savedInstanceState.getString(STATE_LAUNCH_STATE, LaunchState.CHECKING_GOOGLE_PLAY_SERVICES.name());
			mStateManager.setDefaultState(LaunchState.valueOf(savedState));
		}

		ActionBar ab = getActivity().getActionBar();
		ab.setCustomView(R.layout.actionbar_tablet_title);

		mAbText1 = Ui.findView(ab.getCustomView(), R.id.text1);
		mAbText1.setText(Ui.obtainThemeResID(getActivity(), R.attr.skin_tablet_ab_launch_text1_base));
		mAbText2 = Ui.findView(ab.getCustomView(), R.id.text2);
		mAbText2.setText(Ui.obtainThemeResID(getActivity(), R.attr.skin_tablet_ab_launch_text2_pin_detail));
		mAbText2.setAlpha(0f);

		// Fit width
		Matrix viewport = new Matrix();
		Point screen = Ui.getScreenSize(getActivity());
		float scale = screen.x / 3000.0f;
		viewport.preScale(scale, scale);

		SVG globeSvg = SVGParser.getSVGFromResource(getResources(), R.raw.map_tablet_launch);
		SvgDrawable globeSvgDrawable = new SvgDrawable(globeSvg, viewport);
		mGlobeBackground.setBackgroundDrawable(globeSvgDrawable);

		registerStateListener(mNoConnectivityStateListener, false);
		registerStateListener(mCheckedForServicesListener, false);
		registerStateListener(mCurrentLocationStateListener, false);
		registerStateListener(mDetailsStateListener, false);
		registerStateListener(mWaypointStateListener, false);
		registerStateListener(new StateListenerLogger<LaunchState>(), false);

		mSearchBarC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setLaunchState(LaunchState.WAYPOINT, true);
				OmnitureTracking.trackTabletDestinationSearchPageLoad(getActivity());
			}
		});
		mSearchBarC.setVisibility(View.INVISIBLE);
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
		if (!NetUtils.isOnline(getActivity())) {
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
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_MAP:
			return mMapFragment;
		case FRAG_TAG_TILES:
			return mTilesFragment;
		case FRAG_TAG_WAYPOINT:
			return mWaypointFragment;
		case FRAG_TAG_PIN:
			return mPinFragment;
		default:
			return null;
		}
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_MAP:
			return TabletLaunchMapFragment.newInstance();
		case FRAG_TAG_TILES:
			return TabletLaunchDestinationTilesFragment.newInstance();
		case FRAG_TAG_WAYPOINT:
			return TabletWaypointFragment.newInstance(true);
		case FRAG_TAG_PIN:
			return TabletLaunchPinDetailFragment.newInstance();
		default:
			return null;
		}
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		// Ignore
	}

	private void setFragmentState(LaunchState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean showFrags = true;
		if (state == LaunchState.CHECKING_GOOGLE_PLAY_SERVICES || state == LaunchState.NO_CONNECTIVITY) {
			showFrags = false;
		}

		mMapFragment = FragmentAvailabilityUtils.setFragmentAvailability(showFrags, FRAG_TAG_MAP, manager, transaction, this, R.id.map_container, false);
		mTilesFragment = FragmentAvailabilityUtils.setFragmentAvailability(showFrags, FRAG_TAG_TILES, manager, transaction, this, R.id.tiles_container, false);
		mWaypointFragment = FragmentAvailabilityUtils.setFragmentAvailability(showFrags, FRAG_TAG_WAYPOINT, manager, transaction, this, R.id.waypoint_container, false);
		mPinFragment = FragmentAvailabilityUtils.setFragmentAvailability(showFrags, FRAG_TAG_PIN, manager, transaction, this, R.id.pin_detail_container, false);

		transaction.commit();
	}

	/*
	 * MeasurableFragment
	 */

	@Override
	public boolean isMeasurable() {
		return mMapFragment != null && mMapFragment.isMeasurable()
			&& mTilesFragment != null && mTilesFragment.isMeasurable();
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

	private SingleStateListener<LaunchState> mDetailsStateListener = new SingleStateListener<>(
		LaunchState.OVERVIEW, LaunchState.DETAILS, true, new ISingleStateListener() {

		private float mSearchBarY;

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mSearchBarY = mRootC.getHeight() - mSearchBarC.getTop();

			ActionBar ab = getActivity().getActionBar();
			mAbText1 = Ui.findView(ab.getCustomView(), R.id.text1);
			mAbText2 = Ui.findView(ab.getCustomView(), R.id.text2);

			getActivity().invalidateOptionsMenu();

			mSearchBarC.setVisibility(View.VISIBLE);
			mTilesC.setVisibility(View.VISIBLE);
			mPinDetailC.setVisibility(View.VISIBLE);
			mPinDetailC.setConsumeTouch(!isReversed);
			mPinDetailC.getBackground().setAlpha(isReversed ? 255 : 0);

			if (isReversed) {
				mSearchBarC.setTranslationY(mSearchBarY);
				mTilesC.setTranslationY(mSearchBarY);
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			// Slide the tiles and search bar down off the bottom of the screen
			mSearchBarC.setTranslationY(percentage * mSearchBarY);
			mTilesC.setTranslationY(percentage * mSearchBarY);
			mAbText1.setAlpha(1f - percentage);
			mAbText2.setAlpha(percentage);
			mPinDetailC.getBackground().setAlpha((int) (255f * percentage));
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			ActionBar ab = getActivity().getActionBar();
			ab.setDisplayHomeAsUpEnabled(!isReversed);
			ab.setHomeButtonEnabled(!isReversed);

			mPinDetailC.setConsumeTouch(!isReversed);
			mPinDetailC.getBackground().setAlpha(isReversed ? 0 : 255);

			if (isReversed) {
				// details hidden
				mSearchBarC.setVisibility(View.VISIBLE);
				mTilesC.setVisibility(View.VISIBLE);
				mPinDetailC.setVisibility(View.INVISIBLE);
			}
			else {
				// details showing
				mSearchBarC.setVisibility(View.INVISIBLE);
				mTilesC.setVisibility(View.INVISIBLE);
				mPinDetailC.setVisibility(View.VISIBLE);
			}

			getActivity().invalidateOptionsMenu();
		}
	}
	);

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


	/*
	 * Otto events
	 */

	@Subscribe
	public void onMapPinClicked(Events.LaunchMapPinClicked event) {
		setLaunchState(LaunchState.DETAILS, true);
	}

	@Subscribe
	public void onSearchSuggestionSelected(Events.SearchSuggestionSelected event) {
		if (event.suggestion != null) {
			if (event.isFromSavedParamsAndBucket) {
				Sp.loadSearchParamsFromDisk(getActivity());
			}
			else {
				Sp.getParams().restoreToDefaults();
				Sp.getParams().setDestination(event.suggestion);
				// Deleting the trip bucket removes air attach qualification
				// so lets just clear it out and leave air attach alone
				// Db.deleteTripBucket(getActivity());
				Db.getTripBucket().clear();
				Db.saveTripBucket(getActivity());
			}
			doSearch();
		}
	}

	private void doSearch() {
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

		Db.deleteCachedFlightData(getActivity());
		Db.deleteHotelSearchData(getActivity());

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

}
