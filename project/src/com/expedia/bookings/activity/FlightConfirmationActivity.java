package com.expedia.bookings.activity;

import android.os.Bundle;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class FlightConfirmationActivity extends SherlockFragmentActivity {

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The app will get in to this state if being restored after background kill. In this case let's just be a good
		// guy and send them to the itin screen.
		if (Db.getFlightCheckout() == null) {
			Log.d("FlightConfirmationActivity launched without confirmation data, sending to itin");
			NavUtils.goToItin(this);
			finish();
			return;
		}

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (savedInstanceState == null) {
			clearImportantBillingInfo(Db.getBillingInfo());

			// Get data
			final FlightSearch search = Db.getFlightSearch();
			final String itinNum = search.getSelectedFlightTrip().getItineraryNumber();

			// Add guest itin to ItinManager
			if (!User.isLoggedIn(this)) {
				String email = Db.getBillingInfo().getEmail();
				String tripId = Db.getItinerary(itinNum).getItineraryNumber();
				ItineraryManager.getInstance().addGuestTrip(email, tripId);
			}
		}

		setContentView(R.layout.activity_flight_confirmation);
		getWindow().setBackgroundDrawable(null);

		ImageView bgImageView = Ui.findView(this, R.id.background_bg_view);
		bgImageView.setImageBitmap(Db.getBackgroundImage(this, true));

		// Action bar setup
		ActionBar actionBar = this.getSupportActionBar();
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

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_confirmation, menu);
		ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_done);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_done:
			NavUtils.goToItin(this);
			finish();
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
