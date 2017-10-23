package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.data.sos.MemberDealDestination
import com.expedia.bookings.data.sos.TrendingDestination
import com.expedia.bookings.data.sos.TrendingLocation

class TrendingDestinationsViewModel(val context: Context, val destination: TrendingLocation) {

    val backgroundUrl: String? = destination.imageurl
    val cityName: String? = destination.title
    val countryName: String? = destination.subtitle
    val regionId: String? = destination.regionid
    val rank: Integer? = destination.rank

}
