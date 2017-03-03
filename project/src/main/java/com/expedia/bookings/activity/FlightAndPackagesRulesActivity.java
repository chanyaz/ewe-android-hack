package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.BaseRulesFragment;
import com.expedia.bookings.fragment.FlightRulesFragmentV1;
import com.expedia.bookings.fragment.FlightRulesFragmentV2;
import com.expedia.bookings.fragment.PackagesRulesFragment;
import com.expedia.bookings.tracking.OmnitureTracking;

public class FlightAndPackagesRulesActivity extends FragmentActivity {

	public static final String LOB_KEY = "LOB";

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, FlightAndPackagesRulesActivity.class);
		intent.putExtra(LOB_KEY, lob);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (shouldBail()) {
			return;
		}

		setContentView(R.layout.activity_flight_and_packages_rules);


		LineOfBusiness lob = LineOfBusiness.FLIGHTS;
		BaseRulesFragment rulesFragment;

		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(LOB_KEY)) {
			lob = (LineOfBusiness) getIntent().getExtras().get(LOB_KEY);
		}

		if (lob == LineOfBusiness.PACKAGES) {
			rulesFragment = new PackagesRulesFragment();
		}
		else if (lob == LineOfBusiness.FLIGHTS_V2) {
			rulesFragment = new FlightRulesFragmentV2();
		}
		else {
			rulesFragment = new FlightRulesFragmentV1();
		}

		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, rulesFragment).commit();

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private boolean shouldBail() {
		return !ExpediaBookingApp.useTabletInterface() && !getResources().getBoolean(R.bool.portrait);
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
