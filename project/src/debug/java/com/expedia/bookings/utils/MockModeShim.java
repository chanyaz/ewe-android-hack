package com.expedia.bookings.utils;

import java.net.URL;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class MockModeShim {

	private static ExpediaDispatcher dispatcher = null;

	public static void initMockWebServer(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				MockWebServer mockWebServer = new MockWebServer();
				try {
					mockWebServer.start();
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to init MockWebServer, wut?", e);
				}
				AndroidFileOpener fileOpener = new AndroidFileOpener(context);
				dispatcher = new ExpediaDispatcher(fileOpener);
				mockWebServer.setDispatcher(dispatcher);

				// Persist MockWebServer address to be used for the services classes
				URL mockUrl = mockWebServer.getUrl("");
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
