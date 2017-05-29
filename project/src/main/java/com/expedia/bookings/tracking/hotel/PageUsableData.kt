package com.expedia.bookings.tracking.hotel

open class PageUsableData {
    protected val INVALID_TIME = -1L
    protected var pageLoadStartedMillis: Long = INVALID_TIME
    protected var viewsUsableTimeMillis: Long = INVALID_TIME

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

    protected fun reset() {
        pageLoadStartedMillis = INVALID_TIME
        viewsUsableTimeMillis = INVALID_TIME
    }
}