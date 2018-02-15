package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.test.MockClientLogServices;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

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
}
