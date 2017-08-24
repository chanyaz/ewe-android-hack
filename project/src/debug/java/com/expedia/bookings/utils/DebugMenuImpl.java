package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.mobiata.android.Log;

class DebugMenuImpl implements DebugMenu {
	private final Activity hostActivity;
	private final Class<? extends Activity> settingsActivityClass;
	private final List<DebugActivityInfo> debugActivities = new ArrayList<>();

	DebugMenuImpl(@NonNull Activity hostActivity, @Nullable Class<? extends Activity> settingsActivityClass) {
		this.hostActivity = hostActivity;
		this.settingsActivityClass = settingsActivityClass;
		try {
			PackageManager pm = hostActivity.getPackageManager();
			ActivityInfo[] activities = pm.getPackageInfo(hostActivity.getPackageName(),
				PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA).activities;
			for (ActivityInfo ai : activities) {
				if (ai.metaData != null && ai.metaData.getBoolean("debug", false)) {
					DebugActivityInfo dai = new DebugActivityInfo();
					dai.className = ai.name;
					dai.displayName = ai.loadLabel(pm).toString();
					debugActivities.add(dai);
				}
			}
		}
		catch (PackageManager.NameNotFoundException e) {
			Log.d("DebugMenu", "Failed to load debug activity list", e);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(hostActivity);
		inflater.inflate(R.menu.menu_debug, menu);
		if (debugActivities.size() > 0) {
			Menu activityMenu = menu.addSubMenu(R.string.debug_screens_sub_menu);
			for (final DebugActivityInfo activityInfo : debugActivities) {
				MenuItem item = activityMenu.add(activityInfo.displayName);
				item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						startTestActivity(activityInfo.className);
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

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.debug_menu_settings).setVisible((settingsActivityClass != null));
		updateStatus(menu);
	}

	@Override
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

	@Override
	public List<DebugActivityInfo> getDebugActivityInfoList() {
		return Collections.unmodifiableList(debugActivities);
	}

	@Override
	public void addShortcutsForAllLaunchers() {
		for (DebugActivityInfo activityInfo : debugActivities) {
			try {
				Intent shortcutIntent = new Intent(hostActivity, Class.forName(activityInfo.className));
				shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
					installShortcutToHomeScreen(activityInfo, shortcutIntent);
				}
				else {
					createLongPressShortcut(activityInfo, shortcutIntent);
				}
			}
			catch (ClassNotFoundException e) {
				Log.d("DebugMenu", "Unable to install shortcuts", e);
				Toast.makeText(hostActivity, "Unable to install shortcuts; see logs", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void installShortcutToHomeScreen(DebugActivityInfo activityInfo, Intent shortcutIntent) {
		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getBuildSpecificActivityName(activityInfo.displayName));
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(hostActivity, R.drawable.ic_launcher));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		hostActivity.sendBroadcast(addIntent);
	}

	@TargetApi(25)
	private void createLongPressShortcut(DebugActivityInfo activityInfo, Intent shortcutIntent) {
		Context context = hostActivity.getApplicationContext();
		ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
		shortcutManager.removeAllDynamicShortcuts();
		ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, activityInfo.displayName.replace(" ", ""))
				.setShortLabel(activityInfo.displayName)
				.setIcon(Icon.createWithResource(context, R.drawable.ic_launcher))
				.setIntent(shortcutIntent.setAction(Intent.ACTION_MAIN))
				.build();

		shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcutInfo));
	}

	@Override
	public Intent getSettingActivityIntent() {
		return new Intent(hostActivity, settingsActivityClass);
	}

	@Override
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
