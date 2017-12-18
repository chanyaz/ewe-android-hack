package com.expedia.bookings.tracking

open class TimeLogger(private val timeSource: TimeSource = TimeSourceInMillis(), val pageName: String) {
    var startTime: Long? = null
    var endTime: Long? = null

    fun setStartTime() {
        startTime = timeSource.now()
    }

    fun setEndTime() {
        endTime = timeSource.now()
    }

    open fun clear() {
        startTime = null
        endTime = null
    }


    fun isComplete(): Boolean = startTime != null && endTime != null
    fun calculateTotalTime(): Long = (endTime ?: -1L).minus(startTime ?: -1L)
}

class TimeSourceInMillis : TimeSource {
    override fun now(): Long {
        return System.currentTimeMillis()
    }
}
