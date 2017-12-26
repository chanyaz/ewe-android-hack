package com.expedia.bookings.utils

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UITestRequestInterceptor : Interceptor {

    var urlToIntercept: List<String> = emptyList()
    var onRequest: ((Request) -> Unit)? = null
    var onResponse: ((Response) -> Unit)? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url()
        val urlStr = url.encodedPath()
        val response: Response

        if (urlToIntercept.contains(urlStr)) {
            onRequest?.invoke(request)
            response = chain.proceed(request)
            onResponse?.invoke(response)
        } else {
            response = chain.proceed(request)
        }
        return response
    }
}
