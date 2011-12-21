package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Filter.SearchRadius;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.fragment.FilterDialogFragment;
import com.expedia.bookings.fragment.GeocodeDisambiguationDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.expedia.bookings.fragment.HotelDetailsFragment;
import com.expedia.bookings.fragment.MiniDetailsFragment;
import com.expedia.bookings.fragment.SortDialogFragment;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.AvailabilitySummaryLayoutUtils.OnRateClickListener;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.SearchUtils;
import com.expedia.bookings.widget.SummarizedRoomRates;
import com.google.android.maps.MapActivity;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.omniture.AppMeasurement;

public class SearchResultsFragmentActivity extends MapActivity implements LocationListener, OnFilterChangedListener {

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
	public static final int EVENT_FILTER_CHANGED = 9;
	public static final int EVENT_SEARCH_PARAMS_CHANGED = 10;
	public static final int EVENT_REVIEWS_QUERY_STARTED = 11;
	public static final int EVENT_REVIEWS_QUERY_COMPLETE = 12;
	public static final int EVENT_REVIEWS_QUERY_ERROR = 13;
	public static final int EVENT_SEARCH_LOCATION_FOUND = 14;
	public static final int EVENT_SETTINGS_CHANGED = 15;

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_AVAILABILITY_SEARCH = "KEY_AVAILABILITY_SEARCH";
	private static final String KEY_GEOCODE = "KEY_GEOCODE";
	private static final String KEY_REVIEWS = "KEY_REVIEWS";

	private static final int REQUEST_CODE_SETTINGS = 1;

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private Context mContext;
	private Resources mResources;

	public EventManager mEventManager = new EventManager();
	public InstanceFragment mInstance;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mContext = this;
		mResources = getResources();

