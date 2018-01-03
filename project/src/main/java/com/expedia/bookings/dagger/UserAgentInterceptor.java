package com.expedia.bookings.dagger;

import com.expedia.bookings.utils.ServicesUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class UserAgentInterceptor implements Interceptor {
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request.Builder request = chain.request().newBuilder();
		request.addHeader("User-Agent", ServicesUtil.generateUserAgentString());
		Response response = chain.proceed(request.build());
		return response;
	}
}
