package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.vm.flights.FlightViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightViewModelTest {

    lateinit private var sut: FlightViewModel
    lateinit var flightLeg: FlightLeg

    fun createSystemUnderTest() {
        sut = FlightViewModel(getContext(), flightLeg)
    }

    fun createExpectedFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.elapsedDays = 1
        flightLeg.durationHour = 19
        flightLeg.durationMinute = 10
        flightLeg.departureTimeShort = "1:10AM"
        flightLeg.arrivalTimeShort = "12:20PM"
        flightLeg.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flightLeg.stopCount = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$11"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "200.0"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.0", "USD")
        flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")


        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
    }

    @Test
    fun priceString() {
        createExpectedFlightLeg()
        createSystemUnderTest()
        assertEquals("$200", sut.price())
    }

    @Test
    fun testFlightContentDesc() {
        createExpectedFlightLeg()
        flightLeg.flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightLeg.flightSegments.add(createFlightSegment1("San Francisco", "SFO", "Honolulu", "HNL", 1, 34))
        flightLeg.flightSegments.add(createFlightSegment1("Honolulu", "HNL", "Tokyo", "NRT", 1, 34))
        createSystemUnderTest()
        assertEquals("Flight time is 01:10:00 to 12:20:00 plus 1d with price difference of $200. Flying with UnitedDelta. The flight duration is 19 hours 10 minutes with 1 stops\\u0020SFO to HNL. 2 hours 2 minutes. \\u0020Layover 1 hour 34 minutes. \\u0020HNL to NRT. 2 hours 2 minutes. \\u0020Layover 1 hour 34 minutes. \\u0020Button", sut.getFlightContentDesc())
    }

    private fun createFlightSegment1(departureCity: String, departureAirport: String, arrivalCity: String, arrivalAirport: String, layoverHrs: Int, layoverMins: Int): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.flightNumber = "51"
        airlineSegment.airplaneType = "Airbus A320"
        airlineSegment.carrier = "Virgin America"
        airlineSegment.operatingAirlineCode = ""
        airlineSegment.operatingAirlineName = ""
        airlineSegment.departureDateTimeISO = ""
        airlineSegment.arrivalDateTimeISO = ""
        airlineSegment.departureCity = departureCity
        airlineSegment.arrivalCity = arrivalCity
        airlineSegment.departureAirportCode = departureAirport
        airlineSegment.arrivalAirportCode = arrivalAirport
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = layoverHrs
        airlineSegment.layoverDurationMinutes = layoverMins
        airlineSegment.elapsedDays = 0
        airlineSegment.seatClass = "coach"
        airlineSegment.bookingCode = "O"
        return airlineSegment
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testFlightOfferContentDescription() {
        createExpectedFlightLeg()
        createSystemUnderTest()
        val expectedResult = SpannableBuilder()
        expectedResult.append("Flight time is 01:10:00 to 12:20:00 plus 1d with price $200. Flying with UnitedDelta. The flight duration is 19 hours 10 minutes with 1 stops\\u0020Button")
        assertEquals(sut.getFlightContentDesc(), expectedResult.build())
    }
}
