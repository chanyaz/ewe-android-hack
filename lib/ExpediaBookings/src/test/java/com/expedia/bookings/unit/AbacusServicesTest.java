package com.expedia.bookings.unit;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.services.AbacusServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AbacusServicesTest {
	@Rule
	public MockWebServerRule mServer = new MockWebServerRule();

	@Test(expected = RetrofitError.class)
	public void testMockDownloadBlowsUp() throws Throwable {
		mServer.enqueue(new MockResponse()
			.setBody("{garbage}"));

		BlockingObserver<AbacusResponse> observer = new BlockingObserver<>(1);
		AbacusServices service = new AbacusServices(new OkHttpClient(), "http://localhost:" + mServer.getPort(), Schedulers.immediate(), Schedulers.immediate());

		Subscription sub = service.downloadBucket("TEST-TEST-TEST-TEST", "1", observer);
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
			.setBody("{\"payload\" = {}}"));

		BlockingObserver<AbacusResponse> observer = new BlockingObserver<>(1);
		AbacusServices service = new AbacusServices(new OkHttpClient(), "http://localhost:" + mServer.getPort(), Schedulers.immediate(), Schedulers.immediate());

		Subscription sub = service.downloadBucket("TEST-TEST-TEST-TEST", "1", observer);
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
		AbacusServices service = new AbacusServices(new OkHttpClient(), "http://localhost:" + mServer.getPort(), Schedulers.immediate(), Schedulers.immediate());

		Subscription sub = service.downloadBucket("TEST-TEST-TEST-TEST", "1", observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(1, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());

		for (AbacusResponse abacus : observer.getItems()) {
			assertEquals(7, abacus.numberOfTests());
		}

		assertFalse(observer.getItems().get(0).isUserBucketedForTest("EBTestAlwaysShowHotelDestinationsKey"));
		assertTrue(observer.getItems().get(0).isUserBucketedForTest("EBTestPreferDealsOverDiscountsKey"));
	}

}
