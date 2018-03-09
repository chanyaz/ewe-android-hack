package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.data.payment.Traveler
import org.joda.time.LocalDate
import java.math.BigDecimal
import java.util.ArrayList

class FlightTestUtil {

    companion object {

        @JvmStatic fun getFlightSearchParams(isRoundTrip: Boolean, includeChild: Boolean = true): FlightSearchParams {
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

            if (includeChild) {
                childList.add(4)
            }
            val checkIn = LocalDate().plusDays(2)
            val checkOut = if (isRoundTrip) LocalDate().plusDays(3) else null

            return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null)
        }

        @JvmStatic
        fun getKrazyglueResponse(isSuccessful: Boolean, containsHotels: Boolean = true): KrazyglueResponse {
            val krazyGlueResponse = KrazyglueResponse()
            krazyGlueResponse.success = isSuccessful
            if (isSuccessful) {
                krazyGlueResponse.krazyglueHotels = getKrazyGlueHotels(containsHotels)
                krazyGlueResponse.destinationDeepLink = "https://www.expedia.com/go?type=Hotel-Search&siteid=1&langid=1033&destination=Las+Vegas+%28and+vicinity%29&regionId=178276&startDate=11%2F13%2F2017&endDate=11%2F20%2F2017&adults=2&sort=recommended&tripStartDate=2017-11-13&tripEndDate=2017-11-20&bookingDateTime=2017-09-08T16:38:09.336Z&partnerId=expedia-hot-mobile-conf&mdpcid=US.direct.expedia-hot-mobile-conf.xsell_viewmore.hotel"
            } else {
                val xSellError = KrazyglueResponse.XSellError()
                xSellError.errorCause = "KrazyglueError"
                krazyGlueResponse.xsellError = xSellError
            }

            return krazyGlueResponse
        }

