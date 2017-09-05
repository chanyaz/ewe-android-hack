package com.expedia.bookings.test;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.FailedUrlCache;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.android.util.SettingUtils;

public class Settings {
	public static void clearPrivateData() {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Images.setCustomHost(null);
		ClearPrivateDataUtil.clear(inst.getTargetContext());
		FailedUrlCache.getInstance().clearCache();
	}

	public static void setMockModeEndPoint() {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Context c = inst.getTargetContext();
		MockModeShim.initMockWebServer(c);
		Images.setCustomHost("http://localhost:80/");
		SettingUtils.save(c, getString(R.string.preference_which_api_to_use_key), "Mock Mode");
	}

	public static void setCustomServer(String server) {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Context c = inst.getTargetContext();
		Images.setCustomHost(server);
		SettingUtils.save(c, getString(R.string.preference_which_api_to_use_key), "Custom Server");
		SettingUtils.save(c, getString(R.string.preference_proxy_server_address), server);
	}

	public static void setServer(String api) {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Context c = inst.getTargetContext();
		Images.setCustomHost(null);
		SettingUtils.save(c, getString(R.string.preference_which_api_to_use_key), api);
	}

	public static void setFakeCurrentLocation(String lat, String lng) {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Context c = inst.getTargetContext();
		SettingUtils.save(c, getString(R.string.preference_fake_current_location), lat + "," + lng);
	}

	public static void setOnboardingScreenVisibility(boolean visibility) {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Context c = inst.getTargetContext();
		SettingUtils.save(c, "PREF_FIRST_LAUNCH", visibility);
	}

	private static String getString(int id) {
		Instrumentation inst = InstrumentationRegistry.getInstrumentation();
		Context context = inst.getTargetContext();
		return context.getResources().getString(id);
	}
}
