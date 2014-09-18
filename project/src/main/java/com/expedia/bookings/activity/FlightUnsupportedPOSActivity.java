package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.StatusFragment;
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

		StatusFragment fragment = Ui.findSupportFragment(this, StatusFragment.TAG);

		if (fragment == null) {
			fragment = new StatusFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.content_container, fragment, StatusFragment.TAG)
					.commit();
		}

		fragment.showGrounded(Html.fromHtml(getString(R.string.invalid_flights_pos)));
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackErrorPageLoadFlightUnsupportedPOS(this);
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
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
