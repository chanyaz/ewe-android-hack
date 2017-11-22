package com.expedia.bookings.utils

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse

object PackageResponseStore {

    var packageHotelResponse: BundleSearchResponse? = null
    var packageHotelRoomResponse: HotelOffersResponse? = null
    var packageOutboundFlightResponse: BundleSearchResponse? = null
    var packageInboundFlightResponse: BundleSearchResponse? = null
}
