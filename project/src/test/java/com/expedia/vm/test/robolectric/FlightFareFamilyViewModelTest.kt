package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.flights.FlightFareFamilyViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class FlightFareFamilyViewModelTest {

    val flightServiceRule = ServicesRule(FlightServices::class.java)
        @Rule get

    private val context = RuntimeEnvironment.application
    lateinit private var sut: FlightFareFamilyViewModel
    val params = FlightCreateTripParams("happy_fare_family_round_trip")
    lateinit var flightCreateTripResponse: FlightCreateTripResponse

    @Before
    fun setup() {
        val createTripResponseObserver = TestSubscriber<FlightCreateTripResponse>()
        flightServiceRule.services!!.createTrip( params , createTripResponseObserver)
        flightCreateTripResponse = createTripResponseObserver.onNextEvents[0]
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
        SettingUtils.save(activity, R.string.preference_fare_family_flight_summary, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        sut = FlightFareFamilyViewModel(activity)
    }

    @Test
    fun testGetAirlinesString() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        val testSubscriber = TestSubscriber<String>()
        sut.airlinesObservable.subscribe(testSubscriber)

        //validate distinct airline names
        var segments = ArrayList<String>()
        segments.add("American Airlines")
        segments.add("Delta Airlines")
        segments.add("American Airlines")
        flightCreateTripResponse.details.getLegs()[0] = createFlightLegWithSegments(segments)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)
        assertEquals("American Airlines, Delta Airlines, Delta", testSubscriber.onNextEvents[0])

        //validate that Multiple carrier is displayed when distinct airlines names are greater than 3
        segments.add("Jet Airlines")
        flightCreateTripResponse.details.legs[0] = createFlightLegWithSegments(segments)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)
        assertEquals("Multiple Carriers", testSubscriber.onNextEvents[1])
    }

    @Test
    fun testRoundTrip() {
        var flightSearchParam = setupFlightSearchParams(2, 2, true)

        //validate search is round trip
        Db.setFlightSearchParams(flightSearchParam)
        val testSubscriber = TestSubscriber<Boolean>()
        sut.roundTripObservable.subscribe(testSubscriber)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)
        assertEquals(true, testSubscriber.onNextEvents[0])

        //validate search is one-way
        flightSearchParam = setupFlightSearchParams(2, 2, false)
        Db.setFlightSearchParams(flightSearchParam)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)
        assertEquals(false, testSubscriber.onNextEvents[1])
    }

    @Test
    fun testGetFareFamilyTripLocation() {
        var flightSearchParam = setupFlightSearchParams(2, 2, true)
        //validate search is round trip
        Db.setFlightSearchParams(flightSearchParam)
        val testSubscriber = TestSubscriber<String>()
        sut.fareFamilyTripLocationObservable.subscribe(testSubscriber)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)
        assertEquals("SFO - LAX - SFO", testSubscriber.onNextEvents[0])

        //validate search is one-way
        flightSearchParam = setupFlightSearchParams(2, 2, false)
        Db.setFlightSearchParams(flightSearchParam)
        sut.fareFamilyTripLocationObservable.subscribe(testSubscriber)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)
        assertEquals("SFO - LAX", testSubscriber.onNextEvents[1])

    }

    @Test
    fun testFareFamilyWhenNoClick() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        val fareFamilyDetailsSubscriber = TestSubscriber<String>()
        val selectedFareFamilySubscriber = TestSubscriber<FlightTripResponse.FareFamilyDetails>()
        val choosingFareFamilySubscriber = TestSubscriber<FlightTripResponse.FareFamilyDetails>()
        val fareFamilyTripLocationSubscriber = TestSubscriber<String>()
        val roundTripSubscriber = TestSubscriber<Boolean>()
        val airlinesSubscriber = TestSubscriber<String>()

        sut.fareFamilyTripLocationObservable.subscribe(fareFamilyDetailsSubscriber)
        sut.selectedFareFamilyObservable.subscribe(selectedFareFamilySubscriber)
        sut.choosingFareFamilyObservable.subscribe(choosingFareFamilySubscriber)
        sut.fareFamilyTripLocationObservable.subscribe(fareFamilyTripLocationSubscriber)
        sut.roundTripObservable.subscribe(roundTripSubscriber)
        sut.airlinesObservable.subscribe(airlinesSubscriber)
        sut.tripObservable.onNext(flightCreateTripResponse)

        assertEquals(0, fareFamilyDetailsSubscriber.valueCount)
        assertEquals(0, selectedFareFamilySubscriber.valueCount)
        assertEquals(0, choosingFareFamilySubscriber.valueCount)
        assertEquals(0, fareFamilyTripLocationSubscriber.valueCount)
        assertEquals(0, roundTripSubscriber.valueCount)
        assertEquals(0, airlinesSubscriber.valueCount)

    }

    @Test
    fun testFareFamilyWhenClick() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        val fareFamilyDetailsSubscriber = TestSubscriber<String>()
        val selectedFareFamilySubscriber = TestSubscriber<FlightTripResponse.FareFamilyDetails>()
        val choosingFareFamilySubscriber = TestSubscriber<FlightTripResponse.FareFamilyDetails>()
        val fareFamilyTripLocationSubscriber = TestSubscriber<String>()
        val roundTripSubscriber = TestSubscriber<Boolean>()
        val airlinesSubscriber = TestSubscriber<String>()

        sut.fareFamilyTripLocationObservable.subscribe(fareFamilyDetailsSubscriber)
        sut.selectedFareFamilyObservable.subscribe(selectedFareFamilySubscriber)
        sut.choosingFareFamilyObservable.subscribe(choosingFareFamilySubscriber)
        sut.fareFamilyTripLocationObservable.subscribe(fareFamilyTripLocationSubscriber)
        sut.roundTripObservable.subscribe(roundTripSubscriber)
        sut.airlinesObservable.subscribe(airlinesSubscriber)
        sut.tripObservable.onNext(flightCreateTripResponse)
        sut.showFareFamilyObservable.onNext(Unit)

        assertEquals(1, fareFamilyDetailsSubscriber.valueCount)
        assertEquals(1, selectedFareFamilySubscriber.valueCount)
        assertEquals(1, choosingFareFamilySubscriber.valueCount)
        assertEquals(1, fareFamilyTripLocationSubscriber.valueCount)
        assertEquals(1, roundTripSubscriber.valueCount)
        assertEquals(1, airlinesSubscriber.valueCount)

    }

    @Test
    fun testFareFamilyWhenDoneButtonIsClicked() {
        val selectedFareFamilySubscriber = TestSubscriber<FlightTripResponse.FareFamilyDetails>()
        sut.selectedFareFamilyObservable.subscribe(selectedFareFamilySubscriber)
        sut.choosingFareFamilyObservable.onNext(flightCreateTripResponse.fareFamilyList!!.fareFamilyDetails.get(1))
        sut.doneButtonObservable.onNext(Unit)
        assertEquals(flightCreateTripResponse.fareFamilyList!!.fareFamilyDetails.get(1), selectedFareFamilySubscriber.onNextEvents[0])
        assertEquals(flightCreateTripResponse.fareFamilyList!!.fareFamilyDetails.get(1).fareFamilyCode, selectedFareFamilySubscriber.onNextEvents[0].fareFamilyCode)
    }

    private fun setupFlightSearchParams(adultCount: Int, childCount: Int, isroundTrip: Boolean): FlightSearchParams {
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
        hierarchyInfoDepart.airport!!.airportCode = "SFO"
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
        hierarchyInfoArrive.airport!!.airportCode = "LAX"
        arrivalSuggestion.hierarchyInfo = hierarchyInfoArrive

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        for (childIndex in 1..childCount) {
            childList.add(2)
        }

        var checkOut: LocalDate? = null
        val checkIn = LocalDate().plusDays(2)
        if (isroundTrip) {
            checkOut = LocalDate().plusDays(3)
        }

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, adultCount, childList, false, null, null, null, null, null,null)
    }

    private fun createFlightLegWithSegments(segmentAirlineNames: ArrayList<String>) : FlightLeg {
        var flightLeg = FlightLeg()
        flightLeg.stopCount = 1
        val segments = ArrayList<FlightLeg.FlightSegment>()
        for (segmentAirlineName: String in segmentAirlineNames) {
            val segment = FlightLeg.FlightSegment()
            segment.airlineName = segmentAirlineName
            segment.departureTimeRaw = "2016-03-22T17:40:00.000-07:00"
            segment.arrivalTimeRaw = "2016-03-22T17:40:00.000-08:00"
            segments.add(segment)
        }

        flightLeg.segments = segments
        return flightLeg
    }

}
