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
        val zeroFeesResponse = getMockResponse("/api/flight/trip/cardFee/zero_fees.json")

        return when (creditCardId) {
            ""  -> zeroFeesResponse

            "000000" -> zeroFeesResponse

            "343434" -> zeroFeesResponse

            "6011111111111111" -> getMockResponse("/api/flight/trip/cardFee/flex_arbitrage.json")

            else -> getMockResponse("/api/flight/trip/cardFee/happy.json")
        }
    }

    private fun isCardFeeServiceRequest(urlPath: String): Boolean {
        return doesItMatch("^/api/flight/trip/cardFee.*$", urlPath)
    }
}
