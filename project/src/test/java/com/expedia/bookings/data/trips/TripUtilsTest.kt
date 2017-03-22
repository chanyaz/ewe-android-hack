package com.expedia.bookings.data.trips

import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Waypoint
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripUtilsTest {

    @Test
    fun customerHasTripsInNextTwoWeeks() {
        val tripWithinTwoWeeks = Trip()
        tripWithinTwoWeeks.startDate = dateTimeTwoWeeksFromNow().minusDays(2)
        val customerTrips = listOf(tripWithinTwoWeeks)

        assertTrue(TripUtils.customerHasTripsInNextTwoWeeks(customerTrips, false))
    }

    @Test
    fun customerHasZeroTrips() {
        val customerTrips = emptyList<Trip>()

        assertFalse(TripUtils.customerHasTripsInNextTwoWeeks(customerTrips, false))
    }

    @Test
    fun customerHasNoTripsInNextTwoWeeks() {
        val tripAfterTwoWeeks = Trip()
        tripAfterTwoWeeks.startDate = dateTimeTwoWeeksFromNow().plusDays(1)
        val customerTrips = listOf(tripAfterTwoWeeks)

        assertFalse(TripUtils.customerHasTripsInNextTwoWeeks(customerTrips, false))
    }

    @Test
    fun fetchTripsBeforeDateTime() {
        val oldTrip = Trip()
        val olderTrip = Trip()
        val goodTrip = Trip()
        val anotherGoodTrip = Trip()

        val now = DateTime.now()
        val dateTimeOutsideOfWindow = now.plusDays(15)

        goodTrip.startDate = now
        anotherGoodTrip.startDate = now.plusDays(7)
        oldTrip.startDate = dateTimeOutsideOfWindow
        olderTrip.startDate = dateTimeOutsideOfWindow.plusDays(2)

        val trips = listOf(oldTrip, goodTrip, olderTrip, anotherGoodTrip)

        val fourteenDaysAway = now.plusDays(14)

        val result = TripUtils.hasTripStartDateBeforeDateTime(trips, fourteenDaysAway, false)
        assertTrue(result)
    }

    @Test
    fun fetchTripsAndIncludeSharedItins() {
        val dateTimeTwoWeeksFromNow = dateTimeTwoWeeksFromNow()
        val nonSharedTrip = Trip()
        val includeSharedItins = true
        val sharedTrip = Trip()
        sharedTrip.setIsShared(true)

        nonSharedTrip.startDate = dateTimeTwoWeeksFromNow.minusDays(3)
        sharedTrip.startDate = dateTimeTwoWeeksFromNow.minusDays(4)

        val trips = listOf(nonSharedTrip, sharedTrip)
        val result = TripUtils.hasTripStartDateBeforeDateTime(trips, dateTimeTwoWeeksFromNow, includeSharedItins)

        assertTrue(result)
    }

    @Test
    fun fetchTripsAndDontIncludeSharedItins() {
        val dateTimeTwoWeeksFromNow = dateTimeTwoWeeksFromNow()
        val nonSharedTrip = Trip()
        val includeSharedItins = false
        val sharedTrip = Trip()
        sharedTrip.setIsShared(true)

        nonSharedTrip.startDate = dateTimeTwoWeeksFromNow.minusDays(3)
        sharedTrip.startDate = dateTimeTwoWeeksFromNow.minusDays(4)

        val trips = listOf(nonSharedTrip, sharedTrip)
        val result = TripUtils.hasTripStartDateBeforeDateTime(trips, dateTimeTwoWeeksFromNow, includeSharedItins)

        assertTrue(result)
    }

    @Test
    fun fetchSharedItinAndDontIncludeSharedItins() {
        val dateTimeTwoWeeksFromNow = dateTimeTwoWeeksFromNow()
        val includeSharedItins = false
        val sharedTrip = Trip()
        sharedTrip.setIsShared(true)

        sharedTrip.startDate = dateTimeTwoWeeksFromNow.minusDays(4)

        val trips = listOf(sharedTrip)
        val result = TripUtils.hasTripStartDateBeforeDateTime(trips, dateTimeTwoWeeksFromNow, includeSharedItins)

        assertFalse(result)
    }

    @Test
    fun ignoreTripsWithNullStartDate() {
        val trip = Trip()
        val anotherTrip = Trip()
        val result = TripUtils.hasTripStartDateBeforeDateTime(listOf(trip, anotherTrip), dateTimeTwoWeeksFromNow(), false)
        assertFalse(result)
    }

    @Test
    fun upcomingFlightTripsWhenAirAttachNotQualified() {
        val flightTrip = Trip()
        flightTrip.addTripComponent(TripFlight())

        val packageTrip = Trip()
        packageTrip.addTripComponent(TripFlight())
        packageTrip.addTripComponent(TripHotel())

        val hotelTrip = Trip()
        hotelTrip.addTripComponent(TripHotel())

        val carTrip = Trip()
        carTrip.addTripComponent(TripCar())

        val trips = listOf(packageTrip, flightTrip, hotelTrip, carTrip)

        val upcomingFlightTrips = TripUtils.getUpcomingAirAttachQualifiedFlightTrips(trips)

        assertEquals(0, upcomingFlightTrips.size)
    }

    @Test
    fun upcomingFlightTripsWhenAirAttachQualified() {

        val epochSeconds = LocalDateTime.now().plusDays(1).toDateTime(DateTimeZone.UTC).millis
        val jsonObj = setUpAirAttachObject(epochSeconds)

        val flightTrip = Trip()
        flightTrip.addTripComponent(TripFlight())
        flightTrip.airAttach = AirAttach(jsonObj)

        val packageTrip = Trip()
        packageTrip.addTripComponent(TripFlight())
        packageTrip.addTripComponent(TripHotel())

        val hotelTrip = Trip()
        hotelTrip.addTripComponent(TripHotel())

        val carTrip = Trip()
        carTrip.addTripComponent(TripCar())

        val trips = listOf(packageTrip, flightTrip, hotelTrip, carTrip)

        val upcomingFlightTrips = TripUtils.getUpcomingAirAttachQualifiedFlightTrips(trips)

        assertEquals(1, upcomingFlightTrips.size)
        assertEquals(flightTrip, upcomingFlightTrips[0])
    }

    @Test
    fun flightTripWithExpiredAirAttach() {
        val epochSeconds = 1481660245L
        val jsonObj = setUpAirAttachObject(epochSeconds)

        val flightTrip = Trip()
        flightTrip.addTripComponent(TripFlight())
        flightTrip.airAttach = AirAttach(jsonObj)

        val packageTrip = Trip()
        packageTrip.addTripComponent(TripFlight())
        packageTrip.addTripComponent(TripHotel())

        val hotelTrip = Trip()
        hotelTrip.addTripComponent(TripHotel())

        val trips = listOf(packageTrip, flightTrip, hotelTrip)
        val upcomingFlightTrips = TripUtils.getUpcomingAirAttachQualifiedFlightTrips(trips)

        assertEquals(0, upcomingFlightTrips.size)
    }

    @Test
    fun recentUpcomingAirAttachFlightTrip() {
        val epochSeconds = LocalDateTime.now().plusDays(1).toDateTime(DateTimeZone.UTC).millis
        val jsonObj = setUpAirAttachObject(epochSeconds)

        val recentUpcomingFlightTrip = Trip()
        recentUpcomingFlightTrip.addTripComponent(TripFlight())
        recentUpcomingFlightTrip.startDate = DateTime.now().plusDays(5)
        recentUpcomingFlightTrip.airAttach = AirAttach(jsonObj)

        val secondUpcomingFlightTrip = Trip()
        secondUpcomingFlightTrip.addTripComponent(TripFlight())
        secondUpcomingFlightTrip.startDate = DateTime.now().plusDays(10)
        secondUpcomingFlightTrip.airAttach = AirAttach(jsonObj)

        val packageTrip = Trip()
        packageTrip.addTripComponent(TripFlight())
        packageTrip.addTripComponent(TripHotel())

        val hotelTrip = Trip()
        hotelTrip.addTripComponent(TripHotel())

        val trips = listOf(packageTrip, recentUpcomingFlightTrip, secondUpcomingFlightTrip, hotelTrip)
        val recentFlightTrips = TripUtils.getUpcomingAirAttachQualifiedFlightTrip(trips)
        val upcomingFlightTrips = TripUtils.getUpcomingAirAttachQualifiedFlightTrips(trips)

        assertEquals(2, upcomingFlightTrips.size)
        assertEquals(recentUpcomingFlightTrip.startDate, recentFlightTrips!!.startDate)
    }

    @Test
    fun firstFlightTripExpiredSecondFlightTripNotAAQualified() {
        val epochSeconds = 1481660245L
        val jsonObj = setUpAirAttachObject(epochSeconds)

        val firstFlightTrip = Trip()
        firstFlightTrip.addTripComponent(TripFlight())
        firstFlightTrip.airAttach = AirAttach(jsonObj)

        val secondFlightTrip = Trip()
        secondFlightTrip.addTripComponent(TripFlight())

        val trips = listOf(firstFlightTrip, secondFlightTrip)
        val upcomingFlightTrips = TripUtils.getUpcomingAirAttachQualifiedFlightTrips(trips)

        assertEquals(0, upcomingFlightTrips.size)
    }

    @Test
    fun flightTripDestinationCity() {
        val expectedCity = "San Francisco"
        val tripFlight = TripFlight()
        val flightTrip = FlightTrip()
        val flightLeg = FlightLeg()
        val segment = Flight()
        val destinationWaypoint = Mockito.mock(Waypoint::class.java)
        val destinationAirport = Airport()

        destinationAirport.mCity = expectedCity
        Mockito.`when`(destinationWaypoint.airport).thenReturn(destinationAirport)
        segment.destinationWaypoint = destinationWaypoint
        flightLeg.addSegment(segment)
        flightTrip.addLeg(flightLeg)
        tripFlight.flightTrip = flightTrip

        val city = TripUtils.getFlightTripDestinationCity(tripFlight)
        Mockito.verify(destinationWaypoint, Mockito.times(1)).airport
        assertEquals(expectedCity, city)
    }

    @Test
    fun ignoreStartedTrip() {
        val trip = Trip()
        trip.startDate = DateTime.now().minusDays(2)
        val result = TripUtils.hasTripStartDateBeforeDateTime(listOf(trip), dateTimeTwoWeeksFromNow(), false)
        assertFalse(result)
    }

    private fun setUpAirAttachObject(expirationEpochSeconds: Long): JSONObject {
        val jsonObj = JSONObject()
        val offerExpiresObj = JSONObject()

        offerExpiresObj.put("epochSeconds", expirationEpochSeconds)
        offerExpiresObj.put("timeZoneOffsetSeconds", -28800)
        jsonObj.put("airAttachQualified", true)
        jsonObj.put("offerExpiresTime", offerExpiresObj)

        val airAttach = AirAttach(jsonObj)
        val toJson = airAttach.toJson()
        val airAttachFromJson = AirAttach()
        airAttachFromJson.fromJson(toJson)
        return jsonObj
    }

    private fun dateTimeTwoWeeksFromNow() = DateTime.now().plusDays(14)
}
