package com.expedia.bookings.data.trips

interface ItineraryManagerInterface {
    fun getItinCardDataFromItinId (id: String?): ItinCardData?
}
