package com.expedia.bookings.utils

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
        MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testDepartArrivalNegativeElapsedDays() {
        testFlightLeg.elapsedDays = -1
        val expectedWithElapsedDaysAccesibleString = "12:30 pm - 4:40 pm -1d"

        val testString = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testDepartArrivalMultipleElapsedDays() {
        testFlightLeg.elapsedDays = 2
        val expectedWithElapsedDaysAccesibleString = "12:30 pm - 4:40 pm +2d"

        val testString = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testDepartArrivalNoElapsedDays() {
        testFlightLeg.elapsedDays = 0
        val expectedWithElapsedDaysAccesibleString = "12:30 pm - 4:40 pm"

        val testString = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }


    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testAccessibleDepartArrivalNegativeElapsedDays() {
        testFlightLeg.elapsedDays = -1
        val expectedWithElapsedDaysAccesibleString = "12:30 pm to 4:40 pm minus 1d"

        val testString = FlightV2Utils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedWithElapsedDaysAccesibleString, testString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testAccessibleDepartArrivalNoElapsedDays() {
        testFlightLeg.elapsedDays = 0
        val expectedNoElapsedDaysAccesibleString = "12:30 pm to 4:40 pm"

        val testString = FlightV2Utils.getAccessibleDepartArrivalTime(activity, testFlightLeg)
        assertEquals(expectedNoElapsedDaysAccesibleString, testString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
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
    fun testFlightLegDurationWithButtonInfoContentDescription() {
        testFlightLeg.durationHour = 2
        testFlightLeg.durationMinute = 20
        val controlString = FlightV2Utils.getFlightLegDurationWithButtonInfoContentDescription(activity, testFlightLeg)
        assertEquals("Total Duration: 2 hour 20 minutes Expanded button. Double tap to collapse.", controlString)
    }

    fun buildTestFlightLeg(): FlightLeg {
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

    @Test
    fun testGetFlightCabinPreferenceWithSingleSegment() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.packageOfferModel.segmentsSeatClassAndBookingCode = buildTestSeatClassAndBookingCodeList(1)
        assertEquals("Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithTwoSegments() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.packageOfferModel.segmentsSeatClassAndBookingCode = buildTestSeatClassAndBookingCodeList(2)
        assertEquals("Economy + Premium Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithThreeSegments() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.packageOfferModel.segmentsSeatClassAndBookingCode = buildTestSeatClassAndBookingCodeList(3)
        assertEquals("Mixed classes", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testIsAllFlightCabinPreferencesSame() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.packageOfferModel.segmentsSeatClassAndBookingCode = buildTestSeatClassAndBookingCodeList(4)
        assertEquals("Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithNoSegments() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.packageOfferModel.segmentsSeatClassAndBookingCode = buildTestSeatClassAndBookingCodeList(0)
        assertEquals("", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithBasicEconomy() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.packageOfferModel.segmentsSeatClassAndBookingCode = buildTestSeatClassAndBookingCodeList(2)
        testFlightLeg.isBasicEconomy = true
        assertEquals("Basic Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetAdvanceSearchFilterHeaderString() {
        assertNull(FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, false, false))
        assertEquals("•  Nonstop  •  Refundable  •", FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, true, true))
        assertEquals("•  Nonstop  •", FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, true, false))
        assertEquals("•  Refundable  •", FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, false, true))
    }

    @Test
    fun testGetAdvanceSearchFilterHeaderContDesc() {
        assertNull(FlightV2Utils.getAdvanceSearchFilterHeaderContDesc(activity, false, false))
        assertEquals("Showing NonStop Refundable flights only", FlightV2Utils.getAdvanceSearchFilterHeaderContDesc(activity, true, true))
        assertEquals("Showing NonStop flights only", FlightV2Utils.getAdvanceSearchFilterHeaderContDesc(activity, true, false))
        assertEquals("Showing Refundable flights only", FlightV2Utils.getAdvanceSearchFilterHeaderContDesc(activity, false, true))
    }

    fun buildTestSeatClassAndBookingCodeList(numberOfObjects: Int): List<FlightTripDetails.SeatClassAndBookingCode> {
        val seatClassAndBookingCodeList = arrayListOf<FlightTripDetails.SeatClassAndBookingCode>()
        when (numberOfObjects) {
            1 -> seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
            2 -> {
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("premium coach"))
            }
            3 -> {
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("premium coach"))
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("business"))
            }
            4 -> { // kept all segments same to check if Economy is returned as expected output
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
                seatClassAndBookingCodeList.add(buildTestSeatClassAndBookingCode("coach"))
            }
        }
        return seatClassAndBookingCodeList
    }

    fun buildTestSeatClassAndBookingCode(seatClass: String): FlightTripDetails.SeatClassAndBookingCode {
        val seatClassAndBookingCode = FlightTripDetails().SeatClassAndBookingCode()
        seatClassAndBookingCode.seatClass = seatClass
        return seatClassAndBookingCode
    }
}
