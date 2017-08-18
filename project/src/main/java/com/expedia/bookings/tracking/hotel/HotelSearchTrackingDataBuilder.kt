package com.expedia.bookings.tracking.hotel

import android.text.TextUtils
import com.expedia.bookings.data.hotels.Hotel
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

    fun searchParams(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams) {
        populateSearchParamFields(searchParams)
        paramsPopulated = true
    }

    fun searchResponse(searchResponse: com.expedia.bookings.data.hotels.HotelSearchResponse) {
        populateSearchResponseFields(searchResponse)
        responsePopulated = true
    }

    private fun populateSearchParamFields(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams) {
        populateRegionData(searchParams)

        trackingData.searchWindowDays = Integer.toString(JodaUtils.daysBetween(LocalDate.now(), searchParams.checkIn))

        trackingData.numberOfAdults = searchParams.adults
        trackingData.numberOfChildren = searchParams.children.size
        trackingData.numberOfGuests = searchParams.guests

        trackingData.checkInDate = searchParams.checkIn
        trackingData.checkoutDate = searchParams.checkOut

        val now = LocalDate.now()
        trackingData.daysOut = JodaUtils.daysBetween(now, trackingData.checkInDate)

        if (trackingData.checkoutDate != null) {
            trackingData.duration = JodaUtils.daysBetween(trackingData.checkInDate, trackingData.checkoutDate)
        } else {
            trackingData.duration = 0
        }

        trackingData.swpEnabled = searchParams.shopWithPoints
    }

    private fun populateSearchResponseFields(searchResponse: com.expedia.bookings.data.hotels.HotelSearchResponse) {
        if (searchResponse != null && !searchResponse.hotelList.isEmpty()) {
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
        }
    }

    private fun populateRegionData(params: com.expedia.bookings.data.hotels.HotelSearchParams) {
        if (params.suggestion.isCurrentLocationSearch) {
            trackingData.region = "Current Location"
        } else {
            trackingData.region = params.suggestion.gaiaId
        }

        if (!TextUtils.isEmpty(params.suggestion.regionNames.fullName)) {
            trackingData.freeFormRegion = params.suggestion.regionNames.fullName
        }
    }

    private fun calculateLowestTotalPrice(properties: List<Hotel>): String? {
        if (properties.isEmpty()) return null

        var minPropertyRate = properties[0].lowRateInfo

        for (property in properties) {
            var propertyRate = property.lowRateInfo
            if (propertyRate == null || minPropertyRate == null)
                continue
            else if (propertyRate.displayTotalPrice.getAmount() < minPropertyRate.displayTotalPrice.getAmount()) {
                minPropertyRate = propertyRate
            }
        }
        return minPropertyRate?.displayTotalPrice?.getAmount()?.toString()
    }
}