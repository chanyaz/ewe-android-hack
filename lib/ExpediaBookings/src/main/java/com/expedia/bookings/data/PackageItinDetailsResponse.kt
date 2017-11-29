package com.expedia.bookings.data

class PackageItinDetailsResponse: AbstractItinDetailsResponse() {

    lateinit var responseData: PackageResponseData

    class PackageResponseData : ResponseData() {
        var packages = emptyList<Package>()
    }

    class Package {
        var flights = emptyList<FlightItinDetailsResponse.Flight>()
        var hotels = emptyList<HotelItinDetailsResponse.Hotels>()
    }

    override fun getResponseDataForItin(): ResponseData? {
        return responseData
    }
}
