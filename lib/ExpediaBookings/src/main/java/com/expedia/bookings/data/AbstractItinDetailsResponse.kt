package com.expedia.bookings.data

import java.util.ArrayList

abstract class AbstractItinDetailsResponse {
    var responseType: String? = null
    val errors = ArrayList<ApiError>()

    open class ResponseData {
        var levelOfDetail: String? = null
        var tripId: String? = null
        var webDetailsURL: String? = null
        var tripNumber: Long? = null
        var orderNumber: Long? = null
        var title: String? = null
        var updateTripNameDescPathURL: String? = null
        val sharableDetailsURL: String? = null
        var rewardList = emptyList<Rewards>()

        lateinit var startTime: Time
        lateinit var endTime: Time

        var totalTripPrice: TotalTripPrice? = null

        class TotalTripPrice {
            var currency: String? = null
            var total: String? = null
            var totalFormatted: String? = null
        }

        class Rewards {
            var totalPoints: Long? = null
        }
    }

    class Time {
        lateinit var localizedShortDate: String
        lateinit var localizedMediumDate: String
        lateinit var localizedShortTime: String
        lateinit var raw: String
    }

    abstract fun getResponseDataForItin(): ResponseData?
}
