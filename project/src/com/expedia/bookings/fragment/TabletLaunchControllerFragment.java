package com.expedia.bookings.fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.squareup.otto.Subscribe;

/**
 * Created by dmelton on 6/6/14.
 */
public class TabletLaunchControllerFragment extends MeasurableFragment
	implements IBackManageable, IStateProvider<LaunchState>,
	TabletWaypointFragment.ITabletWaypointFragmentListener {

	private static final String STATE_LAUNCH_STATE = "STATE_LAUNCH_STATE";

	private static final String FRAG_TAG_MAP = "FRAG_TAG_MAP";
	private static final String FRAG_TAG_TILES = "FRAG_TAG_TILES";
	private static final String FRAG_TAG_WAYPOINT = "FRAG_TAG_WAYPOINT";
	private static final String FRAG_TAG_PIN = "FRAG_TAG_PIN";

	// Containers
	private ViewGroup mRootC;
	private ViewGroup mSearchBarC;
	private ViewGroup mWaypointC;
	private FrameLayoutTouchController mPinDetailC;
	private ViewGroup mTilesC;

	// Fragments
	private TabletLaunchMapFragment mMapFragment;
	private DestinationTilesFragment mTilesFragment;
	private TabletWaypointFragment mWaypointFragment;
	private TabletLaunchPinDetailFragment mPinFragment;

	private TextView mAbText1;
	private TextView mAbText2;

	//vars
	private StateManager<LaunchState> mStateManager = new StateManager<>(LaunchState.DEFAULT, this);

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

		FragmentManager fm = getChildFragmentManager();
		if (savedInstanceState == null) {
			mMapFragment = TabletLaunchMapFragment.newInstance();
			mTilesFragment = DestinationTilesFragment.newInstance();
			mWaypointFragment = TabletWaypointFragment.newInstance(true);
			mPinFragment = TabletLaunchPinDetailFragment.newInstance();

			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.map_container, mMapFragment);
			ft.add(R.id.tiles_container, mTilesFragment);
			ft.add(R.id.waypoint_container, mWaypointFragment);
			ft.add(R.id.pin_detail_container, mPinFragment);

			ft.commit();
		}
		else {
			mMapFragment = Ui.findSupportFragment(this, R.id.map_container);
			mTilesFragment = Ui.findSupportFragment(this, R.id.tiles_container);
			mWaypointFragment = Ui.findSupportFragment(this, R.id.waypoint_container);
			mPinFragment = Ui.findSupportFragment(this, R.id.pin_detail_container);

			mStateManager.setDefaultState(LaunchState.valueOf(savedInstanceState.getString(
				STATE_LAUNCH_STATE, LaunchState.DEFAULT.name())));
		}

		ActionBar ab = getActivity().getActionBar();
		ab.setCustomView(R.layout.actionbar_tablet_title);

		mAbText1 = Ui.findView(ab.getCustomView(), R.id.text1);
		mAbText1.setText(R.string.Explore);
		mAbText2 = Ui.findView(ab.getCustomView(), R.id.text2);
		mAbText2.setText(R.string.Destination);
		mAbText2.setAlpha(0f);

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

		checkConnectivityAndDisplayMessage();
	}

	private void checkConnectivityAndDisplayMessage() {
		if (!NetUtils.isOnline(getActivity())) {
			int srcColor = Color.WHITE;
			PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
			PorterDuffColorFilter filter = new PorterDuffColorFilter(srcColor, mode);
			Paint paint = new Paint();
			paint.setColorFilter(filter);
			Ui.findView(getView(), R.id.globe_background).setLayerType(View.LAYER_TYPE_SOFTWARE, paint);
			Ui.findView(getView(), R.id.no_connectivity_container).setVisibility(View.VISIBLE);
		}
		else {
			Ui.findView(getView(), R.id.no_connectivity_container).setVisibility(View.GONE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		Events.unregister(this);
		mBackManager.unregisterWithParent(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LAUNCH_STATE, mStateManager.getState().name());
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
		mPinFragment.setOriginRect(mMapFragment.getClickedPinRect());
		mStateManager.setState(state, animate);
	}

	public boolean shouldDisplayMenu() {
		return mStateManager.getState() == LaunchState.DEFAULT && !mStateManager.isAnimating();
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
			else if (getLaunchState() != LaunchState.DEFAULT) {
				setLaunchState(LaunchState.DEFAULT, true);
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

	private SingleStateListener<LaunchState> mWaypointStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.WAYPOINT, true, new ISingleStateListener() {
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
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			mTilesC.setAlpha(1f - percentage);
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
		}
	}
	);

	private SingleStateListener<LaunchState> mDetailsStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.DETAILS, true, new ISingleStateListener() {

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
			mPinDetailC.getBackground().setAlpha((int)(255f * percentage));
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
				if (!TextUtils.isEmpty(event.queryText)) {
					Sp.getParams().setCustomDestinationQryText(event.queryText);
				}
				else {
					Sp.getParams().setDefaultCustomDestinationQryText();
				}
				Db.deleteTripBucket(getActivity());
				Db.getTripBucket().clear();
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

		startActivity(new Intent(getActivity(), TabletResultsActivity.class));
	}
}
