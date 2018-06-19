package com.expedia.bookings.interceptors

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response

class MockCall(val request: Request, val response: Response) : Call {

    override fun request(): Request {
        return request
    }

    override fun execute(): Response {
        return response
    }

    override fun enqueue(responseCallback: Callback?) {
    }

    override fun cancel() {
    }

    override fun isExecuted(): Boolean {
        return true
    }

    override fun isCanceled(): Boolean {
        return true
    }

    override fun clone(): Call {
        return this
    }
}
