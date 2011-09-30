package com.expedia.bookings.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;

public class TabletActivity extends MapActivity {

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle events

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
