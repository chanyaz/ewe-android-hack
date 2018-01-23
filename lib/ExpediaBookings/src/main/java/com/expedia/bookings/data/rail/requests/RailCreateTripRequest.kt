package com.expedia.bookings.data.rail.requests

import com.expedia.bookings.utils.Constants

// variable names in this model have to match 1:1 to the format the api expects
class RailCreateTripRequest(val offerTokens: List<String>) {
    val pos = PointOfSaleKey()
    val locale = "en_GB"
    val clientCode = Constants.RAIL_CLIENT_CODE
    var messageInfo: MessageInfo? = null
}
