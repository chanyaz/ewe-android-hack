package com.expedia.ui;

import com.expedia.bookings.utils.TuneUtils;

public abstract class TrackingAbstractAppCompatActivity extends AbstractAppCompatActivity {
	@Override
	protected void onResume() {
		super.onResume();
		TuneUtils.startTune(this);
	}
}
