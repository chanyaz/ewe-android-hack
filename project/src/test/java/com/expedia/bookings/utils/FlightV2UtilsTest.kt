package com.expedia.bookings.utils

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.expedia.bookings.text.HtmlCompat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class FlightV2UtilsTest {
    val testDepartTime = "2014-07-05T12:30:00.000-05:00"
    val testArrivalTime = "2014-07-05T16:40:00.000-05:00"
    val testFlightLeg = buildTestFlightLeg()
    private val FARE_FAMILY_INCLUDED_CATEGORY = "included"
    private val FARE_FAMILY_CHARGEABLE_CATEGORY = "chargeable"
    private val FARE_FAMILY_NOT_OFFERED_CATEGORY = "notoffered"

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
        testFlightLeg.seatClassAndBookingCodeList = buildTestSeatClassAndBookingCodeList(1)
        assertEquals("Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithTwoSegments() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.seatClassAndBookingCodeList = buildTestSeatClassAndBookingCodeList(2)
        assertEquals("Economy + Premium Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithThreeSegments() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.seatClassAndBookingCodeList = buildTestSeatClassAndBookingCodeList(3)
        assertEquals("Mixed classes", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testIsAllFlightCabinPreferencesSame() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.seatClassAndBookingCodeList = buildTestSeatClassAndBookingCodeList(4)
        assertEquals("Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithNoSegments() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.seatClassAndBookingCodeList = buildTestSeatClassAndBookingCodeList(0)
        assertEquals("", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetFlightCabinPreferenceWithBasicEconomy() {
        testFlightLeg.packageOfferModel = PackageOfferModel()
        testFlightLeg.seatClassAndBookingCodeList = buildTestSeatClassAndBookingCodeList(2)
        testFlightLeg.isBasicEconomy = true
        assertEquals("Basic Economy", FlightV2Utils.getFlightCabinPreferences(activity, testFlightLeg))
    }

    @Test
    fun testGetAdvanceSearchFilterHeaderString() {
        val priceString = "Prices roundtrip, per person."
        assertNull(FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, false, false, priceString))
        assertEquals("Showing nonstop flights. " + priceString, FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, true, false, priceString).toString())
        assertEquals("Showing nonstop and refundable flights. " + priceString, FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, true, true, priceString).toString())
        assertEquals("Showing refundable flights. " + priceString, FlightV2Utils.getAdvanceSearchFilterHeaderString(activity, false, true, priceString).toString())
    }

    @Test
    fun testGetSelectedClassesStringForOneWay() {
        val flightTripDetails = FlightTripDetails()
        flightTripDetails.offer = FlightTripDetails.FlightOffer()
        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1))
        flightTripDetails.legs = listOf(testFlightLeg)
        assertEquals("Selected: Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(2))
        assertEquals("Selected: Economy, Premium Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(3))
        assertEquals("Selected: Mixed classes", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(4))
        assertEquals("Selected: Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(3))
        testFlightLeg.isBasicEconomy = true
        flightTripDetails.legs = listOf(testFlightLeg)
        assertEquals("Selected: Basic Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))
    }

    @Test
    fun testGetSelectedClassesStringForRoundTrip() {
        val flightTripDetails = FlightTripDetails()
        flightTripDetails.offer = FlightTripDetails.FlightOffer()
        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1), buildTestSeatClassAndBookingCodeList(1))
        flightTripDetails.legs = listOf(buildTestFlightLeg(), buildTestFlightLeg())
        assertEquals("Selected: Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1), listOf(buildTestSeatClassAndBookingCode("premium coach")))
        assertEquals("Selected: Economy, Premium Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(2),
                listOf(buildTestSeatClassAndBookingCode("business"), buildTestSeatClassAndBookingCode("first")))
        assertEquals("Selected: Mixed classes", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.legs[0].isBasicEconomy = true
        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1), buildTestSeatClassAndBookingCodeList(1))
        assertEquals("Selected: Basic Economy, Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1), buildTestSeatClassAndBookingCodeList(2))
        assertEquals("Selected: Basic Economy, Mixed classes", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.legs[0].isBasicEconomy = true
        flightTripDetails.legs[1].isBasicEconomy = true
        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1), buildTestSeatClassAndBookingCodeList(2))
        assertEquals("Selected: Basic Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.legs[0].isBasicEconomy = false
        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(1), buildTestSeatClassAndBookingCodeList(2))
        assertEquals("Selected: Economy, Basic Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))

        flightTripDetails.offer.offersSeatClassAndBookingCode = listOf(buildTestSeatClassAndBookingCodeList(2), buildTestSeatClassAndBookingCodeList(1))
        assertEquals("Selected: Mixed classes, Basic Economy", FlightV2Utils.getSelectedClassesString(activity, flightTripDetails, false))
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
        val seatClassAndBookingCode = FlightTripDetails.SeatClassAndBookingCode()
        seatClassAndBookingCode.seatClass = seatClass
        return seatClassAndBookingCode
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHasMoreAmenities() {
        val fareFamilyComponents = getFareFamilyComponents()
        assertTrue(FlightV2Utils.hasMoreAmenities(fareFamilyComponents))

        fareFamilyComponents.remove("included")
        fareFamilyComponents.remove("chargeable")
        assertFalse(FlightV2Utils.hasMoreAmenities(fareFamilyComponents))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBagsAmenityResource() {
        var amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", FARE_FAMILY_INCLUDED_CATEGORY))
        assertEquals(R.drawable.flight_upsell_tick_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", FARE_FAMILY_CHARGEABLE_CATEGORY))
        assertEquals("$", amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", FARE_FAMILY_NOT_OFFERED_CATEGORY))
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", "")) //Empty Fare Family Component
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getFareFamilyComponents("ExtraLegroom", "Premium Seat", "")) //No Bags Amenity
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBagLuggageAmenityResource() {
        var amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(1))
        assertEquals(resources.getString(R.string.one), amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(2))
        assertEquals(resources.getString(R.string.two), amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(3))
        assertEquals(resources.getString(R.string.three), amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(4))
        assertEquals(resources.getString(R.string.four), amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(5))
        assertEquals("", amenityResourceType.dispVal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBagLuggageContDesc() {
        var amenityContDesc = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(1)).contentDescription
        assertEquals("1 Checked Bag available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(2)).contentDescription
        assertEquals("2 Checked Bags available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(3)).contentDescription
        assertEquals("3 Checked Bags available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getBagsAmenityResource(activity, getBagLuggageFareFamilyComponents(4)).contentDescription
        assertEquals("4 Checked Bags available.  ", amenityContDesc)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCarrOnBagContDesc() {
        var amenityContDesc = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", FARE_FAMILY_INCLUDED_CATEGORY)).contentDescription
        assertEquals("Carry on Bag available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", FARE_FAMILY_CHARGEABLE_CATEGORY)).contentDescription
        assertEquals("Carry on Bag available for a fee.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", FARE_FAMILY_NOT_OFFERED_CATEGORY)).contentDescription
        assertEquals("Carry on Bag not available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", "")).contentDescription //Empty Fare Family Component
        assertEquals("Carry on Bag not available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", "")).contentDescription //No CarryOnBag Amenity
        assertEquals("Carry on Bag not available.  ", amenityContDesc)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSeatSelectionContDesc() {
        var amenityContDesc = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", FARE_FAMILY_INCLUDED_CATEGORY)).contentDescription
        assertEquals("Seat Choice available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", FARE_FAMILY_CHARGEABLE_CATEGORY)).contentDescription
        assertEquals("Seat Choice available for a fee.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", FARE_FAMILY_NOT_OFFERED_CATEGORY)).contentDescription
        assertEquals("Seat Choice not available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", "")).contentDescription //Empty Fare Family Component
        assertEquals("Seat Choice not available.  ", amenityContDesc)

        amenityContDesc = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", "")).contentDescription //No Cancellation Amenity
        assertEquals("Seat Choice not available.  ", amenityContDesc)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCancellationAmenityResource() {
        var amenityResourceType = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", FARE_FAMILY_INCLUDED_CATEGORY))
        assertEquals(R.drawable.flight_upsell_tick_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", FARE_FAMILY_CHARGEABLE_CATEGORY))
        assertEquals("$", amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", FARE_FAMILY_NOT_OFFERED_CATEGORY))
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("CarryOnBag", "Carry on Bag", "")) //Empty Fare Family Component
        assertEquals(0, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getCarryOnBagAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", "")) //No Cancellation Amenity
        assertEquals(0, amenityResourceType.resourceId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSeatSelectionAmenityResource() {
        var amenityResourceType = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", FARE_FAMILY_INCLUDED_CATEGORY))
        assertEquals(R.drawable.flight_upsell_tick_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", FARE_FAMILY_CHARGEABLE_CATEGORY))
        assertEquals("$", amenityResourceType.dispVal)

        amenityResourceType = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", FARE_FAMILY_NOT_OFFERED_CATEGORY))
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("SeatReservation", "Seat Choice", "")) //Empty Fare Family Component
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)

        amenityResourceType = FlightV2Utils.getSeatSelectionAmenityResource(activity, getFareFamilyComponents("Bags", "Checked Bags", "")) //No Cancellation Amenity
        assertEquals(R.drawable.flight_upsell_cross_icon, amenityResourceType.resourceId)
    }

    private fun getFareFamilyComponents(): HashMap<String, HashMap<String, String>> {
        val fareFamilyComponentMap = HashMap<String, HashMap<String, String>>()
        fareFamilyComponentMap.put("notoffered", HashMap<String, String>())
        fareFamilyComponentMap.put("unknown", HashMap<String, String>())
        var amenityMap = HashMap<String, String>()
        amenityMap.put("SeatReservation", "Seat Choice")
        amenityMap.put("RefundBeforeDeparture", "Cancellation")
        fareFamilyComponentMap.put("included", amenityMap)
        amenityMap = HashMap<String, String>()
        amenityMap.put("Bags", "Checked Bags")
        amenityMap.put("ExtraLegroom", "Premium Seat")
        amenityMap.put("PriorityBoarding", "Priority Boarding")
        fareFamilyComponentMap.put("chargeable", amenityMap)
        return fareFamilyComponentMap
    }

    private fun getFareFamilyComponents(amenityKey: String, amenityDispValue: String, amenityCategory: String): HashMap<String, HashMap<String, String>> {
        val fareFamilyComponentMap = HashMap<String, HashMap<String, String>>()
        val amenityMap = HashMap<String, String>()
        amenityMap.put(amenityKey, amenityDispValue)
        when (amenityCategory) {
            FARE_FAMILY_INCLUDED_CATEGORY -> fareFamilyComponentMap.put("included", amenityMap)
            FARE_FAMILY_CHARGEABLE_CATEGORY -> fareFamilyComponentMap.put("chargeable", amenityMap)
            FARE_FAMILY_NOT_OFFERED_CATEGORY -> fareFamilyComponentMap.put("notoffered", amenityMap)
        }
        return fareFamilyComponentMap
    }

    private fun getBagLuggageFareFamilyComponents(bagCount: Int): HashMap<String, HashMap<String, String>> {
        val fareFamilyComponentMap = HashMap<String, HashMap<String, String>>()
        val amenityMap = HashMap<String, String>()
        when (bagCount) {
            1 -> amenityMap.put("OneLuggage", "1 x Free Luggage")
            2 -> amenityMap.put("TwoLuggage", "2 x Free Luggage")
            3 -> amenityMap.put("ThreeLuggage", "3 x Free Luggage")
            4 -> amenityMap.put("FourLuggage", "4 x Free Luggage")
        }
        fareFamilyComponentMap.put("included", amenityMap)
        return fareFamilyComponentMap
    }

    @Test
    fun testAirlineMayChargeFeeText() {
        val hasPaymentFeeText = Strings.isNotEmpty(activity.resources.getString(R.string.payment_and_baggage_fees_may_apply))
        var airlineMayChargeFee = FlightV2Utils.getAirlineMayChargeFeeText(activity, true, hasPaymentFeeText, "").toString()
        assertEquals("There may be an additional fee based on your payment method.", airlineMayChargeFee)

        airlineMayChargeFee = FlightV2Utils.getAirlineMayChargeFeeText(activity, true, hasPaymentFeeText, "https://testurl.uk").toString()
        assertEquals("There may be an additional fee based on your payment method.", HtmlCompat.stripHtml(airlineMayChargeFee))

        airlineMayChargeFee = FlightV2Utils.getAirlineMayChargeFeeText(activity, false, hasPaymentFeeText, "").toString()
        assertEquals("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.",
                airlineMayChargeFee)

        airlineMayChargeFee = FlightV2Utils.getAirlineMayChargeFeeText(activity, false, hasPaymentFeeText, "https://testurl.uk").toString()
        assertEquals("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.",
                HtmlCompat.stripHtml(airlineMayChargeFee))

        airlineMayChargeFee = FlightV2Utils.getAirlineMayChargeFeeText(activity, false, false, "").toString()
        assertEquals("", airlineMayChargeFee)
    }
}
