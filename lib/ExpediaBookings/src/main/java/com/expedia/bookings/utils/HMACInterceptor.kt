package com.expedia.bookings.utils

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.io.IOException
import java.util.Locale

class HMACInterceptor(
        private val username: String,
        private val storedKey: String,
        private val dateTimeSource: DateTimeSource,
        private val saltSource: SaltSource,
        private val currentDomainSource: CurrentDomainSource
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (shouldNotSignRequest(originalRequest)) {
            return chain.proceed(originalRequest)
        }
        val signedRequestBuilder = originalRequest.newBuilder()
        val xDate = getXDate(dateTimeSource.now(DateTimeZone.UTC))
        val salt = saltSource.salt(16)
        signedRequestBuilder.addHeader("Authorization", getAuthorizationHeaderValue(originalRequest.url(), originalRequest.method(), xDate, salt, storedKey, username))
        signedRequestBuilder.addHeader("x-date", xDate)
        signedRequestBuilder.addHeader("salt", salt)
        return chain.proceed(signedRequestBuilder.build())
    }

    private fun shouldNotSignRequest(originalRequest: Request): Boolean {
        val host = originalRequest.url()?.host() ?: ""
        return (!(host.startsWith("apim.") || host.endsWith(currentDomainSource.currentDomain())))
    }

    private fun getXDate(dateTime: DateTime): String {
        return DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss zzz")
                .withLocale(Locale.ENGLISH)
                .print(dateTime)
    }

    private fun getAuthorizationHeaderValue(url: HttpUrl, method: String, date: String, salt: String, storedKey: String, username: String): String {
        var pathAndQuery = url.encodedPath()
        url.encodedQuery()?.let {
            pathAndQuery += "?$it"
        }
        val requestLine = "$method $pathAndQuery HTTP/1.1"
        val stringToSign = "$requestLine\nx-date: $date\nsalt: $salt"
        val hmac = HMACUtil.createHmac(unObfuscateKey(storedKey), stringToSign)
        return "hmac username=\"$username\",algorithm=\"hmac-sha1\",headers=\"request-line x-date salt\",signature=\"$hmac\""
    }

    private fun unObfuscateKey(storedKey: String): String {
        val key = StringBuilder()
        for (a in storedKey.reversed()) {
            val value = a.toInt() xor 28
            key.append(value.toChar())
        }
        return key.toString()
    }
}
