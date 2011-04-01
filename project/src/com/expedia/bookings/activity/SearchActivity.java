package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.TagProgressBar;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.widget.NumberPicker;
import com.mobiata.android.widget.Panel;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.Filter.PriceRange;
import com.mobiata.hotellib.data.Filter.SearchRadius;
import com.mobiata.hotellib.data.Filter.Sort;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.server.ExpediaServices;
import com.mobiata.hotellib.utils.StrUtils;

@SuppressWarnings("unused")
public class SearchActivity extends ActivityGroup implements LocationListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final String KEY_SEARCH = "KEY_SEARCH";

	private static final int MSG_SWITCH_TO_NETWORK_LOCATION = 0;
	private static final int MSG_BROADCAST_SEARCH_COMPLETED = 1;
	private static final int MSG_BROADCAST_SEARCH_FAILED = 2;
	private static final int MSG_BROADCAST_SEARCH_STARTED = 3;

	private static final String ACTIVITY_SEARCH_LIST = SearchListActivity.class.getCanonicalName();
	private static final String ACTIVITY_SEARCH_MAP = SearchMapActivity.class.getCanonicalName();

	private static final long TIME_SWITCH_TO_NETWORK_DELAY = 1000 * 3;

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	// Views

	private View mFocusLayout;

	private FrameLayout mContent;

	private EditText mSearchEditText;
	private ImageButton mDatesButton;
	private ImageButton mGuestsButton;

	private Panel mPanel;

	private View mSortLayout;
	private SegmentedControlGroup mSortButtonGroup;
	private SegmentedControlGroup mRadiusButtonGroup;
	private SegmentedControlGroup mPriceButtonGroup;

	private ImageButton mViewButton;

	private View mDatesLayout;
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
	private SearchResponse mSearchResponse;
	private Filter mFilter;
	private boolean mIsSearching;

	private boolean mDatesLayoutIsVisible;
	private boolean mGuestsLayoutIsVisible;
	private boolean mButtonBarIsVisible;

	// Threads / callbacks

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			return ExpediaServices.searchExpedia(mContext, mSearchParams);
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
			mSearchResponse.setFilter(mFilter);

			broadcastSearchCompleted(mSearchResponse);

			hideLoading();
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
			mFilter = state.filter;
			mIsSearching = state.isSearching;
			mSearchDownloader = state.searchDownloader;

			if (mTag != null) {
				setActivityByTag(mTag);
			}
		}
		else {
			setFilter();
		}

		setViewButtonImage();
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
		state.filter = mFilter;
		state.isSearching = mIsSearching;
		state.searchDownloader = mSearchDownloader;

		return state;
	}

	// Key events

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mDatesLayoutIsVisible) {
				hideDatesLayout();
				hideButtonBar();
				return true;
			}
			
			if (mGuestsLayoutIsVisible) {
				hideGuestsLayout();
				hideButtonBar();
				return true;
			}

			if (mSearchEditText.hasFocus() && mButtonBarIsVisible) {
				hideButtonBar();
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	// Location listener implementation

	@Override
	public void onLocationChanged(Location location) {
		Log.i("Location listener detected change");

		setSearchParams(location);
		startSearch();

		stopLocationListener();
	}

	@Override
	public void onProviderDisabled(String provider) {
		stopLocationListener();
		Log.w("Location listener failed");
		//broadcastSearchFailed(getString(R.string.provider_disabled));
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
			//broadcastSearchFailed(getString(R.string.provider_out_of_service));
		}
		else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			stopLocationListener();
			Log.w("Location listener failed: temporarily unavailable");
			//broadcastSearchFailed(getString(R.string.provider_temporarily_unavailable));\
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
	}

	// Show/hide view methods

	private void hideButtonBar() {
		mButtonBarIsVisible = false;
		mButtonBarLayout.setVisibility(View.GONE);
	}

	private void hideDatesLayout() {
		mDatesLayoutIsVisible = false;
		clearRefinementInfo();
		mDatesLayout.setVisibility(View.GONE);
	}

	private void hideGuestsLayout() {
		mGuestsLayoutIsVisible = false;
		clearRefinementInfo();
		mGuestsLayout.setVisibility(View.GONE);
	}

	private void hideLoading() {
		mSearchProgressBar.setVisibility(View.GONE);
	}

	private void showButtonBar() {
		mButtonBarIsVisible = true;
		mButtonBarLayout.setVisibility(View.VISIBLE);
	}

	private void showDatesLayout() {
		mDatesLayoutIsVisible = true;
		hideSoftKeyboard(mSearchEditText);
		hideGuestsLayout();
		setRefinementInfo();
		mDatesLayout.setVisibility(View.VISIBLE);
		showButtonBar();
	}

	private void showGuestsLayout() {
		mGuestsLayoutIsVisible = true;
		hideSoftKeyboard(mSearchEditText);
		hideDatesLayout();
		setRefinementInfo();
		mGuestsLayout.setVisibility(View.VISIBLE);
		mAdultsNumberPicker.requestFocus();
		showButtonBar();
	}

	private void showLoading(int resId) {
		showLoading(getString(resId));
	}

	private void showLoading(String text) {
		mSearchProgressBar.setVisibility(View.VISIBLE);
		mSearchProgressBar.setText(text);
	}

	// Other methods

	private void clearRefinementInfo() {
		mRefinementInfoTextView.setText("");
	}

	private void initializeViews() {
		// Get views
		mFocusLayout = findViewById(R.id.focus_layout);

		mContent = (FrameLayout) findViewById(R.id.content_layout);

		mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
		mDatesButton = (ImageButton) findViewById(R.id.dates_button);
		mGuestsButton = (ImageButton) findViewById(R.id.guests_button);

		mPanel = (Panel) findViewById(R.id.drawer_panel);

		mSortLayout = findViewById(R.id.sort_layout);
		mSortButtonGroup = (SegmentedControlGroup) findViewById(R.id.sort_filter_button_group);
		mRadiusButtonGroup = (SegmentedControlGroup) findViewById(R.id.radius_filter_button_group);
		mPriceButtonGroup = (SegmentedControlGroup) findViewById(R.id.price_filter_button_group);

		mViewButton = (ImageButton) findViewById(R.id.view_button);

		mDatesLayout = findViewById(R.id.dates_layout);
		mGuestsLayout = findViewById(R.id.guests_layout);
		mAdultsNumberPicker = (NumberPicker) findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) findViewById(R.id.children_number_picker);

		mButtonBarLayout = findViewById(R.id.button_bar_layout);
		mRefinementInfoTextView = (TextView) findViewById(R.id.refinement_info_text_view);
		mSearchButton = (Button) findViewById(R.id.search_button);

		mSearchProgressBar = (TagProgressBar) findViewById(R.id.search_progress_bar);

		// Properties
		mPanel.setInterpolator(new AccelerateInterpolator());
		mAdultsNumberPicker.setRange(1, 4);
		mChildrenNumberPicker.setRange(0, 4);

		// Listeners
		mSearchEditText.setOnFocusChangeListener(mSearchEditTextFocusChangeListener);
		mSearchEditText.setOnClickListener(mSearchEditTextClickListener);
		mSearchEditText.setOnEditorActionListener(mSearchEditorActionListener);
		mDatesButton.setOnClickListener(mDatesButtonClickListener);
		mGuestsButton.setOnClickListener(mGuestsButtonClickListener);

		mSortButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mRadiusButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);
		mPriceButtonGroup.setOnCheckedChangeListener(mFilterButtonGroupCheckedChangeListener);

		mAdultsNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mChildrenNumberPicker.setOnChangeListener(mNumberPickerChangedListener);
		mViewButton.setOnClickListener(mViewButtonClickListener);
		mSearchButton.setOnClickListener(mSearchButtonClickListener);
	}

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

		}
		else if (mGuestsLayoutIsVisible) {
			final int adults = mAdultsNumberPicker.getCurrent();
			final int children = mChildrenNumberPicker.getCurrent();

			mRefinementInfoTextView.setText(StrUtils.formatGuests(mContext, adults, children));
		}
	}

	private void setViewButtonImage() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			mViewButton.setImageResource(R.drawable.btn_map);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			mViewButton.setImageResource(R.drawable.btn_list);
		}
	}

	private void switchResultsView() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			setActivity(SearchMapActivity.class);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			setActivity(SearchListActivity.class);
		}

		setViewButtonImage();
		setDrawerViews();
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

	private void setSearchParams() {
		setSearchParams(null);
	}

	private void setSearchParams(Location location) {
		mSearchParams = new SearchParams();

		if (location != null) {
			mSearchParams.setSearchLatLon(location.getLatitude(), location.getLongitude());
		}
		else {
			mSearchParams.setFreeformLocation(mSearchEditText.getText().toString());
		}

		mSearchParams.setNumAdults(mAdultsNumberPicker.getCurrent());
		mSearchParams.setNumChildren(mChildrenNumberPicker.getCurrent());
	}

	private void startSearch() {
		showLoading(R.string.progress_searching_hotels);
		setFilter();

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

	private View.OnFocusChangeListener mSearchEditTextFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				hideDatesLayout();
				hideGuestsLayout();
				showButtonBar();
				showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
			}
			else {
				hideButtonBar();
				hideSoftKeyboard(mSearchEditText);
			}
		}
	};

	private TextView.OnEditorActionListener mSearchEditorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				setSearchParams();
				startSearch();

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				return true;
			}

			return false;
		}
	};

	private RadioGroup.OnCheckedChangeListener mFilterButtonGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			setFilter();
			mPanel.setOpen(false, true);
		}
	};

	private View.OnClickListener mSearchEditTextClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			hideDatesLayout();
			hideGuestsLayout();
			showButtonBar();
			showSoftKeyboard(mSearchEditText, new SoftKeyResultReceiver(mHandler));
		}
	};

	private View.OnClickListener mDatesButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showDatesLayout();
		}
	};

	private View.OnClickListener mGuestsButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showGuestsLayout();
		}
	};

	private View.OnClickListener mViewButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchResultsView();
		}
	};

	private View.OnClickListener mSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			resetFocus();

			stopLocationListener();
			setSearchParams();
			startSearch();
		}
	};

	private NumberPicker.OnChangedListener mNumberPickerChangedListener = new NumberPicker.OnChangedListener() {
		@Override
		public void onChanged(NumberPicker picker, int oldVal, int newVal) {
			setRefinementInfo();
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Handlers, Messages

	public Handler mHandler = new Handler() {
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
		public Filter filter;
		public Boolean isSearching;
		private BackgroundDownloader searchDownloader;
	}

	private class SoftKeyResultReceiver extends ResultReceiver {
		public SoftKeyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			Log.i("Result code: " + resultCode);

			if (resultCode == InputMethodManager.RESULT_HIDDEN
					|| resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN) {

				//resetFocus();
			}
		}
	}
}