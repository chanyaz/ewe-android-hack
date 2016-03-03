package com.expedia.bookings.data.clientlog

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ClientLog(val pageName: String?, val deviceName: String?, val logTime: String?, val requestTime: Long, val responseTime: Long, val processingTime: Long, val requestToUser: Long) {

    class Builder() {
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'")
        var pageName: String? = null
        var deviceName: String? = null
        var logTime: DateTime? = null
        var requestTime: DateTime? = null
        var responseTime: DateTime? = null
        var processingTime: DateTime? = null
        var requestToUser: DateTime? = null

        fun pageName(event: String?): ClientLog.Builder {
            pageName = event
            return this
        }

        fun deviceName(device: String?): ClientLog.Builder {
            deviceName = device
            return this
        }

        fun logTime(time: DateTime?): ClientLog.Builder {
            logTime = time
            return this
        }

        fun requestTime(time: DateTime?): ClientLog.Builder {
            requestTime = time
            return this
        }

        fun responseTime(time: DateTime?): ClientLog.Builder {
            responseTime = time
            return this
        }

        fun processingTime(time: DateTime?): ClientLog.Builder {
            processingTime = time
            return this
        }

        fun requestToUser(time: DateTime?): ClientLog.Builder {
            requestToUser = time
            return this
        }

        fun build(): ClientLog {
            return ClientLog(pageName, deviceName, logTime?.toString(dtf), 0L, calculateTimeDiff(responseTime), calculateTimeDiff(processingTime), calculateTimeDiff(requestToUser))
        }

        private fun calculateTimeDiff(time: DateTime?) : Long {
            val startTime = requestTime
            if (time == null || startTime == null) {
                return 0
            }
            return time.millis - startTime.millis
        }
    }
}
