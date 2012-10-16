package com.expedia.bookings.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.ResourcesCompat;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightFilter.Sort;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.FlightDetailsFragment;
import com.expedia.bookings.fragment.FlightFilterDialogFragment;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.FlightListFragment.FlightListFragmentListener;
import com.expedia.bookings.fragment.RetryErrorDialogFragment;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class FlightSearchResultsActivity extends SherlockFragmentActivity implements FlightListFragmentListener,
		OnBackStackChangedListener, RetryErrorDialogFragmentListener {

	public static final String EXTRA_DESELECT_LEG_ID = "EXTRA_DESELECT_LEG_ID";

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";

	private static final String INSTANCE_LEG_POSITION = "INSTANCE_LEG_POSITION";

	private static final int REQUEST_CODE_SEARCH_PARAMS = 1;

	private static final String BACKSTACK_LOADING = "BACKSTACK_LOADING";
	private static final String BACKSTACK_FLIGHT_DETAILS_PREFIX = "BACKSTACK_FLIGHT_DETAILS";
	private static final String BACKSTACK_FLIGHT_LIST_PREFIX = "BACKSTACK_FLIGHT_LIST";

	private static final Pattern BACKSTACK_PATTERN = Pattern.compile(".*#(\\d)");

	private Context mContext;

	private BlurredBackgroundFragment mBgFragment;
	private StatusFragment mStatusFragment;
	private FlightListFragment mListFragment;
	private FlightDetailsFragment mFlightDetailsFragment;

	// Current leg being displayed
	private int mLegPosition = 0;

	// This is needed in order to avoid timing issues with fragments.
	// If you want to indicate to the app to start a new search, but
	// you may not have resumed yet, use this variable.
	private boolean mStartSearchOnPostResume;

	// Sets up a leg to be deselected in post resume (for fragment state
	// reasons, this must be done later).
	private int mDeselectLegPos = -1;

	// Action bar views
	private ViewGroup mFlightSummaryContainer;
	private TextView mTitleTextView;
	private TextView mSubtitleTextView;
	private ViewGroup mFlightDetailsActionContainer;
	private View mCancelButton;
	private View mSelectFlightButton;

	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// Recover data if it was flushed from memory
		if (savedInstanceState != null && !BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY)
				&& Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		if (savedInstanceState != null) {
			mLegPosition = savedInstanceState.getInt(INSTANCE_LEG_POSITION);
		}

		setContentView(R.layout.activity_flight_results);

		// Try to recover any Fragments
		mBgFragment = Ui.findSupportFragment(this, R.id.background_fragment);
		mStatusFragment = Ui.findSupportFragment(this, StatusFragment.TAG);
		mListFragment = Ui.findSupportFragment(this, FlightListFragment.TAG);
		mFlightDetailsFragment = Ui.findSupportFragment(this, FlightDetailsFragment.TAG);

		// Configure the custom action bar view
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.action_bar_flight_results);
		actionBar.setTitle(R.string.searching);

		View customView = actionBar.getCustomView();
		mFlightSummaryContainer = Ui.findView(customView, R.id.flight_summary_container);
		mTitleTextView = Ui.findView(customView, R.id.title_text_view);
		mSubtitleTextView = Ui.findView(customView, R.id.subtitle_text_view);
		mFlightDetailsActionContainer = Ui.findView(customView, R.id.flight_details_action_container);
		mCancelButton = Ui.findView(customView, R.id.cancel_button);
		mSelectFlightButton = Ui.findView(customView, R.id.select_button);

		mCancelButton.setOnClickListener(mOnCancelClick);
		mSelectFlightButton.setOnClickListener(mSelectFlightClick);

		// Need to do this, or else the custom view won't take up the entire space available
		customView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		if (savedInstanceState == null) {
			// On first launch, start a search
			mStartSearchOnPostResume = true;

			// #664: We delay the starting of the first search
			// until *after* we've created the options menu.  That way
			// the PlaneWindowView will know if it needs to account
			// for a split action bar taking up space or not.
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.hasExtra(EXTRA_DESELECT_LEG_ID)) {
			String legId = intent.getStringExtra(EXTRA_DESELECT_LEG_ID);

			Log.i("Got new intent, deselecting leg id=" + legId);

			FlightTripLeg selectedLegs[] = Db.getFlightSearch().getSelectedLegs();
			for (mDeselectLegPos = 0; mDeselectLegPos < selectedLegs.length; mDeselectLegPos++) {
				if (selectedLegs[mDeselectLegPos].getFlightLeg().getLegId().equals(legId)) {
					break;
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		if (mStartSearchOnPostResume) {
			mStartSearchOnPostResume = false;
			supportInvalidateOptionsMenu();
			startSearch();
		}
		else if (mDeselectLegPos != -1) {
			getSupportFragmentManager().popBackStack(getFlightListBackStackName(mDeselectLegPos), 0);
		}
		else {
			BackgroundDownloader.getInstance().registerDownloadCallback(DOWNLOAD_KEY, mDownloadCallback);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(INSTANCE_LEG_POSITION, mLegPosition);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!isFinishing()) {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
		}
		else {
			BackgroundDownloader.getInstance().cancelDownload(DOWNLOAD_KEY);
		}

		getSupportFragmentManager().removeOnBackStackChangedListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_SEARCH_PARAMS && resultCode == RESULT_OK) {
			Log.i("Got new search params from FlightSearchOverlayActivity");

			FlightSearchParams params = JSONUtils.getJSONable(data, FlightSearchOverlayActivity.EXTRA_SEARCH_PARAMS,
					FlightSearchParams.class);
			Db.getFlightSearch().setSearchParams(params);
			mStartSearchOnPostResume = true;
		}
	}

	@Override
	public void onBackPressed() {
		String name = getTopBackStackName();

		if (name == null || name.equals(BACKSTACK_LOADING) || name.equals(getFlightListBackStackName(0))) {
			finish();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mKillReceiver.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	private MenuItem mSearchMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_results, menu);

		mSearchMenuItem = menu.findItem(R.id.menu_search);
		mSearchMenuItem.getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(mSearchMenuItem);
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Either show standard action bar options, or just show the custom
		// flight details action view, depending on whether flight details
		// are currently visible
		boolean isSearching = BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY);
		boolean areFlightDetailsShowing = areFlightDetailsShowing();
		mFlightSummaryContainer.setVisibility(areFlightDetailsShowing ? View.GONE : View.VISIBLE);
		mFlightDetailsActionContainer.setVisibility(areFlightDetailsShowing ? View.VISIBLE : View.GONE);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(!areFlightDetailsShowing);
		actionBar.setDisplayShowCustomEnabled(!isSearching);
		actionBar.setDisplayShowTitleEnabled(isSearching);
		for (int a = 0; a < menu.size(); a++) {
			menu.getItem(a).setVisible(!areFlightDetailsShowing);
		}

		// Search item has slightly different rules for visibility
		mSearchMenuItem.setVisible(!isSearching && !areFlightDetailsShowing);

		if (!areFlightDetailsShowing) {
			updateTitleBar();

			// Show sort/filter only if we have results and are not showing the search params fragment/flight details
			FlightSearchResponse response = Db.getFlightSearch().getSearchResponse();
			boolean resultsVisible = response != null && !response.hasErrors();
			menu.setGroupVisible(R.id.group_results, resultsVisible);

			// Crazy hack to get the item view to take up space.
			//
			// ASSUMPTIONS:
			// 1. You can detect split action bar status using ABS
			// 2. There are a detectable # of menu items
			// 3. The action bar is the full window width
			if (ResourcesCompat.getResources_getBoolean(this, R.bool.abs__split_action_bar_is_narrow)
					&& mSearchMenuItem.isVisible()) {
				int numVisible = 0;
				for (int a = 0; a < menu.size(); a++) {
					if (menu.getItem(a).isVisible()) {
						numVisible++;
					}
				}
				mSearchMenuItem.getActionView().setMinimumWidth(
						getWindowManager().getDefaultDisplay().getWidth() / numVisible);
			}

			if (resultsVisible) {
				// Configure the checked sort button
				FlightFilter filter = Db.getFlightSearch().getFilter(mLegPosition);
				int selectedId;
				switch (filter.getSort()) {
				default:
				case PRICE:
					selectedId = R.id.menu_select_sort_price;
					break;
				case DEPARTURE:
					selectedId = R.id.menu_select_sort_departs;
					break;
				case ARRIVAL:
					selectedId = R.id.menu_select_sort_arrives;
					break;
				case DURATION:
					selectedId = R.id.menu_select_sort_duration;
					break;
				}
				menu.findItem(selectedId).setChecked(true);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			// Push user back to search page if they hit the home button
			Intent intent = new Intent(this, FlightSearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_CLEAR_TASK
					+ Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}
		case R.id.menu_select_sort_price:
		case R.id.menu_select_sort_departs:
		case R.id.menu_select_sort_arrives:
		case R.id.menu_select_sort_duration:
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegPosition);
			switch (item.getItemId()) {
			case R.id.menu_select_sort_price:
				filter.setSort(Sort.PRICE);
				break;
			case R.id.menu_select_sort_departs:
				filter.setSort(Sort.DEPARTURE);
				break;
			case R.id.menu_select_sort_arrives:
				filter.setSort(Sort.ARRIVAL);
				break;
			case R.id.menu_select_sort_duration:
				filter.setSort(Sort.DURATION);
				break;
			}
			filter.notifyFilterChanged();
			item.setChecked(true);

			OmnitureTracking.trackLinkFlightSort(mContext, filter.getSort().name(), mLegPosition);

			return true;
		case R.id.menu_search: {
			Intent intent = new Intent(this, FlightSearchOverlayActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SEARCH_PARAMS);

			OmnitureTracking.trackLinkFlightRefine(mContext, mLegPosition);

			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	public void updateTitleBar() {
		// Configure the title based on which leg the user is selecting
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int titleStrId = (mLegPosition == 0) ? R.string.outbound_TEMPLATE : R.string.inbound_TEMPLATE;
		Location location = (mLegPosition == 0) ? params.getArrivalLocation() : params.getDepartureLocation();
		mTitleTextView.setText(getString(titleStrId, StrUtils.getLocationCityOrCode(location)));

		// Configure subtitle based on which user the leg is selecting
		Date date = (mLegPosition == 0) ? params.getDepartureDate() : params.getReturnDate();
		mSubtitleTextView.setText(DateUtils.formatDateTime(mContext, date.getCalendar().getTimeInMillis(),
				DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR + DateUtils.FORMAT_SHOW_WEEKDAY));
	}

	//////////////////////////////////////////////////////////////////////////
	// Search download

	private void startSearch() {
		if (mStatusFragment == null) {
			mStatusFragment = new StatusFragment();
		}

		// #445: Need to reset the search results before starting a new one
		Db.getFlightSearch().setSearchResponse(null);
		if (mListFragment != null && mListFragment.isAdded()) {
			mListFragment.reset();
		}
		mLegPosition = 0;
		Db.kickOffBackgroundSave(this);

		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() == 0) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.content_container, mStatusFragment, StatusFragment.TAG);
			ft.addToBackStack(BACKSTACK_LOADING);
			ft.commit();
		}
		else {
			fm.popBackStack(BACKSTACK_LOADING, 0);
		}

		mStatusFragment.showLoading(getString(R.string.loading_flights));

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(DOWNLOAD_KEY);
		bd.startDownload(DOWNLOAD_KEY, mDownload, mDownloadCallback);
	}

	private Download<FlightSearchResponse> mDownload = new Download<FlightSearchResponse>() {
		@Override
		public FlightSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);
			return services.flightSearch(Db.getFlightSearch().getSearchParams(), 0);
		}
	};

	private OnDownloadComplete<FlightSearchResponse> mDownloadCallback = new OnDownloadComplete<FlightSearchResponse>() {
		@Override
		public void onDownload(FlightSearchResponse response) {
			Log.i("Finished flights download!");

			// If the response is null, fake an error response (for the sake of cleaner code)
			if (response == null) {
				response = new FlightSearchResponse();
				ServerError error = new ServerError(ApiMethod.FLIGHT_SEARCH);
				error.setPresentationMessage(getString(R.string.error_server));
				error.setCode("SIMULATED");
				response.addError(error);
			}

			FlightSearch search = Db.getFlightSearch();
			search.setSearchResponse(response);

			if (response.hasErrors()) {
				RetryErrorDialogFragment df = new RetryErrorDialogFragment();
				df.show(getSupportFragmentManager(), "retryErrorDialog");
				mStatusFragment.showError(null);
			}
			else if (response.getTripCount() == 0) {
				mStatusFragment.showError(getString(R.string.error_no_flights_found));
			}
			else {
				if (mListFragment == null) {
					mListFragment = new FlightListFragment();
				}

				mStatusFragment.setCoverEnabled(true);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.content_container, mListFragment, FlightListFragment.TAG);
				ft.addToBackStack(getFlightListBackStackName(0));
				ft.commit();

				supportInvalidateOptionsMenu();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Flight list

	private String getFlightListBackStackName(int legPosition) {
		return BACKSTACK_FLIGHT_LIST_PREFIX + "#" + legPosition;
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight details

	private boolean areFlightDetailsShowing() {
		return getTopBackStackName().startsWith(BACKSTACK_FLIGHT_DETAILS_PREFIX);
	}

	private String getFlightDetailsBackStackName(int legPosition) {
		return BACKSTACK_FLIGHT_DETAILS_PREFIX + "#" + legPosition;
	}

	private OnClickListener mOnCancelClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			getSupportFragmentManager().popBackStack();
		}
	};

	private OnClickListener mSelectFlightClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Set the selected leg
			FlightTripLeg ftl = mFlightDetailsFragment.getFlightTripLeg();
			FlightSearch flightSearch = Db.getFlightSearch();
			flightSearch.setSelectedLeg(mLegPosition, new FlightTripLeg(ftl.getFlightTrip(), ftl.getFlightLeg()));
			Db.kickOffBackgroundSave(mContext);

			if (flightSearch.getSelectedFlightTrip() == null) {
				OmnitureTracking.trackPageLoadFlightSearchResultsInboundList(mContext);
				mLegPosition++;

				mListFragment.setLegPosition(mLegPosition);

				// Remove the flight details fragment
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.content_container, mListFragment);
				ft.addToBackStack(getFlightListBackStackName(mLegPosition));
				ft.commit();
			}
			else {
				Intent intent = new Intent(mContext, FlightTripOverviewActivity.class);
				startActivity(intent);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Filter dialog

	public void showFilterDialog() {
		FlightFilterDialogFragment fragment = FlightFilterDialogFragment.newInstance(mLegPosition);
		fragment.show(getSupportFragmentManager(), FlightFilterDialogFragment.TAG);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightListFragmentListener

	@Override
	public void onFlightLegClick(FlightTrip trip, FlightLeg leg, int legPosition) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFlightDetailsFragment = FlightDetailsFragment.newInstance(trip, leg, mLegPosition);
		ft.replace(R.id.content_container, mFlightDetailsFragment, FlightDetailsFragment.TAG);
		ft.addToBackStack(getFlightDetailsBackStackName(mLegPosition));
		ft.commit();
	}

	@Override
	public void onDeselectFlightLeg() {
		getSupportFragmentManager().popBackStack(getFlightListBackStackName(0), 0);

		// Note: For now, deselecting a flight here always means it is the outbound flight that is removed
		OmnitureTracking.trackLinkFlightRemoveOutboundSelection(mContext);
	}

	@Override
	public void onDisableFade() {
		mBgFragment.setFadeEnabled(false);
	}

	@Override
	public void onFadeRangeChange(int startY, int endY) {
		mBgFragment.setFadeRange(startY, endY);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnBackStackChangedListener

	@Override
	public void onBackStackChanged() {
		boolean didSave = false;

		String name = getTopBackStackName();
		Matcher m = BACKSTACK_PATTERN.matcher(name);
		if (m.matches()) {
			int legPosition = Integer.parseInt(m.group(1));

			// This indicates that we moved *backwards* to the previous details.  Normally
			// when we move forward, we make sure to update the leg position at the same time.
			if (mLegPosition != legPosition) {
				// Remove selected leg
				Db.getFlightSearch().setSelectedLeg(mLegPosition, null);
				Db.getFlightSearch().clearQuery(mLegPosition); // #443: Clear cached query
				Db.kickOffBackgroundSave(mContext);

				mLegPosition = legPosition;
				mListFragment.setLegPosition(legPosition);
				mFlightDetailsFragment = Ui.findSupportFragment(this, FlightDetailsFragment.TAG);

				didSave = true;
			}
		}

		if (name.startsWith(BACKSTACK_FLIGHT_DETAILS_PREFIX)) {
			onDisableFade();
		}

		supportInvalidateOptionsMenu();

		// Leave debug message
		Log.d("onBackStackChanged(): legPos=" + mLegPosition + ", saveAndUpdate=" + didSave + ", stack="
				+ getBackStackDebugString());
	}

	//////////////////////////////////////////////////////////////////////////
	// Back stack utils

	public String getTopBackStackName() {
		FragmentManager fm = getSupportFragmentManager();
		int backStackEntryCount = fm.getBackStackEntryCount();
		if (backStackEntryCount > 0) {
			return fm.getBackStackEntryAt(backStackEntryCount - 1).getName();
		}
		return "";
	}

	public String getBackStackDebugString() {
		FragmentManager fm = getSupportFragmentManager();
		StringBuilder sb = new StringBuilder();
		for (int a = 0; a < fm.getBackStackEntryCount(); a++) {
			if (a != 0) {
				sb.append(" --> ");
			}

			sb.append(fm.getBackStackEntryAt(a).getName());
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// RetryErrorDialogFragmentListener

	@Override
	public void onRetryError() {
		startSearch();
	}

	@Override
	public void onCancelErorr() {
		finish();
	}
}
