package com.expedia.bookings.activity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.Rotate3dAnimation;
import com.expedia.bookings.dialog.LocationSuggestionDialog;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.widget.SearchSuggestionAdapter;
import com.expedia.bookings.widget.TagProgressBar;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.SelectionMode;
import com.mobiata.android.widget.NumberPicker;
import com.mobiata.android.widget.Panel;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.PriceRange;
import com.mobiata.hotellib.data.Filter.SearchRadius;
import com.mobiata.hotellib.data.Filter.Sort;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.StrUtils;

@SuppressWarnings("unused")
public class SearchActivity extends ActivityGroup implements LocationListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String ACTIVITY_SEARCH_LIST = SearchListActivity.class.getCanonicalName();
	public static final String ACTIVITY_SEARCH_MAP = SearchMapActivity.class.getCanonicalName();

	private static final String KEY_SEARCH = "KEY_SEARCH";

	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;

	private static final int MSG_SWITCH_TO_NETWORK_LOCATION = 0;
	private static final int MSG_BROADCAST_SEARCH_COMPLETED = 1;
	private static final int MSG_BROADCAST_SEARCH_FAILED = 2;
	private static final int MSG_BROADCAST_SEARCH_STARTED = 3;

	private static final long TIME_SWITCH_TO_NETWORK_DELAY = 1000 * 3;

	private static final boolean ANIMATION_VIEW_FLIP_ENABLED = true;
	private static final int ANIMATION_VIEW_FLIP_SPEED = 200;
	private static final float ANIMATION_VIEW_FLIP_DEPTH = 250f;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	// Views

	private View mFocusLayout;

	private FrameLayout mContent;
	private ImageView mViewFlipImage;

	private TextView mBookingInfoTextView;
	private EditText mSearchEditText;
	private ImageButton mDatesButton;
	private ImageButton mGuestsButton;

	private Panel mPanel;

	private View mSortLayout;
	private SegmentedControlGroup mSortButtonGroup;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;

	private View mDismissView;
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
	private SearchParams mSearchParams;
	private Session mSession;
	private SearchResponse mSearchResponse;
	private Filter mFilter;
	private boolean mLocationListenerStarted;
	private boolean mIsSearching;

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
			if (mSearchResponse != null) {
				mFilter.removeOnFilterChangedListener(mSearchResponse);
			}

			mSearchResponse = (SearchResponse) results;

			if (mSearchResponse != null && !mSearchResponse.hasErrors()) {
				mSearchResponse.setFilter(mFilter);
				mSession = mSearchResponse.getSession();
				broadcastSearchCompleted(mSearchResponse);

				hideLoading();
			}
			else if (mSearchResponse != null && mSearchResponse.getLocations() != null
					&& mSearchResponse.getLocations().size() > 0) {

				mSearchProgressBar.setShowProgress(false);
				mSearchProgressBar.setText(null);
				showDialog(DIALOG_LOCATION_SUGGESTIONS);
			}
			else {
				mSearchProgressBar.setShowProgress(false);
				mSearchProgressBar.setText(R.string.progress_search_failed);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		initializeViews();

		// Load both activites
		mLocalActivityManager = getLocalActivityManager();
		setActivity(SearchMapActivity.class);
		setActivity(SearchListActivity.class);

		ActivityState state = (ActivityState) getLastNonConfigurationInstance();
		if (state != null) {
			mHandler = state.handler;
			mTag = state.tag;
			mIntent = state.intent;
			mLaunchedView = state.launchedView;
			mSearchListeners = state.searchListeners;
			mSearchParams = state.searchParams;
			mSearchResponse = state.searchResponse;
			mSession = state.session;
			mFilter = state.filter;
			mSearchSuggestionAdapter = state.searchSuggestionAdapter;
			mIsSearching = state.isSearching;
			mSearchDownloader = state.searchDownloader;

			if (mTag != null) {
				setActivityByTag(mTag);
			}

			if (mSearchResponse != null) {
				if (mFilter != null) {
					mSearchResponse.setFilter(mFilter);
				}

				broadcastSearchCompleted(mSearchResponse);
			}
		}
		else {
			mSearchParams = new SearchParams();
			mSearchParams.setSearchType(SearchType.MY_LOCATION);
			mSearchSuggestionAdapter = new SearchSuggestionAdapter(this);

			startSearch();
		}

		mSearchSuggestionsListView.setAdapter(mSearchSuggestionAdapter);

		setBookingInfoText();
		setDrawerViews();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopLocationListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mIsSearching && mSearchResponse == null) {
			startLocationListener();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		ActivityState state = new ActivityState();
		state.handler = mHandler;
		state.tag = mTag;
		state.intent = mIntent;
		state.launchedView = mLaunchedView;
		state.searchListeners = mSearchListeners;
		state.searchParams = mSearchParams;
		state.searchResponse = mSearchResponse;
		state.session = mSession;
		state.filter = mFilter;
		state.searchSuggestionAdapter = mSearchSuggestionAdapter;
		state.isSearching = mIsSearching;
		state.searchDownloader = mSearchDownloader;

		return state;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOCATION_SUGGESTIONS: {
			CharSequence[] charSequenceArray = new CharSequence[mSearchResponse.getLocations().size()];
			mSearchResponse.getLocations().toArray(charSequenceArray);

			final CharSequence[] freeformLocations = charSequenceArray;

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.ChooseLocation);
			builder.setItems(freeformLocations, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mSearchEditText.setText(freeformLocations[which].toString());
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearch();
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
		case R.id.about: {
			Intent intent = new Intent(this, AboutActivity.class);
			intent.putExtra(AboutActivity.EXTRA_APP, AboutActivity.APP_EXPEDIA_BOOKINGS);
			intent.putExtra(AboutActivity.EXTRA_TAF_SUBJECT, getString(R.string.tell_a_friend_subject));
			intent.putExtra(AboutActivity.EXTRA_TAF_MESSAGE, getString(R.string.tell_a_friend_body));
			intent.putExtra(AboutActivity.EXTRA_SUPPORT_URL,
					"http://m.expedia.com/mt/support.expedia.com/app/home/p/532/?rfrr=app.android");
			intent.putExtra(AboutActivity.EXTRA_ABOUT_TEXT, getString(R.string.copyright));
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
				hideDismissView();
				hideButtonBar();
				return true;
			}

			if (mGuestsLayoutIsVisible) {
				hideGuestsLayout();
				hideDismissView();
				hideButtonBar();
				return true;
			}

			if (mSearchEditText.hasFocus() && mButtonBarIsVisible) {
				hideButtonBar();
				hideDismissView();
				hideSearchSuggestions();
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
		startSearchDownload();

		stopLocationListener();
	}

	@Override
	public void onProviderDisabled(String provider) {
		stopLocationListener();
		mSearchProgressBar.setShowProgress(false);
		mSearchProgressBar.setText(R.string.ProviderDisabled);
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

		mSearchListeners.add(searchListener);
	}

	public SearchParams getSearchParams() {
		return mSearchParams;
	}

	public Session getSession() {
		return mSession;
	}

	public void setSearchParams() {
		setSearchParams(null, null);
	}

	public void setSearchParams(Double latitde, Double longitude) {
		if (mSearchParams == null) {
			mSearchParams = new SearchParams();
		}

		if (latitde != null && longitude != null) {
			mSearchParams.setSearchLatLon(latitde, longitude);
		}
		else {
			mSearchParams.setFreeformLocation(mSearchEditText.getText().toString().trim());
		}

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
		hideDatesLayout();
		hideGuestsLayout();
		hideSearchSuggestions();
		hideButtonBar();
		hideDismissView();
		hideSoftKeyboard(mSearchEditText);

		resetFocus();
		setFilter();

		switch (mSearchParams.getSearchType()) {
		case FREEFORM: {
			showLoading(R.string.progress_searching_hotels);
			setSearchParams();
			startSearchDownload();

			Search.add(this, mSearchParams);
			mSearchSuggestionAdapter.refreshData();

			break;
		}
		case MY_LOCATION: {
			mSearchEditText.setText(R.string.current_location);
			mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
			mSearchParams.setSearchType(SearchType.MY_LOCATION);

			showLoading(R.string.progress_finding_location);
			startLocationListener();

			break;
		}
		case PROXIMITY: {
			mSearchEditText.setText(R.string.visible_map_area);
			mSearchEditText.setTextColor(getResources().getColor(R.color.MyLocationBlue));
			mSearchParams.setSearchType(SearchType.PROXIMITY);

			showLoading(R.string.progress_searching_hotels);
			startSearchDownload();

			break;
		}
		}

		setBookingInfoText();
	}

	public void switchResultsView() {
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
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			newActivityClass = SearchListActivity.class;

			if (ANIMATION_VIEW_FLIP_ENABLED) {
				animationOut = new Rotate3dAnimation(0, 90, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, true);
				animationIn = new Rotate3dAnimation(-90, 0, centerX, centerY, ANIMATION_VIEW_FLIP_DEPTH, false);
			}
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
					mContent.post(new Runnable() {
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
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

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
	}

	// Show/hide soft keyboard

	private void hideSoftKeyboard(TextView v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		hideDismissView();
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

	private void closeDrawer() {
		mPanel.setOpen(false, true);
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

	private void hideDismissView() {
		mDismissView.setVisibility(View.GONE);
	}

	private void hideGuestsLayout() {
		mGuestsLayoutIsVisible = false;
		clearRefinementInfo();
		mGuestsLayout.setVisibility(View.GONE);
	}

	private void hideLoading() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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
		mDatesLayoutIsVisible = true;
		hideSoftKeyboard(mSearchEditText);
		hideGuestsLayout();
		hideSearchSuggestions();
		setRefinementInfo();
		mDatesLayout.setVisibility(View.VISIBLE);
		showDismissView();
		closeDrawer();
		showButtonBar();
	}

	private void showDismissView() {
		mDismissView.setVisibility(View.VISIBLE);
	}

	private void showGuestsLayout() {
		mGuestsLayoutIsVisible = true;
		hideSoftKeyboard(mSearchEditText);
		hideDatesLayout();
		hideSearchSuggestions();
		setRefinementInfo();
		mGuestsLayout.setVisibility(View.VISIBLE);
		mAdultsNumberPicker.requestFocus();
		showDismissView();
		closeDrawer();
		showButtonBar();
	}

	private void showLoading(int resId) {
		showLoading(getString(resId));
	}

	private void showLoading(String text) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

	///////////////////////////////////////////////////////////////////////
	// VIEW INITIALIZATION

	private void initializeViews() {
		// Get views
		mFocusLayout = findViewById(R.id.focus_layout);

		mContent = (FrameLayout) findViewById(R.id.content_layout);
		mViewFlipImage = (ImageView) findViewById(R.id.view_flip_image);

		mBookingInfoTextView = (TextView) findViewById(R.id.booking_info_text_view);
		mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
		mDatesButton = (ImageButton) findViewById(R.id.dates_button);
		mGuestsButton = (ImageButton) findViewById(R.id.guests_button);

		mPanel = (Panel) findViewById(R.id.drawer_panel);

		mSortLayout = findViewById(R.id.sort_layout);
		mSortButtonGroup = (SegmentedControlGroup) findViewById(R.id.sort_filter_button_group);
		mRadiusButtonGroup = (SegmentedControlGroup) findViewById(R.id.radius_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) findViewById(R.id.price_filter_button_group);

		mDismissView = findViewById(R.id.dismiss_view);
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

		// Properties		
		mPanel.setInterpolator(new AccelerateInterpolator());
		mPanel.setOnPanelListener(mPanelListener);

		mAdultsNumberPicker.setRange(1, 4);
		mChildrenNumberPicker.setRange(0, 4);

		Time now = new Time();
		now.setToNow();
		mDatesCalendarDatePicker.setSelectionMode(SelectionMode.RANGE);
		mDatesCalendarDatePicker.setMinDate(now.year, now.month, now.monthDay);
		mDatesCalendarDatePicker.setMaxRange(28);

		// Listeners
		mSearchEditText.setOnFocusChangeListener(mSearchEditTextFocusChangeListener);
		mSearchEditText.setOnClickListener(mSearchEditTextClickListener);
		mSearchEditText.addTextChangedListener(mSearchEditTextWatcher);
		mSearchEditText.setOnEditorActionListener(mSearchEditorActionListener);
		mDatesButton.setOnClickListener(mDatesButtonClickListener);
		mGuestsButton.setOnClickListener(mGuestsButtonClickListener);

		mSortButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);

		mDismissView.setOnClickListener(mDismissViewClickListener);
		mSearchSuggestionsListView.setOnItemClickListener(mSearchSuggestionsItemClickListner);

		mDatesCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);
		mAdultsNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mChildrenNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mSearchButton.setOnClickListener(mSearchButtonClickListener);
	}

	///////////////////////////////////////////////////////////////////////

	private void resetFocus() {
		mFocusLayout.requestFocus();

		hideSoftKeyboard(mSearchEditText);
		hideDatesLayout();
		hideGuestsLayout();
		hideButtonBar();
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

		String[] shortMonthNames = getResources().getStringArray(R.array.short_month_names);

		mBookingInfoTextView.setText(getString(R.string.booking_info_template, location, shortMonthNames[startMonth],
				startDay, shortMonthNames[endMonth], endDay, endYear));
	}

	private void setDrawerViews() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			mSortLayout.setVisibility(View.VISIBLE);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			mSortLayout.setVisibility(View.GONE);
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
			final int nights = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

			mRefinementInfoTextView.setText(String.format("%d nights", nights > 0 ? nights : 1));
		}
		else if (mGuestsLayoutIsVisible) {
			final int adults = mAdultsNumberPicker.getCurrent();
			final int children = mChildrenNumberPicker.getCurrent();

			mRefinementInfoTextView.setText(StrUtils.formatGuests(mContext, adults, children));
		}
	}

	private void setSearchViews(SearchParams searchParams) {
		mSearchEditText.setText(searchParams.getFreeformLocation());
		mAdultsNumberPicker.setCurrent(searchParams.getNumAdults());
		mChildrenNumberPicker.setCurrent(searchParams.getNumChildren());
	}

	// Searching methods

	private void setFilter() {
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

	private void startSearchDownload() {
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
				hideDatesLayout();
				hideGuestsLayout();
				showSearchSuggestions();
				showButtonBar();
				showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));

				mSearchEditText.selectAll();
			}
			else {
				hideSearchSuggestions();
				hideButtonBar();
				hideSoftKeyboard(mSearchEditText);
			}
		}
	};

	private final TextWatcher mSearchEditTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mSearchEditText.setTextColor(getResources().getColor(android.R.color.black));
			mSearchParams.setSearchType(SearchType.FREEFORM);
		}
	};

	private final TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				startSearch();
				hideSoftKeyboard(v);
				hideDismissView();

				return true;
			}

			return false;
		}
	};

	private final RadioGroup.OnCheckedChangeListener mFilterButtonGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			setFilter();
			mPanel.setOpen(false, true);
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

	private final View.OnClickListener mDismissViewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			hideButtonBar();
			hideSoftKeyboard(mSearchEditText);
			hideDatesLayout();
			hideGuestsLayout();
			hideDismissView();
		}
	};

	private final View.OnClickListener mSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			resetFocus();
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
				startSearch();
			}
			else {
				mSearchParams = (SearchParams) mSearchSuggestionAdapter.getItem(position);
				setSearchViews(mSearchParams);
				startSearch();
			}
		}
	};

	private final Panel.OnPanelListener mPanelListener = new Panel.OnPanelListener() {
		@Override
		public void onPanelOpened(Panel panel) {
		}

		@Override
		public void onPanelClosed(Panel panel) {
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
		public Handler handler;
		public String tag;
		public Intent intent;
		public View launchedView;
		public List<SearchListener> searchListeners;
		public SearchParams searchParams;
		public SearchResponse searchResponse;
		public Session session;
		public Filter filter;
		public SearchSuggestionAdapter searchSuggestionAdapter;
		public Boolean isSearching;
		public BackgroundDownloader searchDownloader;
	}

	private class SoftKeyResultReceiver extends ResultReceiver {
		public SoftKeyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			Log.t("Result code: %d", resultCode);

			if (resultCode == InputMethodManager.RESULT_HIDDEN) {
				hideDismissView();
				hideSearchSuggestions();
			}
		}
	}
}