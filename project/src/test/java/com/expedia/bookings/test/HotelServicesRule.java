package com.expedia.bookings.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.expedia.bookings.services.HotelServices;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.schedulers.Schedulers;

public class HotelServicesRule implements TestRule {
	private MockWebServer server;
	private HotelServices service;

	public HotelServices hotelServices() {
		return service;
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		server = new MockWebServer();
		server.setDispatcher(ReviewsServicesRule.diskExpediaDispatcher());

		Statement createService = new Statement() {
			@Override
			public void evaluate() throws Throwable {
				service = generateService();
				base.evaluate();
				service = null;
			}
		};

		return server.apply(createService, description);
	}

	private HotelServices generateService() {
		RequestInterceptor emptyInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// ignore
			}
		};

		return new HotelServices("http://localhost:" + server.getPort(), new OkHttpClient(), emptyInterceptor,
			Schedulers
				.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}
}
