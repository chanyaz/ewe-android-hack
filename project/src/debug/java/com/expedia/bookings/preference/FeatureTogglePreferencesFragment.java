
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

		// Hotel Features
		initializeFeatureCheck(R.string.preference_dateless_infosite);

		// Login Features
		initializeFeatureCheck(R.string.preference_enable_smart_lock);
		initializeFeatureCheck(R.string.preference_enable_activity_map);

		//Flight Features

		//Packages Features
		initializeFeatureCheck(R.string.preference_packages_mid_api);

		//Universal Checkout Features
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.preference_populate_cardholder_name);
		initializeFeatureCheck(R.string.preference_enable_secure_icon);
		initializeFeatureCheck(R.string.preference_allow_unknown_card_types);
		initializeFeatureCheck(R.string.preference_show_flights_checkout_webview);
		initializeFeatureCheck(R.string.preference_display_eligible_cards_on_payment_form);

		//Flight Features
		initializeFeatureCheck(R.string.preference_flight_rate_detail_from_cache);

		//Itin Features
		initializeFeatureCheck(R.string.preference_trips_hotel_scheduled_notifications);
		initializeFeatureCheck(R.string.preference_trips_new_flights_design);

		// Permission Request
		initializeFeatureCheck(R.string.preference_soft_prompt_permission);

		// Carnival Notifications
		initializeFeatureCheck(R.string.preference_new_carnival_notifications);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}
}
