package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.data.hotels.Hotel
import org.joda.time.LocalDate

class HotelSearchTrackingData {
    var city: String? = null
    var stateProvinceCode: String? = null
    var countryCode: String? = null

    var region: String? = null
    var freeFormRegion: String? = null
    var searchRegionId: String? = null

    var checkInDate: LocalDate? = null
    var checkoutDate: LocalDate? = null

    var numberOfGuests = 0
    var numberOfAdults = 0
    var numberOfChildren = 0

    var searchWindowDays: String? = null
    var daysOut: Int? = null
    var duration: Int? = null

    var resultsReturned = false
    var numberOfResults: String? = null
    var hasSponsoredListingPresent = false
    var lowestHotelTotalPrice: String? = null
    var hotels: List<Hotel> = emptyList()

    var performanceData = PerformanceData()

    fun hasResponse() : Boolean {
        return resultsReturned
    }

    class PerformanceData {
        var timeToLoadUsable: String? = null

        private val INVALID_TIME = -1L
        private var searchClickedMillis = INVALID_TIME

        var requestStartTime: Long? = null
        var responseReceivedTime: Long? = null
        var resultsProcessedTime: Long? = null
        var resultsUserActiveTime: Long? = null

        fun markSearchClicked(time: Long) {
            searchClickedMillis = time
        }

        fun markSearchApiCallMade(time: Long) {
            requestStartTime = time
        }

        fun markApiResponseReceived(time: Long) {
            responseReceivedTime = time
        }

        fun markResultsProcessed(time: Long) {
            resultsProcessedTime = time
        }

        fun markResultsUsable(time: Long) {
            setTimeToLoadUsable(time)
            resultsUserActiveTime = time
        }

        private fun setTimeToLoadUsable(resultsUserActiveMillis: Long) {
            timeToLoadUsable = null
            if (searchClickedMillis != INVALID_TIME && resultsUserActiveMillis != INVALID_TIME) {
                val loadingTimeMillis = resultsUserActiveMillis - searchClickedMillis
                val loadingTimeSecs: Float = loadingTimeMillis / 1000f
                timeToLoadUsable = String.format("%.2f",loadingTimeSecs)
            }
        }
    }
}