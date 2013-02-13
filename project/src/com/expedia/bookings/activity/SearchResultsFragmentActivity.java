package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Date;
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
import com.expedia.bookings.data.pos.PointOfSale;
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
import com.expedia.bookings.fragment.MiniDetailsFragment;
import com.expedia.bookings.fragment.MiniDetailsFragment.MiniDetailsFragmentListener;
import com.expedia.bookings.fragment.SortDialogFragment;
import com.expedia.bookings.fragment.SortDialogFragment.SortDialogFragmentListener;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.maps.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.SearchUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;
import com.expedia.bookings.widget.SummarizedRoomRates;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;

// This is the TABLET search results activity

// Target(HONEYCOMB) because the ABS SearchView is not ready for prime time
// (as of 2013.02.13). The dropdown suggestion code is all TODOed in ABS:SearchView.java.
// So for now, we'll use the Honeycomb SearchView in here.

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SearchResultsFragmentActivity extends SherlockFragmentActivity implements LocationListener,
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

	private ArrayList<String> mDownloadKeys = new ArrayList<String>();

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

	// For doing manual updates
	private HockeyPuck mHockeyPuck;

	private ActivityKillReceiver mKillReciever;

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
			mLastSearchParamsJson = icicle.getString(INSTANCE_LAST_SEARCH_PARAMS);
			mLastFilterJson = icicle.getString(INSTANCE_LAST_FILTER);
			mPartialSearch = icicle.getString(INSTANCE_PARTIAL_SEARCH);
		}

		setContentView(R.layout.activity_search_results_fragment);

		mHotelMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_hotel_map));
		mHotelListFragment = Ui.findSupportFragment(this, getString(R.string.tag_hotel_list));
		mMiniDetailsFragment = Ui.findSupportFragment(this, getString(R.string.tag_mini_details));
		mHotelDetailsFragment = Ui.findSupportFragment(this, getString(R.string.tag_details));
		mFilterDialogFragment = Ui.findSupportFragment(this, getString(R.string.tag_filter_dialog));

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		// Load initial data, if it already exists (aka, screen rotated)
		if (Db.getSearchResponse() != null) {
			loadSearchResponse(Db.getSearchResponse(), false);
		}

		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));

		mKillReciever = new ActivityKillReceiver(this);
		mKillReciever.onCreate();
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
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);

		mSearchView = new CustomSearchView(this);
		actionBar.setCustomView(mSearchView);
		actionBar.setDisplayShowCustomEnabled(true);

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
						|| Db.getSearchParams() == null || newText.equals(Db.getSearchParams().getQuery())) {
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
					else {
						mSearchView.getQueryTextView().setSelection(currQuery.length());
						//TODO: mSearchView.getQueryTextView().showDropDown();
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
	}

	@Override
	protected void onResume() {
		super.onResume();

		Db.getFilter().addOnFilterChangedListener(this);

		invalidateOptionsMenu();

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		updateMapOffsets(mMiniDetailsFragment != null);

		// #13546 - Need to put any methods that may affect Fragment state in onPostResume() instead of
		// onResume() for the compatibility library (otherwise we get state loss errors).
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_GEOCODE)) {
			bd.registerDownloadCallback(KEY_GEOCODE, mGeocodeCallback);
		}
		else if (bd.isDownloading(KEY_SEARCH)) {
			bd.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
		}
		else if (Db.getSearchResponse() != null) {
			if (Db.getSelectedProperty() != null) {
				String key = getDownloadKey(Db.getSelectedProperty());
				if (bd.isDownloading(key)) {
					bd.registerDownloadCallback(key, mRoomAvailabilityCallback);
				}
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

		Db.getFilter().removeOnFilterChangedListener(this);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_GEOCODE);
		bd.unregisterDownloadCallback(KEY_SEARCH);
		for (String key : mDownloadKeys) {
			// unregister KEY_AVAILABILITY_SEARCH related downloads
			bd.unregisterDownloadCallback(key);
		}
		bd.unregisterDownloadCallback(KEY_REVIEWS);

		OmnitureTracking.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			clearSearch();
		}

		if (mKillReciever != null) {
			mKillReciever.onDestroy();
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

			// Clear out the search results data - this will automatically start a search
			// when we get to onResume().
			clearSearch();
		}
		else if (requestCode == REQUEST_CODE_SETTINGS
				&& resultCode == ExpediaBookingPreferenceActivity.RESULT_POS_CHANGED) {
			Db.getFilter().addOnFilterChangedListener(this);
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

	private CustomSearchView mSearchView;
	private MenuItem mGuestsMenuItem;
	private MenuItem mDatesMenuItem;
	private MenuItem mFilterMenuItem;

	private boolean mSearchViewFocused = false;
	private boolean mUseCondensedActionBar = false;

	private View mGuestsActionView;
	private TextView mGuestsTextView;

	private boolean mCreatedOptionsMenu = false;

	@Override
	public void invalidateOptionsMenu() {
		// #13554: If we haven't even created the options menu yet, don't other invalidating!
		if (mCreatedOptionsMenu) {
			super.invalidateOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mCreatedOptionsMenu = true;

		getSupportMenuInflater().inflate(R.menu.menu_fragment_search, menu);
		getSupportMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		mGuestsMenuItem = menu.findItem(R.id.menu_guests);
		mDatesMenuItem = menu.findItem(R.id.menu_dates);
		mFilterMenuItem = menu.findItem(R.id.menu_filter);

		// Use a condensed ActionBar if the screen width is not large enough
		mUseCondensedActionBar = LayoutUtils.isScreenNarrow(this);

		if (mUseCondensedActionBar) {
			mFilterMenuItem.setTitle(R.string.filter);

			// Configure the custom action view (which is more condensed than the normal one)
			mGuestsActionView = getLayoutInflater().inflate(R.layout.action_menu_item_guests, null);
			View button = mGuestsActionView.findViewById(R.id.guests_button);
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					showGuestsDialog();
				}
			});
			mGuestsTextView = (TextView) mGuestsActionView.findViewById(R.id.guests_text_view);
		}

		DebugMenu.onCreateOptionsMenu(this, menu);

		mHockeyPuck.onCreateOptionsMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// This can be run before we've actually initialized the options menu - in that case, don't run
		// the preparation (it will get run on its own course later).
		if (mSearchView == null) {
			return super.onPrepareOptionsMenu(menu);
		}

		if (mGuestsActionView != null) {
			mGuestsMenuItem.setActionView(mGuestsActionView);
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

		mHockeyPuck.onPrepareOptionsMenu(menu);

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

		if (DebugMenu.onOptionsItemSelected(this, item) || mHockeyPuck.onOptionsItemSelected(item)) {
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
			OmnitureTracking.trackPageLoadHotelDetails(mContext, property);
		}
		else if (selectionChanged && !detailsShowing) {
			String referrer = null;
			if (source == SOURCE_LIST) {
				referrer = "App.Hotels.Search.QuickView.List";
			}
			else if (source == SOURCE_MAP) {
				referrer = "App.Hotels.Search.QuickView.Map";
			}

			OmnitureTracking.trackPageLoadHotelsSearchQuickView(mContext, property, referrer);
		}
	}

	public void moreDetailsForPropertySelected(int source) {
		propertySelected(Db.getSelectedProperty(), source);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment management

	private static final String MINI_DETAILS_PUSH = "mini_details_push";

	public void showMiniDetailsFragment() {
		if (mMiniDetailsFragment == null) {
			mMiniDetailsFragment = MiniDetailsFragment.newInstance();
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (AndroidUtils.getSdkVersion() >= 13) {
			ft.setCustomAnimations(R.anim.fragment_mini_details_slide_enter, R.anim.fragment_mini_details_slide_exit,
					R.anim.fragment_mini_details_slide_enter, R.anim.fragment_mini_details_slide_exit);
		}
		ft.add(R.id.fragment_mini_details, mMiniDetailsFragment, getString(R.string.tag_mini_details));
		ft.addToBackStack(MINI_DETAILS_PUSH);
		ft.commit();

		updateMapOffsets(true);

		mHotelMapFragment
				.setCenterOffsetY((float) (getResources().getDimensionPixelSize(R.dimen.mini_details_height) / 2.0f));
	}

	public void showHotelDetailsFragment() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_details)) == null) {
			mHotelDetailsFragment = HotelDetailsFragment.newInstance();

			FragmentTransaction ft = fm.beginTransaction();
			if (AndroidUtils.getSdkVersion() >= 13) {
				ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit,
						R.anim.fragment_slide_right_enter, R.anim.fragment_slide_right_exit);
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

		// Refocus the map pins
		updateMapOffsets(false);
		mHotelMapFragment.showAll();
	}

	private void updateMapOffsets(boolean needsOffset) {
		if (needsOffset) {
			mHotelMapFragment.setCenterOffsetY((float) (getResources().getDimensionPixelSize(
					R.dimen.mini_details_height) / 2.0f));
		}
		else {
			mHotelMapFragment.setCenterOffsetY(0);
		}
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
			DialogFragment newFragment = CalendarDialogFragment.newInstance(Db.getSearchParams().getCheckInDate(), Db
					.getSearchParams().getCheckOutDate());
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
		if (Db.getSearchResponse() != null) {
			FragmentManager fm = getSupportFragmentManager();
			if (fm.findFragmentByTag(getString(R.string.tag_filter_dialog)) == null) {
				mFilterDialogFragment = FilterDialogFragment.newInstance();
				mFilterDialogFragment.show(getSupportFragmentManager(), getString(R.string.tag_filter_dialog));
			}
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
		Db.getSearchParams().setQuery(freeformLocation);

		invalidateOptionsMenu();
	}

	public void setGuests(int numAdults, List<Integer> children) {
		Log.d("Setting guests: " + numAdults + " adult(s), " + children.size() + " child(ren)");

		Db.getSearchParams().setNumAdults(numAdults);
		Db.getSearchParams().setChildren(children);

		invalidateOptionsMenu();
	}

	public void setDates(Date checkIn, Date checkOut) {
		Log.d("Setting dates: " + checkIn + " to " + checkOut);

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
		SearchParams params = Db.getSearchParams();
		switch (params.getSearchType()) {
		case CITY:
			if (params.hasEnoughToSearch()) {
				Search.add(this, params);
				setShowDistances(false);
				startSearchDownloader();
			}
			else {
				startGeocode();
			}
			break;
		case ADDRESS:
		case POI:
		case FREEFORM:
			if (params.hasEnoughToSearch()) {
				Search.add(this, params);
				setShowDistances(params.hasSearchLatLon());
				startSearchDownloader();
			}
			else {
				startGeocode();
			}
			break;
		case VISIBLE_MAP_AREA:
			// TODO: Implement VISIBLE_MAP_AREA search (once a MapView is available)
			Log.w("VISIBLE_MAP_AREA searches not yet supported!");
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
		Log.i("startGeocode(): " + Db.getSearchParams().getQuery());

		Db.getSearchParams().setUserQuery(Db.getSearchParams().getQuery());

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_GEOCODE, mGeocodeDownload, mGeocodeCallback);
	}

	private final Download<List<Address>> mGeocodeDownload = new Download<List<Address>>() {
		public List<Address> doDownload() {
			return LocationServices.geocode(mContext, Db.getSearchParams().getQuery());
		}
	};

	private final OnDownloadComplete<List<Address>> mGeocodeCallback = new OnDownloadComplete<List<Address>>() {
		public void onDownload(List<Address> addresses) {
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
		String formattedAddress = StrUtils.removeUSAFromAddress(address);

		// Determine if this is a specific place by whether there is an address.
		SearchType searchType = SearchUtils.isExactLocation(address) ? SearchType.ADDRESS : SearchType.CITY;

		Db.getSearchParams().setQuery(formattedAddress);
		Db.getSearchParams().setSearchType(searchType);
		invalidateOptionsMenu();

		setLatLng(address.getLatitude(), address.getLongitude());

		// #13072: Always show as if it was an exact location search for geocodes
		// v1.5 un-does this logic.
		setShowDistances(searchType == SearchType.ADDRESS);

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

		Db.clearSelectedProperty();

		// This method essentially signifies that we've found the location to search;
		// take this opportunity to notify handlers that we know where we're looking.
		notifySearchLocationFound();

		BackgroundDownloader.getInstance().startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private final Download<SearchResponse> mSearchDownload = new Download<SearchResponse>() {
		public SearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(Db.getSearchParams(), 0);
		}
	};

	private final OnDownloadComplete<SearchResponse> mSearchCallback = new OnDownloadComplete<SearchResponse>() {
		public void onDownload(SearchResponse response) {
			loadSearchResponse(response, true);
		}
	};

	private void loadSearchResponse(SearchResponse response, boolean initialLoad) {
		Db.setSearchResponse(response);

		if (response == null) {
			mHotelListFragment.updateStatus(LayoutUtils.noHotelsFoundMessage(mContext), false);
			TrackingUtils.trackErrorPage(this, "HotelListRequestFailed");
		}
		else {
			if (response.hasErrors()) {
				mHotelListFragment.updateStatus(response.getErrors().get(0).getPresentableMessage(mContext), false);
				TrackingUtils.trackErrorPage(this, "HotelListRequestFailed");
			}
			else {
				response.setFilter(Db.getFilter());

				if (initialLoad) {
					onSortChanged(Sort.DISTANCE);
				}

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

	// Use if you want to get back to ground zero.  Typically not if you want
	// to modify search params, but useful when destroying the Activity (or if
	// for example the POS changes).
	public void clearSearch() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_GEOCODE);
		bd.cancelDownload(KEY_SEARCH);
		for (String key : mDownloadKeys) {
			// Cancel KEY_AVAILABILITY_SEARCH related downloads
			bd.cancelDownload(key);
		}
		bd.cancelDownload(KEY_REVIEWS);

		Db.setSearchResponse(null);
		Db.resetFilter();
		Db.clearAvailabilityResponses();
		Db.clearReviewsResponses();
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
		// If we have the proper rates cached, don't bother downloading
		AvailabilityResponse previousResponse = Db.getSelectedAvailabilityResponse();
		if (previousResponse != null && !previousResponse.canRequestMoreData()) {
			return;
		}

		String key = getDownloadKey(property);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(key);
		mDownloadKeys.remove(key);

		bd.startDownload(key, mRoomAvailabilityDownload, mRoomAvailabilityCallback);
		mDownloadKeys.add(key);

		notifyAvailabilityQueryStarted();
	}

	private String getDownloadKey(Property p) {
		return KEY_AVAILABILITY_SEARCH + "_" + p.getPropertyId();
	}

	private final Download<AvailabilityResponse> mRoomAvailabilityDownload = new Download<AvailabilityResponse>() {
		public AvailabilityResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			String key = getDownloadKey(Db.getSelectedProperty());
			BackgroundDownloader.getInstance().addDownloadListener(key, services);

			if (Db.getSelectedInfoResponse() == null) {
				return services.information(Db.getSelectedProperty());
			}
			else {
				return services.availability(Db.getSearchParams(), Db.getSelectedProperty(),
						ExpediaServices.F_EXPENSIVE);
			}
		}
	};

	private final OnDownloadComplete<AvailabilityResponse> mRoomAvailabilityCallback = new OnDownloadComplete<AvailabilityResponse>() {
		public void onDownload(AvailabilityResponse availabilityResponse) {
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
					Db.addAvailabilityResponse(availabilityResponse);

					Db.getProperty(availabilityResponse.getProperty().getPropertyId()).updateFrom(
							availabilityResponse.getProperty());

					notifyAvailabilityQueryComplete();

					// Immediately kick off another (more expensive) request to get more data (if possible)
					if (availabilityResponse.canRequestMoreData()) {
						startRoomsAndRatesDownload(Db.getProperty(availabilityResponse.getProperty().getPropertyId()));
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
	private final Download<ReviewsResponse> mReviewsDownload = new Download<ReviewsResponse>() {
		@Override
		public ReviewsResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REVIEWS, services);

			List<String> languages = Arrays.asList(PointOfSale.getPointOfSale().getReviewLanguages());

			return services.reviews(Db.getSelectedProperty(), ReviewSort.HIGHEST_RATING_FIRST, 0, languages,
					MAX_SUMMARIZED_REVIEWS);
		}
	};

	private final OnDownloadComplete<ReviewsResponse> mReviewsCallback = new OnDownloadComplete<ReviewsResponse>() {
		@Override
		public void onDownload(ReviewsResponse reviewResponse) {
			Db.addReviewsResponse(reviewResponse);

			if (reviewResponse == null || reviewResponse.hasErrors()) {
				notifyReviewsQueryError(null);
				TrackingUtils.trackErrorPage(mContext, "UserReviewLoadFailed");
			}
			else {
				notifyReviewsQueryComplete();
			}
		}
	};

	/**
	 * We use the SearchView in this activity to both display the search
	 * location as well as allow the user to enter a new one. The default
	 * behavior when calling setQuery is for it to setSelection() on the
	 * text that we just set (which moves the cursor to the end). In that
	 * case, the user will usually see the least interesting parts of
	 * his query (like ..."cisco, California, USA"). We'd rather show the
	 * beginning of the query instead (like "San Francisco, Califo...").
	 * But the default SearchView makes this difficult. So we'll just do
	 * some trickery here to make it possible.
	 * Related Redmine ticket: #13769
	 */
	public class CustomSearchView extends SearchView {
		private AutoCompleteTextView mQueryTextView = null;

		public CustomSearchView(Context context) {
			super(context);
		}

		@Override
		public void setQuery(CharSequence query, boolean submit) {
			super.setQuery(query, submit);
			getQueryTextView();
			if (mQueryTextView != null) {
				mQueryTextView.setSelection(0);
			}
		}

		// We have to do some trickery here to find the AutoCompleteTextView,
		// since it's not exposed by the SearchView object. Walk the View
		// hierarchy, starting with this object, until we find the
		// AutoCompleteTextView. We know you're there, don't be coy.
		public AutoCompleteTextView getQueryTextView() {
			if (mQueryTextView != null) {
				return mQueryTextView;
			}

			Queue<View> views = new LinkedList<View>();
			views.add(this);

			while (!views.isEmpty()) {
				View v = views.poll();
				if (v instanceof AutoCompleteTextView) {
					mQueryTextView = (AutoCompleteTextView) v;
					return mQueryTextView;
				}
				else if (v instanceof ViewGroup) {
					for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
						views.add(((ViewGroup) v).getChildAt(i));
					}
				}
			}

			return null;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener implementation

	@Override
	public void onFilterChanged() {
		if (mHotelListFragment != null && mHotelListFragment.isAdded()) {
			mHotelListFragment.notifyFilterChanged();
		}
		if (mHotelMapFragment != null && mHotelMapFragment.isAdded()) {
			mHotelMapFragment.notifyFilterChanged();
		}

		if (mFilterDialogFragment != null && mFilterDialogFragment.isAdded()) {
			mFilterDialogFragment.notifyFilterChanged();
		}

		hideDetails();

		onSearchResultsChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment communication

	private void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;

		if (mHotelListFragment != null && mHotelListFragment.isAdded()) {
			mHotelListFragment.setShowDistances(showDistances);
		}
		if (mHotelMapFragment != null && mHotelMapFragment.isAdded()) {
			mHotelMapFragment.setShowDistances(showDistances);
		}
	}

	private void notifySearchStarted() {
		if (mHotelListFragment != null && mHotelListFragment.isAdded()) {
			mHotelListFragment.notifySearchStarted();
		}
		if (mHotelMapFragment != null && mHotelMapFragment.isAdded()) {
			mHotelMapFragment.notifySearchStarted();
		}
	}

	private void notifySearchLocationFound() {
		if (mHotelMapFragment != null && mHotelMapFragment.isAdded()) {
			mHotelMapFragment.notifySearchLocationFound();
		}

		// TODO: Update autocomplete cursor
		Search.add(this, Db.getSearchParams());
	}

	private void notifySearchComplete() {
		if (mHotelListFragment != null && mHotelListFragment.isAdded()) {
			mHotelListFragment.notifySearchComplete();
		}
		if (mHotelMapFragment != null && mHotelMapFragment.isAdded()) {
			mHotelMapFragment.notifySearchComplete();
		}
	}

	private void notifyPropertySelected() {
		if (mHotelListFragment != null && mHotelListFragment.isAdded()) {
			mHotelListFragment.notifyPropertySelected();
		}
		if (mHotelMapFragment != null && mHotelMapFragment.isAdded()) {
			mHotelMapFragment.notifyPropertySelected();
		}
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

		OmnitureTracking.trackAppHotelsSearch(this, Db.getSearchParams(), Db.getSearchResponse(), refinements);
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
		Intent intent = new Intent(this, RoomsAndRatesFragmentActivity.class);
		Db.setSelectedRate(rate);

		if (specificRateClicked) {
			intent.putExtra(RoomsAndRatesFragmentActivity.EXTRA_SPECIFIC_RATE, true);
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
	public void onChangeDates(Date start, Date end) {
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
	public void onHotelMapFragmentAttached(HotelMapFragment fragment) {
		//ignore
	}

	@Override
	public void onPropertyClicked(Property property) {
		propertySelected(property, SOURCE_MAP);
	}

	@Override
	public void onPropertyBubbleClicked(Property property) {
		propertySelected(property, SOURCE_MAP);
	}

	@Override
	public void onExactLocationClicked() {
		// ignore
	}

	@Override
	public void onMapClicked() {
		hideDetails();
		Db.setSelectedProperty((Property) null);
		if (mHotelListFragment != null && mHotelListFragment.isAdded()) {
			mHotelListFragment.clearSelectedProperty();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelListFragmentListener

	@Override
	public void onHotelListFragmentAttached(HotelListFragment fragment) {
		// ignore
	}

	@Override
	public void onSortButtonClicked() {
		showSortDialog();
	}

	@Override
	public void onListItemClicked(Property property, int position) {
		propertySelected(property, SOURCE_LIST);
	}
}
