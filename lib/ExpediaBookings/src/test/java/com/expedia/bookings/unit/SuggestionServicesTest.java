package com.expedia.bookings.unit;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.services.SuggestionServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import rx.Subscription;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;


public class SuggestionServicesTest {
	@Rule
	public MockWebServerRule mServer = new MockWebServerRule();

	@Test
	public void testSublistOfResponse() throws Throwable {
		String root = new File("../mocke3/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		mServer.get().setDispatcher(new ExpediaDispatcher(opener));

		BlockingObserver<List<Suggestion>> observer = new BlockingObserver<>(1);
		SuggestionServices services = getTestSuggestionServices();
		Subscription sub = services.getAirportSuggestions("seattle", observer);
		observer.await();
		sub.unsubscribe();

		assertEquals(observer.getItems().get(0).size(), 2);
	}

	private SuggestionServices getTestSuggestionServices() throws Throwable {
		return new SuggestionServices("http://localhost:" + mServer.getPort(), new OkHttpClient(), Schedulers.immediate(),
			Schedulers.immediate());
	}

}
