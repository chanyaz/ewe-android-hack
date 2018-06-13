package com.expedia.bookings.utils

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse

object PackageResponseUtils {

    var recentPackageHotelsResponse: BundleSearchResponse? = null
    var recentPackageHotelOffersResponse: HotelOffersResponse? = null
    var recentPackageOutboundFlightsResponse: BundleSearchResponse? = null
    var recentPackageInboundFlightsResponse: BundleSearchResponse? = null
}
