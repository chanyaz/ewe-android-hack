
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
		initializeFeatureCheck(R.string.preference_user_search_history);
		initializeFeatureCheck(R.string.preference_enable_disabled_stp_hotels);

		// Login Features
		initializeFeatureCheck(R.string.preference_enable_smart_lock);
		initializeFeatureCheck(R.string.preference_enable_activity_map);
		initializeFeatureCheck(R.string.preference_enable_lx_mod);

		//Flight Features

		//Packages Features
		initializeFeatureCheck(R.string.preference_packages_mid_api);
		initializeFeatureCheck(R.string.preference_packages_breadcrumbs);

		//Universal Checkout Features
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.preference_show_flights_checkout_webview);
		initializeFeatureCheck(R.string.pay_later_credit_card_messaging);
		initializeFeatureCheck(R.string.preference_enable_hotel_material_forms);
		initializeFeatureCheck(R.string.preference_enable_mid_checkout);

		//Flight Features
		initializeFeatureCheck(R.string.preference_flight_rate_detail_from_cache);
		initializeFeatureCheck(R.string.preference_enable_krazy_glue_on_flights_confirmation);

		//Itin Features
		initializeFeatureCheck(R.string.preference_trips_new_flights_design);
		initializeFeatureCheck(R.string.preference_trips_new_flights_managing_booking_design);
		initializeFeatureCheck(R.string.preference_enable_trips_flight_alerts);

		// Other
		initializeFeatureCheck(R.string.preference_soft_prompt_permission);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}
}
