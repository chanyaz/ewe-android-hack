package com.expedia.bookings.preference;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Courier;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager;
import com.expedia.bookings.fragment.SelectLanguageDialogFragment;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.TNSServices;
import com.expedia.bookings.trace.fragment.ServerDebugTracingPreferenceFragment;
import com.expedia.bookings.utils.ChuckShim;
import com.expedia.bookings.utils.UniqueIdentifierPersistenceProvider;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UniqueIdentifierHelper;
import com.expedia.bookings.vm.DebugSelectLanguageVM;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

import java.io.File;
import java.io.IOException;

import io.reactivex.functions.Consumer;
import kotlin.Unit;

public class EBPreferencesFragment extends BasePreferenceFragment {

	private static final String GCM_ID_POPUP_TAG = "GCM_ID_POPUP_TAG";
	private UserStateManager userStateManager;

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle(R.string.Settings);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userStateManager = Ui.getApplication(getContext()).appComponent().userStateManager();

		if (BuildConfig.DEBUG) {
			addPreferencesFromResource(R.xml.preferences_dev);

			String apiKey = getString(R.string.preference_which_api_to_use_key);
			ListPreference apiPref = (ListPreference) findPreference(apiKey);
			apiPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, final Object newValue) {
					showChangeServerDialog(newValue);
					return true;
				}
			});

			String retainPrevFlightSearchParamsKey = getString(R.string.preference_enable_retain_prev_flight_search_params);
			final CheckBoxPreference retainPrevFlightSearchParamsPreference = (CheckBoxPreference) findPreference(retainPrevFlightSearchParamsKey);
			retainPrevFlightSearchParamsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					SettingUtils.save(getActivity(), getString(R.string.preference_enable_retain_prev_flight_search_params), (boolean)newValue);
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

			String tnsServerEndpointKey = getString(R.string.preference_push_notification_tns_server);
			final CheckBoxPreference tnsServerEndpointPreference = (CheckBoxPreference) findPreference(tnsServerEndpointKey);
			tnsServerEndpointPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, final Object newValue) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AccountDialogTheme);
					builder.setTitle("TNS Server Switching");
					builder.setMessage(getActivity().getResources().getString(R.string.server_change_message));
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							SettingUtils.save(getActivity(), getString(R.string.preference_push_notification_tns_server), (boolean) newValue);
							deregisterFromExistingTNSServer();
							restartApp();
						}
					});
					builder.create().show();
					return true;
				}
			});
		}
	}

	@VisibleForTesting
	public AlertDialog showChangeServerDialog(final Object newValue) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AccountDialogTheme);
		builder.setTitle("Server Switching");
		builder.setMessage(getActivity().getResources().getString(R.string.server_change_message));
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {


				SettingUtils.save(getActivity(), getString(R.string.preference_disable_modern_https_security),
					!newValue.toString().equals("Production"));
				SettingUtils
					.save(getActivity(), getString(R.string.preference_which_api_to_use_key), newValue.toString());
				SatelliteFeatureConfigManager.clearFeatureConfig(getContext());
				restartApp();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		return alertDialog;
	}

	private void deregisterFromExistingTNSServer() {
		TNSServices tnsServices = Ui.getApplication(getActivity()).appComponent().tnsService();
		String regId = GCMRegistrationKeeper.getInstance(getActivity()).getRegistrationId(getActivity());
		int langId = PointOfSale.getPointOfSale().getDualLanguageId();
		if (!TextUtils.isEmpty(regId)) {
			tnsServices.deregisterDevice(new Courier("gcm", Integer.toString(langId), BuildConfig.APPLICATION_ID,
				regId, UniqueIdentifierHelper.getID(new UniqueIdentifierPersistenceProvider(getContext()))));
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

	protected void restartApp() {
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
		else if (getString(R.string.preference_open_remote_feature_toggle_settings).equals(key)) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new RemoteFeaturesListFragment())
					.addToBackStack(RemoteFeaturesListFragment.class.getName())
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
			userStateManager.signOut();
			SettingUtils.save(getContext(), PhoneLaunchActivity.PREF_USER_ENTERS_FROM_SIGNIN, false);
			SettingUtils.save(getContext(), PhoneLaunchActivity.PREF_LOCATION_PERMISSION_PROMPT_TIMES, 0);
		}
		else if (getString(R.string.preference_which_lang_to_use_key).equals(key)) {
			final SelectLanguageDialogFragment dialogFragment = new SelectLanguageDialogFragment();

			final DebugSelectLanguageVM vm = new DebugSelectLanguageVM();
			vm.getRestartAppSubject().subscribe(new Consumer<Unit>() {
				@Override
				public void accept(Unit restartApp) {
					if (getView() != null) {
						getView().postDelayed(new Runnable() {
							@Override
							public void run() {
								restartApp();
							}
						}, 500);
					}
				}
			});

			dialogFragment.setViewModel(vm);
			dialogFragment.show(getFragmentManager(), "tag_select_lang_dialog_fragment");
			return true;
		}
		else if (getString(R.string.preference_trips_store_json_files).equals(key)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AccountDialogTheme);
			builder.setTitle("Trips Store");
			StringBuilder sb = new StringBuilder();
			File tripsDirectory = getContext().getDir("TRIPS_JSON_STORE", Context.MODE_PRIVATE);
			String[] storedFiles = null;
			if (tripsDirectory.exists()) {
				storedFiles = tripsDirectory.list();
			}
			if (storedFiles != null) {
				sb.append("Number of storedFiles: ");
				sb.append(storedFiles.length);
				sb.append("\n\nFiles: ");
				for (String file : storedFiles) {
					sb.append("\n\n");
					sb.append(file);
				}
			}
			else {
				sb.append("Trips directory does not exist");
			}
			builder.setMessage(sb.toString());
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
			return true;
		}
		else if (getString(R.string.preference_trips_mock_scenarios).equals(key)) {
			final TripsScenarioSelectFragment fragment = new TripsScenarioSelectFragment();
			fragment.viewModel = new TripScenariosViewModel();
			fragment.viewModel.getRestartAppSubject().subscribe(new Consumer<Unit>() {
				@Override
				public void accept(Unit restartApp) {
					restartApp();
				}
			});
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.addToBackStack(TripsScenarioSelectFragment.class.getName())
				.commit();
			return true;
		}
		else if (getString(R.string.preference_server_debug_tracing).equals(key)) {
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_container, new ServerDebugTracingPreferenceFragment())
				.addToBackStack(ServerDebugTracingPreferenceFragment.class.getName())
				.commit();
			return true;
		}

		return super.onPreferenceTreeClick(preference);
	}
}
