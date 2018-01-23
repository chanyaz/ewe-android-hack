package com.expedia.bookings.data.trips

import com.expedia.bookings.data.rail.responses.RailProduct
import java.util.ArrayList

class TripRails : TripComponent(Type.RAILS) {
    val railProducts = ArrayList<RailProduct>()
}
