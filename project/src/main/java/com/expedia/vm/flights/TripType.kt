package com.expedia.vm.flights

import com.expedia.bookings.data.flights.FlightLeg
import java.io.Serializable

sealed class TripType() : Serializable{
    class OneWay(val results: List<FlightLeg>) : TripType()
    class RoundTrip(val results: List<FlightLeg>) : TripType()
    class MultiDest(val results: List<List<FlightLeg>>) : TripType()
}
