package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinFlight
import com.expedia.bookings.itin.tripstore.data.ItinLeg

fun Itin.getLegs(): List<ItinLeg> {
    val retList = mutableListOf<ItinLeg>()
    val packageFlights = packages?.firstOrNull()?.flights.orEmpty()
    val standAloneFlights = flights.orEmpty()
    fetchLegs(packageFlights, retList)
    fetchLegs(standAloneFlights, retList)
    return retList
}

private fun fetchLegs(flightList: List<ItinFlight>, retList: MutableList<ItinLeg>) {
    flightList.forEach { flight ->
        flight.legs?.forEach { leg ->
            retList.add(leg)
        }
    }
}
