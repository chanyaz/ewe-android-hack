package com.expedia.bookings.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentMapActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp.OnSearchParamsChangedInWidgetListener;
import com.expedia.bookings.animation.Rotate3dAnimation;
import com.expedia.bookings.content.AutocompleteProvider;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.ConfirmationState.Type;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Filter.PriceRange;
import com.expedia.bookings.data.Filter.SearchRadius;
import com.expedia.bookings.data.Filter.Sort;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.fragment.HotelListFragment;
import com.expedia.bookings.fragment.HotelListFragment.HotelListFragmentListener;
import com.expedia.bookings.fragment.HotelMapFragment;
import com.expedia.bookings.fragment.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.model.WidgetConfigurationState;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.SearchUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SearchSuggestionAdapter;
import com.expedia.bookings.widget.SimpleNumberPicker;
import com.expedia.bookings.widget.gl.GLTagProgressBar;
import com.expedia.bookings.widget.gl.GLTagProgressBarRenderer.OnDrawStartedListener;
import com.google.android.maps.GeoPoint;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class PhoneSearchActivity extends SherlockFragmentMapActivity implements LocationListener,
		OnDrawStartedListener, HotelListFragmentListener, HotelMapFragmentListener, OnFilterChangedListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	//////////////////////////////////////////////////////////////////////////////////////////
	// ENUMS
	//////////////////////////////////////////////////////////////////////////////////////////

	private enum DisplayType {
		NONE(false), KEYBOARD(true), CALENDAR(true), GUEST_PICKER(true), FILTER(true);

		private boolean mIsSearchDisplay;

		private DisplayType(boolean isSearchDisplay) {
			mIsSearchDisplay = isSearchDisplay;
		}

		public boolean isSearchDisplay() {
			return mIsSearchDisplay;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////////

	private static final String KEY_GEOCODE = "KEY_GEOCODE";
	public static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_LOADING_PREVIOUS = "KEY_LOADING_PREVIOUS";

	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;
	private static final int DIALOG_CLIENT_DEPRECATED = 1;
	private static final int DIALOG_ENABLE_LOCATIONS = 2;

	public static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago

	private static final boolean ANIMATION_VIEW_FLIP_ENABLED = true;
	private static final long ANIMATION_VIEW_FLIP_SPEED = 350;
	private static final float ANIMATION_VIEW_FLIP_DEPTH = 300f;

	private static final long ANIMATION_PANEL_DISMISS_SPEED = 150;

	// the offset is to ensure that the list loads before the animation
	// is played to make it flow smoother and also to grab the user's attention.
	private static final long WIDGET_NOTIFICATION_BAR_ANIMATION_DELAY = 2000L;
	private static final long WIDGET_NOTIFICATION_BAR_ANIMATION_DURATION = 1000L;

	private static final int DEFAULT_RADIUS_RADIO_GROUP_CHILD = R.id.radius_large_button;
	private static final int DEFAULT_PRICE_RADIO_GROUP_CHILD = R.id.price_all_button;

	// Used in onNewIntent(), if the calling Activity wants the SearchActivity to start fresh
	private static final String EXTRA_NEW_SEARCH = "EXTRA_NEW_SEARCH";

	public static final long SEARCH_EXPIRATION = 1000 * 60 * 60; // 1 hour
	private static final String SEARCH_RESULTS_VERSION_FILE = "savedsearch-version.dat";
	private static final String SEARCH_RESULTS_FILE = "savedsearch.dat";

	private static final int F_NO_DIVIDERS = 1;
	private static final int F_FIRST = 2;
	private static final int F_LAST = 4;

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEWS
	//----------------------------------

	private ViewTreeObserver mViewTreeObserver;

	private CalendarDatePicker mDatesCalendarDatePicker;
	private AutoCompleteTextView mSearchEditText;
	private ImageView mClearSearchButton;
	private FrameLayout mContent;
	private ImageButton mDatesButton;
	private ImageButton mGuestsButton;
	private ImageButton mViewButton;
	private ImageView mViewFlipImage;
	private SimpleNumberPicker mAdultsNumberPicker;
	private SimpleNumberPicker mChildrenNumberPicker;
	private EditText mFilterHotelNameEditText;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mRatingButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;
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
	private View mFilterFocusLayout;
	private PopupWindow mFilterPopupWindow;
	private PopupWindowPreDrawListener mPopupWindowPreDrawLisetner;

	private View mActionBarCustomView;

	// Progress bar stuff
	private ViewGroup mProgressBarLayout;
	private View mProgressBarHider;
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
	private boolean mShowDistance = true;

	private Bitmap mViewFlipBitmap;
	private Canvas mViewFlipCanvas;

	private int mSortOptionSelectedId;
	private int mRadiusCheckedId = 0;
	private int mRatingCheckedId = 0;
	private int mPriceCheckedId = 0;

	private ArrayList<Address> mAddresses;
	private SearchParams mOldSearchParams;
	private SearchParams mEditedSearchParams;
	private Filter mOldFilter;
	public boolean mStartSearchOnResume;
	private long mLastSearchTime = -1;
	private boolean mIsWidgetNotificationShowing;
	private boolean mWasOnDestroyCalled = false;

	private SearchSuggestionAdapter mSearchSuggestionAdapter;

	private boolean mIsActivityResumed = false;
	private boolean mIsOptionsMenuCreated = false;
	private boolean mIsSearchEditTextTextWatcherEnabled = false;

	// This indicates to mSearchCallback that we just loaded saved search results,
	// and as such should behave a bit differently than if we just did a new search.
	private boolean mLoadedSavedResults;

	// The last selection for the search EditText.  Used to maintain between rotations
	private int mSearchTextSelectionStart = -1;
	private int mSearchTextSelectionEnd = -1;

	private int mSearchEditTextPaddingRight = -1;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private final Download<SearchResponse> mSearchDownload = new Download<SearchResponse>() {
		@Override
		public SearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(PhoneSearchActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			if (mEditedSearchParams != null) {
				throw new RuntimeException("edited search params not commited or cleared before search");
			}
			return services.search(Db.getSearchParams(), 0);
		}
	};

	private final OnDownloadComplete<SearchResponse> mSearchCallback = new OnDownloadComplete<SearchResponse>() {
		@Override
		public void onDownload(SearchResponse searchResponse) {
			// Clear the old listener so we don't end up with a memory leak
			Db.getFilter().clearOnFilterChangedListeners();

			Db.setSearchResponse(searchResponse);

			if (searchResponse != null && searchResponse.getPropertiesCount() > 0 && !searchResponse.hasErrors()) {
				incrementNumSearches();

				Filter filter = Db.getFilter();
				searchResponse.setFilter(filter);
				filter.addOnFilterChangedListener(PhoneSearchActivity.this);

				SearchParams searchParams = Db.getSearchParams();
				searchResponse.setSearchType(searchParams.getSearchType());
				searchResponse.setSearchLatLon(searchParams.getSearchLatitude(), searchParams.getSearchLongitude());

				if (!mLoadedSavedResults && searchResponse.getFilteredAndSortedProperties().length <= 10) {
					Log.i("Initial search results had not many results, expanding search radius filter to show all.");
					filter.setSearchRadius(SearchRadius.ALL);
					mRadiusCheckedId = R.id.radius_all_button;
					searchResponse.clearCache();
				}
				ImageCache.recycleCache(true);
				broadcastSearchCompleted(searchResponse);

				hideLoading();

				// #9773: Show distance sort initially, if user entered street address-level search params
				if (mShowDistance) {
					mSortOptionSelectedId = R.id.menu_select_sort_distance;
					buildFilter();
				}

				mLastSearchTime = Calendar.getInstance().getTimeInMillis();
			}
			else if (searchResponse != null && searchResponse.getPropertiesCount() > 0
					&& searchResponse.getLocations() != null && searchResponse.getLocations().size() > 0) {
				showDialog(DIALOG_LOCATION_SUGGESTIONS);
			}
			else if (searchResponse != null && searchResponse.getPropertiesCount() == 0 && !searchResponse.hasErrors()) {
				simulateErrorResponse(LayoutUtils.noHotelsFoundMessage(mContext));
				handleError();
			}
			else {
				handleError();
			}
		}
	};

	private final Download<SearchResponse> mLoadSavedResults = new Download<SearchResponse>() {
		@Override
		public SearchResponse doDownload() {
			SearchResponse response = null;
			File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);
			if (savedSearchResults.exists()) {
				if (savedSearchResults.lastModified() + SEARCH_EXPIRATION < Calendar.getInstance().getTimeInMillis()) {
					Log.d("There are saved search results, but they expired.  Starting a new search instead.");
				}
				else {
					try {
						long start = System.currentTimeMillis();
						JSONObject obj = new JSONObject(IoUtils.readStringFromFile(SEARCH_RESULTS_FILE, mContext));
						response = new SearchResponse(obj);
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

	private final OnDownloadComplete<SearchResponse> mLoadSavedResultsCallback = new OnDownloadComplete<SearchResponse>() {
		@Override
		public void onDownload(SearchResponse results) {
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
		public void onSearchParamsChanged(SearchParams searchParams) {
			Db.setSearchParams(searchParams);
			if (searchParams != null) {
				searchParams.ensureValidCheckInDate();
			}
			else {
				Db.resetSearchParams();
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

		setContentView(R.layout.activity_search);

		mHotelListFragment = Ui.findSupportFragment(this, getString(R.string.tag_hotel_list));
		mHotelMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_hotel_map));

		initializeViews();

		mSearchSuggestionAdapter = new SearchSuggestionAdapter(this);
		mSearchEditText.setAdapter(mSearchSuggestionAdapter);

		boolean localeChanged = SettingUtils.get(this, LocaleChangeReceiver.KEY_LOCALE_CHANGED, false);
		boolean startNewSearch = getIntent().getBooleanExtra(EXTRA_NEW_SEARCH, false);

		if (startNewSearch) {
			Db.clear();
			saveParams();
			// Remove it so we don't keep doing this on rotation
			getIntent().removeExtra(EXTRA_NEW_SEARCH);
		}

		boolean toBroadcastSearchCompleted = false;
		SearchResponse searchResponse = Db.getSearchResponse();
		if (getIntent().hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
			//If this is a search coming from flights, we expect the Db.searchParams to already be valid
			mSearchEditText.setText(Db.getSearchParams().getUserQuery());
			Log.i("searchEditText...:" + mSearchEditText.getText().toString());
			Db.resetFilter();
			mTag = mHotelListFragment.getTag();
			mShowDistance = false;
			startSearch();
			getIntent().removeExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS);
		}
		else if (!localeChanged) {
			restoreActivityState(savedInstanceState);

			if (searchResponse != null) {
				if (searchResponse.hasErrors()) {
					handleError();
				}
				else {
					searchResponse.setFilter(Db.getFilter());
					toBroadcastSearchCompleted = true;
				}
			}
		}
		else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Db.setSearchParams(new SearchParams(prefs));
			String filterJson = prefs.getString("filter", null);
			mTag = prefs.getString("tag", mHotelListFragment.getTag());
			mShowDistance = prefs.getBoolean("showDistance", true);

			if (filterJson != null) {
				try {
					JSONObject obj = new JSONObject(filterJson);
					Db.setFilter(new Filter(obj));
				}
				catch (JSONException e) {
					Log.e("Failed to load saved filter.");
				}
			}
			else {
				Db.resetFilter();
			}

			boolean versionGood = false;
			if (AndroidUtils.getAppCodeFromFilePath(SEARCH_RESULTS_VERSION_FILE, mContext) >= AndroidUtils.APP_CODE_E3) {
				versionGood = true;
			}

			// Attempt to load saved search results; if we fail, start a new search
			if (!localeChanged && versionGood && !startNewSearch) {
				BackgroundDownloader.getInstance().startDownload(KEY_LOADING_PREVIOUS, mLoadSavedResults,
						mLoadSavedResultsCallback);
				broadcastSearchStarted();
				showLoading(true, R.string.loading_previous);
			}
			else {
				startSearch();
			}
		}

		// Setup custom action bar view
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setCustomView(mActionBarCustomView);

		SearchParams searchParams = getCurrentSearchParams();

		mAdultsNumberPicker.setFormatter(mAdultsNumberPickerFormatter);
		mAdultsNumberPicker.setMinValue(1);
		mAdultsNumberPicker.setMaxValue(GuestsPickerUtils.getMaxPerType());
		mAdultsNumberPicker.setValue(searchParams.getNumAdults());

		mChildrenNumberPicker.setFormatter(mChildrenNumberPickerFormatter);
		mChildrenNumberPicker.setMinValue(0);
		mChildrenNumberPicker.setMaxValue(GuestsPickerUtils.getMaxPerType());
		mChildrenNumberPicker.setValue(searchParams.getNumChildren());

		showFragment(mTag);
		setShowDistance(mShowDistance);

		// 9028:t only broadcast search completed once all 
		// elements have been setup
		if (toBroadcastSearchCompleted) {
			broadcastSearchCompleted(searchResponse);
		}

		if (localeChanged) {
			// Mark that we've read the change
			SettingUtils.save(this, LocaleChangeReceiver.KEY_LOCALE_CHANGED, false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsActivityResumed = false;

		mProgressBar.onPause();
		stopLocationListener();

		Db.getFilter().removeOnFilterChangedListener(this);

		if (!isFinishing()) {
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			downloader.unregisterDownloadCallback(KEY_GEOCODE);
			downloader.unregisterDownloadCallback(KEY_LOADING_PREVIOUS);
			downloader.unregisterDownloadCallback(KEY_SEARCH);
		}
		else {
			saveParams();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Haxxy fix for #13798, only required on pre-Honeycomb
		if (ConfirmationState.hasSavedData(this, Type.HOTEL)) {
			finish();
			return;
		}

		((ExpediaBookingApp) getApplicationContext())
				.registerSearchParamsChangedInWidgetListener(mSearchParamsChangedListener);

		Db.getFilter().addOnFilterChangedListener(this);

		if (mDisplayType != DisplayType.CALENDAR) {
			mProgressBar.onResume();
			mProgressBar.reset();
		}

		CalendarUtils.configureCalendarDatePicker(mDatesCalendarDatePicker, CalendarDatePicker.SelectionMode.RANGE);

		// setDisplayType here because it could possibly add a TextWatcher before the view has restored causing the listener to fire
		setDisplayType(mDisplayType, false);

		setSearchEditViews();
		GuestsPickerUtils.configureAndUpdateDisplayedValues(this, mAdultsNumberPicker, mChildrenNumberPicker);

		displayRefinementInfo();
		setActionBarBookingInfoText();

		if (mStartSearchOnResume) {
			startSearch();
			mStartSearchOnResume = false;
		}
		else if (mLastSearchTime != -1
				&& mLastSearchTime + SEARCH_EXPIRATION < Calendar.getInstance().getTimeInMillis()) {
			Log.d("onResume(): There are cached search results, but they expired.  Starting a new search instead.");
			Db.getSearchParams().ensureValidCheckInDate();
			startSearch();
		}
		else if (Db.getSearchParams().getSearchType() != null
				&& Db.getSearchParams().getSearchType() == SearchType.MY_LOCATION
				&& !Db.getSearchParams().hasSearchLatLon()) {
			Log.d("onResume(): We were attempting to search by current location, but do not yet have valid coordinates. Starting a new search (and getting new coords if needed).");
			startSearch();
		}
		else {
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			if (downloader.isDownloading(KEY_LOADING_PREVIOUS)) {
				Log.d("Already loading previous search results, resuming the load...");
				downloader.registerDownloadCallback(KEY_LOADING_PREVIOUS, mLoadSavedResultsCallback);
				showLoading(true, R.string.loading_previous);
			}
			else if (downloader.isDownloading(KEY_GEOCODE)) {
				Log.d("Already geocoding, resuming the search...");
				downloader.registerDownloadCallback(KEY_GEOCODE, mGeocodeCallback);
				showLoading(true, R.string.progress_searching_hotels);
			}
			else if (downloader.isDownloading(KEY_SEARCH)) {
				Log.d("Already searching, resuming the search...");
				downloader.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
				showLoading(true, R.string.progress_searching_hotels);
			}
		}

		mIsActivityResumed = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}

		mWasOnDestroyCalled = true;

		// #9018: There was an insidious memory leak happening on rotation.  There might be a number of 
		// ListView rows that were trying to load images.  The callbacks for these loads involved an ImageView
		// which (in turn) held onto the Activity.  On rotation, these callbacks would be retained until the
		// image loaded.
		//
		// Now we clear all callbacks (since no one else should be loading images via ImageCache at this time)
		// upon rotation.  Any images that were loading will continue in the background, but they will not
		// load onto an image until explicitly requested.
		ImageCache.clearAllCallbacks();

		((ExpediaBookingApp) getApplicationContext())
				.unregisterSearchParamsChangedInWidgetListener(mSearchParamsChangedListener);

		if (mActionMode != null) {
			mActionMode.finish();
		}

		// do not attempt to save parameters if the user was short circuited to the
		// confirmation screen when the search activity started
		if (isFinishing() && !ConfirmationState.hasSavedData(this, Type.HOTEL)) {
			saveParams();

			File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);

			SearchResponse searchResponse = Db.getSearchResponse();

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
					SearchParams searchParams = getCurrentSearchParams();
					SearchType searchType = SearchUtils.isExactLocation(address) ? SearchType.ADDRESS : SearchType.CITY;

					// The user found a better version of the search they ran,
					// so we'll replace it from startSearchDownloader
					Search.delete(PhoneSearchActivity.this, searchParams);

					searchParams.setQuery(formattedAddress);
					setSearchEditViews();
					searchParams.setSearchLatLon(address.getLatitude(), address.getLongitude());
					searchParams.setSearchType(searchType);

					setShowDistance(searchType == SearchType.ADDRESS);
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearchDownloader();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
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
			final ServerError error = Db.getSearchResponse().getErrors().get(0);
			builder.setMessage(error.getExtra("message"));
			builder.setPositiveButton(R.string.upgrade, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SocialUtils.openSite(PhoneSearchActivity.this, error.getExtra("url"));
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
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
			builder.setNegativeButton(android.R.string.cancel, null);
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

		boolean shouldEnableMenuItems = Db.getSearchResponse() != null;
		menu.findItem(R.id.menu_select_sort).setEnabled(shouldEnableMenuItems);
		menu.findItem(R.id.menu_select_filter).setEnabled(shouldEnableMenuItems);
		menu.findItem(R.id.menu_select_search_map).setEnabled(shouldEnableMenuItems);
		menu.findItem(R.id.menu_select_change_view).setEnabled(shouldEnableMenuItems);

		// Disable distance sort
		menu.findItem(R.id.menu_select_sort_distance).setVisible(mShowDistance);

		// Configure the map/list view action
		if (mTag == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			mTag = prefs.getString("tag", mHotelListFragment.getTag());
		}
		boolean isListShowing = mTag.equals(mHotelListFragment.getTag());
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
		case R.id.menu_select_sort_deals:
		case R.id.menu_select_sort_price:
		case R.id.menu_select_sort_user_rating:
		case R.id.menu_select_sort_distance:
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
			SearchParams searchParams = getCurrentSearchParams();
			searchParams.clearQuery();

			if (mHotelMapFragment != null) {
				GeoPoint center = mHotelMapFragment.getCenter();
				searchParams.setSearchType(SearchType.VISIBLE_MAP_AREA);

				double lat = MapUtils.getLatitude(center);
				double lng = MapUtils.getLongitude(center);
				searchParams.setSearchLatLon(lat, lng);
				setShowDistance(true);
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
		}

		if (rebuildFilter) {
			buildFilter();
			invalidateOptionsMenu = true;
		}

		if (invalidateOptionsMenu) {
			supportInvalidateOptionsMenu();
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
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			mode.getMenuInflater().inflate(R.menu.action_mode_search, menu);
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
	// LOCATION LISTENER IMPLEMENTATION
	//----------------------------------

	@Override
	public void onLocationChanged(Location location) {
		Log.d("onLocationChanged(): " + location.toString());

		onLocationFound(location);

		stopLocationListener();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.w("onProviderDisabled(): " + provider);

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean stillWorking = true;

		// If the NETWORK provider is disabled, switch to GPS (if available)
		if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				lm.removeUpdates(this);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			}
			else {
				stillWorking = false;
			}
		}
		// If the GPS provider is disabled and we were using it, send error
		else if (provider.equals(LocationManager.GPS_PROVIDER)
				&& !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			stillWorking = false;
		}

		if (!stillWorking) {
			lm.removeUpdates(this);
			simulateErrorResponse(R.string.ProviderDisabled);
			TrackingUtils.trackErrorPage(this, "LocationServicesNotAvailable");
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("onProviderDisabled(): " + provider);

		// Switch to network if it's now available (because it's much faster)
		if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(this);
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.w("onStatusChanged(): provider=" + provider + " status=" + status);

		if (status == LocationProvider.OUT_OF_SERVICE) {
			stopLocationListener();
			Log.w("Location listener failed: out of service");
			simulateErrorResponse(R.string.ProviderOutOfService);
		}
		else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			stopLocationListener();
			Log.w("Location listener failed: temporarily unavailable");
			simulateErrorResponse(R.string.ProviderTemporarilyUnavailable);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	//public SearchParams getSearchParams() {
	//	return Db.getSearchParams();
	//}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEW INITIALIZATION
	//----------------------------------

	private void initializeViews() {
		// Get views
		mFocusLayout = findViewById(R.id.focus_layout);

		mContent = (FrameLayout) findViewById(R.id.content_layout);
		mViewFlipImage = (ImageView) findViewById(R.id.view_flip_image);

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

		CalendarUtils.configureCalendarDatePicker(mDatesCalendarDatePicker, CalendarDatePicker.SelectionMode.RANGE);

		mFilterLayout = getLayoutInflater().inflate(R.layout.popup_filter_options, null);
		mFilterFocusLayout = mFilterLayout.findViewById(R.id.filter_focus_layout);
		mFilterHotelNameEditText = (EditText) mFilterLayout.findViewById(R.id.filter_hotel_name_edit_text);
		mRadiusButtonGroup = (SegmentedControlGroup) mFilterLayout.findViewById(R.id.radius_filter_button_group);
		mRatingButtonGroup = (SegmentedControlGroup) mFilterLayout.findViewById(R.id.rating_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) mFilterLayout.findViewById(R.id.price_filter_button_group);

		mFilterHotelNameEditText.setOnEditorActionListener(mEditorActionLisenter);

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

		Filter filter = Db.getFilter();
		Filter currentFilter = filter.copy();

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

		/*
		 * Don't notify listeners of the filter having changed when the activity is either not 
		 * completely setup or paused. This is because we don't want the filter changes to propogate
		 * when the radio buttons are being setup as it causes wasted cycles notifying all listeners
		 */
		if (currentFilter == null || !filter.equals(currentFilter) && mIsActivityResumed) {
			Log.d("Filter has changed, notifying listeners.");
			filter.notifyFilterChanged();
		}
	}

	private void saveParams() {
		Log.d("Saving search parameters, filter and tag...");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Db.getSearchParams().saveToSharedPreferences(prefs);
		Editor editor = prefs.edit();
		editor.putString("filter", Db.getFilter().toJson().toString());
		editor.putString("tag", mTag);
		editor.putBoolean("showDistance", mShowDistance);
		SettingUtils.commitOrApply(editor);
	}

	private void resetFilter() {
		Log.d("Resetting filter...");

		Db.resetFilter();
	}

	private void startSearch() {
		Log.i("Starting a new search...");

		Db.setSearchResponse(null);
		Db.clearAvailabilityResponses();

		broadcastSearchStarted();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_GEOCODE);
		bd.cancelDownload(KEY_SEARCH);

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

		SearchType searchType = Db.getSearchParams().getSearchType();
		switch (searchType) {
		case CITY:
		case ADDRESS:
		case POI:
		case FREEFORM:
			setShowDistance(searchType != SearchType.CITY);
			stopLocationListener();
			startGeocode();
			break;

		case VISIBLE_MAP_AREA:
			stopLocationListener();
			startSearchDownloader();
			break;

		case MY_LOCATION:
			// See if we have a good enough location stored
			long minTime = Calendar.getInstance().getTimeInMillis() - MINIMUM_TIME_AGO;
			Location location = LocationServices.getLastBestLocation(this, minTime);
			if (location != null) {
				onLocationChanged(location);
			}
			else {
				startLocationListener();
			}

			break;
		}
	}

	private void startGeocode() {
		showLoading(true, R.string.progress_searching_hotels);

		SearchParams searchParams = Db.getSearchParams();

		if (searchParams.hasEnoughToSearch()) {
			Log.d("User already has region id or lat/lng for freeform location, skipping geocoding.");
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
			return LocationServices.geocodeGoogle(mContext, Db.getSearchParams().getQuery());
		}
	};

	private final OnDownloadComplete<List<Address>> mGeocodeCallback = new OnDownloadComplete<List<Address>>() {
		public void onDownload(List<Address> results) {
			if (results == null || results.size() == 0) {
				TrackingUtils.trackErrorPage(PhoneSearchActivity.this, "LocationNotFound");
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
					SearchParams searchParams = Db.getSearchParams();
					SearchType searchType = SearchUtils.isExactLocation(address) ? SearchType.ADDRESS : SearchType.CITY;

					// The user found a better version of the search they ran,
					// so we'll replace it from startSearchDownloader
					Search.delete(PhoneSearchActivity.this, searchParams);

					searchParams.setQuery(formattedAddress);
					setSearchEditViews();
					searchParams.setSearchLatLon(address.getLatitude(), address.getLongitude());
					searchParams.setSearchType(searchType);

					setShowDistance(searchType == SearchType.ADDRESS);
					startSearchDownloader();
				}
			}
		}
	};

	private void onLocationFound(Location location) {
		Db.getSearchParams().setSearchLatLon(location.getLatitude(), location.getLongitude());
		setShowDistance(true);
		startSearchDownloader();
	}

	private void startSearchDownloader() {
		showLoading(true, R.string.progress_searching_hotels);

		commitEditedSearchParams();

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

		SearchType type = Db.getSearchParams().getSearchType();
		if (type != SearchType.MY_LOCATION && type != SearchType.VISIBLE_MAP_AREA) {
			Search.add(this, Db.getSearchParams());
		}

		resetFilter();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_SEARCH);
		bd.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
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

	private Bundle saveActivityState() {
		Bundle outState = new Bundle();
		outState.putString(INSTANCE_TAG, mTag);
		outState.putBoolean(INSTANCE_SHOW_DISTANCES, mShowDistance);
		outState.putBoolean(INSTANCE_START_SEARCH_ON_RESUME, mStartSearchOnResume);
		outState.putLong(INSTANCE_LAST_SEARCH_TIME, mLastSearchTime);
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
			mLastSearchTime = savedInstanceState.getLong(INSTANCE_LAST_SEARCH_TIME);
			mIsWidgetNotificationShowing = savedInstanceState
					.getBoolean(INSTANCE_IS_WIDGET_NOTIFICATION_SHOWING, false);
			mSearchTextSelectionStart = savedInstanceState.getInt(INSTANCE_SEARCH_TEXT_SELECTION_START);
			mSearchTextSelectionEnd = savedInstanceState.getInt(INSTANCE_SEARCH_TEXT_SELECTION_END);
			mAddresses = savedInstanceState.getParcelableArrayList(INSTANCE_ADDRESSES);
			mDisplayType = DisplayType.values()[savedInstanceState.getInt(INSTANCE_DISPLAY_TYPE)];

			mOldSearchParams = JSONUtils
					.getJSONable(savedInstanceState, INSTANCE_OLD_SEARCH_PARAMS, SearchParams.class);
			mEditedSearchParams = JSONUtils.getJSONable(savedInstanceState, INSTANCE_EDITED_SEARCH_PARAMS,
					SearchParams.class);
			mOldFilter = JSONUtils.getJSONable(savedInstanceState, INSTANCE_OLD_FILTER, Filter.class);
		}
	}

	//----------------------------------
	// BROADCAST METHODS
	//----------------------------------

	private void broadcastSearchCompleted(SearchResponse searchResponse) {
		Db.setSearchResponse(searchResponse);
		Db.clearSelectedProperty();

		supportInvalidateOptionsMenu();

		// Inform fragments
		mHotelListFragment.notifySearchComplete();
		mHotelMapFragment.notifySearchComplete();

		onSearchResultsChanged();
	}

	private void broadcastSearchStarted() {
		supportInvalidateOptionsMenu();
		mHotelListFragment.notifySearchStarted();
		mHotelMapFragment.notifySearchStarted();
	}

	//--------------------------------------------
	// Widget notification related private methods
	//--------------------------------------------

	private static final String NUM_SEARCHES = "NUM_SEARCHES";
	private static final String APP_VERSION = "APP_VERSION";
	private static final String WIDGET_NOTIFICATION_SHOWN = "WIDGET_NOTIFICATION_SHOWN";
	private static final int THRESHOLD_LAUNCHES = 2;

	private boolean shouldShowWidgetNotification() {

		// wait for 2 launches before deciding to show the widget
		if (getNumSearches() > THRESHOLD_LAUNCHES) {
			return !wasWidgetNotificationShown() && !areWidgetsInstalled();
		}

		return false;
	}

	private void markWidgetNotificationAsShown() {
		SettingUtils.save(this, WIDGET_NOTIFICATION_SHOWN, true);
	}

	private void incrementNumSearches() {
		// reset bookkeeping if the app was upgraded
		// so that the widget can be shown again
		if (wasAppUpgraded()) {
			SettingUtils.save(this, NUM_SEARCHES, 1);
			SettingUtils.save(this, WIDGET_NOTIFICATION_SHOWN, false);
		}
		else {
			int numLaunches = SettingUtils.get(this, NUM_SEARCHES, 0);
			SettingUtils.save(this, NUM_SEARCHES, ++numLaunches);
		}
	}

	private int getNumSearches() {
		int numLaunches = SettingUtils.get(this, NUM_SEARCHES, 0);
		return numLaunches;
	}

	private boolean wasAppUpgraded() {
		String currentVersionNumber = AndroidUtils.getAppVersion(this);
		String savedVersionNumber = SettingUtils.get(this, APP_VERSION, null);
		SettingUtils.save(this, APP_VERSION, currentVersionNumber);
		return !currentVersionNumber.equals(savedVersionNumber);
	}

	private boolean wasWidgetNotificationShown() {
		return SettingUtils.get(this, WIDGET_NOTIFICATION_SHOWN, false);
	}

	private boolean areWidgetsInstalled() {
		List<WidgetConfigurationState> widgetConfigs = WidgetConfigurationState.getAll();
		return !widgetConfigs.isEmpty();
	}

	//----------------------------------
	// Search results handling
	//----------------------------------

	private void simulateErrorResponse(int strId) {
		simulateErrorResponse(getString(strId));
	}

	private void simulateErrorResponse(String text) {
		SearchResponse response = new SearchResponse();
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
		SearchResponse searchResponse = Db.getSearchResponse();
		if (searchResponse != null && searchResponse.hasErrors()) {
			ServerError errorOne = searchResponse.getErrors().get(0);
			if (errorOne.getCode().equals("01")) {
				// Deprecated client version
				showDialog(DIALOG_CLIENT_DEPRECATED);

				TrackingUtils.trackErrorPage(PhoneSearchActivity.this, "OutdatedVersion");

				showLoading(false, errorOne.getExtra("message"));
			}
			else {
				showLoading(false, errorOne.getPresentableMessage(PhoneSearchActivity.this));
			}
			handledError = true;
		}

		if (!handledError) {
			TrackingUtils.trackErrorPage(PhoneSearchActivity.this, "HotelListRequestFailed");
			showLoading(false, LayoutUtils.noHotelsFoundMessage(mContext));
		}
	}

	//----------------------------------
	// SHOW/HIDE SOFT KEYBOARD METHODS
	//----------------------------------

	void hideSoftKeyboard(TextView v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	private void showSoftKeyboard(View view, ResultReceiver resultReceiver) {
		Configuration config = getResources().getConfiguration();
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, resultReceiver);
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
				mEditedSearchParams = Db.getSearchParams().copy();
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

		if (mDisplayType == DisplayType.KEYBOARD) {
			showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
			addSearchTextWatcher();
		}
		else {
			hideSoftKeyboard(mSearchEditText);
			removeSearchTextWatcher();
		}

		setSearchEditViews();
		displayRefinementInfo();
		setActionBarBookingInfoText();
	}

	private void switchResultsView() {
		setDisplayType(DisplayType.NONE);

		String newFragmentTag = null;
		Rotate3dAnimation animationOut = null;
		Rotate3dAnimation animationIn = null;

		final float centerX = mViewFlipImage.getWidth() / 2.0f;
		final float centerY = mViewFlipImage.getHeight() / 2.0f;

		if (mTag.equals(mHotelMapFragment.getTag())) {
			newFragmentTag = mHotelListFragment.getTag();

			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, 90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(-90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}

			Tracker.trackAppHotelsSearch(this, Db.getSearchParams(), Db.getSearchResponse(), null);
		}
		else {
			newFragmentTag = mHotelMapFragment.getTag();

			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, -90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}

			onSwitchToMap();
		}

		if (animationOut != null && animationIn != null) {
			final Rotate3dAnimation nextAnimation = animationIn;

			if (mViewFlipCanvas == null) {
				final int width = mContent.getWidth();
				final int height = mContent.getHeight();
				mViewFlipBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				mViewFlipCanvas = new Canvas(mViewFlipBitmap);
				mViewFlipImage.setImageBitmap(mViewFlipBitmap);
			}

			mContent.draw(mViewFlipCanvas);
			mContent.setVisibility(View.INVISIBLE);
			showFragment(newFragmentTag);

			nextAnimation.setDuration(ANIMATION_VIEW_FLIP_SPEED);
			nextAnimation.setFillAfter(true);
			nextAnimation.setInterpolator(new DecelerateInterpolator());
			nextAnimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mContent.setVisibility(View.VISIBLE);
				}
			});

			animationOut.setDuration(ANIMATION_VIEW_FLIP_SPEED);
			animationOut.setFillAfter(true);
			animationOut.setInterpolator(new AccelerateInterpolator());
			animationOut.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mContent.draw(mViewFlipCanvas);
							mViewFlipImage.startAnimation(nextAnimation);
						}
					});
				}
			});

			mViewFlipImage.clearAnimation();
			mViewFlipImage.startAnimation(animationOut);
		}
		else {
			if (newFragmentTag != null) {
				showFragment(newFragmentTag);
			}
		}
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

		LayoutUtils.configureRadiusFilterLabels(this, mRadiusButtonGroup, Db.getFilter());

		mRadiusButtonGroup.setOnCheckedChangeListener(null);
		mRatingButtonGroup.setOnCheckedChangeListener(null);
		mPriceButtonGroup.setOnCheckedChangeListener(null);

		mRadiusButtonGroup.check(mRadiusCheckedId);
		mRatingButtonGroup.check(mRatingCheckedId);
		mPriceButtonGroup.check(mPriceCheckedId);

		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);

		mRadiusButtonGroup.setVisibility(mShowDistance ? View.VISIBLE : View.GONE);

		mContent.post(new Runnable() {
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
		mProgressBarLayout.setVisibility(View.GONE);

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

		// In the case that the user is an emulator and this isn't a release build,
		// disable the hanging tag for speed purposes
		if (AndroidUtils.isEmulator() && !AndroidUtils.isRelease(mContext)) {
			mProgressBar.setVisibility(View.GONE);
		}
		else {
			mProgressBar.setVisibility(View.VISIBLE);
			mProgressBar.setShowProgress(showProgress);
		}

		mProgressText.setText(text);
	}

	@Override
	public void onDrawStarted() {
		mProgressBarHider.postDelayed(new Runnable() {
			public void run() {
				mProgressBarHider.setVisibility(View.GONE);
			}
		}, 50);
	}

	//----------------------------------
	// STORE/RESTORE SEARCH PARAMS
	//----------------------------------

	private SearchParams getCurrentSearchParams() {
		// Determines if we are editing search params and returns those
		if (mEditedSearchParams != null) {
			return mEditedSearchParams;
		}
		else {
			return Db.getSearchParams();
		}
	}

	private void commitEditedSearchParams() {
		if (mEditedSearchParams != null) {
			Db.setSearchParams(mEditedSearchParams);
			mEditedSearchParams = null;
		}
	}

	//----------------------------------
	// VIEW ATTRIBUTE METHODS
	//----------------------------------

	private void setActionBarBookingInfoText() {
		// If we are currently editing params render those values
		SearchParams params = getCurrentSearchParams();
		int startDay = params.getCheckInDate().get(Calendar.DAY_OF_MONTH);
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
			SearchParams searchParams = mEditedSearchParams;
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
		SearchParams searchParams = getCurrentSearchParams();
		if (searchParams.getSearchType() == SearchType.VISIBLE_MAP_AREA) {
			stopLocationListener();
		}

		setSearchText(searchParams.getSearchDisplayText(this));
		if (mSearchTextSelectionStart != -1 && mSearchTextSelectionEnd != -1) {
			mSearchEditText.setSelection(mSearchTextSelectionStart, mSearchTextSelectionEnd);
		}

		// Temporarily remove the OnDateChangedListener so that it is not fired
		// while we manually update the start/end dates
		mDatesCalendarDatePicker.setOnDateChangedListener(null);

		Calendar checkIn = searchParams.getCheckInDate();
		mDatesCalendarDatePicker.updateStartDate(checkIn.get(Calendar.YEAR), checkIn.get(Calendar.MONTH),
				checkIn.get(Calendar.DAY_OF_MONTH));

		Calendar checkOut = searchParams.getCheckOutDate();
		mDatesCalendarDatePicker.updateEndDate(checkOut.get(Calendar.YEAR), checkOut.get(Calendar.MONTH),
				checkOut.get(Calendar.DAY_OF_MONTH));

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		mGuestsLayout.post(new Runnable() {
			@Override
			public void run() {
				SearchParams searchParams = getCurrentSearchParams();
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

		int visibility = mShowDistance ? View.VISIBLE : View.GONE;

		mHotelListFragment.setShowDistances(showDistance);
		mHotelMapFragment.setShowDistances(showDistance);
	}

	//----------------------------------
	// LOCATION METHODS
	//----------------------------------

	private void startLocationListener() {
		showLoading(true, R.string.progress_finding_location);

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

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
			showLoading(false, R.string.ProviderDisabled);
			showDialog(DIALOG_ENABLE_LOCATIONS);
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
			SearchParams searchParams = mEditedSearchParams;
			if (str.equals(searchParams.getQuery())) {
				// SearchParams hasn't changed
			}
			else if (str.equals(getString(R.string.current_location)) || len == 0) {
				changed |= searchParams.setSearchType(SearchType.MY_LOCATION);
			}
			else if (str.equals(getString(R.string.visible_map_area))) {
				changed |= searchParams.setSearchType(SearchType.VISIBLE_MAP_AREA);
				searchParams.setSearchLatLonUpToDate();
			}
			else if (searchParams.getSearchType() != SearchType.FREEFORM) {
				// Got here if the user clicked a search suggestion
			}
			else {
				changed |= searchParams.setQuery(str);
			}
			if (changed) {
				startAutocomplete();
			}
		}
	};

	private final OnEditorActionListener mEditorActionLisenter = new OnEditorActionListener() {
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
			Filter filter = Db.getFilter();
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
		Uri uri = AutocompleteProvider.generateSearchUri(getCurrentSearchParams().getQuery(), 50);
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
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PhoneSearchActivity.this);
				mEditedSearchParams = new SearchParams(prefs);

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
			syncDatesFromPicker();

			displayRefinementInfo();
			setActionBarBookingInfoText();
		}
	};

	private void syncDatesFromPicker() {
		Calendar startCalendar = Calendar.getInstance(CalendarUtils.getFormatTimeZone());
		Calendar endCalendar = Calendar.getInstance(CalendarUtils.getFormatTimeZone());

		final int startYear = mDatesCalendarDatePicker.getStartYear();
		final int startMonth = mDatesCalendarDatePicker.getStartMonth();
		final int startDay = mDatesCalendarDatePicker.getStartDayOfMonth();

		final int endYear = mDatesCalendarDatePicker.getEndYear();
		final int endMonth = mDatesCalendarDatePicker.getEndMonth();
		final int endDay = mDatesCalendarDatePicker.getEndDayOfMonth();

		startCalendar.set(startYear, startMonth, startDay, 0, 0, 0);
		endCalendar.set(endYear, endMonth, endDay, 0, 0, 0);

		startCalendar.set(Calendar.MILLISECOND, 0);
		endCalendar.set(Calendar.MILLISECOND, 0);

		SearchParams searchParams = mEditedSearchParams;
		searchParams.setCheckInDate(startCalendar);
		searchParams.setCheckOutDate(endCalendar);
	}

	private final SimpleNumberPicker.OnValueChangeListener mNumberPickerChangedListener = new SimpleNumberPicker.OnValueChangeListener() {
		@Override
		public void onValueChange(SimpleNumberPicker picker, int oldVal, int newVal) {
			int numAdults = mAdultsNumberPicker.getValue();
			int numChildren = mChildrenNumberPicker.getValue();
			SearchParams searchParams = getCurrentSearchParams();
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

	private final TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			startSearch();
			return true;
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

			//// Get rid of IME if it appeared for the filter
			//InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			//imm.hideSoftInputFromWindow(mFilterHotelNameEditText.getWindowToken(), 0);
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
		{
			mSearchEditTextPaddingRight = mSearchEditText.getPaddingRight();
			int left = mSearchEditText.getPaddingLeft();
			int top = mSearchEditText.getPaddingTop();
			int right = mSearchEditTextPaddingRight + mClearSearchButton.getMeasuredWidth();
			int bottom = mSearchEditText.getPaddingBottom();
			mSearchEditText.setPadding(left, top, right, bottom);
		}
	}

	private void hideClearSeachButton() {
		mClearSearchButton.setVisibility(View.GONE);
		{
			int left = mSearchEditText.getPaddingLeft();
			int top = mSearchEditText.getPaddingTop();
			int right = mSearchEditTextPaddingRight;
			int bottom = mSearchEditText.getPaddingBottom();
			mSearchEditText.setPadding(left, top, right, bottom);
		}
		mSearchEditTextPaddingRight = -1;
	}

	private final View.OnFocusChangeListener mSearchEditTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				showClearSearchButton();

				//expandSearchEditText();
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

				//collapseSearchEditText();
				TextView tv = (TextView) v;
				hideSoftKeyboard(tv);
			}
		}
	};

	private AnimatorUpdateListener mUpDownListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animator) {
			int val = (Integer) animator.getAnimatedValue();
			((RelativeLayout.LayoutParams) mDatesButton.getLayoutParams()).bottomMargin = val;
			((RelativeLayout.LayoutParams) mGuestsButton.getLayoutParams()).bottomMargin = val;
			mDatesButton.requestLayout();
			mGuestsButton.requestLayout();
		}
	};

	private AnimatorUpdateListener mGrowShrinkListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animator) {
			int val = (Integer) animator.getAnimatedValue();
			((RelativeLayout.LayoutParams) mDatesButton.getLayoutParams()).width = val;
			((RelativeLayout.LayoutParams) mGuestsButton.getLayoutParams()).width = val;
			mDatesButton.requestLayout();
			mGuestsButton.requestLayout();
		}
	};

	private void expandSearchEditText() {
		ValueAnimator animUp = ValueAnimator.ofInt(0, mSearchEditText.getMeasuredHeight());
		animUp.addUpdateListener(mUpDownListener);

		ValueAnimator animShrink = ValueAnimator.ofInt(
				getResources().getDimensionPixelSize(R.dimen.actionbar_refinement_width), 0);
		animShrink.addUpdateListener(mGrowShrinkListener);

		AnimatorSet set = new AnimatorSet();
		set.setStartDelay(1000);
		set.playSequentially(animUp, animShrink);
		set.start();
	}

	private void collapseSearchEditText() {
		ValueAnimator animDown = ValueAnimator.ofInt(mSearchEditText.getMeasuredHeight(), 0);
		animDown.addUpdateListener(mUpDownListener);

		ValueAnimator animGrow = ValueAnimator.ofInt(0,
				getResources().getDimensionPixelSize(R.dimen.actionbar_refinement_width));
		animGrow.addUpdateListener(mGrowShrinkListener);

		AnimatorSet set = new AnimatorSet();
		set.playSequentially(animGrow, animDown);
		set.start();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HANDLERS
	//////////////////////////////////////////////////////////////////////////////////////////

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

		}
	};

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
		SearchParams searchParams = Db.getSearchParams();
		Filter filter = Db.getFilter();
		SearchResponse searchResponse = Db.getSearchResponse();

		// If we already have results, check for refinements; if there were none, it's possible
		// that the user just opened/closed a search param change without changing anything.
		// 
		// This is a somewhat lazy way of doing things, but it is easiest and catches a bunch
		// of refinements at once instead of flooding the system with a ton of different refinements
		String refinements = TrackingUtils.getRefinements(searchParams, mOldSearchParams, filter, mOldFilter);

		// Update the last filter/search params we used to track refinements 
		mOldSearchParams = searchParams.copy();
		mOldFilter = filter.copy();

		// Start actually tracking the search result change
		Tracker.trackAppHotelsSearch(this, searchParams, searchResponse, refinements);
	}

	private void onOpenFilterPanel() {
		Log.d("Tracking \"App.Hotels.Search.Refine\" onClick...");
		TrackingUtils.trackSimpleEvent(this, "App.Hotels.Search.Refine", null, "Shopper", null);
	}

	private void onSwitchToMap() {
		Log.d("Tracking \"App.Hotels.Search.Map\" pageLoad...");
		TrackingUtils.trackSimpleEvent(this, "App.Hotels.Search.Map", null, "Shopper", null);
	}

	private void onGuestsChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.NumberTravelers\" change");

		final String pageName = "App.Hotels.Search.Refine.NumberTravelers."
				+ (mAdultsNumberPicker.getValue() + mChildrenNumberPicker.getValue());

		TrackingUtils.trackSimpleEvent(this, pageName, null, "Shopper", null);
	}

	// Filter tracking

	private void onFilterClosed() {
		Log.d("Tracking \"App.Hotels.Search.Refine.Name\" change...");
		OmnitureTracking.trackLinkHotelRefineName(this, mFilterHotelNameEditText.getText().toString());
	}

	private void onPriceFilterChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.PriceRange\" change...");

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
		Log.d("Tracking \"App.Hotels.Search.Refine.SearchRadius\" rating change...");

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
		Log.d("Tracking \"App.Hotels.Search.Refine\" rating change...");

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

		if (tag.equals(mHotelMapFragment.getTag())) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mHotelMapFragment);
			ft.hide(mHotelListFragment);
			ft.commit();

			mTag = mHotelMapFragment.getTag();
		}
		else {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mHotelListFragment);
			ft.hide(mHotelMapFragment);
			ft.commit();

			mTag = mHotelListFragment.getTag();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelListFragmentListener

	@Override
	public void onSortButtonClicked() {
		// Do nothing
	}

	@Override
	public void onListItemClicked(Property property, int position) {
		Db.setSelectedProperty(property);

		Intent intent = new Intent(this, HotelDetailsFragmentActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// HotelMapFragmentListener

	@Override
	public void onBalloonShown(Property property) {
		// Do nothing
	}

	@Override
	public void onBalloonClicked(Property property) {
		Db.setSelectedProperty(property);

		Intent intent = new Intent(this, HotelDetailsFragmentActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener

	@Override
	public void onFilterChanged() {
		supportInvalidateOptionsMenu();
		mHotelListFragment.notifyFilterChanged();
		mHotelMapFragment.notifyFilterChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// FragmentMapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

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
