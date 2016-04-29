package com.expedia.bookings.unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.lx.ActivityAvailabilities;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategorySortOrder;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.RecommendedActivitiesResponse;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.testrule.ServicesRule;

import okhttp3.mockwebserver.MockResponse;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LXServicesTest {
	@Rule

	public ServicesRule<LxServices> serviceRule = new ServicesRule<>(LxServices.class, "../mocked/templates", false);
	private LXCheckoutParams checkoutParams;
	private LXCreateTripParams createTripParams;

	@Before
	public void before() {
		checkoutParams = new LXCheckoutParams();
		checkoutParams.guid = "";
		checkoutParams.suppressFinalBooking = true;
		checkoutParams.storedCreditCardId = "";
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
		checkoutParams.expirationDateMonth = "9";
		checkoutParams.expirationDateYear = "1989";
		checkoutParams.email = "test@gmail.com";
	}

	@After
	public void tearDown() {
		createTripParams = null;
		checkoutParams = null;
	}

	@Test
	public void lxSearchResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "happy";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(1);
		serviceRule.getServices().lxSearch(searchParams, observer);

		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		assertEquals(4, observer.getOnNextEvents().get(0).activities.size());
	}

	@Test
	public void emptySearchResponse() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{\"regionId\":1,\"activities\": []}"));
		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();

		serviceRule.getServices().lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		assertEquals(0, observer.getOnNextEvents().get(0).activities.size());
	}

	@Test
	public void unexpectedSearchResponseThrowsError() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{Unexpected}"));
		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();

		serviceRule.getServices().lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void searchFailure() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{\"regionId\":1, \"searchFailure\": true}"));
		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();

		serviceRule.getServices().lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError)observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.LX_SEARCH_NO_RESULTS, apiError.errorCode);
	}

	// Details.

	@Test
	public void detailsResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<ActivityDetailsResponse> observer = new TestSubscriber<>();
		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		serviceRule.getServices().lxDetails(lxActivity.id, null, LocalDate.now(), LocalDate.now().plusDays(1), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		observer.assertCompleted();

		ActivityDetailsResponse activityDetailsResponse = observer.getOnNextEvents().get(0);
		assertEquals(5, activityDetailsResponse.images.size());
		assertEquals(5, activityDetailsResponse.highlights.size());
	}

	@Test
	public void recommendedResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<RecommendedActivitiesResponse> observer = new TestSubscriber<>();
		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "happy_recommend";
		serviceRule.getServices().lxRecommendedSearch(lxActivity.id, null, LocalDate.now(), LocalDate.now().plusDays(1),
			observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		observer.assertCompleted();

		RecommendedActivitiesResponse activityDetailsResponse = observer.getOnNextEvents().get(0);

		assertEquals(3, activityDetailsResponse.getActivities().size());
		assertEquals(new BigDecimal("135.00"), activityDetailsResponse.getActivities().get(0).price.amount);
		assertEquals("USD", activityDetailsResponse.getActivities().get(0).price.currencyCode);
		assertEquals("USD", activityDetailsResponse.getCurrencyCode());
	}

	@Test
	public void emptyDetailsResponse() throws Throwable {
		serviceRule.getServer().enqueue(
			new MockResponse().setBody("{\"id\": \"183615\", \"offersDetail\": { \"offers\": [] }}"));
		TestSubscriber<ActivityDetailsResponse> observer = new TestSubscriber<>();

		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		serviceRule.getServices().lxDetails(lxActivity.id, null, LocalDate.now(), LocalDate.now().plusDays(1), observer);
		observer.awaitTerminalEvent();

		observer.assertCompleted();
		observer.assertValueCount(1);
		assertNull(observer.getOnNextEvents().get(0).images);
	}

	@Test
	public void createTripResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("happy");
		TestSubscriber<LXCreateTripResponse> observer = new TestSubscriber<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		assertNotNull(observer.getOnNextEvents().get(0).tripId);
	}

	@Test
	public void createTripGenericErrorResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("error_create_trip");
		TestSubscriber<LXCreateTripResponse> observer = new TestSubscriber<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError)observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.UNKNOWN_ERROR, apiError.errorCode);
	}

	@Test
	public void createTripInvalidInputErrorResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("error_invalid_input_create_trip");
		TestSubscriber<LXCreateTripResponse> observer = new TestSubscriber<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError)observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
	}

	@Test
	public void createTripPriceChangeResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("price_change");
		TestSubscriber<LXCreateTripResponse> observer = new TestSubscriber<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		LXCreateTripResponse lxCreateTripResponse = observer.getOnNextEvents().get(0);
		assertTrue(lxCreateTripResponse.hasPriceChange());
		assertNotNull(lxCreateTripResponse.newTotalPrice);
	}

	@Test
	public void unexpectedDetailsResponseThrowsError() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{Unexpected}"));
		TestSubscriber<ActivityDetailsResponse> observer = new TestSubscriber<>();

		serviceRule.getServices().lxDetails(new LXActivity().id, null, null, null, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void lxCheckoutResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
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
	public void unexpectedCheckoutResponseThrowsError() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{Unexpected}"));
		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();

		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void checkoutWithInvalidInput() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();
		// Invalid credit card number
		checkoutParams.tripId = "invalid_credit_card_number_trip_id";
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	@Test
	public void checkoutPaymentFailure() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();
		checkoutParams.tripId = "payment_failed_trip_id";
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);

		ApiError apiError = (ApiError) observer.getOnErrorEvents().get(0);
		assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	@Test
	public void checkoutPriceChange() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<LXCheckoutResponse> observer = new TestSubscriber<>();
		checkoutParams.tripId = "price_change";
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		LXCheckoutResponse lxCheckoutResponse = observer.getOnNextEvents().get(0);
		assertTrue(lxCheckoutResponse.hasPriceChange());
		assertNotNull(lxCheckoutResponse.newTotalPrice);
	}

	@Test
	public void lxCategorySearchResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestSubscriber<LXSearchResponse> observer = new TestSubscriber<>();
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "happy";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(1);
		serviceRule.getServices().lxCategorySearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		assertEquals(4, observer.getOnNextEvents().get(0).activities.size());
		assertNotNull(observer.getOnNextEvents().get(0).filterCategories.get("Attractions").activities);
		assertEquals(4, observer.getOnNextEvents().get(0).filterCategories.get("Attractions").activities.size());
		assertEquals("Attractions",
			observer.getOnNextEvents().get(0).filterCategories.get("Attractions").categoryKeyEN);
		assertEquals(LXCategorySortOrder.Unknown,
			observer.getOnNextEvents().get(0).filterCategories.get("Unknown Category").sortOrder);
		assertEquals(LXCategorySortOrder.Attractions,
			observer.getOnNextEvents().get(0).filterCategories.get("Attractions").sortOrder);
		assertEquals(LXCategorySortOrder.PrivateTransfers,
			observer.getOnNextEvents().get(0).filterCategories.get("Private Transfers").sortOrder);
	}

	private void givenCreateTripParamsHasOneOffer(String activityId) {
		createTripParams = new LXCreateTripParams();
		Offer offer = new Offer();
		offer.availabilityInfoOfSelectedDate = new AvailabilityInfo();
		offer.availabilityInfoOfSelectedDate.availabilities = new ActivityAvailabilities();
		offer.availabilityInfoOfSelectedDate.availabilities.valueDate = "2020-06-01 00:00:00";
		LXOfferSelected selectedOffer = new LXOfferSelected(activityId, offer, Collections.<Ticket>emptyList(), "");
		List<LXOfferSelected> offersSelected = Arrays.asList(selectedOffer);
		createTripParams.offersSelected(offersSelected);
	}
}
