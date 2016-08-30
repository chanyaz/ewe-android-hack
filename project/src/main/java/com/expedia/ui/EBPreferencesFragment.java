package com.expedia.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.ExpediaBookingPreferenceActivity;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.MockModeShim;
import com.expedia.util.PermissionsHelperKt;
import com.github.stkent.bugshaker.BugShaker;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import java.io.IOException;

public class EBPreferencesFragment extends BasePreferenceFragment {

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
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if ("Mock Mode".equals(newValue)) {
						MockModeShim.initMockWebServer(getActivity());
					}
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
							ExpediaBookingApp.startNewBugShaker(getActivity().getApplication());
						}
					}
					else {
						BugShaker.turnOff();
					}
					return true;
				}
			});
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
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

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
