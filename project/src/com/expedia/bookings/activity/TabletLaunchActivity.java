package com.expedia.bookings.activity;

import android.content.Intent;
import android.location.Location;
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
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.fragment.DestinationTilesFragment;
import com.expedia.bookings.fragment.ExpediaServicesFragment;
import com.expedia.bookings.fragment.ExpediaServicesFragment.ExpediaServicesFragmentListener;
import com.expedia.bookings.fragment.ExpediaServicesFragment.ServiceType;
import com.expedia.bookings.fragment.FusedLocationProviderFragment;
import com.expedia.bookings.fragment.FusedLocationProviderFragment.FusedLocationProviderListener;
import com.expedia.bookings.fragment.TabletLaunchMapFragment;
import com.expedia.bookings.fragment.TabletSearchFragment;
import com.expedia.bookings.fragment.TabletSearchFragment.SearchFragmentListener;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.app.SimpleProgressDialogFragment;
import com.mobiata.android.app.SimpleProgressDialogFragment.SimpleProgressDialogFragmentListener;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
		SearchFragmentListener, ExpediaServicesFragmentListener, SimpleProgressDialogFragmentListener,
		FusedLocationProviderListener {

	// On top when search params covers up everything
	private static final String BACKSTACK_SEARCH_PARAMS = "BACKSTACK_SEARCH_PARAMS";

	private static final int REQUEST_SETTINGS = 1234;

	private MeasurableFragment mMapFragment;
	private MeasurableFragment mTilesFragment;
	private TabletSearchFragment mSearchFragment;

	// TODO: REMOVE LATER, THIS IS DEV ONLY
	// We're loading all results here right now, until we figure out where we need to load it later.
	private static final String TAG_SERVICES = "TAG_SERVICES";
	private static final String TAG_LOAD_SEARCH_DIALOG = "TAG_LOAD_SEARCH_DIALOG";
	private ExpediaServicesFragment mServicesFragment;
	private FusedLocationProviderFragment mLocationFragment;
	private SimpleProgressDialogFragment mLoadSearchDialogFragment;
	private SearchParams mSearchParams;

	// HockeyApp
	private HockeyPuck mHockeyPuck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);

		getWindow().setBackgroundDrawable(null);

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			mMapFragment = TabletLaunchMapFragment.newInstance();
			mTilesFragment = DestinationTilesFragment.newInstance();
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

		mLocationFragment = FusedLocationProviderFragment.getInstance(this);

		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHockeyPuck.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocationFragment.stop();
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
	// Search code
	//
	// This should eventually all get moved into TabletSearchActivity

	private void startSearch(SearchParams searchParams) {
		// Validate that we have all data we need
		if (!searchParams.hasOrigin()) {
			Toast.makeText(this, "Origin is required for search (Loc String TODO)", Toast.LENGTH_LONG).show();
			return;
		}
		else if (!searchParams.hasDestination()) {
			Toast.makeText(this, "Destination is required for search (Loc String TODO)", Toast.LENGTH_LONG).show();
			return;
		}

		mSearchParams = searchParams;

		// If either the origin or the destination are "current location", then fill that out before starting a search
		if (searchParams.getOrigin().getResultType() == ResultType.CURRENT_LOCATION
				|| searchParams.getDestination().getResultType() == ResultType.CURRENT_LOCATION) {
			mLocationFragment.find(this);
		}
		else {
			doSearch();
		}
	}

	private void onCurrentLocationSuggestions(SuggestionResponse suggestResponse) {
		if (suggestResponse == null) {
			showDevErrorDialog("No current location suggestions: null response.");
			return;
		}
		else if (suggestResponse.hasErrors()) {
			showDevErrorDialog("No current location suggestions: error response.");
			return;
		}
		else if (suggestResponse.getSuggestions().size() == 0) {
			showDevErrorDialog("No current location suggestions: nothing nearby!.");
			return;
		}

		// Use the 1st suggestion
		SuggestionV2 suggestion = suggestResponse.getSuggestions().get(0);

		if (mSearchParams.getOrigin().getResultType() == ResultType.CURRENT_LOCATION) {
			mSearchParams.setOriginAirport(suggestion);
		}

		if (mSearchParams.getDestination().getResultType() == ResultType.CURRENT_LOCATION) {
			mSearchParams.setDestinationAirport(suggestion);
		}

		doSearch();
	}

	private void doSearch() {
		Db.getHotelSearch().setSearchResponse(null);
		Db.getFlightSearch().setSearchResponse(null);
		Db.getFilter().reset();

		mLoadSearchDialogFragment = SimpleProgressDialogFragment.newInstance("Loading results...");
		mLoadSearchDialogFragment.show(getSupportFragmentManager(), TAG_LOAD_SEARCH_DIALOG);

		mServicesFragment.startHotelSearch(mSearchParams, false);
		mServicesFragment.startFlightSearch(mSearchParams, false);

		Db.getHotelSearch().setSearchParams(mSearchParams.toHotelSearchParams());
		Db.getFlightSearch().setSearchParams(mSearchParams.toFlightSearchParams());

		Log.i("Starting search with params: " + mSearchParams);
	}

	private void showDevErrorDialog(String msg) {
		SimpleDialogFragment.newInstance(null, "DEV (NO LOC): " + msg).show(getSupportFragmentManager(), "errorDf");
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
		startSearch(searchParams);
	}

	//////////////////////////////////////////////////////////////////////////
	// ExpediaServicesFragmentListener

	@Override
	public void onExpediaServicesDownload(ServiceType type, Response response) {
		switch (type) {
		case SUGGEST_NEARBY:
			SuggestionResponse suggestResponse = (SuggestionResponse) response;
			onCurrentLocationSuggestions(suggestResponse);
			return;
		case HOTEL_SEARCH:
			// TODO ideally we keep trip bucket around, but right now it is just not compatible with new searches
			Db.getTripBucket().clear();
			Db.saveTripBucket(this);

			Db.getHotelSearch().setSearchResponse((HotelSearchResponse) response);
			Db.saveHotelSearchTimestamp(this);
			Db.kickOffBackgroundHotelSearchSave(this);
			break;
		case FLIGHT_SEARCH:
			// TODO ideally we keep trip bucket around, but right now it is just not compatible with new searches
			Db.getTripBucket().clear();
			Db.saveTripBucket(this);

			Db.getFlightSearch().setSearchResponse((FlightSearchResponse) response);
			if (response != null) {
				Db.kickOffBackgroundFlightSearchSave(this);
				Db.addAirlineNames(((FlightSearchResponse) response).getAirlineNames());
			}
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
	// FusedLocationProviderListener

	@Override
	public void onFound(Location currentLocation) {
		// Update any "current location" fields
		com.expedia.bookings.data.Location location = new com.expedia.bookings.data.Location();
		location.setLatitude(currentLocation.getLatitude());
		location.setLongitude(currentLocation.getLongitude());

		if (mSearchParams.getOrigin().getResultType() == ResultType.CURRENT_LOCATION) {
			mSearchParams.getOrigin().setLocation(location);
		}

		if (mSearchParams.getDestination().getResultType() == ResultType.CURRENT_LOCATION) {
			mSearchParams.getDestination().setLocation(location);
		}

		mServicesFragment.startSuggestionsNearby(currentLocation.getLatitude(), currentLocation.getLongitude(), false);
	}

	@Override
	public void onError() {
		showDevErrorDialog("Tried current location search, but could not get current location.");
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
