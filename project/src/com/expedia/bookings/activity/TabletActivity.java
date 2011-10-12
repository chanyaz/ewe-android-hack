package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.List;

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
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.fragment.FilterDialogFragment;
import com.expedia.bookings.fragment.GeocodeDisambiguationDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.expedia.bookings.fragment.HotelDetailsFragment;
import com.expedia.bookings.fragment.HotelGalleryDialogFragment;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.InstanceFragment;
import com.expedia.bookings.fragment.MiniDetailsFragment;
import com.expedia.bookings.fragment.SearchFragment;
import com.expedia.bookings.fragment.SortDialogFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.util.NetUtils;

public class TabletActivity extends MapActivity implements LocationListener, OnBackStackChangedListener,
		OnFilterChangedListener {

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
	public static final int EVENT_FILTER_CHANGED = 10;
	public static final int EVENT_SEARCH_PARAMS_CHANGED = 11;

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_AVAILABILITY_SEARCH = "KEY_AVAILABILITY_SEARCH";
	private static final String KEY_GEOCODE = "KEY_GEOCODE";

	//////////////////////////////////////////////////////////////////////////
	// Fragments

	private InstanceFragment mInstance;

	//////////////////////////////////////////////////////////////////////////
	// Event handling

	private EventManager mEventManager = EventManager.getInstance();

	public boolean registerEventHandler(EventHandler eventHandler) {
		return mEventManager.registerEventHandler(eventHandler);
	}

	public boolean unregisterEventHandler(EventHandler eventHandler) {
		return mEventManager.unregisterEventHandler(eventHandler);
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mResources = getResources();

		initializeInstanceFragment();

		setContentView(R.layout.activity_tablet);

		initializeFragmentViews();

		getFragmentManager().addOnBackStackChangedListener(this);

		// Show initial search interface
		showSearchFragment();

		// if the device was rotated, update layout 
		// to ensure that containers with fragments in them
		// are visible
		if (savedInstanceState != null) {
			updateLayout();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		mInstance.mFilter.addOnFilterChangedListener(this);
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

		mInstance.mFilter.removeOnFilterChangedListener(this);
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
	private MenuItem mFilterMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tablet, menu);

		mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		mGuestsMenuItem = menu.findItem(R.id.menu_guests);
		mDatesMenuItem = menu.findItem(R.id.menu_dates);
		mFilterMenuItem = menu.findItem(R.id.menu_filter);

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

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_HOTEL_LIST) != null) {
			menu.setGroupVisible(R.id.search_location_group, true);
			menu.setGroupVisible(R.id.filter_group, true);
			menu.setGroupVisible(R.id.search_options_group, true);

			mSearchView.setQuery(mInstance.mSearchParams.getSearchDisplayText(this), false);

			int numGuests = mInstance.mSearchParams.getNumAdults() + mInstance.mSearchParams.getNumChildren();
			mGuestsMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests));

			int numNights = mInstance.mSearchParams.getStayDuration();
			mDatesMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_nights, numNights, numNights));

			mFilterMenuItem.setEnabled(mInstance.mSearchResponse != null && !mInstance.mSearchResponse.hasErrors());
		}
		else {
			menu.setGroupVisible(R.id.search_location_group, false);
			menu.setGroupVisible(R.id.filter_group, false);
			menu.setGroupVisible(R.id.search_options_group, false);
		}

		return super.onPrepareOptionsMenu(menu);
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
			showFilterDialog();
			return true;
		case R.id.menu_about:
			// TODO: Launch About fragment
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment management

	private static final String TAG_INSTANCE_FRAGMENT = "INSTANCE_FRAGMENT";
	private static final String TAG_SEARCH = "SEARCH";
	private static final String TAG_HOTEL_LIST = "HOTEL_LIST";
	private static final String TAG_HOTEL_MAP = "HOTEL_MAP";
	private static final String TAG_HOTEL_DETAILS = "HOTEL_DETAILS";
	private static final String TAG_MINI_DETAILS = "MINI_DETAILS";

	private View mLauncherFragmentContainer;
	private View mSearchFragmentsContainer;
	private View mLeftFragmentContainer;
	private View mRightFragmentContainer;
	private View mBottomRightFragmentContainer;

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
		mLauncherFragmentContainer = findViewById(R.id.fragment_launcher);
		mSearchFragmentsContainer = findViewById(R.id.fragment_search);
		mLeftFragmentContainer = findViewById(R.id.fragment_left);
		mRightFragmentContainer = findViewById(R.id.fragment_right);
		mBottomRightFragmentContainer = findViewById(R.id.fragment_bottom_right);
	}

	public void showSearchFragment() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentById(R.id.fragment_launcher) == null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fragment_launcher, SearchFragment.newInstance(), TAG_SEARCH);
			ft.commit();
		}
	}

	public void showResultsFragments() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentById(R.id.fragment_left) == null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fragment_left, HotelListFragment.newInstance(), TAG_HOTEL_LIST);
			ft.add(R.id.fragment_right, HotelMapFragment.newInstance(), TAG_HOTEL_MAP);
			ft.hide(fm.findFragmentById(R.id.fragment_launcher));
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	public void showMiniDetailsFragment() {
		MiniDetailsFragment fragment = MiniDetailsFragment.newInstance();

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
			ft.remove(fragment);
		}
		ft.replace(R.id.fragment_bottom_right, hotelDetailsFragment, TAG_HOTEL_DETAILS);
		ft.addToBackStack(null);
		ft.commit();
	}

	//////////////////////////////////////////////////////////////////////////
	// Layout management
	// 
	// (includes OnBackStackChangedListener implementation)

	public void updateLayout() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_HOTEL_LIST) != null) {
			mLauncherFragmentContainer.setVisibility(View.GONE);
			mSearchFragmentsContainer.setVisibility(View.VISIBLE);
		}
		else {
			mLauncherFragmentContainer.setVisibility(View.VISIBLE);
			mSearchFragmentsContainer.setVisibility(View.GONE);
		}

		updateContainerVisibility(mLeftFragmentContainer);
		updateContainerVisibility(mRightFragmentContainer);
		updateContainerVisibility(mBottomRightFragmentContainer);
	}

	/*
	 * This method makes "gone" the containers that either dont
	 * have any views at all, or none that are added to the 
	 * activity
	 */
	private void updateContainerVisibility(View container) {
		Fragment fragment = getFragmentManager().findFragmentById(container.getId());
		container.setVisibility((fragment != null && fragment.isAdded()) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onBackStackChanged() {
		Log.v("onBackStackChanged()");

		updateLayout();

		invalidateOptionsMenu();
	}

	//////////////////////////////////////////////////////////////////////////
	// Dialogs

	private void showGuestsDialog() {
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

	private void showFilterDialog() {
		DialogFragment newFragment = FilterDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "FilterDialog");
	}

	public void showSortDialog() {
		DialogFragment newFragment = SortDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "SortDialog");
	}

	private void showHotelGalleryDialog(String selectedImageUrl) {
		DialogFragment newFragment = HotelGalleryDialogFragment.newInstance(selectedImageUrl);
		newFragment.show(getFragmentManager(), "HotelGalleryDialog");
	}

	//////////////////////////////////////////////////////////////////////////
	// Events (called from Fragments)

	public void propertySelected(Property property) {
		mInstance.mProperty = property;
		Log.v("propertySelected(): " + property.getName());

		// Ensure that a MiniDetailsFragment is being displayed
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_MINI_DETAILS) == null) {
			showMiniDetailsFragment();
		}

		mEventManager.notifyEventHandlers(EVENT_PROPERTY_SELECTED, property);

		// start downloading the availability response for this property
		// ahead of time (from when it might actually be needed) so that 
		// the results are instantly displayed in the hotel details view to the user
		startRoomsAndRatesDownload(mInstance.mProperty);
	}

	public void moreDetailsForPropertySelected() {
		// Ensure that a HotelDetailsFragment is being displayed
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_HOTEL_DETAILS) == null) {
			showHotelDetailsFragment();
		}

		mEventManager.notifyEventHandlers(EVENT_DETAILS_OPENED, mInstance.mProperty);
	}

	
	public void showPictureGalleryForHotel(String selectedImageUrl) {
		showHotelGalleryDialog(selectedImageUrl);		
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Data access

	public SearchParams getSearchParams() {
		return mInstance.mSearchParams;
	}

	public String getSearchStatus() {
		return mInstance.mSearchStatus;
	}

	public Property getPropertyToDisplay() {
		return mInstance.mProperty;
	}

	public SearchResponse getSearchResultsToDisplay() {
		return mInstance.mSearchResponse;
	}

	public AvailabilityResponse getRoomsAndRatesAvailability() {
		return mInstance.mAvailabilityResponse;
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParams management

	public void setMyLocationSearch() {
		Log.d("Setting search to use 'my location'");

		mInstance.mSearchParams.setSearchType(SearchType.MY_LOCATION);

		invalidateOptionsMenu();

		mEventManager.notifyEventHandlers(EVENT_SEARCH_PARAMS_CHANGED, null);
	}

	public void setFreeformLocation(String freeformLocation) {
		Log.d("Setting freeform location: " + freeformLocation);

		mInstance.mSearchParams.setFreeformLocation(freeformLocation);
		mInstance.mSearchParams.setSearchType(SearchType.FREEFORM);

		invalidateOptionsMenu();

		mEventManager.notifyEventHandlers(EVENT_SEARCH_PARAMS_CHANGED, null);
	}

	public void setGuests(int numAdults, int numChildren) {
		Log.d("Setting guests: " + numAdults + " adult(s), " + numChildren + " child(ren)");

		mInstance.mSearchParams.setNumAdults(numAdults);
		mInstance.mSearchParams.setNumChildren(numChildren);

		invalidateOptionsMenu();

		mEventManager.notifyEventHandlers(EVENT_SEARCH_PARAMS_CHANGED, null);
	}

	public void setDates(Calendar checkIn, Calendar checkOut) {
		Log.d("Setting dates: " + checkIn.getTimeInMillis() + " to " + checkOut.getTimeInMillis());

		mInstance.mSearchParams.setCheckInDate(checkIn);
		mInstance.mSearchParams.setCheckOutDate(checkOut);

		invalidateOptionsMenu();

		mEventManager.notifyEventHandlers(EVENT_SEARCH_PARAMS_CHANGED, null);
	}

	public void setLatLng(double latitude, double longitude) {
		Log.d("Setting lat/lng: lat=" + latitude + ", lng=" + longitude);

		mInstance.mSearchParams.setSearchLatLon(latitude, longitude);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search

	public void startSearch() {
		Log.i("startSearch(): " + mInstance.mSearchParams.toJson().toString());

		showResultsFragments();

		mEventManager.notifyEventHandlers(EVENT_SEARCH_STARTED, null);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		// Cancel existing downloads
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_GEOCODE);

		// Remove existing search results (and references to it)
		mInstance.mSearchResponse = null;
		mInstance.mFilter.setOnDataListener(null);

		// Let action bar views react to change in state (downloading)
		invalidateOptionsMenu();

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
		invalidateOptionsMenu();

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
				mEventManager.notifyEventHandlers(EVENT_SEARCH_ERROR, getString(R.string.progress_search_failed));
			}
			else {
				if (response.getSession() != null) {
					mInstance.mSession = response.getSession();
				}

				if (response.hasErrors()) {
					mEventManager.notifyEventHandlers(EVENT_SEARCH_ERROR, response.getErrors().get(0)
							.getPresentableMessage(mContext));
				}
				else {
					response.setFilter(mInstance.mFilter);

					mEventManager.notifyEventHandlers(EVENT_SEARCH_COMPLETE, response);
				}
			}

			// Update action bar views based on results
			invalidateOptionsMenu();
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
	// OnFilterChangedListener implementation

	@Override
	public void onFilterChanged() {
		mEventManager.notifyEventHandlers(EVENT_FILTER_CHANGED, null);
	}

	//////////////////////////////////////////////////////////////////////////
	// Hotel Details

	private void startRoomsAndRatesDownload(Property property) {
		mInstance.mProperty = property;

		// clear out previous results
		mInstance.mAvailabilityResponse = null;

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(KEY_AVAILABILITY_SEARCH);
		bd.startDownload(KEY_AVAILABILITY_SEARCH, mRoomAvailabilityDownload, mRoomAvailabilityCallback);
		mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_STARTED, null);
	}

	private Download mRoomAvailabilityDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mInstance.mSession);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_AVAILABILITY_SEARCH, services);
			return services.availability(mInstance.mSearchParams, mInstance.mProperty);
		}
	};

	private OnDownloadComplete mRoomAvailabilityCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			AvailabilityResponse availabilityResponse = mInstance.mAvailabilityResponse = (AvailabilityResponse) results;

			if (availabilityResponse == null) {
				mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_ERROR,
						getString(R.string.error_no_response_room_rates));
			}
			else if (availabilityResponse.hasErrors()) {
				mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_ERROR, availabilityResponse.getErrors()
						.get(0).getPresentableMessage(mContext));
			}
			else {
				mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_COMPLETE, availabilityResponse);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Location

	private void startLocationListener() {
		mEventManager.notifyEventHandlers(EVENT_SEARCH_PROGRESS, getString(R.string.progress_finding_location));

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

	// This is a workaround.  We are only allowed to create a MapView *once* per MapActivity, even if
	// we delete the Fragment that had the MapView from the backstack (thus clearing the old MapView).
	// Anytime you need to use a MapView, use this one.
	private MapView mMapView;

	public MapView getMapView() {
		if (mMapView == null) {
			mMapView = MapUtils.createMapView(this);
		}

		return mMapView;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
