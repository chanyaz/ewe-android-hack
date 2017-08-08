
package com.expedia.bookings.preference;

import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.FeatureToggleUtil;

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

		// Login Features
		initializeFeatureCheck(R.string.preference_enable_smart_lock);
		initializeFeatureCheck(R.string.preference_single_page_sign_up);
		initializeFeatureCheck(R.string.preference_enable_activity_map);
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.preference_enable_secure_icon);

		//hotel features
		initializeFeatureCheck(R.string.preference_populate_cardholder_name);

		//Flight features
		initializeFeatureCheck(R.string.preference_enable_flights_frequent_flyer_number);
		//Packages Features
		initializeFeatureCheck(R.string.preference_packages_mid_api);
		initializeFeatureCheck(R.string.preference_packages_title_change);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}
}
