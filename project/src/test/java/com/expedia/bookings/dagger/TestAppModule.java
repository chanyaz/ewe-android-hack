package com.expedia.bookings.dagger;

import javax.inject.Named;

import android.content.Context;

import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.test.MockClientLogServices;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Overrides <code>@Provides</code> methods to return mocked objects for testing.
 */
public class TestAppModule extends AppModule {
	public TestAppModule(Context context) {
		super(context);
	}

	@Override
	IClientLogServices provideClientLog(OkHttpClient client, EndpointProvider endpointProvider,
		Interceptor interceptor) {
		return new MockClientLogServices();
	}

	@Override
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("ESSInterceptor") Interceptor essRequestInterceptor,
		@Named("GaiaInterceptor") Interceptor gaiaRequestInterceptor) {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor mockInterceptor = new MockInterceptor();
		MockWebServer mockWebServer = new MockWebServer();
		return new SuggestionV4Services("http://localhost:" + mockWebServer.getPort(),
			"http://localhost:" + mockWebServer.getPort(), client, mockInterceptor,
			mockInterceptor, mockInterceptor, Schedulers.trampoline(), Schedulers.trampoline());
	}
}
