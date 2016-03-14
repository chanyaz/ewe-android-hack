package com.expedia.bookings.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;

/**
 * This is a debug menu that you can attach to any Activity in Expedia Bookings.
 *
 * Just add hooks at the appropriate points.
 *
 * TODO: Reflection here, so as not to duplicate code.  (Being lazy for the moment, though.)
 *
 */
public class DebugMenu {

	private Activity hostActivity;
	private Class<? extends Activity> settingsActivityClass;

	public DebugMenu(@NonNull Activity hostActivity, @Nullable Class<? extends Activity> settingsActivityClass) {
		this.hostActivity = hostActivity;
		this.settingsActivityClass = settingsActivityClass;
	}

	public void onCreateOptionsMenu(Menu menu) {
		if (BuildConfig.DEBUG) {
			MenuInflater inflater = new MenuInflater(hostActivity);
			inflater.inflate(R.menu.menu_debug, menu);
			updateStatus(menu);
		}
	}

	public void onPrepareOptionsMenu(Menu menu) {
		if (BuildConfig.DEBUG) {
			menu.findItem(R.id.debug_menu_settings).setVisible((settingsActivityClass != null));
			updateStatus(menu);
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.debug_menu_settings: {
			if (settingsActivityClass != null) {
				Intent intent = new Intent(hostActivity, settingsActivityClass);
				hostActivity.startActivityForResult(intent, Constants.REQUEST_SETTINGS);
			}
			return true;
		}
		case R.id.debug_menu_build_server:
		case R.id.debug_menu_build_number:
			// just consume the click
			return true;
		}

		return false;
	}

	private void updateStatus(Menu menu) {
		MenuItem serverMenuItem = menu.findItem(R.id.debug_menu_build_server);
		MenuItem buildMenuItem = menu.findItem(R.id.debug_menu_build_number);
		MenuItem gitHashItem = menu.findItem(R.id.debug_menu_git_hash);
		if (serverMenuItem != null) {
			serverMenuItem.setTitle(getBuildServerString());
		}
		if (buildMenuItem != null) {
			buildMenuItem.setTitle(getBuildNumberString());
		}
		if (gitHashItem != null) {
			gitHashItem.setTitle(BuildConfig.GIT_REVISION);
		}
	}

	private String getBuildServerString() {
		String endpoint = Ui.getApplication(hostActivity).appComponent().endpointProvider().getEndPoint().toString();
		return hostActivity.getString(R.string.connected_server, endpoint);
	}

	private String getBuildNumberString() {
		String buildNumber = BuildConfig.BUILD_NUMBER;
		return hostActivity.getString(R.string.build_number, buildNumber);
	}

}
