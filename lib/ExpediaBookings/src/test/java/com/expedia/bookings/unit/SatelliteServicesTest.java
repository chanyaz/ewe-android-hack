package com.expedia.bookings.unit;

import com.expedia.bookings.data.SatelliteSearchResponse;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.SatelliteServices;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class SatelliteServicesTest {

	@Test
	public void happytest() {
		SatelliteServices service;
		TestSubscriber<SatelliteSearchResponse> searchResponseObserver;

		searchResponseObserver = new TestSubscriber();
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		service = new SatelliteServices("https://apim.expedia.com/m/api/config/feature/",new OkHttpClient.Builder().addInterceptor(logger).build(), interceptor, Schedulers.immediate(), Schedulers.immediate());
		service.satelliteSearch(searchResponseObserver);
		searchResponseObserver.awaitTerminalEvent();

	}
}
