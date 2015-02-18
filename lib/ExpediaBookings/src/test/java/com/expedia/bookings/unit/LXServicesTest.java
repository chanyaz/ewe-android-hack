package com.expedia.bookings.unit;

import java.io.File;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.lx.ActivityDetailsParams;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.services.LXServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class LXServicesTest {
	@Rule
	public MockWebServerRule mockServer = new MockWebServerRule();

	// Search.

	@Test
	public void testLXSearchResponse() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<List<LXActivity>> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(1);
		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();

		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		assertEquals(86, blockingObserver.getItems().get(0).size());

	}

	@Test
	public void testEmptySearchResponse() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{regionId:1,\"activities\": []}"));
		BlockingObserver<List<LXActivity>> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();

		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		assertEquals(0, blockingObserver.getItems().get(0).size());
	}

	@Test(expected = RetrofitError.class)
	public void testUnexpectedSearchResponseThrowsError() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{Unexpected}"));
		BlockingObserver<List<LXActivity>> blockingObserver = new BlockingObserver<>(1);
		LXSearchParams searchParams = new LXSearchParams();

		Subscription subscription = getLXServices().lxSearch(searchParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());
		throw blockingObserver.getErrors().get(0);
	}

	// Details.

	@Test
	public void testDetailsResponse() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mockServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<ActivityDetailsResponse> blockingObserver = new BlockingObserver<>(1);
		ActivityDetailsParams activityDetailsParams = new ActivityDetailsParams();
		activityDetailsParams.activityId = "183615";
		activityDetailsParams.startDate = LocalDate.now();
		activityDetailsParams.endDate = LocalDate.now().plusDays(1);
		Subscription subscription = getLXServices().lxDetails(activityDetailsParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(0,blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		// Check the details object for required values.
		ActivityDetailsResponse activityDetailsResponse = blockingObserver.getItems().get(0);
		assertEquals(5, activityDetailsResponse.images.size());
		assertEquals(5, activityDetailsResponse.highlights.size());
	}

	@Test
	public void testEmptyDetailsResponse() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{\"id\": \"183615\"}"));
		BlockingObserver<ActivityDetailsResponse> blockingObserver = new BlockingObserver<>(1);

		ActivityDetailsParams activityDetailsParams = new ActivityDetailsParams();
		activityDetailsParams.activityId = "183615";
		activityDetailsParams.startDate = LocalDate.now();
		activityDetailsParams.endDate = LocalDate.now().plusDays(1);
		Subscription subscription = getLXServices().lxDetails(activityDetailsParams,blockingObserver);

		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(0, blockingObserver.getErrors().size());
		assertEquals(1, blockingObserver.getItems().size());
		assertNull(blockingObserver.getItems().get(0).images);
	}

	@Test(expected = RetrofitError.class)
	public void testUnexpectedDetailsResponseThrowsError() throws Throwable {
		mockServer.enqueue(new MockResponse().setBody("{Unexpected}"));
		BlockingObserver<ActivityDetailsResponse> blockingObserver = new BlockingObserver<>(1);
		ActivityDetailsParams activityDetailsParams = new ActivityDetailsParams();

		Subscription subscription = getLXServices().lxDetails(activityDetailsParams, blockingObserver);
		blockingObserver.await();
		subscription.unsubscribe();
		assertEquals(1, blockingObserver.getErrors().size());
		assertEquals(0, blockingObserver.getItems().size());
		throw blockingObserver.getErrors().get(0);
	}

	private LXServices getLXServices() {
		return new LXServices("http://localhost:" + mockServer.getPort(), new OkHttpClient(), Schedulers.immediate(),
			Schedulers.immediate());
	}
}
