package com.expedia.bookings.activity;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ClearPrivateDataDialogPreference;
import com.expedia.bookings.dialog.ClearPrivateDataDialogPreference.ClearPrivateDataListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class ExpediaBookingPreferenceActivity extends SherlockPreferenceActivity implements ClearPrivateDataListener {
	public static final int RESULT_NO_CHANGES = 1;
	public static final int RESULT_CHANGED_PREFS = 2;

	private static final int DIALOG_CLEAR_DATA = 0;
	private static final int DIALOG_CLEAR_DATA_SIGNED_OUT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		if (!AndroidUtils.isRelease(this)) {
			addPreferencesFromResource(R.xml.preferences_dev);

			String apiKey = getString(R.string.preference_which_api_to_use_key);
			ListPreference apiPref = (ListPreference) findPreference(apiKey);
			apiPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					ClearPrivateDataUtil.clear(ExpediaBookingPreferenceActivity.this);
					return true;
				}
			});
		}

		String clearPrivateDateKey = getString(R.string.preference_clear_private_data_key);
		String pointOfSaleKey = getString(R.string.PointOfSaleKey);

		ClearPrivateDataDialogPreference clearPrivateDataPreference = (ClearPrivateDataDialogPreference) findPreference(clearPrivateDateKey);
		ListPreference pointOfSalePref = (ListPreference) findPreference(pointOfSaleKey);

		clearPrivateDataPreference.setClearPrivateDataListener(this);
		pointOfSalePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PointOfSale.onPointOfSaleChanged(ExpediaBookingPreferenceActivity.this);
				configurePointOfSalePreferenceSummary();

				setResult(RESULT_CHANGED_PREFS);

				// IMPORTANT: DomainPreference purposefully breaks the contract a bit.  Changing
				// this to "false" will not prevent the preference change from continuing without
				// modifying DomainPreference as well.
				return true;
			}
		});

		configurePointOfSalePreferenceSummary();

		// By default, assume nothing changed
		setResult(RESULT_NO_CHANGES);
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String key = preference.getKey();

		// This is not a foolproof way to determine if preferences were changed, but
		// it's close enough; should only affect dev options
		if (!key.equals(getString(R.string.preference_clear_private_data_key))
				&& !key.equals(getString(R.string.PointOfSaleKey))) {
			setResult(RESULT_CHANGED_PREFS);
		}

		if (key.equals(getString(R.string.preference_stubconfig_page))) {
			startActivity(new Intent(this, StubConfigActivity.class));
		}
		else if (key.equals(getString(R.string.preference_force_fs_db_update))) {
			try {
				FlightStatsDbUtils.setUpgradeCutoff(0);
				FlightStatsDbUtils.createDatabaseIfNotExists(this);
			}
			catch (IOException e) {
				Log.w("Could not force update FS.db", e);
			}
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CLEAR_DATA: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title_cleared_private_data);
			builder.setMessage(R.string.dialog_message_cleared_private_data);
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});
			builder.setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});

			return builder.create();
		}
		case DIALOG_CLEAR_DATA_SIGNED_OUT: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title_logged_out_and_cleared_private_data);
			builder.setMessage(R.string.dialog_message_logged_out_and_cleared_private_data);
			if (ExpediaBookingApp.IS_TRAVELOCITY) {
				builder.setMessage(R.string.dialog_message_logged_out_and_cleared_private_data_tvly);
			}
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});
			builder.setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});

			return builder.create();
		}
		}

		return super.onCreateDialog(id);
	}

	// Sets the currency summary to display whichever point of sale is currently selected
	public void configurePointOfSalePreferenceSummary() {
		PreferenceManager pm = getPreferenceManager();
		Preference pointOfSalePref = pm.findPreference(getString(R.string.PointOfSaleKey));
		pointOfSalePref.setSummary(PointOfSale.getPointOfSale().getUrl());
	}

	@Override
	public void onClearPrivateData(boolean signedOut) {
		showDialog(signedOut ? DIALOG_CLEAR_DATA_SIGNED_OUT : DIALOG_CLEAR_DATA);

		setResult(RESULT_CHANGED_PREFS);
	}
}
