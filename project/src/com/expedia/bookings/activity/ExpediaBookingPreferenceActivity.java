package com.expedia.bookings.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.expedia.bookings.R;
import com.mobiata.hotellib.utils.CurrencyUtils;

public class ExpediaBookingPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Setup the default currency so it states what it considers default
		// Depends on the first value being the default, so don't change the order
		ListPreference currencyPref = (ListPreference) findPreference(getString(R.string.CurrencyKey));
		CharSequence[] entries = currencyPref.getEntries();
		entries[0] = getString(R.string.default_currency_template, CurrencyUtils.getDefaultCurrencyCode(this));
		currencyPref.setEntries(entries);

		// If the result is canceled, means no prefs were modified
		setResult(RESULT_CANCELED);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		// This is not a foolproof way to determine if preferences were changed, but
		// it's close enough.
		setResult(RESULT_OK);

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
