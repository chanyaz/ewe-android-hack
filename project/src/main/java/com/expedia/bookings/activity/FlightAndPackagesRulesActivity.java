package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.BaseRulesFragment;
import com.expedia.bookings.fragment.FlightRulesFragmentV2;
import com.expedia.bookings.fragment.PackagesRulesFragment;
import com.expedia.bookings.tracking.OmnitureTracking;

public class FlightAndPackagesRulesActivity extends AppCompatActivity {

	public static final String LOB_KEY = "LOB";

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, FlightAndPackagesRulesActivity.class);
		intent.putExtra(LOB_KEY, lob);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (shouldBail()) {
			return;
		}
		setContentView(R.layout.fragment_container_with_toolbar);

		LineOfBusiness lob = LineOfBusiness.FLIGHTS_V2;
		BaseRulesFragment rulesFragment;

		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(LOB_KEY)) {
			lob = (LineOfBusiness) getIntent().getExtras().get(LOB_KEY);
		}

		if (lob == LineOfBusiness.PACKAGES) {
			rulesFragment = new PackagesRulesFragment();
		}
		else {
			rulesFragment = new FlightRulesFragmentV2();
		}

		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, rulesFragment).commit();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private boolean shouldBail() {
		return !getResources().getBoolean(R.bool.portrait);
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
