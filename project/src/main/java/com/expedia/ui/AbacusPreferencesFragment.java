package com.expedia.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

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

	private Preference.OnPreferenceChangeListener abacusPrefListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			int value = Integer.valueOf(newValue.toString());
			Db.getAbacusResponse().updateABTestForDebug(Integer.valueOf(preference.getKey()), value);
			return true;
		}
	};
}
