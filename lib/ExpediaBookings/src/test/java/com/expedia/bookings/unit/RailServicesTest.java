package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.rail.Passengers;
import com.expedia.bookings.data.rail.requests.RailValidateRequest;
import com.expedia.bookings.data.rail.responses.RailSearchResponse;
import com.expedia.bookings.data.rail.requests.RailDetailsRequest;
import com.expedia.bookings.data.rail.responses.RailDetailsResponse;
import com.expedia.bookings.data.rail.responses.RailValidateResponse;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.data.rail.requests.RailSearchRequest;
import com.expedia.bookings.services.RailServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RailServicesTest {

	@Rule
	public MockWebServer server = new MockWebServer();
	private RailServices service;
	private RailSearchRequest railSearchRequest;
	private TestSubscriber<RailSearchResponse> searchResponseObserver;
	private TestSubscriber<RailDetailsResponse> detailsResponseObserver;
	private TestSubscriber<RailValidateResponse> validateResponseObserver;
	private RailDetailsRequest railDetailsRequest;
	private RailValidateRequest railValidateRequest;

	@Before
	public void before() throws IOException {
		service = new RailServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			new MockInterceptor(), Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		searchResponseObserver = new TestSubscriber();
		detailsResponseObserver = new TestSubscriber();
		validateResponseObserver = new TestSubscriber();
	}

	@Test
	public void happyMockSearch() {
		givenHappySearchRequest();

		service.railSearch(railSearchRequest, searchResponseObserver);
		searchResponseObserver.awaitTerminalEvent();

		searchResponseObserver.assertCompleted();
		searchResponseObserver.assertValueCount(1);
		RailSearchResponse railSearchResponse = searchResponseObserver.getOnNextEvents().get(0);
		List<Passengers> passengers = railSearchResponse.railSearchResult.passengers;
		assertEquals(2, passengers.size());
		assertEquals(30, passengers.get(0).age);
		assertEquals(14, passengers.get(1).age);
	}

	@Test
	public void happyMockDetails() {
		givenHappyDetailsRequest();

		service.railDetails(railDetailsRequest, detailsResponseObserver);
		detailsResponseObserver.awaitTerminalEvent();

		detailsResponseObserver.assertCompleted();
		detailsResponseObserver.assertValueCount(1);
		RailDetailsResponse railDetailsResponse = detailsResponseObserver.getOnNextEvents().get(0);
		assertNotNull(railDetailsResponse.railGetDetailsResult);
	}

	@Test
	public void happyMockValidate() {
		givenHappyValidateRequest();

		service.railValidate(railValidateRequest, validateResponseObserver);
		validateResponseObserver.awaitTerminalEvent();

		validateResponseObserver.assertCompleted();
		validateResponseObserver.assertValueCount(1);
		RailValidateResponse railValidateResponse = validateResponseObserver.getOnNextEvents().get(0);
		// TODO validate the response
		 assertNotNull(railValidateResponse.railGetDetailsResult);
	}

	private void givenHappyValidateRequest() {
		railValidateRequest = new RailValidateRequest();
	}

	private void givenHappyDetailsRequest() {
		railDetailsRequest = new RailDetailsRequest();
	}

	private void givenHappySearchRequest() {
		railSearchRequest = new RailSearchRequest();
	}
}
