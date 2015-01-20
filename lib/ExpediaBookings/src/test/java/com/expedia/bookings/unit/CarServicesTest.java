package com.expedia.bookings.unit;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.Rule;

import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.services.CarServices;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import retrofit.RetrofitError;
import rx.Observer;
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
		CarServices service = new CarServices("http://localhost:" + mServer.getPort(), Schedulers.immediate(), Schedulers.immediate());

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
		CarServices service = new CarServices("http://localhost:" + mServer.getPort(), Schedulers.immediate(), Schedulers.immediate());

		Subscription sub = service.carSearch(params, observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(1, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File( "../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<CarSearch> observer = new BlockingObserver<>(1);
		CarServices service = new CarServices("http://localhost:" + mServer.getPort(), Schedulers.immediate(), Schedulers.immediate());

		Subscription sub = service.doBoringCarSearch(observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(1, observer.getItems().size());
		assertEquals(0, observer.getErrors().size());

		for (CarSearch search : observer.getItems()) {
			assertEquals(9, search.carCategoryOfferMap.keySet().size());
		}
	}

	public static class BlockingObserver<T> implements Observer<T> {
		private final CountDownLatch mLatch;
		private final ArrayList<T> mItems = new ArrayList<>();
		private final ArrayList<Throwable> mErrors = new ArrayList<>();

		public BlockingObserver(int count) {
			mLatch = new CountDownLatch(count);
		}

		@Override
		public void onCompleted() {
			mLatch.countDown();
		}

		@Override
		public void onError(Throwable e) {
			mErrors.add(e);
			mLatch.countDown();
		}

		@Override
		public void onNext(T object) {
			mItems.add(object);
			mLatch.countDown();
		}

		public void await() throws Throwable {
			mLatch.await(5, TimeUnit.SECONDS);
		}

		public ArrayList<T> getItems() {
			return mItems;
		}

		public ArrayList<Throwable> getErrors() {
			return mErrors;
		}
	}
}
