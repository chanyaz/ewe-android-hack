package com.expedia.bookings.tracking.hotel

class PageUsableData {
    val INVALID_TIME = -1L
    private var pageLoadStartedMillis: Long = INVALID_TIME
    private var viewsUsableTimeMillis: Long = INVALID_TIME

    fun getLoadTimeInSeconds(): String? {
        if (pageLoadStartedMillis != INVALID_TIME && viewsUsableTimeMillis != INVALID_TIME) {
            val loadTimeMillis = viewsUsableTimeMillis - pageLoadStartedMillis
            val loadTimeSecs: Float = loadTimeMillis / 1000f
            reset()
            return String.format("%.2f", loadTimeSecs)
        }
        return null
    }

    fun markPageLoadStarted(time: Long) {
        pageLoadStartedMillis = time
    }

    fun markAllViewsLoaded(time: Long) {
        viewsUsableTimeMillis = time
    }

    private fun reset() {
        pageLoadStartedMillis = INVALID_TIME
        viewsUsableTimeMillis = INVALID_TIME
    }
}