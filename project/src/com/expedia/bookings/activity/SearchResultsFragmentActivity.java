package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentMapActivity;
import android.support.v4.app.FragmentTransaction;
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
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Filter.SearchRadius;
import com.expedia.bookings.data.Filter.Sort;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.CalendarDialogFragment.CalendarDialogFragmentListener;
import com.expedia.bookings.fragment.FilterDialogFragment;
import com.expedia.bookings.fragment.GeocodeDisambiguationDialogFragment;
import com.expedia.bookings.fragment.GeocodeDisambiguationDialogFragment.GeocodeDisambiguationDialogFragmentListener;
import com.expedia.bookings.fragment.GuestsDialogFragment;
import com.expedia.bookings.fragment.GuestsDialogFragment.GuestsDialogFragmentListener;
import com.expedia.bookings.fragment.HotelDetailsFragment;
import com.expedia.bookings.fragment.HotelDetailsFragment.HotelDetailsFragmentListener;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelListFragment.HotelListFragmentListener;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.fragment.MiniDetailsFragment;
import com.expedia.bookings.fragment.MiniDetailsFragment.MiniDetailsFragmentListener;
import com.expedia.bookings.fragment.SortDialogFragment;
import com.expedia.bookings.fragment.SortDialogFragment.SortDialogFragmentListener;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;
import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.omniture.AppMeasurement;

