package com.expedia.bookings.unit;

import com.mobiata.mocke3.FlightApiMockResponseGenerator;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.flights.FlightSearchParams;
import com.expedia.bookings.data.flights.FlightSearchResponse;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.FlightServices;
import com.expedia.bookings.utils.Constants;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import kotlin.Unit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class FlightServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private FlightServices service;

	@Before
	public void before() {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		service = new FlightServices("http://localhost:" + server.getPort(),
			new OkHttpClient.Builder().addInterceptor(logger).build(),
			interceptor, Schedulers.immediate(), Schedulers.immediate());
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));
		PublishSubject<Unit> resultsResponseReceived = PublishSubject.create();

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		TestSubscriber resultsResponseReceivedTestSubscriber = new TestSubscriber();
		resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber);
		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26, 500)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.adults(1)
			.build();

		service.flightSearch(params, observer, resultsResponseReceived);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
		resultsResponseReceivedTestSubscriber.assertValueCount(0);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
		PublishSubject<Unit> resultsResponseReceived = PublishSubject.create();

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		TestSubscriber resultsResponseReceivedTestSubscriber = new TestSubscriber();
		resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber);
		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26, 500)
			.flightCabinClass("COACH")
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.adults(1)
			.build();

		service.flightSearch(params, observer, resultsResponseReceived);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		resultsResponseReceivedTestSubscriber.assertValueCount(1);
		FlightSearchResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(7, response.getLegs().size());
		Assert.assertEquals(5, response.getOffers().size());
		Assert.assertEquals("coach", response.getOffers().get(0).offersSeatClassAndBookingCode.get(0).get(0).seatClass);
		Assert.assertEquals("-3.00", response.getOffers().get(0).discountAmount.amount.toString());
		Assert.assertEquals(Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", "AA"), response.getLegs().get(0).segments.get(0).airlineLogoURL);
	}

	@Test
	public void testMockOutboundSearchWorksForByot() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
		PublishSubject<Unit> resultsResponseReceived = PublishSubject.create();

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		TestSubscriber resultsResponseReceivedTestSubscriber = new TestSubscriber();
		resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber);

		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26, 500)
			.legNo(0)
			.origin(getDummySuggestionForByot())
			.destination(getDummySuggestionForByot())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.adults(1)
			.build();

		service.flightSearch(params, observer, resultsResponseReceived);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		resultsResponseReceivedTestSubscriber.assertValueCount(1);
	}

	@Test
	public void testMockInboundSearchWorksForByot() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
		PublishSubject<Unit> resultsResponseReceived = PublishSubject.create();

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		TestSubscriber resultsResponseReceivedTestSubscriber = new TestSubscriber();
		resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber);


		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26, 500)
			.legNo(1)
			.selectedLegID("leg-id")
			.origin(getDummySuggestionForByot())
			.destination(getDummySuggestionForByot())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.adults(1)
			.build();
		service.flightSearch(params, observer, resultsResponseReceived);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		resultsResponseReceivedTestSubscriber.assertValueCount(1);
	}

	@Test
	public void testSearchErrorWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		SuggestionV4 departureSuggestion = getDummySuggestion();
		String suggestion = FlightApiMockResponseGenerator.SuggestionResponseType.SEARCH_ERROR.getSuggestionString();
		departureSuggestion.hierarchyInfo.airport.airportCode = suggestion;
		departureSuggestion.gaiaId = suggestion;
		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26, 500)
			.origin(departureSuggestion)
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.adults(1)
			.build();

		service.flightSearch(params, observer, null);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		FlightSearchResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(0, response.getLegs().size());
		Assert.assertEquals(0, response.getOffers().size());
	}

	private SuggestionV4 getDummySuggestion()  {
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "";
		suggestion.regionNames = new SuggestionV4.RegionNames();
		suggestion.regionNames.displayName = "";
		suggestion.regionNames.fullName = "";
		suggestion.regionNames.shortName = "";
		suggestion.hierarchyInfo = new SuggestionV4.HierarchyInfo();
		suggestion.hierarchyInfo.airport = new SuggestionV4.Airport();
		suggestion.hierarchyInfo.airport.airportCode = "";
		return suggestion;
	}

	private SuggestionV4 getDummySuggestionForByot() {
		SuggestionV4 dummySuggestion = getDummySuggestion();
		dummySuggestion.hierarchyInfo.airport.airportCode = "byot_search";
		return dummySuggestion;
	}
}
