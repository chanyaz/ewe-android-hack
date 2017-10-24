package com.expedia.bookings.utils

import com.expedia.bookings.data.LineOfBusiness
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat


class DeeplinkCreatorUtils() {

    companion object {
        var hotelSearchParams: HotelSearchParams? = null
        var hotelSelectionParams: HotelSelectionParams? = null
        var hotelRoomSelectionParams: HotelRoomSelectionParams? = null

        val PACKAGES_DEEPLINK_SUFFIX = "expda:\\replayPackages?"

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

class HotelSearchParams {
    lateinit var origin: String
    lateinit var destination: String
    lateinit var startDate: LocalDate
    lateinit var endDate: LocalDate
    lateinit var originID: String
    lateinit var destinationID: String
    lateinit var originAirportCode: String
    lateinit var destinationAirportCode: String
}

class HotelSelectionParams {
    lateinit var selectedHotelID: String
}
