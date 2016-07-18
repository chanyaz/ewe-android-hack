package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.robolectric.ItineraryManagerTest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.UserSignInTest
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.JodaUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

import com.expedia.vm.flights.FlightConfirmationCardViewModel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightConfirmationCardViewModelTest {
    @Test
    fun flightConfirmationCardViewModelTest() {

        val titleSubscriber = TestSubscriber<String>()
        val subtitleSubscriber = TestSubscriber<String>()

        val outboundDepartureDateTimeISO = DateTime.now().toString()
        val flightTime = DateUtils.formatTimeShort(outboundDepartureDateTimeISO)
        val formattedDate = formatDate(outboundDepartureDateTimeISO)
        val numberOfTravelers = 3
        val flight = makeFlightLeg(outboundDepartureDateTimeISO)

        val viewModel = FlightConfirmationCardViewModel(getContext(), flight, numberOfTravelers)
        viewModel.titleSubject.subscribe(titleSubscriber)
        viewModel.subtitleSubject.subscribe(subtitleSubscriber)

        titleSubscriber.assertValue("Flight to (OAX) Oakland")
        subtitleSubscriber.assertValue("$formattedDate at $flightTime, $numberOfTravelers Travelers")
    }

    fun formatDate(ISODate: String) : String {
        val outboundLocal = DateTime.parse(ISODate).toLocalDate()
        val formattedDate = DateUtils.localDateToMMMd(outboundLocal).toString()
        return formattedDate
    }

    private fun makeFlightLeg(departureTime: String) : FlightLeg {
        val flight = FlightLeg()
        flight.segments = java.util.ArrayList<FlightLeg.FlightSegment>()
        
        val outboundAirportArrivalCode = "OAX"
        val outboundCity = "Oakland"
        val flightSegment = makeFlightSegment(outboundAirportArrivalCode, outboundCity)

        flight.departureDateTimeISO = departureTime

        flight.segments.add(0, flightSegment)

        return flight
    }

    private fun makeFlightSegment(airportCode: String, city: String) :  FlightLeg.FlightSegment{
        val arrivalSegment = FlightLeg.FlightSegment()
        arrivalSegment.arrivalAirportAddress = FlightLeg.FlightSegment.AirportAddress()

        arrivalSegment.arrivalAirportCode = airportCode
        arrivalSegment.arrivalAirportAddress.city = city

        return arrivalSegment
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}