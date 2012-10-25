package com.expedia.bookings.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.dialog.ClearPrivateDataDialogPreference;
import com.expedia.bookings.dialog.ClearPrivateDataDialogPreference.ClearPrivateDataListener;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.util.AndroidUtils;

public class ExpediaBookingPreferenceActivity extends SherlockPreferenceActivity implements ClearPrivateDataListener {
	public static final int RESULT_POS_CHANGED = 1;

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
					User.signOut(ExpediaBookingPreferenceActivity.this);
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
				configurePointOfSalePreferenceSummary((String) newValue);
				LocaleUtils.onPointOfSaleChanged(ExpediaBookingPreferenceActivity.this);
				setResult(RESULT_POS_CHANGED);
				return true;
			}
		});

		configurePointOfSalePreferenceSummary(pointOfSalePref.getValue());

		// If the result is canceled, means no prefs were modified
		setResult(RESULT_CANCELED);
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
		// This is not a foolproof way to determine if preferences were changed, but
		// it's close enough.
		if (!preference.getKey().equals(getString(R.string.preference_clear_private_data_key))) {
			setResult(RESULT_OK);
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
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
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
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_CLEAR_DATA);
				}
			});
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
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
	public void configurePointOfSalePreferenceSummary(String pos) {
		PreferenceManager pm = getPreferenceManager();
		Preference pointOfSalePref = pm.findPreference(getString(R.string.PointOfSaleKey));
		pointOfSalePref.setSummary(pos);
	}

	@Override
	public void onClearPrivateDate(boolean signedOut) {
		showDialog(signedOut ? DIALOG_CLEAR_DATA_SIGNED_OUT : DIALOG_CLEAR_DATA);
	}
}
