package com.expedia.bookings.data.trips

import com.expedia.bookings.data.rail.responses.RailProduct
import org.joda.time.DateTime

class ItinCardDataRails(tripRails: TripRails): ItinCardData(tripRails) {

    override fun hasDetailData(): Boolean {
        return true
    }

}