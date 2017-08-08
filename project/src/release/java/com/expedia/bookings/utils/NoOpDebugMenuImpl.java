package com.expedia.bookings.utils;

import java.util.Collections;
import java.util.List;
import android.view.Menu;
import android.view.MenuItem;

// a no-op implementation of the DebugMenu interface
class NoOpDebugMenuImpl implements DebugMenu {

	@Override
	public void onCreateOptionsMenu(Menu menu) {
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public List<DebugActivityInfo> getDebugActivityInfoList() {
		return Collections.emptyList();
	}

	@Override
	public void addShortcutsForAllLaunchers() {
	}

	@Override
	public void startTestActivity(String className) {
	}

	@Override
	public Intent getSettingActivityIntent() {
	}
}
