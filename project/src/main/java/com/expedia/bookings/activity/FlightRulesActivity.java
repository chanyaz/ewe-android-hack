package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.BaseRulesFragment;
import com.expedia.bookings.fragment.FlightRulesFragmentV2;
import com.expedia.bookings.tracking.OmnitureTracking;

public class FlightRulesActivity extends AppCompatActivity {


	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, FlightRulesActivity.class);
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

		BaseRulesFragment rulesFragment;
		rulesFragment = new FlightRulesFragmentV2();

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
