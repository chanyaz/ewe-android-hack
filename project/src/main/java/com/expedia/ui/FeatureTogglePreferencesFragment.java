
package com.expedia.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;

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
		initializeFeatureCheck(R.string.preference_enable_activity_map);
		initializeFeatureCheck(R.string.preference_itin_card_detail);
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.preference_enable_additional_content_flight_confirmation);
		initializeFeatureCheck(R.string.preference_enable_flex);
		initializeFeatureCheck(R.string.preference_payment_legal_message);
		//hotel features
		//trips features
		initializeFeatureCheck(R.string.preference_share_button_remove_calendar);
		//Flight features
		initializeFeatureCheck(R.string.preference_flight_retain_search_params);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}
}
