package com.expedia.bookings.utils;

import java.net.URL;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class MockModeShim {

	private static MockWebServer server = null;
	private static ExpediaDispatcher dispatcher = null;

	public static void initMockWebServer(Context c) {
		final Context context = c.getApplicationContext();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (server != null) {
						server.shutdown();
						dispatcher = null;
						server = null;
					}

					server = new MockWebServer();
					server.start();
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to init MockWebServer, wut?", e);
				}

				AndroidFileOpener opener = new AndroidFileOpener(context);
				dispatcher = new ExpediaDispatcher(opener);
				server.setDispatcher(dispatcher);

				// Persist MockWebServer address to be used for the services classes
				URL mockUrl = server.getUrl("");
				String server = mockUrl.getHost() + ":" + mockUrl.getPort();
				SettingUtils.save(context, R.string.preference_proxy_server_address, server);
				SettingUtils.save(context, R.string.preference_force_custom_server_http_only, true);
			}
		}).start();
	}

	public static ExpediaDispatcher getDispatcher() {
		return dispatcher;
	}
}
