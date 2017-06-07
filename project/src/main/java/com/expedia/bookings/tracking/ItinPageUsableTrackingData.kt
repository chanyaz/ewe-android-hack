package com.expedia.bookings.tracking

import android.support.annotation.VisibleForTesting
import com.expedia.bookings.tracking.hotel.PageUsableData

open class ItinPageUsableTrackingData {
    val tripLoadPageUsableData = PageUsableData()
    private val INVALID_TIME = tripLoadPageUsableData.INVALID_TIME
    private var startTimeOnSuccessfulSignInResponse: Long? = INVALID_TIME

    @VisibleForTesting
    open fun markSuccessfulSignIn(time: Long) {
        tripLoadPageUsableData.markPageLoadStarted(time)
        startTimeOnSuccessfulSignInResponse = time
    }

    open fun markItinRefreshStartTime(time: Long) {
        tripLoadPageUsableData.markPageLoadStarted(time)
        startTimeOnSuccessfulSignInResponse = time
    }

    open fun markTripResultsUsable(time: Long) {
        tripLoadPageUsableData.markAllViewsLoaded(time)
    }

    open fun hasStartTime(): Boolean {
        return (startTimeOnSuccessfulSignInResponse != INVALID_TIME)
    }

    fun resetStartTime() {
        tripLoadPageUsableData.markPageLoadStarted(INVALID_TIME)
        startTimeOnSuccessfulSignInResponse = INVALID_TIME
    }
}