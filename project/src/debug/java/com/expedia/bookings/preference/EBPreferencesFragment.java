package com.expedia.bookings.preference;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.BugShakerShim;
import com.expedia.bookings.utils.ChuckShim;
import com.expedia.util.PermissionsHelperKt;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class EBPreferencesFragment extends BasePreferenceFragment {

	private static final String GCM_ID_POPUP_TAG = "GCM_ID_POPUP_TAG";

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.Settings);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (BuildConfig.DEBUG) {
			addPreferencesFromResource(R.xml.preferences_dev);

			String apiKey = getString(R.string.preference_which_api_to_use_key);
			ListPreference apiPref = (ListPreference) findPreference(apiKey);
			apiPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, final Object newValue) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AccountDialogTheme);
					builder.setTitle("Server Switching");
					builder.setMessage(getActivity().getResources().getString(R.string.server_change_message));
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							SettingUtils.save(getActivity(), getString(R.string.preference_disable_modern_https_security), !newValue.toString().equals("Production"));
							SettingUtils.save(getActivity(), getString(R.string.preference_which_api_to_use_key), newValue.toString());
							restartApp();
						}
					});
					builder.create().show();
					return true;
				}
			});

			String picassoKey = getString(R.string.preference_enable_picasso_logging);
			CheckBoxPreference picassoPreference = (CheckBoxPreference) findPreference(picassoKey);
			picassoPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					boolean isLoggingEnabled = Boolean.valueOf(newValue.toString());
					new PicassoHelper.Builder(getActivity()).build()
						.setLoggingEnabled(isLoggingEnabled);
					return true;
				}
			});

			String bugShakerKey = getString(R.string.preference_enable_bugshaker);
			final CheckBoxPreference bugShakerPreference = (CheckBoxPreference) findPreference(bugShakerKey);

			bugShakerPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					boolean isBugshakerEnabled = Boolean.valueOf(newValue.toString());
					if (isBugshakerEnabled) {
						boolean permissionForExternalStorage = PermissionsHelperKt
							.hasPermissionToWriteToExternalStorage(getActivity());
						if (!permissionForExternalStorage) {
							PermissionsHelperKt.requestWriteToExternalStoragePermission(getActivity());
						}
						else {
							BugShakerShim.startNewBugShaker(getActivity().getApplication());
						}
					}
					else {
						BugShakerShim.turnOff();
					}
					return true;
				}
			});

			String chuckNotificationKey = getString(R.string.preference_enable_chuck_notification);
			final CheckBoxPreference chuckNotificationPreference = (CheckBoxPreference) findPreference(chuckNotificationKey);
			chuckNotificationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					SettingUtils.save(getActivity(), getString(R.string.preference_enable_chuck_notification), (boolean)newValue);
					restartApp();
					return true;
				}
			});

		}
	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		GCMIdDialogPreferenceFragment dialogFragment;
		if (preference instanceof GCMIdDialogPreference) {
			dialogFragment = GCMIdDialogPreferenceFragment.newInstance(preference.getKey());
			dialogFragment.setTargetFragment(this, 0);
			dialogFragment.show(this.getFragmentManager(), GCM_ID_POPUP_TAG);
		}
		else {
			super.onDisplayPreferenceDialog(preference);
		}
	}

	private void restartApp() {
		Intent mStartActivity = new Intent(getActivity(), RouterActivity.class);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent
			.getActivity(getActivity(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		System.exit(0);
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		final ExpediaBookingPreferenceActivity activity = (ExpediaBookingPreferenceActivity) getActivity();
		String key = preference.getKey();

		// This is not a foolproof way to determine if preferences were changed, but
		// it's close enough
		activity.changedPrefs();

		if (getString(R.string.preference_force_fs_db_update).equals(key)) {
			try {
				FlightStatsDbUtils.setUpgradeCutoff(0);
				FlightStatsDbUtils.createDatabaseIfNotExists(getActivity(), BuildConfig.RELEASE);
			}
			catch (IOException e) {
				Log.w("Could not force update FS.db", e);
			}
		}
		else if (getString(R.string.preference_open_abacus_settings).equals(key)) {
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new AbacusPreferencesFragment())
				.addToBackStack(AbacusPreferencesFragment.class.getName())
				.commit();
			return true;
		}

		else if (getString(R.string.preference_open_feature_toggle_settings).equals(key)) {
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new FeatureTogglePreferencesFragment())
				.addToBackStack(FeatureTogglePreferencesFragment.class.getName())
				.commit();
			return true;
		}
		else if (getString(R.string.preference_clear_user_cookies).equals(key)) {
			ExpediaServices.removeUserCookieFromUserLoginCookies(getActivity());
		}
		else if (getString(R.string.preference_chuck_show_ui).equals(key)) {
			Intent intent = ChuckShim.getLaunchIntent(getActivity());
			if (intent != null) {
				startActivity(intent);
			}
		}
		else if ("PREF_FIRST_LAUNCH".equals(key)) {
			User.signOut(getActivity());
		}

		return super.onPreferenceTreeClick(preference);
	}
}