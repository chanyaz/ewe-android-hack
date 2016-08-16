package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.rail.RailPassenger;
import com.expedia.bookings.data.rail.requests.RailCheckoutRequest;
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel;
import com.expedia.bookings.data.rail.responses.RailCard;
import com.expedia.bookings.data.rail.responses.RailCardsResponse;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.expedia.bookings.data.rail.responses.RailProduct;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.RailServices;
import com.expedia.bookings.utils.Constants;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RailServicesTest {

	@Rule
	public MockWebServer server = new MockWebServer();
	private RailServices service;
	private RailApiSearchModel railSearchRequest;
	private TestSubscriber<RailSearchResponse> searchResponseObserver;
	private TestSubscriber<RailCreateTripResponse> createTripResponseObserver;
	private TestSubscriber<RailCheckoutResponse> checkoutTripResponseObserver;

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
	}

	@Test
	public void happyMockSearch() {
		givenHappySearchRequest();

		service.railSearch(railSearchRequest, searchResponseObserver);
		searchResponseObserver.awaitTerminalEvent();

		searchResponseObserver.assertCompleted();
		searchResponseObserver.assertValueCount(1);
		RailSearchResponse railSearchResponse = searchResponseObserver.getOnNextEvents().get(0);
		List<RailPassenger> passengers = railSearchResponse.passengerList;
		assertEquals(1, passengers.size());
		assertEquals(25, passengers.get(0).age);
		assertEquals(true, passengers.get(0).primaryTraveler);
		assertTrue(railSearchResponse.offerList.size() > 0);
		List<RailSearchResponse.RailOffer> railOffers = railSearchResponse.offerList;
		assertTrue(railOffers.get(0).railProductList.size() > 0);
		RailProduct railProduct = railOffers.get(0).railProductList.get(0);
		assertEquals(3, railProduct.segmentFareDetailList.size());
		assertEquals(3, railProduct.getSegmentToFareMapping().size());
		assertEquals(1, railProduct.fareQualifierList.size());
	}

	@Test
	public void happyMockCreateTrip() {
		String railOfferToken = "fakeToken";

		service.railCreateTrip(railOfferToken, createTripResponseObserver);
		createTripResponseObserver.awaitTerminalEvent();

		createTripResponseObserver.assertCompleted();
		createTripResponseObserver.assertValueCount(1);
		RailCreateTripResponse createTripResponse = createTripResponseObserver.getOnNextEvents().get(0);

		assertEquals("19558305-ea58-4cef-a0dc-6ca1fe6ce901", createTripResponse.tripId);
		assertEquals(1, createTripResponse.railDomainProduct.railOffer.railProductList.size());
	}

	@Test
	public void happyMockCheckout() {
		RailCheckoutRequest params = new RailCheckoutRequest();

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

	private void givenHappySearchRequest() {
		SuggestionV4 origin = new SuggestionV4();
		SuggestionV4 destination = new SuggestionV4();
		DateTime startDateTime = DateTime.now().plusDays(1);
		LocalDate startDate = startDateTime.toLocalDate();
		Integer startTime = startDateTime.toLocalTime().getMillisOfDay();
		railSearchRequest = new RailApiSearchModel(origin, destination, startDate, null, startTime, null, false, Collections.<RailCard>emptyList());
	}
}
