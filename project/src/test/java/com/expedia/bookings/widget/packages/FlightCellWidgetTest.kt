package com.expedia.bookings.widget.packages

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.flights.FlightOverviewRowViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCellWidgetTest {

    private val context = RuntimeEnvironment.application

    @Test
    fun testFSRInfographicVisibility() {
        val flightCellWidget = FlightCellWidget(RuntimeEnvironment.application, false)
        val selectedFlight = getMockSelectedFlight()
        flightCellWidget.bind(FlightOverviewRowViewModel(context, selectedFlight), 5)
        assertEquals(View.VISIBLE, flightCellWidget.flightLayoverWidget.visibility)

        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightHideFSRInfographic, 1)
        flightCellWidget.bind(FlightOverviewRowViewModel(context, selectedFlight), 5)
        assertEquals(View.GONE, flightCellWidget.flightLayoverWidget.visibility)
    }

    private fun  getMockSelectedFlight(): FlightLeg {
        val flight = FlightLeg()
        flight.elapsedDays = 1
        flight.durationHour = 19
        flight.durationMinute = 10
        flight.departureTimeShort = "1:10AM"
        flight.arrivalTimeShort = "12:20PM"
        flight.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flight.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flight.stopCount = 1
        flight.packageOfferModel = PackageOfferModel()
        flight.packageOfferModel.price = PackageOfferModel.PackagePrice()
        val price = Money("111", "USD")
        price.roundedAmount = BigDecimal(111)
        flight.packageOfferModel.price.averageTotalPricePerTicket = price
        val segmentList = ArrayList<FlightLeg.FlightSegment>()
        val segment = FlightLeg.FlightSegment()
        segment.departureAirportCode = "departureAirportCode"
        segment.arrivalAirportCode = "arrivalAirportCode"
        segment.durationHours = 19
        segment.durationMinutes = 10
        segment.arrivalTimeRaw = DateTime.now().plusDays(1).toString()
        segmentList.add(segment)
        flight.flightSegments = segmentList
        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flight.airlines = airlines
        return flight
    }
}