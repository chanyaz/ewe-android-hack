package com.expedia.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;

import com.expedia.bookings.R;
import com.expedia.util.ToggleFeatureConfiguration;

public class FeatureTogglePreferencesFragment extends BasePreferenceFragment {

	@Override
	public void onStart() {
		super.onStart();
		getActivity().setTitle("Feature Toggle");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_feature_toggle);
		String smartLockKey = getString(R.string.preference_enable_smart_lock);
		CheckBoxPreference smartLockPreference = (CheckBoxPreference) findPreference(smartLockKey);
		smartLockPreference.setChecked(ToggleFeatureConfiguration.SMART_LOCK_FEATURE);

	}

}
