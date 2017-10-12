package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.tracking.AbstractTrackingDataBuilder
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.LocalDate

class HotelSearchTrackingDataBuilder : AbstractTrackingDataBuilder<HotelSearchTrackingData>() {
    override var trackingData = HotelSearchTrackingData()

    override fun build(): HotelSearchTrackingData {
        paramsPopulated = false
        responsePopulated = false
        responseTimePopulated = false
        return trackingData
    }

    fun searchParams(searchParams: HotelSearchParams) {
        populateSearchParamFields(searchParams)
        paramsPopulated = true
    }

    fun searchResponse(searchResponse: HotelSearchResponse) {
        populateSearchResponseFields(searchResponse)
        responsePopulated = true
    }

    private fun populateSearchParamFields(searchParams: HotelSearchParams) {
        populateRegionData(searchParams)

        trackingData.searchWindowDays = Integer.toString(JodaUtils.daysBetween(LocalDate.now(), searchParams.checkIn))

        trackingData.numberOfAdults = searchParams.adults
        trackingData.numberOfChildren = searchParams.children.size
        trackingData.numberOfGuests = searchParams.guests

        trackingData.checkInDate = searchParams.checkIn
        trackingData.checkoutDate = searchParams.checkOut

        if (trackingData.checkoutDate != null) {
            trackingData.duration = JodaUtils.daysBetween(trackingData.checkInDate, trackingData.checkoutDate)
        } else {
            trackingData.duration = 0
        }

        trackingData.swpEnabled = searchParams.shopWithPoints
    }

    private fun populateSearchResponseFields(searchResponse: HotelSearchResponse) {
        if (!searchResponse.hotelList.isEmpty()) {
            trackingData.resultsReturned = true
            trackingData.numberOfResults = Integer.toString(searchResponse.hotelList.size)
            trackingData.hasSponsoredListingPresent = searchResponse.hotelList[0].isSponsoredListing

            trackingData.city = searchResponse.hotelList[0].city
            trackingData.stateProvinceCode = searchResponse.hotelList[0].stateProvinceCode
            trackingData.countryCode = searchResponse.hotelList[0].countryCode
            trackingData.searchRegionId = searchResponse.searchRegionId

            val hotelList: List<Hotel> = searchResponse.hotelList
            trackingData.hotels = hotelList
            trackingData.lowestHotelTotalPrice = calculateLowestTotalPrice(hotelList)

            trackingData.hasPinnedHotel = searchResponse.hasPinnedHotel()
            trackingData.pinnedHotelSoldOut = trackingData.hasPinnedHotel && searchResponse.hotelList[0].isSoldOut

            trackingData.hasSoldOutHotel = haveSoldOutProperties(hotelList)
        }
    }

    private fun populateRegionData(params: HotelSearchParams) {
        if (params.suggestion.isCurrentLocationSearch) {
            trackingData.region = "Current Location"
        } else {
            trackingData.region = params.suggestion.gaiaId
        }

        if (!params.suggestion.regionNames.fullName.isNullOrBlank()) {
            trackingData.freeFormRegion = params.suggestion.regionNames.fullName
        }
    }

    private fun calculateLowestTotalPrice(properties: List<Hotel>): String? {
        if (properties.isEmpty()) return null

        var minPropertyRate = properties[0].lowRateInfo

        for (property in properties) {
            val propertyRate = property.lowRateInfo
            if (propertyRate == null || minPropertyRate == null)
                continue
            else if (propertyRate.displayTotalPrice.getAmount() < minPropertyRate.displayTotalPrice.getAmount()) {
                minPropertyRate = propertyRate
            }
        }
        return minPropertyRate?.displayTotalPrice?.getAmount()?.toString()
    }

    private fun haveSoldOutProperties(properties: List<Hotel>): Boolean {
        for (property in properties.asReversed()) {
            if (property.isSoldOut) {
                return true
            }
        }
        return false
    }
}
