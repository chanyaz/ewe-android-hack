
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
		initializeFeatureCheck(R.string.preference_enable_activity_map);

		//Flight Features

		//Packages Features

		//Universal Checkout Features
		initializeFeatureCheck(R.string.preference_enable_universal_checkout_on_lx);
		initializeFeatureCheck(R.string.pay_later_credit_card_messaging);

		//Flight Features
		initializeFeatureCheck(R.string.preference_flight_rate_detail_from_cache);

		//Itin Features
		initializeFeatureCheck(R.string.preference_enable_universal_deeplink);
	}

	private void initializeFeatureCheck(int featureKey) {
		CheckBoxPreference featurePreference = (CheckBoxPreference) findPreference(getString(featureKey));
		boolean enableFeature = FeatureToggleUtil.isFeatureEnabled(getActivity(), featureKey);
		featurePreference.setChecked(enableFeature);
	}
}
