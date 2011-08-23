package com.expedia.bookings.activity;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.Rotate3dAnimation;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.widget.SearchSuggestionAdapter;
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
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.SelectionMode;
import com.mobiata.android.widget.NumberPicker;
import com.mobiata.android.widget.Panel;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.PriceRange;
import com.mobiata.hotellib.data.Filter.Rating;
import com.mobiata.hotellib.data.Filter.SearchRadius;
import com.mobiata.hotellib.data.Filter.Sort;
import com.mobiata.hotellib.data.PriceTier;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.ServerError;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.CalendarUtils;
import com.mobiata.hotellib.utils.StrUtils;
import com.omniture.AppMeasurement;

public class SearchActivity extends ActivityGroup implements LocationListener, OnDrawStartedListener {
	//////////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////////

	private static final int SORT_POPUP_ANIMATION_SPEED = 200;

	public interface MapViewListener {
		public GeoPoint onRequestMapCenter();
	}

	public interface SetShowDistanceListener {
		public void onSetShowDistance(boolean showDistance);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// ENUMS
	//////////////////////////////////////////////////////////////////////////////////////////

	private enum DisplayType {
		NONE(false),
		KEYBOARD(true),
		CALENDAR(true),
		GUEST_PICKER(true),
		DRAWER(false),
		SORT_POPUP(false);

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

	private static final HashMap<Sort, Integer> SORT_DESCRIPTIONS = new HashMap<Sort, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put(Sort.DISTANCE, R.string.sort_description_distance);
			put(Sort.POPULAR, R.string.sort_description_popular);
			put(Sort.PRICE, R.string.sort_description_price);
			put(Sort.RATING, R.string.sort_description_rating);
		}
	};

	private static final String ACTIVITY_SEARCH_LIST = SearchListActivity.class.getCanonicalName();
	private static final String ACTIVITY_SEARCH_MAP = SearchMapActivity.class.getCanonicalName();

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_LOADING_PREVIOUS = "KEY_LOADING_PREVIOUS";

	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;
	private static final int DIALOG_CLIENT_DEPRECATED = 1;
	private static final int DIALOG_ENABLE_LOCATIONS = 2;

	private static final int REQUEST_CODE_SETTINGS = 1;

	public static final long MINIMUM_TIME_AGO = 1000 * 60 * 15; // 15 minutes ago

	private static final boolean ANIMATION_VIEW_FLIP_ENABLED = true;
	private static final long ANIMATION_VIEW_FLIP_SPEED = 350;
	private static final float ANIMATION_VIEW_FLIP_DEPTH = 300f;

	private static final long ANIMATION_PANEL_DISMISS_SPEED = 150;

	private static final int MAX_GUESTS_TOTAL = 5;
	private static final int MAX_GUEST_NUM = 4;

	private static final int DEFAULT_RADIUS_RADIO_GROUP_CHILD = R.id.radius_large_button;
	private static final int DEFAULT_PRICE_RADIO_GROUP_CHILD = R.id.price_all_button;

	public static final long SEARCH_EXPIRATION = 1000 * 60 * 60; // 1 hour
	private static final String SEARCH_RESULTS_FILE = "savedsearch.dat";

	// Used in onNewIntent(), if the calling Activity wants the SearchActivity to start fresh
	public static final String EXTRA_NEW_SEARCH = "EXTRA_NEW_SEARCH";

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEWS
	//----------------------------------

	private View mSearchButton;
	private Button mTripAdvisorOnlyButton;
	private CalendarDatePicker mDatesCalendarDatePicker;
	private EditText mFilterHotelNameEditText;
	private EditText mSearchEditText;
	private FrameLayout mContent;
	private ImageButton mDatesButton;
	private ImageButton mGuestsButton;
	private ImageButton mViewButton;
	private ImageView mViewFlipImage;
	private ListView mSearchSuggestionsListView;
	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;
	private Panel mPanel;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mRatingButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;
	private TextView mDatesTextView;
	private TextView mFilterInfoTextView;
	private TextView mGuestsTextView;
	private TextView mRefinementInfoTextView;
	private TextView mSortTypeTextView;
	private View mButtonBarLayout;
	private View mDatesLayout;
	private View mFocusLayout;
	private View mGuestsLayout;
	private View mPanelDismissView;
	private View mRefinementDismissView;
	private View mBottomBarLayout;
	private View mSortButton;
	private View mFilterButton;
	private View mUpArrowFilterHotels;
	private View mUpArrowSortHotels;
	private ViewGroup mSortOptionsLayout;

	private View mSortPriceButton;
	private View mSortDistanceButton;
	private View mSortUserRatingButton;
	private View mSortPopularityButton;

	// Progress bar stuff
	private ViewGroup mProgressBarLayout;
	private View mProgressBarHider;
	private GLTagProgressBar mProgressBar;
	private TextView mProgressText;

	//----------------------------------
	// OTHERS
	//----------------------------------

	private Context mContext;

	private LocalActivityManager mLocalActivityManager;
	private String mTag = ACTIVITY_SEARCH_LIST;
	private Intent mIntent;
	private View mLaunchedView;

	private DisplayType mDisplayType = DisplayType.NONE;
	private boolean mShowDistance = true;

	private Bitmap mViewFlipBitmap;
	private Canvas mViewFlipCanvas;

	private Bitmap mSortOptionDividerBitmap;
	private BitmapDrawable mSortOptionDivider;
	private int mSortOptionSelectedId;

	private List<SearchListener> mSearchListeners;
	private MapViewListener mMapViewListener;
	private List<SetShowDistanceListener> mSetShowDistanceListeners;

	private List<Address> mAddresses;
	private SearchParams mSearchParams;
	private SearchParams mOldSearchParams;
	private SearchParams mOriginalSearchParams;
	private Session mSession;
	private SearchResponse mSearchResponse;
	private Map<PriceRange, PriceTier> mPriceTierCache;
	private Filter mFilter;
	private Filter mOldFilter;
	public boolean mStartSearchOnResume;
	private long mLastSearchTime = -1;

	private Thread mGeocodeThread;
	private SearchSuggestionAdapter mSearchSuggestionAdapter;

	private boolean mIsActivityResumed = false;

