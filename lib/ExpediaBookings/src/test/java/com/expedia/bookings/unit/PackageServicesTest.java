package com.expedia.bookings.unit;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.packages.PackageSearchResponse;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.PackageServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class PackageServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private PackageServices service;

	@Before
	public void before() {
		service = new PackageServices("http://localhost:" + server.getPort(),
			new OkHttpClient(), new MockInterceptor(),
			Schedulers.immediate(), Schedulers.immediate(),
			RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<PackageSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26)
			.departure(getDummySuggestion())
			.arrival(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		service.packageSearch(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<PackageSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26)
			.departure(getDummySuggestion())
			.arrival(getDummySuggestion())
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
