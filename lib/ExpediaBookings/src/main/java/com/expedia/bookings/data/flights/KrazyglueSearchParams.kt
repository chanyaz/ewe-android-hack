package com.expedia.bookings.data.flights

import com.expedia.bookings.utils.Constants

class KrazyglueSearchParams(val destinationCode: String, val arrivalDateTime: String, val returnDateTime: String) {
    val apiKey = Constants.KRAZY_GLUE_API_KEY
    val baseUrl = Constants.KRAZY_GLUE_BASE_URL
}