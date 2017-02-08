package com.expedia.bookings.deeplink

import com.expedia.bookings.data.ChildTraveler
import com.expedia.vm.AbstractHotelFilterViewModel
import org.joda.time.LocalDate

class HotelDeepLink: DeepLink() {
    var location: String? = null
    var hotelId: String? = null
    var checkInDate: LocalDate? = null
    var checkOutDate: LocalDate? = null
    var numAdults: Int = 0
    var children: List<ChildTraveler>? = null
    var sortType: String? = null

}

