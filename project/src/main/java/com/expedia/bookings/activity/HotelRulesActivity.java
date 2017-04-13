package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.tracking.OmnitureTracking;

public class HotelRulesActivity extends FragmentActivity {

	public static final String LOB_KEY = "LOB";

	public static Intent createIntent(Context context, LineOfBusiness lob) {
		Intent intent = new Intent(context, HotelRulesActivity.class);
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

		setContentView(R.layout.activity_hotel_rules);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private boolean shouldBail() {
		return !getResources().getBoolean(R.bool.portrait);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutWarsaw();
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
