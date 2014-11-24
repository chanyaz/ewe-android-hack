package com.expedia.bookings.test.ui.tablet.pagemodels;

import android.app.Instrumentation;
import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

public class Settings {
	public static void clearPrivateData(Instrumentation inst) {
		ClearPrivateDataUtil.clear(inst.getTargetContext());
	}

	public static void setCustomServer(Instrumentation inst, String server) {
		Context c = inst.getTargetContext();
		SettingUtils.save(c, getString(c, R.string.preference_which_api_to_use_key), "Custom Server");
		SettingUtils.save(c, getString(c, R.string.preference_proxy_server_address), server);
		SettingUtils.save(c, getString(c, R.string.preference_force_custom_server_http_only), true);
	}

	public static void setServer(Instrumentation inst, String api) {
		Context c = inst.getTargetContext();
		SettingUtils.save(c, getString(c, R.string.preference_which_api_to_use_key), api);
	}

	public static void setFakeCurrentLocation(Instrumentation inst, String lat, String lng) {
		Context c = inst.getTargetContext();
		SettingUtils.save(c, getString(c, R.string.preference_fake_current_location), lat + "," + lng);
	}

	private static String getString(Context context, int id) {
		return context.getResources().getString(id);
	}
}
