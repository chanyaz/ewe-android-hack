package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.KrazyglueResponse
import org.joda.time.LocalDate
import java.util.ArrayList

class FlightPresenterTestUtil {

    companion object {

        @JvmStatic fun getFlightSearchParams(isRoundTrip: Boolean): FlightSearchParams {
            val departureSuggestion = SuggestionV4()
            departureSuggestion.gaiaId = "1234"
            val departureRegionNames = SuggestionV4.RegionNames()
            departureRegionNames.displayName = "San Francisco"
            departureRegionNames.shortName = "SFO"
            departureRegionNames.fullName = "SFO - San Francisco"
            departureSuggestion.regionNames = departureRegionNames
            val departureAirport = SuggestionV4.Airport()
            departureAirport.airportCode = "SFO"
            val departureHierarchy = SuggestionV4.HierarchyInfo()
            departureHierarchy.airport = departureAirport
            departureSuggestion.hierarchyInfo = departureHierarchy

            val testDepartureCoordinates = SuggestionV4.LatLng()
            testDepartureCoordinates.lat = 600.5
            testDepartureCoordinates.lng = 300.3
            departureSuggestion.coordinates = testDepartureCoordinates

            val arrivalSuggestion = SuggestionV4()
            arrivalSuggestion.gaiaId = "5678"
            val arrivalRegionNames = SuggestionV4.RegionNames()
            arrivalRegionNames.displayName = "Detroit"
            arrivalRegionNames.shortName = "DTW"
            arrivalRegionNames.fullName = "DTW - Detroit"
            arrivalSuggestion.regionNames = arrivalRegionNames
            val arrivalAirport = SuggestionV4.Airport()
            arrivalAirport.airportCode = "DTW"
            val arrivalHierarchy = SuggestionV4.HierarchyInfo()
            arrivalHierarchy.airport = arrivalAirport
            arrivalSuggestion.hierarchyInfo = arrivalHierarchy

            val testArrivalCoordinates = SuggestionV4.LatLng()
            testArrivalCoordinates.lat = 100.00
            testArrivalCoordinates.lng = 500.00
            arrivalSuggestion.coordinates = testArrivalCoordinates

            val childList = ArrayList<Int>()
            childList.add(4)
            val checkIn = LocalDate().plusDays(2)
            val checkOut = if (isRoundTrip) LocalDate().plusDays(3) else null

            return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null)
        }

        @JvmStatic
        fun getKrazyglueResponse(isSuccessful: Boolean) : KrazyglueResponse {
            val krazyGlueResponse = KrazyglueResponse()
            krazyGlueResponse.success = isSuccessful
            if (isSuccessful) {
                krazyGlueResponse.krazyglueHotels = getKrazyGlueHotels()
                krazyGlueResponse.destinationDeepLink = "https://www.expedia.com/go?type=Hotel-Search&siteid=1&langid=1033&destination=Las+Vegas+%28and+vicinity%29&regionId=178276&startDate=11%2F13%2F2017&endDate=11%2F20%2F2017&adults=2&sort=recommended&tripStartDate=2017-11-13&tripEndDate=2017-11-20&bookingDateTime=2017-09-08T16:38:09.336Z&partnerId=expedia-hot-mobile-conf&mdpcid=US.direct.expedia-hot-mobile-conf.xsell_viewmore.hotel"
            }

            return krazyGlueResponse
        }

        @JvmStatic
        fun getKrazyGlueHotels(): List<KrazyglueResponse.KrazyglueHotel> {
            val firstKrazyHotel = getKrazyglueHotel("11111", "Mariot")
            val secondKrazyHotel = getKrazyglueHotel("99999", "Cosmopolitan")
            val thirdKrazyHotel = getKrazyglueHotel("55555", "Holiday Inn")

            return listOf(firstKrazyHotel, secondKrazyHotel, thirdKrazyHotel)
        }

        @JvmStatic
        fun getKrazyglueHotel(hotelID: String, hoteName: String): KrazyglueResponse.KrazyglueHotel {
            val hotel = KrazyglueResponse.KrazyglueHotel()
            hotel.hotelId = hotelID
            hotel.hotelName = hoteName
            hotel.guestRating = "4.0"
            hotel.airAttachedPrice = "220$"
            hotel.standAlonePrice = "330$"
            hotel.hotelImage = "image"
            hotel.starRating = "2.5"
            return hotel
        }
    }
}
