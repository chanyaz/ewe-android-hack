package com.expedia.bookings.utils

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class FlightV2UtilsTest {
    val testDepartTime = "2014-07-05T12:30:00.000-05:00"
    val testArrivalTime = "2014-07-05T16:40:00.000-05:00"
    val testFlightLeg = buildTestFlightLeg()

    var activity: Activity by Delegates.notNull()
    var resources: Resources by Delegates.notNull()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
    }

    @Test
    fun testDepartArrivalNegativeElapsedDays() {
        testFlightLeg.elapsedDays = -1
        val expectedWithElapsedDaysAccesibleString = "12:30 pm - 4:40 pm -1d"

        val testString = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testDepartArrivalMultipleElapsedDays() {
        testFlightLeg.elapsedDays = 2
        val expectedWithElapsedDaysAccesibleString = "12:30 pm - 4:40 pm +2d"

        val testString = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testDepartArrivalNoElapsedDays() {
        testFlightLeg.elapsedDays = 0
        val expectedWithElapsedDaysAccesibleString = "12:30 pm - 4:40 pm"

        val testString = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }


    @Test
    fun testAccessibleDepartArrivalNegativeElapsedDays() {
        testFlightLeg.elapsedDays = -1
        val expectedWithElapsedDaysAccesibleString = "12:30 pm to 4:40 pm minus 1d"

        val testString = FlightV2Utils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testAccessibleDepartArrivalNoElapsedDays() {
        testFlightLeg.elapsedDays = 0
        val expectedNoElapsedDaysAccesibleString = "12:30 pm to 4:40 pm"

        val testString = FlightV2Utils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedNoElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testAccessibleDepartArrivalWithElapsedDays() {
        testFlightLeg.elapsedDays = 2
        val expectedWithElapsedDaysAccesibleString = "12:30 pm to 4:40 pm plus 2d"

        val testString = FlightV2Utils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    fun testNoOperatingAirlineName() {
        buildTestFlightSegment()
        val testOperatingAirlinesNameString = FlightV2Utils.getOperatingAirlineNameString(activity, testFlightLeg.flightSegments[0])
        assertEquals(null, testOperatingAirlinesNameString)
    }

    @Test
    fun testGetOperatingAirlineNameWithCode() {
        buildTestFlightSegment()
        testFlightLeg.flightSegments[0].operatingAirlineName = "Alaska Airlines"
        testFlightLeg.flightSegments[0].operatingAirlineCode = "AS"
        val testOperatingAirlinesNameString = FlightV2Utils.getOperatingAirlineNameString(activity, testFlightLeg.flightSegments[0])
        assertEquals("Operated by Alaska Airlines (AS)", testOperatingAirlinesNameString)
    }

    @Test
    fun testGetOperatingAirlineNameWithoutCode() {
        buildTestFlightSegment()
        testFlightLeg.flightSegments[0].operatingAirlineName = "Alaska Airlines"
        val testOperatingAirlinesNameString = FlightV2Utils.getOperatingAirlineNameString(activity, testFlightLeg.flightSegments[0])
        assertEquals("Operated by Alaska Airlines", testOperatingAirlinesNameString)
    }

    @Test
    fun testStylizedFlightDurationString() {
        testFlightLeg.durationHour = 2
        testFlightLeg.durationMinute = 20
        testFlightLeg.totalTravelDistance = 939
        testFlightLeg.totalTravelDistanceUnits = "miles"

        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppMaterialFlightDistanceOnDetails)

        val controlString = FlightV2Utils.getStylizedFlightDurationString(activity, testFlightLeg, R.color.packages_total_duration_text)
        assertEquals("Total Duration: 2h 20m", controlString)

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppMaterialFlightDistanceOnDetails)

        val variantString = FlightV2Utils.getStylizedFlightDurationString(activity, testFlightLeg, R.color.packages_total_duration_text)
        assertEquals("Total Duration: 2h 20m • 939 miles", variantString)

        testFlightLeg.totalTravelDistanceUnits = ""
        val string = FlightV2Utils.getStylizedFlightDurationString(activity, testFlightLeg, R.color.packages_total_duration_text)
        assertEquals("Total Duration: 2h 20m", string)

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
