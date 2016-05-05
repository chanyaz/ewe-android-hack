package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.packages.PackageCreateTripParams;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.packages.PackageSearchResponse;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.PackageServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class PackageServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private PackageServices service;

	@Before
	public void before() {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		service = new PackageServices("http://localhost:" + server.getPort(),
			new OkHttpClient.Builder().addInterceptor(logger).addInterceptor(interceptor).build(),
			Schedulers.immediate(), Schedulers.immediate());
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<PackageSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		service.packageSearch(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<PackageSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		service.packageSearch(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		PackageSearchResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(50, response.packageResult.hotelsPackage.hotels.size());
		Assert.assertEquals(2, response.packageResult.flightsPackage.flights.size());
	}

	@Test
	public void testCreateTripMultiTravelerWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		String prodID = "create_trip_multitraveler";
		String destID = "6139057";

		TestSubscriber<PackageCreateTripResponse> observer = new TestSubscriber<>();
		PackageCreateTripParams params = new PackageCreateTripParams(prodID, destID, 2, false, Arrays.asList(0, 8, 12));
		service.createTrip(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertCompleted();
		PackageCreateTripResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals("$2,202.34", response.packageDetails.pricing.packageTotal.getFormattedMoneyFromAmountAndCurrencyCode());
		Assert.assertEquals("4", response.packageDetails.flight.details.offer.numberOfTickets);
	}

	@Test
	public void testInfantsInSeat() throws Throwable {
		PackageCreateTripParams params = new PackageCreateTripParams("", "", 2, true, Arrays.asList(0, 8, 12));
		Assert.assertTrue(params.isInfantsInLap());

		params = new PackageCreateTripParams("", "", 2, false, Arrays.asList(8, 8, 12));
		Assert.assertFalse(params.isInfantsInLap());
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
		suggestion.hierarchyInfo.airport.multicity = "happy";
		return suggestion;
	}
}
