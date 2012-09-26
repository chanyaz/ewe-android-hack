package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class FlightSearchActivity extends SherlockFragmentActivity {

	public static final String EXTRA_DATA_EXPIRED = "EXTRA_DATA_EXPIRED";

	private static final String INSTANCE_UPDATE_ON_RESUME = "INSTANCE_UPDATE_ON_RESUME";

	private FlightSearchParamsFragment mSearchParamsFragment;

	private HockeyPuck mHockeyPuck;

	// Keeps track of whether we should update the search params fragment with
	// the latest SearchParams on resume.  This is checked when the user leaves
	// this activity and comes back later (e.g., did a search).
	private boolean mUpdateOnResume = false;

	// Determines whether we should save the state on pause.  We want to save
	// when the user is leaving the app, but not if they're simply progressing
	// forward or they're rotating the screen.
	private boolean mConfigChange = false;
	private boolean mSaveState = true;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// On first launch, try to restore cached flight data (in this case, just for the search params)
		if (savedInstanceState == null) {
			if (Db.loadCachedFlightData(this)) {
				Log.i("Restoring search params from disk...");
			}
		}

		if (savedInstanceState != null) {
			mUpdateOnResume = savedInstanceState.getBoolean(INSTANCE_UPDATE_ON_RESUME);
		}

		View root = findViewById(android.R.id.content);
		root.setBackgroundResource(R.drawable.bg_search_nyc);

		if (savedInstanceState == null) {
			mSearchParamsFragment = FlightSearchParamsFragment.newInstance(Db.getFlightSearch().getSearchParams(),
					false);
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mSearchParamsFragment,
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

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();

		mSaveState = true;

		//HockeyApp crash
		mHockeyPuck.onResume();

		if (mUpdateOnResume) {
			mSearchParamsFragment.setSearchParams(new FlightSearchParams(Db.getFlightSearch().getSearchParams()));
			mUpdateOnResume = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

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

		mHockeyPuck.onSaveInstanceState(outState);
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_search, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);

		mHockeyPuck.onCreateOptionsMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		mHockeyPuck.onPrepareOptionsMenu(menu);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			FlightSearchParams params = mSearchParamsFragment.getSearchParams();
			if (!params.isFilled()) {
				Toast.makeText(this, R.string.toast_flight_search_params_missing, Toast.LENGTH_SHORT).show();
			}
			else if (!FlightUnsupportedPOSActivity.isSupportedPOS(this)) {
				Log.i("Search requested from unsupported POS");
				startActivity(new Intent(this, FlightUnsupportedPOSActivity.class));
			}
			else {
				Log.i("Initial search requested!");
				Db.getFlightSearch().setSearchParams(params);
				startActivity(new Intent(FlightSearchActivity.this, FlightSearchResultsActivity.class));
				mUpdateOnResume = true;
				mSaveState = false;
			}
			return true;
		case R.id.settings:
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivity(intent);
			return true;
		}

		if (DebugMenu.onOptionsItemSelected(this, item) || mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
