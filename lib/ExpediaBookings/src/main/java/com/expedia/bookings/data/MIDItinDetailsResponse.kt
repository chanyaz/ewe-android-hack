package com.expedia.bookings.data

class MIDItinDetailsResponse: AbstractItinDetailsResponse() {
    lateinit var responseData: MIDResponseData

    class MIDResponseData: ResponseData() {
        var flights = emptyList<FlightItinDetailsResponse.Flight>()
        var hotels = emptyList<HotelItinDetailsResponse.Hotels>()
    }

    override fun getResponseDataForItin(): ResponseData? {
        return responseData
    }
}
