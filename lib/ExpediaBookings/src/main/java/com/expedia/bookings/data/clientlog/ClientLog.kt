package com.expedia.bookings.data.clientlog

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ClientLog(val pageName: String, val eventName: String, val deviceName: String, val requestTime: Long, val responseTime: Long, val processingTime: Long, val requestToUser: Long) {

    class Builder() {
        private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'")
        private var pageName: String? = null
        private var eventName: String? = null
        private var deviceName: String? = null
        private var requestTime: DateTime? = null
        private var responseTime: DateTime? = null
        private var processingTime: DateTime? = null
        private var requestToUser: DateTime? = null

        fun pageName(page: String?): ClientLog.Builder {
            pageName = page
            return this
        }

        fun eventName(event: String?): ClientLog.Builder {
            eventName = event
            return this
        }

        fun deviceName(device: String?): ClientLog.Builder {
            deviceName = device
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
            return ClientLog(pageName!!, eventName!!, deviceName!!, 0L, calculateTimeDiff(responseTime), calculateTimeDiff(processingTime), calculateTimeDiff(requestToUser))
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
