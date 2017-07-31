package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.vm.flights.FlightConfirmationCardViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightConfirmationCardViewModelTest {

    val titleSubscriber = TestSubscriber<String>()
    val subtitleSubscriber = TestSubscriber<String>()
    val urlSubscriber = TestSubscriber<String>()
    val secondaryHeaderText = TestSubscriber.create<String>()

    val outboundDepartureDateTimeISO = DateTime.now().toString()
    val arrivalDateTimeIso  = DateTime.now().plusDays(2).toString()
    val flightTime = FlightV2Utils.formatTimeShort(getContext(), outboundDepartureDateTimeISO)
    val formattedDate = formatDate(outboundDepartureDateTimeISO)
    val numberOfTravelers = 3

    @Test
    fun testFlightConfirmationCardViewModelTest() {
        val arrivalTime = FlightV2Utils.formatTimeShort(getContext(), arrivalDateTimeIso)
        val flight = makeFlightLeg(outboundDepartureDateTimeISO, arrivalDateTimeIso)
        val viewModel = FlightConfirmationCardViewModel(getContext(), flight, numberOfTravelers)
        setupTestSubscriptions(viewModel)

        titleSubscriber.assertValue("SEA to OAX")
        secondaryHeaderText.assertValue(" on $formattedDate")
        subtitleSubscriber.assertValue("$flightTime - $arrivalTime · Nonstop")
        urlSubscriber.assertValue(null)
    }

    fun formatDate(ISODate: String) : String {
        val outboundLocal = DateTime.parse(ISODate).toLocalDate()
        val formattedDate = LocaleBasedDateFormatUtils.localDateToMMMd(outboundLocal).toString()
        return formattedDate
    }

    private fun makeFlightLeg(departureTime: String, arrivalTime: String = "") : FlightLeg {
        val arrivalAirportCode = "OAX"
        val arrivalCity = "Oakland"
        val departureAirportCode = "SEA"
        val departureCity = "Seattle"
        val flight = FlightLeg()
        flight.segments = java.util.ArrayList<FlightLeg.FlightSegment>()

        val flightSegment = makeFlightSegment(arrivalAirportCode, arrivalCity, departureAirportCode, departureCity)
        flightSegment.departureDateTimeISO = departureTime
        flightSegment.arrivalDateTimeISO = arrivalTime

        flightSegment.departureTimeRaw = departureTime
        flightSegment.arrivalTimeRaw = arrivalTime
        flight.departureDateTimeISO = departureTime

        flight.segments.add(0, flightSegment)

        return flight
    }

    private fun makeFlightSegment(arrivalAirportCode: String, arrivalCity: String, departureAirportCode: String, departureCity: String) :  FlightLeg.FlightSegment{
        val arrivalSegment = FlightLeg.FlightSegment()
        arrivalSegment.arrivalAirportAddress = FlightLeg.FlightSegment.AirportAddress()
        arrivalSegment.departureAirportAddress = FlightLeg.FlightSegment.AirportAddress()

        arrivalSegment.arrivalAirportCode = arrivalAirportCode
        arrivalSegment.arrivalAirportAddress.city = arrivalCity

        arrivalSegment.departureAirportCode = departureAirportCode
        arrivalSegment.departureAirportAddress.city = departureCity

        return arrivalSegment
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    private fun setupTestSubscriptions(viewModel: FlightConfirmationCardViewModel) {
        viewModel.titleSubject.subscribe(titleSubscriber)
        viewModel.subtitleSubject.subscribe(subtitleSubscriber)
        viewModel.urlSubject.subscribe(urlSubscriber)
        viewModel.secondaryTitleSubject.subscribe(secondaryHeaderText)
    }
}