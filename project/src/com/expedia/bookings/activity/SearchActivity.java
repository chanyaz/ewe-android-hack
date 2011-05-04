package com.expedia.bookings.activity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.Rotate3dAnimation;
import com.expedia.bookings.dialog.LocationSuggestionDialog;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.tracking.TrackingData;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.widget.SearchSuggestionAdapter;
import com.expedia.bookings.widget.TagProgressBar;
import com.google.android.maps.GeoPoint;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.SelectionMode;
import com.mobiata.android.widget.NumberPicker;
import com.mobiata.android.widget.Panel;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.PriceRange;
import com.mobiata.hotellib.data.Filter.Rating;
import com.mobiata.hotellib.data.Filter.SearchRadius;
import com.mobiata.hotellib.data.Filter.Sort;
import com.mobiata.hotellib.data.JSONable;
import com.mobiata.hotellib.data.PriceTier;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.ServerError;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.StrUtils;
import com.omniture.AppMeasurement;

@SuppressWarnings("unused")
public class SearchActivity extends ActivityGroup implements LocationListener {
	public interface MapViewListener {
		public GeoPoint onRequestMapCenter();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String ACTIVITY_SEARCH_LIST = SearchListActivity.class.getCanonicalName();
	public static final String ACTIVITY_SEARCH_MAP = SearchMapActivity.class.getCanonicalName();

	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final String KEY_ACTIVITY_STATE = "KEY_ACTIVITY_STATE";

	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;
	private static final int DIALOG_CLIENT_DEPRECATED = 1;

	private static final int REQUEST_CODE_SETTINGS = 1;

	private static final int MSG_SWITCH_TO_NETWORK_LOCATION = 0;
	private static final int MSG_BROADCAST_SEARCH_COMPLETED = 1;
	private static final int MSG_BROADCAST_SEARCH_FAILED = 2;
	private static final int MSG_BROADCAST_SEARCH_STARTED = 3;

	private static final long TIME_SWITCH_TO_NETWORK_DELAY = 1000;

	private static final boolean ANIMATION_VIEW_FLIP_ENABLED = true;
	private static final long ANIMATION_VIEW_FLIP_SPEED = 350;
	private static final float ANIMATION_VIEW_FLIP_DEPTH = 300f;

	private static final long ANIMATION_PANEL_DISMISS_SPEED = 150;

	private static final int MAX_GUESTS_TOTAL = 5;
	private static final int MAX_GUEST_NUM = 4;

	private static final int DEFAULT_SORT_RADIO_GROUP_CHILD = 0;
	private static final int DEFAULT_RADIUS_RADIO_GROUP_CHILD = 2;
	private static final int DEFAULT_PRICE_RADIO_GROUP_CHILD = 3;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	// Views

	private View mFocusLayout;

	private FrameLayout mContent;
	private ImageView mViewFlipImage;

	private ImageButton mMapSearchButton;
	private ImageButton mViewButton;

	private TextView mBookingInfoTextView;
	private EditText mSearchEditText;
	private ImageButton mDatesButton;
	private TextView mDatesTextView;
	private ImageButton mGuestsButton;
	private TextView mGuestsTextView;

	private View mPanelDismissView;
	private Panel mPanel;

	private View mSortLayout;
	private Button mTripAdvisorOnlyButton;
	private SegmentedControlGroup mSortButtonGroup;
	private SegmentedControlGroup mRadiusButtonGroup;
	private TextView mPriceRangeTextView;
	private SegmentedControlGroup mPriceButtonGroup;

	private View mRefinementDismissView;
	private ListView mSearchSuggestionsListView;

	private View mDatesLayout;
	private CalendarDatePicker mDatesCalendarDatePicker;
	private View mGuestsLayout;
	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;

	private View mButtonBarLayout;
	private TextView mRefinementInfoTextView;
	private Button mSearchButton;

	private TagProgressBar mSearchProgressBar;

	// Others

	private Context mContext = this;

	private LocalActivityManager mLocalActivityManager;
	private String mTag;
	private Intent mIntent;
	private View mLaunchedView;

	private List<SearchListener> mSearchListeners;
	private MapViewListener mMapViewListener;

	private List<Address> mAddresses;
	private SearchParams mSearchParams;
	private SearchParams mOldSearchParams;
	private Session mSession;
	private SearchResponse mSearchResponse;
	private Map<PriceRange, PriceTier> mPriceTierCache;
	private Filter mFilter;
	private Filter mOldFilter;
	private boolean mLocationListenerStarted;
	private boolean mIsSearching;
	private boolean mScreenRotationLocked;

	private Thread mGeocodeThread;

	private SearchSuggestionAdapter mSearchSuggestionAdapter;

	private boolean mDatesLayoutIsVisible;
	private boolean mGuestsLayoutIsVisible;
	private boolean mButtonBarIsVisible;

