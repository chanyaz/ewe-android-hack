package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelApplyCouponParams;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
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

import static junit.framework.Assert.assertEquals;

public class HotelServicesTest {
	@Rule
	public MockWebServerRule server = new MockWebServerRule();

	private HotelServices service;
	private HotelCreateTripResponse createTripResponse;
	private HotelApplyCouponParams couponParams;

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

	@Test
	public void testCouponError() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCouponParams("hotel_coupon_errors_expired");

		service.applyCoupon(couponParams).subscribe(observer);
		observer.awaitTerminalEvent();
		observer.assertCompleted();
		ApiError apiError = observer.getOnNextEvents().get(0).getFirstError();
		assertEquals(ApiError.Code.APPLY_COUPON_ERROR, apiError.errorCode);
	}

	private void givenServerUsingMockResponses() throws IOException {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));
	}

	private void givenCreateTripCarOffer() {
		createTripResponse = new HotelCreateTripResponse();
	}

	private void givenCouponParams(String mockFileName) {
		couponParams = new HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", mockFileName);
	}
}
