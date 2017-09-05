
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
		initializeFeatureCheck(R.string.preference_enable_activity_map);

		//Flight Features

		//Packages Features
		initializeFeatureCheck(R.string.preference_packages_mid_api);

		//Universal Checkout Features
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.preference_populate_cardholder_name);
		initializeFeatureCheck(R.string.preference_enable_secure_icon);
		initializeFeatureCheck(R.string.preference_enable_flights_frequent_flyer_number);
		initializeFeatureCheck(R.string.preference_hide_form_fields_based_on_billing_country_address);

		//Flight Features
		initializeFeatureCheck(R.string.preference_flight_rate_detail_from_cache);

		//Itin Features
		initializeFeatureCheck(R.string.preference_trips_hotel_scheduled_notifications);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}
}