	private Bitmap mViewFlipBitmap;
	private Canvas mViewFlipCanvas;

	private LocationSuggestionDialog mLocationSuggestionDialog;

	// Threads / callbacks

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mSession);
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
				mSession = mSearchResponse.getSession();

				if (mSearchResponse.getFilteredAndSortedProperties().length <= 10) {
					final int index = mRadiusButtonGroup.getChildCount() - 1;
					((RadioButton) mRadiusButtonGroup.getChildAt(index)).setChecked(true);
				}

				ImageCache.getInstance().recycleCache(true);
				broadcastSearchCompleted(mSearchResponse);

				buildPriceTierCache();
				enablePanelHandle();
				hideLoading();
				setPriceRangeText();

			}
			else if (mSearchResponse != null && mSearchResponse.getLocations() != null
					&& mSearchResponse.getLocations().size() > 0) {

				mSearchProgressBar.setShowProgress(false);
				mSearchProgressBar.setText(null);
				showDialog(DIALOG_LOCATION_SUGGESTIONS);
			}
			else {
				// Handling for particular errors
				boolean handledError = false;
				if (mSearchResponse != null && mSearchResponse.hasErrors()) {
					ServerError errorOne = mSearchResponse.getErrors().get(0);
					if (errorOne.getCode().equals("01")) {
						// Deprecated client version
						showDialog(DIALOG_CLIENT_DEPRECATED);

						TrackingUtils.trackErrorPage(mContext, "OutdatedVersion");

						mSearchProgressBar.setShowProgress(false);
						mSearchProgressBar.setText(errorOne.getExtra("message"));
					}
					else {
						mSearchProgressBar.setShowProgress(false);
						mSearchProgressBar.setText(errorOne.getPresentableMessage(mContext));
					}
					handledError = true;
				}

				if (!handledError) {
					TrackingUtils.trackErrorPage(mContext, "HotelListRequestFailed");
					mSearchProgressBar.setShowProgress(false);
					mSearchProgressBar.setText(R.string.progress_search_failed);
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onPageLoad();
		setContentView(R.layout.activity_search);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {
			extractActivityState(state);
			initializeViews();

			setActivity(SearchMapActivity.class);
			setActivity(SearchListActivity.class);

			if (state.tag != null) {
				setActivityByTag(state.tag);
			}

			if (mSearchResponse != null) {
				if (mFilter != null) {
					mSearchResponse.setFilter(mFilter);
				}
				broadcastSearchCompleted(mSearchResponse);
			}

			if (state.guestsLayoutIsVisible) {
				showGuestsLayout();
			}
			else if (state.datesLayoutIsVisible) {
				showDatesLayout();
			}
			else if (state.panelIsOpen) {
				mPanel.setOpen(true, false);
				mPanelDismissView.setVisibility(View.VISIBLE);
			}
		}
		else {
			String searchParamsJson = SettingUtils.get(this, "searchParams", null);
			String filterJson = SettingUtils.get(this, "filter", null);
			String tag = SettingUtils.get(this, "tag", null);

			if (searchParamsJson != null) {
				try {
					JSONObject obj = new JSONObject(searchParamsJson);
					mSearchParams = new SearchParams(obj);
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

			mSearchSuggestionAdapter = new SearchSuggestionAdapter(this);
			mLocalActivityManager = getLocalActivityManager();

			initializeViews();

			mAdultsNumberPicker.setTextEnabled(false);
			mChildrenNumberPicker.setTextEnabled(false);
			mAdultsNumberPicker.setRange(1, 4);
			mChildrenNumberPicker.setRange(0, 4);
			mAdultsNumberPicker.setCurrent(mSearchParams.getNumAdults());
			mChildrenNumberPicker.setCurrent(mSearchParams.getNumChildren());
			setNumberPickerRanges();

			setActivity(SearchMapActivity.class);
			setActivity(SearchListActivity.class);
			if (tag != null) {
				setActivityByTag(tag);
			}

			startSearch();
		}

		mSearchSuggestionsListView.setAdapter(mSearchSuggestionAdapter);
	}

	@Override
	protected void onDestroy() {
		SettingUtils.save(this, "searchParams", mSearchParams.toJson().toString());
		SettingUtils.save(this, "filter", mFilter.toJson().toString());
		SettingUtils.save(this, "tag", mTag);

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		setMapSearchButtonVisibility();
		setViewButtonImage();
		setDrawerViews();
		setSearchEditViews();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return buildActivityState();
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

	// Dialogs

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOCATION_SUGGESTIONS: {
			final int size = mAddresses.size();
			final CharSequence[] freeformLocations = new CharSequence[mAddresses.size()];
			for (int i = 0; i < size; i++) {
				freeformLocations[i] = LocationServices.formatAddress(mAddresses.get(i));
			}

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.ChooseLocation);
			builder.setItems(freeformLocations, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Address address = mAddresses.get(which);
					mSearchParams.setFreeformLocation(LocationServices.formatAddress(address));
					setSearchEditViews();

					setSearchParams(address.getLatitude(), address.getLongitude());

					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearchDownloader();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					mSearchProgressBar.setShowProgress(false);
					mSearchProgressBar.setText(getString(R.string.NoGeocodingResults,
							mSearchParams.getFreeformLocation()));
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					mSearchProgressBar.setShowProgress(false);
					mSearchProgressBar.setText(getString(R.string.NoGeocodingResults,
							mSearchParams.getFreeformLocation()));
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
					SocialUtils.openSite(mContext, error.getExtra("url"));
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
		}

		return super.onCreateDialog(id);
	}

	// Menus

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

	// Key events

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mDatesLayoutIsVisible) {
				hideDatesLayout();
				hideRefinementDismissView();
				hideButtonBar();
				return true;
			}

			if (mGuestsLayoutIsVisible) {
				hideGuestsLayout();
				hideRefinementDismissView();
				hideButtonBar();
				return true;
			}

			if (mSearchEditText.hasFocus() && mButtonBarIsVisible) {
				resetFocus();
				return true;
			}

			if (mPanel.isOpen()) {
				closeDrawer();
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	// Location listener implementation

	@Override
	public void onLocationChanged(Location location) {
		setSearchParams(location.getLatitude(), location.getLongitude());
		startSearchDownloader();

		stopLocationListener();
	}

	@Override
	public void onProviderDisabled(String provider) {
		stopLocationListener();
		mSearchProgressBar.setShowProgress(false);
		mSearchProgressBar.setText(R.string.ProviderDisabled);
		TrackingUtils.trackErrorPage(this, "LocationServicesNotAvailable");
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
			scheduleSwitchToNetworkLocation();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			stopLocationListener();
			Log.w("Location listener failed: out of service");
			mSearchProgressBar.setShowProgress(false);
			mSearchProgressBar.setText(R.string.ProviderOutOfService);
		}
		else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			stopLocationListener();
			Log.w("Location listener failed: temporarily unavailable");
			mSearchProgressBar.setShowProgress(false);
			mSearchProgressBar.setText(R.string.ProviderTemporarilyUnavailable);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public void addSearchListener(SearchListener searchListener) {
		if (mSearchListeners == null) {
			mSearchListeners = new ArrayList<SearchListener>();
		}

		if (!mSearchListeners.contains(searchListener)) {
			mSearchListeners.add(searchListener);
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

	public void setSearchParamsForFreeform() {
		showLoading(R.string.progress_searching_hotels);
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
							mSearchProgressBar.setShowProgress(false);
							mSearchProgressBar.setText(null);
							showDialog(DIALOG_LOCATION_SUGGESTIONS);
						}
						else if (mAddresses != null && mAddresses.size() > 0) {
							Address address = mAddresses.get(0);
							setSearchParams(address.getLatitude(), address.getLongitude());
							startSearchDownloader();
						}
						else {
							TrackingUtils.trackErrorPage(mContext, "LocationNotFound");
							mSearchProgressBar.setShowProgress(false);
							mSearchProgressBar.setText(R.string.geolocation_failed);
						}
					}
				});
			}
		});
		mGeocodeThread.start();
	}

	public void setSearchParams(Double latitde, Double longitude) {
		if (mSearchParams == null) {
			mSearchParams = new SearchParams();
		}

		mSearchParams.setSearchLatLon(latitde, longitude);

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
		mSearchParams.setNumAdults(mAdultsNumberPicker.getCurrent());
		mSearchParams.setNumChildren(mChildrenNumberPicker.getCurrent());
	}

	public void setSearchParams(SearchParams searchParams) {
		mSearchParams = searchParams;
	}

	public void startSearch() {
		mSearchDownloader.cancelDownload(KEY_SEARCH);

		buildFilter();
		setSearchEditViews();
		resetFocus();
		disablePanelHandle();

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
			startLocationListener();

			break;
		}
		}
	}

	public void switchResultsView() {
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
			setMapSearchButtonVisibility();

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
			mViewFlipImage.startAnimation(animationOut);
		}
		else {
			if (newActivityClass != null) {
				setActivity(newActivityClass);
			}
			setDrawerViews();
			setMapSearchButtonVisibility();
			setViewButtonImage();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	// Activity State stuff

	private ActivityState buildActivityState() {
		ActivityState state = new ActivityState();
		state.handler = mHandler;
		state.tag = mTag;
		state.searchListeners = mSearchListeners;
		state.searchParams = mSearchParams;
		state.oldSearchParams = mOldSearchParams;
		state.searchResponse = mSearchResponse;
		state.priceTierCache = mPriceTierCache;
		state.session = mSession;
		state.filter = mFilter;
		state.oldFilter = mOldFilter;
		state.searchSuggestionAdapter = mSearchSuggestionAdapter;
		state.isSearching = mIsSearching;
		state.searchDownloader = mSearchDownloader;
		state.datesLayoutIsVisible = mDatesLayoutIsVisible;
		state.guestsLayoutIsVisible = mGuestsLayoutIsVisible;
		state.panelIsOpen = mPanel.isOpen();

		return state;
	}

	private void extractActivityState(ActivityState state) {
		mHandler = state.handler;
		mSearchListeners = state.searchListeners;
		mSearchParams = state.searchParams;
		mOldSearchParams = state.oldSearchParams;
		mSearchResponse = state.searchResponse;
		mPriceTierCache = state.priceTierCache;
		mSession = state.session;
		mFilter = state.filter;
		mOldFilter = state.oldFilter;
		mSearchSuggestionAdapter = state.searchSuggestionAdapter;
		mIsSearching = state.isSearching;
		mSearchDownloader = state.searchDownloader;
		mLocalActivityManager = getLocalActivityManager();
	}

	// Broadcast methods

	private void broadcastSearchStarted() {
		mIsSearching = true;

		if (mSearchListeners != null) {
			for (SearchListener searchListener : mSearchListeners) {
				searchListener.onSearchStarted();
			}
		}
	}

	private void broadcastSearchFailed(String message) {
		mIsSearching = false;
		//mIsShowingMessage = true;

		if (mSearchListeners != null) {
			for (SearchListener searchListener : mSearchListeners) {
				searchListener.onSearchFailed(message);
			}
		}
	}

	private void broadcastSearchCompleted(SearchResponse searchResponse) {
		mIsSearching = false;
		mSearchResponse = searchResponse;

		if (mSearchListeners != null) {
			for (SearchListener searchListener : mSearchListeners) {
				searchListener.onSearchCompleted(searchResponse);
			}
		}

		onSearchResultsChanged();
	}

	// Show/hide soft keyboard

	private void hideSoftKeyboard(TextView v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		hideRefinementDismissView();
	}

	private void showSoftKeyboard(View view) {
		showSoftKeyboard(view, null);
	}

	private void showSoftKeyboard(View view, ResultReceiver resultReceiver) {
		Configuration config = getResources().getConfiguration();
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, resultReceiver);
		}

		showDismissView();
		closeDrawer();
	}

	// Show/hide view methods

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

	private void closeDrawer() {
		mPanel.setOpen(false, true);
	}

	private void disablePanelHandle() {
		mPanel.getHandle().setEnabled(false);
	}

	private void enablePanelHandle() {
		mPanel.getHandle().setEnabled(true);
	}

	private void hideButtonBar() {
		mButtonBarIsVisible = false;
		mButtonBarLayout.setVisibility(View.GONE);
	}

	private void hideDatesLayout() {
		mDatesLayoutIsVisible = false;
		clearRefinementInfo();
		mDatesLayout.setVisibility(View.GONE);
	}

	private void hideRefinementDismissView() {
		mRefinementDismissView.setVisibility(View.GONE);
	}

	private void hideGuestsLayout() {
		mGuestsLayoutIsVisible = false;
		clearRefinementInfo();
		mGuestsLayout.setVisibility(View.GONE);
	}

	private void hideLoading() {
		unlockScreenRotation();
		mSearchProgressBar.setVisibility(View.GONE);
	}

	private void hideSearchSuggestions() {
		mSearchSuggestionsListView.setVisibility(View.GONE);
	}

	private void showButtonBar() {
		mButtonBarIsVisible = true;
		mButtonBarLayout.setVisibility(View.VISIBLE);
	}

	private void showDatesLayout() {
		resetFocus();
		mDatesLayout.setVisibility(View.VISIBLE);
		mDatesCalendarDatePicker.requestFocus();
		showDismissView();
		closeDrawer();
		showButtonBar();

		mDatesLayoutIsVisible = true;
		setRefinementInfo();
	}

	private void showDismissView() {
		mRefinementDismissView.setVisibility(View.VISIBLE);
	}

	private void showGuestsLayout() {
		resetFocus();
		mGuestsLayout.setVisibility(View.VISIBLE);
		mAdultsNumberPicker.requestFocus();
		showDismissView();
		closeDrawer();
		showButtonBar();

		mGuestsLayoutIsVisible = true;
		setRefinementInfo();
	}

	private void showLoading(int resId) {
		showLoading(getString(resId));
	}

	private void showLoading(String text) {
		lockScreenRotation();
		mSearchProgressBar.setVisibility(View.VISIBLE);
		mSearchProgressBar.setShowProgress(true);
		mSearchProgressBar.setText(text);
	}

	private void showSearchSuggestions() {
		mSearchSuggestionsListView.setVisibility(View.VISIBLE);
	}

	// Other methods

	private void clearRefinementInfo() {
		mRefinementInfoTextView.setText("");
	}

	// Screen orientation

	private void lockScreenRotation() {
		if (!mScreenRotationLocked) {
			final int orientation = getWindowManager().getDefaultDisplay().getOrientation();
			switch (orientation) {
			case Surface.ROTATION_0: {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			}
			case Surface.ROTATION_90: {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			}
			case Surface.ROTATION_270: {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				break;
			}
			}

			mScreenRotationLocked = true;
		}
	}

	private void unlockScreenRotation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		mScreenRotationLocked = false;
	}

	///////////////////////////////////////////////////////////////////////
	// VIEW INITIALIZATION

	private void initializeViews() {
		// Get views
		mFocusLayout = findViewById(R.id.focus_layout);

		mContent = (FrameLayout) findViewById(R.id.content_layout);
		mViewFlipImage = (ImageView) findViewById(R.id.view_flip_image);

		mMapSearchButton = (ImageButton) findViewById(R.id.map_search_button);
		mViewButton = (ImageButton) findViewById(R.id.view_button);

		mBookingInfoTextView = (TextView) findViewById(R.id.booking_info_text_view);
		mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
		mDatesButton = (ImageButton) findViewById(R.id.dates_button);
		mDatesTextView = (TextView) findViewById(R.id.dates_text_view);
		mGuestsButton = (ImageButton) findViewById(R.id.guests_button);
		mGuestsTextView = (TextView) findViewById(R.id.guests_text_view);

		mPanelDismissView = findViewById(R.id.panel_dismiss_view);
		mPanel = (Panel) findViewById(R.id.drawer_panel);

		mSortLayout = findViewById(R.id.sort_layout);
		mTripAdvisorOnlyButton = (Button) findViewById(R.id.tripadvisor_only_button);
		mSortButtonGroup = (SegmentedControlGroup) findViewById(R.id.sort_filter_button_group);
		mRadiusButtonGroup = (SegmentedControlGroup) findViewById(R.id.radius_filter_button_group);
		mPriceRangeTextView = (TextView) findViewById(R.id.price_range_text_view);
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
		mSearchButton = (Button) findViewById(R.id.search_button);

		mSearchProgressBar = (TagProgressBar) findViewById(R.id.search_progress_bar);

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

		Time now = new Time();
		now.setToNow();
		mDatesCalendarDatePicker.setSelectionMode(SelectionMode.RANGE);
		mDatesCalendarDatePicker.setMinDate(now.year, now.month, now.monthDay);
		mDatesCalendarDatePicker.setMaxRange(28);

		//===================================================================
		// Listeners
		mMapSearchButton.setOnClickListener(mMapSearchButtonClickListener);
		mViewButton.setOnClickListener(mViewButtonClickListener);

		mSearchEditText.setOnFocusChangeListener(mSearchEditTextFocusChangeListener);
		mSearchEditText.setOnClickListener(mSearchEditTextClickListener);
		mSearchEditText.setOnEditorActionListener(mSearchEditorActionListener);
		mSearchEditText.addTextChangedListener(mSearchEditTextTextWatcher);
		mDatesButton.setOnClickListener(mDatesButtonClickListener);
		mGuestsButton.setOnClickListener(mGuestsButtonClickListener);

		mPanelDismissView.setOnClickListener(mPanelDismissViewClickListener);
		mTripAdvisorOnlyButton.setOnClickListener(mTripAdvisorOnlyButtonClickListener);
		mSortButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);

		mRefinementDismissView.setOnClickListener(mRefinementDismissViewClickListener);
		mSearchSuggestionsListView.setOnItemClickListener(mSearchSuggestionsItemClickListner);

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);
		mAdultsNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mChildrenNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mSearchButton.setOnClickListener(mSearchButtonClickListener);
	}

	///////////////////////////////////////////////////////////////////////

	private void resetFilter() {
		mFilter = new Filter();

		((RadioButton) mSortButtonGroup.getChildAt(DEFAULT_SORT_RADIO_GROUP_CHILD)).setChecked(true);
		((RadioButton) mRadiusButtonGroup.getChildAt(DEFAULT_RADIUS_RADIO_GROUP_CHILD)).setChecked(true);
		((RadioButton) mPriceButtonGroup.getChildAt(DEFAULT_PRICE_RADIO_GROUP_CHILD)).setChecked(true);

		setDrawerViews();
		buildFilter();
	}

	private void resetFocus() {
		mFocusLayout.requestFocus();

		hideSoftKeyboard(mSearchEditText);
		hideRefinementDismissView();
		hideDatesLayout();
		hideGuestsLayout();
		hideButtonBar();
		hideSearchSuggestions();
	}

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

	private void setBookingInfoText() {
		final String location = mSearchEditText.getText().toString();
		final int startYear = mDatesCalendarDatePicker.getStartYear();
		final int startMonth = mDatesCalendarDatePicker.getStartMonth();
		final int startDay = mDatesCalendarDatePicker.getStartDayOfMonth();
		final int endYear = mDatesCalendarDatePicker.getEndYear();
		final int endMonth = mDatesCalendarDatePicker.getEndMonth();
		final int endDay = mDatesCalendarDatePicker.getEndDayOfMonth();
		final int adults = mSearchParams.getNumAdults();
		final int children = mSearchParams.getNumChildren();

		String[] shortMonthNames = getResources().getStringArray(R.array.short_month_names);

		mBookingInfoTextView.setText(getString(R.string.booking_info_template, location, shortMonthNames[startMonth],
				startDay, shortMonthNames[endMonth], endDay, endYear));
		mDatesTextView.setText(String.valueOf(startDay));
		mGuestsTextView.setText(String.valueOf((adults + children)));
	}

	private void setDrawerViews() {
		final Rating rating = mFilter.getRating();
		switch (rating) {
		case ALL: {
			mTripAdvisorOnlyButton.setText(R.string.tripadvisor_rating_high);
			break;
		}
		case HIGHLY_RATED: {
			mTripAdvisorOnlyButton.setText(R.string.tripadvisor_rating_all);
			break;
		}
		}

		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			mSortLayout.setVisibility(View.VISIBLE);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			mSortLayout.setVisibility(View.GONE);
		}

		setPriceRangeText();
		setRadioButtonShadowLayers();
	}

	private void setMapSearchButtonVisibility() {
		if (ANIMATION_VIEW_FLIP_ENABLED) {
			if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
				if (mMapSearchButton.getVisibility() == View.GONE) {
					return;
				}

				mMapSearchButton.setEnabled(false);

				final Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
				animation.setDuration(ANIMATION_VIEW_FLIP_SPEED * 2);
				animation.setInterpolator(new AccelerateDecelerateInterpolator());
				animation.setAnimationListener(new AnimationListener() {
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
								mMapSearchButton.setVisibility(View.GONE);
							}
						});
					}
				});
				mMapSearchButton.startAnimation(animation);
			}
			else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
				if (mMapSearchButton.getVisibility() == View.VISIBLE) {
					return;
				}

				mMapSearchButton.setEnabled(true);

				final Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
				animation.setDuration(ANIMATION_VIEW_FLIP_SPEED * 2);
				animation.setInterpolator(new AccelerateDecelerateInterpolator());
				animation.setAnimationListener(new AnimationListener() {
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
								mMapSearchButton.setVisibility(View.VISIBLE);
							}
						});
					}
				});
				mMapSearchButton.startAnimation(animation);
			}
		}
		else {
			if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
				mMapSearchButton.setVisibility(View.GONE);
			}
			else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
				mMapSearchButton.setVisibility(View.VISIBLE);
			}
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

	private void setPriceRangeText() {
		if (mPriceTierCache != null) {

			PriceRange priceRange = PriceRange.ALL;
			switch (mPriceButtonGroup.getCheckedRadioButtonId()) {
			case R.id.price_cheap_button: {
				priceRange = PriceRange.CHEAP;
				break;
			}
			case R.id.price_moderate_button: {
				priceRange = PriceRange.MODERATE;
				break;
			}
			case R.id.price_expensive_button: {
				priceRange = PriceRange.EXPENSIVE;
				break;
			}
			}

			PriceTier priceTier = mPriceTierCache.get(priceRange);
			if (priceTier != null) {
				int priceMin = (int) priceTier.getMinRate().getAmount();
				int priceMax = (int) priceTier.getMaxRate().getAmount();
				mPriceRangeTextView.setText(getString(R.string.price_range_template, priceMin, priceMax));
			}
		}
		else {
			mPriceRangeTextView.setText(null);
		}
	}

	private void setRadioButtonShadowLayers() {
		List<SegmentedControlGroup> groups = new ArrayList<SegmentedControlGroup>();
		groups.add(mSortButtonGroup);
		groups.add(mRadiusButtonGroup);
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
		if (mDatesLayoutIsVisible) {
			final int startYear = mDatesCalendarDatePicker.getStartYear();
			final int startMonth = mDatesCalendarDatePicker.getStartMonth();
			final int startDay = mDatesCalendarDatePicker.getStartDayOfMonth();

			final int endYear = mDatesCalendarDatePicker.getEndYear();
			final int endMonth = mDatesCalendarDatePicker.getEndMonth();
			final int endDay = mDatesCalendarDatePicker.getEndDayOfMonth();

			Date startDate = new Date(startYear, startMonth, startDay);
			Date endDate = new Date(endYear, endMonth, endDay);
			int nights = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
			nights = nights > 0 ? nights : 1;

			mRefinementInfoTextView.setText(getResources().getQuantityString(R.plurals.length_of_stay, nights, nights));
		}
		else if (mGuestsLayoutIsVisible) {
			final int adults = mAdultsNumberPicker.getCurrent();
			final int children = mChildrenNumberPicker.getCurrent();

			mRefinementInfoTextView.setText(StrUtils.formatGuests(mContext, adults, children));
		}

		setBookingInfoText();
	}

	private void setSearchEditViews() {
		if (mSearchParams == null) {
			mSearchParams = new SearchParams();
		}

		switch (mSearchParams.getSearchType()) {
		case FREEFORM: {
			mSearchEditText.setText(mSearchParams.getFreeformLocation());
			break;
		}
		case MY_LOCATION: {
			mSearchEditText.setText(R.string.current_location);
			mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
			break;
		}
		case PROXIMITY: {
			stopLocationListener();

			mSearchEditText.setText(R.string.visible_map_area);
			mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
			break;
		}
		}

		mAdultsNumberPicker.setCurrent(mSearchParams.getNumAdults());
		mChildrenNumberPicker.setCurrent(mSearchParams.getNumChildren());

		setBookingInfoText();
	}

	private void setViewButtonImage() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			mViewButton.setImageResource(R.drawable.btn_map);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			mViewButton.setImageResource(R.drawable.btn_list);
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

		setDrawerViews();
	}

	// Searching methods

	private void buildFilter() {
		if (mFilter == null) {
			mFilter = new Filter();
		}

		// Sort
		switch (mSortButtonGroup.getCheckedRadioButtonId()) {
		case R.id.sort_popular_button: {
			mFilter.setSort(Sort.POPULAR);
			break;
		}
		case R.id.sort_price_button: {
			mFilter.setSort(Sort.PRICE);
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
	}

	private void startSearchDownloader() {
		showLoading(R.string.progress_searching_hotels);
		if (mSearchParams.getSearchType() == SearchType.FREEFORM) {
			Search.add(this, mSearchParams);
			mSearchSuggestionAdapter.refreshData();
		}

		resetFilter();

		mSearchDownloader.cancelDownload(KEY_SEARCH);
		mSearchDownloader.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	// Location methods

	private void scheduleSwitchToNetworkLocation() {
		Message msg = new Message();
		msg.what = MSG_SWITCH_TO_NETWORK_LOCATION;
		mHandler.sendMessageDelayed(msg, TIME_SWITCH_TO_NETWORK_DELAY);
	}

	private void startLocationListener() {
		showLoading(R.string.progress_finding_location);

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider;
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
			if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				scheduleSwitchToNetworkLocation();
			}
		}
		else {
			provider = LocationManager.NETWORK_PROVIDER;
		}

		lm.requestLocationUpdates(provider, 0, 0, this);
	}

	private void stopLocationListener() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
		mHandler.removeMessages(MSG_SWITCH_TO_NETWORK_LOCATION);
	}

	private void switchToNetworkLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			stopLocationListener();
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listeners

	private final View.OnFocusChangeListener mSearchEditTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (mSearchParams.getSearchType() != SearchType.FREEFORM) {
					mSearchEditText.setText(null);
					mSearchEditText.setTextColor(getResources().getColor(android.R.color.black));
				}
				else {
					mSearchEditText.selectAll();
				}

				hideDatesLayout();
				hideGuestsLayout();
				showSearchSuggestions();
				showButtonBar();
				showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
			}
			else {
				hideSearchSuggestions();
				hideButtonBar();
				hideSoftKeyboard(mSearchEditText);
				setSearchEditViews();
			}
		}
	};

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

	private final View.OnClickListener mTripAdvisorOnlyButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchRatingFilter();
			closeDrawer();
		}
	};

	private final RadioGroup.OnCheckedChangeListener mFilterButtonGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			buildFilter();
			setPriceRangeText();
			setRadioButtonShadowLayers();
			mPanel.setOpen(false, true);
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

				setSearchParams(MapUtils.getLatitiude(center), MapUtils.getLongitiude(center));
				startSearch();
			}
		}
	};

	private final View.OnClickListener mViewButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchResultsView();
		}
	};

	private final View.OnClickListener mSearchEditTextClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			hideDatesLayout();
			hideGuestsLayout();
			showDismissView();
			showSearchSuggestions();
			showButtonBar();
			showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
		}
	};

	private final View.OnClickListener mDatesButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showDatesLayout();
		}
	};

	private final View.OnClickListener mGuestsButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showGuestsLayout();
		}
	};

	private final View.OnClickListener mRefinementDismissViewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			hideButtonBar();
			hideSoftKeyboard(mSearchEditText);
			hideDatesLayout();
			hideGuestsLayout();
			hideRefinementDismissView();
		}
	};

	private final View.OnClickListener mPanelDismissViewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			closeDrawer();
		}
	};

	private final View.OnClickListener mSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startSearch();
		}
	};

	private final CalendarDatePicker.OnDateChangedListener mDatesDateChangedListener = new CalendarDatePicker.OnDateChangedListener() {
		@Override
		public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
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

	private final AdapterView.OnItemClickListener mSearchSuggestionsItemClickListner = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			if (position == 0) {
				mSearchEditText.setText(R.string.current_location);
				mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
				mSearchEditText.selectAll();
				mSearchEditText.requestFocus();

				mSearchParams.setSearchType(SearchType.MY_LOCATION);
				setSearchEditViews();
				showDatesLayout();
			}
			else {
				setSearchParams((SearchParams) mSearchSuggestionAdapter.getItem(position));
				setSearchEditViews();
				showDatesLayout();
			}
		}
	};

	private final Panel.OnPanelListener mPanelListener = new Panel.OnPanelListener() {
		@Override
		public void onPanelOpened(Panel panel) {
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
			mPanelDismissView.startAnimation(animation);

			onSearchResultsChanged();
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Handlers, Messages

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SWITCH_TO_NETWORK_LOCATION: {
				switchToNetworkLocation();
				break;
			}
			case MSG_BROADCAST_SEARCH_COMPLETED: {
				broadcastSearchCompleted((SearchResponse) msg.obj);
				break;
			}
			case MSG_BROADCAST_SEARCH_FAILED: {
				broadcastSearchFailed((String) msg.obj);
				break;
			}
			case MSG_BROADCAST_SEARCH_STARTED: {
				broadcastSearchStarted();
				break;
			}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Private classes

	private class ActivityState {
		private static final long serialVersionUID = 1L;

		public Handler handler;
		public String tag;
		public List<SearchListener> searchListeners;
		public SearchParams searchParams;
		public SearchParams oldSearchParams;
		public SearchResponse searchResponse;
		public Map<PriceRange, PriceTier> priceTierCache;
		public Session session;
		public Filter filter;
		public Filter oldFilter;
		public SearchSuggestionAdapter searchSuggestionAdapter;
		public Boolean isSearching;
		public BackgroundDownloader searchDownloader;

		public boolean datesLayoutIsVisible;
		public boolean guestsLayoutIsVisible;
		public boolean panelIsOpen;
	}

	private class SoftKeyResultReceiver extends ResultReceiver {
		public SoftKeyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (resultCode == InputMethodManager.RESULT_HIDDEN) {
				hideRefinementDismissView();
				hideSearchSuggestions();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		// Only send page load when the app just started up - if there's a previous instance, that means
		// it was just a configuration change.
		if (getLastNonConfigurationInstance() == null) {
			Log.d("Tracking \"App.Loading\" pageLoad...");

			AppMeasurement s = new AppMeasurement(getApplication());

			TrackingUtils.addStandardFields(this, s);

			s.pageName = "App.Loading";

			// Determine if this is a new install, an upgrade, or just a regular launch
			TrackingData trackingData = new TrackingData();
			String trackVersion = null;
			if (trackingData.load(this)) {
				trackVersion = trackingData.getVersion();
			}

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
				trackingData.setVersion(currentVersion);
				trackingData.save(this);
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
			boolean refinedLocation = false;
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
		s.eVar4 = s.prop4 = mSearchParams.getSearchLatitude() + "|" + mSearchParams.getSearchLongitude();

		// Check in/check out date
		s.eVar5 = s.prop5 = getDayDifference(mSearchParams.getCheckInDate(), Calendar.getInstance()) + "";
		s.eVar6 = s.prop16 = getDayDifference(mSearchParams.getCheckOutDate(), mSearchParams.getCheckInDate()) + "";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Number adults searched for
		s.eVar47 = mSearchParams.getNumAdults() + "";

		// Freeform location
		s.eVar48 = mSearchParams.getFreeformLocation();

		// Number of search results
		s.prop1 = mSearchResponse.getFilteredAndSortedProperties().length + "";

		// Send the tracking data
		s.track();
	}

	private int getDayDifference(Calendar date1, Calendar date2) {
		// Round the calendars so that they are zero'd in on a day
		Calendar date1Rounded = new GregorianCalendar(date1.get(Calendar.YEAR), date1.get(Calendar.MONTH),
				date1.get(Calendar.DAY_OF_MONTH));
		Calendar date2Rounded = new GregorianCalendar(date2.get(Calendar.YEAR), date2.get(Calendar.MONTH),
				date2.get(Calendar.DAY_OF_MONTH));

		return Math.round((date1Rounded.getTimeInMillis() - date2Rounded.getTimeInMillis()) / (1000 * 60 * 60 * 24));
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