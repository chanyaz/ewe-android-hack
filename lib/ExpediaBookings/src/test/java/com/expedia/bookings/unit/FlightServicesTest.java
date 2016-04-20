package com.expedia.bookings.unit;

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
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

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
			new OkHttpClient.Builder().addInterceptor(logger).addInterceptor(interceptor).build(),
			Schedulers.immediate(), Schedulers.immediate());
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26)
			.departure(getDummySuggestion())
			.arrival(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.adults(1)
			.build();

		service.flightSearch(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<FlightSearchResponse> observer = new TestSubscriber<>();
		FlightSearchParams params = (FlightSearchParams) new FlightSearchParams.Builder(26)
			.departure(getDummySuggestion())
			.arrival(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.adults(1)
			.build();

		service.flightSearch(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		FlightSearchResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(4, response.getLegs().size());
		Assert.assertEquals(2, response.getOffers().size());
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
}
