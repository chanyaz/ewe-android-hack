package com.expedia.bookings.utils

import okhttp3.Interceptor
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.IOException

class HMACInterceptor(private val username: String, private val storedKey: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val xDate = HMACUtil.getXDate(DateTime.now(DateTimeZone.UTC))
        val salt = HMACUtil.generateSalt(16)
        request.addHeader("Authorization",
                HMACUtil.getAuthorizationHeaderValue(chain.request().url(), chain.request().method(), xDate, salt, storedKey, username))
        request.addHeader("x-date", xDate)
        request.addHeader("salt", salt)
        return chain.proceed(request.build())
    }
}
