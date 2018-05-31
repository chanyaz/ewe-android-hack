package com.expedia.bookings.utils;

import java.util.concurrent.CountDownLatch;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.ExpediaDispatcher;

public class MockModeShim {

	private static ExpediaMockWebServer server = null;

	public static void initMockWebServer(Context c) {
		final Context context = c.getApplicationContext();
		final CountDownLatch latch = new CountDownLatch(1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (server == null) {
					server = new ExpediaMockWebServer(context);
					server.start();
				}
				SettingUtils.save(context, R.string.preference_proxy_server_address, server.getHostWithPort());

				latch.countDown();
			}
		}).start();

		try {
			latch.await();
		}
		catch (Throwable e) {
			throw new RuntimeException("Problem waiting for mock web server to start", e);
		}
	}

	public static ExpediaDispatcher getDispatcher() {
		return server.getDispatcher();
	}
}
