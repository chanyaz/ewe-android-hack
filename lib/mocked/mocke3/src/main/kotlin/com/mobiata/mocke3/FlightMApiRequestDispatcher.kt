package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.LinkedHashMap

class FlightMApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        var params: MutableMap<String, String> = LinkedHashMap()

        if (!FlightMApiRequestMatcher.isFlightMApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            FlightMApiRequestMatcher.isRichContentRequest(urlPath) ->
                getMockResponse(FlightMApiMockResponseGenerator.getRichContentResponseFilePath())

            FlightMApiRequestMatcher.isSearchRequest(urlPath)-> {
                getMockResponse(FlightMApiMockResponseGenerator.getSearchResponseFilePath(params), params)
            }

            else -> make404()
        }
    }
}

class FlightMApiMockResponseGenerator {

    companion object {
        val RICH_CONTENT = "flight_rich_content"

        fun getRichContentResponseFilePath(): String {
            return "m/api/flight/richcontent/$RICH_CONTENT.json"
        }

        fun getSearchResponseFilePath(params: MutableMap<String, String>): String {
            val departureAirport = if (params["departureAirport"].isNullOrBlank()) "SEA" else params["departureAirport"]
            val suggestionResponseType = FlightDispatcherUtils.SuggestionResponseType.getValueOf(departureAirport!!)

            val returnDate = if (params["returnDate"].isNullOrBlank()) "10-12-2018" else params["returnDate"]

            val isReturnFlightSearch = returnDate!!.isNotEmpty()
            val departureDate = if (params["departureDate"].isNullOrBlank()) "10-10-2018" else params["departureDate"]

            val fileName =
                    if (isReturnFlightSearch) {
                        suggestionResponseType.suggestionString + "_round_trip"
                    } else {
                        suggestionResponseType.suggestionString + "_one_way"
                    }

            val departCalTakeoff = parseYearMonthDay(departureDate, 10, 0)
            val departCalLanding = parseYearMonthDay(departureDate, 12 + 4, 0)
            params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.timeInMillis / 1000))
            params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.timeInMillis / 1000))

            if (isReturnFlightSearch || suggestionResponseType == FlightDispatcherUtils.SuggestionResponseType.MAY_CHARGE_OB_FEES) {
                val returnCalTakeoff = parseYearMonthDay(returnDate, 10, 0)
                val returnCalLanding = parseYearMonthDay(returnDate, 12 + 4, 0)
                params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.timeInMillis / 1000))
                params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.timeInMillis / 1000))
            }
            params.put("tzOffsetSeconds", "" + (departCalTakeoff.timeZone.getOffset(departCalTakeoff.timeInMillis) / 1000))

            return "api/flight/search/$fileName.json"
        }
    }
}

class FlightMApiRequestMatcher() {
    companion object {
        fun isFlightMApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/.*$", urlPath)
        }

        fun isRichContentRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/getRichContent.*$", urlPath)
        }

        fun isSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/flight/search.*$", urlPath)
        }
    }
}
