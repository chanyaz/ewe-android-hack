package com.expedia.bookings.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.OmnitureTracking;

public class HotelRulesActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ExpediaBookingApp.useTabletInterface(this)) {
			// Use FlightTheme on hotel checkout for tablet.
			int themeId = R.style.FlightTheme_Rules;
			setTheme(themeId);
		}
		else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.activity_hotel_rules);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutWarsaw(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
		}
		}
		return super.onOptionsItemSelected(item);
	}
}
