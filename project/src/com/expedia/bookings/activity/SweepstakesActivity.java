package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.SweepstakesFragment;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

public class SweepstakesActivity extends FragmentActivity {
	private SweepstakesFragment mSweepstakesFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sweepstakes);

		mSweepstakesFragment = Ui.findSupportFragment(this, R.id.sweepstakes_fragment);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		NavUtils.goToLaunchScreen(this);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// If they are logged in...
		// Save setting to hide sweepstakes
		if (Db.getUser() != null) {
			mSweepstakesFragment.enterSweepstakes();
		}
	}
}
