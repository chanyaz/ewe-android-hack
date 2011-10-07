package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.GeocodeDisambiguationDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.expedia.bookings.fragment.HotelDetailsFragment;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.InstanceFragment;
import com.expedia.bookings.fragment.MiniDetailsFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.google.android.maps.MapActivity;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;

public class TabletActivity extends MapActivity implements LocationListener, OnBackStackChangedListener {

	private Context mContext;
	private Resources mResources;

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_SEARCH_STARTED = 1;
	public static final int EVENT_SEARCH_PROGRESS = 2;
	public static final int EVENT_SEARCH_COMPLETE = 3;
	public static final int EVENT_SEARCH_ERROR = 4;
	public static final int EVENT_PROPERTY_SELECTED = 5;
	public static final int EVENT_AVAILABILITY_SEARCH_STARTED = 6;
	public static final int EVENT_AVAILABILITY_SEARCH_COMPLETE = 7;
	public static final int EVENT_AVAILABILITY_SEARCH_ERROR = 8;
	public static final int EVENT_DETAILS_OPENED = 9;

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_AVAILABILITY_SEARCH = "KEY_AVAILABILITY_SEARCH";
	private static final String KEY_GEOCODE = "KEY_GEOCODE";

	//////////////////////////////////////////////////////////////////////////
	// Fragments

	private InstanceFragment mInstance;

	//////////////////////////////////////////////////////////////////////////
	// Event handling

	public interface EventHandler {
		public void handleEvent(int eventCode, Object data);
	};

	private Set<EventHandler> mEventHandlers = new HashSet<EventHandler>();

	public boolean registerEventHandler(EventHandler eventHandler) {
		return mEventHandlers.add(eventHandler);
	}

	public boolean unregisterEventHandler(EventHandler eventHandler) {
		return mEventHandlers.remove(eventHandler);
	}