        @JvmStatic
        fun getKrazyGlueHotels(containsHotels: Boolean = true): List<KrazyglueResponse.KrazyglueHotel> {
            if (containsHotels) {
                val firstKrazyHotel = getKrazyglueHotel("11111", "Mariot")
                val secondKrazyHotel = getKrazyglueHotel("99999", "Cosmopolitan")
                val thirdKrazyHotel = getKrazyglueHotel("55555", "Holiday Inn")

                return listOf(firstKrazyHotel, secondKrazyHotel, thirdKrazyHotel)
            }
            return emptyList()
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

        @JvmStatic
        fun getFlightCreateTripResponse(remainingSeats: Int = 9): FlightCreateTripResponse {
            val flightCreateTripResponse = FlightCreateTripResponse()
            flightCreateTripResponse.tealeafTransactionId = "123456"
            val newTrip = TripDetails("1234", "5678", "9101112")
            flightCreateTripResponse.newTrip = newTrip
            val pricePerPassengerList = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
            val passengerInfo = FlightTripDetails().PricePerPassengerCategory()
            passengerInfo.passengerCategory = FlightTripDetails.PassengerCategory.ADULT
            passengerInfo.basePrice = Money(170, "USD")
            passengerInfo.totalPrice = Money(223, "USD")
            passengerInfo.taxesPrice = Money(53, "USD")
            pricePerPassengerList.add(passengerInfo)
            val flightOffer = FlightTripDetails.FlightOffer()
            flightOffer.totalPrice = Money(223, "USD")
            flightOffer.seatsRemaining = remainingSeats
            val flightTripDetails = FlightTripDetails()

            flightTripDetails.legs = ArrayList()
            val flightLeg = FlightLeg()
            flightLeg.segments = ArrayList()
            flightLeg.segments.add(FlightLeg.FlightSegment())
            flightTripDetails.legs.add(flightLeg)
            flightOffer.pricePerPassengerCategory = pricePerPassengerList
            flightOffer.numberOfTickets = "2"
            flightTripDetails.offer = flightOffer
            flightCreateTripResponse.details = flightTripDetails
            flightCreateTripResponse.totalPriceIncludingFees = Money(223, "USD")
            flightCreateTripResponse.totalPrice = Money()
            flightCreateTripResponse.totalPrice.currencyCode = "USD"
            flightCreateTripResponse.selectedCardFees = Money(0, "USD")
            return flightCreateTripResponse
        }

        @JvmStatic
        fun getFlightItinDetailsResponse(): FlightItinDetailsResponse {
            val outboundLeg = FlightItinDetailsResponse.Flight.Leg()
            outboundLeg.sharableFlightLegURL = "www.expedia_test_outbound.com"
            val outboundSegments = ArrayList<FlightItinDetailsResponse.Flight.Leg.Segment>()
            val outboundSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
            outboundSegment.departureTime = AbstractItinDetailsResponse.Time()
            outboundSegment.departureTime.localizedShortDate = "5/20/17"
            outboundSegment.departureLocation = FlightItinDetailsResponse.Flight.Leg.Segment.Location()
            outboundSegment.arrivalLocation = FlightItinDetailsResponse.Flight.Leg.Segment.Location()
            outboundSegment.departureLocation.city = "Seattle"
            outboundSegment.arrivalLocation.city = "Oakland"
            outboundSegments.add(outboundSegment)
            outboundLeg.segments = outboundSegments

            val legs = ArrayList<FlightItinDetailsResponse.Flight.Leg>()
            legs.add(outboundLeg)

            val flight = FlightItinDetailsResponse.Flight()
            flight.legs = legs

            val flights = ArrayList<FlightItinDetailsResponse.Flight>()
            flights.add(flight)

            val insurance = FlightItinDetailsResponse.FlightResponseData.Insurance()
            insurance.insuranceTypeId = 12345
            insurance.price = FlightItinDetailsResponse.FlightResponseData.Insurance.Price()
            insurance.price.total = "10.00"
            val insuranceList = ArrayList<FlightItinDetailsResponse.FlightResponseData.Insurance>()
            insuranceList.add(insurance)

            val response = FlightItinDetailsResponse()
            response.responseData = FlightItinDetailsResponse.FlightResponseData()
            response.responseData.orderNumber = 111111
            response.responseData.flights = flights
            return response
        }

        @JvmStatic
        fun getCheckoutResponse(itineraryNumber: String = "11111",
                                travelRecordLocator: String = "22222",
                                tripId: String = "33333",
                                listOfTravelers: List<Traveler> = listOf(Traveler("test", "traveler", "1", "9999999", "test@email.com", false)),
                                flightAggregatedResponse: FlightCheckoutResponse.FlightAggregatedResponse = FlightCheckoutResponse.FlightAggregatedResponse(),
                                details: FlightTripDetails = FlightTripDetails(),
                                hasDetails: Boolean = true): FlightCheckoutResponse {
            val response = FlightCheckoutResponse()
            response.newTrip = TripDetails(itineraryNumber, travelRecordLocator, tripId)
            if (hasDetails) {
                response.details = details
            }
            response.passengerDetails = listOfTravelers
            response.flightAggregatedResponse = flightAggregatedResponse

            return response
        }

        @JvmStatic
        fun getFlightAggregatedResponse(listOfFlightDetails: List<FlightTripDetails> = listOf<FlightTripDetails>()): FlightCheckoutResponse.FlightAggregatedResponse {
            val aggregatedResponse = FlightCheckoutResponse.FlightAggregatedResponse()
            aggregatedResponse.flightsDetailResponse = listOfFlightDetails
            return aggregatedResponse
        }

        @JvmStatic
        fun getFlightTripDetails(numberOfTickets: String = "1",
                                 insuranceTypeId: String = "typeId",
                                 totalPrice: BigDecimal = BigDecimal.ZERO): FlightTripDetails {
            val offer = FlightTripDetails.FlightOffer()
            val insuranceProduct = InsuranceProduct()
            insuranceProduct.typeId = insuranceTypeId
            insuranceProduct.totalPrice = Money()
            insuranceProduct.totalPrice.amount = totalPrice
            offer.numberOfTickets = numberOfTickets
            offer.selectedInsuranceProduct = insuranceProduct

            val details = FlightTripDetails()
            details.offer = offer
            return details
        }
    }
}
