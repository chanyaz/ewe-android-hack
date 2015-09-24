package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.ExpediaDispatcher;

public class MockModeShim {

	private static ExpediaMockWebServer server = null;

	public static void initMockWebServer(Context c) {
		final Context context = c.getApplicationContext();
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (server != null) {
					server.shutdown();
					server = null;
				}

				server = new ExpediaMockWebServer(context);

				// Persist MockWebServer address to be used for the services classes
				SettingUtils.save(context, R.string.preference_proxy_server_address, server.getHostWithPort());
				SettingUtils.save(context, R.string.preference_force_custom_server_http_only, true);
			}
		}).start();
	}

	public static ExpediaDispatcher getDispatcher() {
		return server.getDispatcher();
	}
}
