package com.expedia.bookings.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.CurrencyUtils;

public class ExpediaBookingPreferenceActivity extends PreferenceActivity {

	private String mDefaultCurrency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		mDefaultCurrency = CurrencyUtils.getDefaultCurrencyCode(this);

		// Setup the default currency so it states what it considers default
		// Depends on the first value being the default, so don't change the order
		ListPreference currencyPref = (ListPreference) findPreference(getString(R.string.CurrencyKey));
		CharSequence[] entries = currencyPref.getEntries();
		entries[0] = getString(R.string.default_currency_template, mDefaultCurrency);
		currencyPref.setEntries(entries);

		currencyPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				configureCurrencyPreferenceSummary((String) newValue);
				return true;
			}
		});

		configureCurrencyPreferenceSummary(CurrencyUtils.getCurrencyCode(this));

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

	// Sets the currency summary to display whichever currency is currently selected
	public void configureCurrencyPreferenceSummary(String currencyCode) {
		if (currencyCode.equals(getString(R.string.CurrencyDefault))) {
			currencyCode = mDefaultCurrency;
		}
		String currencyName = CurrencyUtils.getCurrencyName(this, currencyCode);

		PreferenceManager pm = getPreferenceManager();
		Preference currencyPref = pm.findPreference(getString(R.string.CurrencyKey));
		currencyPref.setSummary(getString(R.string.preference_currency_summary_template, currencyName, currencyCode));
	}
}
