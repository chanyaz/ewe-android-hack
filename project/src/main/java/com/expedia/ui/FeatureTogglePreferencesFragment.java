package com.expedia.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FeatureToggleUtil;
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

		// Hotel Features
		initializeFeatureCheck(R.string.preference_enable_hotel_favorite, ToggleFeatureConfiguration.HOTEL_FAVORITE_FEATURE);
		// Login Features
		initializeFeatureCheck(R.string.preference_enable_smart_lock, ToggleFeatureConfiguration.SMART_LOCK_FEATURE);

	}

	private void initializeFeatureCheck(int featureKey, Boolean isFeatureEnabled) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil
			.isFeatureEnabled(getActivity(),
				featureKey, isFeatureEnabled);
		featurePreference
			.setChecked(enableFeature);
	}

}
