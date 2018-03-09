package com.expedia.bookings.data

abstract class BaseDealsRequest {

    companion object {
        private const val DEFAULT_SITE_ID = "1"
        private const val DEFAULT_LOCALE = "en_US"
        private const val CLIENT_ID = "ebad"
        private const val PRODUCT_TYPE = "Hotel"
        private const val GROUP_BY = "destination"
        private const val DESTINATION_LIMIT = 20
    }

    val productType = PRODUCT_TYPE
    val groupBy = GROUP_BY
    val destinationLimit = DESTINATION_LIMIT
    val clientId = CLIENT_ID

    var siteId = DEFAULT_SITE_ID
    var locale = DEFAULT_LOCALE
}
