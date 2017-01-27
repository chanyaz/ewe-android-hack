package com.expedia.bookings.tracking.hotel

import android.os.Build
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.tracking.AbstractSearchTrackingData
import com.expedia.bookings.utils.ClientLogConstants

class ClientLogTracker(val clientLogServices: ClientLogServices) {
    fun trackResultsPerformance(performanceData: AbstractSearchTrackingData.PerformanceData, pageName: String, eventName: String) {
        val clientLogBuilder: ClientLog.ResultBuilder = ClientLog.ResultBuilder()
        clientLogBuilder.requestTime(performanceData.requestStartTime)
        clientLogBuilder.responseTime(performanceData.responseReceivedTime)
        clientLogBuilder.processingTime(performanceData.resultsProcessedTime)
        clientLogBuilder.requestToUser(performanceData.resultsUserActiveTime)

        clientLogBuilder.eventName(eventName)
        clientLogBuilder.pageName(pageName)
        clientLogBuilder.deviceName(Build.MODEL)
        clientLogServices.log(clientLogBuilder.build())
    }
}