	// This indicates to mSearchCallback that we just loaded saved search results,
	// and as such should behave a bit differently than if we just did a new search.
	private boolean mLoadedSavedResults;

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(SearchActivity.this, mSession);
			mSearchDownloader.addDownloadListener(KEY_SEARCH, services);
			return services.search(mSearchParams, 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			// Clear the old listener so we don't end up with a memory leak
			mFilter.clearOnFilterChangedListeners();
			mSearchResponse = (SearchResponse) results;

			if (mSearchResponse != null && !mSearchResponse.hasErrors()) {
				mSearchResponse.setFilter(mFilter);
				mSearchResponse.setSearchType(mSearchParams.getSearchType());
				mSearchResponse.setSearchLatLon(mSearchParams.getSearchLatitude(), mSearchParams.getSearchLongitude());
				mSession = mSearchResponse.getSession();

				if (!mLoadedSavedResults && mSearchResponse.getFilteredAndSortedProperties().length <= 10) {
					Log.i("Initial search results had not many results, expanding search radius filter to show all.");
					mFilter.setSearchRadius(SearchRadius.ALL);
					mRadiusButtonGroup.check(R.id.radius_all_button);
					mSearchResponse.clearCache();
				}

				ImageCache.recycleCache(true);
				broadcastSearchCompleted(mSearchResponse);

				buildPriceTierCache();
				hideLoading();
				setFilterInfoText();

				mLastSearchTime = Calendar.getInstance().getTimeInMillis();
				enablePanelHandle();
			}
			else if (mSearchResponse != null && mSearchResponse.getLocations() != null
					&& mSearchResponse.getLocations().size() > 0) {
				showDialog(DIALOG_LOCATION_SUGGESTIONS);
			}
			else {
				handleError();
			}
		}
	};

	private Download mLoadSavedResults = new Download() {
		@Override
		public Object doDownload() {
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

	private OnDownloadComplete mLoadSavedResultsCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
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

	//////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// LIFECYCLE EVENTS
	//----------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #7090: If the user was just sent from the ConfirmationActivity, quit (if desired)
		if (getIntent().getBooleanExtra(ConfirmationActivity.EXTRA_FINISH, false)) {
			finish();
			return;
		}

		// #7090: First, check to see if the user last confirmed a booking.  If that is the case,
		//        then we should forward the user to the ConfirmationActivity
		if (ConfirmationActivity.hasSavedConfirmationData(this)) {
			Intent intent = new Intent(this, ConfirmationActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		mContext = this;

		onPageLoad();
		setContentView(R.layout.activity_search);

		initializeViews();

		mSortOptionDividerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sort_option_border);
		mSortOptionDivider = new BitmapDrawable(mSortOptionDividerBitmap);
		mSortOptionDivider.setTileModeX(Shader.TileMode.REPEAT);

		mSortPopularityButton = addSortOption(R.id.sort_popular_button, R.drawable.ic_sort_popular,
				R.string.sort_description_popular, true);
		mSortPriceButton = addSortOption(R.id.sort_price_button, R.drawable.ic_sort_price,
				R.string.sort_description_price, false);
		mSortUserRatingButton = addSortOption(R.id.sort_reviews_button, R.drawable.ic_sort_user_rating,
				R.string.sort_description_rating, false);
		mSortDistanceButton = addSortOption(R.id.sort_distance_button, R.drawable.ic_sort_distance,
				R.string.sort_description_distance, false);

		mSortPriceButton.setOnClickListener(mSortOptionChangedListener);
		mSortPopularityButton.setOnClickListener(mSortOptionChangedListener);
		mSortDistanceButton.setOnClickListener(mSortOptionChangedListener);
		mSortUserRatingButton.setOnClickListener(mSortOptionChangedListener);

		mLocalActivityManager = getLocalActivityManager();
		setActivity(SearchMapActivity.class);
		setActivity(SearchListActivity.class);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		boolean toBroadcastSearchCompleted = false;
		if (state != null) {
			extractActivityState(state);

			if (mSearchResponse != null) {
				if (mSearchResponse.hasErrors()) {
					handleError();
				}
				else {
					if (mFilter != null) {
						mSearchResponse.setFilter(mFilter);
					}
					toBroadcastSearchCompleted = true;
				}
			}
		}
		else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String searchParamsJson = prefs.getString("searchParams", null);
			String filterJson = prefs.getString("filter", null);
			mTag = prefs.getString("tag", mTag);
			mShowDistance = prefs.getBoolean("showDistance", true);

			if (searchParamsJson != null) {
				try {
					JSONObject obj = new JSONObject(searchParamsJson);
					setSearchParams(new SearchParams(obj));
				}
				catch (JSONException e) {
					Log.e("Failed to load saved search params.");
				}
			}
			else {
				mSearchParams = new SearchParams();
				mSearchParams.setNumAdults(1);
			}

			if (filterJson != null) {
				try {
					JSONObject obj = new JSONObject(filterJson);
					mFilter = new Filter(obj);
				}
				catch (JSONException e) {
					Log.e("Failed to load saved filter.");
				}
			}
			else {
				mFilter = new Filter();
			}

			// Attempt to load saved search results; if we fail, start a new search
			BackgroundDownloader.getInstance().startDownload(KEY_LOADING_PREVIOUS, mLoadSavedResults,
					mLoadSavedResultsCallback);
			showLoading(true, R.string.loading_previous);
		}

		mAdultsNumberPicker.setTextEnabled(false);
		mChildrenNumberPicker.setTextEnabled(false);
		mAdultsNumberPicker.setRange(1, 4);
		mChildrenNumberPicker.setRange(0, 4);
		mAdultsNumberPicker.setCurrent(mSearchParams.getNumAdults());
		mChildrenNumberPicker.setCurrent(mSearchParams.getNumChildren());
		setNumberPickerRanges();

		mSearchSuggestionAdapter = new SearchSuggestionAdapter(this);
		mSearchSuggestionsListView.setAdapter(mSearchSuggestionAdapter);

		setActivityByTag(mTag);
		setShowDistance(mShowDistance);
		setDisplayType(mDisplayType, false);

		// 9028:t only broadcast search completed once all 
		// elements have been setup
		if (toBroadcastSearchCompleted) {
			broadcastSearchCompleted(mSearchResponse);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		hideSortOptions();
		hideFilterOptions();

		if (intent.getBooleanExtra(EXTRA_NEW_SEARCH, false)) {
			mStartSearchOnResume = true;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsActivityResumed = false;

		mProgressBar.onPause();
		stopLocationListener();

		if (!isFinishing()) {
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
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

		mProgressBar.onResume();

		Time now = new Time();
		now.setToNow();
		mDatesCalendarDatePicker.setMinDate(now.year, now.month, now.monthDay);

		setViewButtonImage();
		setDrawerViews();
		setSearchEditViews();
		setBottomBarOptions();

		if (mStartSearchOnResume) {
			startSearch();
			mStartSearchOnResume = false;
		}
		else if (mLastSearchTime != -1
				&& mLastSearchTime + SEARCH_EXPIRATION < Calendar.getInstance().getTimeInMillis()) {
			Log.d("onResume(): There are cached search results, but they expired.  Starting a new search instead.");
			mSearchParams.ensureValidCheckInDate();
			startSearch();
		}
		else {
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			if (downloader.isDownloading(KEY_LOADING_PREVIOUS)) {
				Log.d("Already loading previous search results, resuming the load...");
				downloader.registerDownloadCallback(KEY_LOADING_PREVIOUS, mLoadSavedResultsCallback);
				showLoading(true, R.string.loading_previous);
			}
			else if (downloader.isDownloading(KEY_SEARCH)) {
				Log.d("Already searching, resuming the search...");
				downloader.registerDownloadCallback(KEY_SEARCH, mSearchCallback);
				showLoading(true, R.string.progress_searching_hotels);
			}
		}

		// Set max calendar date
		Time maxTime = new Time(System.currentTimeMillis());
		maxTime.monthDay += 330;
		maxTime.normalize(true);

		mDatesCalendarDatePicker.setMaxDate(maxTime.year, maxTime.month, maxTime.monthDay);
		mIsActivityResumed = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// do not attempt to save parameters if the user was short circuited to the
		// confirmation screen when the search activity started
		if (isFinishing() && !ConfirmationActivity.hasSavedConfirmationData(this)) {
			saveParams();

			File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);

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
			else if (mSearchResponse != null && !mSearchResponse.hasErrors() && !savedSearchResults.exists()) {
				try {
					long start = System.currentTimeMillis();
					IoUtils.writeStringToFile(SEARCH_RESULTS_FILE, mSearchResponse.toJson().toString(0), this);
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
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = buildActivityState();
		mLocalActivityManager.removeAllActivities();

		return state;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
			// This indicates that settings have changed; we should start a new search,
			// as the currency (or possibly other settings in the future) have changed.
			startSearch();
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
					mSearchParams.setFreeformLocation(formattedAddress);
					setSearchEditViews();

					setSearchParams(address.getLatitude(), address.getLongitude());
					setShowDistance(address.getSubThoroughfare() != null);

					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearchDownloader();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					simulateErrorResponse(getString(R.string.NoGeocodingResults, mSearchParams.getFreeformLocation()));
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					simulateErrorResponse(getString(R.string.NoGeocodingResults, mSearchParams.getFreeformLocation()));
				}
			});
			return builder.create();
		}
		case DIALOG_CLIENT_DEPRECATED: {
			AlertDialog.Builder builder = new Builder(this);
			final ServerError error = mSearchResponse.getErrors().get(0);
			builder.setMessage(error.getExtra("message"));
			builder.setPositiveButton(R.string.upgrade, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SocialUtils.openSite(SearchActivity.this, error.getExtra("url"));
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
					Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
					startActivity(intent);
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
		getMenuInflater().inflate(R.menu.menu_search, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings: {
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SETTINGS);
			break;
		}
		case R.id.about: {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			break;
		}
		}

		return super.onOptionsItemSelected(item);
	}

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

		setSearchParams(location.getLatitude(), location.getLongitude());
		startSearchDownloader();

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

	public void addSearchListener(SearchListener searchListener) {
		if (mSearchListeners == null) {
			mSearchListeners = new ArrayList<SearchListener>();
		}

		if (!mSearchListeners.contains(searchListener)) {
			mSearchListeners.add(searchListener);
		}
	}

	public void addSetShowDistanceListener(SetShowDistanceListener setShowDistanceListener) {
		if (mSetShowDistanceListeners == null) {
			mSetShowDistanceListeners = new ArrayList<SetShowDistanceListener>();
		}

		if (!mSetShowDistanceListeners.contains(setShowDistanceListener)) {
			mSetShowDistanceListeners.add(setShowDistanceListener);
		}
	}

	public SearchParams getSearchParams() {
		return mSearchParams;
	}

	public Session getSession() {
		return mSession;
	}

	public void setMapViewListener(MapViewListener mapViewListener) {
		mMapViewListener = mapViewListener;
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

		mContent = (FrameLayout) findViewById(R.id.content_layout);
		mViewFlipImage = (ImageView) findViewById(R.id.view_flip_image);

		mViewButton = (ImageButton) findViewById(R.id.view_button);

		mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
		mDatesButton = (ImageButton) findViewById(R.id.dates_button);
		mDatesTextView = (TextView) findViewById(R.id.dates_text_view);
		mGuestsButton = (ImageButton) findViewById(R.id.guests_button);
		mGuestsTextView = (TextView) findViewById(R.id.guests_text_view);

		mPanelDismissView = findViewById(R.id.panel_dismiss_view);
		mPanel = (Panel) findViewById(R.id.drawer_panel);

		mFilterInfoTextView = (TextView) findViewById(R.id.filter_info_text_view);
		mTripAdvisorOnlyButton = (Button) findViewById(R.id.tripadvisor_only_button);
		mFilterHotelNameEditText = (EditText) findViewById(R.id.filter_hotel_name_edit_text);
		mSortTypeTextView = (TextView) findViewById(R.id.sort_type_text_view);
		mRadiusButtonGroup = (SegmentedControlGroup) findViewById(R.id.radius_filter_button_group);
		mRatingButtonGroup = (SegmentedControlGroup) findViewById(R.id.rating_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) findViewById(R.id.price_filter_button_group);

		mRefinementDismissView = findViewById(R.id.refinement_dismiss_view);
		mSearchSuggestionsListView = (ListView) findViewById(R.id.search_suggestions_list_view);

		mDatesLayout = findViewById(R.id.dates_layout);
		mDatesCalendarDatePicker = (CalendarDatePicker) findViewById(R.id.dates_date_picker);
		mGuestsLayout = findViewById(R.id.guests_layout);
		mAdultsNumberPicker = (NumberPicker) findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) findViewById(R.id.children_number_picker);

		mButtonBarLayout = findViewById(R.id.button_bar_layout);
		mRefinementInfoTextView = (TextView) findViewById(R.id.refinement_info_text_view);
		mSearchButton = findViewById(R.id.search_button);

		mProgressBarLayout = (ViewGroup) findViewById(R.id.search_progress_layout);
		mProgressBar = (GLTagProgressBar) findViewById(R.id.search_progress_bar);
		mProgressText = (TextView) findViewById(R.id.search_progress_text_view);
		mProgressBarHider = findViewById(R.id.search_progress_hider);

		mBottomBarLayout = findViewById(R.id.bottom_bar_layout);
		mFilterButton = findViewById(R.id.filter_button_layout);
		mSortButton = findViewById(R.id.sort_button_layout);
		mUpArrowFilterHotels = findViewById(R.id.up_arrow_filter_hotels);
		mUpArrowSortHotels = findViewById(R.id.up_arrow_sort_hotels);

		mSortOptionsLayout = (ViewGroup) findViewById(R.id.sort_options_layout);

		//===================================================================
		// Properties

		//-------------------------------------------------------------------
		// Note: Eff everything about this. First off, this footer has to be
		// added to the listview because it's behind a transparent view so it
		// needs to be padded up. Padding doesn't work like it should so we
		// we have to do it with a view. You'll notice that instead of
		// setting the footer's layout params we're adding a view with the
		// layout params we require. For some reason setting the layout
		// params of the footer view results in class cast exception. >:-|
		LinearLayout footer = new LinearLayout(this);
		footer.addView(
				new View(this),
				new LayoutParams(LayoutParams.FILL_PARENT, getResources().getDimensionPixelSize(
						R.dimen.row_search_suggestion_footer_height)));

		mSearchSuggestionsListView.addFooterView(footer, null, false);
		//-------------------------------------------------------------------

		mPanel.setInterpolator(new AccelerateInterpolator());
		mPanel.setOnPanelListener(mPanelListener);

		final View delegate = mPanel.getHandle();
		final View parent = (View) delegate.getParent();
		parent.post(new Runnable() {
			@Override
			public void run() {
				final Rect r = new Rect();
				delegate.getHitRect(r);
				r.top -= 50;
				parent.setTouchDelegate(new TouchDelegate(r, delegate));
			}
		});
		Time now = new Time();
		now.setToNow();
		mDatesCalendarDatePicker.setSelectionMode(SelectionMode.RANGE);
		mDatesCalendarDatePicker.setMinDate(now.year, now.month, now.monthDay);
		mDatesCalendarDatePicker.setMaxRange(29);

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
		mViewButton.setOnClickListener(mViewButtonClickListener);

		mSearchEditText.setOnFocusChangeListener(mSearchEditTextFocusChangeListener);
		mSearchEditText.setOnClickListener(mSearchEditTextClickListener);
		mSearchEditText.setOnEditorActionListener(mSearchEditorActionListener);
		mSearchEditText.addTextChangedListener(mSearchEditTextTextWatcher);
		mDatesButton.setOnClickListener(mDatesButtonClickListener);
		mGuestsButton.setOnClickListener(mGuestsButtonClickListener);

		mPanelDismissView.setOnClickListener(mPanelDismissViewClickListener);
		mTripAdvisorOnlyButton.setOnClickListener(mTripAdvisorOnlyButtonClickListener);
		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mRatingButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);

		mRefinementDismissView.setOnClickListener(mRefinementDismissViewClickListener);
		mSearchSuggestionsListView.setOnItemClickListener(mSearchSuggestionsItemClickListner);

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);
		mAdultsNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mChildrenNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mSearchButton.setOnClickListener(mSearchButtonClickListener);

		mFilterButton.setOnClickListener(mFilterButtonPressedListener);
		mSortButton.setOnClickListener(mSortButtonPressedListener);

	}

	//----------------------------------
	// SEARCH METHODS
	//----------------------------------

	private void buildFilter() {
		Log.d("Building up filter from current view settings...");

		Filter currentFilter = null;
		if (mFilter == null) {
			mFilter = new Filter();
		}
		else {
			currentFilter = mFilter.copy();
		}

		// Distance
		switch (mRadiusButtonGroup.getCheckedRadioButtonId()) {
		case R.id.radius_small_button: {
			mFilter.setSearchRadius(SearchRadius.SMALL);
			break;
		}
		case R.id.radius_medium_button: {
			mFilter.setSearchRadius(SearchRadius.MEDIUM);
			break;
		}
		case R.id.radius_large_button: {
			mFilter.setSearchRadius(SearchRadius.LARGE);
			break;
		}
		default:
		case R.id.radius_all_button: {
			mFilter.setSearchRadius(SearchRadius.ALL);
			break;
		}
		}

		// Rating
		switch (mRatingButtonGroup.getCheckedRadioButtonId()) {
		case R.id.rating_low_button: {
			mFilter.setMinimumStarRating(3);
			break;
		}
		case R.id.rating_medium_button: {
			mFilter.setMinimumStarRating(4);
			break;
		}
		case R.id.rating_high_button: {
			mFilter.setMinimumStarRating(5);
			break;
		}
		default:
		case R.id.rating_all_button: {
			mFilter.setMinimumStarRating(0);
			break;
		}
		}

		// Price
		switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
		case R.id.price_cheap_button: {
			mFilter.setPriceRange(PriceRange.CHEAP);
			break;
		}
		case R.id.price_moderate_button: {
			mFilter.setPriceRange(PriceRange.MODERATE);
			break;
		}
		case R.id.price_expensive_button: {
			mFilter.setPriceRange(PriceRange.EXPENSIVE);
			break;
		}
		default:
		case R.id.price_all_button: {
			mFilter.setPriceRange(PriceRange.ALL);
			break;
		}
		}

		// Sort
		switch (mSortOptionSelectedId) {
		case R.id.sort_popular_button: {
			mFilter.setSort(Sort.POPULAR);
			break;
		}
		case R.id.sort_price_button: {
			mFilter.setSort(Sort.PRICE);
			break;
		}
		case R.id.sort_reviews_button: {
			mFilter.setSort(Sort.RATING);
			break;
		}
		case R.id.sort_distance_button: {
			mFilter.setSort(Sort.DISTANCE);
			break;
		}
		}

		/*
		 * Don't notify listeners of the filter having changed when the activity is either not 
		 * completely setup or paused. This is because we don't want the filter changes to propogate
		 * when the radio buttons are being setup as it causes wasted cycles notifying all listeners
		 */
		if (currentFilter == null || !mFilter.equals(currentFilter) && mIsActivityResumed) {
			Log.d("Filter has changed, notifying listeners.");
			mFilter.notifyFilterChanged();
		}

		setFilterInfoText();
	}

	private void resetFilter() {
		Log.d("Resetting filter...");

		mFilter = new Filter();
		mFilter.setSearchRadius(Filter.SearchRadius.LARGE);

		setDrawerViews();
		buildFilter();
	}

	private void setSearchParamsForFreeform() {
		showLoading(true, R.string.progress_searching_hotels);

		mSearchParams.setUserFreeformLocation(mSearchParams.getFreeformLocation());

		if (!NetUtils.isOnline(this)) {
			showLoading(false, R.string.error_no_internet);
			return;
		}

		if (mGeocodeThread != null) {
			mGeocodeThread.interrupt();
		}
		mGeocodeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				mAddresses = LocationServices.geocode(SearchActivity.this, mSearchParams.getFreeformLocation());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mAddresses != null && mAddresses.size() > 1) {
							showLoading(false, null);

							showDialog(DIALOG_LOCATION_SUGGESTIONS);
						}
						else if (mAddresses != null && mAddresses.size() > 0) {
							Address address = mAddresses.get(0);
							String formattedAddress = LocationServices.formatAddress(address);
							formattedAddress = formattedAddress.replace(", USA", "");

							mSearchParams.setFreeformLocation(formattedAddress);
							setSearchEditViews();
							setSearchParams(address.getLatitude(), address.getLongitude());
							setShowDistance(address.getSubThoroughfare() != null);
							startSearchDownloader();
						}
						else {
							TrackingUtils.trackErrorPage(SearchActivity.this, "LocationNotFound");
							simulateErrorResponse(R.string.geolocation_failed);
						}
					}
				});
			}
		});
		mGeocodeThread.start();
	}

	private void setSearchParams(Double latitde, Double longitude) {
		if (mSearchParams == null) {
			mSearchParams = new SearchParams();
		}

		mSearchParams.setSearchLatLon(latitde, longitude);

		setShowDistance(true);
	}

	private void setSearchParams(SearchParams searchParams) {
		mSearchParams = searchParams;
		mSearchParams.ensureValidCheckInDate();
	}

	private void startSearch() {
		Log.i("Starting a new search...");

		mOriginalSearchParams = null;
		mSearchDownloader.cancelDownload(KEY_SEARCH);

		// Delete the currently saved search results
		File savedSearchResults = getFileStreamPath(SEARCH_RESULTS_FILE);
		if (savedSearchResults.exists()) {
			boolean results = savedSearchResults.delete();
			Log.d("Deleting previous search results.  Success: " + results);
		}

		buildFilter();
		setSearchEditViews();
		setDisplayType(DisplayType.NONE);
		disablePanelHandle();
		hideBottomBar();

		switch (mSearchParams.getSearchType()) {
		case FREEFORM: {
			stopLocationListener();
			setSearchParamsForFreeform();

			break;
		}
		case PROXIMITY: {
			stopLocationListener();
			startSearchDownloader();

			break;
		}
		case MY_LOCATION: {
			// See if we have a good enough location stored
			long minTime = Calendar.getInstance().getTimeInMillis() - MINIMUM_TIME_AGO;
			Location location = LocationServices.getLastBestLocation(this, minTime);
			if (location != null) {
				setSearchParams(location.getLatitude(), location.getLongitude());
				startSearchDownloader();
			}
			else {
				startLocationListener();
			}

			break;
		}
		}
	}

	private void startSearchDownloader() {
		// save params so that a widget can pick up the params 
		// to show results based on the last search
		saveParams();

		// broadcast the change of the search params
		// only after the params have been setup 
		// and are ready to be used to query expedia
		// services for relevant hotels
		broadcastSearchParamsChanged();

		showLoading(true, R.string.progress_searching_hotels);

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

		if (mSearchParams.getSearchType() == SearchType.FREEFORM) {
			Search.add(this, mSearchParams);
			mSearchSuggestionAdapter.refreshData();
		}

		resetFilter();

		mSearchDownloader.cancelDownload(KEY_SEARCH);
		mSearchDownloader.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	//----------------------------------
	// ACTIVITY STATE METHODS
	//----------------------------------

	private ActivityState buildActivityState() {
		ActivityState state = new ActivityState();
		state.tag = mTag;
		state.searchParams = mSearchParams;
		state.oldSearchParams = mOldSearchParams;
		state.originalSearchParams = mOriginalSearchParams;
		state.searchResponse = mSearchResponse;
		state.priceTierCache = mPriceTierCache;
		state.session = mSession;
		state.filter = mFilter;
		state.oldFilter = mOldFilter;
		state.showDistance = mShowDistance;
		state.displayType = mDisplayType;
		state.startSearchOnResume = mStartSearchOnResume;
		state.lastSearchTime = mLastSearchTime;
		state.addresses = mAddresses;

		if (state.searchResponse != null) {
			if (state.searchResponse.getFilter() != null) {
				state.searchResponse.getFilter().clearOnFilterChangedListeners();
			}
		}

		if (state.filter != null) {
			state.filter.clearOnFilterChangedListeners();
		}

		return state;
	}

	private void extractActivityState(ActivityState state) {
		mTag = state.tag;
		mSearchParams = state.searchParams;
		mOldSearchParams = state.oldSearchParams;
		mOriginalSearchParams = state.originalSearchParams;
		mSearchResponse = state.searchResponse;
		mPriceTierCache = state.priceTierCache;
		mSession = state.session;
		mFilter = state.filter;
		mOldFilter = state.oldFilter;
		mShowDistance = state.showDistance;
		mDisplayType = state.displayType;
		mStartSearchOnResume = state.startSearchOnResume;
		mLastSearchTime = state.lastSearchTime;
		mAddresses = state.addresses;
	}

	private void saveParams() {

		Log.d("Saving search parameters, filter and tag...");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("searchParams", mSearchParams.toJson().toString());
		editor.putString("filter", mFilter.toJson().toString());
		editor.putString("tag", mTag);
		SettingUtils.commitOrApply(editor);
	}

	//----------------------------------
	// BROADCAST METHODS
	//----------------------------------

	private void broadcastSearchCompleted(SearchResponse searchResponse) {
		mSearchResponse = searchResponse;
		if (mSearchListeners != null) {
			for (SearchListener searchListener : mSearchListeners) {
				searchListener.onSearchCompleted(searchResponse);
			}
		}

		onSearchResultsChanged();
		showBottomBar();
	}

	private void broadcastSearchParamsChanged() {
		// Inform all interested parties that search params have changed
		Intent i2 = new Intent("com.expedia.bookings.SEARCH_PARAMS_CHANGED");
		i2.putExtra(Codes.SEARCH_PARAMS, mSearchParams.toJson().toString());
		startService(i2);
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
	}

	public void handleError() {
		// Handling for particular errors
		boolean handledError = false;
		if (mSearchResponse != null && mSearchResponse.hasErrors()) {
			ServerError errorOne = mSearchResponse.getErrors().get(0);
			if (errorOne.getCode().equals("01")) {
				// Deprecated client version
				showDialog(DIALOG_CLIENT_DEPRECATED);

				TrackingUtils.trackErrorPage(SearchActivity.this, "OutdatedVersion");

				showLoading(false, errorOne.getExtra("message"));
			}
			else {
				showLoading(false, errorOne.getPresentableMessage(SearchActivity.this));
			}
			handledError = true;
		}

		if (!handledError) {
			TrackingUtils.trackErrorPage(SearchActivity.this, "HotelListRequestFailed");
			showLoading(false, R.string.progress_search_failed);
		}

		// Ensure that users cannot open the handle if there's an error up
		disablePanelHandle();
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
		if (!currentIsSearchDisplay && nextIsSearchDisplay) {
			storeSearchParams();
		}
		else if (currentIsSearchDisplay && !nextIsSearchDisplay) {
			restoreSearchParams();
		}

		mDisplayType = displayType;

		switch (mDisplayType) {
		case NONE: {
			// Reset focus
			mFocusLayout.requestFocus();

			hideSoftKeyboard(mSearchEditText);
			mSearchSuggestionsListView.setVisibility(View.GONE);

			//mPanelDismissView.setVisibility(View.GONE);
			openPanel(false, animate);

			mRefinementDismissView.setVisibility(View.GONE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);
			break;
		}
		case KEYBOARD: {
			showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
			mSearchSuggestionAdapter.refreshData();
			mSearchSuggestionsListView.setVisibility(View.VISIBLE);

			mPanelDismissView.setVisibility(View.GONE);
			openPanel(false, animate);

			hideSortOptions();
			mRefinementDismissView.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.VISIBLE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);

			break;
		}
		case CALENDAR: {
			mSearchEditText.clearFocus();

			hideSoftKeyboard(mSearchEditText);
			mSearchSuggestionsListView.setVisibility(View.GONE);

			mPanelDismissView.setVisibility(View.GONE);
			openPanel(false, animate);

			mRefinementDismissView.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.VISIBLE);
			mDatesLayout.setVisibility(View.VISIBLE);
			mGuestsLayout.setVisibility(View.GONE);

			break;
		}
		case GUEST_PICKER: {
			mSearchEditText.clearFocus();

			hideSoftKeyboard(mSearchEditText);
			mSearchSuggestionsListView.setVisibility(View.GONE);

			mPanelDismissView.setVisibility(View.GONE);
			openPanel(false, animate);

			mRefinementDismissView.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.VISIBLE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.VISIBLE);

			break;
		}
		case DRAWER: {
			mSearchEditText.clearFocus();

			hideSoftKeyboard(mSearchEditText);
			mSearchSuggestionsListView.setVisibility(View.GONE);

			mPanelDismissView.setVisibility(View.VISIBLE);
			openPanel(true, animate);

			mRefinementDismissView.setVisibility(View.GONE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);

			break;
		}
		case SORT_POPUP: {
			mSearchEditText.clearFocus();

			hideSoftKeyboard(mSearchEditText);
			mSearchSuggestionsListView.setVisibility(View.GONE);

			mPanelDismissView.setVisibility(View.GONE);
			openPanel(false, animate);
			showSortOptions(animate);

			mRefinementDismissView.setVisibility(View.GONE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);

			break;

		}
		}

		setSearchEditViews();
		setRefinementInfo();
		setBookingInfoText();
		setFilterInfoText();
	}

	private void openPanel(boolean toOpen, boolean animate) {
		mPanel.setOpen(toOpen, animate);

		int animationId = toOpen ? R.anim.rotate_down : R.anim.rotate_up;
		Animation rotate = AnimationUtils.loadAnimation(this, animationId);
		if (!animate) {
			rotate.setDuration(0);
		}
		mUpArrowFilterHotels.startAnimation(rotate);
	}

	private void switchResultsView() {
		mViewButton.setEnabled(false);

		Class<?> newActivityClass = null;
		Rotate3dAnimation animationOut = null;
		Rotate3dAnimation animationIn = null;

		final float centerX = mViewFlipImage.getWidth() / 2.0f;
		final float centerY = mViewFlipImage.getHeight() / 2.0f;

		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			newActivityClass = SearchMapActivity.class;

			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, -90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}

			onSwitchToMap();
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			newActivityClass = SearchListActivity.class;
			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, 90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(-90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}

			onListLoad(false, null);
		}

		if (animationOut != null && animationIn != null) {
			final Rotate3dAnimation nextAnimation = animationIn;
			final Class<?> nextActivityClass = newActivityClass;

			if (mViewFlipCanvas == null) {
				final int width = mContent.getWidth();
				final int height = mContent.getHeight();
				mViewFlipBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				mViewFlipCanvas = new Canvas(mViewFlipBitmap);
				mViewFlipImage.setImageBitmap(mViewFlipBitmap);
			}

			mContent.draw(mViewFlipCanvas);
			mContent.setVisibility(View.INVISIBLE);
			setActivity(nextActivityClass);
			setViewButtonImage();
			setBottomBarOptions();

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
					setDrawerViews();
					mContent.setVisibility(View.VISIBLE);
					mViewButton.setEnabled(true);
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
			if (newActivityClass != null) {
				setActivity(newActivityClass);
			}
			setDrawerViews();
			setViewButtonImage();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Progress bar tag

	private void hideLoading() {
		mProgressBarLayout.setVisibility(View.GONE);

		// Here, we post it so that we have a few precious frames more of the progress bar before
		// it's covered up by search results (or a lack thereof).  This keeps a black screen from
		// showing up for a split second for reason I'm not entirely sure of.  ~dlew
		mProgressBar.post(new Runnable() {
			public void run() {
				mProgressBar.setVisibility(View.GONE);
			}
		});
	}

	private void showLoading(boolean showProgress, int resId) {
		showLoading(showProgress, getString(resId));
	}

	private void showLoading(boolean showProgress, String text) {
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setShowProgress(showProgress);
		mProgressText.setText(text);
	}

	@Override
	public void onDrawStarted() {
		mProgressBarHider.post(new Runnable() {
			public void run() {
				mProgressBarHider.setVisibility(View.GONE);
			}
		});
	}

	//----------------------------------
	// VIEW BUILDING METHODS
	//----------------------------------

	private void buildPriceTierCache() {
		if (mSearchResponse != null) {
			mSearchResponse.clusterProperties();

			mPriceTierCache = new HashMap<PriceRange, PriceTier>();
			mPriceTierCache.put(PriceRange.CHEAP, mSearchResponse.getPriceTier(PriceRange.CHEAP));
			mPriceTierCache.put(PriceRange.MODERATE, mSearchResponse.getPriceTier(PriceRange.MODERATE));
			mPriceTierCache.put(PriceRange.EXPENSIVE, mSearchResponse.getPriceTier(PriceRange.EXPENSIVE));
			mPriceTierCache.put(PriceRange.ALL, mSearchResponse.getPriceTier(PriceRange.ALL));
		}
	}

	private void disablePanelHandle() {
		mPanel.getHandle().setEnabled(false);
	}

	private void enablePanelHandle() {
		mPanel.getHandle().setEnabled(true);
	}

	private void hideBottomBar() {
		mBottomBarLayout.setVisibility(View.GONE);
		mPanel.setVisibility(View.GONE);
	}

	private void showBottomBar() {
		mBottomBarLayout.setVisibility(View.VISIBLE);
		mPanel.setVisibility(View.VISIBLE);
	}

	private void showSortOptions() {
		showSortOptions(true);
	}

	private void showSortOptions(boolean animate) {
		if (mSortOptionsLayout.getVisibility() == View.VISIBLE) {
			return;
		}

		Animation rotateAnimation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.rotate_down);
		if (!animate) {
			rotateAnimation.setDuration(0);
		}
		mUpArrowSortHotels.startAnimation(rotateAnimation);

		if (!animate) {
			mSortOptionsLayout.setVisibility(View.VISIBLE);
			return;
		}

		Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.popup);
		animation.setDuration(SORT_POPUP_ANIMATION_SPEED);
		animation.setInterpolator(new AnticipateInterpolator());
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSortOptionsLayout.setVisibility(View.VISIBLE);
				mDisplayType = DisplayType.SORT_POPUP;
			}
		});
		mSortOptionsLayout.startAnimation(animation);
		hideFilterOptions();
	}

	private void hideSortOptions() {
		if (mSortOptionsLayout.getVisibility() == View.INVISIBLE) {
			return;
		}

		mDisplayType = DisplayType.NONE;
		Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_out);
		animation.setDuration(SORT_POPUP_ANIMATION_SPEED);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSortOptionsLayout.setVisibility(View.INVISIBLE);
			}
		});
		mSortOptionsLayout.startAnimation(animation);
		mUpArrowSortHotels.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, R.anim.rotate_up));
	}

	private void showFilterOptions() {
		mPanel.setOpen(true, true);
		mPanel.setVisibility(View.VISIBLE);
		mUpArrowFilterHotels.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, R.anim.rotate_down));
		hideSortOptions();
	}

	private void hideFilterOptions() {
		hideFilterOptions(true);
	}

	private void hideFilterOptions(boolean animate) {
		if (!mPanel.isOpen()) {
			return;
		}

		mPanel.setOpen(false, animate);
		mUpArrowFilterHotels.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, R.anim.rotate_up));
	}

	private View addSortOption(int sortOptionId, int sortOptionImageResId, int sortOptionTextResId, boolean noDivider) {
		View sortOption = getLayoutInflater().inflate(R.layout.snippet_sort_option, null);
		TextView sortTextView = (TextView) sortOption.findViewById(R.id.sort_option_text);
		ImageView sortImageView = (ImageView) sortOption.findViewById(R.id.sort_option_image);
		View sortOptionDivider = sortOption.findViewById(R.id.sort_option_divider);

		sortOption.setId(sortOptionId);
		sortTextView.setText(sortOptionTextResId);
		sortImageView.setImageResource(sortOptionImageResId);

		if (noDivider) {
			sortOptionDivider.setVisibility(View.GONE);
		}
		else {
			sortOptionDivider.setBackgroundDrawable(mSortOptionDivider);
		}

		mSortOptionsLayout.addView(sortOption);
		return sortOption;
	}

	private void setupSortOptions() {

		switch (mSortOptionSelectedId) {
		case R.id.sort_popular_button: {
			mSortPopularityButton.setSelected(true);
			mSortPriceButton.setSelected(false);
			mSortDistanceButton.setSelected(false);
			mSortUserRatingButton.setSelected(false);

			mSortPopularityButton.setEnabled(false);
			mSortPriceButton.setEnabled(true);
			mSortDistanceButton.setEnabled(true);
			mSortUserRatingButton.setEnabled(true);

			break;
		}
		case R.id.sort_reviews_button: {
			mSortPopularityButton.setSelected(false);
			mSortPriceButton.setSelected(false);
			mSortDistanceButton.setSelected(false);
			mSortUserRatingButton.setSelected(true);

			mSortPopularityButton.setEnabled(true);
			mSortPriceButton.setEnabled(true);
			mSortDistanceButton.setEnabled(true);
			mSortUserRatingButton.setEnabled(false);

			break;

		}
		case R.id.sort_distance_button: {
			mSortPopularityButton.setSelected(false);
			mSortPriceButton.setSelected(false);
			mSortDistanceButton.setSelected(true);
			mSortUserRatingButton.setSelected(false);

			mSortPopularityButton.setEnabled(true);
			mSortPriceButton.setEnabled(true);
			mSortDistanceButton.setEnabled(false);
			mSortUserRatingButton.setEnabled(true);

			break;

		}
		case R.id.sort_price_button: {
			mSortPopularityButton.setSelected(false);
			mSortPriceButton.setSelected(true);
			mSortDistanceButton.setSelected(false);
			mSortUserRatingButton.setSelected(false);

			mSortPopularityButton.setEnabled(true);
			mSortPriceButton.setEnabled(false);
			mSortDistanceButton.setEnabled(true);
			mSortUserRatingButton.setEnabled(true);

			break;

		}
		default: {
			mSortPopularityButton.setSelected(false);
			mSortPriceButton.setSelected(false);
			mSortDistanceButton.setSelected(false);
			mSortUserRatingButton.setSelected(false);

			mSortPopularityButton.setEnabled(true);
			mSortPriceButton.setEnabled(true);
			mSortDistanceButton.setEnabled(true);
			mSortUserRatingButton.setEnabled(true);

			break;
		}
		}
	}

	//----------------------------------
	// ACTIVITY GROUP METHODS
	//----------------------------------

	private void setActivityByTag(String tag) {
		if (tag.equals(ACTIVITY_SEARCH_LIST)) {
			setActivity(SearchListActivity.class);
		}
		else if (tag.equals(ACTIVITY_SEARCH_MAP)) {
			setActivity(SearchMapActivity.class);
		}
	}

	private void setActivity(Class<?> activity) {
		mIntent = new Intent(this, activity);
		mTag = activity.getCanonicalName();

		final Window w = mLocalActivityManager.startActivity(mTag, mIntent);
		final View wd = w != null ? w.getDecorView() : null;
		if (mLaunchedView != wd && mLaunchedView != null) {
			if (mLaunchedView.getParent() != null) {
				((FrameLayout) mLaunchedView.getParent()).removeView(mLaunchedView);
			}
		}
		mLaunchedView = wd;

		if (mLaunchedView != null) {
			mLaunchedView.setVisibility(View.VISIBLE);
			mLaunchedView.setFocusableInTouchMode(true);
			((ViewGroup) mLaunchedView).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

			if (mLaunchedView.getParent() != null) {
				((FrameLayout) mLaunchedView.getParent()).removeView(mLaunchedView);
			}

			mContent.addView(mLaunchedView);
		}
	}

	//----------------------------------
	// STORE/RESTORE SEARCH PARAMS
	//----------------------------------

	private void restoreSearchParams() {
		if (mOriginalSearchParams != null) {
			mSearchParams = mOriginalSearchParams.copy();
			mOriginalSearchParams = null;

			setSearchEditViews();
		}
	}

	private void storeSearchParams() {
		if (mOriginalSearchParams == null) {
			mOriginalSearchParams = mSearchParams.copy();
		}
	}

	//----------------------------------
	// VIEW ATTRIBUTE METHODS
	//----------------------------------

	private static final String FORMAT_HEADER = "MMM d";
	private static final String FORMAT_HEADER_WITH_YEAR = "MMM d, yyyy";

	public CharSequence getBookingInfoHeaderText() {
		String location = getSearchText();
		int startYear = mDatesCalendarDatePicker.getStartYear();
		int endYear = mDatesCalendarDatePicker.getEndYear();

		Calendar start = new GregorianCalendar(startYear, mDatesCalendarDatePicker.getStartMonth(),
				mDatesCalendarDatePicker.getStartDayOfMonth());
		Calendar end = new GregorianCalendar(endYear, mDatesCalendarDatePicker.getEndMonth(),
				mDatesCalendarDatePicker.getEndDayOfMonth());

		String startFormatter = FORMAT_HEADER;
		String endFormatter = FORMAT_HEADER;
		if (startYear != endYear) {
			// Start year differs from end year - specify year on both dates
			startFormatter = endFormatter = FORMAT_HEADER_WITH_YEAR;
		}
		else if (Calendar.getInstance().get(Calendar.YEAR) != startYear) {
			// The entire selection is in a different year from now - specify year on the end date
			endFormatter = FORMAT_HEADER_WITH_YEAR;
		}

		return Html.fromHtml(getString(R.string.booking_info_template, location,
				android.text.format.DateFormat.format(startFormatter, start),
				android.text.format.DateFormat.format(endFormatter, end)));
	}

	private void setBookingInfoText() {
		int startDay = mDatesCalendarDatePicker.getStartDayOfMonth();
		int adults = mSearchParams.getNumAdults();
		int children = mSearchParams.getNumChildren();

		mDatesTextView.setText(String.valueOf(startDay));
		mGuestsTextView.setText(String.valueOf((adults + children)));
	}

	private void setDrawerViews() {
		if (mFilter == null) {
			Log.t("Filter is null");
			return;
		}

		Log.d("setDrawerViews().  Current filter: " + mFilter.toJson().toString());

		// Temporarily remove Listeners before we start fiddling around with the filter fields
		mFilterHotelNameEditText.removeTextChangedListener(mFilterHotelNameTextWatcher);
		mRadiusButtonGroup.setOnCheckedChangeListener(null);
		mPriceButtonGroup.setOnCheckedChangeListener(null);

		// Configure the hotel name filter
		mFilterHotelNameEditText.setText(mFilter.getHotelName());

		// Configure tripadvisor filter
		switch (mFilter.getRating()) {
		case ALL:
			mTripAdvisorOnlyButton.setText(R.string.tripadvisor_rating_high);
			break;
		case HIGHLY_RATED:
			mTripAdvisorOnlyButton.setText(R.string.tripadvisor_rating_all);
			break;
		}

		// Configure the sort buttons
		switch (mFilter.getSort()) {
		case POPULAR:
			mSortOptionSelectedId = R.id.sort_popular_button;
			setupSortOptions();
			break;
		case PRICE:
			mSortOptionSelectedId = R.id.sort_price_button;
			setupSortOptions();
			break;
		case RATING:
			mSortOptionSelectedId = R.id.sort_reviews_button;
			setupSortOptions();
			break;
		case DISTANCE:
			mSortOptionSelectedId = R.id.sort_distance_button;
			setupSortOptions();
			break;
		default:
			break;
		}

		// Configure the search radius buttons
		switch (mFilter.getSearchRadius()) {
		case SMALL:
			mRadiusButtonGroup.check(R.id.radius_small_button);
			break;
		case MEDIUM:
			mRadiusButtonGroup.check(R.id.radius_medium_button);
			break;
		case LARGE:
			mRadiusButtonGroup.check(R.id.radius_large_button);
			break;
		case ALL:
			mRadiusButtonGroup.check(R.id.radius_all_button);
			break;
		default:
			mRadiusButtonGroup.check(DEFAULT_RADIUS_RADIO_GROUP_CHILD);
		}

		// Configure the price buttons
		switch (mFilter.getPriceRange()) {
		case CHEAP:
			mPriceButtonGroup.check(R.id.price_cheap_button);
			break;
		case MODERATE:
			mPriceButtonGroup.check(R.id.price_moderate_button);
			break;
		case EXPENSIVE:
			mPriceButtonGroup.check(R.id.price_expensive_button);
			break;
		case ALL:
			mPriceButtonGroup.check(R.id.price_all_button);
			break;
		default:
			mPriceButtonGroup.check(DEFAULT_PRICE_RADIO_GROUP_CHILD);
		}

		// Flip to user's preferred view
		setBottomBarOptions();

		setSortTypeText();
		setRadioButtonShadowLayers();

		// Restore Listeners
		mFilterHotelNameEditText.addTextChangedListener(mFilterHotelNameTextWatcher);
		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
	}

	private void setFilterInfoText() {
		if (mSearchResponse != null && !mSearchResponse.hasErrors()) {
			final int count = mSearchResponse.getFilteredAndSortedProperties().length;
			final String text = String.format(getString(R.string.filter_info_template), count);

			mFilterInfoTextView.setText(Html.fromHtml(text));
			mFilterInfoTextView.setVisibility(View.VISIBLE);
		}
		else {
			mFilterInfoTextView.setVisibility(View.GONE);
		}
	}

	private void setNumberPickerRanges() {
		Resources res = getResources();
		String[] adults = new String[4];
		for (int a = 0; a < 4; a++) {
			adults[a] = res.getQuantityString(R.plurals.number_of_adults, a + 1, a + 1);
		}
		String[] children = new String[5];
		for (int a = 0; a < 5; a++) {
			children[a] = res.getQuantityString(R.plurals.number_of_children, a, a);
		}

		final int numAdults = mAdultsNumberPicker.getCurrent();
		final int numChildren = mChildrenNumberPicker.getCurrent();
		final int total = numAdults + numChildren;
		int remaining = MAX_GUESTS_TOTAL - total;

		mAdultsNumberPicker.setRange(1, Math.min(MAX_GUEST_NUM, numAdults + remaining), adults);
		mChildrenNumberPicker.setRange(0, Math.min(MAX_GUEST_NUM, numChildren + remaining), children);

		mAdultsNumberPicker.setCurrent(numAdults);
		mChildrenNumberPicker.setCurrent(numChildren);
	}

	private void setRadioButtonShadowLayers() {
		List<SegmentedControlGroup> groups = new ArrayList<SegmentedControlGroup>();
		groups.add(mRadiusButtonGroup);
		groups.add(mRatingButtonGroup);
		groups.add(mPriceButtonGroup);

		for (SegmentedControlGroup group : groups) {
			final int size = group.getChildCount();
			for (int i = 0; i < size; i++) {
				View view = group.getChildAt(i);
				if (view instanceof RadioButton) {
					RadioButton radioButton = (RadioButton) view;
					if (radioButton.isChecked()) {
						radioButton.setShadowLayer(0.1f, 0, -1, 0x88000000);
					}
					else {
						radioButton.setShadowLayer(0.1f, 0, 1, 0x88FFFFFF);
					}
				}
			}
		}
	}

	private void setRefinementInfo() {
		if (mDisplayType == DisplayType.CALENDAR) {
			int nights = mDatesCalendarDatePicker.getSelectedRange() - 1;
			nights = nights > 0 ? nights : 1;
			mRefinementInfoTextView.setText(getResources().getQuantityString(R.plurals.length_of_stay, nights, nights));
		}
		else if (mDisplayType == DisplayType.GUEST_PICKER) {
			final int adults = mAdultsNumberPicker.getCurrent();
			final int children = mChildrenNumberPicker.getCurrent();
			mRefinementInfoTextView.setText(StrUtils.formatGuests(this, adults, children));
		}
		else {
			mRefinementInfoTextView.setText(null);
		}

		setBookingInfoText();
	}

	private String getSearchText() {
		switch (mSearchParams.getSearchType()) {
		case FREEFORM:
			return mSearchParams.getFreeformLocation();
		case MY_LOCATION:
			return getString(R.string.current_location);

		case PROXIMITY:
			return getString(R.string.visible_map_area);
		}

		return null;
	}

	private void setSearchEditViews() {
		if (mSearchParams == null) {
			mSearchParams = new SearchParams();
		}

		switch (mSearchParams.getSearchType()) {
		case FREEFORM: {
			mSearchEditText.setTextColor(getResources().getColor(android.R.color.black));
			break;
		}
		case MY_LOCATION: {
			mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
			break;
		}
		case PROXIMITY: {
			stopLocationListener();

			mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
			break;
		}
		}

		mSearchEditText.setText(getSearchText());

		Calendar checkIn = mSearchParams.getCheckInDate();
		mDatesCalendarDatePicker.updateStartDate(checkIn.get(Calendar.YEAR), checkIn.get(Calendar.MONTH),
				checkIn.get(Calendar.DAY_OF_MONTH));

		Calendar checkOut = mSearchParams.getCheckOutDate();
		mDatesCalendarDatePicker.updateEndDate(checkOut.get(Calendar.YEAR), checkOut.get(Calendar.MONTH),
				checkOut.get(Calendar.DAY_OF_MONTH));

		mAdultsNumberPicker.post(new Runnable() {
			@Override
			public void run() {
				mAdultsNumberPicker.setCurrent(mSearchParams.getNumAdults());
			}
		});
		mChildrenNumberPicker.post(new Runnable() {
			@Override
			public void run() {
				mChildrenNumberPicker.setCurrent(mSearchParams.getNumChildren());
			}
		});

		setBookingInfoText();
	}

	private void setShowDistance(boolean showDistance) {
		mShowDistance = showDistance;
		mSortDistanceButton.setVisibility(mShowDistance ? View.VISIBLE : View.GONE);
		mSortDistanceButton.findViewById(R.id.sort_option_divider).setVisibility(
				mShowDistance ? View.VISIBLE : View.GONE);

		if (mSetShowDistanceListeners != null) {
			for (SetShowDistanceListener showDistanceListener : mSetShowDistanceListeners) {
				showDistanceListener.onSetShowDistance(showDistance);
			}
		}
	}

	private void setSortTypeText() {
		mSortTypeTextView.setText(getString(SORT_DESCRIPTIONS.get(mFilter.getSort())));
	}

	private void setViewButtonImage() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			mViewButton.setImageResource(R.drawable.btn_actionbar_map);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			mViewButton.setImageResource(R.drawable.btn_actionbar_list);
		}
	}

	private void setBottomBarOptions() {
		ImageView imageView = (ImageView) findViewById(R.id.up_arrow_sort_hotels);
		TextView textView = (TextView) findViewById(R.id.sort_text_view);

		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			imageView.setImageResource(R.drawable.up_arrow);
			textView.setText(R.string.sort_hotels);
			mSortTypeTextView.setVisibility(View.VISIBLE);
			mSortButton.setOnClickListener(mSortButtonPressedListener);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			imageView.setImageResource(R.drawable.ic_search_map);
			textView.setText(R.string.map_search_button);
			mSortButton.setOnClickListener(mMapSearchButtonClickListener);
			mSortTypeTextView.setVisibility(View.GONE);
		}
	}

	private void switchRatingFilter() {
		final Rating rating = mFilter.getRating();
		switch (rating) {
		case ALL: {
			mFilter.setRatingFilter(Rating.HIGHLY_RATED);
			break;
		}
		case HIGHLY_RATED: {
			mFilter.setRatingFilter(Rating.ALL);
			break;
		}
		}

		mFilter.notifyFilterChanged();

		setDrawerViews();
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

	private final TextWatcher mSearchEditTextTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.toString().equals(getString(R.string.current_location))) {
				mSearchParams.setSearchType(SearchType.MY_LOCATION);
				mSearchParams.setFreeformLocation("");
				mSearchParams.setSearchLatLon(mSearchParams.getSearchLatitude(), mSearchParams.getSearchLongitude());
			}
			else if (s.toString().equals(getString(R.string.visible_map_area))) {
				mSearchParams.setSearchType(SearchType.PROXIMITY);
				mSearchParams.setFreeformLocation("");
				mSearchParams.setSearchLatLon(mSearchParams.getSearchLatitude(), mSearchParams.getSearchLongitude());
			}
			else if (count > 0) {
				mSearchParams.setSearchType(SearchType.FREEFORM);
				mSearchParams.setFreeformLocation(s.toString());
			}
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
			mFilter.setHotelName(s.toString());
			mFilter.notifyFilterChanged();
			setFilterInfoText();
		}
	};

	//----------------------------------
	// EVENT LISTENERS
	//----------------------------------

	private final AdapterView.OnItemClickListener mSearchSuggestionsItemClickListner = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			if (position == 0) {
				mSearchParams.setSearchType(SearchType.MY_LOCATION);
			}
			else {
				setSearchParams((SearchParams) mSearchSuggestionAdapter.getItem(position));
			}

			setDisplayType(DisplayType.CALENDAR);
		}
	};

	private final CalendarDatePicker.OnDateChangedListener mDatesDateChangedListener = new CalendarDatePicker.OnDateChangedListener() {
		@Override
		public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
			if (mOriginalSearchParams != null) {
				Calendar startCalendar = Calendar.getInstance();
				Calendar endCalendar = Calendar.getInstance();

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

				mSearchParams.setCheckInDate(startCalendar);
				mSearchParams.setCheckOutDate(endCalendar);
			}

			setRefinementInfo();
		}
	};

	private final NumberPicker.OnChangedListener mNumberPickerChangedListener = new NumberPicker.OnChangedListener() {
		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {
			mSearchParams.setNumAdults(mAdultsNumberPicker.getCurrent());
			mSearchParams.setNumChildren(mChildrenNumberPicker.getCurrent());

			setNumberPickerRanges();
			setRefinementInfo();
		}
	};

	private final Panel.OnPanelListener mPanelListener = new Panel.OnPanelListener() {
		@Override
		public void onPanelOpened(Panel panel) {
			mDisplayType = DisplayType.DRAWER;

			if (mPanelDismissView.getVisibility() == View.VISIBLE) {
				return;
			}
			final Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_in);
			animation.setDuration(ANIMATION_PANEL_DISMISS_SPEED);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					mPanelDismissView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mPanelDismissView.setVisibility(View.VISIBLE);
				}
			});
			mPanelDismissView.startAnimation(animation);

			onOpenFilterPanel();
		}

		@Override
		public void onPanelClosed(Panel panel) {
			if (mPanelDismissView.getVisibility() == View.GONE) {
				return;
			}
			final Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_out);
			animation.setDuration(ANIMATION_PANEL_DISMISS_SPEED);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					mPanelDismissView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mPanelDismissView.setVisibility(View.GONE);
				}
			});
			mPanelDismissView.setAnimation(animation);

			onSearchResultsChanged();
		}
	};

	private final RadioGroup.OnCheckedChangeListener mFilterButtonGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			buildFilter();
			setSortTypeText();
			setRadioButtonShadowLayers();
		}
	};

	private final View.OnClickListener mSortOptionChangedListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mSortOptionSelectedId = v.getId();
			setupSortOptions();
			buildFilter();
			setSortTypeText();
			hideSortOptions();
		}
	};

	private final TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				startSearch();
				return true;
			}

			return false;
		}
	};

	private final View.OnClickListener mDatesButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setDisplayType(DisplayType.CALENDAR);
		}
	};

	private final View.OnClickListener mGuestsButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setDisplayType(DisplayType.GUEST_PICKER);
		}
	};

	private final View.OnClickListener mMapSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mMapViewListener != null) {
				GeoPoint center = mMapViewListener.onRequestMapCenter();
				mSearchParams.setFreeformLocation("");
				mSearchParams.setDestinationId(null);
				mSearchParams.setSearchType(SearchType.PROXIMITY);

				setSearchParams(MapUtils.getLatitude(center), MapUtils.getLongitude(center));
				startSearch();
			}
		}
	};

	private final View.OnClickListener mPanelDismissViewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setDisplayType(DisplayType.NONE);
		}
	};

	private final View.OnClickListener mRefinementDismissViewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setDisplayType(DisplayType.NONE);
		}
	};

	private final View.OnClickListener mSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startSearch();
		}
	};

	private final View.OnClickListener mSearchEditTextClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			setDisplayType(DisplayType.KEYBOARD);
		}
	};

	private final View.OnClickListener mTripAdvisorOnlyButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchRatingFilter();
			setDisplayType(DisplayType.NONE);
		}
	};

	private final View.OnClickListener mViewButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchResultsView();
		}
	};

	private final View.OnFocusChangeListener mSearchEditTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				setDisplayType(DisplayType.KEYBOARD);

				if (mSearchParams.getSearchType() != SearchType.FREEFORM) {
					mSearchEditText.post(new Runnable() {
						@Override
						public void run() {
							mSearchEditText.setText(null);
							mSearchEditText.setTextColor(getResources().getColor(android.R.color.black));
						}
					});
				}
				else {
					mSearchEditText.selectAll();
				}
			}
		}
	};

	private final View.OnClickListener mFilterButtonPressedListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mPanel.isOpen()) {
				hideFilterOptions();
			}
			else {
				showFilterOptions();
			}
		}
	};

	private final View.OnClickListener mSortButtonPressedListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mSortOptionsLayout.getVisibility() == View.VISIBLE) {
				hideSortOptions();
			}
			else {
				showSortOptions();
			}
		}
	};

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

	private class ActivityState {
		// Safe
		public String tag;
		public boolean showDistance;
		public Map<PriceRange, PriceTier> priceTierCache;
		public Session session;
		public SearchParams searchParams;
		public SearchParams oldSearchParams;
		public SearchParams originalSearchParams;
		public boolean startSearchOnResume;

		public DisplayType displayType;
		public long lastSearchTime;
		public List<Address> addresses; // For geocoding disambiguation

		// Questionable
		public SearchResponse searchResponse;
		public Filter filter;
		public Filter oldFilter;
	}

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

	// The SettingUtils key for the last version tracked
	private static final String TRACK_VERSION = "tracking_version";

	public void onPageLoad() {
		// Only send page load when the app just started up - if there's a previous instance, that means
		// it was just a configuration change.
		if (getLastNonConfigurationInstance() == null) {
			Log.d("Tracking \"App.Loading\" pageLoad...");

			AppMeasurement s = new AppMeasurement(getApplication());

			TrackingUtils.addStandardFields(this, s);

			s.pageName = "App.Loading";

			// Determine if this is a new install, an upgrade, or just a regular launch
			String trackVersion = SettingUtils.get(this, TRACK_VERSION, null);
			String currentVersion = AndroidUtils.getAppVersion(this);

			boolean save = false;
			if (trackVersion == null) {
				// New install
				s.events = "event28";
				save = true;
			}
			else if (!trackVersion.equals(currentVersion)) {
				// App was upgraded
				s.events = "event29";
				save = true;
			}
			else {
				// Regular launch
				s.events = "event27";
			}

			if (save) {
				// Save new data
				SettingUtils.save(this, TRACK_VERSION, currentVersion);
			}

			// Send the tracking data
			s.track();
		}
	}

	private void onSearchResultsChanged() {
		// If we already have results, check for refinements; if there were none, it's possible
		// that the user just opened/closed a search param change without changing anything.
		// 
		// This is a somewhat lazy way of doing things, but it is easiest and catches a bunch
		// of refinements at once instead of flooding the system with a ton of different refinements
		String refinementsStr = null;
		if (mOldFilter != null && mOldSearchParams != null) {
			List<String> refinements = new ArrayList<String>();

			// Sort change
			if (mOldFilter.getSort() != mFilter.getSort()) {
				if (mFilter.getSort() == Sort.POPULAR) {
					refinements.add("App.Hotels.Search.Sort.Popular");
				}
				else {
					refinements.add("App.Hotels.Search.Sort.Price");
				}
			}

			// Number of travelers change
			if (mSearchParams.getNumAdults() != mOldSearchParams.getNumAdults()
					|| mSearchParams.getNumChildren() != mOldSearchParams.getNumChildren()) {
				refinements.add("App.Hotels.Search.Refine.NumberTravelers");
			}

			// Location change
			// Checks that the search type is the same, or else that a search of a particular type hasn't
			// been modified (e.g., freeform text changing on a freeform search)
			if (mSearchParams.getSearchType() != mOldSearchParams.getSearchType()
					|| (mSearchParams.getSearchType() == SearchType.FREEFORM && !mSearchParams.getFreeformLocation()
							.equals(mOldSearchParams.getFreeformLocation()))
					|| ((mSearchParams.getSearchType() == SearchType.MY_LOCATION || mSearchParams.getSearchType() == SearchType.PROXIMITY) && (mSearchParams
							.getSearchLatitude() != mOldSearchParams.getSearchLatitude() || mSearchParams
							.getSearchLongitude() != mOldSearchParams.getSearchLongitude()))) {
				refinements.add("App.Hotels.Search.Refine.Location");
			}

			// Checkin date change
			if (!mSearchParams.getCheckInDate().equals(mOldSearchParams.getCheckInDate())) {
				refinements.add("App.Hotels.Search.Refine.CheckinDate");
			}

			// Checkout date change
			if (!mSearchParams.getCheckOutDate().equals(mOldSearchParams.getCheckOutDate())) {
				refinements.add("App.Hotels.Search.Refine.CheckoutDate");
			}

			// Search radius change
			if (mFilter.getSearchRadius() != mOldFilter.getSearchRadius()) {
				refinements.add("App.Hotels.Search.Refine.SearchRadius");
			}

			// Price range change
			if (mFilter.getPriceRange() != mOldFilter.getPriceRange()) {
				refinements.add("App.Hotels.Search.Refine.PriceRange");
			}

			// Star rating change
			if (mFilter.getMinimumStarRating() != mOldFilter.getMinimumStarRating()) {
				refinements.add("App.Hotels.Search.Refine.StarRating");
			}

			boolean hasHotelFilter = mFilter.getHotelName() != null;
			boolean oldHasHotelFilter = mOldFilter.getHotelName() != null;
			if (hasHotelFilter != oldHasHotelFilter
					|| (hasHotelFilter && !mFilter.getHotelName().equals(mOldFilter.getHotelName()))) {
				refinements.add("App.Hotels.Search.Refine.HotelName");
			}

			// Rating filter change
			if (mFilter.getRating() != mOldFilter.getRating()) {
				if (mFilter.getRating() == Rating.HIGHLY_RATED) {
					refinements.add("App.Hotels.Search.Refine.ShowHighlyRatedHotels");
				}
				else {
					refinements.add("App.Hotels.Search.Refine.ShowAllHotels");
				}
			}

			int numRefinements = refinements.size();
			if (numRefinements == 0) {
				return;
			}

			StringBuilder sb = new StringBuilder();
			for (int a = 0; a < numRefinements; a++) {
				if (a != 0) {
					sb.append("|");
				}
				sb.append(refinements.get(a));
			}
			refinementsStr = sb.toString();
		}

		// Update the last filter/search params we used to track refinements 
		mOldSearchParams = mSearchParams.copy();
		mOldFilter = mFilter.copy();

		// Start actually tracking the search result change
		onListLoad(true, refinementsStr);
	}

	private void onListLoad(boolean onSearchCompleted, String refinements) {
		// Start actually tracking the search result change
		Log.d("Tracking \"App.Hotels.Search\" pageLoad...");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.Search";

		if (onSearchCompleted) {
			// Whether this was the first search or a refined search
			s.events = (refinements != null && refinements.length() > 0) ? "event31" : "event30";

			// Refinement  
			s.eVar28 = s.prop16 = refinements;
		}

		// LOB Search
		s.eVar2 = s.prop2 = "hotels";

		// Region
		DecimalFormat df = new DecimalFormat("#.######");
		String region = null;
		if (mSearchParams.getSearchType() == SearchType.FREEFORM) {
			region = mSearchParams.getFreeformLocation();
		}
		else {
			region = df.format(mSearchParams.getSearchLatitude()) + "|" + df.format(mSearchParams.getSearchLongitude());
		}
		s.eVar4 = s.prop4 = region;

		// Check in/check out date
		s.eVar5 = s.prop5 = CalendarUtils.getDaysBetween(mSearchParams.getCheckInDate(), Calendar.getInstance()) + "";
		s.eVar6 = s.prop16 = CalendarUtils.getDaysBetween(mSearchParams.getCheckOutDate(),
				mSearchParams.getCheckInDate())
				+ "";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Number adults searched for
		s.eVar47 = mSearchParams.getNumAdults() + "";

		// Freeform location
		if (mSearchParams.getSearchType() == SearchType.FREEFORM) {
			s.eVar48 = mSearchParams.getUserFreeformLocation();
		}

		// Number of search results
		if (mSearchResponse != null && mSearchResponse.getFilteredAndSortedProperties() != null) {
			s.prop1 = mSearchResponse.getFilteredAndSortedProperties().length + "";
		}

		// Send the tracking data
		s.track();
	}

	private void onOpenFilterPanel() {
		Log.d("Tracking \"App.Hotels.Search.Refine\" onClick...");
		TrackingUtils.trackSimpleEvent(this, "App.Hotels.Search.Refine", null, "Shopper", null);
	}

	private void onSwitchToMap() {
		Log.d("Tracking \"App.Hotels.Search.Map\" pageLoad...");
		TrackingUtils.trackSimpleEvent(this, "App.Hotels.Search.Map", null, "Shopper", null);
	}
}