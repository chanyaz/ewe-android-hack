package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.FlightConfirmationFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class FlightConfirmationActivity extends SherlockFragmentActivity {

	private static final boolean QUICKLAUNCH = false;

	private static final int REQUEST_CODE_SEARCH_PARAMS = 1;

	private BlurredBackgroundFragment mBgFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This is temporary testing code that makes it easy to save/load testing data
		// so that we can quickly test this activity.  DELETE when finished dev!
		if (QUICKLAUNCH) {
			if (savedInstanceState == null) {
				if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
					Db.loadTestData(this);
				}
				else {
					Db.saveDbForTesting(this);
				}
			}
		}

		setContentView(R.layout.activity_flight_confirmation);

		if (savedInstanceState == null) {
			mBgFragment = new BlurredBackgroundFragment();

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.background_container, mBgFragment, BlurredBackgroundFragment.TAG);
			ft.add(R.id.content_container, new FlightConfirmationFragment(), FlightConfirmationFragment.TAG);
			ft.commit();
		}
		else {
			mBgFragment = Ui.findSupportFragment(this, BlurredBackgroundFragment.TAG);
		}

		// Action bar setup
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.booking_complete);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutConfirmation(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mBgFragment.setFadeEnabled(true);
	}

	@Override
	public void onBackPressed() {
		// F854: Do not let users go back to the previous screens if they successfully booked
		Intent intent = new Intent(this, FlightSearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_SEARCH_PARAMS && resultCode == RESULT_OK) {
			Log.i("New search requested");

			FlightSearchParams params = JSONUtils.getJSONable(data, FlightSearchOverlayActivity.EXTRA_SEARCH_PARAMS,
					FlightSearchParams.class);
			Db.getFlightSearch().setSearchParams(params);

			Intent intent = new Intent(this, FlightSearchResultsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

			// Make sure to finish this class, in case 
			// the user launched directly to the conf page
			finish();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_confirmation, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.menu_search:
			Intent intent = new Intent(this, FlightSearchOverlayActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SEARCH_PARAMS);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
