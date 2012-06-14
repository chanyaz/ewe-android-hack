package com.expedia.bookings.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.expedia.bookings.R;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.util.AndroidUtils;

public class ExpediaBookingPreferenceActivity extends PreferenceActivity {
	public static final int RESULT_POS_CHANGED = 1;

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
					ExpediaServices expedia = new ExpediaServices(ExpediaBookingPreferenceActivity.this);
					expedia.signOut();
					return true;
				}
			});
		}

		String pointOfSaleKey = getString(R.string.PointOfSaleKey);

		ListPreference pointOfSalePref = (ListPreference) findPreference(pointOfSaleKey);

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
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		// This is not a foolproof way to determine if preferences were changed, but
		// it's close enough.
		if (!preference.getKey().equals(getString(R.string.preference_clear_private_data_key))) {
			setResult(RESULT_OK);
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	// Sets the currency summary to display whichever point of sale is currently selected
	public void configurePointOfSalePreferenceSummary(String pos) {
		PreferenceManager pm = getPreferenceManager();
		Preference pointOfSalePref = pm.findPreference(getString(R.string.PointOfSaleKey));
		pointOfSalePref.setSummary(pos);
	}
}
