package com.expedia.bookings.tracking

abstract class AbstractTrackingDataBuilder<T : AbstractSearchTrackingData> {
    protected abstract var trackingData: T

    protected var paramsPopulated = false
    protected var responsePopulated = false
    protected var responseTimePopulated = false

    fun markSearchClicked() {
        trackingData.performanceData.markSearchClicked(System.currentTimeMillis())
    }

    fun markSearchApiCallMade() {
        trackingData.performanceData.markSearchApiCallMade(System.currentTimeMillis())
    }

    fun markApiResponseReceived() {
        trackingData.performanceData.markApiResponseReceived(System.currentTimeMillis())
    }

    fun markResultsProcessed() {
        trackingData.performanceData.markResultsProcessed(System.currentTimeMillis())
    }

    fun markResultsUsable() {
        trackingData.performanceData.markResultsUsable(System.currentTimeMillis())
        responseTimePopulated = true
    }

    fun isWorkComplete(): Boolean {
        return paramsPopulated && responsePopulated && responseTimePopulated
    }

    abstract fun build(): AbstractSearchTrackingData
}
