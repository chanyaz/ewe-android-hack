package com.expedia.bookings.unit;

import java.io.File;
import java.util.LinkedHashSet;

import org.junit.Before;
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
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;

public class CarServicesTest {
	@Rule
	public MockWebServerRule server = new MockWebServerRule();

	private CarServices service;

	@Before
	public void before() {
		RequestInterceptor emptyInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// ignore
			}
		};

		service = new CarServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			emptyInterceptor, Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();
		CarSearchParams params = new CarSearchParams();

		service.carSearch(params, observer);
		observer.awaitTerminalEvent();

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testEmptyMockSearchWorks() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{\"offers\" = []}"));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();
		CarSearchParams params = new CarSearchParams();

		service.carSearch(params, observer);
		observer.awaitTerminalEvent();

		observer.assertValueCount(1);
		observer.assertNoErrors();
		observer.assertCompleted();
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();

		service.carSearch(new CarSearchParams(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		for (CarSearch search : observer.getOnNextEvents()) {
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
		server.get().setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<CarSearch> observer = new TestSubscriber<>();

		service.carSearch(new CarSearchParams(), observer);
		observer.awaitTerminalEvent();

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		TestSubscriber<CarSearch> filterObserver = new TestSubscriber<>();
		service.carFilterSearch(filterObserver, carFilter);
		filterObserver.awaitTerminalEvent();

		filterObserver.assertNoErrors();
		filterObserver.assertCompleted();
		filterObserver.assertValueCount(1);

		for (CarSearch search : filterObserver.getOnNextEvents()) {
			assertEquals(1, search.categories.size());
			assertEquals("Standard", search.categories.get(0).carCategoryDisplayLabel);

			assertEquals(1, search.categories.get(0).offers.size());
			assertEquals("NoCCRequired", search.categories.get(0).offers.get(0).vendor.name);
		}

	}
}
