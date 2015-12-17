package com.expedia.bookings.interceptors;

import retrofit.RequestInterceptor;

/**
 * Satisfies ExpediaDispatcher's user-agent checks
 */
public class MockInterceptor implements RequestInterceptor {

	@Override
	public void intercept(RequestFacade request) {
		request.addHeader("User-Agent", "ExpediaBookings/1.1 (EHad; Mobiata)");
	}
}
