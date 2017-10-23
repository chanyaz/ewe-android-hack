package com.expedia.bookings.data.flights

class BaggageInfoResponse {
    lateinit var airlineName: String
    lateinit var charges: ArrayList<HashMap<String, String>>
}
