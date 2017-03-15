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
		initializeFeatureCheck(R.string.preference_enable_hotel_loyalty_earn_message);
		initializeFeatureCheck(R.string.preference_itin_card_detail);
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.preference_enable_3DS_checkout);
		initializeFeatureCheck(R.string.preference_universal_checkout_material_forms);
		initializeFeatureCheck(R.string.preference_itin_new_sign_in_screen);
		initializeFeatureCheck(R.string.preference_flight_byot);
		initializeFeatureCheck(R.string.preference_member_deal_on_launch_screen);
		initializeFeatureCheck(R.string.preference_show_air_attach_message_on_launch_screen);
		initializeFeatureCheck(R.string.preference_enable_payment_traveler_updated_strings);
		initializeFeatureCheck(R.string.preference_guest_itin_on_launch);
		initializeFeatureCheck(R.string.preference_active_itin_on_launch);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}

}
