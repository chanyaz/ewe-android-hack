package com.expedia.bookings.data.sos

import com.expedia.bookings.utils.Constants

open class DealsRequest {
    var siteId = Constants.SOS_SITE_ID
    var locale = Constants.SOS_DEFAULT_LOCALE
    var productType = Constants.SOS_PRODUCT_TYPE
    var groupBy = Constants.SOS_GROUP_BY
    var destinationLimit = Constants.SOS_DESTINATION_LIMIT
    var clientId = Constants.SOS_CLIENT_ID
}
