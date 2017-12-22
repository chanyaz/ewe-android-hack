package com.expedia.bookings.utils

import okhttp3.Interceptor
import okhttp3.Response

class DenyExternalRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val destinedForLocalhost = originalRequest?.url()?.host()?.contains("localhost:") == true
        if (!destinedForLocalhost) {
            return Response.Builder().code(500).build()
        }
        return chain.proceed(originalRequest)
    }
}