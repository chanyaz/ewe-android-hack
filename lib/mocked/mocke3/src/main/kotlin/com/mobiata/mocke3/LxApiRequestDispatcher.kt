package com.mobiata.mocke3

import com.google.gson.JsonParser
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTime

class LxApiRequestDispatcher(fileOpener: FileOpener) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val urlPath = request.path

        if (!LxApiRequestMatcher.isLxApiRequest(urlPath)) {
            throwUnsupportedRequestException(urlPath)
        }

        return when {
            LxApiRequestMatcher.isSearchRequest(urlPath) -> {
                val params = parseHttpRequest(request)
                val location = params["location"]
                // Return happy path response if not testing for special cases.
                return if (location == "search_failure") {
                    getMockResponse("lx/api/search/$location.json")
                }else if (location == "SanFranciscoMip") {
                    getMockResponse("lx/api/search/happyLxMip.json")
                }
                else {
                    getMockResponse("lx/api/search/happy.json")
                }
            }

            LxApiRequestMatcher.isDetailsRequest(urlPath) -> {
                val params = parseHttpRequest(request)
                val activityId = params["activityId"]
                val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
                val startDateTime = DateTime.now().withTimeAtStartOfDay()
                // supply the dates to the response
                params.put("startDate", startDateTime.toString(DATE_TIME_PATTERN))
                // Add availability dates for 13 days which should make the last date selector disabled.
                for (iPlusDays in 1..12) {
                    params.put("startDatePlus" + iPlusDays, startDateTime.plusDays(iPlusDays).toString(DATE_TIME_PATTERN))
                }
                return if (activityId == "happy_gt") {
                    getMockResponse("lx/api/activity/happy_gt.json", params)
                } else {
                    getMockResponse("lx/api/activity/happy.json", params)
                }
            }

            LxApiRequestMatcher.isCreateTripRequest(urlPath) -> {
                val obj = JsonParser().parse(request.body.readUtf8()).asJsonObject
                val activityId = obj.getAsJsonArray("items").get(0).asJsonObject.get("activityId").asString
                if (activityId != null && activityId.isNotBlank()) {
                    return getMockResponse("m/api/lx/trip/create/$activityId.json")
                }
                return make404()
            }

            LxApiRequestMatcher.isCheckoutRequest(urlPath) -> {
                val params = parseHttpRequest(request)
                val firstName = params["firstName"]
                val tripId = params["tripId"]

                when (firstName) {
                    "AlreadyBooked" -> return getMockResponse("m/api/lx/trip/checkout/trip_already_booked.json")
                    "PaymentFailed" -> return getMockResponse("m/api/lx/trip/checkout/payment_failed_trip_id.json")
                    "UnknownError" -> return getMockResponse("m/api/lx/trip/checkout/unknown_error.json")
                    "SessionTimeout" -> return getMockResponse("m/api/lx/trip/checkout/session_timeout.json")
                    "InvalidInput" -> return getMockResponse("m/api/lx/trip/checkout/invalid_input.json")
                    "PriceChange" -> return getMockResponse("m/api/lx/trip/checkout/price_change.json")
                    else -> return getMockResponse("m/api/lx/trip/checkout/$tripId.json")
                }
            }

            else -> make404()
        }
    }
}

class LxApiRequestMatcher {
    companion object {
        fun isLxApiRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/lx/trip/.*$", urlPath) || doesItMatch("^/lx/api/.*$", urlPath)
        }

        fun isSearchRequest(urlPath: String): Boolean {
            return doesItMatch("^/lx/api/search.*$", urlPath)
        }

        fun isDetailsRequest(urlPath: String): Boolean {
            return doesItMatch("^/lx/api/activity.*$", urlPath)
        }

        fun isCreateTripRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/lx/trip/create.*$", urlPath)
        }

        fun isCheckoutRequest(urlPath: String): Boolean {
            return doesItMatch("^/m/api/lx/trip/checkout.*$", urlPath)
        }
    }
}
