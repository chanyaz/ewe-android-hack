package com.expedia.bookings.activity;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.enums.WaypointChooserState;
import com.expedia.bookings.fragment.DestinationTilesFragment;
import com.expedia.bookings.fragment.TabletLaunchMapFragment;
import com.expedia.bookings.fragment.TabletWaypointFragment;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
	IMeasurementProvider, TabletWaypointFragment.ITabletWaypointFragmentListener {

	private static final String STATE_SEARCH_SHOWING = "STATE_SEARCH_SHOWING";
	private static final int REQUEST_SETTINGS = 1234;

	//Views
	private ViewGroup mRootC;
	private ViewGroup mSearchBarC;
	private ViewGroup mWaypointC;

	//UI FRAGs
	private MeasurableFragment mMapFragment;
	private MeasurableFragment mTilesFragment;
	private TabletWaypointFragment mWaypointFragment;

	// HockeyApp
	private HockeyPuck mHockeyPuck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);

		getWindow().setBackgroundDrawable(null);

		mRootC = Ui.findView(this, R.id.root_container);
		mWaypointC = Ui.findView(mRootC, R.id.waypoint_container);
		mSearchBarC = Ui.findView(mRootC, R.id.search_bar_conatiner);

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			mMapFragment = TabletLaunchMapFragment.newInstance();
			mTilesFragment = DestinationTilesFragment.newInstance();
			mWaypointFragment = new TabletWaypointFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.map_container, mMapFragment);
			ft.add(R.id.tiles_container, mTilesFragment);
			ft.add(R.id.waypoint_container, mWaypointFragment);

			ft.commit();
		}
		else {
			mMapFragment = Ui.findSupportFragment(this, R.id.map_container);
			mTilesFragment = Ui.findSupportFragment(this, R.id.tiles_container);
			mWaypointFragment = Ui.findSupportFragment(this, R.id.waypoint_container);

			if (savedInstanceState.getBoolean(STATE_SEARCH_SHOWING, false)) {
				mWaypointFragment.setState(WaypointChooserState.VISIBLE, false);
			}
		}

		mSearchBarC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mWaypointFragment.setState(WaypointChooserState.VISIBLE, true);
			}
		});

		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHockeyPuck.onResume();
		mWaypointFragment.registerStateListener(mWaypointStateHelper, true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWaypointFragment.unRegisterStateListener(mWaypointStateHelper);
	}

	@Override
	public void onBackPressed() {
		if (mWaypointFragment.getState() == WaypointChooserState.VISIBLE) {
			mWaypointFragment.setState(WaypointChooserState.HIDDEN, true);
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mHockeyPuck.onSaveInstanceState(outState);
		if (mWaypointFragment != null && mWaypointFragment.getState() == WaypointChooserState.VISIBLE) {
			outState.putBoolean(STATE_SEARCH_SHOWING, true);
		}
		else {
			outState.putBoolean(STATE_SEARCH_SHOWING, false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_SETTINGS && resultCode == ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS) {
			// TODO reset the state of the SuggestionFragments such that it redraws again, and won't show the recents
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_launch_tablet, menu);
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);
		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onCreateOptionsMenu(menu);
		}

		return super.onCreateOptionsMenu(menu);
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
		case R.id.menu_your_trips: {
			startActivity(ItineraryActivity.createIntent(this));
			return true;
		}
		case R.id.menu_settings: {
			Intent intent = new Intent(this, TabletPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			return true;
		}
		case R.id.menu_about: {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		if (!AndroidUtils.isRelease(this) && mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search state listener

	private StateListenerHelper<WaypointChooserState> mWaypointStateHelper = new StateListenerHelper<WaypointChooserState>() {
		@Override
		public void onStateTransitionStart(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
			if (stateTwo != WaypointChooserState.HIDDEN) {
				mWaypointC.setVisibility(View.VISIBLE);
				getActionBar().hide();
			}
		}

		@Override
		public void onStateTransitionUpdate(WaypointChooserState stateOne, WaypointChooserState stateTwo,
			float percentage) {

		}

		@Override
		public void onStateTransitionEnd(WaypointChooserState stateOne, WaypointChooserState stateTwo) {

		}

		@Override
		public void onStateFinalized(WaypointChooserState state) {
			if (state == WaypointChooserState.HIDDEN) {
				mWaypointC.setVisibility(View.INVISIBLE);
				getActionBar().show();
			}
			else {
				mWaypointC.setVisibility(View.VISIBLE);
				getActionBar().hide();
			}
		}
	};

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

		Db.deleteCachedFlightData(this);
		Db.deleteHotelSearchData(this);

		//Clear trip bucket before search
		Db.getTripBucket().clear();

		//Set the search date to be for today as a default
		Sp.getParams().setStartDate(new LocalDate());

		startActivity(new Intent(this, TabletResultsActivity.class));
	}

	private void showDevErrorDialog(String msg) {
		SimpleDialogFragment.newInstance(null, "DEV (NO LOC): " + msg).show(getSupportFragmentManager(), "errorDf");
	}

	//////////////////////////////////////////////////////////////////////////
	// MeasureableFragmentListener

	@Override
	public void canMeasure(Fragment fragment) {
		if ((fragment == mMapFragment || fragment == mTilesFragment)
			&& mMapFragment.isMeasurable() && mTilesFragment.isMeasurable()) {

			updateContentSize(mRootC.getWidth(), mRootC.getHeight());
		}
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
	 * ITabletWaypointFragmentListener
	 */

	@Override
	public Rect getAnimOrigin() {
		if (mSearchBarC != null) {
			return ScreenPositionUtils.getGlobalScreenPosition(mSearchBarC);
		}
		return new Rect();
	}

	@Override
	public void onWaypointSearchComplete(TabletWaypointFragment caller, SuggestionV2 suggest) {
		if (suggest != null) {
			Sp.getParams().setDestination(suggest);
			doSearch();
		}
	}
}
