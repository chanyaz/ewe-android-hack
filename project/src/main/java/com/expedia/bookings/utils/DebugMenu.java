package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.mobiata.android.Log;

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
	public List<String[]> debugActivities = new ArrayList<>();

	public DebugMenu(@NonNull Activity hostActivity, @Nullable Class<? extends Activity> settingsActivityClass) {
		this.hostActivity = hostActivity;
		this.settingsActivityClass = settingsActivityClass;
		try {
			PackageManager pm = hostActivity.getPackageManager();
			ActivityInfo[] activities = pm.getPackageInfo(hostActivity.getPackageName(),
				PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA).activities;
			for (ActivityInfo ai : activities) {
				if (ai.metaData != null && ai.metaData.getBoolean("debug", false)) {
					debugActivities.add(new String[] { ai.loadLabel(pm).toString(), ai.name });
				}
			}
		}
		catch (PackageManager.NameNotFoundException e) {
			Log.d("DebugMenu", "Failed to load debug activity list", e);
		}
	}

	public void onCreateOptionsMenu(Menu menu) {
		if (BuildConfig.DEBUG) {
			MenuInflater inflater = new MenuInflater(hostActivity);
			inflater.inflate(R.menu.menu_debug, menu);
			if (debugActivities.size() > 0) {
				Menu activityMenu = menu.addSubMenu(R.string.debug_screens_sub_menu);
				for (final String[] activityInfo : debugActivities) {
					MenuItem item = activityMenu.add(activityInfo[0]);
					item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							startTestActivity(activityInfo[1]);
							return true;
						}
					});
				}
				MenuItem item = activityMenu.add(R.string.debug_install_shortcuts);
				item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						addShortcutsForAllLaunchers();
						return true;
					}
				});
			}
			updateStatus(menu);
		}
	}

	public void addShortcutsForAllLaunchers() {
		for (String[] activityInfo : debugActivities) {
			try {
				Intent shortcutIntent = new Intent(hostActivity, Class.forName(activityInfo[1]));
				shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				Intent addIntent = new Intent();
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getBuildSpecificActivityName(activityInfo[0]));
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					Intent.ShortcutIconResource.fromContext(hostActivity, R.drawable.ic_launcher));
				addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
				hostActivity.sendBroadcast(addIntent);
			}
			catch (ClassNotFoundException e) {
				Log.d("DebugMenu", "Unable to install shortcuts", e);
				Toast.makeText(hostActivity, "Unable to install shortcuts; see logs", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void startTestActivity(String className) {
		try {
			Intent intent = new Intent(hostActivity, Class.forName(className));
			hostActivity.startActivity(intent);
		}
		catch (ClassNotFoundException e) {
			Log.d("DebugMenu", "Unable to launch activity", e);
			Toast.makeText(hostActivity, "Unable to launch activity; see logs", Toast.LENGTH_LONG).show();
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

	private String getBuildSpecificActivityName(String name) {
		String[] packageParts = hostActivity.getPackageName().split("\\.");
		return name + " " + packageParts[packageParts.length - 1];
	}

}
