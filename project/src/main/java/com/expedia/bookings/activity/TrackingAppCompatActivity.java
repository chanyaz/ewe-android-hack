package com.expedia.bookings.activity;

import android.support.v7.app.AppCompatActivity;

import com.expedia.bookings.utils.TuneUtils;

public abstract class TrackingAppCompatActivity extends AppCompatActivity {
	@Override
	protected void onResume() {
		super.onResume();
		TuneUtils.startTune(this);
	}
}

