package com.expedia.bookings.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Satisfies ExpediaDispatcher's user-agent checks
 */
class MockInterceptor : Interceptor {

    private var called = false

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().newBuilder()
        called = true
        val request = chain.request().newBuilder()
        request.addHeader("User-Agent", "ExpediaBookings/1.1 (EHad; Mobiata)")
        url.addQueryParameter("clientid", "expedia.app.android.phone:6.9.0")
        request.url(url.build())
        return chain.proceed(request.build())
    }

    fun wasCalled(): Boolean {
        return called
    }
}
