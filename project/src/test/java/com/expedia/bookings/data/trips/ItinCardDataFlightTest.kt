package com.expedia.bookings.data.trips

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinCardDataFlightTest {
    lateinit var context: Context
    lateinit var testItinCardData: ItinCardDataFlight

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        testItinCardData = ItinCardDataFlightBuilder().build(confirmationNumber =  "123")
    }

    @Test
    fun testGetSpannedConfirmationNumbers() {
        setBookingStatus(BookingStatus.BOOKED)
        var confirmations = testItinCardData.getSpannedConfirmationNumbers(context)
        assertEquals(confirmations.toString(), "IKQVCR , 123")
        setBookingStatus(BookingStatus.PENDING)
        confirmations = testItinCardData.getSpannedConfirmationNumbers(context)
        assertEquals(confirmations.toString(), "")
    }

    @Test
    fun testGetConfirmationStatus() {
        setTicketingStatus(TicketingStatus.INPROGRESS)
        assertEquals(testItinCardData.confirmationStatus, TicketingStatus.INPROGRESS)
        setTicketingStatus(TicketingStatus.COMPLETE)
        assertEquals(testItinCardData.confirmationStatus, TicketingStatus.COMPLETE)
        setTicketingStatus(TicketingStatus.CANCELLED)
        assertEquals(testItinCardData.confirmationStatus, TicketingStatus.CANCELLED)
    }

    @Test
    fun testGetTravelersFullNames() {
        val trip = (testItinCardData.tripComponent as TripFlight)
        var names =  testItinCardData.travelersFullName
        assertEquals(names, "Girija Balachandran")
        val newTraveler = Traveler()
        newTraveler.firstName = "Jim"
        newTraveler.middleName = "T"
        newTraveler.lastName = "Bob"
        trip.addTraveler(newTraveler)
        names = testItinCardData.travelersFullName
        assertEquals(names, "Girija Balachandran, Jim T Bob")
    }

    @Test
    fun testGetAirlineName(){
        val trip = (testItinCardData.tripComponent as TripFlight)
        trip.confirmations[0].carrier = "UNITED"
        assertEquals("UNITED",testItinCardData.airlineName)

        trip.confirmations.clear()
        trip.flightTrip.legs[0].segments[0].airlineName = "SpiceJet"
        assertEquals("SpiceJet",testItinCardData.airlineName)

        trip.flightTrip.legs.clear()
        assertEquals("",testItinCardData.airlineName)
    }

    @Test
    fun testGetTicketNumbers(){
        val trip = (testItinCardData.tripComponent as TripFlight)
        val ticketNumberList = ArrayList<String>()
        ticketNumberList.add("0167939252191")
        ticketNumberList.add("0265936252123")
        trip.travelers[0].ticketNumbers = ticketNumberList
        assertEquals("0167939252191 , 0265936252123",testItinCardData.ticketNumbers)
        val traveler = Traveler()
        val secondTicketNumberList = ArrayList<String>()
        secondTicketNumberList.add("0364925253116")
        traveler.ticketNumbers = secondTicketNumberList
        trip.travelers.add(traveler)

        assertEquals("0167939252191 , 0265936252123 , 0364925253116",testItinCardData.ticketNumbers)
        trip.travelers.clear()
        assertEquals("",testItinCardData.ticketNumbers)

    }

    fun setBookingStatus(bookingStatus: BookingStatus) {
        val trip = testItinCardData.tripComponent as TripFlight
        trip.bookingStatus = bookingStatus
    }

    fun setTicketingStatus(ticketingStatus: TicketingStatus) {
        val trip = testItinCardData.tripComponent as TripFlight
        trip.ticketingStatus = ticketingStatus
    }
}
