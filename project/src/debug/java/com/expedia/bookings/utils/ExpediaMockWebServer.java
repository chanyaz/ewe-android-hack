package com.expedia.bookings.utils;

import android.content.Context;
import com.mobiata.mocke3.ExpediaDispatcher;
import java.io.IOException;
import java.net.InetAddress;
import okhttp3.HttpUrl;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockWebServer;

public final class ExpediaMockWebServer {

	private final MockWebServer mockWebServer;
	private ExpediaDispatcher dispatcher = null;


	public ExpediaMockWebServer(Context context) {
		mockWebServer = new MockWebServer();
		SslClient sslClient = SslClient.localhost();
		mockWebServer.useHttps(sslClient.socketFactory,false);
		AndroidFileOpener opener = new AndroidFileOpener(context);
		dispatcher = new ExpediaDispatcher(opener);
		mockWebServer.setDispatcher(dispatcher);

	}

	public void start() {
		try {
			InetAddress address = InetAddress.getByName("localhost");
			mockWebServer.start(address, 0);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to init MockWebServer, wut?", e);
		}
	}

	public void shutdown() {
		try {
			mockWebServer.shutdown();
		}
		catch (Throwable e) {
			throw new RuntimeException("Failed to shutdown MockWebServer, wut?", e);
		}
	}

	public ExpediaDispatcher getDispatcher() {
		return dispatcher;
	}

	public String getHostWithPort() {
		HttpUrl mockUrl = mockWebServer.url("");
		return mockUrl.host() + ":" + mockUrl.port();
	}

}
