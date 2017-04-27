package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.vm.flights.FlightConfirmationCardViewModel
import junit.framework.Assert.assertNull
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightConfirmationCardViewModelTest {
    @Test
    fun flightConfirmationCardViewModelTest() {

        val titleSubscriber = TestSubscriber<String>()
        val subtitleSubscriber = TestSubscriber<String>()
        val urlSubscriber = TestSubscriber<String>()

        val outboundDepartureDateTimeISO = DateTime.now().toString()
        val flightTime = FlightV2Utils.formatTimeShort(getContext(), outboundDepartureDateTimeISO)
        val formattedDate = formatDate(outboundDepartureDateTimeISO)
        val numberOfTravelers = 3
        val flight = makeFlightLeg(outboundDepartureDateTimeISO)

        val viewModel = FlightConfirmationCardViewModel(getContext(), flight, numberOfTravelers)
        viewModel.titleSubject.subscribe(titleSubscriber)
        viewModel.subtitleSubject.subscribe(subtitleSubscriber)
        viewModel.urlSubject.subscribe(urlSubscriber)

        titleSubscriber.assertValue("Flight to (OAX) Oakland")
        subtitleSubscriber.assertValue("$formattedDate at $flightTime, $numberOfTravelers Travelers")
        assertNull(urlSubscriber.onNextEvents.last())
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

        flightSegment.departureTimeRaw = departureTime
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