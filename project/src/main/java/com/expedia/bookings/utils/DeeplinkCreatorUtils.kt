package com.expedia.bookings.utils

import com.expedia.bookings.data.LineOfBusiness
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat


class DeeplinkCreatorUtils() {

    companion object {
        var hotelSearchParams: HotelSearchParams? = null
        var hotelSelectionParams: HotelSelectionParams? = null
        var hotelRoomSelectionParams: HotelRoomSelectionParams? = null

        var flightSearchParams: FlightSearchParams? = null
        var flightInboundParams: List<FlightInboundParams>? = null
        var flightOutboundParams: List<FlightOutboundParams>? = null


        val PACKAGES_DEEPLINK_SUFFIX = "expda:\\replayPackages?"

        val FLIGHT_DEEPLINK_SUFFIX = "expa:\\replayFlights?"

        val DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy")


        fun generateDeeplinkForCurrentPath(lob: LineOfBusiness): String {
            var deeplinkURL = ""
            when (lob) {
                LineOfBusiness.PACKAGES -> {
                    deeplinkURL += PACKAGES_DEEPLINK_SUFFIX
                    if (hotelSearchParams != null) {
                        val searchParams = hotelSearchParams!!
                        deeplinkURL += "origin=" + searchParams.origin + "&originID=" + searchParams.originID + "&destination=" + searchParams.destination +
                                "&destinationID=" + searchParams.destinationID + "&startDate=" + DATE_FORMATTER.print(searchParams.startDate) +
                                "&endDate=" + DATE_FORMATTER.print(searchParams.endDate) + "&originAirportCode=" + searchParams.originAirportCode +
                                "&destinationAirportCode=" + searchParams.destinationAirportCode
                    }
                    if (hotelSelectionParams !=null){
                        deeplinkURL+= "&hotelID=" + hotelSelectionParams!!.selectedHotelID
                    }
                    if (hotelRoomSelectionParams!=null){
                        deeplinkURL+= "&hotelRoomTypeCode=" + hotelRoomSelectionParams!!.selectedRoomTypeCode
                    }
                }
                LineOfBusiness.FLIGHTS -> {
                    deeplinkURL += FLIGHT_DEEPLINK_SUFFIX
                    if (flightSearchParams != null) {
                        val searchParams = flightSearchParams!!
                        deeplinkURL += "origin=" + searchParams.origin + "&destination=" + searchParams.destination + "&startDate=" + DATE_FORMATTER.print(searchParams.startDate) +
                                "&endDate=" + DATE_FORMATTER.print(searchParams.endDate) + "&cabin=" + searchParams.cabinClass + "&traveler=" + searchParams.noOfTravelers
                    }
                    if (flightOutboundParams != null) {
                        deeplinkURL += "&outboundCount=" + flightOutboundParams!!.size
                        var count = 0
                        flightOutboundParams!!.forEach { it ->
                            deeplinkURL += "&outbound_flight_number_" +count +"=" +it.flightNumber +"&outbound_airlineCode_" +count +"=" +it.airlineCode
                            count++
                        }
                    }

                    if (flightInboundParams != null) {
                        deeplinkURL += "&inboundCount=" + flightInboundParams!!.size
                        var count = 0
                        flightInboundParams!!.forEach { it ->
                            deeplinkURL += "&inbound_flight_number_" +count +"=" +it.flightNumber +"&inbound_airlineCode_" +count +"=" +it.airlineCode
                        }
                    }

                }
                LineOfBusiness.HOTELS -> {

                }
            }
            return deeplinkURL
        }
    }


}

class HotelRoomSelectionParams {
    lateinit var selectedRoomTypeCode: String
}

open class HotelSearchParams {
    lateinit var origin: String
    lateinit var destination: String
    lateinit var startDate: LocalDate
    lateinit var endDate: LocalDate
    open lateinit var originID: String
    lateinit var destinationID: String
    lateinit var originAirportCode: String
    lateinit var destinationAirportCode: String
}

class HotelSelectionParams {
    lateinit var selectedHotelID: String
}

class FlightSearchParams{
    lateinit var origin: String
    lateinit var destination: String
    lateinit var startDate: LocalDate
    lateinit var endDate: LocalDate
    lateinit var cabinClass: String
    lateinit var noOfTravelers: String
}

class FlightInboundParams {
    lateinit var flightNumber: String
    lateinit var airlineCode: String
}

class FlightOutboundParams {
    lateinit var flightNumber: String
    lateinit var airlineCode: String
}
