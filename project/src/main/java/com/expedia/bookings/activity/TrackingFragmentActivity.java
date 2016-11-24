package com.expedia.bookings.activity;

import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.utils.TuneUtils;

public abstract class TrackingFragmentActivity extends FragmentActivity {
	@Override
	protected void onResume() {
		super.onResume();
		TuneUtils.startTune(this);
	}
}
