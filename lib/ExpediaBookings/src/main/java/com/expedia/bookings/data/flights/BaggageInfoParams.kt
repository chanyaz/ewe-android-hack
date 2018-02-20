package com.expedia.bookings.data.flights

import com.expedia.bookings.utils.ApiDateUtils
import com.expedia.bookings.utils.FlightClassType

class BaggageInfoParams {

    lateinit var baggageParams: ArrayList<HashMap<String, String>>
    val flightClassType = FlightClassType()

    fun makeBaggageParams(flightLeg: FlightLeg): ArrayList<HashMap<String, String>> {
        if (flightLeg.jsonBaggageFeesUrl != null) {
            flightLeg.jsonBaggageFeesUrl.formData.forEachIndexed { index, hashMap ->
                hashMap.put("traveldate", ApiDateUtils.toMMddyyyy(flightLeg.segments[index].departureTime))
            }
            return flightLeg.jsonBaggageFeesUrl.formData
        } else {
            baggageParams = ArrayList<HashMap<String, String>>()
            val flightSegment = flightLeg.segments
            for ((index, value) in flightSegment.withIndex()) {
                val baggageParam = HashMap<String, String>()
                baggageParam.put("originapt", value.arrivalAirportCode)
                baggageParam.put("destinationapt", value.departureAirportCode)
                baggageParam.put("cabinclass", flightClassType.getCabinClass(value.seatClass))
                baggageParam.put("mktgcarrier", value.airlineCode)
                baggageParam.put("opcarrier", value.operatingAirlineCode)
                baggageParam.put("bookingclass", value.bookingCode)
                baggageParam.put("traveldate", ApiDateUtils.toMMddyyyy(value.departureTime))
                baggageParam.put("flightnumber", value.flightNumber)
                baggageParam.put("segmentnumber", (index + 1).toString())
                baggageParams.add(baggageParam)
            }
            return baggageParams
        }
    }
}
