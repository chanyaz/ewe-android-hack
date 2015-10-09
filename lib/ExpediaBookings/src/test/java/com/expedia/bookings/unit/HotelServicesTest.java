package com.expedia.bookings.unit;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.services.HotelServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class HotelServicesTest {
	@Rule
	public MockWebServerRule server = new MockWebServerRule();

	private HotelServices service;

	@Before
	public void before() {
		RequestInterceptor emptyInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// ignore
			}
		};

		service = new HotelServices("http://localhost:" + server.getPort(),
			new OkHttpClient(), emptyInterceptor,
			Schedulers.immediate(), Schedulers.immediate(),
			RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<List<Hotel>> observer = new TestSubscriber<>();
		NearbyHotelParams params = new NearbyHotelParams("", "", "", "", "", "", "");

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<Hotel>> observer = new TestSubscriber<>();
		NearbyHotelParams params = new NearbyHotelParams("", "", "", "", "", "", "");

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}
}
