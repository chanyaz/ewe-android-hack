package com.expedia.bookings.http

import android.content.Context
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.Strings
import okhttp3.Interceptor
import okhttp3.Response

class TravelGraphRequestInterceptor(private val context: Context, private val endpointProvider: EndpointProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url().newBuilder()
        val request = chain.request().newBuilder()

        url.setEncodedQueryParameter("clientId", ServicesUtil.getTravelGraphClientId(context))
        val langId = ServicesUtil.generateLangId()
        if (Strings.isNotEmpty(langId)) {
            url.addEncodedQueryParameter("langId", langId)
        }
        request.url(url.build())

        request.addHeader("client-token",
                ServicesUtil.getTravelGraphToken(context, endpointProvider.endPoint))

        return chain.proceed(request.build())
    }
}
