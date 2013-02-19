package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.ConfirmationState.Type;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.FlightConfirmationFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class FlightConfirmationActivity extends SherlockFragmentActivity {

	private static final int REQUEST_CODE_SEARCH_PARAMS = 1;

	private ConfirmationState mConfState;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		mConfState = new ConfirmationState(this, Type.FLIGHT);

		if (savedInstanceState == null || Db.getFlightCheckout() == null) {
			if (mConfState.hasSavedData()) {
				// Load saved data from disk
				if (!mConfState.load()) {
					// If we failed to load the saved confirmation data, we should
					// delete the file and go back (since we are only here if we were called
					// directly from a startup).
					mConfState.delete();
					finish();
					return;
				}
			}
			else {
				clearImportantBillingInfo(Db.getBillingInfo());

				// Start a background thread to save this data to the disk
				new Thread(new Runnable() {
					public void run() {
						FlightSearch search = Db.getFlightSearch();
						String itinNum = search.getSelectedFlightTrip().getItineraryNumber();

						// copy billing info
						BillingInfo billingInfo = new BillingInfo(Db.getBillingInfo());

						mConfState.save(search, Db.getItinerary(itinNum), billingInfo,
								Db.getTravelers(),
								Db.getFlightCheckout());
					}
				}).start();
			}
		}

		setContentView(R.layout.activity_flight_confirmation);
		getWindow().setBackgroundDrawable(null);

		ImageView bgImageView = Ui.findView(this, R.id.background_bg_view);
		bgImageView.setImageBitmap(Db.getBackgroundImage(this, true));

		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.content_container, new FlightConfirmationFragment(), FlightConfirmationFragment.TAG);
			ft.commit();
		}

		// Action bar setup
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.booking_complete);
	}

	@Override
	protected void onResume() {
		super.onResume();

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			Db.setBillingInfo(null);
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	public void onBackPressed() {
		// F854: Do not let users go back to the previous screens if they successfully booked
		NavUtils.goToLaunchScreen(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_SEARCH_PARAMS && resultCode == RESULT_OK) {
			Log.i("New search requested");

			// Ensure we can't come back here again
			mConfState.delete();
			Db.clear();

			// Configure new search params
			FlightSearchParams params = JSONUtils.getJSONable(data, FlightSearchOverlayActivity.EXTRA_SEARCH_PARAMS,
					FlightSearchParams.class);
			Db.getFlightSearch().setSearchParams(params);

			// Launch flight search
			NavUtils.goToFlightSearchResults(this);
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

	//////////////////////////////////////////////////////////////////////////
	// Clear some billing information

	private void clearImportantBillingInfo(BillingInfo bi) {
		bi.setNumber(null);
		bi.setSecurityCode(null);
		bi.setSaveCardToExpediaAccount(false);
	}
}
