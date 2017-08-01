package com.expedia.bookings.unit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.lx.ActivityAvailabilities;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripParams;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXTheme;
import com.expedia.bookings.data.lx.LXThemeType;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.testrule.ServicesRule;

import okhttp3.mockwebserver.MockResponse;
import com.expedia.bookings.services.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LXServicesTest {
	@Rule
	public ServicesRule<LxServices> serviceRule = new ServicesRule<>(LxServices.class, Schedulers.trampoline(), "../mocked/templates", false);
	private LXCheckoutParams checkoutParams;
	private LXCreateTripParams createTripParams;

	@Before
	public void before() {
		checkoutParams = new LXCheckoutParams();
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

		TestObserver<LXSearchResponse> observer = new TestObserver<>();
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("happy")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1)).build();
		serviceRule.getServices().lxSearch(searchParams, observer);

		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		assertEquals(4, observer.values().get(0).activities.size());
	}

	@Test
	public void emptySearchResponse() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{\"regionId\":1,\"activities\": []}"));
		TestObserver<LXSearchResponse> observer = new TestObserver<>();
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("happy")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1)).build();

		serviceRule.getServices().lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertComplete();
		observer.assertNoErrors();
		observer.assertValueCount(1);
		assertEquals(0, observer.values().get(0).activities.size());
	}

	@Test
	public void unexpectedSearchResponseThrowsError() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{Unexpected}"));
		TestObserver<LXSearchResponse> observer = new TestObserver<>();
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("happy")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1)).build();

		serviceRule.getServices().lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void searchFailure() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{\"regionId\":1, \"searchFailure\": true}"));
		TestObserver<LXSearchResponse> observer = new TestObserver<>();
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("happy")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1)).build();

		serviceRule.getServices().lxSearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError)observer.errors().get(0);
		assertEquals(ApiError.Code.LX_SEARCH_NO_RESULTS, apiError.errorCode);
	}

	// Details.

	@Test
	public void detailsResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestObserver<ActivityDetailsResponse> observer = new TestObserver<>();
		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		serviceRule.getServices().lxDetails(lxActivity.id, null, LocalDate.now(), LocalDate.now().plusDays(1), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		observer.assertComplete();

		ActivityDetailsResponse activityDetailsResponse = observer.values().get(0);
		assertEquals(5, activityDetailsResponse.images.size());
		assertEquals(5, activityDetailsResponse.highlights.size());
	}

	@Test
	public void emptyDetailsResponse() throws Throwable {
		serviceRule.getServer().enqueue(
			new MockResponse().setBody("{\"id\": \"183615\", \"offersDetail\": { \"offers\": [] }}"));
		TestObserver<ActivityDetailsResponse> observer = new TestObserver<>();

		LXActivity lxActivity = new LXActivity();
		lxActivity.id = "183615";
		serviceRule.getServices().lxDetails(lxActivity.id, null, LocalDate.now(), LocalDate.now().plusDays(1), observer);
		observer.awaitTerminalEvent();

		observer.assertComplete();
		observer.assertValueCount(1);
		assertNull(observer.values().get(0).images);
	}

	@Test
	public void createTripResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("happy");
		TestObserver<LXCreateTripResponse> observer = new TestObserver<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		assertNotNull(observer.values().get(0).tripId);
	}

	@Test
	public void createTripGenericErrorResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("error_create_trip");
		TestObserver<LXCreateTripResponse> observer = new TestObserver<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError)observer.errors().get(0);
		assertEquals(ApiError.Code.UNKNOWN_ERROR, apiError.errorCode);
	}

	@Test
	public void createTripInvalidInputErrorResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("error_invalid_input_create_trip");
		TestObserver<LXCreateTripResponse> observer = new TestObserver<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError)observer.errors().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
	}

	@Test
	public void createTripPriceChangeResponse() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();
		givenCreateTripParamsHasOneOffer("price_change");
		TestObserver<LXCreateTripResponse> observer = new TestObserver<>();

		serviceRule.getServices().createTrip(createTripParams, new Money(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		LXCreateTripResponse lxCreateTripResponse = observer.values().get(0);
		assertTrue(lxCreateTripResponse.hasPriceChange());
		assertNotNull(lxCreateTripResponse.newTotalPrice);
	}

	@Test
	public void unexpectedDetailsResponseThrowsError() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{Unexpected}"));
		TestObserver<ActivityDetailsResponse> observer = new TestObserver<>();

		serviceRule.getServices().lxDetails(new LXActivity().id, null, null, null, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void lxCheckoutResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestObserver<LXCheckoutResponse> observer = new TestObserver<>();
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		LXCheckoutResponse checkoutResponse = observer.values().get(0);
		assertNotNull(checkoutResponse.newTrip);
		assertNotNull(checkoutResponse.activityId);
		assertEquals(checkoutParams.tripId, observer.values().get(0).newTrip.tripId);
	}

	@Test
	public void unexpectedCheckoutResponseThrowsError() throws Throwable {
		serviceRule.getServer().enqueue(new MockResponse().setBody("{Unexpected}"));
		TestObserver<LXCheckoutResponse> observer = new TestObserver<>();

		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void checkoutWithInvalidInput() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestObserver<LXCheckoutResponse> observer = new TestObserver<>();
		// Invalid credit card number
		checkoutParams.tripId = "invalid_credit_card_number_trip_id";
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);
		ApiError apiError = (ApiError) observer.errors().get(0);
		assertEquals(ApiError.Code.INVALID_INPUT, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	@Test
	public void checkoutPaymentFailure() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestObserver<LXCheckoutResponse> observer = new TestObserver<>();
		checkoutParams.tripId = "payment_failed_trip_id";
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(ApiError.class);

		ApiError apiError = (ApiError) observer.errors().get(0);
		assertEquals(ApiError.Code.PAYMENT_FAILED, apiError.errorCode);
		assertNotNull(apiError.errorInfo.field);
		assertNotNull(apiError.errorInfo.summary);
	}

	@Test
	public void checkoutPriceChange() throws IOException {
		serviceRule.setDefaultExpediaDispatcher();

		TestObserver<LXCheckoutResponse> observer = new TestObserver<>();
		checkoutParams.tripId = "price_change";
		serviceRule.getServices().lxCheckout(checkoutParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertValueCount(1);
		LXCheckoutResponse lxCheckoutResponse = observer.values().get(0);
		assertTrue(lxCheckoutResponse.hasPriceChange());
		assertNotNull(lxCheckoutResponse.newTotalPrice);
	}

	@Test
	public void lxCategorySearchResponse() throws Throwable {
		serviceRule.setDefaultExpediaDispatcher();

		TestObserver<LXSearchResponse> observer = new TestObserver<>();
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("happy")
			.startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1)).build();
		serviceRule.getServices().lxCategorySearch(searchParams, observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		assertEquals(4, observer.values().get(0).activities.size());
		assertNotNull(observer.values().get(0).lxThemes);
		assertEquals(7, observer.values().get(0).lxThemes.size());
		LXTheme topRatedTheme = observer.values().get(0).lxThemes.get(0);
		assertEquals(LXThemeType.TopRatedActivities, topRatedTheme.themeType);
		assertEquals(0, topRatedTheme.filterCategories.size());
		assertEquals(4, topRatedTheme.activities.size());
		LXTheme allThingsToDoTheme = observer.values().get(0).lxThemes.get(6);
		assertEquals(LXThemeType.AllThingsToDo, allThingsToDoTheme.themeType);
		assertEquals(7, allThingsToDoTheme.filterCategories.size());
		assertEquals(4, allThingsToDoTheme.activities.size());
		assertEquals(2, observer.values().get(0).lxThemes.get(1).filterCategories.size());
		assertEquals(1, observer.values().get(0).lxThemes.get(2).filterCategories.size());
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
