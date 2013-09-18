package com.expedia.bookings.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.ExpediaServicesFragment;
import com.expedia.bookings.fragment.ExpediaServicesFragment.ExpediaServicesFragmentListener;
import com.expedia.bookings.fragment.ExpediaServicesFragment.ServiceType;
import com.expedia.bookings.fragment.TabletSearchFragment;
import com.expedia.bookings.fragment.TabletSearchFragment.SearchFragmentListener;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.fragment.debug.ColorFragment;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.maps.SvgTileProvider;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleProgressDialogFragment;
import com.mobiata.android.app.SimpleProgressDialogFragment.SimpleProgressDialogFragmentListener;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
		SearchFragmentListener, ExpediaServicesFragmentListener, SimpleProgressDialogFragmentListener {

	// On top when search params covers up everything
	private static final String BACKSTACK_SEARCH_PARAMS = "BACKSTACK_SEARCH_PARAMS";

	private SupportMapFragment mMapFragment;
	private MeasurableFragment mTilesFragment;
	private TabletSearchFragment mSearchFragment;

	// TODO: REMOVE LATER, THIS IS DEV ONLY
	// We're loading all results here right now, until we figure out where we need to load it later.
	private static final String TAG_SERVICES = "TAG_SERVICES";
	private static final String TAG_LOAD_SEARCH_DIALOG = "TAG_LOAD_SEARCH_DIALOG";
	private ExpediaServicesFragment mServicesFragment;
	private SimpleProgressDialogFragment mLoadSearchDialogFragment;

	// HockeyApp
	private HockeyPuck mHockeyPuck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);

		getWindow().setBackgroundDrawable(null);

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			mMapFragment = SupportMapFragment.newInstance();
			mTilesFragment = ColorFragment.newInstance(Color.argb(100, 0, 0, 0));
			mSearchFragment = new TabletSearchFragment();
			mServicesFragment = new ExpediaServicesFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.map_container, mMapFragment);
			ft.add(R.id.tiles_container, mTilesFragment);
			ft.add(R.id.search_container, mSearchFragment);
			ft.add(mServicesFragment, TAG_SERVICES);
			ft.commit();
		}
		else {
			mMapFragment = Ui.findSupportFragment(this, R.id.map_container);
			mTilesFragment = Ui.findSupportFragment(this, R.id.tiles_container);
			mSearchFragment = Ui.findSupportFragment(this, R.id.search_container);
			mServicesFragment = Ui.findSupportFragment(this, TAG_SERVICES);
			mLoadSearchDialogFragment = Ui.findSupportFragment(this, TAG_LOAD_SEARCH_DIALOG);

			if (BACKSTACK_SEARCH_PARAMS.equals(getTopBackStackName())) {
				mSearchFragment.expand();
			}
		}

		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHockeyPuck.onResume();
	}

	@Override
	public void onBackPressed() {
		if (!mSearchFragment.onBackPressed()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mHockeyPuck.onSaveInstanceState(outState);
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
			// Possible TODO: Reset the activity when settings are changed?
			Intent intent = new Intent(this, TabletPreferenceActivity.class);
			startActivity(intent);
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
	// Back stack utils

	public String getTopBackStackName() {
		FragmentManager fm = getSupportFragmentManager();
		int backStackEntryCount = fm.getBackStackEntryCount();
		if (backStackEntryCount > 0) {
			return fm.getBackStackEntryAt(backStackEntryCount - 1).getName();
		}
		return "";
	}

	//////////////////////////////////////////////////////////////////////////
	// MeasureableFragmentListener

	@Override
	public void canMeasure(Fragment fragment) {
		if (fragment == mMapFragment) {
			SupportMapFragment mapFragment = (SupportMapFragment) fragment;
			SvgTileProvider.addToMap(this, mapFragment.getMap());
		}

		if ((fragment == mMapFragment || fragment == mSearchFragment || fragment == mTilesFragment)
				&& mMapFragment.isMeasurable() && mSearchFragment.isMeasurable() && mTilesFragment.isMeasurable()) {
			mSearchFragment.setInitialTranslationY(mMapFragment.getView().getHeight()
					- mTilesFragment.getView().getHeight());
			mSearchFragment.collapse();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchFragmentListener

	@Override
	public void onFinishExpand() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.detach(mMapFragment);
		ft.detach(mTilesFragment);
		ft.addToBackStack(BACKSTACK_SEARCH_PARAMS);
		ft.commit();
	}

	@Override
	public void onSearch(SearchParams searchParams) {
		// Validate that we have all data we need
		if (!searchParams.hasOrigin()) {
			Toast.makeText(this, "Origin is required for search (Loc String TODO)", Toast.LENGTH_LONG).show();
			return;
		}
		else if (!searchParams.hasDestination()) {
			Toast.makeText(this, "Destination is required for search (Loc String TODO)", Toast.LENGTH_LONG).show();
			return;
		}

		// Do actual search
		Db.getHotelSearch().setSearchResponse(null);
		Db.getFlightSearch().setSearchResponse(null);

		mLoadSearchDialogFragment = SimpleProgressDialogFragment.newInstance("Loading results...");
		mLoadSearchDialogFragment.show(getSupportFragmentManager(), TAG_LOAD_SEARCH_DIALOG);

		mServicesFragment.startHotelSearch(searchParams, false);
		mServicesFragment.startFlightSearch(searchParams, false);

		Db.getHotelSearch().setSearchParams(searchParams.toHotelSearchParams());
		Db.getFlightSearch().setSearchParams(searchParams.toFlightSearchParams());

		Log.i("Starting search with params: " + searchParams);
	}

	//////////////////////////////////////////////////////////////////////////
	// ExpediaServicesFragmentListener

	@Override
	public void onExpediaServicesDownload(ServiceType type, Response response) {
		switch (type) {
		case HOTEL_SEARCH:
			Db.getHotelSearch().setSearchResponse((HotelSearchResponse) response);
			break;
		case FLIGHT_SEARCH:
			Db.getFlightSearch().setSearchResponse((FlightSearchResponse) response);
			break;
		}

		// Update progress based on new results
		HotelSearchResponse hotelSearchResponse = Db.getHotelSearch().getSearchResponse();
		FlightSearchResponse flighSearchResponse = Db.getFlightSearch().getSearchResponse();

		if (hotelSearchResponse != null && !hotelSearchResponse.hasErrors() && flighSearchResponse != null
				&& !flighSearchResponse.hasErrors()) {
			mLoadSearchDialogFragment.dismissAllowingStateLoss();
			startActivity(new Intent(this, TabletResultsActivity.class));
		}
		else {
			mLoadSearchDialogFragment.setMessage(checkResponse(hotelSearchResponse, "Hotel search")
					+ checkResponse(flighSearchResponse, "Flight search"));
		}
	}

	private String checkResponse(Response response, String prefix) {
		if (response == null) {
			return prefix + " response is null; ";
		}
		else if (response.hasErrors()) {
			return prefix + " response has errors; ";
		}
		else {
			return prefix + " response loaded; ";
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SimpleProgressDialogFragmentListener

	@Override
	public void onCancel() {
		// Cancel the download
		mServicesFragment.cancel(ServiceType.HOTEL_SEARCH);
		mServicesFragment.cancel(ServiceType.FLIGHT_SEARCH);
	}
}
