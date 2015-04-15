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
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AbacusServicesTest {
	@Rule
	public MockWebServerRule mServer = new MockWebServerRule();

	public AbacusServices service;

	@Before
	public void before() {
		service = new AbacusServices(new OkHttpClient(),
			"http://localhost:" + mServer.getPort(),
			Schedulers.immediate(),
			Schedulers.immediate(),
			RestAdapter.LogLevel.FULL);
	}

	@Test(expected = RetrofitError.class)
	public void testMockDownloadBlowsUp() throws Throwable {
		mServer.enqueue(new MockResponse()
			.setBody("{garbage}"));

		BlockingObserver<AbacusResponse> observer = new BlockingObserver<>(1);
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		Subscription sub = service.downloadBucket(query, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(0, observer.getItems().size());
		assertEquals(1, observer.getErrors().size());
		for (Throwable error : observer.getErrors()) {
			throw error;
		}
	}

	@Test
	public void testEmptyMockDownloadWorks() throws Throwable {
		mServer.enqueue(new MockResponse()
			.setBody("{\"evaluatedExperiments\" = []}"));

		BlockingObserver<AbacusResponse> observer = new BlockingObserver<>(1);
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		Subscription sub = service.downloadBucket(query, observer);
		observer.await();
		sub.unsubscribe();

		for (AbacusResponse abacus : observer.getItems()) {
			assertEquals(0, abacus.numberOfTests());
		}
		assertEquals(0, observer.getErrors().size());
	}

	@Test
	public void testMockDownloadWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<AbacusResponse> observer = new BlockingObserver<>(1);
		AbacusEvaluateQuery query = new AbacusEvaluateQuery("TEST-TEST-TEST-TEST", 1, 0);
		Subscription sub = service.downloadBucket(query, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(1, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());

		for (AbacusResponse abacus : observer.getItems()) {
			assertEquals(4, abacus.numberOfTests());
		}

		AbacusResponse responseV2 = observer.getItems().get(0);
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
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<AbacusResponse> observer = new BlockingObserver<>(1);
		AbacusLogQuery query = new AbacusLogQuery("TEST-TEST-TEST-TEST", 1, 0);
		Subscription sub = service.logExperiment(query);
		observer.await();
		sub.unsubscribe();

		assertEquals(0, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());
	}
}
