package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest

public abstract class AbstractDispatcher(open val fileOpener: FileOpener) : Dispatcher() {

    protected fun getMockResponse(fileName: String, params: Map<String, String>? = null): MockResponse {
        return makeResponse(fileName, params, fileOpener)
    }
}
