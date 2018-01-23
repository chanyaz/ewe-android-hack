package com.expedia.bookings.data.clientlog

import com.expedia.bookings.utils.ClientLogConstants

class ClientLog(val pageName: String?, val eventName: String?, val deviceName: String?, val requestTime: Long?, val responseTime: Long?, val processingTime: Long?, val requestToUser: Long?, val deviceType: String = ClientLogConstants.DEVICE_TYPE) {

    class ResultBuilder {
        private var pageName: String? = null
        private var eventName: String? = null
        private var deviceName: String? = null
        private var requestTime: Long? = null
        private var responseTime: Long? = null
        private var processingTime: Long? = null
        private var requestToUser: Long? = null

        fun pageName(page: String?): ClientLog.ResultBuilder {
            pageName = page
            return this
        }

        fun eventName(event: String?): ClientLog.ResultBuilder {
            eventName = event
            return this
        }

        fun deviceName(device: String?): ClientLog.ResultBuilder {
            deviceName = device
            return this
        }

        fun requestTime(time: Long?): ClientLog.ResultBuilder {
            requestTime = time
            return this
        }

        fun responseTime(time: Long?): ClientLog.ResultBuilder {
            responseTime = time
            return this
        }

        fun processingTime(time: Long?): ClientLog.ResultBuilder {
            processingTime = time
            return this
        }

        fun requestToUser(time: Long?): ClientLog.ResultBuilder {
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
            return ClientLog(pageName, eventName, deviceName, null, responseTime, null, null)
        }
    }

    class AppStartupTimeBuilder() {
        private var requestToUser: Long? = null
        private var pageName: String? = null

        fun pageName(page: String?): ClientLog.AppStartupTimeBuilder {
            pageName = page
            return this
        }

        fun requestToUser(time: Long?): ClientLog.AppStartupTimeBuilder {
            requestToUser = time
            return this
        }

        fun build(): ClientLog {
            return ClientLog(pageName, null, null, null, null, null, requestToUser)
        }
    }
}
