package com.expedia.bookings.data.trips

class ItinCardDataRails(tripRails: TripRails) : ItinCardData(tripRails) {

    override fun hasDetailData(): Boolean {
        return true
    }
}
