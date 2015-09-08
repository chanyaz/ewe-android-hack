package com.expedia.bookings.test;

import java.io.File;
import java.io.IOException;

import com.expedia.bookings.services.HotelServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.schedulers.Schedulers;

public class HotelServicesRule extends MockWebServerRule {
	private HotelServices service;

	@Override
	protected void before() {
		super.before();
		RequestInterceptor emptyInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				// ignore
			}
		};

		String root;
		try {
			root = new File("../lib/mocked/templates").getCanonicalPath();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		FileSystemOpener opener = new FileSystemOpener(root);
		get().setDispatcher(new ExpediaDispatcher(opener));
		service = new HotelServices("http://localhost:" + getPort(), new OkHttpClient(), emptyInterceptor, Schedulers
			.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	public HotelServices hotelServices() {
		return service;
	}
}
