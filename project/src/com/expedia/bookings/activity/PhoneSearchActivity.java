package com.expedia.bookings.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp.OnSearchParamsChangedInWidgetListener;
import com.expedia.bookings.content.AutocompleteProvider;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.OnFilterChangedListener;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelFilter.Sort;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.FusedLocationProviderFragment;
import com.expedia.bookings.fragment.FusedLocationProviderFragment.Listener;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelListFragment.HotelListFragmentListener;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.maps.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.SearchUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DisableableViewPager;
import com.expedia.bookings.widget.SearchSuggestionAdapter;
import com.expedia.bookings.widget.SimpleNumberPicker;
import com.expedia.bookings.widget.gl.GLTagProgressBar;
import com.expedia.bookings.widget.gl.GLTagProgressBarRenderer.OnDrawStartedListener;
import com.google.android.gms.maps.model.LatLng;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.ViewUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.SegmentedControlGroup;

public class PhoneSearchActivity extends SherlockFragmentActivity implements OnDrawStartedListener,
		HotelListFragmentListener, HotelMapFragmentListener, OnFilterChangedListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	//////////////////////////////////////////////////////////////////////////////////////////
	// ENUMS
	//////////////////////////////////////////////////////////////////////////////////////////

	private enum DisplayType {
		NONE(false),
		KEYBOARD(true),
		CALENDAR(true),
		GUEST_PICKER(true),
		FILTER(false), ;

		private boolean mIsSearchDisplay;

		private DisplayType(boolean isSearchDisplay) {
			mIsSearchDisplay = isSearchDisplay;
		}

		public boolean isSearchDisplay() {
			return mIsSearchDisplay;
		}
	}

	private enum ActivityState {
		NONE,
		SEARCHING, ;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////////

	private static final String KEY_GEOCODE = "KEY_GEOCODE";
	public static final String KEY_SEARCH = "KEY_SEARCH";
	public static final String KEY_HOTEL_SEARCH = "KEY_HOTEL_SEARCH";
	private static final String KEY_LOADING_PREVIOUS = "KEY_LOADING_PREVIOUS";

	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;
	private static final int DIALOG_CLIENT_DEPRECATED = 1;
	private static final int DIALOG_ENABLE_LOCATIONS = 2;
	private static final int DIALOG_INVALID_SEARCH_RANGE = 3;

	private static final int REQUEST_SETTINGS = 1;

	// Used in onNewIntent(), if the calling Activity wants the SearchActivity to start fresh
	private static final String EXTRA_NEW_SEARCH = "EXTRA_NEW_SEARCH";

	public static final long SEARCH_EXPIRATION = DateUtils.HOUR_IN_MILLIS;
	private static final String SEARCH_RESULTS_VERSION_FILE = "savedsearch-version.dat";
	private static final String SEARCH_RESULTS_FILE = "savedsearch.dat";

	private static final int ANIMATION_DIMMER_FADE_DURATION = 500;//ms

	private static final int MAXIMUM_SEARCH_LENGTH_DAYS = 28;

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ViewTreeObserver mViewTreeObserver;

	private DisableableViewPager mContentViewPager;
	private CalendarDatePicker mDatesCalendarDatePicker;
	private AutoCompleteTextView mSearchEditText;
	private ImageView mClearSearchButton;
	private ImageButton mDatesButton;
	private ImageButton mGuestsButton;
	private SimpleNumberPicker mAdultsNumberPicker;
	private SimpleNumberPicker mChildrenNumberPicker;
	private EditText mFilterHotelNameEditText;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mRatingButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;
	private View mVipAccessFilterButton;
	private TextView mDatesTextView;
	private TextView mGuestsTextView;
	private TextView mRefinementInfoTextView;
	private TextView mSelectChildAgeTextView;
	private View mButtonBarLayout;
	private View mDatesLayout;
	private View mFocusLayout;
	private View mGuestsLayout;
	private View mRefinementDismissView;
	private View mChildAgesLayout;
	private View mSearchButton;

	private View mFilterLayout;
	private PopupWindow mFilterPopupWindow;
	private PopupWindowPreDrawListener mPopupWindowPreDrawLisetner;

	private View mActionBarCustomView;

	// Progress bar stuff
	private ViewGroup mProgressBarLayout;
	private View mProgressBarHider;
	private View mProgressBarDimmer;
	private GLTagProgressBar mProgressBar;
	private TextView mProgressText;

	//----------------------------------
	// OTHERS
	//----------------------------------

	private Context mContext;

	private HotelListFragment mHotelListFragment;
	private HotelMapFragment mHotelMapFragment;

	private ActionMode mActionMode = null;

	private String mTag;

	private DisplayType mDisplayType = DisplayType.NONE;
	private ActivityState mActivityState = ActivityState.NONE;
	private boolean mShowDistance = true;

	private int mSortOptionSelectedId;
	private int mRadiusCheckedId = 0;
	private int mRatingCheckedId = 0;
	private int mPriceCheckedId = 0;
	private boolean mFilterVipAccess = false;

	private ArrayList<Address> mAddresses;
	private HotelSearchParams mOldSearchParams;
	private HotelSearchParams mEditedSearchParams;
	private HotelFilter mOldFilter;
	public boolean mStartSearchOnResume;
	private DateTime mLastSearchTime;
	private boolean mIsWidgetNotificationShowing;
	private boolean mWasOnDestroyCalled = false;

	private SearchSuggestionAdapter mSearchSuggestionAdapter;

	private boolean mIsActivityResumed = false;
	private boolean mIsOptionsMenuCreated = false;
	private boolean mIsSearchEditTextTextWatcherEnabled = false;
	private boolean mGLProgressBarStarted = false;
	private boolean mHasShownCalendar = false;

	// This indicates to mSearchCallback that we just loaded saved search results,
	// and as such should behave a bit differently than if we just did a new search.
	private boolean mLoadedSavedResults;

	// The last selection for the search EditText.  Used to maintain between rotations
	private int mSearchTextSelectionStart = -1;
	private int mSearchTextSelectionEnd = -1;

	private int mSearchEditTextPaddingRight = -1;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	// Determine if we're just doing a config change vs. finishing
	private boolean mConfigChange = false;

	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment mLocationFragment;

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private final Download<HotelOffersResponse> mSearchHotelDownload = new Download<HotelOffersResponse>() {
		@Override
		public HotelOffersResponse doDownload() {
			ExpediaServices services = new ExpediaServices(PhoneSearchActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_HOTEL_SEARCH, services);
			if (mEditedSearchParams != null) {
				throw new RuntimeException("edited search params not commited or cleared before search");
			}

			Property selectedProperty = new Property();
			selectedProperty.setPropertyId(Db.getHotelSearch().getSearchParams().getRegionId());

			return services.availability(Db.getHotelSearch().getSearchParams(), selectedProperty);
		}
	};

	private final Download<HotelSearchResponse> mSearchDownload = new Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(PhoneSearchActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			if (mEditedSearchParams != null) {
				throw new RuntimeException("edited search params not commited or cleared before search");
			}
			return services.search(Db.getHotelSearch().getSearchParams(), 0);
		}
	};

	private final OnDownloadComplete<HotelOffersResponse> mSearchHotelCallback = new OnDownloadComplete<HotelOffersResponse>() {

		@Override
		public void onDownload(HotelOffersResponse offersResponse) {
			if (offersResponse != null && offersResponse.isSuccess() && offersResponse.getProperty() != null) {
				Property property = offersResponse.getProperty();
				HotelSearchResponse searchResponse = new HotelSearchResponse();

				List<Rate> rates = offersResponse.getRates();
				if (rates != null) {
					Rate lowestRate = null;
					for (Rate rate : rates) {
						Money temp = rate.getDisplayRate();
						if (lowestRate == null) {
							lowestRate = rate;
						}
						if (lowestRate.getDisplayRate().getAmount().compareTo(temp.getAmount()) > 0) {
							lowestRate = rate;
						}
					}
					property.setLowestRate(lowestRate);
				}

				// #1550: Don't try to show the hotel if it has no rates
				if (property.getLowestRate() != null) {
					searchResponse.addProperty(property);

					// Forward to the hotel detail screen if the user searched by hotel name and selected one.
					if (Db.getHotelSearch().getSearchParams().getSearchType() == HotelSearchParams.SearchType.HOTEL) {
						startActivity(HotelDetailsFragmentActivity.createIntent(PhoneSearchActivity.this));
					}
					Db.getHotelSearch().setSearchResponse(searchResponse);
					Db.getHotelSearch().updateFrom(offersResponse);
					Db.getHotelSearch().setSelectedProperty(property);
				}
				else {
					Db.getHotelSearch().setSearchResponse(searchResponse);
				}

				loadSearchResponse(searchResponse);
			}
			else {
				Log.e("PhoneSearchActivity mSearchHotelCallback: Problem downloading HotelOffersResponse");
				simulateErrorResponse(R.string.error_server);
			}
		}

	};

	private final OnDownloadComplete<HotelSearchResponse> mSearchCallback = new OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse searchResponse) {
			Db.getHotelSearch().setSearchResponse(searchResponse);
			loadSearchResponse(searchResponse);
		}
	};

	private void loadSearchResponse(HotelSearchResponse searchResponse) {
		// Clear the old listener so we don't end up with a memory leak
		Db.getFilter().clearOnFilterChangedListeners();

		if (searchResponse != null && searchResponse.getPropertiesCount() > 0 && !searchResponse.hasErrors()) {
			HotelFilter filter = Db.getFilter();

			// HotelFilter reset is already called, hence reset appropriate searchRadius
			SearchType searchType = Db.getHotelSearch().getSearchParams().getSearchType();
			switch (searchType) {
			case CITY:
			case ADDRESS:
			case FREEFORM:
			case HOTEL:
			case VISIBLE_MAP_AREA:
				filter.setSearchRadius(SearchRadius.ALL);
				break;
			case MY_LOCATION:
			case POI:
				filter.setSearchRadius(SearchRadius.LARGE);
				break;
			}
			searchResponse.setFilter(filter);
			filter.addOnFilterChangedListener(PhoneSearchActivity.this);

			HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
			searchResponse.setSearchType(searchParams.getSearchType());
			searchResponse.setSearchLatLon(searchParams.getSearchLatitude(), searchParams.getSearchLongitude());

			if (!mLoadedSavedResults && searchResponse.getFilteredAndSortedProperties().length <= 10) {
				Log.i("Initial search results had not many results, expanding search radius filter to show all.");
				filter.setSearchRadius(SearchRadius.ALL);
				mRadiusCheckedId = R.id.radius_all_button;
				searchResponse.clearCache();
			}

			// #9773: Show distance sort initially, if user entered street address-level search params
			if (mShowDistance) {
				mSortOptionSelectedId = R.id.menu_select_sort_distance;
				buildFilter();
			}

			broadcastSearchCompleted(searchResponse);

			hideLoading();

			mLastSearchTime = DateTime.now();
		}
		else if (searchResponse != null && searchResponse.getPropertiesCount() > 0
				&& searchResponse.getLocations() != null && searchResponse.getLocations().size() > 0) {
			showDialog(DIALOG_LOCATION_SUGGESTIONS);
		}
		else if (searchResponse != null && searchResponse.getPropertiesCount() == 0 && !searchResponse.hasErrors()) {
			simulateErrorResponse(LayoutUtils.noHotelsFoundMessage(mContext));
		}
		else {
			handleError();
		}
	}

	private final Download<HotelSearchResponse> mLoadSavedResults = new Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			HotelSearchResponse response = null;
			File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);
			if (savedSearchResults.exists()) {
				if (CalendarUtils.isExpired(savedSearchResults.lastModified(), SEARCH_EXPIRATION)) {
					Log.d("There are saved search results, but they expired.  Starting a new search instead.");
				}
				else {
					try {
						long start = System.currentTimeMillis();
						JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SEARCH_RESULTS_FILE, mContext));
						response = new HotelSearchResponse(obj);
						Log.i("Loaded current search results, time taken: " + (System.currentTimeMillis() - start)
								+ " ms");
					}
					catch (IOException e) {
						Log.w("Couldn't load saved search results file.", e);
					}
					catch (JSONException e) {
						Log.w("Couldn't parse saved search results file.", e);
					}
				}
			}

			return response;
		}
	};

	private final OnDownloadComplete<HotelSearchResponse> mLoadSavedResultsCallback = new OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse results) {
			if (results == null) {
				// This means the load didn't work; kick off a new search
				startSearch();
			}
			else {
				mLoadedSavedResults = true;
				mSearchCallback.onDownload(results);
				mLoadedSavedResults = false;
			}
		}
	};

	private OnSearchParamsChangedInWidgetListener mSearchParamsChangedListener = new OnSearchParamsChangedInWidgetListener() {

		@Override
		public void onSearchParamsChanged(HotelSearchParams searchParams) {
			Db.getHotelSearch().setSearchParams(searchParams);
			if (searchParams != null) {
				searchParams.ensureValidCheckInDate();
			}
			else {
				Db.getHotelSearch().resetSearchParams();
			}
			mStartSearchOnResume = true;
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	public static Intent createIntent(Context context, boolean startNewSearch) {
		Intent intent = new Intent(context, PhoneSearchActivity.class);
		if (startNewSearch) {
			intent.putExtra(EXTRA_NEW_SEARCH, true);
		}
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// LIFECYCLE EVENTS
	//----------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		mContext = this;

		mLocationFragment = FusedLocationProviderFragment.getInstance(this);

		mListAndMapViewPagerAdapter = new ListAndMapViewPagerAdapter();

		setContentView(R.layout.activity_search);
		getWindow().setBackgroundDrawable(null);

		initializeViews();

		mSearchSuggestionAdapter = new SearchSuggestionAdapter(this);
		mSearchEditText.setAdapter(mSearchSuggestionAdapter);

		boolean startNewSearch = getIntent().getBooleanExtra(EXTRA_NEW_SEARCH, false);

		if (startNewSearch) {
			Db.clear();
			saveParams();
			// Remove it so we don't keep doing this on rotation
			getIntent().removeExtra(EXTRA_NEW_SEARCH);
		}

		boolean toBroadcastSearchCompleted = false;
		HotelSearchResponse searchResponse = Db.getHotelSearch().getSearchResponse();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mTag = prefs.getString("tag", getString(R.string.tag_hotel_list));

		if (getIntent().hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
			//If this is a search coming from flights, we expect the Db.searchParams to already be valid
			mSearchEditText.setText(Db.getHotelSearch().getSearchParams().getUserQuery());
			Log.i("searchEditText...:" + mSearchEditText.getText().toString());
			Db.resetFilter();
			mShowDistance = false;
			startSearch();
			getIntent().removeExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS);
		}
		else {
			restoreActivityState(savedInstanceState);

			if (searchResponse != null) {
				if (searchResponse.hasErrors()) {
					handleError();
				}
				else {
					searchResponse.setFilter(Db.getFilter());
					toBroadcastSearchCompleted = true;
				}

				if (Db.getHotelSearch().getSearchParams() != null
						&& Db.getHotelSearch().getSearchParams().getSearchType() != null) {
					mShowDistance = Db.getHotelSearch().getSearchParams().getSearchType().shouldShowDistance();
				}
			}
			else {
				startSearch();
			}
		}

		// Setup custom action bar view
		ActionBar actionBar = getSupportActionBar();

		if (!ExpediaBookingApp.IS_VSC) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		// For VSC app the hotelListing is the launch screen.
		else {
			actionBar.setHomeButtonEnabled(false);
		}

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setCustomView(mActionBarCustomView);

		HotelSearchParams searchParams = getCurrentSearchParams();

		mAdultsNumberPicker.setFormatter(mAdultsNumberPickerFormatter);
		mAdultsNumberPicker.setMinValue(1);
		mAdultsNumberPicker.setMaxValue(GuestsPickerUtils.getMaxAdults(0));
		mAdultsNumberPicker.setValue(searchParams.getNumAdults());

		mChildrenNumberPicker.setFormatter(mChildrenNumberPickerFormatter);
		mChildrenNumberPicker.setMinValue(0);
		mChildrenNumberPicker.setMaxValue(GuestsPickerUtils.getMaxChildren(1));
		mChildrenNumberPicker.setValue(searchParams.getNumChildren());

		showFragment(mTag);
		setShowDistance(mShowDistance);

		// 9028:t only broadcast search completed once all
		// elements have been setup
		if (toBroadcastSearchCompleted) {
			broadcastSearchCompleted(searchResponse);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		((ExpediaBookingApp) getApplicationContext())
				.unregisterSearchParamsChangedInWidgetListener(mSearchParamsChangedListener);

		mIsActivityResumed = false;

		mProgressBar.onPause();
		stopLocation();

		Db.getFilter().removeOnFilterChangedListener(this);

		if (!isFinishing()) {
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			downloader.unregisterDownloadCallback(KEY_GEOCODE);
			downloader.unregisterDownloadCallback(KEY_LOADING_PREVIOUS);
			downloader.unregisterDownloadCallback(KEY_SEARCH);
			downloader.unregisterDownloadCallback(KEY_HOTEL_SEARCH);
		}
		else {
			saveParams();
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		((ExpediaBookingApp) getApplicationContext())
				.registerSearchParamsChangedInWidgetListener(mSearchParamsChangedListener);

		Db.getFilter().addOnFilterChangedListener(this);

		if (mDisplayType != DisplayType.CALENDAR) {
			mProgressBar.onResume();
			mProgressBar.reset();
		}

		if (Db.getHotelSearch().getSearchResponse() != null
				&& Db.getHotelSearch().getSearchResponse().getPropertiesCount() == 0) {
			simulateErrorResponse(LayoutUtils.noHotelsFoundMessage(mContext));
		}

		CalendarUtils.configureCalendarDatePicker(mDatesCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID,
				LineOfBusiness.HOTELS);

		// setDisplayType here because it could possibly add a TextWatcher before the view has restored causing the listener to fire
		setDisplayType(mDisplayType, false);

		setSearchEditViews();
		GuestsPickerUtils.configureAndUpdateDisplayedValues(this, mAdultsNumberPicker, mChildrenNumberPicker);

		displayRefinementInfo();
		setActionBarBookingInfoText();

		if (mStartSearchOnResume) {
			Db.getHotelSearch().getSearchParams().ensureValidCheckInDate();
			startSearch();
			mStartSearchOnResume = false;
		}
		// Check if the last search results are expired
		else if (JodaUtils.isExpired(mLastSearchTime, SEARCH_EXPIRATION)) {
			Log.d("onResume(): There are cached search results, but they expired.  Starting a new search instead.");
			Db.getHotelSearch().getSearchParams().ensureValidCheckInDate();
			startSearch();
		}
		// Check if the date in the HotelSearchParams is outdated (i.e. if it's now after midnight)
		else if (!Db.getHotelSearch().getSearchParams().hasValidCheckInDate()) {
			// Trigger a new search
			Db.getHotelSearch().getSearchParams().ensureValidCheckInDate();
			startSearch();
		}
		else if (Db.getHotelSearch().getSearchParams().getSearchType() != null
				&& Db.getHotelSearch().getSearchParams().getSearchType() == SearchType.MY_LOCATION
				&& !Db.getHotelSearch().getSearchParams().hasSearchLatLon()) {
			Log.d("onResume(): We were attempting to search by current location, but do not yet have valid coordinates. Starting a new search (and getting new coords if needed).");
			Db.getHotelSearch().getSearchParams().ensureValidCheckInDate();
			startSearch();
		}
		else {
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			if (downloader.isDownloading(KEY_LOADING_PREVIOUS)) {
				Log.d("Already loading previous search results, resuming the load...");
				mActivityState = ActivityState.SEARCHING;
				downloader.registerDownloadCallback(KEY_LOADING_PREVIOUS, mLoadSavedResultsCallback);
				showLoading(true, R.string.loading_previous);
			}
			else if (downloader.isDownloading(KEY_GEOCODE)) {
				Log.d("Already geocoding, resuming the search...");
				mActivityState = ActivityState.SEARCHING;
				downloader.registerDownloadCallback(KEY_GEOCODE, mGeocodeCallback);
				showLoading(true, R.string.progress_searching_hotels);
			}
			else if (downloader.isDownloading(KEY_SEARCH)) {
				Log.d("Already searching, resuming the search...");
				mActivityState = ActivityState.SEARCHING;
				downloader.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
				showLoading(true, R.string.progress_searching_hotels);
			}
			else if (downloader.isDownloading(KEY_HOTEL_SEARCH)) {
				Log.d("Already searching, resuming the hotel name search...");
				mActivityState = ActivityState.SEARCHING;
				downloader.registerDownloadCallback(KEY_HOTEL_SEARCH, mSearchHotelCallback);
				//TODO: Check if this is the correct string to display while searching for hotel by name.
				showLoading(true, R.string.progress_searching_selected_hotel);
			}
			else {
				hideLoading();
			}
		}

		mIsActivityResumed = true;

		OmnitureTracking.onResume(this);
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		mConfigChange = true;
		return super.onRetainCustomNonConfigurationInstance();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}

		mWasOnDestroyCalled = true;

		if (mActionMode != null) {
			mActionMode.finish();
		}

		// do not attempt to save parameters if the user was short circuited to the
		// confirmation screen when the search activity started
		if (isFinishing()) {
			saveParams();

			File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);

			HotelSearchResponse searchResponse = Db.getHotelSearch().getSearchResponse();

			// Cancel any currently downloading searches
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			if (downloader.isDownloading(KEY_SEARCH)) {
				Log.d("Cancelling search because activity is ending.");
				downloader.cancelDownload(KEY_SEARCH);
			}
			if (downloader.isDownloading(KEY_LOADING_PREVIOUS)) {
				Log.d("Cancelling loading previous results because activity is ending.");
				downloader.cancelDownload(KEY_LOADING_PREVIOUS);
			}
			if (downloader.isDownloading(KEY_HOTEL_SEARCH)) {
				Log.d("Cancelling search by hotel name because activity is ending.");
				downloader.cancelDownload(KEY_HOTEL_SEARCH);
			}

			// Save a search response as long as:
			// 1. We weren't currently searching
			// 2. The search response exists and has no errors.
			// 3. We don't already have a saved search response (means nothing changed)
			else if (searchResponse != null && !searchResponse.hasErrors() && !savedSearchResults.exists()) {
				try {
					long start = System.currentTimeMillis();
					IoUtils.writeStringToFile(SEARCH_RESULTS_VERSION_FILE, "" + AndroidUtils.getAppCode(mContext), this);
					IoUtils.writeStringToFile(SEARCH_RESULTS_FILE, searchResponse.toJson().toString(0), this);
					Log.i("Saved current search results, time taken: " + (System.currentTimeMillis() - start) + " ms");
				}
				catch (IOException e) {
					Log.w("Couldn't save search results.", e);
				}
				catch (JSONException e) {
					Log.w("Couldn't save search results.", e);
				}
				catch (OutOfMemoryError e) {
					Log.w("Ran out of memory while trying to save search results file", e);
				}
			}

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putAll(saveActivityState());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// We have a settings menu for VSC app only. And is shown only for debug builds.
		if (requestCode == REQUEST_SETTINGS
				&& resultCode == ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS) {
			Db.getHotelSearch().resetSearchData();
		}
	}

	//----------------------------------
	// DIALOGS
	//----------------------------------

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOCATION_SUGGESTIONS: {
			// If we're displaying this, show an empty progress bar
			showLoading(false, null);

			CharSequence[] freeformLocations = StrUtils.formatAddresses(mAddresses);

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.ChooseLocation);
			builder.setItems(freeformLocations, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Address address = mAddresses.get(which);
					String formattedAddress = StrUtils.removeUSAFromAddress(address);
					HotelSearchParams searchParams = getCurrentSearchParams();
					SearchType searchType = SearchUtils.isExactLocation(address) ? SearchType.ADDRESS : SearchType.CITY;

					// The user found a better version of the search they ran,
					// so we'll replace it from startSearchDownloader
					Search.delete(PhoneSearchActivity.this, searchParams);

					searchParams.setQuery(formattedAddress);
					setSearchEditViews();
					searchParams.setSearchLatLon(address.getLatitude(), address.getLongitude());
					searchParams.setSearchType(searchType);

					setShowDistance(searchType.shouldShowDistance());
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearchDownloader();
					notifySearchLocationFound();
				}
			});
			builder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					simulateErrorResponse(getString(R.string.NoGeocodingResults, getCurrentSearchParams().getQuery()));
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					simulateErrorResponse(getString(R.string.NoGeocodingResults, getCurrentSearchParams().getQuery()));
				}
			});
			return builder.create();
		}
		case DIALOG_CLIENT_DEPRECATED: {
			AlertDialog.Builder builder = new Builder(this);
			final ServerError error = Db.getHotelSearch().getSearchResponse().getErrors().get(0);
			builder.setMessage(error.getExtra("message"));
			builder.setPositiveButton(R.string.upgrade, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SocialUtils.openSite(PhoneSearchActivity.this, error.getExtra("url"));
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			return builder.create();
		}
		case DIALOG_ENABLE_LOCATIONS: {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(R.string.EnableLocationSettings);
			builder.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					if (NavUtils.isIntentAvailable(mContext, intent)) {
						startActivity(intent);
					}
					mStartSearchOnResume = true;
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			return builder.create();
		}
		case DIALOG_INVALID_SEARCH_RANGE: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.search_error);
			builder.setMessage(getString(R.string.hotel_search_range_error_TEMPLATE, MAXIMUM_SEARCH_LENGTH_DAYS));
			builder.setPositiveButton(R.string.ok, null);
			return builder.create();
		}
		}

		return super.onCreateDialog(id);
	}

	//----------------------------------
	// MENUS
	//----------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_search, menu);

		// #1169. Add "About/Info" menu and debug API status menu item if VSC build.
		if (ExpediaBookingApp.IS_VSC) {
			getSupportMenuInflater().inflate(R.menu.menu_launch_vsc, menu);
			DebugMenu.onCreateOptionsMenu(this, menu);
		}

		boolean ret = super.onCreateOptionsMenu(menu);

		mIsOptionsMenuCreated = true;
		return ret;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Configure the sort buttons
		switch (Db.getFilter().getSort()) {
		case POPULAR:
			mSortOptionSelectedId = R.id.menu_select_sort_popularity;
			break;
		case DEALS:
			mSortOptionSelectedId = R.id.menu_select_sort_deals;
			break;
		case PRICE:
			mSortOptionSelectedId = R.id.menu_select_sort_price;
			break;
		case RATING:
			mSortOptionSelectedId = R.id.menu_select_sort_user_rating;
			break;
		case DISTANCE:
			mSortOptionSelectedId = R.id.menu_select_sort_distance;
			break;
		default:
			mSortOptionSelectedId = R.id.menu_select_sort_popularity;
			break;
		}
		menu.findItem(mSortOptionSelectedId).setChecked(true);

		boolean shouldEnableMenuItems = Db.getHotelSearch().getSearchResponse() != null;
		menu.findItem(R.id.menu_select_sort).setEnabled(shouldEnableMenuItems);
		menu.findItem(R.id.menu_select_filter).setEnabled(shouldEnableMenuItems);
		menu.findItem(R.id.menu_select_search_map).setEnabled(shouldEnableMenuItems);
		menu.findItem(R.id.menu_select_change_view).setEnabled(shouldEnableMenuItems);

		// Disable distance sort
		menu.findItem(R.id.menu_select_sort_distance).setVisible(mShowDistance);

		// Configure the map/list view action
		if (mTag == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			mTag = prefs.getString("tag", getString(R.string.tag_hotel_list));
		}
		boolean isListShowing = mTag.equals(getString(R.string.tag_hotel_list));
		if (isListShowing) {
			menu.findItem(R.id.menu_select_change_view).setIcon(R.drawable.ic_menu_map);
		}
		else {
			menu.findItem(R.id.menu_select_change_view).setIcon(R.drawable.ic_menu_list);
		}
		menu.findItem(R.id.menu_select_sort).setVisible(isListShowing);
		menu.findItem(R.id.menu_select_search_map).setVisible(!isListShowing);

		// Push actions into the overflow in landscape mode
		int orientation = getWindowManager().getDefaultDisplay().getOrientation();
		final boolean shouldShowMenuItems = orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180;
		final int menuFlags = shouldShowMenuItems ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_NEVER;
		menu.findItem(R.id.menu_select_sort).setShowAsActionFlags(menuFlags);
		menu.findItem(R.id.menu_select_filter).setShowAsActionFlags(menuFlags);
		menu.findItem(R.id.menu_select_search_map).setShowAsActionFlags(menuFlags);

		// #1169. VSC app related menu arrangement.
		// We need to only show an "About/Info" menu item. Show settings only for debug build for testing purpose.
		if (ExpediaBookingApp.IS_VSC && AndroidUtils.isRelease(this)) {
			MenuItem settingsBtn = menu.findItem(R.id.settings);
			if (settingsBtn != null) {
				settingsBtn.setVisible(false);
			}

			DebugMenu.onPrepareOptionsMenu(this, menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean rebuildFilter = false;
		boolean invalidateOptionsMenu = false;

		switch (item.getItemId()) {
		// Home as up
		case android.R.id.home:
			NavUtils.goToLaunchScreen(mContext);

			finish();
			break;

		// Sort
		case R.id.menu_select_sort: {
			setDisplayType(DisplayType.NONE);
			break;
		}

		case R.id.menu_select_sort_popularity:
			OmnitureTracking.trackLinkHotelSort(mContext, OmnitureTracking.HOTELS_SEARCH_SORT_POPULAR);
			mSortOptionSelectedId = item.getItemId();
			rebuildFilter = true;
			break;
		case R.id.menu_select_sort_deals:
			OmnitureTracking.trackLinkHotelSort(mContext, OmnitureTracking.HOTELS_SEARCH_SORT_DEALS);
			mSortOptionSelectedId = item.getItemId();
			rebuildFilter = true;
			break;
		case R.id.menu_select_sort_price:
			OmnitureTracking.trackLinkHotelSort(mContext, OmnitureTracking.HOTELS_SEARCH_SORT_PRICE);
			mSortOptionSelectedId = item.getItemId();
			rebuildFilter = true;
			break;
		case R.id.menu_select_sort_user_rating:
			OmnitureTracking.trackLinkHotelSort(mContext, OmnitureTracking.HOTELS_SEARCH_SORT_RATING);
			mSortOptionSelectedId = item.getItemId();
			rebuildFilter = true;
			break;
		case R.id.menu_select_sort_distance:
			OmnitureTracking.trackLinkHotelSort(mContext, OmnitureTracking.HOTELS_SEARCH_SORT_DISTANCE);
			mSortOptionSelectedId = item.getItemId();
			rebuildFilter = true;
			break;

		// Map Button
		case R.id.menu_select_change_view: {
			switchResultsView();
			invalidateOptionsMenu = true;
			break;
		}

		// Search visible map area
		case R.id.menu_select_search_map: {
			HotelSearchParams searchParams = getCurrentSearchParams();
			searchParams.clearQuery();

			if (mHotelMapFragment != null) {
				LatLng center = mHotelMapFragment.getCameraCenter();
				searchParams.setSearchType(SearchType.VISIBLE_MAP_AREA);
				searchParams.setSearchLatLon(center.latitude, center.longitude);
				setShowDistance(searchParams.getSearchType().shouldShowDistance());
				startSearch();
			}
			break;
		}

		// Filters
		case R.id.menu_select_filter: {
			if (mFilterPopupWindow.isShowing()) {
				setDisplayType(DisplayType.NONE);
			}
			else {
				setDisplayType(DisplayType.FILTER);
			}
			break;
		}

		////////////////////////////////////////////////////////////////
		// VSC related menu items

		// #1169. VSC "About" menu item.
		case R.id.about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			break;

		// VSC "Settings" menu item.
		// Currently we show this only for Debug build.
		case R.id.settings: {
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			return true;
		}

		}

		if (rebuildFilter) {
			buildFilter();
			invalidateOptionsMenu = true;
		}

		if (invalidateOptionsMenu) {
			supportInvalidateOptionsMenu();
		}

		// VSC related menu
		if (ExpediaBookingApp.IS_VSC && DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void supportInvalidateOptionsMenu() {
		if (mIsOptionsMenuCreated) {
			super.supportInvalidateOptionsMenu();
		}
	}

	private ActionMode.Callback mSearchActionMode = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			mode.getMenuInflater().inflate(R.menu.action_mode_search, menu);
			final MenuItem searchMenuItem = menu.findItem(R.id.menu_select_search);
			Button searchButton = (Button) getLayoutInflater().inflate(R.layout.actionbar_checkmark_item, null);
			searchButton.setText(getString(R.string.SEARCH));
			ViewUtils.setAllCaps(searchButton);

			searchButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mSearchActionMode.onActionItemClicked(mode, searchMenuItem);
				}
			});

			searchMenuItem.setActionView(searchButton);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_select_search:
				startSearch();
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			if (!mWasOnDestroyCalled) {
				setDisplayType(DisplayType.NONE);
			}
		}
	};

	//----------------------------------
	// KEY EVENTS
	//----------------------------------

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mDisplayType != DisplayType.NONE) {
				setDisplayType(DisplayType.NONE);
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	//----------------------------------
	// LOCATION METHODS
	//----------------------------------

	private void findLocation() {
		if (!NetUtils.isOnline(mContext)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}
		else {
			showLoading(true, R.string.progress_finding_location);
		}

		mLocationFragment.find(new Listener() {
			@Override
			public void onFound(Location currentLocation) {
				PhoneSearchActivity.this.onLocationFound(currentLocation);
			}

			@Override
			public void onError() {
				simulateErrorResponse(R.string.ProviderDisabled);
				OmnitureTracking.trackErrorPage(mContext, "LocationServicesNotAvailable");
			}
		});
	}

	private void stopLocation() {
		if (mLocationFragment != null) {
			mLocationFragment.stop();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEW INITIALIZATION
	//----------------------------------

	private void initializeViews() {
		// Get views
		mFocusLayout = findViewById(R.id.focus_layout);

		// We have created the fragments in onCreate so we can attach the adapter safely
		mContentViewPager = (DisableableViewPager) findViewById(R.id.content_viewpager);
		mContentViewPager.setPageSwipingEnabled(false);
		mContentViewPager.setAdapter(mListAndMapViewPagerAdapter);
		mContentViewPager.setOnPageChangeListener(mListAndMapViewPagerAdapter);

		// Handled in the actionbar's custom view now
		mActionBarCustomView = getLayoutInflater().inflate(R.layout.actionbar_search_hotels, null);
		mSearchEditText = (AutoCompleteTextView) mActionBarCustomView.findViewById(R.id.search_edit_text);
		mClearSearchButton = (ImageView) mActionBarCustomView.findViewById(R.id.clear_search_button);
		mDatesButton = (ImageButton) mActionBarCustomView.findViewById(R.id.dates_button);
		mDatesTextView = (TextView) mActionBarCustomView.findViewById(R.id.dates_text_view);
		mGuestsButton = (ImageButton) mActionBarCustomView.findViewById(R.id.guests_button);
		mGuestsTextView = (TextView) mActionBarCustomView.findViewById(R.id.guests_text_view);

		mRefinementDismissView = findViewById(R.id.refinement_dismiss_view);
		mSearchButton = findViewById(R.id.search_button);

		mDatesLayout = findViewById(R.id.dates_layout);
		mDatesCalendarDatePicker = (CalendarDatePicker) findViewById(R.id.dates_date_picker);
		mGuestsLayout = findViewById(R.id.guests_layout);
		mChildAgesLayout = findViewById(R.id.child_ages_layout);
		mAdultsNumberPicker = (SimpleNumberPicker) findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (SimpleNumberPicker) findViewById(R.id.children_number_picker);

		mButtonBarLayout = findViewById(R.id.button_bar_layout);
		mRefinementInfoTextView = (TextView) findViewById(R.id.refinement_info_text_view);
		mSelectChildAgeTextView = (TextView) findViewById(R.id.label_select_each_childs_age);

		mProgressBarLayout = (ViewGroup) findViewById(R.id.search_progress_layout);
		mProgressBar = (GLTagProgressBar) findViewById(R.id.search_progress_bar);
		mProgressText = (TextView) findViewById(R.id.search_progress_text_view);
		mProgressBarHider = findViewById(R.id.search_progress_hider);
		mProgressBarDimmer = findViewById(R.id.search_progress_dimmer);

		CalendarUtils.configureCalendarDatePicker(mDatesCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID,
				LineOfBusiness.HOTELS);

		mFilterLayout = getLayoutInflater().inflate(R.layout.popup_filter_options, null);
		mFilterHotelNameEditText = (EditText) mFilterLayout.findViewById(R.id.filter_hotel_name_edit_text);
		mRadiusButtonGroup = (SegmentedControlGroup) mFilterLayout.findViewById(R.id.radius_filter_button_group);
		mRatingButtonGroup = (SegmentedControlGroup) mFilterLayout.findViewById(R.id.rating_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) mFilterLayout.findViewById(R.id.price_filter_button_group);
		mVipAccessFilterButton = Ui.findView(mFilterLayout, R.id.filter_vip_access);
		if (PointOfSale.getPointOfSale().supportsVipAccess()) {
			mVipAccessFilterButton.setVisibility(View.VISIBLE);
			mVipAccessFilterButton.setOnClickListener(mVipAccessClickListener);
		}

		mFilterHotelNameEditText.setOnEditorActionListener(mFilterEditorActionLisenter);

		// Special case for HTC keyboards, which seem to ignore the android:inputType="textFilter|textNoSuggestions" xml flag
		mSearchEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
				| InputType.TYPE_TEXT_VARIATION_FILTER);

		// Setup popup
		mFilterLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mFilterPopupWindow = new PopupWindow(mFilterLayout, mFilterLayout.getMeasuredWidth(),
				mFilterLayout.getMeasuredHeight(), true);
		mFilterPopupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.abs__menu_dropdown_panel_holo_dark));
		mFilterPopupWindow.setAnimationStyle(R.style.Animation_Popup);
		mFilterPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);

		// Progress bar
		mProgressBar.addOnDrawStartedListener(this);

		// mProgressText is positioned differently based on orientation
		// Could do this in XML, but more difficult due to include rules
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mProgressText.getLayoutParams();
		int orientation = getWindowManager().getDefaultDisplay().getOrientation();
		if (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
		else {
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		}

		//===================================================================
		// Listeners
		mSearchEditText.setOnFocusChangeListener(mSearchEditTextFocusChangeListener);
		mSearchEditText.setOnItemClickListener(mSearchSuggestionsItemClickListener);
		mSearchEditText.setOnEditorActionListener(mSearchEditorActionListener);
		mDatesButton.setOnClickListener(mDatesButtonClickListener);
		mGuestsButton.setOnClickListener(mGuestsButtonClickListener);
		if (mSearchButton != null) {
			mSearchButton.setOnClickListener(mSearchButtonClickListener);
		}

		mClearSearchButton.setOnClickListener(mClearSearchButtonOnClickListener);

		mRefinementDismissView.setOnClickListener(mRefinementDismissViewClickListener);

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);
		mAdultsNumberPicker.setOnValueChangeListener(mNumberPickerChangedListener);
		mChildrenNumberPicker.setOnValueChangeListener(mNumberPickerChangedListener);
	}

	//----------------------------------
	// SEARCH METHODS
	//----------------------------------

	private void buildFilter() {
		Log.d("Building up filter from current view settings...");

		HotelFilter filter = Db.getFilter();
		HotelFilter currentFilter = filter.copy();

		// Distance
		switch (mRadiusCheckedId) {
		case R.id.radius_small_button: {
			filter.setSearchRadius(SearchRadius.SMALL);
			break;
		}
		case R.id.radius_medium_button: {
			filter.setSearchRadius(SearchRadius.MEDIUM);
			break;
		}
		case R.id.radius_large_button: {
			filter.setSearchRadius(SearchRadius.LARGE);
			break;
		}
		default:
		case R.id.radius_all_button: {
			filter.setSearchRadius(SearchRadius.ALL);
			break;
		}
		}

		// Rating
		switch (mRatingCheckedId) {
		case R.id.rating_low_button: {
			filter.setMinimumStarRating(3);
			break;
		}
		case R.id.rating_medium_button: {
			filter.setMinimumStarRating(4);
			break;
		}
		case R.id.rating_high_button: {
			filter.setMinimumStarRating(5);
			break;
		}
		default:
		case R.id.rating_all_button: {
			filter.setMinimumStarRating(0);
			break;
		}
		}

		// Price
		switch (mPriceCheckedId) {
		case R.id.price_cheap_button: {
			filter.setPriceRange(PriceRange.CHEAP);
			break;
		}
		case R.id.price_moderate_button: {
			filter.setPriceRange(PriceRange.MODERATE);
			break;
		}
		case R.id.price_expensive_button: {
			filter.setPriceRange(PriceRange.EXPENSIVE);
			break;
		}
		default:
		case R.id.price_all_button: {
			filter.setPriceRange(PriceRange.ALL);
			break;
		}
		}

		// Sort
		switch (mSortOptionSelectedId) {
		case R.id.menu_select_sort_popularity: {
			filter.setSort(Sort.POPULAR);
			break;
		}
		case R.id.menu_select_sort_deals: {
			filter.setSort(Sort.DEALS);
			break;
		}
		case R.id.menu_select_sort_price: {
			filter.setSort(Sort.PRICE);
			break;
		}
		case R.id.menu_select_sort_user_rating: {
			filter.setSort(Sort.RATING);
			break;
		}
		case R.id.menu_select_sort_distance: {
			filter.setSort(Sort.DISTANCE);
			break;
		}
		}

		// VIP Access
		filter.setVipAccessOnly(mFilterVipAccess);

		/*
		 * Don't notify listeners of the filter having changed when the activity is either not
		 * completely setup or paused. This is because we don't want the filter changes to propogate
		 * when the radio buttons are being setup as it causes wasted cycles notifying all listeners
		 */
		if (currentFilter == null || !filter.equals(currentFilter) && mIsActivityResumed) {
			Log.d("HotelFilter has changed, notifying listeners.");
			if (currentFilter == null) {
				Log.d("HotelFilter diff: Current filter == null");
			}
			else {
				filter.diff(currentFilter);
			}
			filter.notifyFilterChanged();
		}
	}

	private void saveParams() {
		Log.d("Saving search parameters, filter and tag...");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Db.getHotelSearch().getSearchParams().saveToSharedPreferences(prefs);
		Editor editor = prefs.edit();
		editor.putString("filter", Db.getFilter().toJson().toString());
		editor.putString("tag", mTag);
		editor.putBoolean("showDistance", mShowDistance);
		SettingUtils.commitOrApply(editor);
	}

	@Override
	public boolean onSearchRequested() {
		Log.d("onSearchRequested called");
		boolean currentIsSearchDisplay = mDisplayType.isSearchDisplay();
		if (currentIsSearchDisplay) {
			commitEditedSearchParams();
			startSearch();
		}

		// Returning false blocks a higher order search from launching
		return false;
	}

	private void updateCalendarInstructionText() {
		HotelSearchParams params = getCurrentSearchParams();
		if (mDatesCalendarDatePicker != null) {
			String dateRangeFormatStr = getString(R.string.calendar_instructions_date_range_TEMPLATE);
			String dateRangeNightsFormatStr = getString(R.string.calendar_instructions_date_range_with_nights_TEMPLATE);
			DateTimeFormatter format = DateTimeFormat.forPattern("MMM dd");

			LocalDate dateStart = params.getCheckInDate();
			LocalDate dateEnd = params.getCheckOutDate();
			boolean researchMode = mDatesCalendarDatePicker.getOneWayResearchMode();

			if (dateStart != null && dateEnd != null) {
				int nightCount = params.getStayDuration();
				String nightCountStr = getResources().getQuantityString(R.plurals.length_of_stay, nightCount,
						nightCount);
				mDatesCalendarDatePicker.setHeaderInstructionText(String.format(dateRangeNightsFormatStr,
						format.print(dateStart), format.print(dateEnd), nightCountStr));
			}
			else if (dateStart != null && researchMode) {
				mDatesCalendarDatePicker
						.setHeaderInstructionText(getString(R.string.calendar_instructions_hotels_no_dates_selected));
			}
			else if (dateStart != null) {
				mDatesCalendarDatePicker.setHeaderInstructionText(String.format(dateRangeFormatStr,
						format.print(dateStart), ""));
			}
			else {
				mDatesCalendarDatePicker
						.setHeaderInstructionText(getString(R.string.calendar_instructions_hotels_no_dates_selected));
			}
		}
	}

	private boolean checkSearchRange() {
		if (getCurrentSearchParams() != null && getCurrentSearchParams().getStayDuration() > MAXIMUM_SEARCH_LENGTH_DAYS) {
			showDialog(DIALOG_INVALID_SEARCH_RANGE);
			return false;
		}
		else {
			return true;
		}
	}

	private void startSearch() {
		if (!checkSearchRange()) {
			return;
		}
		Log.i("Starting a new search...");
		mActivityState = ActivityState.SEARCHING;

		Db.getHotelSearch().resetSearchData();

		broadcastSearchStarted();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_GEOCODE);
		bd.cancelDownload(KEY_SEARCH);
		bd.cancelDownload(KEY_HOTEL_SEARCH);
		bd.cancelDownload(KEY_LOADING_PREVIOUS);

		// Delete the currently saved search results
		File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);
		if (savedSearchResults.exists()) {
			boolean results = savedSearchResults.delete();
			Log.d("Deleting previous search results.  Success: " + results);
		}

		buildFilter();
		commitEditedSearchParams();
		setDisplayType(DisplayType.NONE);
		saveParams();

		SearchType searchType = Db.getHotelSearch().getSearchParams().getSearchType();
		switch (searchType) {
		case CITY:
		case ADDRESS:
		case POI:
		case FREEFORM:
		case HOTEL:
			setShowDistance(searchType.shouldShowDistance());
			stopLocation();
			startGeocode();
			break;

		case VISIBLE_MAP_AREA:
			stopLocation();
			startSearchDownloader();
			break;

		case MY_LOCATION:
			// Use faked location first
			Location location = ExpediaDebugUtil.getFakeLocation(mContext);

			if (location != null) {
				onLocationFound(location);
			}
			else {
				findLocation();
			}

			break;
		}
	}

	private void startGeocode() {
		showLoading(true, R.string.progress_searching_hotels);

		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();

		if (searchParams.hasEnoughToSearch()) {
			Log.d("User already has region id or lat/lng for freeform location, skipping geocoding.");
			notifySearchLocationFound();
			startSearchDownloader();
			return;
		}

		Log.d("Geocoding: " + searchParams.getQuery());

		searchParams.setUserQuery(searchParams.getQuery());

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_GEOCODE);
		bd.startDownload(KEY_GEOCODE, mGeocodeDownload, mGeocodeCallback);
	}

	private final Download<List<Address>> mGeocodeDownload = new Download<List<Address>>() {
		public List<Address> doDownload() {
			return LocationServices.geocodeGoogle(mContext, Db.getHotelSearch().getSearchParams().getQuery());
		}
	};

	private final OnDownloadComplete<List<Address>> mGeocodeCallback = new OnDownloadComplete<List<Address>>() {
		public void onDownload(List<Address> results) {
			if (results == null || results.size() == 0) {
				OmnitureTracking.trackErrorPage(PhoneSearchActivity.this, "LocationNotFound");
				simulateErrorResponse(R.string.geolocation_failed);
			}
			else {
				// Need to convert to ArrayList so it can be saved easily in Bundles
				mAddresses = new ArrayList<Address>();
				for (Address address : results) {
					mAddresses.add(address);
				}

				if (mAddresses.size() > 1) {
					showLoading(false, null);

					showDialog(DIALOG_LOCATION_SUGGESTIONS);
				}
				else if (mAddresses.size() > 0) {
					Address address = mAddresses.get(0);
					String formattedAddress = StrUtils.removeUSAFromAddress(address);
					HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
					SearchType searchType = SearchUtils.isExactLocation(address) ? SearchType.ADDRESS : SearchType.CITY;

					// The user found a better version of the search they ran,
					// so we'll replace it from startSearchDownloader
					Search.delete(PhoneSearchActivity.this, searchParams);

					searchParams.setQuery(formattedAddress);
					setSearchEditViews();
					searchParams.setSearchLatLon(address.getLatitude(), address.getLongitude());
					searchParams.setSearchType(searchType);
					notifySearchLocationFound();
					setShowDistance(searchType.shouldShowDistance());
					startSearchDownloader();
				}
			}
		}
	};

	private void onLocationFound(Location location) {
		Log.d("onLocationFound(): " + location.toString());
		Db.getHotelSearch().getSearchParams().setSearchLatLon(location.getLatitude(), location.getLongitude());
		setShowDistance(true);
		notifySearchLocationFound();
		startSearchDownloader();
	}

	private void startSearchDownloader() {
		SearchType searchType = Db.getHotelSearch().getSearchParams().getSearchType();

		if (searchType == SearchType.HOTEL) {
			showLoading(true, R.string.progress_searching_selected_hotel);
		}
		else {
			showLoading(true, R.string.progress_searching_hotels);
		}

		commitEditedSearchParams();

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

		SearchType type = Db.getHotelSearch().getSearchParams().getSearchType();
		if (type != SearchType.MY_LOCATION && type != SearchType.VISIBLE_MAP_AREA) {
			Search.add(this, Db.getHotelSearch().getSearchParams());
		}

		Log.d("Resetting filter...");
		Db.resetFilter();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		if (searchType == SearchType.HOTEL) {
			bd.cancelDownload(KEY_HOTEL_SEARCH);
			bd.startDownload(KEY_HOTEL_SEARCH, mSearchHotelDownload, mSearchHotelCallback);
		}
		else {
			bd.cancelDownload(KEY_SEARCH);
			bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
		}
	}

	//----------------------------------
	// ACTIVITY STATE METHODS
	//----------------------------------

	private static final String INSTANCE_TAG = "INSTANCE_TAG";
	private static final String INSTANCE_OLD_SEARCH_PARAMS = "INSTANCE_OLD_SEARCH_PARAMS";
	private static final String INSTANCE_EDITED_SEARCH_PARAMS = "INSTANCE_EDITED_SEARCH_PARAMS";
	private static final String INSTANCE_OLD_FILTER = "INSTANCE_OLD_FILTER";
	private static final String INSTANCE_SHOW_DISTANCES = "INSTANCE_SHOW_DISTANCES";
	private static final String INSTANCE_START_SEARCH_ON_RESUME = "INSTANCE_START_SEARCH_ON_RESUME";
	private static final String INSTANCE_LAST_SEARCH_TIME = "INSTANCE_LAST_SEARCH_TIME";
	private static final String INSTANCE_ADDRESSES = "INSTANCE_ADDRESSES";
	private static final String INSTANCE_IS_WIDGET_NOTIFICATION_SHOWING = "INSTANCE_IS_WIDGET_NOTIFICATION_SHOWING";
	private static final String INSTANCE_SEARCH_TEXT_SELECTION_START = "INSTANCE_SEARCH_TEXT_SELECTION_START";
	private static final String INSTANCE_SEARCH_TEXT_SELECTION_END = "INSTANCE_SEARCH_TEXT_SELECTION_END";
	private static final String INSTANCE_DISPLAY_TYPE = "INSTANCE_DISPLAY_TYPE";
	private static final String INSTANCE_ACTIVITY_STATE = "INSTANCE_ACTIVITY_STATE";
	private static final String INSTANCE_HAS_SHOWN_CALENDAR = "INSTANCE_HAS_SHOWN_CALENDAR";

	private Bundle saveActivityState() {
		Bundle outState = new Bundle();
		outState.putString(INSTANCE_TAG, mTag);
		outState.putBoolean(INSTANCE_SHOW_DISTANCES, mShowDistance);
		outState.putBoolean(INSTANCE_START_SEARCH_ON_RESUME, mStartSearchOnResume);
		JodaUtils.putDateTime(outState, INSTANCE_LAST_SEARCH_TIME, mLastSearchTime);
		outState.putBoolean(INSTANCE_IS_WIDGET_NOTIFICATION_SHOWING, mIsWidgetNotificationShowing);
		outState.putInt(INSTANCE_SEARCH_TEXT_SELECTION_START, mSearchTextSelectionStart);
		outState.putInt(INSTANCE_SEARCH_TEXT_SELECTION_END, mSearchTextSelectionEnd);
		outState.putParcelableArrayList(INSTANCE_ADDRESSES, mAddresses);

		JSONUtils.putJSONable(outState, INSTANCE_OLD_SEARCH_PARAMS, mOldSearchParams);
		JSONUtils.putJSONable(outState, INSTANCE_EDITED_SEARCH_PARAMS, mEditedSearchParams);
		JSONUtils.putJSONable(outState, INSTANCE_OLD_FILTER, mOldFilter);

		// #9733: You cannot keep displaying a PopupWindow on rotation.  Since it's not essential the popup
		// stay visible, it's easier here just to hide it between activity shifts.
		outState.putInt(INSTANCE_DISPLAY_TYPE, mDisplayType.ordinal());

		outState.putInt(INSTANCE_ACTIVITY_STATE, mActivityState.ordinal());

		outState.putBoolean(INSTANCE_HAS_SHOWN_CALENDAR, mHasShownCalendar);

		return outState;
	}

	private void restoreActivityState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTag = savedInstanceState.getString(INSTANCE_TAG);
			if (mTag == null) {
				// #13543: Bundle.getString() with defaults not supported until API 12, have to
				// write code like this instead.
				mTag = getString(R.string.tag_hotel_list);
			}
			mShowDistance = savedInstanceState.getBoolean(INSTANCE_SHOW_DISTANCES, false);
			mStartSearchOnResume = savedInstanceState.getBoolean(INSTANCE_START_SEARCH_ON_RESUME, false);
			mLastSearchTime = JodaUtils.getDateTime(savedInstanceState, INSTANCE_LAST_SEARCH_TIME);
			mIsWidgetNotificationShowing = savedInstanceState
					.getBoolean(INSTANCE_IS_WIDGET_NOTIFICATION_SHOWING, false);
			mSearchTextSelectionStart = savedInstanceState.getInt(INSTANCE_SEARCH_TEXT_SELECTION_START);
			mSearchTextSelectionEnd = savedInstanceState.getInt(INSTANCE_SEARCH_TEXT_SELECTION_END);
			mAddresses = savedInstanceState.getParcelableArrayList(INSTANCE_ADDRESSES);
			mDisplayType = DisplayType.values()[savedInstanceState.getInt(INSTANCE_DISPLAY_TYPE)];
			mActivityState = ActivityState.values()[savedInstanceState.getInt(INSTANCE_ACTIVITY_STATE)];
			mHasShownCalendar = savedInstanceState.getBoolean(INSTANCE_HAS_SHOWN_CALENDAR);
			mOldSearchParams = JSONUtils
					.getJSONable(savedInstanceState, INSTANCE_OLD_SEARCH_PARAMS, HotelSearchParams.class);
			mEditedSearchParams = JSONUtils.getJSONable(savedInstanceState, INSTANCE_EDITED_SEARCH_PARAMS,
					HotelSearchParams.class);
			mOldFilter = JSONUtils.getJSONable(savedInstanceState, INSTANCE_OLD_FILTER, HotelFilter.class);
		}
	}

	//----------------------------------
	// BROADCAST METHODS
	//----------------------------------

	private void notifySearchLocationFound() {
		if (mHotelMapFragment != null) {
			mHotelMapFragment.notifySearchLocationFound();
		}
	}

	private void broadcastSearchCompleted(HotelSearchResponse searchResponse) {
		if (Db.getHotelSearch().getSearchParams().getSearchType() != HotelSearchParams.SearchType.HOTEL) {
			Db.getHotelSearch().clearSelectedProperty();
		}

		supportInvalidateOptionsMenu();

		if (mHotelListFragment != null) {
			mHotelListFragment.notifySearchComplete();
		}

		if (mHotelMapFragment != null) {
			mHotelMapFragment.notifySearchComplete();
		}

		onSearchResultsChanged();
		mActivityState = ActivityState.NONE;
	}

	private void broadcastSearchStarted() {
		supportInvalidateOptionsMenu();

		if (mHotelListFragment != null) {
			mHotelListFragment.notifySearchStarted();
		}

		if (mHotelMapFragment != null) {
			mHotelMapFragment.notifySearchStarted();
		}
	}

	//----------------------------------
	// Search results handling
	//----------------------------------

	private void simulateErrorResponse(int strId) {
		simulateErrorResponse(getString(strId));
	}

	private void simulateErrorResponse(String text) {
		HotelSearchResponse response = new HotelSearchResponse();
		ServerError error = new ServerError();
		error.setPresentationMessage(text);
		error.setCode("SIMULATED");
		response.addError(error);

		mSearchCallback.onDownload(response);
		mStartSearchOnResume = true;
	}

	public void handleError() {
		// Handling for particular errors
		boolean handledError = false;
		HotelSearchResponse searchResponse = Db.getHotelSearch().getSearchResponse();
		if (searchResponse != null && searchResponse.hasErrors()) {
			ServerError errorOne = searchResponse.getErrors().get(0);
			if (errorOne.getCode().equals("01")) {
				// Deprecated client version
				showDialog(DIALOG_CLIENT_DEPRECATED);

				OmnitureTracking.trackErrorPage(PhoneSearchActivity.this, "OutdatedVersion");

				showLoading(false, errorOne.getExtra("message"));
			}
			else {
				showLoading(false, errorOne.getPresentableMessage(PhoneSearchActivity.this));
			}
			handledError = true;
		}

		if (!handledError) {
			OmnitureTracking.trackErrorPage(PhoneSearchActivity.this, "HotelListRequestFailed");
			showLoading(false, LayoutUtils.noHotelsFoundMessage(mContext));
		}
	}

	//----------------------------------
	// SHOW/HIDE VIEW METHODS
	//----------------------------------

	private void setDisplayType(DisplayType displayType) {
		setDisplayType(displayType, true);
	}

	private void setDisplayType(DisplayType displayType, boolean animate) {
		boolean currentIsSearchDisplay = mDisplayType.isSearchDisplay();
		boolean nextIsSearchDisplay = displayType.isSearchDisplay();
		if (nextIsSearchDisplay) {
			if (mEditedSearchParams == null) {
				mEditedSearchParams = Db.getHotelSearch().getSearchParams().copy();
			}
		}
		else if (currentIsSearchDisplay && !nextIsSearchDisplay) {
			// We are leaving edit search params mode
			mEditedSearchParams = null;
		}

		mDisplayType = displayType;
		if (mActionMode != null) {
			mActionMode.finish();
		}

		switch (mDisplayType) {
		case NONE: {
			// Reset focus
			mFocusLayout.requestFocus();
			mSearchEditText.clearFocus();

			hideFilterOptions();

			mProgressBar.onResume();
			mProgressBar.reset();
			mRefinementDismissView.setVisibility(View.INVISIBLE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);
			break;
		}
		case KEYBOARD: {
			hideFilterOptions();

			// 13550: In some cases, the list has been cleared
			// (like as a result of memory cleanup or rotation). So just
			// populate it here just in case that happens.
			startAutocomplete();

			mRefinementDismissView.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);

			break;
		}
		case CALENDAR: {
			mFocusLayout.requestFocus();
			mSearchEditText.clearFocus();

			hideFilterOptions();

			// make sure to draw/redraw the calendar
			mDatesCalendarDatePicker.markAllCellsDirty();

			mProgressBar.onPause();
			mRefinementDismissView.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.VISIBLE);
			mDatesLayout.setVisibility(View.VISIBLE);
			mGuestsLayout.setVisibility(View.GONE);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mActionMode = startActionMode(mSearchActionMode);
			}

			break;
		}
		case GUEST_PICKER: {
			mFocusLayout.requestFocus();
			mSearchEditText.clearFocus();

			hideFilterOptions();

			mRefinementDismissView.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.VISIBLE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.VISIBLE);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mActionMode = startActionMode(mSearchActionMode);
			}

			break;
		}
		case FILTER: {
			mFocusLayout.requestFocus();
			mSearchEditText.clearFocus();

			mRefinementDismissView.setVisibility(View.INVISIBLE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);

			showFilterOptions();

			break;
		}
		}

		mDatesCalendarDatePicker.setTooltipSuppressed(mDisplayType != DisplayType.CALENDAR);

		if (mDisplayType == DisplayType.KEYBOARD) {
			Ui.showKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
			addSearchTextWatcher();
		}
		else {
			Ui.hideKeyboard(mSearchEditText);
			removeSearchTextWatcher();
		}

		setSearchEditViews();
		displayRefinementInfo();
		setActionBarBookingInfoText();

		if (mDisplayType == DisplayType.CALENDAR && !mHasShownCalendar) {
			//Instead of displaying the default stay, show an empty calendar
			if (Db.getHotelSearch().getSearchParams().isDefaultStay()) {
				mDatesCalendarDatePicker.reset();
			}
			mHasShownCalendar = true;
		}
	}

	private void switchResultsView() {
		setDisplayType(DisplayType.NONE);

		String newFragmentTag = null;

		if (mTag.equals(getString(R.string.tag_hotel_list))) {
			newFragmentTag = getString(R.string.tag_hotel_map);
			OmnitureTracking.trackAppHotelsSearchWithoutRefinements(this, Db.getHotelSearch().getSearchParams(), Db
					.getHotelSearch().getSearchResponse());
		}
		else {
			newFragmentTag = getString(R.string.tag_hotel_list);
			onSwitchToMap();
		}

		showFragment(newFragmentTag);
	}

	private void showFilterOptions() {
		if (mFilterPopupWindow.isShowing()) {
			return;
		}

		mFilterPopupWindow.setOnDismissListener(mFilterPopupOnDismissListener);

		mFilterHotelNameEditText.removeTextChangedListener(mFilterHotelNameTextWatcher);
		mFilterHotelNameEditText.setText(Db.getFilter().getHotelName());
		mFilterHotelNameEditText.addTextChangedListener(mFilterHotelNameTextWatcher);

		switch (Db.getFilter().getSearchRadius()) {
		case SMALL:
			mRadiusCheckedId = R.id.radius_small_button;
			break;
		case MEDIUM:
			mRadiusCheckedId = R.id.radius_medium_button;
			break;
		case LARGE:
			mRadiusCheckedId = R.id.radius_large_button;
			break;
		case ALL:
			mRadiusCheckedId = R.id.radius_all_button;
			break;
		}

		switch (Db.getFilter().getPriceRange()) {
		case CHEAP:
			mPriceCheckedId = R.id.price_cheap_button;
			break;
		case MODERATE:
			mPriceCheckedId = R.id.price_moderate_button;
			break;
		case EXPENSIVE:
			mPriceCheckedId = R.id.price_expensive_button;
			break;
		case ALL:
			mPriceCheckedId = R.id.price_all_button;
			break;
		}

		double minStarRating = Db.getFilter().getMinimumStarRating();
		if (minStarRating >= 5) {
			mRatingCheckedId = R.id.rating_high_button;
		}
		else if (minStarRating >= 4) {
			mRatingCheckedId = R.id.rating_medium_button;
		}
		else if (minStarRating >= 3) {
			mRatingCheckedId = R.id.rating_low_button;
		}
		else {
			mRatingCheckedId = R.id.rating_all_button;
		}

		mFilterVipAccess = Db.getFilter().isVipAccessOnly();

		LayoutUtils.configureRadiusFilterLabels(this, mRadiusButtonGroup, Db.getFilter());

		mRadiusButtonGroup.setOnCheckedChangeListener(null);
		mRatingButtonGroup.setOnCheckedChangeListener(null);
		mPriceButtonGroup.setOnCheckedChangeListener(null);

		mRadiusButtonGroup.check(mRadiusCheckedId);
		mRatingButtonGroup.check(mRatingCheckedId);
		mPriceButtonGroup.check(mPriceCheckedId);
		mVipAccessFilterButton.setSelected(mFilterVipAccess);

		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);

		mRadiusButtonGroup.setVisibility(mShowDistance ? View.VISIBLE : View.GONE);

		mContentViewPager.post(new Runnable() {
			@Override
			public void run() {
				mFilterLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

				int width = mFilterLayout.getMeasuredWidth();
				int height = mFilterLayout.getMeasuredHeight();
				int offsetX = 0;
				int offsetY = 0;

				// Get vertical offset
				Drawable background = mFilterPopupWindow.getBackground();
				if (background != null) {
					Rect padding = new Rect();
					background.getPadding(padding);

					width += padding.left + padding.right;
					height += padding.top + padding.bottom;

					offsetY = -padding.top;
				}

				// Get anchor view and horizontal offset
				View anchor = findViewById(R.id.menu_select_filter);
				if (anchor != null) {
					// Really hacky solution to a bug in <2.3 devices, where the ViewTreeObserver doesn't
					// call onScrollChanged when the screen is resized. Instead we must listen for
					// onPreDraw and manually re-anchor the PopupWindow to the correct view. For this we
					// use a private class that implements the listener, and holds the anchor view and
					// the width and height. We add the listener when the PopupWindow is show, and remove
					// it on dismissal of the PopupWindow.

					anchor = (View) anchor.getParent();
					offsetX = (anchor.getWidth() - width) / 2;

					mViewTreeObserver = anchor.getViewTreeObserver();
					mPopupWindowPreDrawLisetner = new PopupWindowPreDrawListener(anchor, width, height);

					mViewTreeObserver.addOnPreDrawListener(mPopupWindowPreDrawLisetner);
				}
				else {
					anchor = findViewById(R.id.menu_select_change_view);
				}

				mFilterPopupWindow.showAsDropDown(anchor, offsetX, offsetY);
				mFilterPopupWindow.update(width, height);
			}
		});

		onOpenFilterPanel();
	}

	private void hideFilterOptions() {
		if (!mFilterPopupWindow.isShowing()) {
			return;
		}

		mFilterPopupWindow.setOnDismissListener(null);
		mFilterPopupWindow.dismiss();
	}

	//////////////////////////////////////////////////////////////////////////
	// Progress bar tag

	private void hideLoading() {
		if (mContentViewPager.getCurrentItem() == VIEWPAGER_PAGE_MAP) {
			Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
			fadeout.setDuration(ANIMATION_DIMMER_FADE_DURATION);
			fadeout.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationEnd(Animation anim) {
					mProgressBarLayout.setVisibility(View.GONE);
					mProgressBarDimmer.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationStart(Animation anim) {
					//ignore
				}

				@Override
				public void onAnimationRepeat(Animation anim) {
					//ignore
				}
			});
			mProgressBarDimmer.startAnimation(fadeout);
		}
		else {
			mProgressBarLayout.setVisibility(View.GONE);
		}

		// Here, we post it so that we have a few precious frames more of the progress bar before
		// it's covered up by search results (or a lack thereof).  This keeps a black screen from
		// showing up for a split second for reason I'm not entirely sure of.  ~dlew
		mProgressBar.postDelayed(new Runnable() {
			public void run() {
				mProgressBar.setVisibility(View.GONE);
			}
		}, 500);
	}

	private void showLoading(boolean showProgress, int resId) {
		showLoading(showProgress, getString(resId));
	}

	private void showLoading(boolean showProgress, String text) {
		mProgressBarLayout.setVisibility(View.VISIBLE);

		if (mContentViewPager.getCurrentItem() == VIEWPAGER_PAGE_HOTEL) {
			if (!mGLProgressBarStarted) {
				mProgressBarHider.setVisibility(View.VISIBLE);
			}

			// In the case that the user is an emulator and this isn't a release build,
			// disable the hanging tag for speed purposes
			if (AndroidUtils.isEmulator() && !AndroidUtils.isRelease(mContext)) {
				mProgressBar.setVisibility(View.GONE);
			}
			else {
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setShowProgress(showProgress);
			}

			// Dark text on light background
			mProgressText.setTextColor(getResources().getColor(R.color.hotel_list_progress_text_color));
		}
		else {
			// Map
			mProgressBarHider.setVisibility(View.GONE); // In case the hang tag never drew, we don't want to see it on the map fragment

			Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
			fadein.setDuration(ANIMATION_DIMMER_FADE_DURATION);
			mProgressBarDimmer.startAnimation(fadein);
			mProgressBarDimmer.setVisibility(View.VISIBLE);

			// Light text on dark background
			mProgressText.setTextColor(getResources().getColor(R.color.hotel_map_progress_text_color));
		}

		mProgressText.setText(text);
	}

	@Override
	public void onDrawStarted() {
		mGLProgressBarStarted = true;
		mProgressBarHider.postDelayed(new Runnable() {
			public void run() {
				mProgressBarHider.setVisibility(View.GONE);
			}
		}, 50);
	}

	//----------------------------------
	// STORE/RESTORE SEARCH PARAMS
	//----------------------------------

	private HotelSearchParams getCurrentSearchParams() {
		// Determines if we are editing search params and returns those
		if (mEditedSearchParams != null) {
			return mEditedSearchParams;
		}
		else {
			return Db.getHotelSearch().getSearchParams();
		}
	}

	private void commitEditedSearchParams() {
		if (mEditedSearchParams != null) {
			mEditedSearchParams.ensureDatesSet();
			Db.getHotelSearch().setSearchParams(mEditedSearchParams);
			mEditedSearchParams = null;
		}
	}

	//----------------------------------
	// VIEW ATTRIBUTE METHODS
	//----------------------------------

	private void setActionBarBookingInfoText() {
		// If we are currently editing params render those values
		HotelSearchParams params = getCurrentSearchParams();
		LocalDate checkInDate = params.getCheckInDate();
		int startDay = checkInDate != null ? checkInDate.getDayOfMonth() : LocalDate.now().getDayOfMonth();
		int numAdults = params.getNumAdults();
		int numChildren = params.getNumChildren();

		mDatesTextView.setText(String.valueOf(startDay));
		mGuestsTextView.setText(String.valueOf((numAdults + numChildren)));
	}

	private void displayRefinementInfo() {
		CharSequence text;
		if (mDisplayType == DisplayType.CALENDAR) {
			text = CalendarUtils.getCalendarDatePickerTitle(this, getCurrentSearchParams());
		}
		else if (mDisplayType == DisplayType.GUEST_PICKER) {
			HotelSearchParams searchParams = mEditedSearchParams;
			final int numAdults = searchParams.getNumAdults();
			final int numChildren = searchParams.getNumChildren();
			text = StrUtils.formatGuests(this, numAdults, numChildren);

			int orientation = getWindowManager().getDefaultDisplay().getOrientation();
			final int hidden = (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) ? View.GONE
					: View.INVISIBLE;
			mChildAgesLayout.setVisibility(numChildren == 0 ? hidden : View.VISIBLE);
			mSelectChildAgeTextView.setText(getResources().getQuantityString(R.plurals.select_each_childs_age,
					numChildren));

			GuestsPickerUtils.showOrHideChildAgeSpinners(PhoneSearchActivity.this, searchParams.getChildren(),
					mChildAgesLayout, mChildAgeSelectedListener);
		}
		else {
			text = null;
		}

		mRefinementInfoTextView.setText(text);
		if (mActionMode != null) {
			mActionMode.setTitle(text);
		}
	}

	private void setSearchEditViews() {
		HotelSearchParams searchParams = getCurrentSearchParams();
		if (searchParams.getSearchType() == SearchType.VISIBLE_MAP_AREA) {
			stopLocation();
		}

		setSearchText(searchParams.getSearchDisplayText(this));
		if (mSearchTextSelectionStart != -1 && mSearchTextSelectionEnd != -1) {
			mSearchEditText.setSelection(mSearchTextSelectionStart, mSearchTextSelectionEnd);
		}

		// Temporarily remove the OnDateChangedListener so that it is not fired
		// while we manually update the start/end dates
		mDatesCalendarDatePicker.setOnDateChangedListener(null);

		CalendarUtils.updateCalendarPickerStartDate(mDatesCalendarDatePicker, searchParams.getCheckInDate());
		CalendarUtils.updateCalendarPickerEndDate(mDatesCalendarDatePicker, searchParams.getCheckOutDate());

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		mGuestsLayout.post(new Runnable() {
			@Override
			public void run() {
				HotelSearchParams searchParams = getCurrentSearchParams();
				int numAdults = searchParams.getNumAdults();
				int numChildren = searchParams.getNumChildren();
				mAdultsNumberPicker.setMinValue(numAdults);
				mAdultsNumberPicker.setMaxValue(numAdults);
				mChildrenNumberPicker.setMinValue(numChildren);
				mChildrenNumberPicker.setMaxValue(numChildren);
				GuestsPickerUtils.configureAndUpdateDisplayedValues(PhoneSearchActivity.this, mAdultsNumberPicker,
						mChildrenNumberPicker);
			}
		});

		setActionBarBookingInfoText();
	}

	private void setShowDistance(boolean showDistance) {
		mShowDistance = showDistance;

		if (mHotelListFragment != null) {
			mHotelListFragment.setShowDistances(showDistance);
		}

		if (mHotelMapFragment != null) {
			mHotelMapFragment.setShowDistances(showDistance);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// VIEW MODIFIERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// TEXT WATCHERS
	//----------------------------------

	private void setSearchText(String str) {
		if (mIsSearchEditTextTextWatcherEnabled) {
			mSearchEditText.removeTextChangedListener(mSearchEditTextTextWatcher);
		}
		mSearchEditText.setText(str);
		if (mIsSearchEditTextTextWatcherEnabled) {
			mSearchEditText.addTextChangedListener(mSearchEditTextTextWatcher);
		}
	}

	private void addSearchTextWatcher() {
		if (!mIsSearchEditTextTextWatcherEnabled) {
			mSearchEditText.addTextChangedListener(mSearchEditTextTextWatcher);
			mIsSearchEditTextTextWatcherEnabled = true;
		}
	}

	private void removeSearchTextWatcher() {
		if (mIsSearchEditTextTextWatcherEnabled) {
			mSearchEditText.removeTextChangedListener(mSearchEditTextTextWatcher);
			mIsSearchEditTextTextWatcherEnabled = false;
		}
	}

	private final TextWatcher mSearchEditTextTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			mSearchTextSelectionStart = mSearchTextSelectionEnd = -1;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String str = s.toString();
			int len = s.length();
			boolean changed = false;
			HotelSearchParams searchParams = mEditedSearchParams;
			if (searchParams == null || str.equals(searchParams.getQuery())) {
				// HotelSearchParams hasn't changed
			}
			else if (str.equals(getString(R.string.current_location)) || len == 0) {
				changed |= searchParams.setSearchType(SearchType.MY_LOCATION);
			}
			else if (str.equals(getString(R.string.visible_map_area))) {
				changed |= searchParams.setSearchType(SearchType.VISIBLE_MAP_AREA);
				searchParams.setSearchLatLonUpToDate();
			}
			else {
				//TODO: Always changing it to FREEFORM here might not be right,
				// only when the user types something.
				changed |= searchParams.setSearchType(SearchType.FREEFORM);
				changed |= searchParams.setQuery(str);
			}
			if (changed) {
				startAutocomplete();
			}
		}
	};

	private final OnEditorActionListener mFilterEditorActionLisenter = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
				setDisplayType(DisplayType.NONE);
			}
			return false;
		}
	};

	private final TextWatcher mFilterHotelNameTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			HotelFilter filter = Db.getFilter();
			filter.setHotelName(s.toString());
			filter.notifyFilterChanged();
		}
	};

	//----------------------------------
	// AUTOCOMPLETE SEARCH LOADER
	//----------------------------------

	private void startAutocomplete() {
		getSupportLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// We only have one Loader, so we don't care about the ID.
		Uri uri = AutocompleteProvider.generateSearchUri(this, getCurrentSearchParams().getQuery(), 50);
		return new CursorLoader(this, uri, AutocompleteProvider.COLUMNS, null, null, "");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mSearchSuggestionAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mSearchSuggestionAdapter.swapCursor(null);
	}

	//----------------------------------
	// EVENT LISTENERS
	//----------------------------------

	private final AdapterView.OnItemClickListener mSearchSuggestionsItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Cursor c = mSearchSuggestionAdapter.getCursor();
			c.moveToPosition(position);

			if (c.getString(AutocompleteProvider.COLUMN_TEXT_INDEX).equals(getString(R.string.current_location))) {
				getCurrentSearchParams().setSearchType(SearchType.MY_LOCATION);
			}
			else {
				if (mEditedSearchParams == null) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PhoneSearchActivity.this);
					mEditedSearchParams = new HotelSearchParams(prefs);
				}

				Object o = AutocompleteProvider.extractSearchOrString(c);

				if (o instanceof Search) {
					mEditedSearchParams.fillFromSearch((Search) o);
				}
				else {
					mEditedSearchParams.setSearchType(SearchType.FREEFORM);
					mEditedSearchParams.setQuery(o.toString());
				}
			}

			startSearch();
		}
	};

	private final CalendarDatePicker.OnDateChangedListener mDatesDateChangedListener = new CalendarDatePicker.OnDateChangedListener() {
		@Override
		public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
			if (mEditedSearchParams != null) {
				CalendarUtils.syncParamsFromDatePickerHybrid(mEditedSearchParams, mDatesCalendarDatePicker);

				displayRefinementInfo();
				setActionBarBookingInfoText();
				updateCalendarInstructionText();
			}
		}
	};

	private final SimpleNumberPicker.OnValueChangeListener mNumberPickerChangedListener = new SimpleNumberPicker.OnValueChangeListener() {
		@Override
		public void onValueChange(SimpleNumberPicker picker, int oldVal, int newVal) {
			int numAdults = mAdultsNumberPicker.getValue();
			int numChildren = mChildrenNumberPicker.getValue();
			HotelSearchParams searchParams = getCurrentSearchParams();
			searchParams.setNumAdults(numAdults);
			GuestsPickerUtils.resizeChildrenList(PhoneSearchActivity.this, searchParams.getChildren(), numChildren);
			GuestsPickerUtils.configureAndUpdateDisplayedValues(mContext, mAdultsNumberPicker, mChildrenNumberPicker);
			displayRefinementInfo();
			setActionBarBookingInfoText();
			onGuestsChanged();
		}
	};

	private final OnItemSelectedListener mChildAgeSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			List<Integer> children = getCurrentSearchParams().getChildren();
			GuestsPickerUtils.setChildrenFromSpinners(PhoneSearchActivity.this, mChildAgesLayout, children);
			GuestsPickerUtils.updateDefaultChildAges(PhoneSearchActivity.this, children);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	};

	private final RadioGroup.OnCheckedChangeListener mFilterButtonGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (mRadiusButtonGroup != null) {
				mRadiusCheckedId = mRadiusButtonGroup.getCheckedRadioButtonId();
			}
			if (mRatingButtonGroup != null) {
				mRatingCheckedId = mRatingButtonGroup.getCheckedRadioButtonId();
			}
			if (mPriceButtonGroup != null) {
				mPriceCheckedId = mPriceButtonGroup.getCheckedRadioButtonId();
			}
			buildFilter();

			// tracking
			switch (group.getId()) {
			case R.id.radius_filter_button_group: {
				onRadiusFilterChanged();
				break;
			}
			case R.id.rating_filter_button_group: {
				onRatingFilterChanged();
				break;
			}
			case R.id.price_filter_button_group: {
				onPriceFilterChanged();
				break;
			}
			}
		}
	};

	private final View.OnClickListener mVipAccessClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mFilterVipAccess = !mFilterVipAccess;
			mVipAccessFilterButton.setSelected(mFilterVipAccess);
			buildFilter();
		}
	};

	private final TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// Select the first item in the autosuggest list when the user hits the search softkey and then start the search.
			Cursor c = mSearchSuggestionAdapter.getCursor();
			// 1574: It seems that the cursor is null if we are still finding location
			if (c == null) {
				c.moveToPosition(0);
				Object o = AutocompleteProvider.extractSearchOrString(c);
				if (o instanceof Search) {
					mEditedSearchParams.fillFromSearch((Search) o);
				}
				startSearch();
				return true;
			}
			else {
				// Just use what the user typed
				startSearch();
				return true;
			}
		}
	};

	private final View.OnClickListener mDatesButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mDisplayType == DisplayType.CALENDAR) {
				setDisplayType(DisplayType.NONE);
			}
			else {
				setDisplayType(DisplayType.CALENDAR);
			}
		}
	};

	private final View.OnClickListener mGuestsButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mDisplayType == DisplayType.GUEST_PICKER) {
				setDisplayType(DisplayType.NONE);
			}
			else {
				setDisplayType(DisplayType.GUEST_PICKER);
			}
		}
	};

	private final View.OnClickListener mSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startSearch();
		}
	};

	private final View.OnClickListener mRefinementDismissViewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setDisplayType(DisplayType.NONE);
		}
	};

	private final OnDismissListener mFilterPopupOnDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss() {
			if (mViewTreeObserver != null) {
				mViewTreeObserver.removeOnPreDrawListener(mPopupWindowPreDrawLisetner);
			}

			onFilterClosed();
			setDisplayType(DisplayType.NONE);
		}
	};

	private final View.OnClickListener mClearSearchButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setSearchText(null);
		}
	};

	private void showClearSearchButton() {
		mClearSearchButton.setVisibility(View.VISIBLE);
		mClearSearchButton.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

		// Adjust search edit text padding so the clear button doesn't overlap text
		mSearchEditTextPaddingRight = mSearchEditText.getPaddingRight();
		int left = mSearchEditText.getPaddingLeft();
		int top = mSearchEditText.getPaddingTop();
		int right = mSearchEditTextPaddingRight + mClearSearchButton.getMeasuredWidth();
		int bottom = mSearchEditText.getPaddingBottom();
		mSearchEditText.setPadding(left, top, right, bottom);
	}

	private void hideClearSeachButton() {
		mClearSearchButton.setVisibility(View.GONE);

		int left = mSearchEditText.getPaddingLeft();
		int top = mSearchEditText.getPaddingTop();
		int right = mSearchEditTextPaddingRight;
		int bottom = mSearchEditText.getPaddingBottom();
		mSearchEditText.setPadding(left, top, right, bottom);

		mSearchEditTextPaddingRight = -1;
	}

	private final View.OnFocusChangeListener mSearchEditTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				showClearSearchButton();

				setDisplayType(DisplayType.KEYBOARD);
				SearchType searchType = getCurrentSearchParams().getSearchType();
				if (searchType == SearchType.MY_LOCATION || searchType == SearchType.VISIBLE_MAP_AREA) {
					mSearchEditText.post(new Runnable() {
						@Override
						public void run() {
							setSearchText(null);
						}
					});
				}
				else {
					if (AndroidUtils.getSdkVersion() >= 14) {
						// #13062 - in ICS and beyond, EditText's have an entire new action mode for editing.
						// As a result, selectAll() doesn't work (and it drastically changes the UI to properly
						// activate the selection mode).  This is an interim fix that at least starts the cursor
						// at the END of the input rather than the start to make it easier to delete the text.
						mSearchEditText.setSelection(mSearchEditText.length());
					}
					else {
						mSearchEditText.selectAll();
					}
				}
			}
			else {
				hideClearSeachButton();
				Ui.hideKeyboard(v);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// HANDLERS
	//////////////////////////////////////////////////////////////////////////////////////////

	private static final class LeakSafeHandler extends Handler {
		// Intentionally blank, just here for looper
	}

	private Handler mHandler = new LeakSafeHandler();

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CLASSES
	//////////////////////////////////////////////////////////////////////////////////////////

	private class SoftKeyResultReceiver extends ResultReceiver {
		public SoftKeyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (resultCode == InputMethodManager.RESULT_HIDDEN) {
				setDisplayType(DisplayType.NONE);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OMNITURE TRACKING
	//////////////////////////////////////////////////////////////////////////////////////////

	private void onSearchResultsChanged() {
		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		HotelFilter filter = Db.getFilter();
		HotelSearchResponse searchResponse = Db.getHotelSearch().getSearchResponse();

		// Update the last filter/search params we used to track refinements
		mOldSearchParams = searchParams.copy();
		mOldFilter = filter.copy();

		// Start actually tracking the search result change
		OmnitureTracking.trackAppHotelsSearch(this, searchParams, mOldSearchParams, filter, mOldFilter, searchResponse);
		AdTracker.trackHotelSearch();
	}

	private void onOpenFilterPanel() {
		OmnitureTracking.trackSimpleEvent(this, "App.Hotels.Search.Refine", null, null);
	}

	private void onSwitchToMap() {
		OmnitureTracking.trackSimpleEvent(this, "App.Hotels.Search.Map", null, null);
	}

	private void onGuestsChanged() {
		final String pageName = "App.Hotels.Search.Refine.NumberTravelers."
				+ (mAdultsNumberPicker.getValue() + mChildrenNumberPicker.getValue());

		OmnitureTracking.trackSimpleEvent(this, pageName, null, null);
	}

	// HotelFilter tracking

	private void onFilterClosed() {
		OmnitureTracking.trackLinkHotelRefineName(this, mFilterHotelNameEditText.getText().toString());
	}

	private void onPriceFilterChanged() {
		switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
		case R.id.price_cheap_button: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(this, PriceRange.CHEAP);
			break;
		}
		case R.id.price_moderate_button: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(this, PriceRange.MODERATE);
			break;
		}
		case R.id.price_expensive_button: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(this, PriceRange.EXPENSIVE);
			break;
		}
		case R.id.price_all_button:
		default: {
			OmnitureTracking.trackLinkHotelRefinePriceRange(this, PriceRange.ALL);
			break;
		}
		}
	}

	private void onRadiusFilterChanged() {
		switch (mRadiusButtonGroup.getCheckedRadioButtonId()) {
		case R.id.radius_small_button: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(this, SearchRadius.SMALL);
			break;
		}
		case R.id.radius_medium_button: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(this, SearchRadius.MEDIUM);
			break;
		}
		case R.id.radius_large_button: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(this, SearchRadius.LARGE);
			break;
		}
		case R.id.radius_all_button:
		default: {
			OmnitureTracking.trackLinkHotelRefineSearchRadius(this, SearchRadius.ALL);
			break;
		}
		}
	}

	private void onRatingFilterChanged() {
		switch (mRatingButtonGroup.getCheckedRadioButtonId()) {
		case R.id.rating_low_button: {
			OmnitureTracking.trackLinkHotelRefineRating(this, "3Stars");
			break;
		}
		case R.id.rating_medium_button: {
			OmnitureTracking.trackLinkHotelRefineRating(this, "4Stars");
			break;
		}
		case R.id.rating_high_button: {
			OmnitureTracking.trackLinkHotelRefineRating(this, "5Stars");
			break;
		}
		case R.id.rating_all_button:
		default: {
			OmnitureTracking.trackLinkHotelRefineRating(this, "AllStars");
			break;
		}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment tabs

	public void showFragment(String tag) {
		Log.d("Showing fragment with tag: " + tag);

		if (tag == null) {
			// #13543: Bundle.getString() with defaults not supported until API 12, have to
			// write code like this instead.
			tag = getString(R.string.tag_hotel_list);
		}

		if (tag.equals(getString(R.string.tag_hotel_list))) {
			mContentViewPager.setCurrentItem(VIEWPAGER_PAGE_HOTEL);
		}
		else {
			mContentViewPager.setCurrentItem(VIEWPAGER_PAGE_MAP);
		}

		mTag = tag;
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelListFragmentListener

	@Override
	public void onHotelListFragmentAttached(HotelListFragment fragment) {
		mHotelListFragment = fragment;
		mHotelListFragment.setShowDistances(mShowDistance);
	}

	@Override
	public void onSortButtonClicked() {
		// Do nothing
	}

	@Override
	public void onListItemClicked(Property property, int position) {
		Db.getHotelSearch().setSelectedProperty(property);

		Intent intent = new Intent(this, HotelDetailsFragmentActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelMapFragmentListener

	@Override
	public void onHotelMapFragmentAttached(HotelMapFragment fragment) {
		mHotelMapFragment = fragment;
		mHotelMapFragment.setShowDistances(mShowDistance);
		if (Db.getHotelSearch().getSearchResponse() != null) {
			mHotelMapFragment.notifySearchComplete();
		}
	}

	@Override
	public void onPropertyClicked(Property property) {
		if (mHotelMapFragment != null) {
			mHotelMapFragment.focusProperty(property, true);
		}
	}

	@Override
	public void onMapClicked() {
		//ignore
	}

	@Override
	public void onExactLocationClicked() {
		//ignore
	}

	@Override
	public void onPropertyBubbleClicked(Property property) {
		Db.getHotelSearch().setSelectedProperty(property);

		Intent intent = new Intent(this, HotelDetailsFragmentActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener

	@Override
	public void onFilterChanged() {
		supportInvalidateOptionsMenu();
		if (mActivityState != ActivityState.SEARCHING) {
			if (mHotelListFragment != null) {
				mHotelListFragment.notifyFilterChanged();
			}
			if (mHotelMapFragment != null) {
				mHotelMapFragment.notifyFilterChanged();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// View pager adapter

	private static final int VIEWPAGER_PAGE_HOTEL = 0;
	private static final int VIEWPAGER_PAGE_MAP = 1;

	public class ListAndMapViewPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
		private static final int NUM_FRAGMENTS = 2;

		// The page position of the fragments

		public ListAndMapViewPagerAdapter() {
			super(getSupportFragmentManager());
		}

		// FragmentPagerAdapter implementation
		@Override
		public int getCount() {
			return NUM_FRAGMENTS;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment frag;

			switch (position) {
			case VIEWPAGER_PAGE_HOTEL:
				frag = HotelListFragment.newInstance();
				break;
			case VIEWPAGER_PAGE_MAP:
				frag = HotelMapFragment.newInstance();
				break;
			default:
				throw new RuntimeException("Position out of bounds position=" + position);
			}

			return frag;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// ViewPager.OnPageChangeListener interface
		@Override
		public void onPageSelected(int position) {
			supportInvalidateOptionsMenu();
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			//ignore
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			//ignore
		}
	}

	private ListAndMapViewPagerAdapter mListAndMapViewPagerAdapter;

	// Number picker formatters
	private final SimpleNumberPicker.Formatter mAdultsNumberPickerFormatter = new SimpleNumberPicker.Formatter() {
		@Override
		public String format(int value) {
			return mContext.getResources().getQuantityString(R.plurals.number_of_adults, value, value);
		}
	};

	private final SimpleNumberPicker.Formatter mChildrenNumberPickerFormatter = new SimpleNumberPicker.Formatter() {
		@Override
		public String format(int value) {
			return mContext.getResources().getQuantityString(R.plurals.number_of_children, value, value);
		}
	};

	private class PopupWindowPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
		public View mAnchor;
		public int mWidth;
		public int mHeight;

		public PopupWindowPreDrawListener(View anchor, int width, int height) {
			mAnchor = anchor;
			mWidth = width;
			mHeight = height;
		}

		@Override
		public boolean onPreDraw() {
			mFilterPopupWindow.update(mAnchor, mWidth, mHeight);
			return true;
		}
	}
}
