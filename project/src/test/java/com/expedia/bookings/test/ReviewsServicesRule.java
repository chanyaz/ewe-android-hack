package com.expedia.bookings.test;

import java.io.File;
import java.io.IOException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.expedia.bookings.services.ReviewsServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import rx.schedulers.Schedulers;

public class ReviewsServicesRule implements TestRule {

	public MockWebServer server;
	public ReviewsServices service;

	@Override
	public Statement apply(final Statement base, Description description) {
		server = new MockWebServer();
		server.setDispatcher(diskExpediaDispatcher());

		Statement createService = new Statement() {
			@Override
			public void evaluate() throws Throwable {
				service = new ReviewsServices("http://localhost:" + server.getPort(), new OkHttpClient(), Schedulers
					.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL);
				base.evaluate();
				service = null;
			}
		};

		return server.apply(createService, description);
	}

	public static ExpediaDispatcher diskExpediaDispatcher() {
		String root;
		try {
			root = new File("../lib/mocked/templates").getCanonicalPath();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		FileSystemOpener opener = new FileSystemOpener(root);
		return new ExpediaDispatcher(opener);
	}
}
