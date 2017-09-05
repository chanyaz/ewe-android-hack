package com.expedia.bookings.data.trips

import android.text.TextUtils
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.Traveler
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
import java.util.HashSet
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
    fun hotelSearchParamsForDeeplinkAAOneWayFlight() {
        val expectedAirportCode = "SFO"
        val flightStartDate = DateTime.now().plusDays(10)
        val tripFlight = TripFlight()
        val flightTrip = FlightTrip()
        val segment = Flight()
        val flightLeg = FlightLeg()
        val traveler = Traveler()
        val destinationWaypoint = Mockito.mock(Waypoint::class.java)
        val destinationAirport = Airport()

        destinationAirport.mAirportCode = expectedAirportCode
        Mockito.`when`(destinationWaypoint.airport).thenReturn(destinationAirport)
        segment.destinationWaypoint = destinationWaypoint
        flightLeg.addSegment(segment)

        Mockito.`when`(flightLeg.lastWaypoint.bestSearchDateTime).thenReturn(flightStartDate)
        flightTrip.addLeg(flightLeg)

        tripFlight.addTraveler(traveler)
        tripFlight.flightTrip = flightTrip

        val hotelSearchParams = TripUtils.getHotelSearchParamsForRecentFlightAirAttach(tripFlight)

        assertEquals(flightStartDate.toLocalDate(), hotelSearchParams.checkInDate)
        assertEquals(flightStartDate.plusDays(1).toLocalDate(), hotelSearchParams.checkOutDate)
    }

    @Test
    fun hotelSearchParamsForDeeplinkAARoundTripFlight() {
        val expectedAirportCode = "SFO"
        val flightStartDate = DateTime.now().plusDays(10)
        val flightEndDate = DateTime.now().plusDays(13)
        val tripFlight = TripFlight()
        val flightTrip = FlightTrip()
        val firstFlightLeg = FlightLeg()
        val secondFlightLeg = FlightLeg()
        val segment = Flight()
        val traveler = Traveler()
        val originWayPoint = Mockito.mock(Waypoint::class.java)
        val destinationWaypoint = Mockito.mock(Waypoint::class.java)
        val destinationAirport = Airport()

        destinationAirport.mAirportCode = expectedAirportCode
        Mockito.`when`(destinationWaypoint.airport).thenReturn(destinationAirport)
        segment.originWaypoint = originWayPoint
        segment.destinationWaypoint = destinationWaypoint
        firstFlightLeg.addSegment(segment)
        secondFlightLeg.addSegment(segment)

        Mockito.`when`(firstFlightLeg.lastWaypoint.bestSearchDateTime).thenReturn(flightStartDate)
        flightTrip.addLeg(firstFlightLeg)

        Mockito.`when`(secondFlightLeg.firstWaypoint.mostRelevantDateTime).thenReturn(flightEndDate)
        flightTrip.addLeg(secondFlightLeg)

        tripFlight.addTraveler(traveler)
        tripFlight.flightTrip = flightTrip

        val hotelSearchParams = TripUtils.getHotelSearchParamsForRecentFlightAirAttach(tripFlight)

        assertEquals(flightStartDate.toLocalDate(), hotelSearchParams.checkInDate)
        assertEquals(flightEndDate.toLocalDate(), hotelSearchParams.checkOutDate)
    }

    @Test
    fun ignoreStartedTrip() {
        val trip = Trip()
        trip.startDate = DateTime.now().minusDays(2)
        val result = TripUtils.hasTripStartDateBeforeDateTime(listOf(trip), dateTimeTwoWeeksFromNow(), false)
        assertFalse(result)
    }

    @Test
    fun getTripTypesInUsersTrips() {
        val usersTrips = getUsersTripsForEventString()
        val usersTripTypeEventSet = TripUtils.createUsersTripComponentTypeEventString(usersTrips)
        val expectedValues = HashSet<String>()
        expectedValues.add("event250")
        expectedValues.add("event251")
        val expectedString = TextUtils.join(",", expectedValues)
        assertEquals(expectedString, usersTripTypeEventSet)
    }

    @Test
    fun userWithOneActivePackageTrip() {
        val oneTrip = getUserWithOneTrip()
        val oneTripComponentTypesEventString = TripUtils.createUsersTripComponentTypeEventString(oneTrip)
        val oneTripProp75String = TripUtils.createUsersProp75String(oneTrip)
        val expectedEventString = "event250,event252,event251"
        val expectedProp75String = "HOT:3:10|AIR:3:10|CAR:3:10"
        assertEquals(expectedEventString, oneTripComponentTypesEventString)
        assertEquals(expectedProp75String, oneTripProp75String)
    }

    @Test
    fun getTripsTypesAndProp75WithNoTrips() {
        val noTrips = emptyList<Trip>()
        val noTripTypesEventString = TripUtils.createUsersTripComponentTypeEventString(noTrips)
        val noTripProp75String = TripUtils.createUsersProp75String(noTrips)
        val expectedValue = ""
        assertEquals(expectedValue, noTripTypesEventString)
        assertEquals(expectedValue, noTripProp75String)
    }

    @Test
    fun calculateHotelTripDates() {
        val hotelTripsWithDates = setUsersHotelTripDates()
        val calculatedDatesString = TripUtils.getUsersActiveTrip(hotelTripsWithDates, TripComponent.Type.HOTEL)

        val expectedTripString = "HOT:-1:3"

        assertEquals(expectedTripString, calculatedDatesString)
    }

    @Test
    fun getUsersProp75String() {
        val usersTrips = getUsersTrips();
        val prop75String = TripUtils.createUsersProp75String(usersTrips);
        val expectedProp75String = "HOT:-1:3|AIR:-1:3|CAR:-5:0|LX:2:2|RAIL:0:0"
        assertEquals(expectedProp75String, prop75String)
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

    private fun getUsersTrips(): Collection<Trip> {
        val flightTrip = Trip() //trip in progress
        flightTrip.addTripComponent(TripFlight())
        flightTrip.startDate = DateTime.now().minusDays(1)
        flightTrip.endDate = DateTime.now().plusDays(3)

        val packageTrip = Trip() //trip in future
        packageTrip.addTripComponent(TripPackage())
        packageTrip.startDate = DateTime.now().plusDays(3)
        packageTrip.endDate = DateTime.now().plusDays(10)

        val hotelTrip = Trip() //trip in progress
        hotelTrip.addTripComponent(TripHotel())
        hotelTrip.startDate = DateTime.now().minusDays(1)
        hotelTrip.endDate = DateTime.now().plusDays(3)

        val secondHotelTrip = Trip()
        secondHotelTrip.addTripComponent(TripHotel())
        secondHotelTrip.startDate = DateTime.now().minusDays(20)
        secondHotelTrip.endDate = DateTime.now().minusDays(13)

        val activityTrip = Trip() //one day trip completely in future
        activityTrip.addTripComponent(TripActivity())
        activityTrip.startDate = DateTime.now().plusDays(2)
        activityTrip.endDate = DateTime.now().plusDays(2)

        val carTrip = Trip() //trip ending today
        carTrip.addTripComponent(TripCar())
        carTrip.startDate = DateTime.now().minusDays(5)
        carTrip.endDate = DateTime.now()

        val railTrip = Trip() //trips happening today
        railTrip.addTripComponent(TripRails())
        railTrip.startDate = DateTime.now()
        railTrip.endDate = DateTime.now()

        val trips = mutableListOf(packageTrip, flightTrip, hotelTrip, secondHotelTrip, activityTrip, carTrip, railTrip)
        return trips
    }

    private fun setUsersHotelTripDates(): MutableCollection<Trip> {
        val hotelTripInPast = Trip()
        hotelTripInPast.addTripComponent(TripHotel())
        hotelTripInPast.startDate = DateTime.now().minusDays(10)
        hotelTripInPast.endDate = DateTime.now().minusDays(2)

        val hotelTripInProgress = Trip()
        hotelTripInProgress.addTripComponent(TripHotel())
        hotelTripInProgress.startDate = DateTime.now().minusDays(1)
        hotelTripInProgress.endDate = DateTime.now().plusDays(3)

        val hotelTripInFuture = Trip()
        hotelTripInFuture.addTripComponent(TripHotel())
        hotelTripInFuture.startDate = DateTime.now().plusDays(2)
        hotelTripInFuture.endDate = DateTime.now().plusDays(10)

        val hotelTripNullEndDate = Trip()
        hotelTripNullEndDate.addTripComponent(TripHotel())
        hotelTripNullEndDate.startDate = DateTime.now().plusDays(5)
        hotelTripNullEndDate.endDate = null

        val userHotelTrips = mutableListOf(hotelTripInPast, hotelTripInProgress, hotelTripInFuture, hotelTripNullEndDate)

        return userHotelTrips
    }

    private fun getUsersTripsForEventString(): Collection<Trip> {
        val flightTrip = Trip()
        flightTrip.addTripComponent(TripFlight())
        flightTrip.startDate = DateTime.now().minusDays(1)
        flightTrip.endDate = DateTime.now().plusDays(3)

        val packageTrip = Trip()
        packageTrip.addTripComponent(TripPackage())
        packageTrip.startDate = DateTime.now().plusDays(3)
        packageTrip.endDate = DateTime.now().plusDays(10)

        val hotelTrip = Trip()
        hotelTrip.addTripComponent(TripHotel())
        hotelTrip.startDate = DateTime.now().minusDays(1)
        hotelTrip.endDate = DateTime.now().plusDays(3)

        val secondHotelTrip = Trip()
        secondHotelTrip.addTripComponent(TripHotel())
        secondHotelTrip.startDate = DateTime.now().plusDays(13)
        secondHotelTrip.endDate = DateTime.now().plusDays(20)

        val trips = mutableListOf(packageTrip, flightTrip, hotelTrip, secondHotelTrip)

        return trips
    }

    private fun getUserWithOneTrip() : Collection<Trip> {
        val packageTrip = Trip()
        packageTrip.addTripComponent(TripPackage())
        packageTrip.addTripComponents(listOf(TripHotel(), TripFlight(), TripCar()))
        packageTrip.startDate = DateTime.now().plusDays(3)
        packageTrip.endDate = DateTime.now().plusDays(10)

        val trip = mutableListOf(packageTrip)
        return trip
    }

}
