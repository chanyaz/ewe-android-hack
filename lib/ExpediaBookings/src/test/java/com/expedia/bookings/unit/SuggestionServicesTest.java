package com.expedia.bookings.unit;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.Suggestion;
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

	private static final int NUM_SUGGESTIONS_IN_V3_MOCK_TEMPLATES = 3;
	private static final int NUM_SUGGESTIONS_IN_V1_MOCK_TEMPLATES = 1;

	@Rule
	public MockWebServer server = new MockWebServer();

	private SuggestionServices service;

	@Before
	public void before() {
		service = new SuggestionServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			Schedulers.immediate(),
			Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testSublistOfAirportResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<Suggestion>> observerV3 = new TestSubscriber<>();
		service.getCarSuggestions("seattle", "en_US", observerV3);
		observerV3.awaitTerminalEvent();
		assertEquals(NUM_SUGGESTIONS_IN_V3_MOCK_TEMPLATES, observerV3.getOnNextEvents().get(0).size());

		TestSubscriber<List<Suggestion>> observerV1 = new TestSubscriber<>();
		String latLong = "37.615940|-122.387996";
		service.getNearbyCarSuggestions("en_US", latLong, 1, observerV1);
		observerV1.awaitTerminalEvent();
		assertEquals(NUM_SUGGESTIONS_IN_V1_MOCK_TEMPLATES, observerV1.getOnNextEvents().get(0).size());
	}

	@Test
	public void testSublistOfCityResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<Suggestion>> observerV3 = new TestSubscriber<>();
		service.getLxSuggestions("seattle", "en_US", observerV3);
		observerV3.awaitTerminalEvent();
		assertEquals(observerV3.getOnNextEvents().get(0).size(), NUM_SUGGESTIONS_IN_V3_MOCK_TEMPLATES);

		TestSubscriber<List<Suggestion>> observerV1 = new TestSubscriber<>();
		String latLong = "28.489515|77.092398";
		service.getNearbyLxSuggestions("en_US", latLong, 1, observerV1);
		observerV1.awaitTerminalEvent();
		assertEquals(observerV1.getOnNextEvents().get(0).size(), NUM_SUGGESTIONS_IN_V1_MOCK_TEMPLATES);
	}

	@Test
	public void testNearbyAirportResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<Suggestion>> observer = new TestSubscriber<>();
		String latLong = "37.615940|-122.387996";
		service.getNearbyCarSuggestions("en_US", latLong, 1, observer);
		observer.awaitTerminalEvent();
		assertEquals(observer.getOnNextEvents().get(0).get(0).airportCode, "SFO");
	}

	@Test
	public void testNearbyCityResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<Suggestion>> observer = new TestSubscriber<>();
		String latLong = "28.489515|77.092398";
		service.getNearbyLxSuggestions("en_US", latLong, 1, observer);
		observer.awaitTerminalEvent();
		assertEquals(observer.getOnNextEvents().get(0).get(0).fullName, "Global Business Park, Gurgaon, India");
	}
}
