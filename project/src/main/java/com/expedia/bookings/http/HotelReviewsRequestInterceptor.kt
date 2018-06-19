package com.expedia.bookings.http

import android.content.Context
import com.expedia.bookings.utils.ServicesUtil
import okhttp3.Interceptor
import okhttp3.Response

class HotelReviewsRequestInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().newBuilder()
        val requestBuilder = chain.request().newBuilder()

        requestBuilder.url(url.build())

        val clientId = ServicesUtil.getHotelReviewsClientId(context)
        requestBuilder.addHeader("clientId", clientId)

        val apiKey = ServicesUtil.getHotelReviewsApiKey(context)
        requestBuilder.addHeader("apiKey", apiKey)

        return chain.proceed(requestBuilder.build())
    }
}
