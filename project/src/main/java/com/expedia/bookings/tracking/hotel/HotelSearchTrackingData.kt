package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.tracking.AbstractSearchTrackingData
import org.joda.time.LocalDate

class HotelSearchTrackingData : AbstractSearchTrackingData(){
    var city: String? = null
    var stateProvinceCode: String? = null
    var countryCode: String? = null

    var region: String? = null
    var freeFormRegion: String? = null
    var searchRegionId: String? = null

    var checkInDate: LocalDate? = null
    var checkoutDate: LocalDate? = null

    var numberOfGuests = 0
    var numberOfAdults = 0
    var numberOfChildren = 0

    var searchWindowDays: String? = null
    var daysOut: Int? = null
    var duration: Int? = null

    var resultsReturned = false
    var numberOfResults: String? = null
    var hasSponsoredListingPresent = false
    var lowestHotelTotalPrice: String? = null
    var hotels: List<Hotel> = emptyList()

    var swpEnabled = false

    fun hasResponse() : Boolean {
        return resultsReturned
    }

}