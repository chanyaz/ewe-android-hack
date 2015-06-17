package com.expedia.bookings.unit;

import java.io.File;
import java.util.LinkedHashSet;

import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarFilter;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.Transmission;
import com.expedia.bookings.services.CarServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<CarSearch> observer = new BlockingObserver<>(2);
		CarServices service = getTestCarServices();

		Subscription sub = service.carSearch(new CarSearchParams(), observer);
		observer.await();
		sub.unsubscribe();

		for (Throwable throwable : observer.getErrors()) {
			throw throwable;
		}
		assertTrue(observer.completed());
		assertEquals(1, observer.getItems().size());

		for (CarSearch search : observer.getItems()) {
			assertEquals(4, search.categories.size());
		}
	}

	@Test
	public void testMockFilterSearchWorks() throws Throwable {
		//Set Car filter object
		CarFilter carFilter = new CarFilter();
		carFilter.categoriesIncluded = new LinkedHashSet();
		carFilter.suppliersIncluded = new LinkedHashSet();
		carFilter.carTransmissionType = Transmission.MANUAL_TRANSMISSION;
		carFilter.suppliersIncluded.add("NoCCRequired");

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<CarSearch> observer = new BlockingObserver<>(2);
		CarServices service = getTestCarServices();

		Subscription sub = service.carSearch(new CarSearchParams(), observer);
		observer.await();
		sub.unsubscribe();

		for (Throwable throwable : observer.getErrors()) {
			throw throwable;
		}
		assertTrue(observer.completed());
		assertEquals(1, observer.getItems().size());

		BlockingObserver<CarSearch> filterObserver = new BlockingObserver<>(2);
		CarServices filterService = getTestCarServices();
		Subscription filterSub = filterService.carFilterSearch(filterObserver, carFilter);
		filterObserver.await();
		filterSub.unsubscribe();

		for (Throwable throwable : filterObserver.getErrors()) {
			throw throwable;
		}
		assertTrue(filterObserver.completed());
		assertEquals(1, filterObserver.getItems().size());

		for (CarSearch search : filterObserver.getItems()) {
			assertEquals(1, search.categories.size());
			assertEquals("Standard", search.categories.get(0).carCategoryDisplayLabel);

			assertEquals(1, search.categories.get(0).offers.size());
			assertEquals("NoCCRequired", search.categories.get(0).offers.get(0).vendor.name);
		}

	}

	private CarServices getTestCarServices() throws Throwable {
		return new CarServices("http://localhost:" + mServer.getPort(), new OkHttpClient(),
			sEmptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	private static final RequestInterceptor sEmptyInterceptor = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {

		}
	};

}
