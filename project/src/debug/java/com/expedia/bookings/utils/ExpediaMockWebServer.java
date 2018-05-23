package com.expedia.bookings.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import android.content.Context;

import com.expedia.bookings.preference.TripMockScenarios;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.mocke3.Dispatchers;
import com.mobiata.mocke3.ExpediaDispatcher;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.internal.tls.SslClient;

public final class ExpediaMockWebServer {

	private MockWebServer mockWebServer;
	private ExpediaDispatcher dispatcher = null;


	public ExpediaMockWebServer(Context context) {
		mockWebServer = new MockWebServer();
		SslClient sslClient = SslClient.localhost();
		mockWebServer.useHttps(sslClient.socketFactory, false);
		AndroidFileOpener opener = new AndroidFileOpener(context);
		HashMap<Dispatchers, String> dispatcherSettings = new HashMap<>();
		addDispatcherSettings(context, dispatcherSettings);
		dispatcher = new ExpediaDispatcher(opener, dispatcherSettings);
		mockWebServer.setDispatcher(dispatcher);
	}

	private void addDispatcherSettings(Context context, HashMap<Dispatchers, String> settings) {

		//Trips Dispatcher
		String tripsResponseToDispatch = SettingUtils.get(context, TripMockScenarios.TRIP_SCENARIOS_FILENAME_KEY,
			TripMockScenarios.Scenarios.TRIP_FOLDERS_M1_ONLY_HOTEL.getFilename());
		settings.put(Dispatchers.TRIPS_DISPATCHER, tripsResponseToDispatch);
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
