package com.expedia.vm.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.flights.FareFamilyItemWidget
import com.expedia.bookings.widget.flights.FlightFareFamilyWidget
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
class FareFamilyItemWidgetTest {

    val flightServiceRule = ServicesRule(FlightServices::class.java)
        @Rule get

    private val context = RuntimeEnvironment.application
    lateinit private var sut: FareFamilyItemWidget
    lateinit private var fareFamilyWidget: FlightFareFamilyWidget
    val params = FlightCreateTripParams.Builder().productKey("happy_fare_family_round_trip").build()
    lateinit var flightCreateTripResponse: FlightCreateTripResponse
    lateinit private var activity: Activity

    @Before
    fun before() {
        val createTripResponseObserver = TestSubscriber<FlightCreateTripResponse>()
        flightServiceRule.services!!.createTrip( params , createTripResponseObserver)
        flightCreateTripResponse = createTripResponseObserver.onNextEvents[0]
        activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        SettingUtils.save(activity, R.string.preference_fare_family_flight_summary, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        fareFamilyWidget = LayoutInflater.from(activity).inflate(R.layout.fare_family_details_stub, null) as FlightFareFamilyWidget
        fareFamilyWidget.viewModel = FlightFareFamilyViewModel(activity)
    }

    @Test
    fun testFareFamilyItemDefaultSelectionOnFreshCreateTrip() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)
        val firstFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(0) as FareFamilyItemWidget
        assertEquals(true, firstFareFamilyItem.fareFamilyRadioButton.isChecked)
        val secondFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(1) as FareFamilyItemWidget
        assertEquals(false, secondFareFamilyItem.fareFamilyRadioButton.isChecked)
    }

    @Test
    fun testFareFamilyItemDetails() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)
        val firstFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(0) as FareFamilyItemWidget
        val thirdFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(2) as FareFamilyItemWidget

        var fareFamilyTitle = firstFareFamilyItem.fareFamilyClassTitle
        var fareFamilyCabinClass = firstFareFamilyItem.fareFamilyCabinClass
        var fareFamilyDeltaAmount = firstFareFamilyItem.priceDelta
        var fareFamilyRoundTrip = firstFareFamilyItem.roundTrip

        assertEquals(View.VISIBLE, fareFamilyTitle.visibility)
        assertEquals("Economy", fareFamilyTitle.text)
        assertEquals(View.VISIBLE, fareFamilyCabinClass.visibility)
        assertEquals("Cabin: Economy", fareFamilyCabinClass.text)
        assertEquals(View.VISIBLE, fareFamilyDeltaAmount.visibility)
        assertEquals("+$0.00", fareFamilyDeltaAmount.text)
        assertEquals(View.VISIBLE, fareFamilyRoundTrip.visibility)
        assertEquals("roundtrip", fareFamilyRoundTrip.text)

        fareFamilyTitle = thirdFareFamilyItem.fareFamilyClassTitle
        fareFamilyCabinClass = thirdFareFamilyItem.fareFamilyCabinClass
        fareFamilyDeltaAmount = thirdFareFamilyItem.priceDelta
        fareFamilyRoundTrip = thirdFareFamilyItem.roundTrip

        assertEquals(View.VISIBLE, fareFamilyTitle.visibility)
        assertEquals("Economy Flexible", fareFamilyTitle.text)
        assertEquals(View.VISIBLE, fareFamilyCabinClass.visibility)
        assertEquals("Cabin: Economy", fareFamilyCabinClass.text)
        assertEquals(View.VISIBLE, fareFamilyDeltaAmount.visibility)
        assertEquals("-$271.00", fareFamilyDeltaAmount.text)
        assertEquals(View.VISIBLE, fareFamilyRoundTrip.visibility)
        assertEquals("roundtrip", fareFamilyRoundTrip.text)
    }

    @Test
    fun testNumberOfFareFamilyItemsDisplayed() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)
        val numberOfFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.childCount
        assertEquals(3, numberOfFareFamilyItem)
        for (i in 0..2) {
            val fareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(i) as FareFamilyItemWidget
            assertEquals(View.VISIBLE, fareFamilyItem.visibility)
        }

    }

    @Test
    fun testTravelerTextForMoreThanOneTraveller() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, false))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)
        var firstFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(0) as FareFamilyItemWidget
        assertEquals("4 travelers" , firstFareFamilyItem.travelerTextView.text)
        assertEquals(View.VISIBLE , firstFareFamilyItem.travelerTextView.visibility)
    }

    @Test
    fun testTravelerTextForOneTraveller() {
        Db.setFlightSearchParams(setupFlightSearchParams(1, 0, false))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)
        val firstFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(0) as FareFamilyItemWidget
        assertEquals(View.INVISIBLE , firstFareFamilyItem.travelerTextView.visibility)
    }

    @Test
    fun testRoundTripMessageVisibility() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, false))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)

        for (i in 0..2) {
            val fareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(i) as FareFamilyItemWidget
            assertEquals(View.GONE, fareFamilyItem.roundTrip.visibility)
        }
    }

    @Test
    fun testFareFamilyTripTotalWidget() {
        Db.setFlightSearchParams(setupFlightSearchParams(2, 2, true))
        fareFamilyWidget.viewModel.tripObservable.onNext(flightCreateTripResponse)
        fareFamilyWidget.viewModel.showFareFamilyObservable.onNext(Unit)
        val thirdFareFamilyItem = fareFamilyWidget.fareFamilyRadioGroup.getChildAt(2) as FareFamilyItemWidget
        val tripTotalTextView = fareFamilyWidget.totalPriceWidget.bundleTotalText
        val includesTaxesTextView = fareFamilyWidget.totalPriceWidget.bundleTotalIncludes
        val totalPriceTextView = fareFamilyWidget.totalPriceWidget.bundleTotalPrice

        assertEquals(View.VISIBLE, fareFamilyWidget.totalPriceWidget.visibility)
        assertEquals(View.VISIBLE, tripTotalTextView.visibility)
        assertEquals(View.VISIBLE, includesTaxesTextView.visibility)
        assertEquals(View.VISIBLE, totalPriceTextView.visibility)

        assertEquals("$142.20", totalPriceTextView.text)
        assertEquals("Trip Total for Economy", tripTotalTextView.text)
        thirdFareFamilyItem.fareFamilyRadioButton.performClick()
        assertEquals("$413.20", totalPriceTextView.text)
        assertEquals(View.VISIBLE, totalPriceTextView.visibility)
        assertEquals("Trip Total for Economy Flexible", tripTotalTextView.text)
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
}
