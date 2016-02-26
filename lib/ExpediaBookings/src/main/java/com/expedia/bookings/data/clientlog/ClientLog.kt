package com.expedia.bookings.data.clientlog

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ClientLog(val pageName: String?, val requestTime: String?, val responseTime: String?, val processingTime: String?, val requestToUser: String?) {

    class Builder() {
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        var pageName: String? = null
        var requestTime: DateTime? = null
        var responseTime: DateTime? = null
        var processingTime: DateTime? = null
        var requestToUser: DateTime? = null

        fun pageName(event: String?): ClientLog.Builder {
            pageName = event
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
            return ClientLog(pageName, requestTime?.toString(dtf), responseTime?.toString(dtf), processingTime?.toString(dtf), requestToUser?.toString(dtf))
        }
    }
}
