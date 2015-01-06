package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.OmnitureTracking;

public class HotelRulesActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.legal_information);
		int themeId;
		if (ExpediaBookingApp.IS_TRAVELOCITY || ExpediaBookingApp.IS_AAG) {
			themeId = R.style.Theme_Phone_WebView_WithTitle;
		}
		else if (ExpediaBookingApp.useTabletInterface(this)) {
			// Use FlightTheme on hotel checkout for tablet.
			themeId = R.style.FlightTheme;
		}
		else {
			themeId = R.style.Theme_Phone_WebView;
		}
		setTheme(themeId);

		setContentView(R.layout.activity_hotel_rules);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (ExpediaBookingApp.IS_EXPEDIA) {
			getActionBar().setLogo(R.drawable.ic_expedia_action_bar_logo_dark);
		}
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
