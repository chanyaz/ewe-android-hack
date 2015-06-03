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
import rx.Subscription;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	@Test(expected = RetrofitError.class)
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		BlockingObserver<List<Hotel>> observer = new BlockingObserver<>(1);
		NearbyHotelParams params = new NearbyHotelParams("", "", "", "", "", "", "");

		Subscription sub = service.nearbyHotels(params, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(0, observer.getItems().size());
		assertEquals(1, observer.getErrors().size());
		for (Throwable error : observer.getErrors()) {
			throw error;
		}
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<List<Hotel>> observer = new BlockingObserver<>(1);
		NearbyHotelParams params = new NearbyHotelParams("", "", "", "", "", "", "");

		Subscription sub = service.nearbyHotels(params, observer);
		observer.await();
		sub.unsubscribe();

		for (Throwable throwable : observer.getErrors()) {
			throw throwable;
		}
		assertTrue(observer.completed());
		assertEquals(1, observer.getItems().size());
	}
}
