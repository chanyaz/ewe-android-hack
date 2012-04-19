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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentMapActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp.OnSearchParamsChangedInWidgetListener;
import com.expedia.bookings.animation.Rotate3dAnimation;
import com.expedia.bookings.content.AutocompleteProvider;
import com.expedia.bookings.data.Codes;
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
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
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
import com.mobiata.android.hockey.helper.HockeyAppUtil;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.NumberPicker;
import com.mobiata.android.widget.Panel;
import com.mobiata.android.widget.SegmentedControlGroup;

public class PhoneSearchActivity extends FragmentMapActivity implements LocationListener, OnDrawStartedListener,
		HotelListFragmentListener, HotelMapFragmentListener, OnFilterChangedListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	//////////////////////////////////////////////////////////////////////////////////////////
	// ENUMS
	//////////////////////////////////////////////////////////////////////////////////////////

	private enum DisplayType {
		NONE(false), KEYBOARD(true), CALENDAR(true), GUEST_PICKER(true), DRAWER(false), SORT_POPUP(false);

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

	private static final int REQUEST_CODE_SETTINGS = 1;

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

	private View mSearchButton;
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
	private TextView mSelectChildAgeTextView;
	private TextView mSortTextView;
	private View mButtonBarLayout;
	private View mDatesLayout;
	private View mFocusLayout;
	private View mGuestsLayout;
	private View mChildAgesLayout;
	private View mPanelDismissView;
	private View mSortPopupDismissView;
	private View mRefinementDismissView;
	private View mBottomBarLayout;
	private View mSortButton;
	private View mSearchMapButton;
	private View mFilterButton;
	private ImageView mUpArrowFilterHotels;
	private ImageView mUpArrowSortHotels;
	private ViewGroup mSortOptionsLayout;
	private PopupWindow mSortPopup;

	private List<View> mSortButtons;
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

	private HotelListFragment mHotelListFragment;
	private HotelMapFragment mHotelMapFragment;

	private String mTag;

	private DisplayType mDisplayType = DisplayType.NONE;
	private boolean mShowDistance = true;

	private Bitmap mViewFlipBitmap;
	private Canvas mViewFlipCanvas;

	private Bitmap mSortOptionDividerBitmap;
	private BitmapDrawable mSortOptionDivider;
	private int mSortOptionSelectedId;
	private boolean mFilterButtonArrowUp = true;

	private ArrayList<Address> mAddresses;
	private SearchParams mOldSearchParams;
	private SearchParams mOriginalSearchParams;
	private Filter mOldFilter;
	public boolean mStartSearchOnResume;
	private long mLastSearchTime = -1;
	private boolean mIsWidgetNotificationShowing;

	private SearchSuggestionAdapter mSearchSuggestionAdapter;

	private boolean mIsActivityResumed = false;

	// This indicates to mSearchCallback that we just loaded saved search results,
	// and as such should behave a bit differently than if we just did a new search.
	private boolean mLoadedSavedResults;

	// The last selection for the search EditText.  Used to maintain between rotations
	private int mSearchTextSelectionStart = -1;
	private int mSearchTextSelectionEnd = -1;

	//----------------------------------
	// THREADS/CALLBACKS
	//----------------------------------

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(PhoneSearchActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SEARCH, services);
			return services.search(Db.getSearchParams(), 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			// Clear the old listener so we don't end up with a memory leak
			Db.getFilter().clearOnFilterChangedListeners();

			SearchResponse searchResponse = (SearchResponse) results;
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
					mRadiusButtonGroup.check(R.id.radius_all_button);
					searchResponse.clearCache();
				}
				ImageCache.recycleCache(true);
				broadcastSearchCompleted(searchResponse);

				hideLoading();
				setFilterInfoText();

				// #9773: Show distance sort initially, if user entered street address-level search params
				if (mShowDistance) {
					mSortOptionChangedListener.onClick(mSortDistanceButton);
				}

				mLastSearchTime = Calendar.getInstance().getTimeInMillis();
				enablePanelHandle();
			}
			else if (searchResponse != null && searchResponse.getPropertiesCount() > 0
					&& searchResponse.getLocations() != null && searchResponse.getLocations().size() > 0) {
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

	private OnSearchParamsChangedInWidgetListener mSearchpParamsChangedListener = new OnSearchParamsChangedInWidgetListener() {

		@Override
		public void onSearchParamsChanged(SearchParams searchParams) {
			Db.setSearchParams(searchParams);
			searchParams.ensureValidCheckInDate();
			mStartSearchOnResume = true;
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
		if (getIntent().getBooleanExtra(Codes.EXTRA_FINISH, false)) {
			finish();
			return;
		}

		mContext = this;

		setContentView(R.layout.activity_search);

		mHotelListFragment = Ui.findSupportFragment(this, getString(R.string.tag_hotel_list));
		mHotelMapFragment = Ui.findSupportFragment(this, getString(R.string.tag_hotel_map));

		initializeViews();

		mSortOptionDividerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sort_option_border);
		mSortOptionDivider = new BitmapDrawable(mSortOptionDividerBitmap);
		mSortOptionDivider.setTileModeX(Shader.TileMode.REPEAT);

		mSortPopularityButton = addSortOption(R.id.sort_popular_button, R.drawable.ic_sort_popular,
				R.string.sort_description_popular, F_NO_DIVIDERS + F_FIRST);
		mSortPriceButton = addSortOption(R.id.sort_price_button, R.drawable.ic_sort_price,
				R.string.sort_description_price, 0);
		mSortUserRatingButton = addSortOption(R.id.sort_reviews_button, R.drawable.ic_sort_user_rating,
				R.string.sort_description_rating, 0);
		mSortDistanceButton = addSortOption(R.id.sort_distance_button, R.drawable.ic_sort_distance,
				R.string.sort_description_distance, F_LAST);
		mSortOptionsLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mSortPopup = new PopupWindow(mSortOptionsLayout, mSortOptionsLayout.getMeasuredWidth(),
				mSortOptionsLayout.getMeasuredHeight());
		mSortPopup.setAnimationStyle(R.style.Animation_Popup);
		mSortPopup.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				mSortPopupDismissView.setVisibility(View.GONE);
			}
		});

		mSortPriceButton.setOnClickListener(mSortOptionChangedListener);
		mSortPopularityButton.setOnClickListener(mSortOptionChangedListener);
		mSortDistanceButton.setOnClickListener(mSortOptionChangedListener);
		mSortUserRatingButton.setOnClickListener(mSortOptionChangedListener);

		mSortButtons = new ArrayList<View>();
		mSortButtons.add(mSortPopularityButton);
		mSortButtons.add(mSortPriceButton);
		mSortButtons.add(mSortUserRatingButton);
		mSortButtons.add(mSortDistanceButton);

		mSearchSuggestionAdapter = new SearchSuggestionAdapter(this);
		mSearchSuggestionsListView.setAdapter(mSearchSuggestionAdapter);

		boolean localeChanged = SettingUtils.get(this, LocaleChangeReceiver.KEY_LOCALE_CHANGED, false);

		boolean toBroadcastSearchCompleted = false;
		SearchResponse searchResponse = Db.getSearchResponse();
		if (savedInstanceState != null && !localeChanged) {
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
			if (!localeChanged && versionGood) {
				BackgroundDownloader.getInstance().startDownload(KEY_LOADING_PREVIOUS, mLoadSavedResults,
						mLoadSavedResultsCallback);
				broadcastSearchStarted();
				showLoading(true, R.string.loading_previous);
			}
			else {
				startSearch();
			}
		}

		SearchParams searchParams = Db.getSearchParams();
		mAdultsNumberPicker.setTextEnabled(false);
		mChildrenNumberPicker.setTextEnabled(false);
		mAdultsNumberPicker.setRange(1, GuestsPickerUtils.getMaxPerType());
		mChildrenNumberPicker.setRange(0, GuestsPickerUtils.getMaxPerType());
		mAdultsNumberPicker.setCurrent(searchParams.getNumAdults());
		mChildrenNumberPicker.setCurrent(searchParams.getNumChildren());

		showFragment(mTag);
		setShowDistance(mShowDistance);
		setDisplayType(mDisplayType, false);

		// 9028:t only broadcast search completed once all 
		// elements have been setup
		if (toBroadcastSearchCompleted) {
			broadcastSearchCompleted(searchResponse);
		}

		if (localeChanged) {
			// Mark that we've read the change
			SettingUtils.save(this, LocaleChangeReceiver.KEY_LOCALE_CHANGED, false);
		}

		// HockeyApp update
		if (!AndroidUtils.isRelease(mContext)) {
			HockeyAppUtil.checkForUpdatesHockeyApp(mContext, this, Codes.HOCKEY_APP_ID);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		hideSortOptions();
		hideFilterOptions(false);

		if (intent.getBooleanExtra(Codes.EXTRA_NEW_SEARCH, false)) {
			mStartSearchOnResume = true;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsActivityResumed = false;

		mProgressBar.onPause();
		stopLocationListener();

		Db.getFilter().removeOnFilterChangedListener(this);

		mSearchEditText.removeTextChangedListener(mSearchEditTextTextWatcher);

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
		((ExpediaBookingApp) getApplicationContext())
				.registerSearchParamsChangedInWidgetListener(mSearchpParamsChangedListener);

		Db.getFilter().addOnFilterChangedListener(this);

		mProgressBar.onResume();

		CalendarUtils.configureCalendarDatePicker(mDatesCalendarDatePicker);

		setViewButtonImage();
		setDrawerViews();
		setSearchEditViews();
		setBottomBarOptions();
		GuestsPickerUtils.configureAndUpdateDisplayedValues(this, mAdultsNumberPicker, mChildrenNumberPicker);

		displayRefinementInfo();
		setActionBarBookingInfoText();

		// #9103: Must add this after onResume(); otherwise it gets called when mSearchEditText
		// automagically restores its previous state.
		mSearchEditText.addTextChangedListener(mSearchEditTextTextWatcher);

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

		//HockeyApp crash
		if (!AndroidUtils.isRelease(mContext)) {
			HockeyAppUtil.checkForCrashesHockeyApp(mContext, Codes.HOCKEY_APP_ID);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

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
				.unregisterSearchParamsChangedInWidgetListener(mSearchpParamsChangedListener);

		// do not attempt to save parameters if the user was short circuited to the
		// confirmation screen when the search activity started
		if (isFinishing() && !ConfirmationUtils.hasSavedConfirmationData(this)) {
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
					SearchParams searchParams = Db.getSearchParams();
					searchParams.setFreeformLocation(formattedAddress);
					setSearchEditViews();

					searchParams.setSearchLatLon(address.getLatitude(), address.getLongitude());
					determineWhetherExactLocationSpecified(address);
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearchDownloader();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					simulateErrorResponse(getString(R.string.NoGeocodingResults, Db.getSearchParams()
							.getFreeformLocation()));
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					simulateErrorResponse(getString(R.string.NoGeocodingResults, Db.getSearchParams()
							.getFreeformLocation()));
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
		DebugMenu.onCreateOptionsMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.log_in: {
			Intent intent = new Intent(this, SignInActivity.class);
			startActivity(intent);
			break;
		}
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

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
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

	public SearchParams getSearchParams() {
		return Db.getSearchParams();
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
		mFilterHotelNameEditText = (EditText) findViewById(R.id.filter_hotel_name_edit_text);
		mSortTextView = (TextView) findViewById(R.id.sort_text_view);
		mRadiusButtonGroup = (SegmentedControlGroup) findViewById(R.id.radius_filter_button_group);
		mRatingButtonGroup = (SegmentedControlGroup) findViewById(R.id.rating_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) findViewById(R.id.price_filter_button_group);

		mRefinementDismissView = findViewById(R.id.refinement_dismiss_view);
		mSearchSuggestionsListView = (ListView) findViewById(R.id.search_suggestions_list_view);

		mDatesLayout = findViewById(R.id.dates_layout);
		mDatesCalendarDatePicker = (CalendarDatePicker) findViewById(R.id.dates_date_picker);
		mGuestsLayout = findViewById(R.id.guests_layout);
		mChildAgesLayout = findViewById(R.id.child_ages_layout);
		mAdultsNumberPicker = (NumberPicker) findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) findViewById(R.id.children_number_picker);

		mButtonBarLayout = findViewById(R.id.button_bar_layout);
		mRefinementInfoTextView = (TextView) findViewById(R.id.refinement_info_text_view);
		mSelectChildAgeTextView = (TextView) findViewById(R.id.label_select_each_childs_age);
		mSearchButton = findViewById(R.id.search_button);

		mProgressBarLayout = (ViewGroup) findViewById(R.id.search_progress_layout);
		mProgressBar = (GLTagProgressBar) findViewById(R.id.search_progress_bar);
		mProgressText = (TextView) findViewById(R.id.search_progress_text_view);
		mProgressBarHider = findViewById(R.id.search_progress_hider);

		mBottomBarLayout = findViewById(R.id.bottom_bar_layout);
		mFilterButton = findViewById(R.id.filter_button_layout);
		mSortButton = findViewById(R.id.sort_button_layout);
		mSearchMapButton = findViewById(R.id.search_map_button_layout);
		mUpArrowFilterHotels = (ImageView) findViewById(R.id.up_arrow_filter_hotels);
		mUpArrowSortHotels = (ImageView) findViewById(R.id.up_arrow_sort_hotels);

		mSortOptionsLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.include_sort_popup, null);
		mSortPopupDismissView = findViewById(R.id.sort_popup_dismiss_view);

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
		CalendarUtils.configureCalendarDatePicker(mDatesCalendarDatePicker);

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

		LayoutUtils.configureRadiusFilterLabels(this, mRadiusButtonGroup, Db.getFilter());

		// 8781: Clear focus on hotel name edit text when user clicks "done".  Otherwise, we retain
		// focus and some keyboards keep up parts of themselves (like the "suggestion" bar)
		mFilterHotelNameEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					v.clearFocus();
				}
				return false;
			}
		});

		//===================================================================
		// Listeners
		mViewButton.setOnClickListener(mViewButtonClickListener);

		mSearchEditText.setOnFocusChangeListener(mSearchEditTextFocusChangeListener);
		mSearchEditText.setOnClickListener(mSearchEditTextClickListener);
		mSearchEditText.setOnEditorActionListener(mSearchEditorActionListener);
		mDatesButton.setOnClickListener(mDatesButtonClickListener);
		mGuestsButton.setOnClickListener(mGuestsButtonClickListener);

		mPanelDismissView.setOnClickListener(mPanelDismissViewClickListener);
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
		mSearchMapButton.setOnClickListener(mMapSearchButtonClickListener);
		mSortPopupDismissView.setOnClickListener(mSortPopupDismissViewClickListener);

	}

	//----------------------------------
	// SEARCH METHODS
	//----------------------------------

	private void buildFilter() {
		Log.d("Building up filter from current view settings...");

		Filter filter = Db.getFilter();
		Filter currentFilter = filter.copy();

		// Distance
		switch (mRadiusButtonGroup.getCheckedRadioButtonId()) {
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
		switch (mRatingButtonGroup.getCheckedRadioButtonId()) {
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
		switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
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
		case R.id.sort_popular_button: {
			filter.setSort(Sort.POPULAR);
			break;
		}
		case R.id.sort_price_button: {
			filter.setSort(Sort.PRICE);
			break;
		}
		case R.id.sort_reviews_button: {
			filter.setSort(Sort.RATING);
			break;
		}
		case R.id.sort_distance_button: {
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

		setFilterInfoText();
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

		setDrawerViews();
	}

	private void startSearch() {
		Log.i("Starting a new search...");

		mOriginalSearchParams = null;
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
		setDisplayType(DisplayType.NONE);
		disablePanelHandle();
		hideBottomBar();
		saveParams();

		switch (Db.getSearchParams().getSearchType()) {
		case FREEFORM: {
			stopLocationListener();
			startGeocode();

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
				onLocationChanged(location);
			}
			else {
				startLocationListener();
			}

			break;
		}
		// TODO: Add "region" search once enum is added.
		}
	}

	private void startGeocode() {
		showLoading(true, R.string.progress_searching_hotels);

		SearchParams searchParams = Db.getSearchParams();

		if (searchParams.hasEnoughToSearch()) {
			Log.d("User already has region id or lat/lng for freeform location, skipping geocoding.");

			if (searchParams.hasSearchLatLon()) {
				setShowDistance(true);
			}
			else {
				setShowDistance(false);
			}

			startSearchDownloader();
			return;
		}

		Log.d("Geocoding: " + searchParams.getFreeformLocation());

		searchParams.setUserFreeformLocation(searchParams.getFreeformLocation());

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_GEOCODE);
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
			// Need to convert to ArrayList so it can be saved easily in Bundles
			mAddresses = new ArrayList<Address>();
			for (Address address : (List<Address>) results) {
				mAddresses.add(address);
			}

			if (mAddresses != null && mAddresses.size() > 1) {
				showLoading(false, null);

				showDialog(DIALOG_LOCATION_SUGGESTIONS);
			}
			else if (mAddresses != null && mAddresses.size() > 0) {
				Address address = mAddresses.get(0);
				String formattedAddress = StrUtils.removeUSAFromAddress(address);
				SearchParams searchParams = Db.getSearchParams();
				searchParams.setFreeformLocation(formattedAddress);
				setSearchEditViews();
				searchParams.setSearchLatLon(address.getLatitude(), address.getLongitude());
				determineWhetherExactLocationSpecified(address);
				startSearchDownloader();
			}
			else {
				TrackingUtils.trackErrorPage(PhoneSearchActivity.this, "LocationNotFound");
				simulateErrorResponse(R.string.geolocation_failed);
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

		if (!NetUtils.isOnline(this)) {
			simulateErrorResponse(R.string.error_no_internet);
			return;
		}

		if (Db.getSearchParams().getSearchType() == SearchType.FREEFORM) {
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
	private static final String INSTANCE_ORIGINAL_SEARCH_PARAMS = "INSTANCE_ORIGINAL_SEARCH_PARAMS";
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
		JSONUtils.putJSONable(outState, INSTANCE_ORIGINAL_SEARCH_PARAMS, mOriginalSearchParams);
		JSONUtils.putJSONable(outState, INSTANCE_OLD_FILTER, mOldFilter);

		// #9733: You cannot keep displaying a PopupWindow on rotation.  Since it's not essential the popup
		// stay visible, it's easier here just to hide it between activity shifts.
		if (mDisplayType == DisplayType.SORT_POPUP) {
			outState.putInt(INSTANCE_DISPLAY_TYPE, DisplayType.NONE.ordinal());
		}
		else {
			outState.putInt(INSTANCE_DISPLAY_TYPE, mDisplayType.ordinal());
		}
		return outState;
	}

	private void restoreActivityState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTag = savedInstanceState.getString(INSTANCE_TAG, getString(R.string.tag_hotel_list));
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
			mOriginalSearchParams = JSONUtils.getJSONable(savedInstanceState, INSTANCE_ORIGINAL_SEARCH_PARAMS,
					SearchParams.class);
			mOldFilter = JSONUtils.getJSONable(savedInstanceState, INSTANCE_OLD_FILTER, Filter.class);
		}
	}

	//----------------------------------
	// BROADCAST METHODS
	//----------------------------------

	private void broadcastSearchCompleted(SearchResponse searchResponse) {
		Db.setSearchResponse(searchResponse);

		// Inform fragments
		mHotelListFragment.notifySearchComplete();
		mHotelMapFragment.notifySearchComplete();

		onSearchResultsChanged();
		showBottomBar();

		showWidgetNotificationIfApplicable();
	}

	private void showWidgetNotificationIfApplicable() {
		// only show the notification if its never been shown before
		// or if it were showing before orientation change
		boolean shouldShowWidget = shouldShowWidgetNotification();
		if (!mIsWidgetNotificationShowing && !shouldShowWidget) {
			return;
		}

		// Inflate the view stub
		ViewStub stub = Ui.findView(this, R.id.widget_notification_stub);
		View widgetNotificationBar = stub.inflate();

		// Configure the layout
		widgetNotificationBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(PhoneSearchActivity.this, getString(R.string.widget_add_instructions),
						Toast.LENGTH_SHORT).show();
			}
		});

		View widgetNotificationCloseButton = findViewById(R.id.widget_notification_close_btn);
		widgetNotificationCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.widget_notification_bar_layout).setVisibility(View.GONE);
				mIsWidgetNotificationShowing = false;
			}
		});

		// Animate it or not (depending if it's first being shown)
		if (mIsWidgetNotificationShowing) {
			widgetNotificationBar.setVisibility(View.VISIBLE);
		}
		else if (shouldShowWidget) {
			animateWidgetNotification(widgetNotificationBar);
		}
	}

	private void animateWidgetNotification(final View widgetNotificationBarLayout) {
		View searchBarLayout = findViewById(R.id.search_bar_layout);
		searchBarLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

		// animate the notification bar from above the search bar into its place
		// NOTE: If the animation moves the layout or if we intend to set FillAfterEnabled,
		// the layout parameters of the button will have to be updated to reflect where
		// the clickabe region is as the animation only redraws the view without 
		// actually moving the button
		TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, -searchBarLayout.getMeasuredHeight(),
				Animation.ABSOLUTE, 0.0f);
		animation.setDuration(WIDGET_NOTIFICATION_BAR_ANIMATION_DURATION);
		animation.setStartOffset(WIDGET_NOTIFICATION_BAR_ANIMATION_DELAY);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				widgetNotificationBarLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				widgetNotificationBarLayout.setVisibility(View.VISIBLE);
				// set it in stone that the notification was shown so that
				// its never shown again to the user
				markWidgetNotificationAsShown();
				mIsWidgetNotificationShowing = true;
			}
		});
		widgetNotificationBarLayout.startAnimation(animation);
	}

	private void broadcastSearchStarted() {
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

			hideSortOptions();

			mRefinementDismissView.setVisibility(View.GONE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);
			break;
		}
		case KEYBOARD: {
			showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
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

			hideSortOptions();

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

			hideSortOptions();

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

			hideSortOptions();

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

			showSortOptions();

			mRefinementDismissView.setVisibility(View.GONE);
			mButtonBarLayout.setVisibility(View.GONE);
			mDatesLayout.setVisibility(View.GONE);
			mGuestsLayout.setVisibility(View.GONE);

			break;
		}

		}

		setSearchEditViews();
		displayRefinementInfo();
		setActionBarBookingInfoText();
		setFilterInfoText();
	}

	private void openPanel(boolean toOpen, boolean animate) {
		mPanel.setOpen(toOpen, animate);

		if (toOpen) {
			rotateFilterArrowDown(animate);
		}
		else {
			rotateFilterArrowUp(animate);
		}
	}

	private void switchResultsView() {
		setDisplayType(DisplayType.NONE);
		mViewButton.setEnabled(false);

		String newFragmentTag = null;
		Rotate3dAnimation animationOut = null;
		Rotate3dAnimation animationIn = null;

		final float centerX = mViewFlipImage.getWidth() / 2.0f;
		final float centerY = mViewFlipImage.getHeight() / 2.0f;

		if (mTag.equals(mHotelListFragment.getTag())) {
			newFragmentTag = mHotelMapFragment.getTag();

			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, -90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}

			onSwitchToMap();
		}
		else {
			newFragmentTag = mHotelListFragment.getTag();

			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, 90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(-90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}

			Tracker.trackAppHotelsSearch(this, Db.getSearchParams(), Db.getSearchResponse(), null);
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
			if (newFragmentTag != null) {
				showFragment(newFragmentTag);
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
	// VIEW BUILDING METHODS
	//----------------------------------

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

		if (mSortPopup.isShowing()) {
			return;
		}

		Animation rotateAnimation = AnimationUtils.loadAnimation(PhoneSearchActivity.this, R.anim.rotate_down);
		mUpArrowSortHotels.startAnimation(rotateAnimation);

		mSortPopup.showAsDropDown(mSortButton, ((mSortButton.getWidth() - mSortOptionsLayout.getMeasuredWidth()) / 2),
				0);
		mSortPopupDismissView.setVisibility(View.VISIBLE);
		hideFilterOptions();
	}

	private void hideSortOptions() {
		if (!mSortPopup.isShowing()) {
			return;
		}

		mDisplayType = DisplayType.NONE;
		mSortPopup.dismiss();
		mUpArrowSortHotels.startAnimation(AnimationUtils.loadAnimation(PhoneSearchActivity.this, R.anim.rotate_up));
	}

	private void showFilterOptions() {
		mPanel.setOpen(true, true);
		mPanel.setVisibility(View.VISIBLE);
		rotateFilterArrowDown();
		hideSortOptions();
	}

	private void rotateFilterArrowDown(boolean animate) {
		mFilterButtonArrowUp = false;
		Animation animation = AnimationUtils.loadAnimation(PhoneSearchActivity.this, R.anim.rotate_down);
		if (!animate) {
			animation.setDuration(0);
		}
		mUpArrowFilterHotels.startAnimation(animation);
	}

	private void rotateFilterArrowDown() {
		rotateFilterArrowDown(true);
	}

	private void hideFilterOptions() {
		hideFilterOptions(true);
	}

	private void hideFilterOptions(boolean animate) {
		if (!mPanel.isOpen()) {
			return;
		}

		mPanel.setOpen(false, animate);
		rotateFilterArrowUp(animate);
	}

	private void rotateFilterArrowUp(boolean animate) {
		if (mFilterButtonArrowUp) {
			return;
		}

		mFilterButtonArrowUp = true;
		Animation animation = AnimationUtils.loadAnimation(PhoneSearchActivity.this, R.anim.rotate_up);
		if (!animate) {
			animation.setDuration(0);
		}
		mUpArrowFilterHotels.startAnimation(animation);
	}

	private View addSortOption(int sortOptionId, int sortOptionImageResId, int sortOptionTextResId, int flags) {
		View sortOption = getLayoutInflater().inflate(R.layout.snippet_sort_option, null);
		TextView sortTextView = (TextView) sortOption.findViewById(R.id.sort_option_text);
		ImageView sortImageView = (ImageView) sortOption.findViewById(R.id.sort_option_image);
		View sortOptionDivider = sortOption.findViewById(R.id.sort_option_divider);

		sortOption.setId(sortOptionId);
		sortTextView.setText(sortOptionTextResId);
		sortImageView.setImageResource(sortOptionImageResId);

		if ((flags & F_NO_DIVIDERS) != 0) {
			sortOptionDivider.setVisibility(View.GONE);
		}
		else {
			sortOptionDivider.setBackgroundDrawable(mSortOptionDivider);
		}

		if ((flags & F_FIRST) != 0) {
			sortOption.setBackgroundResource(R.drawable.bg_sort_option_row_first);
		}
		else if ((flags & F_LAST) != 0) {
			sortOption.setBackgroundResource(R.drawable.bg_sort_option_row_last);
		}

		mSortOptionsLayout.addView(sortOption);
		return sortOption;
	}

	private void setupSortOptions() {
		// Set all buttons to default state
		for (View v : mSortButtons) {
			v.setSelected(false);
			v.setEnabled(true);
		}

		// Setup current sort as selected and disabled
		View selected = null;
		switch (mSortOptionSelectedId) {
		case R.id.sort_popular_button: {
			selected = mSortPopularityButton;
			break;
		}
		case R.id.sort_reviews_button: {
			selected = mSortUserRatingButton;
			break;
		}
		case R.id.sort_distance_button: {
			selected = mSortDistanceButton;
			break;
		}
		case R.id.sort_price_button: {
			selected = mSortPriceButton;
			break;
		}
		}

		if (selected != null) {
			selected.setSelected(true);
			selected.setEnabled(false);
		}
	}

	//----------------------------------
	// STORE/RESTORE SEARCH PARAMS
	//----------------------------------

	private void restoreSearchParams() {
		if (mOriginalSearchParams != null) {
			Db.setSearchParams(mOriginalSearchParams.copy());
			mOriginalSearchParams = null;
		}
	}

	private void storeSearchParams() {
		if (mOriginalSearchParams == null) {
			mOriginalSearchParams = Db.getSearchParams().copy();
		}
	}

	//----------------------------------
	// VIEW ATTRIBUTE METHODS
	//----------------------------------

	private void setActionBarBookingInfoText() {
		int startDay = mDatesCalendarDatePicker.getStartDayOfMonth();
		int numAdults = Db.getSearchParams().getNumAdults();
		int numChildren = Db.getSearchParams().getNumChildren();

		mDatesTextView.setText(String.valueOf(startDay));
		mGuestsTextView.setText(String.valueOf((numAdults + numChildren)));
	}

	private void setDrawerViews() {
		Filter filter = Db.getFilter();

		Log.d("setDrawerViews().  Current filter: " + filter.toJson().toString());

		// Temporarily remove Listeners before we start fiddling around with the filter fields
		mFilterHotelNameEditText.removeTextChangedListener(mFilterHotelNameTextWatcher);
		mRadiusButtonGroup.setOnCheckedChangeListener(null);
		mPriceButtonGroup.setOnCheckedChangeListener(null);

		// Configure the hotel name filter
		mFilterHotelNameEditText.setText(filter.getHotelName());

		// Configure the sort buttons
		switch (filter.getSort()) {
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

		// Configure the rating buttons
		double minStarRating = filter.getMinimumStarRating();
		if (minStarRating >= 5) {
			mRatingButtonGroup.check(R.id.rating_high_button);
		}
		else if (minStarRating >= 4) {
			mRatingButtonGroup.check(R.id.rating_medium_button);
		}
		else if (minStarRating >= 3) {
			mRatingButtonGroup.check(R.id.rating_low_button);
		}
		else {
			mRatingButtonGroup.check(R.id.rating_all_button);
		}

		// Configure the search radius buttons
		switch (filter.getSearchRadius()) {
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
		switch (filter.getPriceRange()) {
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
		SearchResponse searchResponse = Db.getSearchResponse();
		if (searchResponse != null && searchResponse.getPropertiesCount() > 0 && !searchResponse.hasErrors()) {
			final int count = searchResponse.getFilteredAndSortedProperties().length;
			final String text = getResources().getQuantityString(R.plurals.number_of_matching_hotels, count, count);

			mFilterInfoTextView.setText(Html.fromHtml(text));
			mFilterInfoTextView.setVisibility(View.VISIBLE);
		}
		else {
			mFilterInfoTextView.setVisibility(View.GONE);
		}
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

	private void displayRefinementInfo() {
		if (mDisplayType == DisplayType.CALENDAR) {
			mRefinementInfoTextView.setText(CalendarUtils.getCalendarDatePickerTitle(this, mDatesCalendarDatePicker));
		}
		else if (mDisplayType == DisplayType.GUEST_PICKER) {
			SearchParams searchParams = Db.getSearchParams();
			final int numAdults = searchParams.getNumAdults();
			final int numChildren = searchParams.getNumChildren();
			mRefinementInfoTextView.setText(StrUtils.formatGuests(this, numAdults, numChildren));

			mChildAgesLayout.setVisibility(numChildren == 0 ? View.GONE : View.VISIBLE);
			mSelectChildAgeTextView.setText(getResources().getQuantityString(R.plurals.select_each_childs_age,
					numChildren));

			GuestsPickerUtils.showOrHideChildAgeSpinners(PhoneSearchActivity.this, searchParams.getChildren(),
					mChildAgesLayout, mChildAgeSelectedListener);
		}
		else {
			mRefinementInfoTextView.setText(null);
		}
	}

	private void setSearchEditViews() {
		SearchParams searchParams = Db.getSearchParams();
		switch (searchParams.getSearchType()) {
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

		mSearchEditText.setText(searchParams.getSearchDisplayText(this));
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

		// Ensure that our checkin/checkout dates match the calendar date picker (the calendar stay may have
		// changed due to enforcing min/max dates).
		syncDatesFromPicker();

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		mGuestsLayout.post(new Runnable() {
			@Override
			public void run() {
				SearchParams searchParams = Db.getSearchParams();
				int numAdults = searchParams.getNumAdults();
				int numChildren = searchParams.getNumChildren();
				mAdultsNumberPicker.setRange(numAdults, numAdults);
				mChildrenNumberPicker.setRange(numChildren, numChildren);
				GuestsPickerUtils.configureAndUpdateDisplayedValues(PhoneSearchActivity.this, mAdultsNumberPicker,
						mChildrenNumberPicker);
			}
		});

		setActionBarBookingInfoText();
	}

	// #13072: As you can tell, there's very little "determined" here nowadays. 
	// The logic has changed such that we should basically always show distance
	// (except when we completely lack this information).  I've kept this method
	// in anticipation of someday needing it back again (once we can suss out
	// the difference between autocomplete searches and geocodes).
	private void determineWhetherExactLocationSpecified(Address location) {
		Log.d("determineWhetherExactLocationSpecified(): " + location);
		setShowDistance(true);
	}

	private void setShowDistance(boolean showDistance) {
		mShowDistance = showDistance;

		int visibility = mShowDistance ? View.VISIBLE : View.GONE;

		// #9585: Need to adjust the drawable for the bottom popup item
		int numSortButtons = mSortButtons.size();
		View lastSortButton = mSortButtons.get(numSortButtons - 1);
		if (lastSortButton == mSortDistanceButton) {
			int secondToLastDrawable = mShowDistance ? R.drawable.bg_sort_option_row
					: R.drawable.bg_sort_option_row_last;
			mSortButtons.get(numSortButtons - 2).setBackgroundResource(secondToLastDrawable);
		}

		// Hide/reveal the sort-by-distance popup option
		mSortDistanceButton.setVisibility(visibility);
		mSortDistanceButton.findViewById(R.id.sort_option_divider).setVisibility(visibility);
		mSortOptionsLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mSortPopup.setWidth(mSortOptionsLayout.getMeasuredWidth());
		mSortPopup.setHeight(mSortOptionsLayout.getMeasuredHeight());

		mHotelListFragment.setShowDistances(showDistance);
		mHotelMapFragment.setShowDistances(showDistance);

		// Hide/reveal the distance filter
		mRadiusButtonGroup.setVisibility(visibility);
	}

	private void setSortTypeText() {
		String sortType = getString(Db.getFilter().getSort().getDescriptionResId());
		mSortTextView.setText(Html.fromHtml(getString(R.string.sort_hotels_template, sortType)));
	}

	private void setViewButtonImage() {
		if (mTag.equals(mHotelListFragment.getTag())) {
			mViewButton.setImageResource(R.drawable.btn_actionbar_map);
		}
		else {
			mViewButton.setImageResource(R.drawable.btn_actionbar_list);
		}
	}

	private void setBottomBarOptions() {
		if (mTag.equals(mHotelListFragment.getTag())) {
			mSortButton.setVisibility(View.VISIBLE);
			mSearchMapButton.setVisibility(View.GONE);
		}
		else {
			mSortButton.setVisibility(View.GONE);
			mSearchMapButton.setVisibility(View.VISIBLE);
		}
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
			mSearchTextSelectionStart = mSearchTextSelectionEnd = -1;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String str = s.toString();
			int len = s.length();
			boolean changed = false;
			SearchParams searchParams = Db.getSearchParams();
			if (str.equals(getString(R.string.current_location)) || len == 0) {
				changed |= searchParams.setSearchType(SearchType.MY_LOCATION);
				changed |= searchParams.setFreeformLocation(getString(R.string.current_location));
				searchParams.setSearchLatLonUpToDate();
			}
			else if (str.equals(getString(R.string.visible_map_area))) {
				changed |= searchParams.setSearchType(SearchType.PROXIMITY);
				searchParams.setSearchLatLonUpToDate();
			}
			else {
				changed |= searchParams.setSearchType(SearchType.FREEFORM);
				changed |= searchParams.setFreeformLocation(str);
			}
			if (changed) {
				startAutocomplete();
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
			Filter filter = Db.getFilter();
			filter.setHotelName(s.toString());
			filter.notifyFilterChanged();
			setFilterInfoText();
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
		Uri uri = AutocompleteProvider.generateSearchUri(Db.getSearchParams().getFreeformLocation(), 50);
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

	private final AdapterView.OnItemClickListener mSearchSuggestionsItemClickListner = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Cursor c = mSearchSuggestionAdapter.getCursor();

			if (c.getString(AutocompleteProvider.COLUMN_TEXT_INDEX).equals(getString(R.string.current_location))) {
				Db.getSearchParams().setSearchType(SearchType.MY_LOCATION);
			}
			else {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PhoneSearchActivity.this);
				SearchParams searchParams = new SearchParams(prefs);
				Db.setSearchParams(searchParams);

				Object o = AutocompleteProvider.extractSearchOrString(c);

				if (o instanceof Search) {
					searchParams.fillFromSearch((Search) o);
				}
				else {
					searchParams.setFreeformLocation(o.toString());
				}
			}

			setDisplayType(DisplayType.CALENDAR);
		}
	};

	private final CalendarDatePicker.OnDateChangedListener mDatesDateChangedListener = new CalendarDatePicker.OnDateChangedListener() {
		@Override
		public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
			if (mOriginalSearchParams != null) {
				syncDatesFromPicker();
			}

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

		SearchParams searchParams = Db.getSearchParams();
		searchParams.setCheckInDate(startCalendar);
		searchParams.setCheckOutDate(endCalendar);
	}

	private final NumberPicker.OnChangedListener mNumberPickerChangedListener = new NumberPicker.OnChangedListener() {
		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {
			int numAdults = mAdultsNumberPicker.getCurrent();
			int numChildren = mChildrenNumberPicker.getCurrent();
			SearchParams searchParams = Db.getSearchParams();
			searchParams.setNumAdults(numAdults);
			GuestsPickerUtils.resizeChildrenList(PhoneSearchActivity.this, searchParams.getChildren(), numChildren);
			GuestsPickerUtils.configureAndUpdateDisplayedValues(mContext, mAdultsNumberPicker, mChildrenNumberPicker);
			displayRefinementInfo();
			setActionBarBookingInfoText();
		}
	};

	private final OnItemSelectedListener mChildAgeSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			List<Integer> children = Db.getSearchParams().getChildren();
			GuestsPickerUtils.setChildrenFromSpinners(PhoneSearchActivity.this, mChildAgesLayout, children);
			GuestsPickerUtils.updateDefaultChildAges(PhoneSearchActivity.this, children);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	};

	private final Panel.OnPanelListener mPanelListener = new Panel.OnPanelListener() {
		@Override
		public void onPanelOpened(Panel panel) {
			mDisplayType = DisplayType.DRAWER;

			if (mPanelDismissView.getVisibility() == View.VISIBLE) {
				return;
			}
			final Animation animation = AnimationUtils.loadAnimation(PhoneSearchActivity.this, android.R.anim.fade_in);
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
			mDisplayType = DisplayType.NONE;

			final Animation animation = AnimationUtils.loadAnimation(PhoneSearchActivity.this, android.R.anim.fade_out);
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
			if (!mFilterButtonArrowUp) {
				rotateFilterArrowUp(false);
			}

			// Get rid of IME if it appeared for the filter
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mFilterHotelNameEditText.getWindowToken(), 0);
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
			buildFilter();
			setupSortOptions();
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
			SearchParams searchParams = Db.getSearchParams();
			searchParams.invalidateFreeformLocation();

			if (mHotelMapFragment != null) {
				GeoPoint center = mHotelMapFragment.getCenter();
				searchParams.setSearchType(SearchType.PROXIMITY);

				double lat = MapUtils.getLatitude(center);
				double lng = MapUtils.getLongitude(center);
				searchParams.setSearchLatLon(lat, lng);
				setShowDistance(true);
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

	private final View.OnClickListener mSortPopupDismissViewClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			hideSortOptions();
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

				if (Db.getSearchParams().getSearchType() != SearchType.FREEFORM) {
					mSearchEditText.post(new Runnable() {
						@Override
						public void run() {
							mSearchEditText.setText(null);
							mSearchEditText.setTextColor(getResources().getColor(android.R.color.black));
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
			if (mSortPopup.isShowing()) {
				setDisplayType(DisplayType.NONE);
			}
			else {
				setDisplayType(DisplayType.SORT_POPUP);
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

	//////////////////////////////////////////////////////////////////////////
	// Fragment tabs

	public void showFragment(String tag) {
		Log.d("Showing fragment with tag: " + tag);

		if (tag.equals(mHotelListFragment.getTag())) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mHotelListFragment);
			ft.hide(mHotelMapFragment);
			ft.commit();

			mTag = mHotelListFragment.getTag();
		}
		else {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mHotelMapFragment);
			ft.hide(mHotelListFragment);
			ft.commit();

			mTag = mHotelMapFragment.getTag();
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

		Intent intent = new Intent(this, HotelActivity.class);
		intent.putExtra(HotelActivity.EXTRA_POSITION, position);
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

		Intent intent = new Intent(this, HotelActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener

	@Override
	public void onFilterChanged() {
		mHotelListFragment.notifyFilterChanged();
		mHotelMapFragment.notifyFilterChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// FragmentMapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