	public void notifyEventHandlers(int eventCode, Object data) {
		for (EventHandler eventHandler : mEventHandlers) {
			eventHandler.handleEvent(eventCode, data);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mResources = getResources();

		getFragmentManager().addOnBackStackChangedListener(this);

		initializeInstanceFragment();

		setContentView(R.layout.activity_tablet);

		initializeFragmentViews();

		// Setup search interface.  This is probably not ultimately where this will go.
		showHotelListFragment();
		showHotelMapFragment();
	}

	@Override
	protected void onStart() {
		super.onStart();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_GEOCODE)) {
			bd.registerDownloadCallback(KEY_SEARCH, mGeocodeCallback);
		}
		else if (bd.isDownloading(KEY_SEARCH)) {
			bd.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
		}
		else if (mInstance.mSearchResponse != null) {
			mSearchCallback.onDownload(mInstance.mSearchResponse);
		}
		else {
			startSearch();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_GEOCODE);
		bd.unregisterDownloadCallback(KEY_SEARCH);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	private SearchView mSearchView;
	private MenuItem mGuestsMenuItem;
	private MenuItem mDatesMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tablet, menu);

		mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		mGuestsMenuItem = menu.findItem(R.id.menu_guests);
		mDatesMenuItem = menu.findItem(R.id.menu_dates);

		mSearchView.setIconifiedByDefault(false);
		mSearchView.setSubmitButtonEnabled(true);
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				setFreeformLocation(query);
				mSearchView.clearFocus();
				startSearch();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});

		updateActionBarViews();

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_guests:
			showGuestsDialog();
			return true;
		case R.id.menu_dates:
			showCalendarDialog();
			return true;
		case R.id.menu_filter:
			// TODO: Display filter options
			return true;
		case R.id.menu_about:
			// TODO: Launch About fragment
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateActionBarViews() {
		mSearchView.setQuery(mInstance.mSearchParams.getSearchDisplayText(this), false);

		int numGuests = mInstance.mSearchParams.getNumAdults() + mInstance.mSearchParams.getNumChildren();
		mGuestsMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests));

		int numNights = mInstance.mSearchParams.getStayDuration();
		mDatesMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_nights, numNights, numNights));
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment management

	private static final String TAG_INSTANCE_FRAGMENT = "INSTANCE_FRAGMENT";
	private static final String TAG_HOTEL_LIST = "HOTEL_LIST";
	private static final String TAG_HOTEL_MAP = "HOTEL_MAP";
	private static final String TAG_HOTEL_DETAILS = "HOTEL_DETAILS";
	private static final String TAG_MINI_DETAILS = "MINI_DETAILS";

	private ViewGroup mLeftFragmentContainer;
	private ViewGroup mRightFragmentContainer;
	private ViewGroup mBottomRightFragmentContainer;

	private void initializeInstanceFragment() {
		// Add (or retrieve an existing) InstanceFragment to hold our state
		FragmentManager fragmentManager = getFragmentManager();
		mInstance = (InstanceFragment) fragmentManager.findFragmentByTag(TAG_INSTANCE_FRAGMENT);
		if (mInstance == null) {
			mInstance = InstanceFragment.newInstance();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(mInstance, TAG_INSTANCE_FRAGMENT);
			ft.commit();
		}
	}

	private void initializeFragmentViews() {
		mLeftFragmentContainer = (ViewGroup) findViewById(R.id.fragment_left);
		mRightFragmentContainer = (ViewGroup) findViewById(R.id.fragment_right);
		mBottomRightFragmentContainer = (ViewGroup) findViewById(R.id.fragment_bottom_right);
	}

	public void showHotelListFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager.findFragmentById(R.id.fragment_left) == null) {
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(R.id.fragment_left, HotelListFragment.newInstance(), TAG_HOTEL_LIST);
			ft.commit();
		}
	}

	public void showHotelMapFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager.findFragmentById(R.id.fragment_right) == null) {
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(R.id.fragment_right, HotelMapFragment.newInstance(), TAG_HOTEL_MAP);
			ft.commit();
		}
	}

	public void showMiniDetailsFragment() {
		MiniDetailsFragment fragment = MiniDetailsFragment.newInstance();
		registerEventHandler(fragment);

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.fragment_bottom_right, fragment, TAG_MINI_DETAILS);
		ft.addToBackStack(null);
		ft.commit();
	}

	public void showHotelDetailsFragment() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_right);
		HotelDetailsFragment hotelDetailsFragment = HotelDetailsFragment.newInstance();
		if (fragment != null) {
			ft.hide(fragment);
		}
		ft.replace(R.id.fragment_bottom_right, hotelDetailsFragment, TAG_HOTEL_DETAILS);
		ft.addToBackStack(null);
		ft.commit();
		registerEventHandler(hotelDetailsFragment);
	}

	//////////////////////////////////////////////////////////////////////////
	// Layout management
	// 
	// (includes OnBackStackChangedListener implementation)

	public void updateLayout() {
		updateContainerVisibility(mLeftFragmentContainer);
		updateContainerVisibility(mRightFragmentContainer);
		updateContainerVisibility(mBottomRightFragmentContainer);
	}

	/*
	 * This method makes "gone" the containers that either dont
	 * have any views at all, or none that are actually visible. 
	 */
	private void updateContainerVisibility(ViewGroup container) {
		Fragment fragment = getFragmentManager().findFragmentById(container.getId());
		container.setVisibility((fragment != null && fragment.isVisible()) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onBackStackChanged() {
		Log.v("onBackStackChanged()");

		updateLayout();
	}

	//////////////////////////////////////////////////////////////////////////
	// Dialogs

	void showGuestsDialog() {
		DialogFragment newFragment = GuestsDialogFragment.newInstance(mInstance.mSearchParams.getNumAdults(),
				mInstance.mSearchParams.getNumChildren());
		newFragment.show(getFragmentManager(), "GuestsDialog");
	}

	private void showCalendarDialog() {
		DialogFragment newFragment = CalendarDialogFragment.newInstance(mInstance.mSearchParams.getCheckInDate(),
				mInstance.mSearchParams.getCheckOutDate());
		newFragment.show(getFragmentManager(), "CalendarDialog");
	}

	private void showGeocodeDisambiguationDialog(List<Address> addresses) {
		DialogFragment newFragment = GeocodeDisambiguationDialogFragment.newInstance(addresses);
		newFragment.show(getFragmentManager(), "GeocodeDisambiguationDialog");
	}

	//////////////////////////////////////////////////////////////////////////
	// Events (called from Fragments)

	public void propertySelected(Property property) {
		Log.v("propertySelected(): " + property.getName());

		// Ensure that a MiniDetailsFragment is being displayed
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_MINI_DETAILS) == null) {
			showMiniDetailsFragment();
		}

		notifyEventHandlers(EVENT_PROPERTY_SELECTED, property);
	}

	public void moreDetailsForPropertySelected(Property property) {

		// Ensure that a HotelDetailsFragment is being displayed
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_HOTEL_DETAILS) == null) {
			showHotelDetailsFragment();
		}
		notifyEventHandlers(EVENT_DETAILS_OPENED, property);
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParams management

	public void setFreeformLocation(String freeformLocation) {
		Log.d("Setting freeform location: " + freeformLocation);

		mInstance.mSearchParams.setFreeformLocation(freeformLocation);
		mInstance.mSearchParams.setSearchType(SearchType.FREEFORM);

		updateActionBarViews();
	}

	public void setGuests(int numAdults, int numChildren) {
		Log.d("Setting guests: " + numAdults + " adult(s), " + numChildren + " child(ren)");

		mInstance.mSearchParams.setNumAdults(numAdults);
		mInstance.mSearchParams.setNumChildren(numChildren);

		updateActionBarViews();
	}

	public void setDates(Calendar checkIn, Calendar checkOut) {
		Log.d("Setting dates: " + checkIn.getTimeInMillis() + " to " + checkOut.getTimeInMillis());

		mInstance.mSearchParams.setCheckInDate(checkIn);
		mInstance.mSearchParams.setCheckOutDate(checkOut);

		updateActionBarViews();
	}

	public void setLatLng(double latitude, double longitude) {
		Log.d("Setting lat/lng: lat=" + latitude + ", lng=" + longitude);

		mInstance.mSearchParams.setSearchLatLon(latitude, longitude);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search

	public void startSearch() {
		Log.i("startSearch(): " + mInstance.mSearchParams.toJson().toString());

		notifyEventHandlers(EVENT_SEARCH_STARTED, null);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		// Cancel existing downloads
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_GEOCODE);

		// Remove existing search results
		mInstance.mSearchResponse = null;

		if (!NetUtils.isOnline(this)) {
			Log.w("startSearch() - no internet connection.");
			simulateSearchErrorResponse(R.string.error_no_internet);
			return;
		}

		// Determine search type, conduct search
		switch (mInstance.mSearchParams.getSearchType()) {
		case FREEFORM:
			if (mInstance.mSearchParams.hasSearchLatLon()) {
				startSearchDownloader();
			}
			else {
				startGeocode();
			}
			break;
		case PROXIMITY:
			// TODO: Implement PROXIMITY search (once a MapView is available)
			Log.w("PROXIMITY searches not yet supported!");
			break;
		case MY_LOCATION:
			long minTime = Calendar.getInstance().getTimeInMillis() - PhoneSearchActivity.MINIMUM_TIME_AGO;
			Location location = LocationServices.getLastBestLocation(this, minTime);
			if (location != null) {
				onMyLocationFound(location);
			}
			else {
				startLocationListener();
			}
			break;
		}
	}

	public void startGeocode() {
		Log.i("startGeocode(): " + mInstance.mSearchParams.getFreeformLocation());
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_GEOCODE, mGeocodeDownload, mGeocodeCallback);
	}

	private Download mGeocodeDownload = new Download() {
		public Object doDownload() {
			return LocationServices.geocode(mContext, mInstance.mSearchParams.getFreeformLocation());
		}
	};

	private OnDownloadComplete mGeocodeCallback = new OnDownloadComplete() {
		@SuppressWarnings("unchecked")
		public void onDownload(Object results) {
			List<Address> addresses = (List<Address>) results;

			if (addresses != null) {
				int size = addresses.size();
				if (size == 0) {
					Log.w("Geocode callback - got zero results.");
					onGeocodeFailure();
				}
				else if (size == 1) {
					onGeocodeSuccess(addresses.get(0));
				}
				else {
					Log.i("Geocode callback - got multiple results.");
					showGeocodeDisambiguationDialog(addresses);
				}
			}
			else {
				Log.w("Geocode callback - got null results.");
				onGeocodeFailure();
			}
		}
	};

	public void onGeocodeSuccess(Address address) {
		mInstance.mSearchParams.setFreeformLocation(address);
		updateActionBarViews();

		setLatLng(address.getLatitude(), address.getLongitude());

		startSearchDownloader();
	}

	public void onGeocodeFailure() {
		simulateSearchErrorResponse(R.string.geolocation_failed);
	}

	public void onMyLocationFound(Location location) {
		setLatLng(location.getLatitude(), location.getLongitude());
		startSearchDownloader();
	}

	public void startSearchDownloader() {
		Log.i("startSearchDownloader()");

		BackgroundDownloader.getInstance().startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private Download mSearchDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mInstance.mSession);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(mInstance.mSearchParams, 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			SearchResponse response = mInstance.mSearchResponse = (SearchResponse) results;

			if (response == null) {
				notifyEventHandlers(EVENT_SEARCH_ERROR, getString(R.string.progress_search_failed));
			}
			else {
				if (response.getSession() != null) {
					mInstance.mSession = response.getSession();
				}

				if (response.hasErrors()) {
					notifyEventHandlers(EVENT_SEARCH_ERROR, response.getErrors().get(0).getPresentableMessage(mContext));
				}
				else {
					notifyEventHandlers(EVENT_SEARCH_COMPLETE, response);
				}
			}
		}
	};

	private void simulateSearchErrorResponse(int errorMessageResId) {
		SearchResponse response = new SearchResponse();
		ServerError error = new ServerError();
		error.setPresentationMessage(getString(errorMessageResId));
		error.setCode("SIMULATED");
		response.addError(error);

		mSearchCallback.onDownload(response);
	}

	//////////////////////////////////////////////////////////////////////////
	// Hotel Details
	private Property mProperty;

	public void startRoomsAndRatesDownload(Property property) {
		mProperty = property;
		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(KEY_AVAILABILITY_SEARCH);
		bd.startDownload(KEY_AVAILABILITY_SEARCH, mRoomAvailabilityDownload, mRoomAvailabilityCallback);
		notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_STARTED, null);
	}

	private Download mRoomAvailabilityDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mInstance.mSession);
			return services.availability(mInstance.mSearchParams, mProperty);
		}
	};

	private OnDownloadComplete mRoomAvailabilityCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			AvailabilityResponse availabilityResponse = (AvailabilityResponse) results;

			if (availabilityResponse == null) {
				notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_ERROR, getString(R.string.error_no_response_room_rates));
			}
			else if (availabilityResponse.hasErrors()) {
				notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_ERROR, availabilityResponse.getErrors().get(0)
						.getPresentableMessage(mContext));
			}
			else {
				notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_COMPLETE, availabilityResponse);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Location

	private void startLocationListener() {
		notifyEventHandlers(EVENT_SEARCH_PROGRESS, getString(R.string.progress_finding_location));

		// Prefer network location (because it's faster).  Otherwise use GPS
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = null;
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}

		if (provider == null) {
			Log.w("Could not find a location provider, informing user of error...");
			simulateSearchErrorResponse(R.string.ProviderDisabled);

			// TODO: Show user dialog to go to enable location services
		}
		else {
			Log.i("Starting location listener, provider=" + provider);
			lm.requestLocationUpdates(provider, 0, 0, this);
		}
	}

	private void stopLocationListener() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		stopLocationListener();

		onMyLocationFound(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO: Worry about providers being disabled midway through search?
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO: Worry about providers being enabled midway through search?
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.w("onStatusChanged(): provider=" + provider + " status=" + status);

		if (status == LocationProvider.OUT_OF_SERVICE) {
			stopLocationListener();
			Log.w("Location listener failed: out of service");
			simulateSearchErrorResponse(R.string.ProviderOutOfService);
		}
		else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			stopLocationListener();
			Log.w("Location listener failed: temporarily unavailable");
			simulateSearchErrorResponse(R.string.ProviderTemporarilyUnavailable);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
