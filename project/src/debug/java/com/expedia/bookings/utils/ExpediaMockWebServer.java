package com.expedia.bookings.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import android.content.Context;

import com.mobiata.mocke3.ExpediaDispatcher;
import com.squareup.okhttp.mockwebserver.MockWebServer;

/**
 * Created by doug on 9/24/15.
 */
public class ExpediaMockWebServer {
	public static final String DEFAULT_HOSTNAME = "0.0.0.0";
	public static final int DEFAULT_PORT = 0;

	private MockWebServer server = null;
	private ExpediaDispatcher dispatcher = null;

	public ExpediaMockWebServer(Context context) {
		server = new MockWebServer();

		AndroidFileOpener opener = new AndroidFileOpener(context);
		dispatcher = new ExpediaDispatcher(opener);
		server.setDispatcher(dispatcher);

		try {
			InetAddress address = InetAddress.getByName(DEFAULT_HOSTNAME);
			server.start(address, DEFAULT_PORT);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to init MockWebServer, wut?", e);
		}
	}

	public void shutdown() {
		if (server == null) {
			return;
		}
		try {
			server.shutdown();
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to shutdown MockWebServer, wut?", e);
		}
	}

	public ExpediaDispatcher getDispatcher() {
		return dispatcher;
	}

	public String getHostWithPort() {
		URL mockUrl = server.getUrl("");
		return mockUrl.getHost() + ":" + mockUrl.getPort();
	}
}
