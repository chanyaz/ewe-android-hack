package com.expedia.bookings.utils;

import java.util.List;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public interface DebugMenu {
	class DebugActivityInfo {
		public String className;
		public String displayName;
	}

	void onCreateOptionsMenu(Menu menu);
	void onPrepareOptionsMenu(Menu menu);
	boolean onOptionsItemSelected(MenuItem item);

	List<DebugActivityInfo> getDebugActivityInfoList();
	void addShortcutsForAllLaunchers();
	void startTestActivity(String className);
	Intent getSettingActivityIntent();
}
