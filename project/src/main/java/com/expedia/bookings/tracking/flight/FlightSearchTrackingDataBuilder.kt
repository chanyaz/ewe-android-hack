package com.expedia.bookings.tracking.flight

import android.text.TextUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.LocalDate

class FlightSearchTrackingDataBuilder {
    private val trackingData = FlightSearchTrackingData()

    private var paramsPopulated = false
    private var responsePopulated = false
    private var responseTimePopulated = false

    fun searchParams() {
       // populateSearchParamFields(searchParams)
        paramsPopulated = true
    }

    fun searchResponse() {
        //populateSearchResponseFields(searchResponse)
        responsePopulated = true
    }

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

    fun isWorkComplete() : Boolean {
        return paramsPopulated && responsePopulated && responseTimePopulated
    }

    fun build() : FlightSearchTrackingData {
        paramsPopulated = false
        responsePopulated = false
        responseTimePopulated = false
        return trackingData
    }

//    private fun populateSearchParamFields(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams) {
//        populateRegionData(searchParams)
//
//        trackingData.searchWindowDays = Integer.toString(JodaUtils.daysBetween(LocalDate.now(), searchParams.checkIn))
//
//        trackingData.numberOfAdults = searchParams.adults
//        trackingData.numberOfChildren = searchParams.children.size
//        trackingData.numberOfGuests = searchParams.guests
//
//        trackingData.checkInDate = searchParams.checkIn
//        trackingData.checkoutDate = searchParams.checkOut
//
//        val now = LocalDate.now()
//        trackingData.daysOut = JodaUtils.daysBetween(now, trackingData.checkInDate)
//
//        if (trackingData.checkoutDate != null) {
//            trackingData.duration = JodaUtils.daysBetween(trackingData.checkInDate, trackingData.checkoutDate)
//        } else {
//            trackingData.duration = 0
//        }
//    }

//    private fun populateSearchResponseFields(searchResponse: com.expedia.bookings.data.hotels.HotelSearchResponse) {
//        if (searchResponse != null && !searchResponse.hotelList.isEmpty()) {
//            trackingData.resultsReturned = true
//            trackingData.numberOfResults = Integer.toString(searchResponse.hotelList.size)
//            trackingData.hasSponsoredListingPresent = searchResponse.hotelList[0].isSponsoredListing
//
//            trackingData.city = searchResponse.hotelList[0].city
//            trackingData.stateProvinceCode = searchResponse.hotelList[0].stateProvinceCode
//            trackingData.countryCode = searchResponse.hotelList[0].countryCode
//            trackingData.searchRegionId = searchResponse.searchRegionId
//
//            val hotelList: List<Hotel> = searchResponse.hotelList
//            trackingData.hotels = hotelList
//            trackingData.lowestHotelTotalPrice = calculateLowestTotalPrice(hotelList)
//        }
//    }

//    private fun populateRegionData(params: com.expedia.bookings.data.hotels.HotelSearchParams) {
//        if (params.suggestion.isCurrentLocationSearch) {
//            trackingData.region = "Current Location"
//        } else {
//            trackingData.region = params.suggestion.gaiaId
//        }
//
//        if (!TextUtils.isEmpty(params.suggestion.regionNames.fullName)) {
//            trackingData.freeFormRegion = params.suggestion.regionNames.fullName
//        }
//    }
}