package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.CardFeeResponse;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.rail.RailPassenger;
import com.expedia.bookings.data.rail.requests.RailCheckoutParams;
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel;
import com.expedia.bookings.data.rail.responses.RailCard;
import com.expedia.bookings.data.rail.responses.RailCardsResponse;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.expedia.bookings.data.rail.responses.RailOffer;
import com.expedia.bookings.data.rail.responses.RailProduct;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;
import com.expedia.bookings.data.rail.responses.RailsApiStatusCodes;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.RailServices;
import com.expedia.bookings.utils.Constants;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockWebServer;
import com.expedia.bookings.unit.rail.RailCheckoutParamsMock;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RailServicesTest {

	@Rule
	public MockWebServer server = new MockWebServer();
	private RailServices service;
	private RailApiSearchModel railSearchRequest;
	private TestSubscriber<RailSearchResponse> searchResponseObserver;
	private TestSubscriber<RailCreateTripResponse> createTripResponseObserver;
	private TestSubscriber<RailCheckoutResponse> checkoutTripResponseObserver;
	private TestSubscriber<CardFeeResponse> cardFeeResponseObserver;

	@Before
	public void before() throws IOException {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		HashMap<String, String> urlMap = new HashMap<>();
		urlMap.put(Constants.MOCK_MODE, "http://localhost:" + server.getPort());
		service = new RailServices(urlMap, new OkHttpClient.Builder().addInterceptor(logger).build(),
			interceptor, Schedulers.immediate(), Schedulers.immediate());

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		searchResponseObserver = new TestSubscriber();
		createTripResponseObserver = new TestSubscriber();
		checkoutTripResponseObserver = new TestSubscriber();
		cardFeeResponseObserver = new TestSubscriber();
	}

	@Test
	public void happyMockSearch() {
		givenSearchRequest("happy");

		service.railSearch(railSearchRequest, searchResponseObserver);
		searchResponseObserver.awaitTerminalEvent();

		searchResponseObserver.assertCompleted();
		searchResponseObserver.assertValueCount(1);
		RailSearchResponse railSearchResponse = searchResponseObserver.getOnNextEvents().get(0);
		List<RailPassenger> passengers = railSearchResponse.passengerList;
		assertEquals(4, passengers.size());
		assertEquals(30, passengers.get(0).age);
		assertEquals(true, passengers.get(0).primaryTraveler);
		assertEquals(10, passengers.get(1).age);
		assertEquals(false, passengers.get(1).primaryTraveler);
		assertEquals(16, passengers.get(2).age);
		assertEquals(false, passengers.get(2).primaryTraveler);
		assertEquals(60, passengers.get(3).age);
		assertEquals(false, passengers.get(3).primaryTraveler);
		assertTrue(railSearchResponse.offerList.size() > 0);
		List<RailOffer> railOffers = railSearchResponse.offerList;
		assertTrue(railOffers.get(0).railProductList.size() > 0);
		RailProduct railProduct = railOffers.get(0).railProductList.get(0);
		assertEquals(3, railProduct.segmentFareDetailList.size());
		assertEquals(3, railProduct.getSegmentToFareMapping().size());
		assertEquals(1, railProduct.fareQualifierList.size());
		assertTrue(railSearchResponse.legList.get(0).legOptionList.get(0).doesAnyOfferHasFareQualifier);
		assertTrue(railSearchResponse.legList.get(0).legOptionList.get(1).doesAnyOfferHasFareQualifier);
		assertFalse(railSearchResponse.legList.get(0).legOptionList.get(2).doesAnyOfferHasFareQualifier);
		assertEquals(RailsApiStatusCodes.STATUS_SUCCESS, railSearchResponse.responseStatus.status);
	}

	@Test
	public void noSearchResults() {
		givenSearchRequest("no_search_results");
		service.railSearch(railSearchRequest, searchResponseObserver);

		searchResponseObserver.awaitTerminalEvent();

		searchResponseObserver.assertCompleted();
		searchResponseObserver.assertValueCount(1);
		RailSearchResponse railSearchResponse = searchResponseObserver.getOnNextEvents().get(0);

		assertTrue(railSearchResponse.hasError());
		assertEquals(RailsApiStatusCodes.STATUS_SUCCESS, railSearchResponse.responseStatus.status);
		assertEquals(RailsApiStatusCodes.STATUS_CATEGORY_NO_PRODUCT, railSearchResponse.responseStatus.statusCategory);
	}

	@Test
	public void otherSearchErrors() {
		givenSearchRequest("validation_error");
		service.railSearch(railSearchRequest, searchResponseObserver);

		searchResponseObserver.awaitTerminalEvent();

		searchResponseObserver.assertCompleted();
		searchResponseObserver.assertValueCount(1);
		RailSearchResponse railSearchResponse = searchResponseObserver.getOnNextEvents().get(0);

		assertTrue(railSearchResponse.hasError());
	}

	@Test
	public void happyMockCreateTrip() {
		List<String> offerTokens = new ArrayList<>();
		offerTokens.add("fakeToken");

		service.railCreateTrip(offerTokens, createTripResponseObserver);
		createTripResponseObserver.awaitTerminalEvent();

		createTripResponseObserver.assertCompleted();
		createTripResponseObserver.assertValueCount(1);
		RailCreateTripResponse createTripResponse = createTripResponseObserver.getOnNextEvents().get(0);

		assertEquals("2584783d-7b84-406e-9fef-3e8e847d4d87", createTripResponse.tripId);

		assertEquals(1, createTripResponse.railDomainProduct.railOffer.railProductList.size());
		assertNotNull(createTripResponse.railDomainProduct.railOffer.railProductList.get(0).fareQualifierList);

		assertEquals(2, createTripResponse.railDomainProduct.railOffer.priceBreakdown.size());
		assertEquals("£1.00", createTripResponse.railDomainProduct.railOffer.priceBreakdown.get(0).formattedPrice);
		assertEquals("£638.75", createTripResponse.railDomainProduct.railOffer.priceBreakdown.get(1).formattedPrice);
		List<RailPassenger> passengers = createTripResponse.railDomainProduct.railOffer.passengerList;
		assertEquals(RailPassenger.PassengerAgeGroup.ADULT,passengers.get(0).passengerAgeGroup);
		assertEquals(30,passengers.get(0).age);
		assertEquals("£182.50", passengers.get(0).price.formattedPrice);
		assertEquals(RailPassenger.PassengerAgeGroup.CHILD,passengers.get(1).passengerAgeGroup);
		assertEquals(10,passengers.get(1).age);
		assertEquals("£91.25", passengers.get(1).price.formattedPrice);
		assertEquals(RailPassenger.PassengerAgeGroup.YOUTH,passengers.get(2).passengerAgeGroup);
		assertEquals(16,passengers.get(2).age);
		assertEquals("£182.50", passengers.get(2).price.formattedPrice);
		assertEquals(RailPassenger.PassengerAgeGroup.SENIOR,passengers.get(3).passengerAgeGroup);
		assertEquals(60,passengers.get(3).age);
		assertEquals("£182.50", passengers.get(3).price.formattedPrice);
		BigDecimal sum = passengers.get(0).price.amount.add(passengers.get(1).price.amount)
			.add(passengers.get(2).price.amount).add(passengers.get(3).price.amount);
		assertEquals("638.75", sum.toString());
	}

	@Test
	public void happyMockCheckout() {
		RailCheckoutParams params = new RailCheckoutParams(RailCheckoutParamsMock.travelers(),
			RailCheckoutParamsMock.tripDetails(), RailCheckoutParamsMock.paymentInfo(),
			RailCheckoutParamsMock.railTicketDeliveryStationInfo());

		service.railCheckoutTrip(params, checkoutTripResponseObserver);
		checkoutTripResponseObserver.awaitTerminalEvent();

		checkoutTripResponseObserver.assertCompleted();
		checkoutTripResponseObserver.assertValueCount(1);
		RailCheckoutResponse checkoutResponse = checkoutTripResponseObserver.getOnNextEvents().get(0);

		assertEquals("8009690310416", checkoutResponse.orderId);
	}

	@Test
	public void happyGetRailCards() {
		TestSubscriber<RailCardsResponse> railCardsResponseTestSubscriber = new TestSubscriber<>();
		service.railGetCards("en_GB", railCardsResponseTestSubscriber);
		railCardsResponseTestSubscriber.awaitTerminalEvent();
		railCardsResponseTestSubscriber.assertValueCount(1);
		assertEquals(12, railCardsResponseTestSubscriber.getOnNextEvents().get(0).getRailCards().size());
	}

	@Test
	public void happyGetRailCreditCardFees() {
		service.railGetCardFees(RailCheckoutParamsMock.tripDetails().getTripId(),
			RailCheckoutParamsMock.paymentInfo().getCards().get(0).getCreditCardNumber(),
			RailCheckoutParamsMock.railTicketDeliveryStationInfo().getDeliveryOptionToken(), cardFeeResponseObserver);
		cardFeeResponseObserver.awaitTerminalEvent();
		cardFeeResponseObserver.assertCompleted();
		cardFeeResponseObserver.assertValueCount(1);

		CardFeeResponse cardFeeResponse = cardFeeResponseObserver.getOnNextEvents().get(0);
		assertEquals("£2.90", cardFeeResponse.feePrice.formattedPrice);
	}

	private void givenSearchRequest(String clientCode) {
		SuggestionV4 origin = new SuggestionV4();
		SuggestionV4 destination = new SuggestionV4();
		DateTime startDateTime = DateTime.now().plusDays(1);
		LocalDate startDate = startDateTime.toLocalDate();
		Integer startTime = startDateTime.toLocalTime().getMillisOfDay();
		railSearchRequest = new RailApiSearchModel(origin, destination, startDate, null, startTime, null, false, 0, Collections.<Integer>emptyList(), Collections.<Integer>emptyList(), Collections.<Integer>emptyList(), Collections.<RailCard>emptyList());
		railSearchRequest.setClientCode(clientCode);
	}
}
