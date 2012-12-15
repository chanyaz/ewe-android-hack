package com.expedia.bookings.activity;

import android.os.Bundle;
import android.text.Html;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class FlightUnsupportedPOSActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_unsupported_pos);
		getWindow().setBackgroundDrawable(null);

		// ActionBar
		ActionBar actionBar = this.getSupportActionBar();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
