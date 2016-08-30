package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class CardFeeServiceRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path
        val params = parseHttpRequest(request)
        val creditCardId = params["creditCardId"]

        if (!isCardFeeServiceRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when (creditCardId) {
            "" -> getMockResponse("/api/flight/trip/cardFee/zero_fees.json")

            else -> getMockResponse("/api/flight/trip/cardFee/happy.json")
        }
    }

    private fun isCardFeeServiceRequest(urlPath: String): Boolean {
        return doesItMatch("^/api/flight/trip/cardFee.*$", urlPath)
    }
}
