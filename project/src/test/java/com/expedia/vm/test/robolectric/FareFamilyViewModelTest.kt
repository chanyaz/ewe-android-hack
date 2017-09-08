package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.FlightTripResponse.FareFamilyDetails
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.FareFamilyViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FareFamilyViewModelTest {
    private enum class FlightType { DOMESTIC, INTERNATIONAL }

    lateinit private var sut: FareFamilyViewModel

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)

        SettingUtils.save(activity, R.string.preference_fare_family_flight_summary, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        Db.setFlightSearchParams(setupFlightSearchParams())
        sut = FareFamilyViewModel(activity)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFareFamilyCardViewStringsForFreshCreateTrip() {
        val deltaPriceSubscriber = TestSubscriber<String>()
        val selectedClassSubscriber = TestSubscriber<String>()
        val fareFamilyTitleSubscriber = TestSubscriber<String>()
        val fromLabelVisibilitySubscriber = TestSubscriber<Boolean>()
        val travellerTextSubscriber = TestSubscriber<String>()

        sut.deltaPriceObservable.subscribe(deltaPriceSubscriber)
        sut.selectedClassObservable.subscribe(selectedClassSubscriber)
        sut.fromLabelVisibility.subscribe(fromLabelVisibilitySubscriber)
        sut.fareFamilyTitleObservable.subscribe(fareFamilyTitleSubscriber)
        sut.travellerObservable.subscribe(travellerTextSubscriber)
        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable(2))
        assertEquals("+$2", deltaPriceSubscriber.onNextEvents[0])
        assertEquals("Economy", selectedClassSubscriber.onNextEvents[0])
        assertEquals("Upgrade your flights", fareFamilyTitleSubscriber.onNextEvents[0])
        assertEquals("4 travelers", travellerTextSubscriber.onNextEvents[0])
        assertTrue(fromLabelVisibilitySubscriber.onNextEvents[0])
    }

    @Test
    fun testFareFamilyCardViewStringsAfterSelectingFareFamily() {
        val deltaPriceSubscriber = TestSubscriber<String>()
        val selectedClassSubscriber = TestSubscriber<String>()
        val fareFamilyTitleSubscriber = TestSubscriber<String>()
        val fromLabelVisibilitySubscriber = TestSubscriber<Boolean>()

        sut.deltaPriceObservable.subscribe(deltaPriceSubscriber)
        sut.selectedClassObservable.subscribe(selectedClassSubscriber)
        sut.fromLabelVisibility.subscribe(fromLabelVisibilitySubscriber)
        sut.fareFamilyTitleObservable.subscribe(fareFamilyTitleSubscriber)

        sut.tripObservable.onNext(tripResponseWithFareFamilySelected(2))
        assertEquals("",deltaPriceSubscriber.onNextEvents[0])
        assertEquals("Economy",selectedClassSubscriber.onNextEvents[0])
        assertEquals("Change your fare",fareFamilyTitleSubscriber.onNextEvents[0])
        assertFalse(fromLabelVisibilitySubscriber.onNextEvents[0])
    }

    @Test
    fun fareFamilyWidgetVisiblility() {
        val widgetVisibilitySubscriber = TestSubscriber<Boolean>()
        sut.widgetVisibilityObservable.subscribe(widgetVisibilitySubscriber)

        sut.tripObservable.onNext(tripResponseWithoutFareFamilyAvailable())
        assertFalse(widgetVisibilitySubscriber.onNextEvents[0])

        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable(2))
        assertTrue(widgetVisibilitySubscriber.onNextEvents[1])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun fareFamilyDeltaPricing() {
        val deltaPriceSubscriber = TestSubscriber<String>()
        sut.deltaPriceObservable.subscribe(deltaPriceSubscriber)

        sut.tripObservable.onNext(tripResponseWithoutFareFamilyAvailable())
        deltaPriceSubscriber.assertNoValues()

        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable(1))
        assertEquals("+$1", deltaPriceSubscriber.onNextEvents[0])

        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable(2))
        assertEquals("+$2", deltaPriceSubscriber.onNextEvents[1])
    }

    private fun tripResponseWithFareFamilySelected(numberOfObjects: Int): FlightCreateTripResponse {
        val trip = tripResponseWithFareFamilyAvailable(numberOfObjects)
        trip.isFareFamilyUpgraded = true
        return trip
    }

    private fun tripResponseWithFareFamilyAvailable(numberOfObjects: Int): FlightCreateTripResponse {
        val fareFamilyProduct = FlightTripResponse.FareFamilies("product-key", getFareFamilyDetail(numberOfObjects))
        val trip = tripResponseWithoutFareFamilyAvailable()
        trip.fareFamilyList = fareFamilyProduct
        return trip
    }

    private fun getFareFamilyDetail(size: Int): Array<FlightTripResponse.FareFamilyDetails> {
        val fareFamilyDetails = arrayListOf<FareFamilyDetails>()
        when (size) {
            1 -> fareFamilyDetails.add(FlightTripResponse.FareFamilyDetails("Economy", "Economy", "Economy",
                    Money("210.00", "USD"), Money(size, "USD"), true, HashMap()))
            2 -> {
                fareFamilyDetails.add(FlightTripResponse.FareFamilyDetails("Economy", "Economy", "Economy",
                        Money("210.00", "USD"), Money(size, "USD"), true, HashMap()))
                fareFamilyDetails.add(FlightTripResponse.FareFamilyDetails("Economy", "Economy", "Economy",
                        Money("210.00", "USD"), Money(size, "USD"), true, HashMap()))
            }
        }
        return fareFamilyDetails.toTypedArray()
    }

    private fun tripResponseWithoutFareFamilyAvailable(): FlightCreateTripResponse {
        val offer = FlightTripDetails.FlightOffer()
        val seatClassAndBookingCode = FlightTripDetails().SeatClassAndBookingCode()
        seatClassAndBookingCode.seatClass = "coach"
        offer.offersSeatClassAndBookingCode = listOf(listOf(seatClassAndBookingCode))
        val details = FlightTripDetails()
        val leg = FlightLeg()
        leg.isBasicEconomy = false
        details.offer = offer
        details.legs = listOf(leg)
        val trip = FlightCreateTripResponse()
        trip.newTrip = TripDetails(null, null, tripId = "")
        trip.details = details
        trip.fareFamilyList = FlightTripResponse.FareFamilies("product-key", emptyArray())
        return trip
    }

    private fun setupFlightSearchParams(isRoundTrip: Boolean = true): FlightSearchParams {
        val departureSuggestion = SuggestionV4()
        departureSuggestion.gaiaId = "1234"
        val departureRegionNames = SuggestionV4.RegionNames()
        departureRegionNames.displayName = "San Francisco"
        departureRegionNames.shortName = "SFO"
        departureRegionNames.fullName = "SFO - San Francisco"
        departureSuggestion.regionNames = departureRegionNames

        val testDepartureCoordinates = SuggestionV4.LatLng()
        testDepartureCoordinates.lat = 600.5
        testDepartureCoordinates.lng = 300.3
        departureSuggestion.coordinates = testDepartureCoordinates
        val hierarchyInfoDepart = SuggestionV4.HierarchyInfo()
        hierarchyInfoDepart.airport = SuggestionV4.Airport()
        hierarchyInfoDepart.airport!!.airportCode = "12qw"
        departureSuggestion.hierarchyInfo = hierarchyInfoDepart

        val arrivalSuggestion = SuggestionV4()
        arrivalSuggestion.gaiaId = "5678"
        val arrivalRegionNames = SuggestionV4.RegionNames()
        arrivalRegionNames.displayName = "Los Angeles"
        arrivalRegionNames.shortName = "LAX"
        arrivalRegionNames.fullName = "LAX - Los Angeles"
        arrivalSuggestion.regionNames = arrivalRegionNames
        arrivalSuggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name
        val hierarchyInfoArrive = SuggestionV4.HierarchyInfo()
        hierarchyInfoArrive.airport = SuggestionV4.Airport()
        hierarchyInfoArrive.airport!!.airportCode = "12qw"
        arrivalSuggestion.hierarchyInfo = hierarchyInfoArrive

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        childList.add(2)
        childList.add(4)
        val checkIn = LocalDate().plusDays(2)
        val checkOut = if (isRoundTrip) LocalDate().plusDays(3) else null

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null)
    }
}
