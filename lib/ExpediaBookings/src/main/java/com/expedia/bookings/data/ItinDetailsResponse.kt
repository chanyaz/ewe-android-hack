package com.expedia.bookings.data

import org.joda.time.DateTime

open class ItinDetailsResponse {
    var responseType: String? = null
    var responseData: ResponseData? = null

    open class ResponseData {
        var levelOfDetail: String? = null
        var tripId: String? = null
        var webDetailsURL: String? = null
        var tripNumber: Long? = null
        var orderNumber: Long? = null
        var title: String? = null
        var updateTripNameDescPathURL: String? = null

        var startTime: DateTime? = null
        var endTime: DateTime? = null

        var totalTripPrice: TotalTripPrice? = null

        class TotalTripPrice {
            var currency: String? = null
            var total: String? = null
            var totalFormatted: String? = null
        }
    }
}
