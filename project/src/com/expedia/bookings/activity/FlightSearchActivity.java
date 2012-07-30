package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class FlightSearchActivity extends SherlockFragmentActivity {

	public static final String EXTRA_DATA_EXPIRED = "EXTRA_DATA_EXPIRED";

	private FlightSearchParamsFragment mSearchParamsFragment;

	private HockeyPuck mHockeyPuck;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		setTitle(R.string.search_flights);

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//HockeyApp crash
		mHockeyPuck.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

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
			Db.getFlightSearch().setSearchParams(mSearchParamsFragment.getSearchParams());
			startActivity(new Intent(FlightSearchActivity.this, FlightSearchResultsActivity.class));
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
