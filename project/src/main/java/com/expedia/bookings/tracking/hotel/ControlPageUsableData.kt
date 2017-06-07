package com.expedia.bookings.tracking.hotel

class ControlPageUsableData: PageUsableData() {
    private var isTimerAborted = false

    fun hasTimerStarted() = (pageLoadStartedMillis != INVALID_TIME)

    fun abortTimer() {
        isTimerAborted = true
        reset()
    }

    fun isTimerAborted() = isTimerAborted
}