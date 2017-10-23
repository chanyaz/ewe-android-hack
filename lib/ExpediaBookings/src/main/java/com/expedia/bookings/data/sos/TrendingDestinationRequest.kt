package com.expedia.bookings.data.sos

import com.expedia.bookings.utils.Constants

class TrendingDestinationRequest {

    var siteId = Constants.MOD_SITE_ID
    var locale = "en_US"
    var productType = Constants.MOD_PRODUCT_TYPE
    var groupBy = Constants.MOD_GROUP_BY
    var destinationLimit = Constants.MOD_DESTINATION_LIMIT
    var clientId = Constants.MOD_CLIENT_ID
}
