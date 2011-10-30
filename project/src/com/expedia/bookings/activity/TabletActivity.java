package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.fragment.BookingConfirmationFragment;
import com.expedia.bookings.fragment.BookingInfoFragment;
import com.expedia.bookings.fragment.BookingInfoFragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.BookingInfoFragment.ErrorBookingDialogFragment;
import com.expedia.bookings.fragment.BookingInfoFragment.NullBookingDialogFragment;
import com.expedia.bookings.fragment.BookingInfoValidation;
import com.expedia.bookings.fragment.BookingReceiptFragment;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.CompleteBookingInfoFragment;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.fragment.BookingCancellationPolicyFragment;
import com.expedia.bookings.fragment.FilterDialogFragment;
import com.expedia.bookings.fragment.GeocodeDisambiguationDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.expedia.bookings.fragment.HotelDetailsFragment;
import com.expedia.bookings.fragment.HotelGalleryDialogFragment;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.InstanceFragment;
import com.expedia.bookings.fragment.MiniDetailsFragment;
import com.expedia.bookings.fragment.NextOptionsFragment;
import com.expedia.bookings.fragment.QuickSearchFragment;
import com.expedia.bookings.fragment.RoomTypeDescriptionFragment;
import com.expedia.bookings.fragment.RoomsAndRatesFragment;
import com.expedia.bookings.fragment.SearchParamsFragment;
import com.expedia.bookings.fragment.SortDialogFragment;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
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
	public static final int EVENT_FILTER_CHANGED = 10;
	public static final int EVENT_SEARCH_PARAMS_CHANGED = 11;
	public static final int EVENT_REVIEWS_QUERY_STARTED = 12;
	public static final int EVENT_REVIEWS_QUERY_COMPLETE = 13;
	public static final int EVENT_REVIEWS_QUERY_ERROR = 14;
	public static final int EVENT_SEARCH_LOCATION_FOUND = 15;
	public static final int EVENT_PROPERTY_INFO_QUERY_STARTED = 16;
	public static final int EVENT_PROPERTY_INFO_QUERY_COMPLETE = 17;
	public static final int EVENT_PROPERTY_INFO_QUERY_ERROR = 18;
	public static final int EVENT_RATE_SELECTED = 19;
	public static final int EVENT_NEXT_FROM_SECURITY_CODE = 20;

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_AVAILABILITY_SEARCH = "KEY_AVAILABILITY_SEARCH";
	private static final String KEY_GEOCODE = "KEY_GEOCODE";
	private static final String KEY_REVIEWS = "KEY_REVIEWS";
	private static final String KEY_PROPERTY_INFO = "KEY_PROPERTY_INFO";
	private static final String KEY_BOOKING = "KEY_BOOKING";

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

		getFragmentManager().addOnBackStackChangedListener(this);

		if (ConfirmationUtils.hasSavedConfirmationData(this)) {
			if (loadSavedConfirmationData()) {
				// get the property info since that is not saved as part of the
				// confirmation data
				startPropertyInfoDownload(mInstance.mProperty);
				setupBookingConfirmationExperience();
			}
			else {
				ConfirmationUtils.deleteSavedConfirmationData(this);
			}
		}
		else {
			// Show initial search interface
			showSearchFragment();
		}

	}

	@Override
	public void onNewIntent(Intent newIntent) {
		if (Intent.ACTION_SEARCH.equals(newIntent.getAction())) {
			String query = newIntent.getStringExtra(SearchManager.QUERY);
			if (query.equals(getString(R.string.current_location))) {
				setMyLocationSearch();
			}
			else {
				setFreeformLocation(query);
			}

			startSearch();
		}
		else {
			super.onNewIntent(newIntent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_action_bar));

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
		mSearchView.setFocusable(false); // Fixes keyboard on submit, somehow!
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

		// Register the autocomplete provider
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
	private static final String TAG_SEARCH_PARAMS = "SEARCH_PARAMS";
	private static final String TAG_QUICK_SEARCH = "QUICK_SEARCH";
	private static final String TAG_HOTEL_LIST = "HOTEL_LIST";
	private static final String TAG_HOTEL_MAP = "HOTEL_MAP";
	private static final String TAG_HOTEL_DETAILS = "HOTEL_DETAILS";
	private static final String TAG_MINI_DETAILS = "MINI_DETAILS";
	private static final String TAG_AVAILABILITY_LIST = "TAG_AVAILABILITY_LIST";
	private static final String TAG_BOOKING_RECEIPT = "TAG_BOOKING_RECEIPT";
	private static final String TAG_BOOKING_RECEIPT_CONFIRMATION = "TAG_BOOKING_RECEIPT_CONFIRMATION";
	private static final String TAG_BOOKING_INFO = "TAG_BOOKING_INFO";
	private static final String TAG_BOOKING_CANCELLATION_POLICY = "TAG_BOOKING_CANCELLATION_POLICY";
	private static final String TAG_DIALOG_NULL_BOOKING = "TAG_DIALOG_NULL_BOOKING";
	private static final String TAG_DIALOG_BOOKING_ERROR = "TAG_DIALOG_BOOKING_ERROR";
	private static final String TAG_DIALOG_BOOKING_PROGRESS = "TAG_DIALOG_BOOKING_PROGRESS";
	private static final String TAG_CONFIRMATION = "TAG_CONFIRMATION";
	private static final String TAG_CONFIRMATION_CANCELLATION_POLICY = "TAG_CONFIRMATION_CANCELLATION_POLICY";
	private static final String TAG_ROOM_DESCRIPTION = "TAG_ROOM_DESCRIPTION";
	private static final String TAG_COMPLETE_BOOKING_INFO = "TAG_COMPLETE_BOOKING_INFO";
	private static final String TAG_NEXT_OPTIONS = "TAG_NEXT_OPTIONS";

	private static final String BACKSTACK_RESULTS = "RESULTS";

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

	public void showSearchFragment() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_SEARCH_PARAMS) == null) {
			FragmentTransaction ft = fm.beginTransaction();
			addStandardAnimation(ft);
			ft.add(R.id.fragment_search_params, SearchParamsFragment.newInstance(), TAG_SEARCH_PARAMS);
			ft.add(R.id.fragment_quick_search, QuickSearchFragment.newInstance(), TAG_QUICK_SEARCH);
			ft.commit();
		}
	}

	public void showResultsFragments() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentById(R.id.fragment_hotel_list) == null) {
			FragmentTransaction ft = fm.beginTransaction();
			addStandardAnimation(ft);
			ft.add(R.id.fragment_hotel_list, HotelListFragment.newInstance(), TAG_HOTEL_LIST);
			ft.add(R.id.fragment_hotel_map, HotelMapFragment.newInstance(), TAG_HOTEL_MAP);
			ft.remove(fm.findFragmentByTag(TAG_SEARCH_PARAMS));
			ft.remove(fm.findFragmentByTag(TAG_QUICK_SEARCH));
			ft.addToBackStack(BACKSTACK_RESULTS);
			ft.commit();
		}
		else {
			fm.popBackStack(BACKSTACK_RESULTS, 0);
		}
	}

	public void setupBookingInfoExperience() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		addStandardAnimation(ft);
		ft.add(R.id.fragment_rooms, RoomsAndRatesFragment.newInstance(), TAG_AVAILABILITY_LIST);
		ft.add(R.id.fragment_receipt, BookingReceiptFragment.newInstance(), TAG_BOOKING_RECEIPT);
		ft.add(R.id.fragment_complete_booking, CompleteBookingInfoFragment.newInstance(), TAG_COMPLETE_BOOKING_INFO);
		ft.add(R.id.fragment_booking_cancellation_policy, BookingCancellationPolicyFragment.newInstance(),
				TAG_BOOKING_CANCELLATION_POLICY);
		ft.add(R.id.fragment_room_descrption, RoomTypeDescriptionFragment.newInstance(), TAG_ROOM_DESCRIPTION);
		ft.remove(fm.findFragmentByTag(TAG_HOTEL_LIST));
		ft.remove(fm.findFragmentByTag(TAG_HOTEL_DETAILS));
		ft.addToBackStack(null);
		ft.commit();
	}

	public void setupBookingConfirmationExperience() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_BOOKING_RECEIPT_CONFIRMATION) == null) {
			getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			FragmentTransaction ft = getFragmentManager().beginTransaction();

			// remove the search fragments if they exist. We have to manually
			// remove them since they were not added to the backstack in the first place
			if (fm.findFragmentByTag(TAG_SEARCH_PARAMS) != null) {
				ft.remove(fm.findFragmentByTag(TAG_SEARCH_PARAMS));
				ft.remove(fm.findFragmentByTag(TAG_QUICK_SEARCH));
			}
			ft.add(R.id.fragment_confirmation_receipt, BookingReceiptFragment.newInstance(true),
					TAG_BOOKING_RECEIPT_CONFIRMATION);
			ft.add(R.id.fragment_confirmation_cancellation_policy, BookingCancellationPolicyFragment.newInstance(),
					TAG_CONFIRMATION_CANCELLATION_POLICY);
			ft.add(R.id.fragment_confirmation_map, BookingConfirmationFragment.newInstance(), TAG_CONFIRMATION);
			ft.add(R.id.fragment_next_options, NextOptionsFragment.newInstance(), TAG_NEXT_OPTIONS);
			ft.commit();

			// Start a background thread to save this data to the disk
			new Thread(new Runnable() {
				public void run() {
					try {
						ConfirmationUtils.saveConfirmationData(TabletActivity.this, mInstance.mSearchParams,
								mInstance.mProperty, mInstance.mRate, mInstance.mBillingInfo,
								mInstance.mBookingResponse);
					}
					catch (Exception e) {
						Log.e("Could not save Confirmation state", e);
					}
				}
			}).start();
		}
	}

	public void showMiniDetailsFragment() {
		MiniDetailsFragment fragment = MiniDetailsFragment.newInstance();

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (AndroidUtils.getSdkVersion() >= 13) {
			ft.setCustomAnimations(R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit, R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit);
		}
		else {
			ft.setCustomAnimations(R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit);
		}
		ft.add(R.id.fragment_mini_details, fragment, TAG_MINI_DETAILS);
		ft.addToBackStack(null);
		ft.commit();
	}

	public void showHotelDetailsFragment() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		addStandardAnimation(ft);
		ft.remove(fm.findFragmentByTag(TAG_HOTEL_MAP));
		ft.remove(fm.findFragmentByTag(TAG_MINI_DETAILS));
		ft.add(R.id.fragment_details, HotelDetailsFragment.newInstance(), TAG_HOTEL_DETAILS);
		ft.addToBackStack(null);
		ft.commit();
	}

	private void addStandardAnimation(FragmentTransaction ft) {
		// Only API lvl 13+ supports custom popEnter/popExit animations
		if (AndroidUtils.getSdkVersion() >= 13) {
			ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
					R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit);
		}
		else {
			ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Layout management
	// 
	// (includes OnBackStackChangedListener implementation)

	@Override
	public void onBackStackChanged() {
		Log.v("onBackStackChanged()");

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
		startReviewsDownload();
		startPropertyInfoDownload(mInstance.mProperty);
	}

	public void moreDetailsForPropertySelected() {
		// Ensure that a HotelDetailsFragment is being displayed
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_HOTEL_DETAILS) == null) {
			showHotelDetailsFragment();
		}
	}

	public void showPictureGalleryForHotel(String selectedImageUrl) {
		showHotelGalleryDialog(selectedImageUrl);
	}

	public void bookRoom(Rate rate) {
		mInstance.mRate = rate;

		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(TAG_AVAILABILITY_LIST) == null) {
			setupBookingInfoExperience();
		}

	}

	public void completeBookingInfo() {
		BookingInfoFragment.newInstance().show(getFragmentManager(), TAG_BOOKING_INFO);
	}

	public void rateSelected(Rate rate) {
		mInstance.mRate = rate;

		mEventManager.notifyEventHandlers(EVENT_RATE_SELECTED, null);

	}

	public void startNewSearchFromConfirmation() {
		// delete the confirmation data so that we dont
		// direct the user to it again
		ConfirmationUtils.deleteSavedConfirmationData(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.remove(getFragmentManager().findFragmentByTag(TAG_BOOKING_RECEIPT_CONFIRMATION));
		ft.remove(getFragmentManager().findFragmentByTag(TAG_NEXT_OPTIONS));
		ft.remove(getFragmentManager().findFragmentByTag(TAG_CONFIRMATION));
		ft.remove(getFragmentManager().findFragmentByTag(TAG_CONFIRMATION_CANCELLATION_POLICY));
		ft.commit();
		showSearchFragment();

	}

	public void focusOnRulesAndRestrictions() {
		// TODO
	}

	public void bookingCompleted(BillingInfo billingInfo) {
		mInstance.mBillingInfo = billingInfo;
		mInstance.mBookingResponse = null;
		BookingInProgressDialogFragment.newInstance().show(getFragmentManager(), TAG_DIALOG_BOOKING_PROGRESS);
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_BOOKING);
		bd.startDownload(KEY_BOOKING, mBookingDownload, mBookingCallback);

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

	public ReviewsResponse getReviewsForProperty() {
		return mInstance.mReviewsResponse;
	}

	public PropertyInfoResponse getInfoForProperty() {
		return mInstance.mPropertyInfoResponse;
	}

	public String getPropertyInfoQueryStatus() {
		return mInstance.mPropertyInfoStatus;
	}

	public Rate getRoomRateForBooking() {
		return mInstance.mRate;
	}

	public BookingInfoValidation getBookingInfoValidation() {
		if (mInstance.mBookingInfoValidation == null) {
			mInstance.mBookingInfoValidation = new BookingInfoValidation();
		}
		return mInstance.mBookingInfoValidation;
	}

	public BillingInfo getBillingInfo() {
		return mInstance.mBillingInfo;
	}

	public BookingResponse getBookingResponse() {
		return mInstance.mBookingResponse;
	}

	public boolean loadBillingInfo() {
		// load the billing info
		BillingInfo tmpInfo = new BillingInfo();
		if (tmpInfo.load(this)) {
			mInstance.mBillingInfo = tmpInfo;
			return true;
		}

		mInstance.mBillingInfo = new BillingInfo();
		return false;
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParams management

	public void setSearchParams(SearchParams searchParams) {
		Log.d("Setting entirely new set of search params: " + searchParams.toJson().toString());

		mInstance.mSearchParams = searchParams;

		invalidateOptionsMenu();

		mEventManager.notifyEventHandlers(EVENT_SEARCH_PARAMS_CHANGED, null);
	}

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

		// Remove existing search results (and references to it)
		mInstance.mSearchResponse = null;
		mInstance.mFilter.setOnDataListener(null);

		showResultsFragments();
		mInstance.mSearchStatus = getString(R.string.loading_hotels);
		mEventManager.notifyEventHandlers(EVENT_SEARCH_STARTED, null);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		// Cancel existing downloads
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_GEOCODE);

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

		// This method essentially signifies that we've found the location to search;
		// take this opportunity to notify handlers that we know where we're looking.
		mEventManager.notifyEventHandlers(EVENT_SEARCH_LOCATION_FOUND, null);

		// Save this as a "recent search" if it is a freeform search
		if (mInstance.mSearchParams.getSearchType() == SearchType.FREEFORM) {
			Search.add(this, mInstance.mSearchParams);
		}

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
				mInstance.mSearchStatus = getString(R.string.progress_search_failed);
				mEventManager.notifyEventHandlers(EVENT_SEARCH_ERROR, null);
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
	// Hotel Info

	private void startPropertyInfoDownload(Property property) {
		//clear out previous results
		mInstance.mPropertyInfoResponse = null;
		mInstance.mPropertyInfoStatus = null;

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(KEY_PROPERTY_INFO);
		bd.startDownload(KEY_PROPERTY_INFO, mPropertyInfoDownload, mPropertyInfoCallback);
	}

	private Download mPropertyInfoDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			return services.info(mInstance.mProperty);
		}
	};

	private OnDownloadComplete mPropertyInfoCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			mInstance.mPropertyInfoResponse = (PropertyInfoResponse) results;
			mInstance.mPropertyInfoStatus = null;

			if (mInstance.mPropertyInfoResponse == null) {
				mInstance.mPropertyInfoStatus = getString(R.string.error_room_type_load);
				mEventManager.notifyEventHandlers(EVENT_PROPERTY_INFO_QUERY_ERROR, null);
			}
			else {
				mEventManager.notifyEventHandlers(EVENT_PROPERTY_INFO_QUERY_COMPLETE, null);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Hotel Reviews

	private void startReviewsDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_REVIEWS);
		bd.startDownload(KEY_REVIEWS, mReviewsDownload, mReviewsCallback);
		mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_STARTED, null);
	}

	private Download mReviewsDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mInstance.mSession);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REVIEWS, services);
			return services.reviews(mInstance.mProperty, 1, ReviewSort.HIGHEST_RATING_FIRST);
		}
	};

	private OnDownloadComplete mReviewsCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			mInstance.mReviewsResponse = (ReviewsResponse) results;

			if (results == null) {
				mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_ERROR, null);
			}
			else if (mInstance.mReviewsResponse.hasErrors()) {
				mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_ERROR, mInstance.mReviewsResponse.getErrors()
						.get(0).getPresentableMessage(mContext));
			}
			else {
				mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_COMPLETE, mInstance.mReviewsResponse);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Hotel Booking

	private OnDownloadComplete mBookingCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			((BookingInProgressDialogFragment) getFragmentManager().findFragmentByTag(TAG_DIALOG_BOOKING_PROGRESS))
					.dismiss();

			if (results == null) {
				NullBookingDialogFragment.newInstance().show(getFragmentManager(), TAG_DIALOG_NULL_BOOKING);
				TrackingUtils.trackErrorPage(TabletActivity.this, "ReservationRequestFailed");
				return;
			}

			BookingResponse response = mInstance.mBookingResponse = (BookingResponse) results;
			if (response.hasErrors()) {
				ErrorBookingDialogFragment.newInstance().show(getFragmentManager(), TAG_DIALOG_BOOKING_ERROR);
				TrackingUtils.trackErrorPage(TabletActivity.this, "ReservationRequestFailed");
				return;
			}

			mInstance.mSession = response.getSession();

			Intent intent = new Intent(TabletActivity.this, ConfirmationActivity.class);
			intent.fillIn(getIntent(), 0);
			intent.putExtra(Codes.BOOKING_RESPONSE, response.toJson().toString());
			intent.putExtra(Codes.SESSION, mInstance.mSession.toJson().toString());
			if (mInstance.mPropertyInfoResponse != null) {
				intent.putExtra(Codes.PROPERTY_INFO, mInstance.mPropertyInfoResponse.getPropertyInfo().toJson()
						.toString());
			}
			// Create a BillingInfo that lacks the user's security code (for safety)
			JSONObject billingJson = mInstance.mBillingInfo.toJson();
			billingJson.remove("securityCode");
			intent.putExtra(Codes.BILLING_INFO, billingJson.toString());
			setupBookingConfirmationExperience();
		}
	};

	private Download mBookingDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(TabletActivity.this, mInstance.mSession);
			return services.reservation(mInstance.mSearchParams, mInstance.mProperty, mInstance.mRate,
					mInstance.mBillingInfo);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Location

	private void startLocationListener() {
		mInstance.mSearchStatus = getString(R.string.progress_finding_location);
		mEventManager.notifyEventHandlers(EVENT_SEARCH_PROGRESS, null);

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

	//////////////////////////////////////////////////////////////////////////
	// Confirmation data

	public boolean loadSavedConfirmationData() {
		Log.i("Loading saved confirmation data...");
		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(ConfirmationUtils.CONFIRMATION_DATA_FILE, this));
			mInstance.mSearchParams = (SearchParams) JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS,
					SearchParams.class);
			mInstance.mProperty = (Property) JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class);
			mInstance.mRate = (Rate) JSONUtils.getJSONable(data, Codes.RATE, Rate.class);
			mInstance.mBillingInfo = (BillingInfo) JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class);
			mInstance.mBookingResponse = (BookingResponse) JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE,
					BookingResponse.class);
			return true;
		}
		catch (Exception e) {
			Log.e("Could not load ConfirmationActivity state.", e);
			return false;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
