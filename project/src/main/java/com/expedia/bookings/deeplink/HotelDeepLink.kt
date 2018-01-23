package com.expedia.bookings.deeplink

import com.expedia.bookings.data.ChildTraveler
import org.joda.time.LocalDate

class HotelDeepLink : DeepLink() {
    var location: String? = null
    var hotelId: String? = null
    var regionId: String? = null
    var mctc: Int? = null
    var checkInDate: LocalDate? = null
    var checkOutDate: LocalDate? = null
    var numAdults: Int = 0
    var children: List<ChildTraveler>? = null
    var sortType: String? = null
    var memberOnlyDealSearch: Boolean = false
    var selectedHotelId: String? = null
}
