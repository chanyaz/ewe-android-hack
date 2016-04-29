package com.expedia.bookings.interceptors;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Satisfies ExpediaDispatcher's user-agent checks
 */
public class MockInterceptor implements Interceptor {

	@Override
	public Response intercept(Interceptor.Chain chain) throws IOException {
		HttpUrl.Builder url = chain.request().url().newBuilder();
		Request.Builder request = chain.request().newBuilder();
		request.addHeader("User-Agent", "ExpediaBookings/1.1 (EHad; Mobiata)");
		url.addQueryParameter("clientid", "expedia.app.android.phone:6.9.0");
		request.url(url.build());
		Response response = chain.proceed(request.build());
		return response;
	}
}