		FragmentManager fm = getFragmentManager();
		mInstance = (InstanceFragment) fm.findFragmentByTag(InstanceFragment.TAG);
		if (mInstance == null) {
			mInstance = InstanceFragment.newInstance();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(mInstance, InstanceFragment.TAG);
			ft.commit();

			// Fill in data from calling intent
			Intent intent = getIntent();
			mInstance.mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
					SearchParams.class);
		}

		setContentView(R.layout.activity_search_results_fragment);

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		// Load initial data, if it already exists (aka, screen rotated)
		if (mInstance.mSearchResponse != null) {
			loadSearchResponse(mInstance.mSearchResponse, false);
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

		// Configure the ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_action_bar));

		mSearchView = new SearchView(this);
		actionBar.setCustomView(mSearchView);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);

		// Configure the SearchView
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

		mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				mSearchViewFocused = hasFocus;

				if (hasFocus) {
					String currQuery = mSearchView.getQuery().toString();
					if (currQuery.equals(getString(R.string.current_location))) {
						mSearchView.setQuery("", false);
					}
					else if (currQuery.length() == 0) {
						// #10908: If the SearchView is focused when it has no text in it, then it won't fire off
						// an autocomplete query.  By doing resetting the query to the blank string, we invoke an 
						// autocomplete query (even though it seems like this call is completely redundant).
						mSearchView.setQuery("", false);
					}
				}
				else {
					mSearchView.setQuery(mInstance.mSearchParams.getSearchDisplayText(mContext), false);
				}
			}
		});

		// Register the autocomplete provider
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		// #10515: Clear focus on SearchView when user clicks a suggestion
		mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
			public boolean onSuggestionSelect(int position) {
				mSearchView.clearFocus();
				return false;
			}

			public boolean onSuggestionClick(int position) {
				mSearchView.clearFocus();
				return false;
			}
		});

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
			if (bd.isDownloading(KEY_AVAILABILITY_SEARCH)) {
				bd.registerDownloadCallback(KEY_AVAILABILITY_SEARCH, mRoomAvailabilityCallback);
			}
			if (bd.isDownloading(KEY_REVIEWS)) {
				bd.registerDownloadCallback(KEY_REVIEWS, mReviewsCallback);
			}
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
		bd.unregisterDownloadCallback(KEY_AVAILABILITY_SEARCH);
		bd.unregisterDownloadCallback(KEY_REVIEWS);
	}

	@Override
	protected void onStop() {
		super.onStop();

		mInstance.mFilter.removeOnFilterChangedListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.cancelDownload(KEY_GEOCODE);
			bd.cancelDownload(KEY_SEARCH);
			bd.cancelDownload(KEY_AVAILABILITY_SEARCH);
			bd.cancelDownload(KEY_REVIEWS);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Activity overrides

	@Override
	public void finish() {
		// Store the search params going backwards, for the SearchFragmentActivity to use
		Intent data = new Intent();
		data.putExtra(Codes.SEARCH_PARAMS, mInstance.mSearchParams.toString());
		setResult(RESULT_OK, data);

		super.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Indicates that settings were changed - send out a broadcast
		if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
			Log.i("Detected currency settings change.");
			startSearch();
			mEventManager.notifyEventHandlers(EVENT_SETTINGS_CHANGED, null);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// We're ensuring that if the user clicks somewhere else on the screen while the SearchView is focused,
		// we clear focus on the SearchView.
		if (mSearchViewFocused) {
			Rect bounds = new Rect();
			mSearchView.getHitRect(bounds);
			if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
				mSearchView.clearFocus();
			}
		}

		return super.dispatchTouchEvent(ev);
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	private SearchView mSearchView;
	private MenuItem mGuestsMenuItem;
	private MenuItem mDatesMenuItem;
	private MenuItem mFilterMenuItem;

	private boolean mSearchViewFocused = false;
	private boolean mUseCondensedActionBar = false;

	private TextView mGuestsTextView;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_fragment_search, menu);
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		mGuestsMenuItem = menu.findItem(R.id.menu_guests);
		mDatesMenuItem = menu.findItem(R.id.menu_dates);
		mFilterMenuItem = menu.findItem(R.id.menu_filter);

		// Use a condensed ActionBar if the screen width is not large enough
		if (AndroidUtils.getSdkVersion() >= 13) {
			mUseCondensedActionBar = mResources.getConfiguration().screenWidthDp <= 800;
		}
		else {
			mUseCondensedActionBar = mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		}

		if (mUseCondensedActionBar) {
			mFilterMenuItem.setTitle(R.string.filter);

			// Configure the custom action view (which is more condensed than the normal one
			mGuestsMenuItem.setActionView(R.layout.action_menu_item_guests);
			View actionView = mGuestsMenuItem.getActionView();
			View button = actionView.findViewById(R.id.guests_button);
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					showGuestsDialog();
				}
			});
			mGuestsTextView = (TextView) actionView.findViewById(R.id.guests_text_view);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// This can be run before we've actually initialized the options menu - in that case, don't run
		// the preparation (it will get run on its own course later).
		if (mSearchView == null) {
			return super.onPrepareOptionsMenu(menu);
		}

		if (!mSearchViewFocused) {
			mSearchView.setQuery(mInstance.mSearchParams.getSearchDisplayText(this), false);
		}

		int numGuests = mInstance.mSearchParams.getNumAdults() + mInstance.mSearchParams.getNumChildren();
		if (mUseCondensedActionBar) {
			mGuestsTextView.setText(numGuests + "");
		}
		else {
			mGuestsMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests));
		}

		int numNights = mInstance.mSearchParams.getStayDuration();
		mDatesMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_nights, numNights, numNights));

		mFilterMenuItem.setEnabled(mInstance.mSearchResponse != null && !mInstance.mSearchResponse.hasErrors());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.menu_guests:
			showGuestsDialog();
			return true;
		case R.id.menu_dates:
			showCalendarDialog();
			return true;
		case R.id.menu_filter:
			showFilterDialog();
			return true;
		case R.id.menu_settings: {
			Intent intent = new Intent(this, TabletPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SETTINGS);
			return true;
		}
		case R.id.menu_about: {
			Intent intent = new Intent(this, TabletAboutActivity.class);
			startActivity(intent);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Event handling

	public static final int SOURCE_LIST = 1;
	public static final int SOURCE_MAP = 2;
	public static final int SOURCE_MINI_DETAILS = 3;

	public void propertySelected(Property property, int source) {
		Log.v("propertySelected(): " + property.getName());

		boolean selectionChanged = (mInstance.mProperty != property);

		// Ensure that the proper view is being displayed.
		//
		// If the user isn't viewing anything, bring up mini details.  If some details are up, either expand to
		// full details or just changed the selected property (based on the state when this property is selected).
		FragmentManager fm = getFragmentManager();
		boolean miniDetailsShowing = fm.findFragmentByTag(getString(R.string.tag_mini_details)) != null;
		boolean detailsShowing = fm.findFragmentByTag(getString(R.string.tag_details)) != null;
		if (!miniDetailsShowing && !detailsShowing) {
			showMiniDetailsFragment();
		}
		else if (!selectionChanged && miniDetailsShowing) {
			showHotelDetailsFragment();
		}

		// When the selected property changes, a few things need to be done.
		if (selectionChanged) {
			// Clear out the previous property's images from the cache
			if (mInstance.mProperty != null) {
				if (mInstance.mProperty.getMediaCount() > 0) {
					for (Media media : mInstance.mProperty.getMediaList()) {
						ImageCache.removeImage(media.getActiveUrl(), true);
						ImageCache.removeImage(media.getUrl(), true);
					}
				}
			}

			mInstance.mProperty = property;

			// start downloading the availability response for this property
			// ahead of time (from when it might actually be needed) so that 
			// the results are instantly displayed in the hotel details view to the user
			startRoomsAndRatesDownload(mInstance.mProperty);
			startReviewsDownload();

			// notify the necessary components only after starting the 
			// downloads so that the right downlaod information (such as  is picked up by the components
			// when notified of the change in property
			mEventManager.notifyEventHandlers(EVENT_PROPERTY_SELECTED, property);
		}

		// Track what was just pressed
		if ((!selectionChanged && !detailsShowing) || (selectionChanged && detailsShowing)) {
			// Track that the full details has a pageload
			Log.d("Tracking \"App.Hotels.Details\" pageLoad");

			AppMeasurement s = TrackingUtils.createSimpleEvent(this, "App.Hotels.Details", "event32", "Shopper", null);

			TrackingUtils.addHotelRating(s, property);

			s.eVar8 = property.getLowestRate().getPromoDescription();

			s.track();
		}
		else if (selectionChanged && !detailsShowing) {
			// Track that the mini details has a pageload
			Log.d("Tracking \"App.Hotels.Search.QuickView\" onClick");

			String referrer = null;
			if (source == SOURCE_LIST) {
				referrer = "App.Hotels.Search.QuickView.List";
			}
			else if (source == SOURCE_MAP) {
				referrer = "App.Hotels.Search.QuickView.Map";
			}

			AppMeasurement s = TrackingUtils.createSimpleEvent(this, "App.Hotels.Search.QuickView", null, "Shopper",
					referrer);

			s.eVar8 = property.getLowestRate().getPromoDescription();

			s.track();
		}
	}

	public void moreDetailsForPropertySelected(int source) {
		propertySelected(mInstance.mProperty, source);
	}

	//////////////////////////////////////////////////////////////////////////
	// Data access / InstanceFragment

	public static final class InstanceFragment extends Fragment {
		public static final String TAG = "INSTANCE";

		public static InstanceFragment newInstance() {
			InstanceFragment fragment = new InstanceFragment();
			fragment.setRetainInstance(true);
			return fragment;
		}

		public SearchParams mSearchParams = new SearchParams();
		public String mSearchStatus;
		public boolean mShowDistance;
		public SearchResponse mSearchResponse;
		public Property mProperty;
		public Filter mFilter = new Filter();
		public Map<String, AvailabilityResponse> mAvailabilityResponses = new HashMap<String, AvailabilityResponse>();
		public Map<String, ReviewsResponse> mReviewsResponses = new HashMap<String, ReviewsResponse>();
		public Session mSession;

		// For tracking purposes only
		public SearchParams mLastSearchParams;
		public Filter mLastFilter;
	}

	public AvailabilityResponse getRoomsAndRatesAvailability() {
		return mInstance.mAvailabilityResponses.get(mInstance.mProperty.getPropertyId());
	}

	public ReviewsResponse getReviewsForProperty() {
		return mInstance.mReviewsResponses.get(mInstance.mProperty.getPropertyId());
	}

	public SummarizedRoomRates getSummarizedRoomRates() {
		return getRoomsAndRatesAvailability().getSummarizedRoomRates();
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment management

	private static final String MINI_DETAILS_PUSH = "mini_details_push";

	public void showMiniDetailsFragment() {
		MiniDetailsFragment fragment = MiniDetailsFragment.newInstance();

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (AndroidUtils.getSdkVersion() >= 13) {
			ft.setCustomAnimations(R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit, R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit);
		}
		ft.add(R.id.fragment_mini_details, fragment, getString(R.string.tag_mini_details));
		ft.addToBackStack(MINI_DETAILS_PUSH);
		ft.commit();
	}

	public void showHotelDetailsFragment() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_details)) == null) {
			FragmentTransaction ft = fm.beginTransaction();
			if (AndroidUtils.getSdkVersion() >= 13) {
				ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
						R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit);
			}
			ft.hide(fm.findFragmentByTag(getString(R.string.tag_hotel_map)));
			ft.remove(fm.findFragmentByTag(getString(R.string.tag_mini_details)));
			ft.add(R.id.fragment_details, HotelDetailsFragment.newInstance(), getString(R.string.tag_details));
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	public void hideDetails() {
		FragmentManager fm = getFragmentManager();
		fm.popBackStack(MINI_DETAILS_PUSH, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	//////////////////////////////////////////////////////////////////////////
	// Dialogs

	private void showGuestsDialog() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_guests_dialog)) == null) {
			DialogFragment newFragment = GuestsDialogFragment.newInstance(mInstance.mSearchParams.getNumAdults(),
					mInstance.mSearchParams.getNumChildren());
			newFragment.show(fm, getString(R.string.tag_guests_dialog));
		}
	}

	private void showCalendarDialog() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_calendar_dialog)) == null) {
			DialogFragment newFragment = CalendarDialogFragment.newInstance(mInstance.mSearchParams.getCheckInDate(),
					mInstance.mSearchParams.getCheckOutDate());
			newFragment.show(getFragmentManager(), getString(R.string.tag_calendar_dialog));
		}
	}

	private void showGeocodeDisambiguationDialog(List<Address> addresses) {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_geocode_disambiguation_dialog)) == null) {
			DialogFragment newFragment = GeocodeDisambiguationDialogFragment.newInstance(addresses);
			newFragment.show(getFragmentManager(), getString(R.string.tag_geocode_disambiguation_dialog));
		}
	}

	private void showFilterDialog() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_filter_dialog)) == null) {
			DialogFragment newFragment = FilterDialogFragment.newInstance();
			newFragment.show(getFragmentManager(), getString(R.string.tag_filter_dialog));
		}
	}

	public void showSortDialog() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_sort_dialog)) == null) {
			DialogFragment newFragment = SortDialogFragment.newInstance();
			newFragment.show(getFragmentManager(), getString(R.string.tag_sort_dialog));
		}
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

		// Remove existing search results (and references to it)
		mInstance.mSearchResponse = null;
		mInstance.mFilter.setOnDataListener(null);
		mInstance.mAvailabilityResponses.clear();
		mInstance.mReviewsResponses.clear();

		// Reset the filter on each search
		mInstance.mFilter.reset();

		mInstance.mSearchStatus = getString(R.string.loading_hotels);
		mEventManager.notifyEventHandlers(EVENT_SEARCH_STARTED, null);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		// Cancel existing downloads
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_GEOCODE);

		// Clear the image cache
		ImageCache.recycleCache(true);

		// Reset the views
		hideDetails();

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

		mInstance.mSearchParams.setUserFreeformLocation(mInstance.mSearchParams.getFreeformLocation());

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

		mInstance.mShowDistance = SearchUtils.isExactLocation(address);

		startSearchDownloader();
	}

	public void onGeocodeFailure() {
		simulateSearchErrorResponse(R.string.geolocation_failed);

		TrackingUtils.trackErrorPage(this, "App.Error.LocationNotFound");
	}

	public void onMyLocationFound(Location location) {
		setLatLng(location.getLatitude(), location.getLongitude());
		mInstance.mShowDistance = true;
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
			loadSearchResponse((SearchResponse) results, true);
		}
	};

	private void loadSearchResponse(SearchResponse response, boolean initialLoad) {
		mInstance.mSearchResponse = response;

		if (response == null) {
			mInstance.mSearchStatus = getString(R.string.progress_search_failed);
			mEventManager.notifyEventHandlers(EVENT_SEARCH_ERROR, null);
			TrackingUtils.trackErrorPage(this, "HotelListRequestFailed");
		}
		else {
			if (response.getSession() != null) {
				mInstance.mSession = response.getSession();
			}

			if (response.hasErrors()) {
				mInstance.mSearchStatus = response.getErrors().get(0).getPresentableMessage(mContext);
				mEventManager.notifyEventHandlers(EVENT_SEARCH_ERROR, null);
				TrackingUtils.trackErrorPage(this, "HotelListRequestFailed");
			}
			else {
				response.setFilter(mInstance.mFilter);

				if (mInstance.mSearchResponse.getFilteredAndSortedProperties().length <= 10) {
					Log.i("Initial search results had not many results, expanding search radius filter to show all.");
					mInstance.mFilter.setSearchRadius(SearchRadius.ALL);
					mInstance.mSearchResponse.clearCache();
				}

				mEventManager.notifyEventHandlers(EVENT_SEARCH_COMPLETE, response);

				if (initialLoad) {
					onSearchResultsChanged();
				}
			}
		}

		// Update action bar views based on results
		invalidateOptionsMenu();
	}

	private void simulateSearchErrorResponse(int errorMessageResId) {
		SearchResponse response = new SearchResponse();
		ServerError error = new ServerError();
		error.setPresentationMessage(getString(errorMessageResId));
		error.setCode("SIMULATED");
		response.addError(error);
		mSearchCallback.onDownload(response);
	}

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
	// Hotel Details

	private void startRoomsAndRatesDownload(Property property) {
		// If we have rates cached, don't bother downloading
		if (getRoomsAndRatesAvailability() != null) {
			return;
		}

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
			AvailabilityResponse availabilityResponse = (AvailabilityResponse) results;
			mInstance.mAvailabilityResponses.put(mInstance.mProperty.getPropertyId(), availabilityResponse);

			if (availabilityResponse == null) {
				mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_ERROR,
						getString(R.string.error_no_response_room_rates));
				TrackingUtils.trackErrorPage(mContext, "RatesListRequestFailed");
			}
			else {
				if (availabilityResponse.getSession() != null) {
					mInstance.mSession = availabilityResponse.getSession();
				}

				if (availabilityResponse.hasErrors()) {
					mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_ERROR, availabilityResponse.getErrors()
							.get(0).getPresentableMessage(mContext));
					TrackingUtils.trackErrorPage(mContext, "RatesListRequestFailed");
				}
				else {
					mEventManager.notifyEventHandlers(EVENT_AVAILABILITY_SEARCH_COMPLETE, availabilityResponse);
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Hotel Reviews

	private void startReviewsDownload() {
		// Don't download the reviews if we already have them
		if (getReviewsForProperty() != null) {
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_REVIEWS);
		bd.startDownload(KEY_REVIEWS, mReviewsDownload, mReviewsCallback);
		mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_STARTED, null);
	}

	private static final int MAX_SUMMARIZED_REVIEWS = 4;
	private Download mReviewsDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mInstance.mSession);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REVIEWS, services);
			return services.reviews(mInstance.mProperty, 1, ReviewSort.HIGHEST_RATING_FIRST, MAX_SUMMARIZED_REVIEWS);
		}
	};

	private OnDownloadComplete mReviewsCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			ReviewsResponse reviewResponse = (ReviewsResponse) results;
			mInstance.mReviewsResponses.put(mInstance.mProperty.getPropertyId(), reviewResponse);

			if (results == null) {
				mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_ERROR, null);
				TrackingUtils.trackErrorPage(mContext, "UserReviewLoadFailed");
			}
			else if (reviewResponse.hasErrors()) {
				mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_ERROR, reviewResponse.getErrors().get(0)
						.getPresentableMessage(mContext));
				TrackingUtils.trackErrorPage(mContext, "UserReviewLoadFailed");
			}
			else {
				mEventManager.notifyEventHandlers(EVENT_REVIEWS_QUERY_COMPLETE, reviewResponse);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener implementation

	@Override
	public void onFilterChanged() {
		mEventManager.notifyEventHandlers(EVENT_FILTER_CHANGED, null);

		onSearchResultsChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// Tracking

	public void onSearchResultsChanged() {
		String refinements = TrackingUtils.getRefinements(mInstance.mSearchParams, mInstance.mLastSearchParams,
				mInstance.mFilter, mInstance.mLastFilter);

		// Update the last filter/search params we used to track refinements 
		mInstance.mLastSearchParams = mInstance.mSearchParams.copy();
		mInstance.mLastFilter = mInstance.mFilter.copy();

		Tracker.trackAppHotelsSearch(this, mInstance.mSearchParams, mInstance.mSearchResponse, refinements);
	}

	//////////////////////////////////////////////////////////////////////////
	// Forward motion 

	public void startHotelGalleryActivity(Media media) {
		Intent intent = new Intent(this, HotelGalleryActivity.class);
		intent.putExtra(Codes.PROPERTY, mInstance.mProperty.toString());
		intent.putExtra(Codes.SELECTED_IMAGE, media.toString());
		startActivity(intent);
	}

	// (opening rates activity)

	public void bookRoom(Rate rate, boolean specificRateClicked) {
		Intent intent = new Intent(this, BookingFragmentActivity.class);
		intent.putExtra(Codes.SESSION, mInstance.mSession.toJson().toString());
		intent.putExtra(Codes.SEARCH_PARAMS, mInstance.mSearchParams.toJson().toString());
		intent.putExtra(Codes.PROPERTY, mInstance.mProperty.toJson().toString());
		intent.putExtra(Codes.AVAILABILITY_RESPONSE, getRoomsAndRatesAvailability().toJson().toString());
		intent.putExtra(Codes.RATE, rate.toJson().toString());

		if (specificRateClicked) {
			intent.putExtra(BookingFragmentActivity.EXTRA_SPECIFIC_RATE, true);
		}

		startActivity(intent);
	}

	public OnRateClickListener mOnRateClickListener = new OnRateClickListener() {
		public void onRateClick(Rate rate) {
			bookRoom(rate, true);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
