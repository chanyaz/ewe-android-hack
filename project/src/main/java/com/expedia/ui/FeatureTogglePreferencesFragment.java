package com.expedia.ui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
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
		String hotelFavoriteKey = getString(R.string.preference_enable_hotel_favorite);
		CheckBoxPreference hotelFavoritePreference = (CheckBoxPreference) findPreference(hotelFavoriteKey);
		hotelFavoritePreference.setChecked(ToggleFeatureConfiguration.HOTEL_FAVORITE_FEATURE);

		// Login Features
		String smartLockKey = getString(R.string.preference_enable_smart_lock);
		CheckBoxPreference smartLockPreference = (CheckBoxPreference) findPreference(smartLockKey);
		boolean isSmartLockFeatureEnabled = FeatureToggleUtil
			.isUserBucketedAndFeatureEnabled(getActivity(), AbacusUtils.EBAndroidAppSmartLockTest,
				R.string.preference_enable_smart_lock, ToggleFeatureConfiguration.SMART_LOCK_FEATURE);
		smartLockPreference
			.setChecked(isSmartLockFeatureEnabled);

	}

}
