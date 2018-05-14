package com.expedia.bookings.http

import android.content.Context
import com.expedia.bookings.activity.CaptchaWebViewActivity
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class CaptchaRedirectInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code() == 429) {
            val builder = HttpUrl.Builder()
            val url = request.url()
            builder.scheme(url.scheme())
            builder.host(url.host())
            val originalUrl = response.networkResponse()?.request()?.url().toString()
            val baseUrl = builder.build().toString()
            val bodyStringBytes = response.body()?.bytes()
            if (bodyStringBytes != null) {
                val bodyString = String(bodyStringBytes, Charsets.UTF_8)
                context.startActivity(CaptchaWebViewActivity.IntentBuilder(context, originalUrl, bodyString, baseUrl).intent)
            }
        }
        return response
    }
}
