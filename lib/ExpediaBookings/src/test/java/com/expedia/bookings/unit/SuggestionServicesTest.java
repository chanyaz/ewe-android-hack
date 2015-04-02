package com.expedia.bookings.unit;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.services.SuggestionServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;

public class SuggestionServicesTest {

	private static final int NUM_SUGGESTIONS_IN_V3_MOCK_TEMPLATES = 3;
	private static final int NUM_SUGGESTIONS_IN_V1_MOCK_TEMPLATES = 1;

	@Rule
	public MockWebServerRule mServer = new MockWebServerRule();

	@Test
	public void testSublistOfAirportResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		//v3 suggestion request response
		BlockingObserver<List<Suggestion>> observerV3 = new BlockingObserver<>(1);
		SuggestionServices services = getTestSuggestionServices();
		Subscription subV3 = services.getAirportSuggestions("seattle", observerV3);
		observerV3.await();
		subV3.unsubscribe();

		assertEquals(observerV3.getItems().get(0).size(), NUM_SUGGESTIONS_IN_V3_MOCK_TEMPLATES);

		BlockingObserver<List<Suggestion>> observerV1 = new BlockingObserver<>(1);
		String latLong = "37.615940|-122.387996";
		Subscription subV1 = services.getNearbyAirportSuggestions("en_US", latLong, 1, observerV1);

		observerV1.await();
		subV1.unsubscribe();

		assertEquals(observerV1.getItems().get(0).size(), NUM_SUGGESTIONS_IN_V1_MOCK_TEMPLATES);
	}

	@Test
	public void testSublistOfCityResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		//v3 suggestion request response
		BlockingObserver<List<Suggestion>> observerV3 = new BlockingObserver<>(1);
		SuggestionServices services = getTestSuggestionServices();
		Subscription subV3 = services.getLxSuggestions("seattle", observerV3);
		observerV3.await();
		subV3.unsubscribe();

		assertEquals(observerV3.getItems().get(0).size(), NUM_SUGGESTIONS_IN_V3_MOCK_TEMPLATES);

		BlockingObserver<List<Suggestion>> observerV1 = new BlockingObserver<>(1);
		String latLong = "28.489515|77.092398";
		Subscription subV1 = services.getNearbyLxSuggestions("en_US", latLong, 1, observerV1);
		observerV1.await();
		subV1.unsubscribe();

		assertEquals(observerV1.getItems().get(0).size(), NUM_SUGGESTIONS_IN_V1_MOCK_TEMPLATES);
	}

	@Test
	public void testNearbyAirportResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<List<Suggestion>> observer = new BlockingObserver<>(1);
		SuggestionServices services = getTestSuggestionServices();
		String latLong = "37.615940|-122.387996";
		Subscription sub = services.getNearbyAirportSuggestions("en_US", latLong, 1, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(observer.getItems().get(0).get(0).airportCode, "SFO");
	}

	@Test
	public void testNearbyCityResponse() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<List<Suggestion>> observer = new BlockingObserver<>(1);
		SuggestionServices services = getTestSuggestionServices();
		String latLong = "28.489515|77.092398";
		Subscription sub = services.getNearbyLxSuggestions("en_US", latLong, 1, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(observer.getItems().get(0).get(0).fullName, "Global Business Park, Gurgaon, India");
	}

	private SuggestionServices getTestSuggestionServices() throws Throwable {
		return new SuggestionServices("http://localhost:" + mServer.getPort(), new OkHttpClient(),
			Schedulers.immediate(),
			Schedulers.immediate());
	}
}
