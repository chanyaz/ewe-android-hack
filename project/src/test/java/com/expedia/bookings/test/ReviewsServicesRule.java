package com.expedia.bookings.test;

import java.io.File;
import java.io.IOException;

import com.expedia.bookings.services.ReviewsServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;

import retrofit.RestAdapter;
import rx.schedulers.Schedulers;

public class ReviewsServicesRule extends MockWebServerRule {
	private ReviewsServices service;

	@Override
	protected void before() {
		super.before();
		String root;
		try {
			root = new File("../lib/mocked/templates").getCanonicalPath();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		FileSystemOpener opener = new FileSystemOpener(root);
		get().setDispatcher(new ExpediaDispatcher(opener));
		service = new ReviewsServices("http://localhost:" + getPort(), new OkHttpClient(), Schedulers
			.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
	}

	public ReviewsServices reviewsServices() {
		return service;
	}
}
