package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.fragment.FlightFilterDialogFragment;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.FlightListFragment.FlightListFragmentListener;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.NavigationButton;
import com.expedia.bookings.widget.NavigationDropdownAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightSearchResultsActivity extends SherlockFragmentActivity implements FlightListFragmentListener,
		OnBackStackChangedListener {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";

	private static final String BACKSTACK_SEARCH_PARAMS = "BACKSTACK_SEARCH_PARAMS";

	private Context mContext;

	private StatusFragment mStatusFragment;
	private FlightListFragment mListFragment;
	private FlightSearchParamsFragment mSearchParamsFragment;

	// Current leg being displayed
	private int mLegPosition = 0;

	private TextView mTitleTextView;
	private TextView mSubtitleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		// Recover data if it was flushed from memory
		if (savedInstanceState != null && !BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY)
				&& Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		setContentView(R.layout.activity_flight_results);

		// Try to recover any Fragments
		mStatusFragment = Ui.findSupportFragment(this, StatusFragment.TAG);
		mListFragment = Ui.findSupportFragment(this, FlightListFragment.TAG);
		mSearchParamsFragment = Ui.findSupportFragment(this, FlightSearchParamsFragment.TAG);


		// Configure the custom action bar view
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView = inflater.inflate(R.layout.action_bar_flight_results, null);
		mTitleTextView = Ui.findView(customView, R.id.title_text_view);
		mSubtitleTextView = Ui.findView(customView, R.id.subtitle_text_view);
		
		ActionBar actionBar = this.getSupportActionBar();
		NavigationButton nb = NavigationButton.createNewInstanceAndAttach(this, R.drawable.icon, actionBar);
		nb.setDropdownAdapter(new NavigationDropdownAdapter(this));
		nb.setCustomView(customView);

		if (savedInstanceState == null) {
			// On first launch, start a search
			startSearch();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		BackgroundDownloader.getInstance().registerDownloadCallback(DOWNLOAD_KEY, mDownloadCallback);

		getSupportFragmentManager().addOnBackStackChangedListener(this);
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
	public void onBackPressed() {
		if (mListFragment == null || !mListFragment.onBackPressed()) {
			super.onBackPressed();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_results, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		updateTitleBar();

		// Show sort/filter only if we have results and are not showing the search params fragment
		FlightSearchResponse response = Db.getFlightSearch().getSearchResponse();
		menu.setGroupVisible(R.id.group_results, response != null && !response.hasErrors()
				&& (mSearchParamsFragment == null || !mSearchParamsFragment.isAdded()));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Push user back to search page if they hit the home button
			Intent intent = new Intent(this, FlightSearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_CLEAR_TASK
					+ Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_sort:
		case R.id.menu_filter: {
			// TODO: Will need to change with new design someday
			FlightFilterDialogFragment dialog = FlightFilterDialogFragment.newInstance(mLegPosition);
			dialog.show(getSupportFragmentManager(), "filterDialogFragment");
			break;
		}
		case R.id.menu_search:
			if (mSearchParamsFragment != null && mSearchParamsFragment.isAdded() && !mSearchParamsFragment.isDetached()) {
				onSearch();
			}
			else {
				showSearchParameters();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void updateTitleBar() {
		// Configure the title based on which leg the user is selecting
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int titleStrId = (mLegPosition == 0) ? R.string.outbound_TEMPLATE : R.string.inbound_TEMPLATE;
		String airportCode = (mLegPosition == 0) ? params.getArrivalAirportCode() : params.getDepartureAirportCode();
		String city = FlightStatsDbUtils.getAirport(airportCode).mCity;
		mTitleTextView.setText(getString(titleStrId, city != null ? city : airportCode));

		// Configure subtitle based on which user the leg is selecting
		Date date = (mLegPosition == 0) ? params.getDepartureDateWithDefault() : params.getReturnDate();
		mSubtitleTextView.setText(android.text.format.DateFormat.getMediumDateFormat(this).format(
				date.getCalendar().getTime()));
	}

	//////////////////////////////////////////////////////////////////////////
	// Search download

	private void startSearch() {
		if (mStatusFragment == null) {
			mStatusFragment = new StatusFragment();
		}

		// #445: Need to reset the search results before starting a new one
		Db.getFlightSearch().setSearchResponse(null);
		if (mListFragment != null) {
			mListFragment.reset();
		}
		onSelectionChanged(0);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_container, mStatusFragment, StatusFragment.TAG).commit();
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
				mStatusFragment.showError(getString(R.string.error_loading_flights_TEMPLATE, response.getErrors()
						.get(0).getPresentableMessage(mContext)));
			}
			else if (response.getTripCount() == 0) {
				mStatusFragment.showError(getString(R.string.error_no_flights_found));
			}
			else {
				if (mListFragment == null) {
					mListFragment = new FlightListFragment();
				}

				mStatusFragment.setCoverEnabled(true);
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_container, mListFragment, FlightListFragment.TAG).commit();

				supportInvalidateOptionsMenu();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Search parameters

	private void showSearchParameters() {
		Log.i("Showing search paramters in FlightSearchResultsActivity");

		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentById(R.id.search_params_container) == null) {
			if (mSearchParamsFragment == null) {
				mSearchParamsFragment = FlightSearchParamsFragment.newInstance(Db.getFlightSearch().getSearchParams(),
						true);
			}

			FragmentTransaction ft = fm.beginTransaction();
			ft.addToBackStack(BACKSTACK_SEARCH_PARAMS);
			ft.add(R.id.search_params_container, mSearchParamsFragment, FlightSearchParamsFragment.TAG);
			ft.commit();
		}
	}

	private void onSearch() {
		Log.i("New search requested from FlightSearchResultsActivity");

		Db.getFlightSearch().setSearchParams(mSearchParamsFragment.getSearchParams());

		startSearch();

		supportInvalidateOptionsMenu();

		// Remove the search params fragment regardless
		getSupportFragmentManager().popBackStack(BACKSTACK_SEARCH_PARAMS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		mSearchParamsFragment = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Filter dialog

	public void showFilterDialog() {
		FlightFilterDialogFragment fragment = FlightFilterDialogFragment.newInstance(mLegPosition);
		fragment.show(getSupportFragmentManager(), FlightFilterDialogFragment.TAG);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightListFragmentListener

	@Override
	public void onSelectionChanged(int newLegPosition) {
		mLegPosition = newLegPosition;

		invalidateOptionsMenu();

		Db.kickOffBackgroundSave(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnBackStackChangedListener

	@Override
	public void onBackStackChanged() {
		supportInvalidateOptionsMenu();
	}
}