public class SearchResultsFragmentActivity extends FragmentMapActivity implements LocationListener,
		OnFilterChangedListener, SortDialogFragmentListener, CalendarDialogFragmentListener,
		GeocodeDisambiguationDialogFragmentListener, GuestsDialogFragmentListener, HotelDetailsFragmentListener,
		OnCollageImageClickedListener, MiniDetailsFragmentListener, HotelMapFragmentListener, HotelListFragmentListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_AVAILABILITY_SEARCH = "KEY_AVAILABILITY_SEARCH";
	private static final String KEY_GEOCODE = "KEY_GEOCODE";
	private static final String KEY_REVIEWS = "KEY_REVIEWS";

	private static final String INSTANCE_SHOW_DISTANCES = "INSTANCE_SHOW_DISTANCES";
	private static final String INSTANCE_LAST_SEARCH_TIME = "INSTANCE_LAST_SEARCH_TIME";
	private static final String INSTANCE_LAST_SEARCH_PARAMS = "INSTANCE_LAST_SEARCH_PARAMS";
	private static final String INSTANCE_LAST_FILTER = "INSTANCE_LAST_FILTER";
	private static final String INSTANCE_PARTIAL_SEARCH = "INSTANCE_PARTIAL_SEARCH";

	private static final int REQUEST_CODE_SETTINGS = 1;

	private static final long SEARCH_EXPIRATION = 1000 * 60 * 60; // 1 hour

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private Context mContext;
	private Resources mResources;

	private boolean mShowDistances;

	// So we can detect if these search results are stale
	private long mLastSearchTime;

	// Used for tracking purposes only
	private String mLastSearchParamsJson;
	private String mLastFilterJson;

	// If rotating in the middle of an action bar search
	private String mPartialSearch;

	private HotelListFragment mHotelListFragment;
	private HotelMapFragment mHotelMapFragment;
	private MiniDetailsFragment mMiniDetailsFragment;
	private HotelDetailsFragment mHotelDetailsFragment;
	private FilterDialogFragment mFilterDialogFragment;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mContext = this;
		mResources = getResources();

		// If this is the first launch, clear the db
		if (icicle == null) {
			Db.setSearchResponse(null);
			Db.resetFilter();
			Db.clearAvailabilityResponses();
			Db.clearReviewsResponses();
		}
		else {
			mShowDistances = icicle.getBoolean(INSTANCE_SHOW_DISTANCES);
			mLastSearchTime = icicle.getLong(INSTANCE_LAST_SEARCH_TIME, -1);
			mLastSearchParamsJson = icicle.getString(INSTANCE_LAST_SEARCH_PARAMS, null);
			mLastFilterJson = icicle.getString(INSTANCE_LAST_FILTER, null);
			mPartialSearch = icicle.getString(INSTANCE_PARTIAL_SEARCH, null);
		}

		setContentView(R.layout.activity_search_results_fragment);

		mHotelMapFragment = Ui.findFragment(this, getString(R.string.tag_hotel_map));
		mHotelListFragment = Ui.findFragment(this, getString(R.string.tag_hotel_list));
		mMiniDetailsFragment = Ui.findFragment(this, getString(R.string.tag_mini_details));
		mHotelDetailsFragment = Ui.findFragment(this, getString(R.string.tag_details));
		mFilterDialogFragment = Ui.findFragment(this, getString(R.string.tag_filter_dialog));

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		// Load initial data, if it already exists (aka, screen rotated)
		if (Db.getSearchResponse() != null) {
			loadSearchResponse(Db.getSearchResponse(), false);
		}
	}

	@Override
	public void onNewIntent(Intent newIntent) {

		if (Intent.ACTION_SEARCH.equals(newIntent.getAction())) {
			String query = newIntent.getStringExtra(SearchManager.QUERY);
			String searchJson = newIntent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
			if (query.equals(getString(R.string.current_location))) {
				setMyLocationSearch();
			}
			else if (searchJson != null) {
				try {
					Search search = new Search();
					search.fromJson(new JSONObject(searchJson));
					Db.getSearchParams().fillFromSearch(search);
				}
				catch (JSONException e) {
					Log.w("Can't parse search JSON. Setting freeform location instead");
					setFreeformLocation(query);
				}
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
				if (newText == null || newText.equals(getString(R.string.current_location))
						|| Db.getSearchParams() == null || newText.equals(Db.getSearchParams().getFreeformLocation())) {
					mPartialSearch = null;
				}
				else {
					mPartialSearch = newText;
				}
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
					mSearchView.setQuery(Db.getSearchParams().getSearchDisplayText(mContext), false);
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

		Db.getFilter().addOnFilterChangedListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		invalidateOptionsMenu();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_GEOCODE)) {
			bd.registerDownloadCallback(KEY_SEARCH, mGeocodeCallback);
		}
		else if (bd.isDownloading(KEY_SEARCH)) {
			bd.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
		}
		else if (Db.getSearchResponse() != null) {
			if (bd.isDownloading(KEY_AVAILABILITY_SEARCH)) {
				bd.registerDownloadCallback(KEY_AVAILABILITY_SEARCH, mRoomAvailabilityCallback);
			}
			if (bd.isDownloading(KEY_REVIEWS)) {
				bd.registerDownloadCallback(KEY_REVIEWS, mReviewsCallback);
			}
			if (mLastSearchTime != -1 && mLastSearchTime + SEARCH_EXPIRATION < Calendar.getInstance().getTimeInMillis()) {
				Log.d("onResume(): There are cached search results, but they expired.  Starting a new search instead.");
				Db.getSearchParams().ensureValidCheckInDate();
				startSearch();
			}
		}
		else {
			startSearch();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_SHOW_DISTANCES, mShowDistances);
		outState.putLong(INSTANCE_LAST_SEARCH_TIME, mLastSearchTime);
		outState.putString(INSTANCE_LAST_SEARCH_PARAMS, mLastSearchParamsJson);
		outState.putString(INSTANCE_LAST_FILTER, mLastFilterJson);
		outState.putString(INSTANCE_PARTIAL_SEARCH, mPartialSearch);
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

		Db.getFilter().removeOnFilterChangedListener(this);
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

			Db.setSearchResponse(null);
			Db.resetFilter();
			Db.clearAvailabilityResponses();
			Db.clearReviewsResponses();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Activity overrides

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Indicates that settings were changed - send out a broadcast
		if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
			Log.i("Detected currency settings change.");
			startSearch();
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

		DebugMenu.onCreateOptionsMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// This can be run before we've actually initialized the options menu - in that case, don't run
		// the preparation (it will get run on its own course later).
		if (mSearchView == null) {
			return super.onPrepareOptionsMenu(menu);
		}

		SearchParams params = Db.getSearchParams();

		if (mPartialSearch != null) {
			mSearchView.setQuery(mPartialSearch, false);
		}
		else if (!mSearchViewFocused) {
			mSearchView.setQuery(params.getSearchDisplayText(this), false);
		}

		int numGuests = params.getNumAdults() + params.getNumChildren();
		if (mUseCondensedActionBar) {
			mGuestsTextView.setText(numGuests + "");
		}
		else {
			mGuestsMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests));
		}

		int numNights = params.getStayDuration();
		mDatesMenuItem.setTitle(mResources.getQuantityString(R.plurals.number_of_nights, numNights, numNights));

		SearchResponse searchResponse = Db.getSearchResponse();
		mFilterMenuItem.setEnabled(searchResponse != null && !searchResponse.hasErrors());

		DebugMenu.onPrepareOptionsMenu(this, menu);

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
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// Event handling

	public static final int SOURCE_LIST = 1;
	public static final int SOURCE_MAP = 2;
	public static final int SOURCE_MINI_DETAILS = 3;

	public void propertySelected(Property property, int source) {
		Log.v("propertySelected(): " + property.getName());

		Property selectedProperty = Db.getSelectedProperty();
		boolean selectionChanged = (selectedProperty != property);

		// Ensure that the proper view is being displayed.
		//
		// If the user isn't viewing anything, bring up mini details.  If some details are up, either expand to
		// full details or just changed the selected property (based on the state when this property is selected).
		FragmentManager fm = getSupportFragmentManager();
		boolean miniDetailsShowing = fm.findFragmentByTag(getString(R.string.tag_mini_details)) != null;
		boolean detailsShowing = fm.findFragmentByTag(getString(R.string.tag_details)) != null;
		if (!miniDetailsShowing && !detailsShowing) {
			showMiniDetailsFragment();
		}
		else if (!selectionChanged && miniDetailsShowing) {
			showHotelDetailsFragment();
		}

		// Tells you whether the full hotel details will be showing - whether because it already existed, or
		// because we're just opening it now.
		boolean showingFullDetails = (!selectionChanged && !detailsShowing) || (selectionChanged && detailsShowing);

		// When the selected property changes, a few things need to be done.
		if (selectionChanged) {
			// Clear out the previous property's images from the cache
			if (selectedProperty != null) {
				if (selectedProperty.getMediaCount() > 0) {
					for (Media media : selectedProperty.getMediaList()) {
						media.removeFromImageCache();
					}
				}
			}

			Db.setSelectedProperty(property);

			// start downloading the availability response for this property
			// ahead of time (from when it might actually be needed) so that 
			// the results are instantly displayed in the hotel details view to the user
			startRoomsAndRatesDownload(property);

			// notify the necessary components only after starting the 
			// downloads so that the right downlaod information (such as  is picked up by the components
			// when notified of the change in property
			notifyPropertySelected();
		}

		// Only start the reviews download if the full page is being shown
		if (showingFullDetails) {
			startReviewsDownload();
		}

		// Track what was just pressed
		if (showingFullDetails) {
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
		propertySelected(Db.getSelectedProperty(), source);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment management

	private static final String MINI_DETAILS_PUSH = "mini_details_push";

	public void showMiniDetailsFragment() {
		mMiniDetailsFragment = MiniDetailsFragment.newInstance();

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (AndroidUtils.getSdkVersion() >= 13) {
			ft.setCustomAnimations(R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit, R.animator.fragment_mini_details_slide_enter,
					R.animator.fragment_mini_details_slide_exit);
		}
		ft.add(R.id.fragment_mini_details, mMiniDetailsFragment, getString(R.string.tag_mini_details));
		ft.addToBackStack(MINI_DETAILS_PUSH);
		ft.commit();
	}

	public void showHotelDetailsFragment() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_details)) == null) {
			mHotelDetailsFragment = HotelDetailsFragment.newInstance();

			FragmentTransaction ft = fm.beginTransaction();
			if (AndroidUtils.getSdkVersion() >= 13) {
				ft.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit,
						R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit);
			}
			ft.hide(fm.findFragmentByTag(getString(R.string.tag_hotel_map)));
			ft.remove(fm.findFragmentByTag(getString(R.string.tag_mini_details)));
			ft.add(R.id.fragment_details, mHotelDetailsFragment, getString(R.string.tag_details));
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	public void hideDetails() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(MINI_DETAILS_PUSH, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	//////////////////////////////////////////////////////////////////////////
	// Dialogs

	private void showGuestsDialog() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_guests_dialog)) == null) {
			DialogFragment newFragment = GuestsDialogFragment.newInstance(Db.getSearchParams().getNumAdults(), Db
					.getSearchParams().getChildren());
			newFragment.show(fm, getString(R.string.tag_guests_dialog));
		}
	}

	private void showCalendarDialog() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_calendar_dialog)) == null) {
			DialogFragment newFragment = CalendarDialogFragment.newInstance(Db.getSearchParams().getCheckInDate(),
					Db.getSearchParams().getCheckOutDate());
			newFragment.show(getSupportFragmentManager(), getString(R.string.tag_calendar_dialog));
		}
	}

	private void showGeocodeDisambiguationDialog(List<Address> addresses) {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_geocode_disambiguation_dialog)) == null) {
			DialogFragment newFragment = GeocodeDisambiguationDialogFragment.newInstance(addresses);
			newFragment.show(getSupportFragmentManager(), getString(R.string.tag_geocode_disambiguation_dialog));
		}
	}

	private void showFilterDialog() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_filter_dialog)) == null) {
			mFilterDialogFragment = FilterDialogFragment.newInstance();
			mFilterDialogFragment.show(getSupportFragmentManager(), getString(R.string.tag_filter_dialog));
		}
	}

	public void showSortDialog() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_sort_dialog)) == null) {
			DialogFragment newFragment = SortDialogFragment.newInstance(mShowDistances);
			newFragment.show(getSupportFragmentManager(), getString(R.string.tag_sort_dialog));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchParams management

	public void setMyLocationSearch() {
		Log.d("Setting search to use 'my location'");

		Db.getSearchParams().setSearchType(SearchType.MY_LOCATION);

		invalidateOptionsMenu();
	}

	public void setFreeformLocation(String freeformLocation) {
		Log.d("Setting freeform location: " + freeformLocation);

		Db.getSearchParams().setSearchType(SearchType.FREEFORM);
		Db.getSearchParams().setFreeformLocation(freeformLocation);

		invalidateOptionsMenu();
	}

	public void setGuests(int numAdults, List<Integer> children) {
		Log.d("Setting guests: " + numAdults + " adult(s), " + children.size() + " child(ren)");

		Db.getSearchParams().setNumAdults(numAdults);
		Db.getSearchParams().setChildren(children);

		invalidateOptionsMenu();
	}

	public void setDates(Calendar checkIn, Calendar checkOut) {
		Log.d("Setting dates: " + checkIn.getTimeInMillis() + " to " + checkOut.getTimeInMillis());

		Db.getSearchParams().setCheckInDate(checkIn);
		Db.getSearchParams().setCheckOutDate(checkOut);

		invalidateOptionsMenu();
	}

	public void setLatLng(double latitude, double longitude) {
		Log.d("Setting lat/lng: lat=" + latitude + ", lng=" + longitude);

		Db.getSearchParams().setSearchLatLon(latitude, longitude);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search

	public void startSearch() {
		Log.i("startSearch(): " + Db.getSearchParams().toJson().toString());

		// Remove existing search results (and references to it)
		Db.setSearchResponse(null);
		Db.clearAvailabilityResponses();
		Db.clearReviewsResponses();

		// We no longer have a partial search, we have an actual search
		mPartialSearch = null;

		// Reset the filter
		Filter filter = Db.getFilter();
		filter.reset();
		filter.setOnDataListener(null);

		mHotelListFragment.updateStatus(getString(R.string.loading_hotels), true);

		notifySearchStarted();

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
		switch (Db.getSearchParams().getSearchType()) {
		case FREEFORM:
			if (Db.getSearchParams().hasEnoughToSearch()) {
				setShowDistances(Db.getSearchParams().hasSearchLatLon());
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
		// TODO: Add REGION when enumerated properly
	}

	public void startGeocode() {
		Log.i("startGeocode(): " + Db.getSearchParams().getFreeformLocation());

		Db.getSearchParams().setUserFreeformLocation(Db.getSearchParams().getFreeformLocation());

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_GEOCODE, mGeocodeDownload, mGeocodeCallback);
	}

	private Download mGeocodeDownload = new Download() {
		public Object doDownload() {
			return LocationServices.geocode(mContext, Db.getSearchParams().getFreeformLocation());
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
		Db.getSearchParams().setFreeformLocation(address);
		invalidateOptionsMenu();

		setLatLng(address.getLatitude(), address.getLongitude());

		// #13072: Always show as if it was an exact location search for geocodes
		// Used to use SearchUtils.isExactLocation(address).
		setShowDistances(true);

		startSearchDownloader();
	}

	public void onGeocodeFailure() {
		simulateSearchErrorResponse(R.string.geolocation_failed);

		TrackingUtils.trackErrorPage(this, "App.Error.LocationNotFound");
	}

	public void onMyLocationFound(Location location) {
		setLatLng(location.getLatitude(), location.getLongitude());
		setShowDistances(true);
		startSearchDownloader();
	}

	public void startSearchDownloader() {
		Log.i("startSearchDownloader()");

		// This method essentially signifies that we've found the location to search;
		// take this opportunity to notify handlers that we know where we're looking.
		notifySearchLocationFound();

		// Save this as a "recent search" if it is a freeform search
		if (Db.getSearchParams().getSearchType() == SearchType.FREEFORM) {
			Search.add(this, Db.getSearchParams());
		}

		BackgroundDownloader.getInstance().startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private Download mSearchDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(Db.getSearchParams(), 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			loadSearchResponse((SearchResponse) results, true);
		}
	};

	private void loadSearchResponse(SearchResponse response, boolean initialLoad) {
		Db.setSearchResponse(response);

		if (response == null) {
			mHotelListFragment.updateStatus(getString(R.string.progress_search_failed), false);
			TrackingUtils.trackErrorPage(this, "HotelListRequestFailed");
		}
		else {
			if (response.hasErrors()) {
				mHotelListFragment.updateStatus(response.getErrors().get(0).getPresentableMessage(mContext), false);
				TrackingUtils.trackErrorPage(this, "HotelListRequestFailed");
			}
			else {
				response.setFilter(Db.getFilter());

				Property[] properties = response.getFilteredAndSortedProperties();
				if (properties != null && properties.length <= 10) {
					Log.i("Initial search results had not many results, expanding search radius filter to show all.");
					Db.getFilter().setSearchRadius(SearchRadius.ALL);
					response.clearCache();
				}

				notifySearchComplete();
				mLastSearchTime = Calendar.getInstance().getTimeInMillis();

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
		mHotelListFragment.updateStatus(getString(R.string.progress_finding_location), true);

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
		// If we have the proper rates cached, don't bother downloading; otherwise, 
		AvailabilityResponse previousResponse = Db.getSelectedAvailabilityResponse();
		if (previousResponse != null && !previousResponse.canRequestMoreData()) {
			return;
		}

		final boolean requestMoreData = previousResponse != null && previousResponse.canRequestMoreData();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(KEY_AVAILABILITY_SEARCH);

		Download roomAvailabilityDownload = new Download() {
			public Object doDownload() {
				ExpediaServices services = new ExpediaServices(mContext);
				BackgroundDownloader.getInstance().addDownloadListener(KEY_AVAILABILITY_SEARCH, services);
				return services.availability(Db.getSearchParams(), Db.getSelectedProperty(),
						requestMoreData ? ExpediaServices.F_EXPENSIVE : 0);
			}
		};

		bd.startDownload(KEY_AVAILABILITY_SEARCH, roomAvailabilityDownload, mRoomAvailabilityCallback);

		notifyAvailabilityQueryStarted();
	}

	private OnDownloadComplete mRoomAvailabilityCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			AvailabilityResponse availabilityResponse = (AvailabilityResponse) results;
			Db.addAvailabilityResponse(availabilityResponse);

			if (availabilityResponse == null) {
				notifyAvailabilityQueryError(getString(R.string.error_no_response_room_rates));
				TrackingUtils.trackErrorPage(mContext, "RatesListRequestFailed");
			}
			else {
				if (availabilityResponse.hasErrors()) {
					notifyAvailabilityQueryError(availabilityResponse.getErrors().get(0)
							.getPresentableMessage(mContext));
					TrackingUtils.trackErrorPage(mContext, "RatesListRequestFailed");
				}
				else {
					Db.getSelectedProperty().updateFrom(availabilityResponse.getProperty());

					notifyAvailabilityQueryComplete();

					// Immediately kick off another (more expensive) request to get more data (if possible)
					if (availabilityResponse.canRequestMoreData()) {
						startRoomsAndRatesDownload(Db.getSelectedProperty());
					}
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Hotel Reviews

	private void startReviewsDownload() {
		// Don't download the reviews if we already have them
		if (Db.getSelectedReviewsResponse() != null) {
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_REVIEWS);
		bd.startDownload(KEY_REVIEWS, mReviewsDownload, mReviewsCallback);

		notifyReviewsQueryStarted();
	}

	private static final int MAX_SUMMARIZED_REVIEWS = 4;
	private Download mReviewsDownload = new Download() {

		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REVIEWS, services);

			LinkedList<String> languages = LocaleUtils.getLanguages(mContext);

			return services.reviews(Db.getSelectedProperty(), ReviewSort.HIGHEST_RATING_FIRST, 0, languages,
					MAX_SUMMARIZED_REVIEWS);
		}
	};

	private OnDownloadComplete mReviewsCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			ReviewsResponse reviewResponse = (ReviewsResponse) results;
			Db.addReviewsResponse(reviewResponse);

			if (results == null || reviewResponse.hasErrors()) {
				notifyReviewsQueryError(null);
				TrackingUtils.trackErrorPage(mContext, "UserReviewLoadFailed");
			}
			else {
				notifyReviewsQueryComplete();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener implementation

	@Override
	public void onFilterChanged() {
		mHotelListFragment.notifyFilterChanged();
		mHotelMapFragment.notifyFilterChanged();

		if (mFilterDialogFragment != null) {
			mFilterDialogFragment.notifyFilterChanged();
		}

		onSearchResultsChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment communication

	private void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;

		mHotelListFragment.setShowDistances(showDistances);
		mHotelMapFragment.setShowDistances(showDistances);
	}

	private void notifySearchStarted() {
		mHotelListFragment.notifySearchStarted();
		mHotelMapFragment.notifySearchStarted();
	}

	private void notifySearchLocationFound() {
		mHotelMapFragment.notifySearchLocationFound();
	}

	private void notifySearchComplete() {
		mHotelListFragment.notifySearchComplete();
		mHotelMapFragment.notifySearchComplete();
	}

	private void notifyPropertySelected() {
		mHotelListFragment.notifyPropertySelected();
		mHotelMapFragment.notifyPropertySelected();

		if (mMiniDetailsFragment != null && mMiniDetailsFragment.isAdded()) {
			mMiniDetailsFragment.notifyPropertySelected();
		}

		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyPropertySelected();
		}
	}

	private void notifyAvailabilityQueryStarted() {
		if (mMiniDetailsFragment != null && mMiniDetailsFragment.isAdded()) {
			mMiniDetailsFragment.notifyAvailabilityQueryStarted();
		}

		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyAvailabilityQueryStarted();
		}
	}

	private void notifyAvailabilityQueryComplete() {
		if (mMiniDetailsFragment != null && mMiniDetailsFragment.isAdded()) {
			mMiniDetailsFragment.notifyAvailabilityQueryComplete();
		}

		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyAvailabilityQueryComplete();
		}
	}

	private void notifyAvailabilityQueryError(String errMsg) {
		if (mMiniDetailsFragment != null && mMiniDetailsFragment.isAdded()) {
			mMiniDetailsFragment.notifyAvailabilityQueryError(errMsg);
		}

		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyAvailabilityQueryError(errMsg);
		}
	}

	private void notifyReviewsQueryStarted() {
		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyReviewsQueryStarted();
		}
	}

	private void notifyReviewsQueryComplete() {
		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyReviewsQueryComplete();
		}
	}

	private void notifyReviewsQueryError(String message) {
		if (mHotelDetailsFragment != null && mHotelDetailsFragment.isAdded()) {
			mHotelDetailsFragment.notifyReviewsQueryError();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Tracking

	public void onSearchResultsChanged() {
		SearchParams lastSearchParams = null;
		Filter lastFilter = null;

		try {
			if (mLastSearchParamsJson != null) {
				lastSearchParams = new SearchParams(new JSONObject(mLastSearchParamsJson));
			}

			if (mLastFilterJson != null) {
				lastFilter = new Filter(new JSONObject(mLastFilterJson));
			}
		}
		catch (JSONException e) {
			Log.w("Could not restore last search params/filter for tracking", e);
		}

		Filter filter = Db.getFilter();
		String refinements = TrackingUtils.getRefinements(Db.getSearchParams(), lastSearchParams, filter, lastFilter);

		// Update the last filter/search params we used to track refinements
		mLastSearchParamsJson = Db.getSearchParams().toJson().toString();
		mLastFilterJson = filter.toJson().toString();

		Tracker.trackAppHotelsSearch(this, Db.getSearchParams(), Db.getSearchResponse(), refinements);
	}

	//////////////////////////////////////////////////////////////////////////
	// Forward motion 

	public void startHotelGalleryActivity(Media media) {
		Intent intent = new Intent(this, HotelGalleryActivity.class);
		intent.putExtra(Codes.PROPERTY, Db.getSelectedProperty().toString());
		intent.putExtra(Codes.SELECTED_IMAGE, media.toString());
		startActivity(intent);
	}

	// (opening rates activity)

	public void bookRoom(Rate rate, boolean specificRateClicked) {
		Intent intent = new Intent(this, BookingFragmentActivity.class);
		Db.setSelectedRate(rate);

		if (specificRateClicked) {
			intent.putExtra(BookingFragmentActivity.EXTRA_SPECIFIC_RATE, true);
		}

		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// SortDialogFragmentListener

	@Override
	public void onSortChanged(Sort newSort) {
		Filter filter = Db.getFilter();
		filter.setSort(newSort);
		filter.notifyFilterChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// CalendarDialogFragmentListener

	@Override
	public void onChangeDates(Calendar start, Calendar end) {
		setDates(start, end);
		startSearch();
	}

	//////////////////////////////////////////////////////////////////////////
	// GeocodeDisambiguationDialogFragmentListener

	@Override
	public void onLocationPicked(Address address) {
		onGeocodeSuccess(address);
	}

	@Override
	public void onGeocodeDisambiguationFailure() {
		onGeocodeFailure();
	}

	//////////////////////////////////////////////////////////////////////////
	// GuestsDialogFragmentListener

	@Override
	public void onGuestsChanged(int numAdults, ArrayList<Integer> numChildren) {
		setGuests(numAdults, numChildren);
		startSearch();
		GuestsPickerUtils.updateDefaultChildAges(this, numChildren);
	}

	//////////////////////////////////////////////////////////////////////////
	// MiniDetailsFragmentListener

	@Override
	public void onMiniDetailsRateClicked(Rate rate) {
		bookRoom(rate, true);
	}

	@Override
	public void onMoreDetailsClicked() {
		moreDetailsForPropertySelected(SOURCE_MINI_DETAILS);
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelDetailsFragmentListener

	@Override
	public void onDetailsRateClicked(Rate rate) {
		bookRoom(rate, true);
	}

	@Override
	public void onBookNowClicked() {
		SummarizedRoomRates summarizedRoomRates = Db.getSelectedSummarizedRoomRates();
		bookRoom(summarizedRoomRates.getStartingRate(), false);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnCollageImageClickedListener

	@Override
	public void onImageClicked(Media media) {
		if (Db.getSelectedProperty().getMediaCount() > 0) {
			startHotelGalleryActivity(media);
		}
	}

	@Override
	public void onPromotionClicked() {
		SummarizedRoomRates summarizedRoomRates = Db.getSelectedSummarizedRoomRates();

		if (summarizedRoomRates != null) {
			bookRoom(summarizedRoomRates.getStartingRate(), false);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelMapFragmentListener

	@Override
	public void onBalloonShown(Property property) {
		propertySelected(property, SOURCE_MAP);
	}

	@Override
	public void onBalloonClicked() {
		moreDetailsForPropertySelected(SOURCE_MAP);
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelListFragmentListener

	@Override
	public void onSortButtonClicked() {
		showSortDialog();
	}

	@Override
	public void onListItemClicked(Property property) {
		propertySelected(property, SOURCE_LIST);
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
