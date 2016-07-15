package com.expedia.bookings.utils

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageFlightUtilsTest {
    val testFlightLeg = buildTestFlightLeg()
    val testDepartTime = "2014-07-05T12:30:00.000-05:00"
    val testArrivalTime = "2014-07-05T16:40:00.000-05:00"
    var activity: Activity by Delegates.notNull()
    var resources: Resources by Delegates.notNull()

    val expectedNoElapsedDaysAccesibleString = "12:30 pm to 4:40 pm"
    val expectedWithElapsedDaysAccesibleString = "12:30 pm to 4:40 pm plus 2d"


    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
    }

    @Test
    fun testAccessibleDepartArrivalNoElapsedDays() {
        testFlightLeg.elapsedDays = 0
        testFlightLeg.departureDateTimeISO = testDepartTime
        testFlightLeg.arrivalDateTimeISO = testArrivalTime
        val testString = PackageFlightUtils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedNoElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testAccessibleDepartArrivalWithElapsedDays() {
        testFlightLeg.elapsedDays = 2
        testFlightLeg.departureDateTimeISO = testDepartTime
        testFlightLeg.arrivalDateTimeISO = testArrivalTime
        val testString = PackageFlightUtils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testNoOperatingAirlineName() {
        buildTestFlightSegment()
        val testOperatingAirlinesNameString = PackageFlightUtils.getOperatingAirlineNameString(activity, testFlightLeg.flightSegments[0])
        assertEquals(null, testOperatingAirlinesNameString)
    }

    @Test
    fun testGetOperatingAirlineNameWithCode() {
        buildTestFlightSegment()
        testFlightLeg.flightSegments[0].operatingAirlineName = "Alaska Airlines"
        testFlightLeg.flightSegments[0].operatingAirlineCode = "AS"
        val testOperatingAirlinesNameString = PackageFlightUtils.getOperatingAirlineNameString(activity, testFlightLeg.flightSegments[0])
        assertEquals("Operated by Alaska Airlines (AS)", testOperatingAirlinesNameString)
    }

    @Test
    fun testGetOperatingAirlineNameWithoutCode() {
        buildTestFlightSegment()
        testFlightLeg.flightSegments[0].operatingAirlineName = "Alaska Airlines"
        val testOperatingAirlinesNameString = PackageFlightUtils.getOperatingAirlineNameString(activity, testFlightLeg.flightSegments[0])
        assertEquals("Operated by Alaska Airlines", testOperatingAirlinesNameString)
    }

    fun buildTestFlightLeg() : FlightLeg {
        val mockLeg = FlightLeg()
        mockLeg.departureDateTimeISO = testDepartTime
        mockLeg.arrivalDateTimeISO = testArrivalTime
        return mockLeg
    }

    fun buildTestFlightSegment() {
        val mockFlightSegment = FlightLeg.FlightSegment()
        testFlightLeg.flightSegments = arrayListOf<FlightLeg.FlightSegment>()
        testFlightLeg.flightSegments.add(0, mockFlightSegment)
    }
}
