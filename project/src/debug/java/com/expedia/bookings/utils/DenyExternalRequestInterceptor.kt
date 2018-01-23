package com.expedia.bookings.utils

import okhttp3.Interceptor
import okhttp3.Response

class DenyExternalRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val destinedForLocalhost = originalRequest?.url()?.host() == "localhost"
        if (!destinedForLocalhost) {
            println("External request found! ${originalRequest.url()}")
            return Response.Builder()
                    .request(originalRequest)
                    .code(500)
                    .build()
        }
        return chain.proceed(originalRequest)
    }
}
