package com.expedia.bookings.utils;

import java.net.URL;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class MockModeShim {

	public static void initMockWebServer(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				MockWebServer mMockWebServer = new MockWebServer();
				try {
					mMockWebServer.start();
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to init MockWebServer, wut?", e);
				}
				AndroidFileOpener mFileOpener = new AndroidFileOpener(context);
				ExpediaDispatcher dispatcher = new ExpediaDispatcher(mFileOpener);
				mMockWebServer.setDispatcher(dispatcher);

				// Persist MockWebServer address to be used for the services classes
				URL mockUrl = mMockWebServer.getUrl("");
				String server = mockUrl.getHost() + ":" + mockUrl.getPort();
				SettingUtils.save(context, R.string.preference_proxy_server_address, server);
				SettingUtils.save(context, R.string.preference_force_custom_server_http_only, true);
			}
		}).start();
	}

}
