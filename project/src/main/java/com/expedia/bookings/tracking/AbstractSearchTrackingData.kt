package com.expedia.bookings.tracking

import com.expedia.bookings.tracking.hotel.PageUsableData

open class AbstractSearchTrackingData {
    var performanceData = PerformanceData()

    open class PerformanceData {
        private val pageUsableData = PageUsableData()

        var requestStartTime: Long? = null
        var responseReceivedTime: Long? = null
        var resultsProcessedTime: Long? = null
        var resultsUserActiveTime: Long? = null

        fun markSearchClicked(time: Long) {
            pageUsableData.markPageLoadStarted(time)
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
            pageUsableData.markAllViewsLoaded(time)
            resultsUserActiveTime = time
        }

        open fun getPageLoadTime() : String? {
            return pageUsableData.getLoadTimeInSeconds()
        }
    }
}
