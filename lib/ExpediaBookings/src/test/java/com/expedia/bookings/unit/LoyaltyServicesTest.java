package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.payment.CalculatePointsParams;
import com.expedia.bookings.data.payment.CalculatePointsResponse;
import com.expedia.bookings.data.payment.ProgramName;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.LoyaltyServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class LoyaltyServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private LoyaltyServices service;

	@Before
	public void before() {
		service = new LoyaltyServices("http://localhost:" + server.getPort(),
			new OkHttpClient(), new MockInterceptor(),
			Schedulers.immediate(), Schedulers.immediate(),
			RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testCalculatePoints() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("happy", observer);

		Assert.assertNotNull(observer.getOnNextEvents().get(0).getConversion());
		Assert.assertNotNull(observer.getOnNextEvents().get(0).getRemainingPayableByCard());
		Assert.assertNotNull(observer.getOnNextEvents().get(0).getProgramName());
	}

	@Test
	public void testCalculatePointsThrowsInvalidInput() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("invalid_amount_entered", observer);
		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		Assert.assertEquals(ApiError.Code.INVALID_INPUT, getCalculatePointsError(observer));
	}

	@Test
	public void testCalculatePointsThrowsTripServiceError() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("trip_service_error", observer);
		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		Assert.assertEquals(ApiError.Code.TRIP_SERVICE_ERROR, getCalculatePointsError(observer));
	}

	@Test
	public void testCalculatePointsThrowsPointsConversionUnauthenticatedError() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("points_conversion_unauthenticated", observer);
		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		Assert.assertEquals(ApiError.Code.POINTS_CONVERSION_UNAUTHENTICATED_ACCESS,
			getCalculatePointsError(observer));
	}

	private ApiError.Code getCalculatePointsError(TestSubscriber<CalculatePointsResponse> observer) {
		return observer.getOnNextEvents().get(0).getFirstError().errorCode;
	}

	private void setupCalculatePoints(String tripId, TestSubscriber<CalculatePointsResponse> observer) throws IOException {
		givenServerUsingMockResponses();
		CalculatePointsParams calculatePointsParams = new CalculatePointsParams.Builder().
			tripId(tripId).programName(ProgramName.ExpediaRewards).amount("100").rateId("rateId").build();

		service.currencyToPoints(calculatePointsParams, observer);

		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	private void givenServerUsingMockResponses() throws IOException {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
	}
}
