package com.expedia.bookings.activity;

import android.os.Bundle;

import com.expedia.bookings.R;
import com.mobiata.android.app.HoneycombAboutActivity;

public class TabletAboutActivity extends HoneycombAboutActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAboutAppsTitle(getString(R.string.travel_smart));
	}

	@Override
	public boolean useDefaultBehavior() {
		return false;
	}
}
