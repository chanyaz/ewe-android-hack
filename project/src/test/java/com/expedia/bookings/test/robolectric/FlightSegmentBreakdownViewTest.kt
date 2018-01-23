package com.expedia.bookings.test.robolectric

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightSegmentBreakdownViewTest {

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    lateinit var sut: FlightSegmentBreakdownView
    lateinit var seatClassAndBookingCodeTextView: TextView

    @Before
    fun setup() {
        sut = FlightSegmentBreakdownView(getContext(), null)
        sut.viewmodel = FlightSegmentBreakdownViewModel(getContext())
    }

    @Test
    fun testVisibiltyOfCollapseIcon() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview)
        sut.viewmodel.addSegmentRowsObserver.onNext(getFlightSegmentBreakdownList("coach", true))
        assertEquals(View.VISIBLE, sut.linearLayout.findViewById<View>(R.id.flight_overview_collapse_icon).visibility)
        sut.viewmodel.addSegmentRowsObserver.onNext(getFlightSegmentBreakdownList("coach", false))
        assertEquals(View.GONE, sut.linearLayout.findViewById<View>(R.id.flight_overview_collapse_icon).visibility)
    }

    @Test
    fun testSeatClassAndBookingCodeViewForSeatClassAbacusTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        seatClassAndBookingCodeTestCases()
    }

    fun seatClassAndBookingCodeTestCases() {
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals("Economy (O)", seatClassAndBookingCodeTextView.text)
        //Cabin Code is premium coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("premium coach")
        assertEquals("Premium Economy (O)", seatClassAndBookingCodeTextView.text)
        //Cabin Code is business
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("business")
        assertEquals("Business (O)", seatClassAndBookingCodeTextView.text)
        //Cabin Code is first
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("first")
        assertEquals("First Class (O)", seatClassAndBookingCodeTextView.text)
        //Cabin Code is empty
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("")
        assertEquals(View.GONE, seatClassAndBookingCodeTextView.visibility)
    }

    @Test
    fun testSeatClassAndBookingCodeViewVisibilityForSeatClassAbacusTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.VISIBLE, seatClassAndBookingCodeTextView.visibility)

        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.GONE, seatClassAndBookingCodeTextView.visibility)
    }

    @Test
    fun testSeatClassAndBookingCodeViewVisibility() {
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.VISIBLE, seatClassAndBookingCodeTextView.visibility)

        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.GONE, seatClassAndBookingCodeTextView.visibility)
    }

    private fun getTextViewForSeatClassAndBookingCode(seatClass: String): TextView {
        sut.viewmodel.addSegmentRowsObserver.onNext(getFlightSegmentBreakdownList(seatClass))
        return sut.linearLayout.findViewById<View>(R.id.flight_seat_class_booking_code) as TextView
    }

    private fun getFlightSegmentBreakdownList(seatClass: String, showCollapseIcon: Boolean = false): List<FlightSegmentBreakdown> {
        val flightSegment = createFlightSegment(seatClass)
        val breakdown = FlightSegmentBreakdown(flightSegment, false, true, showCollapseIcon)
        val list: ArrayList<FlightSegmentBreakdown> = ArrayList()
        list.add(breakdown)
        return list.toList()
    }

    private fun createFlightSegment(seatClass: String): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.flightNumber = "51"
        airlineSegment.airplaneType = "Airbus A320"
        airlineSegment.carrier = "Virgin America"
        airlineSegment.operatingAirlineCode = ""
        airlineSegment.operatingAirlineName = ""
        airlineSegment.departureDateTimeISO = ""
        airlineSegment.arrivalDateTimeISO = ""
        airlineSegment.departureCity = "San Francisco"
        airlineSegment.arrivalCity = "Honolulu"
        airlineSegment.departureAirportCode = "SFO"
        airlineSegment.arrivalAirportCode = "SEA"
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = 0
        airlineSegment.layoverDurationMinutes = 0
        airlineSegment.elapsedDays = 0
        airlineSegment.seatClass = seatClass
        airlineSegment.bookingCode = "O"
        return airlineSegment
    }
}
