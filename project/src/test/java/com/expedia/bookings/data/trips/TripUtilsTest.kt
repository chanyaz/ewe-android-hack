package com.expedia.bookings.data.trips

import org.joda.time.DateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun ignoreExpiredTrip() {
        val trip = Trip()
        trip.hasExpired(0)
        val result = TripUtils.hasTripStartDateBeforeDateTime(listOf(trip), dateTimeTwoWeeksFromNow(), false)
        assertFalse(result)
    }

    private fun dateTimeTwoWeeksFromNow() = DateTime.now().plusDays(14)
}
