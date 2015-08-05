package com.expedia.bookings.unit;

import java.io.File;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.services.LXServices;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LXServicesTest {
	@Rule
	public MockWebServerRule server = new MockWebServerRule();

	private LXServices service;
	private LXCheckoutParams checkoutParams;

	@Before
	public void before() {
		RequestInterceptor emptyInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// ignore
			}
		};

		service = new LXServices("http://localhost:" + server.getPort(), new OkHttpClient(), emptyInterceptor, Schedulers.immediate(),
			Schedulers.immediate(), RestAdapter.LogLevel.FULL);

		checkoutParams = new LXCheckoutParams();
		checkoutParams.firstName = "FirstName";
		checkoutParams.lastName = "LastName";
		checkoutParams.phone = "415-111-111";
		checkoutParams.phoneCountryCode = "1";
		checkoutParams.tripId = "happypath_trip_id";
		checkoutParams.postalCode = "94123";
		checkoutParams.expectedFareCurrencyCode = "USD";
		checkoutParams.expectedTotalFare = "139.40";
		checkoutParams.nameOnCard = "Test";
		checkoutParams.creditCardNumber = "4111111111111111";
		checkoutParams.expirationDateYear = "2020";
		checkoutParams.cvv = "111";
		checkoutParams.email = "test@gmail.com";
	}

	@Test
	public void testLXSearchResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "happy";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(1);
		service.lxSearch(searchParams, observer);

		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		assertEquals(3, observer.getOnNextEvents().get(0).activities.size());

	}

	@Test
	public void testEmptySearchResponse() throws Throwable {
		server.enqueue(new MockResponse().setBody("{regionId:1,\"activities\": []}"));
		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();

		service.lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		assertEquals(0, observer.getOnNextEvents().get(0).activities.size());
	}

	@Test
	public void testUnexpectedSearchResponseThrowsError() throws Throwable {
		server.enqueue(new MockResponse().setBody("{Unexpected}"));
		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();

		service.lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testSearchFailure() throws Throwable {
		server.enqueue(new MockResponse().setBody("{regionId:1, searchFailure: true}"));
		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();

		service.lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RuntimeException.class);
	}

	// Details.

	@Test
	public void testDetailsResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<ActivityDetailsResponse> observer = new TestSubscriber<>();
		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		service.lxDetails(lxActivity, null, LocalDate.now(), LocalDate.now().plusDays(1), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		observer.assertCompleted();

		ActivityDetailsResponse activityDetailsResponse = observer.getOnNextEvents().get(0);
		assertEquals(5, activityDetailsResponse.images.size());
		assertEquals(5, activityDetailsResponse.highlights.size());
	}

	@Test
	public void testEmptyDetailsResponse() throws Throwable {
		server.enqueue(new MockResponse().setBody("{\"id\": \"183615\", \"offersDetail\": { \"offers\": [] }}"));
		TestSubscriber<ActivityDetailsResponse> observer = new TestSubscriber<>();

		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		service.lxDetails(lxActivity, null, LocalDate.now(), LocalDate.now().plusDays(1), observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertValueCount(1);
		assertNull(observer.getOnNextEvents().get(0).images);
	}

	@Test
	public void testUnexpectedDetailsResponseThrowsError() throws Throwable {
		server.enqueue(new MockResponse().setBody("{Unexpected}"));
		TestSubscriber<ActivityDetailsResponse> observer = new TestSubscriber<>();

		service.lxDetails(new LXActivity(), null, null, null, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testLXCheckoutResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();

		service.lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		LXCheckoutResponse checkoutResponse = observer.getOnNextEvents().get(0);
		assertNotNull(checkoutResponse.newTrip);
		assertNotNull(checkoutResponse.activityId);
		assertEquals(checkoutParams.tripId, observer.getOnNextEvents().get(0).newTrip.tripId);
	}

	@Test
	public void testUnexpectedCheckoutResponseThrowsError() throws Throwable {
		server.enqueue(new MockResponse().setBody("{Unexpected}"));
		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();

		service.lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testCheckoutWithInvalidInput() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();

		// Invalid credit card number
		checkoutParams.tripId = "invalid_credit_card_number_trip_id";
		service.lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);

		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	@Test
	public void testCheckoutPaymentFailure() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();

		checkoutParams.tripId = "payment_failed_trip_id";
		service.lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);

		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}
}
