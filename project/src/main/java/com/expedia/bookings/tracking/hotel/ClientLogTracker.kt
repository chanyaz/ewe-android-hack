package com.expedia.bookings.tracking.hotel

import android.os.Build
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.clientlog.ThumborClientLog
import com.expedia.bookings.services.IClientLogServices
import com.expedia.bookings.tracking.AbstractSearchTrackingData
import javax.inject.Inject

class ClientLogTracker @Inject constructor(private val clientLogServices: IClientLogServices) {
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

    fun trackThumborClientLog(pageName: String, imageBytesDownloaded: Long, numberOfImages: Int, bucket: Int) {
        val thumborClientLog = ThumborClientLog(pageName, imageBytesDownloaded, numberOfImages, bucket)
        clientLogServices.thumborClientLog(thumborClientLog)
    }
}
