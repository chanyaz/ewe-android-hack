package com.expedia.bookings.http

import android.content.Context
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.utils.ServicesUtil
import okhttp3.Interceptor
import okhttp3.Response

class HotelShortlistRequestInterceptor(private val context: Context, private val endpointProvider: EndpointProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().newBuilder()
        val requestBuilder = chain.request().newBuilder()

        url.removeAllEncodedQueryParameters("clientid")
        url.removeAllEncodedQueryParameters("langid")

        val clientId = ServicesUtil.getHotelShortlistClientId(context)
        url.setEncodedQueryParameter("clientId", clientId)
        val langId = ServicesUtil.generateLangId()
        if (!langId.isEmpty()) {
            url.setEncodedQueryParameter("langId", langId)
        }

        requestBuilder.url(url.build())

        requestBuilder.addHeader("client-token",
                ServicesUtil.getHotelShortlistClientToken(context, endpointProvider.endPoint))

        return chain.proceed(requestBuilder.build())
    }
}
