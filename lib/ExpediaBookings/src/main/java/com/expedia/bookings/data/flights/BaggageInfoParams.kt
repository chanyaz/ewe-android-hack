package com.expedia.bookings.data.flights

import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightClassType

class BaggageInfoParams {

    lateinit var baggageParams: ArrayList<HashMap<String, String>>
    val flightClassType = FlightClassType()

    fun makeBaggageParams(flightLeg: FlightLeg): ArrayList<HashMap<String, String>> {
        baggageParams = ArrayList<HashMap<String, String>>()
        val flightSegment = flightLeg.flightSegments
        for ((index, value) in flightSegment.withIndex()) {
            val baggageParam = HashMap<String, String>()
            baggageParam.put("originapt", value.arrivalAirportCode)
            baggageParam.put("destinationapt", value.departureAirportCode)
            baggageParam.put("cabinclass", flightClassType.getCabinClass(value.seatClass))
            baggageParam.put("mktgcarrier", value.airlineCode)
            baggageParam.put("opcarrier", value.operatingAirlineCode)
            baggageParam.put("bookingclass", value.bookingCode)
            baggageParam.put("traveldate", DateUtils.toMMddyyyy(value.departureTime))
            baggageParam.put("flightnumber", value.flightNumber)
            baggageParam.put("segmentnumber", (index + 1).toString())
            baggageParams.add(baggageParam)
        }
        return baggageParams
    }
}