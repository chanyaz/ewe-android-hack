package com.expedia.bookings.preference;

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;

public class AbacusPreferencesFragment extends BasePreferenceFragment {

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle("Abacus Tests");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_abacus);

		for (int key : AbacusUtils.getActiveTests()) {
			ListPreference preference = (ListPreference) findPreference(String.valueOf(key));
			preference.setOnPreferenceChangeListener(abacusPrefListener);
		}
	}

	private final Preference.OnPreferenceChangeListener abacusPrefListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			int value = Integer.valueOf(newValue.toString());
			Db.getAbacusResponse().updateABTestForDebug(Integer.valueOf(preference.getKey()), value);
			return true;
		}
	};
}
