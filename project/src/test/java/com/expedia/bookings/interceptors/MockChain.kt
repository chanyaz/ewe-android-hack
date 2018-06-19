package com.expedia.bookings.interceptors

import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class MockChain(baseUrl: String = "http://endpoint.com/path", params: String = "") : Interceptor.Chain {

    private val requestBuilder = Request.Builder()
    private val responseBuilder = Response.Builder()

    init {
        requestBuilder.url("$baseUrl$params")

        responseBuilder.request(requestBuilder.build())
        responseBuilder.protocol(Protocol.HTTP_2)
        responseBuilder.code(200)
        responseBuilder.message("OK")
    }

    override fun request(): Request {
        return requestBuilder.build()
    }

    @Throws(IOException::class)
    override fun proceed(request: Request): Response {
        responseBuilder.request(request)
        return responseBuilder.build()
    }

    override fun connection(): Connection? {
        return null
    }

    override fun call(): Call {
        return MockCall(requestBuilder.build(), responseBuilder.build())
    }

    override fun connectTimeoutMillis(): Int {
        return 0
    }

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun readTimeoutMillis(): Int {
        return 0
    }

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun writeTimeoutMillis(): Int {
        return 0
    }

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }
}
