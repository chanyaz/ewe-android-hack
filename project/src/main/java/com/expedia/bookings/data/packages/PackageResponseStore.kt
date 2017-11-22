package com.expedia.bookings.data.packages

import android.util.Pair

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse

object PackageResponseStore {
    var packageHotelResponse: BundleSearchResponse? = null
    var packageHotelRoomResponse: HotelOffersResponse? = null
    var packageOutboundFlightResponse: BundleSearchResponse? = null
    var packageInboundFlightResponse: BundleSearchResponse? = null

    var packageParams: PackageSearchParams? = null
    var packageResponse: BundleSearchResponse? = null
    var packageSelectedHotel: Hotel? = null
        private set
    var packageSelectedRoom: HotelOffersResponse.HotelRoomResponse? = null
        private set
    var packageSelectedOutboundFlight: FlightLeg? = null

    //Package outbound and inbound flight pair
    //Save inbound flight in this pair, to avoid stale inbound info if outbound is changed
    var packageFlightBundle: Pair<FlightLeg, FlightLeg>? = null
        private set

    fun setPackageSelectedHotel(packageSelectedHotel: Hotel, packageSelectedRoom: HotelOffersResponse.HotelRoomResponse) {
        this.packageSelectedHotel = packageSelectedHotel
        this.packageSelectedRoom = packageSelectedRoom
    }

    fun clearPackageHotelSelection() {
        packageSelectedHotel = null
        packageSelectedRoom = null
    }

    fun clearPackageHotelRoomSelection() {
        if (packageSelectedRoom != null) {
            packageSelectedRoom!!.ratePlanCode = null
            packageSelectedRoom!!.roomTypeCode = null
        }
    }

    fun clearPackageFlightSelection() {
        packageSelectedOutboundFlight = null
        packageFlightBundle = null
    }

    fun clearPackageSelection() {
        clearPackageHotelSelection()
        clearPackageFlightSelection()
    }

    fun setPackageFlightBundle(outbound: FlightLeg, inbound: FlightLeg) {
        packageFlightBundle = Pair(outbound, inbound)
    }
}
