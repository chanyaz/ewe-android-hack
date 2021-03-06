package com.expedia.bookings.unit;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.SuggestionServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;

public class SuggestionServicesTest {

	private static final int NUM_SUGGESTIONS_IN_V4_MOCK_TEMPLATES = 4;
	private static final int MAX_NUM_SUGGESTIONS_V4_NEARBY_MOCK_TEMPLATES = 3;

	@Rule
	public MockWebServer server = new MockWebServer();

	private SuggestionServices service;

	@Before
	public void before() {
		service = new SuggestionServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			new MockInterceptor(),
			Schedulers.immediate(),
			Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testSublistOfAirportResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<SuggestionV4>> observerV3 = new TestSubscriber<>();
		service.getCarSuggestions("san francisco", "en_US", "expedia.app.android.phone:6.7.0", observerV3);
		observerV3.awaitTerminalEvent();
		assertEquals(NUM_SUGGESTIONS_IN_V4_MOCK_TEMPLATES, observerV3.getOnNextEvents().get(0).size());

		TestSubscriber<List<SuggestionV4>> observerV1 = new TestSubscriber<>();
		String latLong = "37.615940|-122.387996";
		service.getNearbyCarSuggestions("en_US", latLong, 1, "expedia.app.android.phone:6.7.0", observerV1);
		observerV1.awaitTerminalEvent();
		assertEquals(MAX_NUM_SUGGESTIONS_V4_NEARBY_MOCK_TEMPLATES, observerV1.getOnNextEvents().get(0).size());
	}

	@Test
	public void testSublistOfCityResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<SuggestionV4>> observerV3 = new TestSubscriber<>();
		service.getLxSuggestions("seattle", "en_US", "expedia.app.android.phone:6.7.0", observerV3);
		observerV3.awaitTerminalEvent();
		assertEquals(observerV3.getOnNextEvents().get(0).size(), NUM_SUGGESTIONS_IN_V4_MOCK_TEMPLATES);

		TestSubscriber<List<SuggestionV4>> observerV1 = new TestSubscriber<>();
		String latLong = "28.489515|77.092398";
		service.getNearbyLxSuggestions("en_US", latLong, 1, "expedia.app.android.phone:6.7.0", observerV1);
		observerV1.awaitTerminalEvent();
		assertEquals(observerV1.getOnNextEvents().get(0).size(), MAX_NUM_SUGGESTIONS_V4_NEARBY_MOCK_TEMPLATES);
	}

	@Test
	public void testNearbyAirportResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<SuggestionV4>> observer = new TestSubscriber<>();
		String latLong = "37.615940|-122.387996";
		service.getNearbyCarSuggestions("en_US", latLong, 1, "expedia.app.android.phone:6.7.0", observer);
		observer.awaitTerminalEvent();
		assertEquals(observer.getOnNextEvents().get(0).get(0).hierarchyInfo.airport.airportCode, "QSF");
	}

	@Test
	public void testNearbyCityResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<SuggestionV4>> observer = new TestSubscriber<>();
		String latLong = "28.489515|77.092398";
		service.getNearbyLxSuggestions("en_US", latLong, 1, "expedia.app.android.phone:6.7.0", observer);
		observer.awaitTerminalEvent();
		assertEquals(observer.getOnNextEvents().get(0).get(0).regionNames.fullName, "San Francisco (and vicinity), California, United States of America");
	}
}
