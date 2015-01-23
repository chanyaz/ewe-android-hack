package com.expedia.bookings.unit;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.services.CarServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;

public class CarServicesTest {
	@Rule
	public MockWebServerRule mServer = new MockWebServerRule();

	@Test(expected = RetrofitError.class)
	public void testMockSearchBlowsUp() throws Throwable {
		mServer.enqueue(new MockResponse()
			.setBody("{garbage}"));

		BlockingObserver<CarSearch> observer = new BlockingObserver<>(1);
		CarSearchParams params = new CarSearchParams();
		CarServices service = getTestCarServices();

		Subscription sub = service.carSearch(params, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(0, observer.getItems().size());
		assertEquals(1, observer.getErrors().size());
		for (Throwable error : observer.getErrors()) {
			throw error;
		}
	}

	@Test
	public void testEmptyMockSearchWorks() throws Throwable {
		mServer.enqueue(new MockResponse()
			.setBody("{\"offers\" = []}"));

		BlockingObserver<CarSearch> observer = new BlockingObserver<>(1);
		CarSearchParams params = new CarSearchParams();
		CarServices service = getTestCarServices();

		Subscription sub = service.carSearch(params, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(1, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<CarSearch> observer = new BlockingObserver<>(1);
		CarServices service = getTestCarServices();

		Subscription sub = service.doBoringCarSearch(observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(1, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());

		for (CarSearch search : observer.getItems()) {
			assertEquals(9, search.carCategoryOfferMap.keySet().size());
		}
	}

	private CarServices getTestCarServices() throws Throwable {
		return new CarServices("http://localhost:" + mServer.getPort(), new OkHttpClient(), Schedulers.immediate(),
			Schedulers.immediate());
	}

}
