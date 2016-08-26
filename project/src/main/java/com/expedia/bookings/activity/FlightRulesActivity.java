package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.FlightRulesFragmentV1;
import com.expedia.bookings.fragment.FlightRulesFragmentV2;
import com.expedia.bookings.tracking.OmnitureTracking;

public class FlightRulesActivity extends FragmentActivity {

	public static final String LOB_KEY = "LOB";

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, FlightRulesActivity.class);
		intent.putExtra(LOB_KEY, lob);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (shouldBail()) {
			return;
		}

		setContentView(R.layout.activity_flight_rules);
		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(LOB_KEY)
			&& getIntent().getExtras().get(LOB_KEY) == LineOfBusiness.FLIGHTS_V2) {
			FlightRulesFragmentV2 flightRulesFragmentV2 = new FlightRulesFragmentV2();
			getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, flightRulesFragmentV2).commit();

		}
		else {
			FlightRulesFragmentV1 flightRulesFragmentV1 = new FlightRulesFragmentV1();
			getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, flightRulesFragmentV1).commit();
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private boolean shouldBail() {
		return !ExpediaBookingApp.useTabletInterface(this) && !getResources().getBoolean(R.bool.portrait);
	}

	@Override
	protected void onStart() {
		super.onStart();

		OmnitureTracking.trackPageLoadFlightCheckoutWarsaw();
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
