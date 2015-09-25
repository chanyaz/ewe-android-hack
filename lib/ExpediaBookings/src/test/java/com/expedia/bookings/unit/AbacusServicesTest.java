package com.expedia.bookings.unit;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.services.AbacusServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AbacusServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	public AbacusServices service;

	@Before
	public void before() {
		service = new AbacusServices("http://localhost:" + server.getPort(),
			new OkHttpClient(),
			new RequestInterceptor() {
				@Override
				public void intercept(RequestFacade request) {
					// ignore
				}
			},
			Schedulers.immediate(),
			Schedulers.immediate(),
			RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testMockDownloadBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<AbacusResponse> observer = new TestSubscriber<>();
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.downloadBucket(query, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testEmptyMockDownloadWorks() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{\"evaluatedExperiments\" = []}"));

		TestSubscriber<AbacusResponse> observer = new TestSubscriber<>();
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.downloadBucket(query, observer);
		observer.awaitTerminalEvent();

		for (AbacusResponse abacus : observer.getOnNextEvents()) {
			assertEquals(0, abacus.numberOfTests());
		}
		observer.assertCompleted();
	}

	@Test
	public void testMockDownloadWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<AbacusResponse> observer = new TestSubscriber<>();
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.downloadBucket(query, observer);
		observer.awaitTerminalEvent();

		observer.assertValueCount(1);
		observer.assertNoErrors();
		observer.assertCompleted();

		AbacusResponse responseV2 = observer.getOnNextEvents().get(0);
		assertEquals(4, responseV2.numberOfTests());
		assertFalse(responseV2.isUserBucketedForTest(9000));
		assertTrue(responseV2.isUserBucketedForTest(3243));
		assertEquals("3243.17887.1", responseV2.getAnalyticsString(3243));
		assertEquals(1, responseV2.variateForTest(3243));
		assertEquals("", responseV2.getAnalyticsString(9999));
		assertNull(responseV2.testForKey(9999));
		assertEquals(AbacusUtils.DefaultVariate.CONTROL.ordinal(), responseV2.variateForTest(9999));
	}

	@Test
	public void testMockEmptyLogWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		AbacusLogQuery query = new AbacusLogQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.logExperiment(query);
	}
}
