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
import com.mobiata.android.util.SettingUtils
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
    fun testSeatClassAndBookingCodeViewForSeatClassAbacusTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        seatClassAndBookingCodeTestCases()
    }

    @Test
    fun testSeatClassAndBookingCodeViewForFlightPremiumAbacusTest() {
        SettingUtils.save(getContext(), R.string.preference_flight_premium_class, true)
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightPremiumClass)
        seatClassAndBookingCodeTestCases()
    }

    @Test
    fun testSeatClassAndBookingCodeViewForAllAbacusTest() {
        SettingUtils.save(getContext(), R.string.preference_flight_premium_class, true)
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode, AbacusUtils.EBAndroidAppFlightPremiumClass)
        seatClassAndBookingCodeTestCases();
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
    fun testSeatClassAndBookingCodeViewVisibilityForFlightPremiumAbacusTest() {
        SettingUtils.save(getContext(), R.string.preference_flight_premium_class, true)
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightPremiumClass)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.VISIBLE, seatClassAndBookingCodeTextView.visibility)

        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightPremiumClass)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.GONE, seatClassAndBookingCodeTextView.visibility)
    }

    @Test
    fun testSeatClassAndBookingCodeViewVisibilityForAllActiveAbacusTest() {
        SettingUtils.save(getContext(), R.string.preference_flight_premium_class, true)
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightPremiumClass, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.VISIBLE, seatClassAndBookingCodeTextView.visibility)
    }

    @Test
    fun testSeatClassAndBookingCodeViewVisibilityForAllInActiveAbacusTest() {
        SettingUtils.save(getContext(), R.string.preference_flight_premium_class, true)
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightPremiumClass, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        //Cabin Code is coach
        seatClassAndBookingCodeTextView = getTextViewForSeatClassAndBookingCode("coach")
        assertEquals(View.GONE, seatClassAndBookingCodeTextView.visibility)
    }

    private fun getTextViewForSeatClassAndBookingCode(seatClass: String): TextView {
        sut.viewmodel.addSegmentRowsObserver.onNext(getFlightSegmentBreakdownList(seatClass))
        return sut.linearLayout.findViewById(R.id.flight_seat_class_booking_code) as TextView
    }

    private fun getFlightSegmentBreakdownList(seatClass: String): List<FlightSegmentBreakdown> {
        val flightSegment = createFlightSegment(seatClass);
        val breakdown = FlightSegmentBreakdown(flightSegment, false, true);
        var list: ArrayList<FlightSegmentBreakdown> = ArrayList()
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
