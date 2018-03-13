package com.expedia.bookings.itin.common

import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.hotel.PageUsableData

open class ItinPageUsableTracking : PageUsableData() {

    open fun markSuccessfulStartTime(time: Long) {
        markPageLoadStarted(time)
    }

    fun markTripResultsUsable(time: Long) {
        markAllViewsLoaded(time)
    }

    fun hasStartTime(): Boolean = pageLoadStartedMillis != INVALID_TIME

    fun resetStartTime() {
        pageLoadStartedMillis = INVALID_TIME
    }

    fun trackIfReady(itinData: List<ItinCardData>) {
        if (hasStartTime() && hasEndTime() && itinData.isNotEmpty()) {
            OmnitureTracking.trackItin(this)
        }
    }

    private fun hasEndTime(): Boolean = viewsUsableTimeMillis != INVALID_TIME
}
