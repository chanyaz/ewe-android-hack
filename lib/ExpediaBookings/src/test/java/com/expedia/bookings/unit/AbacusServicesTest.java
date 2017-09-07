package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.abacus.ABTest;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.AbacusServices;
import com.google.gson.JsonSyntaxException;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AbacusServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	public AbacusServices service;

	@Before
	public void before() {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		service = new AbacusServices("http://localhost:" + server.getPort(),
			new OkHttpClient.Builder().addInterceptor(logger).build(),
			interceptor,
			Schedulers.immediate(),
			Schedulers.immediate());
	}

	@Test
	public void testMockDownloadBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<AbacusResponse> observer = new TestSubscriber<>();
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.downloadBucket(query, observer);
		observer.awaitTerminalEvent();
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(JsonSyntaxException.class);
	}

	@Test
	public void testEmptyMockDownloadWorks() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{\"evaluatedExperiments\" : []}"));

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
		TestSubscriber<AbacusResponse> observer = getAbacusResponseTestSubscriber();

		observer.assertValueCount(1);
		observer.assertNoErrors();
		observer.assertCompleted();

		AbacusResponse responseV2 = observer.getOnNextEvents().get(0);
		assertEquals(5, responseV2.numberOfTests());

		assertNull(responseV2.testForKey(new ABTest(9000)));

		ABTest test = new ABTest(3243);
		assertNotNull(responseV2.testForKey(test));
		assertTrue(responseV2.testForKey(test).isUserInBucket());
		assertEquals("3243.17887.1", responseV2.getAnalyticsString(test));
		assertEquals(1, responseV2.variateForTest(test));

		test = new ABTest(9999);
		assertEquals("", responseV2.getAnalyticsString(test));
		assertNull(responseV2.testForKey(test));
		assertEquals(AbacusUtils.DefaultVariant.CONTROL.ordinal(), responseV2.variateForTest(test));
	}

	@Test
	public void testMockEmptyLogWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		AbacusLogQuery query = new AbacusLogQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.logExperiment(query);
	}

	@Test
	public void testForceUpdateTestMap() throws Throwable {
		TestSubscriber<AbacusResponse> observer = getAbacusResponseTestSubscriber();

		observer.assertValueCount(1);
		observer.assertNoErrors();
		observer.assertCompleted();

		AbacusResponse responseV2 = observer.getOnNextEvents().get(0);
		assertEquals(5, responseV2.numberOfTests());

		ABTest test = new ABTest(3243);
		assertEquals(1, responseV2.variateForTest(test));

		//force update the test map
		responseV2.forceUpdateABTest(3243, 0);
		assertEquals(0, responseV2.variateForTest(test));
	}

	@Test
	public void testUpdateABTestForDebug() throws Throwable {
		TestSubscriber<AbacusResponse> observer = getAbacusResponseTestSubscriber();

		AbacusResponse responseV2 = observer.getOnNextEvents().get(0);
		assertEquals(5, responseV2.numberOfTestsDebugMap());
		ABTest test = new ABTest(3243);
		assertEquals(1, responseV2.variateForTest(test));

		responseV2.updateABTestForDebug(3243, 0);
		assertEquals(0, responseV2.variateForTest(test));

		//for a test key not present in the map
		responseV2.updateABTestForDebug(5555, 1);
		assertEquals(1, responseV2.variateForTest(new ABTest(5555)));
	}

	private TestSubscriber<AbacusResponse> getAbacusResponseTestSubscriber() throws IOException {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<AbacusResponse> observer = new TestSubscriber<>();
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		service.downloadBucket(query, observer);
		observer.awaitTerminalEvent();
		return observer;
	}

	@Test
	public void testUpdateFromAbacusResponse() throws Throwable {
		TestSubscriber<AbacusResponse> observer = getAbacusResponseTestSubscriber();

		AbacusResponse responseV2 = observer.getOnNextEvents().get(0);
		assertEquals(5, responseV2.numberOfTestsDebugMap());

		TestSubscriber<AbacusResponse> newObserver = new TestSubscriber<>();
		AbacusEvaluateQuery newQuery = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 2, 0);
		service.downloadBucket(newQuery, newObserver);
		newObserver.awaitTerminalEvent();
		newObserver.assertCompleted();

		AbacusResponse newResponseV2 = newObserver.getOnNextEvents().get(0);
		responseV2.updateFrom(newResponseV2);
		assertEquals(3, newResponseV2.numberOfTestsDebugMap());
	}
}
