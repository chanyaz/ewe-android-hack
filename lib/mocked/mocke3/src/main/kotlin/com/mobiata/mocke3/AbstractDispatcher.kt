package com.mobiata.mocke3

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse


abstract class AbstractDispatcher(open val fileOpener: FileOpener) : Dispatcher() {

    protected fun getMockResponse(fileName: String, params: Map<String, String>? = null): MockResponse {
        return makeResponse(fileName, params, fileOpener)
    }

    protected fun throwUnsupportedRequestException(urlPath: String) {
        throw UnsupportedOperationException("Sorry, I don't support the request you passed. (Request:" + urlPath + ")")
    }
}
