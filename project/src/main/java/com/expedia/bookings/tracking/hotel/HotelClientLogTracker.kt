package com.expedia.bookings.tracking.hotel

import android.os.Build
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.tracking.AbstractSearchTrackingData.PerformanceData
import com.expedia.bookings.utils.ClientLogConstants

class HotelClientLogTracker(val clientLogServices: ClientLogServices) {
    fun trackResultsPerformance(trackingData: PerformanceData) {
        val clientLogBuilder: ClientLog.HotelResultBuilder = ClientLog.HotelResultBuilder()
        clientLogBuilder.requestTime(trackingData.requestStartTime)
        clientLogBuilder.responseTime(trackingData.responseReceivedTime)
        clientLogBuilder.processingTime(trackingData.resultsProcessedTime)
        clientLogBuilder.requestToUser(trackingData.resultsUserActiveTime)

        val userBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultsPerceivedInstantTest)
        clientLogBuilder.eventName(if (userBucketedForTest) ClientLogConstants.PERCEIVED_INSTANT_SEARCH_RESULTS else ClientLogConstants.REGULAR_SEARCH_RESULTS)
        clientLogBuilder.pageName(ClientLogConstants.MATERIAL_HOTEL_SEARCH_PAGE)
        clientLogBuilder.deviceName(Build.MODEL)
        clientLogServices.log(clientLogBuilder.build())
    }
}