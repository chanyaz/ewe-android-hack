package com.expedia.bookings.utils;

import java.io.IOException;
import java.net.InetAddress;

import javax.net.ssl.SSLContext;

import android.content.Context;

import com.mobiata.mocke3.ExpediaDispatcher;

import okhttp3.HttpUrl;
import okhttp3.internal.SslContextBuilder;
import okhttp3.mockwebserver.MockWebServer;

public final class ExpediaMockWebServer {

	private MockWebServer server = null;
	private ExpediaDispatcher dispatcher = null;

	private final SSLContext sslContext = SslContextBuilder.localhost();

	public ExpediaMockWebServer(Context context) {
		server = new MockWebServer();
		server.useHttps(sslContext.getSocketFactory(), false);

		AndroidFileOpener opener = new AndroidFileOpener(context);
		dispatcher = new ExpediaDispatcher(opener);
		server.setDispatcher(dispatcher);
	}

	public void start() {
		try {
			InetAddress address = InetAddress.getByName("localhost");
			server.start(address, 0);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to init MockWebServer, wut?", e);
		}
	}

	public void shutdown() {
		try {
			server.shutdown();
		}
		catch (Throwable e) {
			throw new RuntimeException("Failed to shutdown MockWebServer, wut?", e);
		}
	}

	public ExpediaDispatcher getDispatcher() {
		return dispatcher;
	}

	public String getHostWithPort() {
		HttpUrl mockUrl = server.url("");
		return mockUrl.host() + ":" + mockUrl.port();
	}
}
