package com.expedia.bookings.data.clientlog

import com.expedia.bookings.utils.ClientLogConstants

class ClientLog(val pageName: String, val eventName: String, val deviceName: String, val requestTime: Long, val responseTime: Long = 0L, val processingTime: Long = 0L, val requestToUser: Long = 0L, val deviceType: String = ClientLogConstants.DEVICE_TYPE) {

    class HotelResultBuilder {
        private var pageName: String? = null
        private var eventName: String? = null
        private var deviceName: String? = null
        private var requestTime: Long? = null
        private var responseTime: Long? = null
        private var processingTime: Long? = null
        private var requestToUser: Long? = null

        fun pageName(page: String?): ClientLog.HotelResultBuilder {
            pageName = page
            return this
        }

        fun eventName(event: String?): ClientLog.HotelResultBuilder {
            eventName = event
            return this
        }

        fun deviceName(device: String?): ClientLog.HotelResultBuilder {
            deviceName = device
            return this
        }

        fun requestTime(time: Long?): ClientLog.HotelResultBuilder {
            requestTime = time
            return this
        }

        fun responseTime(time: Long?): ClientLog.HotelResultBuilder {
            responseTime = time
            return this
        }

        fun processingTime(time: Long?): ClientLog.HotelResultBuilder {
            processingTime = time
            return this
        }

        fun requestToUser(time: Long?): ClientLog.HotelResultBuilder {
            requestToUser = time
            return this
        }

        fun build(): ClientLog {
            return ClientLog(pageName!!, eventName!!, deviceName!!, 0L, calculateTimeDiff(responseTime), calculateTimeDiff(processingTime), calculateTimeDiff(requestToUser))
        }

        private fun calculateTimeDiff(time: Long?): Long {
            val startTime = requestTime
            if (time == null || startTime == null) {
                return 0
            }
            return time - startTime
        }
    }

    class ResponseCLBuilder() {
        private var pageName: String? = null
        private var eventName: String? = null
        private var deviceName: String? = null
        private var responseTime: Long? = null

        fun pageName(page: String?): ClientLog.ResponseCLBuilder {
            pageName = page
            return this
        }

        fun eventName(event: String?): ClientLog.ResponseCLBuilder {
            eventName = event
            return this
        }

        fun deviceName(device: String?): ClientLog.ResponseCLBuilder {
            deviceName = device
            return this
        }

        fun responseTime(time: Long?): ClientLog.ResponseCLBuilder {
            responseTime = time
            return this
        }

        fun build(): ClientLog {
            return ClientLog(pageName!!, eventName!!, deviceName!!, 0L, responseTime!!)
        }

    }
}
