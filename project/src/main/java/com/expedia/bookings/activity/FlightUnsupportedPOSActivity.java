package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.FlightSearchLoadingFragment;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class FlightUnsupportedPOSActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_unsupported_pos);
		getWindow().setBackgroundDrawable(null);

		ActionBar actionBar = getActionBar();
		setTitle(R.string.taking_off_soon);
		actionBar.setDisplayHomeAsUpEnabled(true);

		FlightSearchLoadingFragment fragment = Ui.findSupportFragment(this, FlightSearchLoadingFragment.TAG);

		if (fragment == null) {
			fragment = new FlightSearchLoadingFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.content_container, fragment, FlightSearchLoadingFragment.TAG)
					.commit();
		}

		fragment.showGrounded(HtmlCompat.fromHtml(getString(R.string.invalid_flights_pos)));
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackErrorPageLoadFlightUnsupportedPOS();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
