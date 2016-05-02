package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.feeds.FeedsResponse;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.FeedsService;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;

public class FeedsServiceTest {

	@Rule
	public MockWebServer server = new MockWebServer();
	private FeedsService service;
	private TestSubscriber<FeedsResponse> feedResponseObserver;

	@Before
	public void before() throws IOException {
		service = new FeedsService("http://localhost:" + server.getPort(), new OkHttpClient(),
			new MockInterceptor(), Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		feedResponseObserver = new TestSubscriber();
	}

	@Test
	public void happyResponse() {
		String tuid = "";
		String expUserId = "";
		service.getFeeds(tuid, expUserId, feedResponseObserver);

		feedResponseObserver.awaitTerminalEvent();
		feedResponseObserver.assertValueCount(1);
		FeedsResponse feedsResponse = feedResponseObserver.getOnNextEvents().get(0);

		assertEquals("ReturnedIfGUIDBasedLookup", feedsResponse.getGuid());
	}
}
