package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.CarCheckoutParams;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarFilter;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.Transmission;
import com.expedia.bookings.services.CarServices;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CarServicesTest {
	@Rule
	public MockWebServerRule server = new MockWebServerRule();

	private CarServices service;
	private CarCheckoutParams params;
	private CreateTripCarOffer offer;

	@Before
	public void before() {
		RequestInterceptor emptyInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// ignore
			}
		};

		service = new CarServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			emptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	@After
	public void tearDown() {
		service = null;
		params = null;
		offer = null;
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();
		CarSearchParams params = new CarSearchParams();

		service.carSearch(params, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testEmptyMockSearchWorks() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{\"offers\" = []}"));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();
		CarSearchParams params = new CarSearchParams();

		service.carSearch(params, observer);
		observer.awaitTerminalEvent();

		observer.assertValueCount(1);
		observer.assertNoErrors();
		observer.assertCompleted();
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();

		service.carSearch(new CarSearchParams(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		for (CarSearch search : observer.getOnNextEvents()) {
			assertEquals(4, search.categories.size());
		}
	}

	@Test
	public void testMockFilterSearchWorks() throws Throwable {
		//Set Car filter object
		CarFilter carFilter = new CarFilter();
		carFilter.categoriesIncluded = new LinkedHashSet();
		carFilter.suppliersIncluded = new LinkedHashSet();
		carFilter.carTransmissionType = Transmission.MANUAL_TRANSMISSION;
		carFilter.suppliersIncluded.add("NoCCRequired");

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();

		service.carSearch(new CarSearchParams(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		TestSubscriber<CarSearch> filterObserver = new TestSubscriber<>();
		service.carFilterSearch(filterObserver, carFilter);
		filterObserver.awaitTerminalEvent();

		filterObserver.assertNoErrors();
		filterObserver.assertCompleted();
		filterObserver.assertValueCount(1);

		for (CarSearch search : filterObserver.getOnNextEvents()) {
			assertEquals(1, search.categories.size());
			assertEquals("Standard", search.categories.get(0).carCategoryDisplayLabel);

			assertEquals(1, search.categories.get(0).offers.size());
			assertEquals("NoCCRequired", search.categories.get(0).offers.get(0).vendor.name);
		}
	}

	@Test
	public void goodCreateTripResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCreateTripResponse> observer = new TestSubscriber<>();
		String productKey = "happy";
		service.createTrip(productKey, new Money(), false, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
	}

	@Test
	public void createTripResponseInsuranceIncludedMapped() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCreateTripResponse> observer = new TestSubscriber<>();
		String productKey = "happy";
		boolean isInsuranceIncluded = true;
		service.createTrip(productKey, new Money(), isInsuranceIncluded, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		CarCreateTripResponse carCreateTripResponse = observer.getOnNextEvents().get(0);
		assertTrue(carCreateTripResponse.carProduct.isInsuranceIncluded);
	}

	@Test
	public void createTripPriceChange() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCreateTripResponse> observer = new TestSubscriber<>();
		String productKey = "CreateTripPriceChange";
		Money fare = new Money();
		fare.formattedPrice = "$100";
		service.createTrip(productKey, fare, false, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		CarCreateTripResponse carCreateTripResponse = observer.getOnNextEvents().get(0);
		assertTrue(carCreateTripResponse.hasPriceChange());
		assertEquals(fare.formattedPrice, carCreateTripResponse.originalPrice);
	}

	@Test
	public void createTripInvalidErrorCodeResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCreateTripResponse> observer = new TestSubscriber<>();
		String productKey = "failure";
		service.createTrip(productKey, new Money(), false, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
	}

	@Test
	public void createTripCreditCardRequired() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCreateTripResponse> observer = new TestSubscriber<>();
		String productKey = "happy_cc_required";
		service.createTrip(productKey, new Money(), false, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertValueCount(1);
		CarCreateTripResponse carCreateTripResponse = observer.getOnNextEvents().get(0);
		assertTrue(carCreateTripResponse.carProduct.checkoutRequiresCard);
	}

	@Test
	public void createTripInvalidCarProductKey() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCreateTripResponse> observer = new TestSubscriber<>();
		String productKey = "invalid_car_product";
		service.createTrip(productKey, new Money(), false, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.INVALID_CAR_PRODUCT_KEY, apiError.errorCode);
	}

	@Test
	public void goodCheckoutResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("happy");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	@Test
	public void checkoutInvalidInputResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("InvalidInput");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
	}

	@Test
	public void checkoutPriceChangeResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("PriceChange");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertValueCount(1);
		CarCheckoutResponse carCheckoutResponse = observer.getOnNextEvents().get(0);
		assertTrue(carCheckoutResponse.hasPriceChange());
		assertNotNull(carCheckoutResponse.originalCarProduct);

		ApiError apiError = carCheckoutResponse.getFirstError();
		assertEquals(ApiError.Code.PRICE_CHANGE, apiError.errorCode);
	}

	@Test
	public void checkoutPaymentFailedResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("PaymentFailed");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode);
	}

	@Test
	public void checkoutSessionTimeoutResponse() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("SessionTimeout");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.SESSION_TIMEOUT, apiError.errorCode);
	}

	@Test
	public void checkoutTripAlreadyBooked() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("AlreadyBooked");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, apiError.errorCode);
	}

	@Test
	public void checkoutUnknownError() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<CarCheckoutResponse> observer = new TestSubscriber<>();
		givenCreateTripCarOffer();
		givenCheckoutParams("UnknownError");

		service.checkout(offer, params, observer);
		observer.awaitTerminalEvent();

		observer.assertNotCompleted();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.UNKNOWN_ERROR, apiError.errorCode);
	}

	private void givenCreateTripCarOffer() {
		offer = new CreateTripCarOffer();
	}

	private void givenCheckoutParams(String mockFileName) {
		params = new CarCheckoutParams();
		params.firstName = mockFileName;
		params.grandTotal = new Money();
	}

	private void givenServerUsingMockResponses() throws IOException {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));
	}
}
