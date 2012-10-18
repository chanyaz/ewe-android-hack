package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.fragment.FlightSearchParamsFragment.FlightSearchParamsFragmentListener;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class FlightSearchActivity extends SherlockFragmentActivity implements FlightSearchParamsFragmentListener {

	public static final String EXTRA_DATA_EXPIRED = "EXTRA_DATA_EXPIRED";

	private static final String INSTANCE_UPDATE_ON_RESUME = "INSTANCE_UPDATE_ON_RESUME";

	private FlightSearchParamsFragment mSearchParamsFragment;

	// Keeps track of whether we should update the search params fragment with
	// the latest SearchParams on resume.  This is checked when the user leaves
	// this activity and comes back later (e.g., did a search).
	private boolean mUpdateOnResume = false;

	// Determines whether we should save the state on pause.  We want to save
	// when the user is leaving the app, but not if they're simply progressing
	// forward or they're rotating the screen.
	private boolean mConfigChange = false;
	private boolean mSaveState = true;

	private ActivityKillReceiver mKillReceiver;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// On first launch, try to restore cached flight data (in this case, just for the search params)
		if (savedInstanceState == null) {
			if (Db.loadCachedFlightData(this)) {
				Log.i("Restoring search params from disk...");

				Db.getFlightSearch().getSearchParams().ensureValidDates();
			}
		}

		if (savedInstanceState != null) {
			mUpdateOnResume = savedInstanceState.getBoolean(INSTANCE_UPDATE_ON_RESUME);
		}

		setContentView(R.layout.activity_flight_search);

		if (savedInstanceState == null) {
			mSearchParamsFragment = FlightSearchParamsFragment.newInstance(Db.getFlightSearch().getSearchParams(),
					false);
			getSupportFragmentManager().beginTransaction().add(R.id.content, mSearchParamsFragment,
					FlightSearchParamsFragment.TAG).commit();
		}
		else {
			mSearchParamsFragment = Ui.findSupportFragment(this, FlightSearchParamsFragment.TAG);
		}

		if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_DATA_EXPIRED, false)) {
			SimpleSupportDialogFragment df = SimpleSupportDialogFragment.newInstance(null,
					getString(R.string.error_data_expired));
			df.show(getSupportFragmentManager(), "dataExpiredDf");
		}

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(R.string.search_flights);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightSearch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSaveState = true;

		if (mUpdateOnResume) {
			Db.getFlightSearch().getSearchParams().ensureValidDates();
			mSearchParamsFragment.setSearchParams(new FlightSearchParams(Db.getFlightSearch().getSearchParams()));
			mUpdateOnResume = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mKillReceiver.onDestroy();

		// Save the current search params to disc
		if (mSaveState && !mConfigChange) {
			FlightSearch search = Db.getFlightSearch();
			search.reset();
			search.setSearchParams(mSearchParamsFragment.getSearchParams());
			Db.kickOffBackgroundSave(this);
			Log.i("Saved search params to disk.");
		}
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		mConfigChange = true;
		return super.onRetainCustomNonConfigurationInstance();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_UPDATE_ON_RESUME, mUpdateOnResume);
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	private MenuItem mSearchMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_search, menu);

		mSearchMenuItem = menu.findItem(R.id.search);
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
		FlightSearchParams params = mSearchParamsFragment.getSearchParams();
		mSearchMenuItem.setVisible(params.isFilled());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.search:
			FlightSearchParams params = mSearchParamsFragment.getSearchParams();
			if (!params.isFilled()) {
				throw new RuntimeException(
						"You should not be able to search unless you have filled out all the search params!");
			}
			else {
				Log.i("Initial search requested!");
				Db.getFlightSearch().setSearchParams(params);
				startActivity(new Intent(FlightSearchActivity.this, FlightSearchResultsActivity.class));
				mUpdateOnResume = true;
				mSaveState = false;
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightSearchParamsFragmentListener

	@Override
	public void onParamsChanged() {
		supportInvalidateOptionsMenu();
	}
}
