package com.expedia.bookings.unit;

import java.io.File;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.ApiException;
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
import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LXServicesTest {
	@Rule
	public MockWebServerRule mockServer = new MockWebServerRule();

	// Search.

	@Test
	public void testLXSearchResponse() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<LXSearchResponse> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "happy";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(1);
		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();

		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		assertEquals(114, blockingObserver.getItems().get(0).activities.size());

	}

	@Test
	public void testEmptySearchResponse() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{regionId:1,\"activities\": []}"));
		BlockingObserver<LXSearchResponse> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();

		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		assertEquals(0, blockingObserver.getItems().get(0).activities.size());
	}

	@Test(expected = RetrofitError.class)
	public void testUnexpectedSearchResponseThrowsError() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{Unexpected}"));
		BlockingObserver<LXSearchResponse> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();

		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());
		throw blockingObserver.getErrors().get(0);
	}

	@Test(expected = RuntimeException.class)
	public void testSearchFailure() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{regionId:1, searchFailure: true}"));
		BlockingObserver<LXSearchResponse> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();

		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();

		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());
		throw blockingObserver.getErrors().get(0);
	}

	// Details.

	@Test
	public void testDetailsResponse() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<ActivityDetailsResponse> blockingObserver = new BlockingObserver<>(1);
		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		Subscription subscription = getLXServices().lxDetails(lxActivity, LocalDate.now(), LocalDate.now().plusDays(1), blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		// Check the details object for required values.
		ActivityDetailsResponse activityDetailsResponse = blockingObserver.getItems().get(0);
		assertEquals(5, activityDetailsResponse.images.size());
		assertEquals(5, activityDetailsResponse.highlights.size());
	}

	@Test
	public void testEmptyDetailsResponse() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{\"id\": \"183615\", \"offersDetail\": { \"offers\": [] }}"));
		BlockingObserver<ActivityDetailsResponse> blockingObserver = new BlockingObserver<>(1);

		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		Subscription subscription = getLXServices().lxDetails(lxActivity, LocalDate.now(), LocalDate.now().plusDays(1), blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		assertNull(blockingObserver.getItems().get(0).images);
	}

	@Test(expected = RetrofitError.class)
	public void testUnexpectedDetailsResponseThrowsError() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{Unexpected}"));
		BlockingObserver<ActivityDetailsResponse> blockingObserver = new BlockingObserver<>(1);

		Subscription subscription = getLXServices().lxDetails(new LXActivity(), null, null, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());
		throw blockingObserver.getErrors().get(0);
	}

	// Checkout
	@Test
	public void testLXCheckoutResponse() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<LXCheckoutResponse> blockingObserver = new BlockingObserver<>(1);

		LXCheckoutParams checkoutParams = checkoutParams();
		Subscription subscription = getLXServices().lxCheckout(checkoutParams, blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();

		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());

		LXCheckoutResponse checkoutResponse = blockingObserver.getItems().get(0);
		assertNotNull(checkoutResponse.newTrip);
		assertNotNull(checkoutResponse.activityId);
		assertEquals(checkoutParams.tripId, blockingObserver.getItems().get(0).newTrip.tripId);
	}

	@Test(expected = RetrofitError.class)
	public void testUnexpectedCheckoutResponseThrowsError() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{Unexpected}"));
		BlockingObserver<LXCheckoutResponse> blockingObserver = new BlockingObserver<>(1);


		Subscription subscription = getLXServices().lxCheckout(checkoutParams(), blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());
		throw blockingObserver.getErrors().get(0);
	}

	@Test
	public void testCheckoutWithInvalidInput() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<LXCheckoutResponse> blockingObserver = new BlockingObserver<>(1);

		LXCheckoutParams checkoutParams = checkoutParams();
		// Invalid credit card number
		checkoutParams.tripId = "invalid_credit_card_number_trip_id";
		Subscription subscription = getLXServices().lxCheckout(checkoutParams, blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();

		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());

		ApiException apiException = (ApiException) blockingObserver.getErrors().get(0);
		ApiError apiError = apiException.apiError;

		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	@Test
	public void testCheckoutPaymentFailure() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<LXCheckoutResponse> blockingObserver = new BlockingObserver<>(1);

		LXCheckoutParams checkoutParams = checkoutParams();
		checkoutParams.tripId = "payment_failed_trip_id";
		Subscription subscription = getLXServices().lxCheckout(checkoutParams, blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();

		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());

		ApiException apiException = (ApiException) blockingObserver.getErrors().get(0);
		ApiError apiError = apiException.apiError;
		assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	private LXCheckoutParams checkoutParams() {
		LXCheckoutParams params = new LXCheckoutParams();
		params.firstName = "FirstName";
		params.lastName = "LastName";
		params.phone = "415-111-111";
		params.phoneCountryCode = "1";
		params.tripId = "happypath_trip_id";
		params.postalCode = "94123";
		params.expectedFareCurrencyCode = "USD";
		params.expectedTotalFare = "139.40";
		params.nameOnCard = "Test";
		params.creditCardNumber = "4111111111111111";
		params.expirationDateYear = "2020";
		params.cvv = "111";
		params.email = "test@gmail.com";
		return params;
	}

	private LXServices getLXServices() {
		return new LXServices("http://localhost:" + mockServer.getPort(), new OkHttpClient(), sEmptyInterceptor, Schedulers.immediate(),
			Schedulers.immediate());
	}

	private static final RequestInterceptor sEmptyInterceptor = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {
			// ignore
		}
	};
}
