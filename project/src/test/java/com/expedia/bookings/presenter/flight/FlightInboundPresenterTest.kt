package com.expedia.bookings.presenter.flight

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import com.expedia.vm.flights.FlightOffersViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(application = ExpediaBookingApp::class)
class FlightInboundPresenterTest {
    lateinit var widget: FlightInboundPresenter
    lateinit var activity: Activity
    lateinit var service: FlightServices

    @Before
    fun before() {
        createSystemUnderTest()
    }

    private fun createSystemUnderTest() {
        val server: MockWebServer = MockWebServer()
        val logger = HttpLoggingInterceptor()
        val interceptor = MockInterceptor()
        service = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())

        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_inbound_stub, null) as FlightInboundPresenter
    }

    @Test
    fun testSetUpComplete() {
        val travellerSubscriber = invokeSetupComplete()

        travellerSubscriber.assertValue(false)
        assertNotNull(widget.resultsPresenter.recyclerView.adapter)
    }

    @Test
    fun testIsInboundFlightPresenter() {
        assertEquals(false, widget.isOutboundResultsPresenter())
    }

    @Test
    fun testSearchParamsObservable() {
        invokeSetupComplete()
        widget.flightOfferViewModel.searchParamsObservable.onNext(getSearchParams(true).build())
        assertEquals(View.GONE, widget.resultsPresenter.filterButton.visibility)
    }

    @Test
    fun testLineOfBusiness() {
        assertEquals(LineOfBusiness.FLIGHTS_V2, widget.getLineOfBusiness())
    }

    @Test
    fun testNumberOfTravelers() {
        invokeSetupComplete()

        var flightSearchParams = setupFlightSearchParams(0, 2)
        var travellerCountSubscriber = TestSubscriber<Int>()
        prepareFlightResultObservables(flightSearchParams, travellerCountSubscriber)
        travellerCountSubscriber.assertValue(2)

        flightSearchParams = setupFlightSearchParams(1, 2)
        travellerCountSubscriber = TestSubscriber<Int>()
        prepareFlightResultObservables(flightSearchParams, travellerCountSubscriber)
        travellerCountSubscriber.assertValue(3)
    }

    @Test
    fun testBaggageFeesWebView() {
        assertNotNull(widget.baggageFeeInfoWebView)
        assertNotNull(widget.baggageFeeInfoWebView.viewModel)
    }

    @Test
    fun testFilterCountObserver() {
        val filterNoText = widget.resultsPresenter.filterButton.findViewById(R.id.filter_number_text) as TextView
        val filterIcon = widget.resultsPresenter.filterButton.findViewById(R.id.filter_icon)
        widget.filterCountObserver.onNext(2)

        assertEquals("2", filterNoText.text)
        assertEquals(View.VISIBLE, filterNoText.visibility)
        assertEquals(View.GONE, filterIcon.visibility)
        widget.filterCountObserver.onNext(0)
        assertEquals(View.GONE, filterNoText.visibility)
        assertEquals(View.VISIBLE, filterIcon.visibility)
    }

    @Test
    fun testToolbar() {
        assertNotNull(widget.toolbarViewModel)
        assertEquals(widget.toolbar.navigationIcon, widget.navIcon)
        assertEquals(widget.toolbar.background, ContextCompat.getDrawable(activity, R.color.packages_primary_color))

        widget.toolbarViewModel.menuVisibilitySubject.onNext(true)
        assertTrue(widget.menuSearch.isVisible)

        widget.toolbarViewModel.menuVisibilitySubject.onNext(false)
        assertFalse(widget.menuSearch.isVisible)

        invokeSetupComplete()
        assertFalse(widget.toolbarViewModel.isOutboundSearch.value)
    }

    @Test
    fun validateBaggageFeeAndSelectFlightButton() {
        invokeSetupComplete()
        assertEquals(View.VISIBLE, widget.overviewPresenter.selectFlightButton.visibility)
        assertTrue(widget.overviewPresenter.selectFlightButton.isEnabled)
        assertEquals("Select this Flight", widget.overviewPresenter.selectFlightButton.text)

        assertEquals(View.VISIBLE, widget.overviewPresenter.showBaggageFeesButton.visibility)
        assertTrue(widget.overviewPresenter.showBaggageFeesButton.isEnabled)
        assertEquals("Baggage fee info", widget.overviewPresenter.showBaggageFeesButton.text)
    }

    @Test
    fun testFlightUrgencyMessage() {
        val flightLeg = setupFlightLeg()

        widget.overviewPresenter.urgencyMessagingText
        widget.overviewPresenter.vm.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 1
        flightLeg.flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightLeg.flightSegments.add(createFlightSegment("San Francisco", "SFO", "Honolulu", "HNL", 1, 34))
        flightLeg.flightSegments.add(createFlightSegment("Honolulu", "HNL", "Tokyo", "NRT", 1, 34))
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("1 seat left, $646.80", widget.overviewPresenter.vm.urgencyMessagingSubject.value)

        widget.overviewPresenter.vm.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 2
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("2 seats left, $646.80 per person", widget.overviewPresenter.vm.urgencyMessagingSubject.value)

        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 6
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.80 per person", widget.overviewPresenter.vm.urgencyMessagingSubject.value)

        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 0
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.80 per person", widget.overviewPresenter.vm.urgencyMessagingSubject.value)

        widget.overviewPresenter.vm.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 3
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("3 seats left, $646.80", widget.overviewPresenter.vm.urgencyMessagingSubject.value)

        widget.overviewPresenter.vm.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 2
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("2 seats left, +$646.80 per person", widget.overviewPresenter.vm.urgencyMessagingSubject.value)

        widget.overviewPresenter.vm.numberOfTravelers.onNext(6)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 10
        widget.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        val urgencyTextView = widget.overviewPresenter.findViewById(R.id.flight_overview_urgency_messaging)
        assertEquals(View.INVISIBLE,urgencyTextView.visibility)
    }

    @Test
    fun testFlightSegment() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightPremiumClass, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        widget.overviewPresenter.flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(getFlightSegmentBreakdownList())
        val textView = widget.overviewPresenter.flightSegmentWidget.findViewById(R.id.flight_segment_layover_in) as TextView
        assertEquals("Swapnil", textView.text)
    }

    private fun getFlightSegmentBreakdownList(): List<FlightSegmentBreakdown> {
        val flightSegment = createFlightSegment("coach")
        val breakdown = FlightSegmentBreakdown(flightSegment, false, true)
        var list: ArrayList<FlightSegmentBreakdown> = ArrayList()
        list.add(breakdown)
        return list.toList()
    }

    private fun createFlightSegment(seatClass: String): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.flightNumber = "51"
        airlineSegment.airplaneType = "Airbus A320"
        airlineSegment.carrier = "Virgin America"
        airlineSegment.operatingAirlineCode = ""
        airlineSegment.operatingAirlineName = ""
        airlineSegment.departureDateTimeISO = ""
        airlineSegment.arrivalDateTimeISO = ""
        airlineSegment.departureCity = "San Francisco"
        airlineSegment.arrivalCity = "Honolulu"
        airlineSegment.departureAirportCode = "SFO"
        airlineSegment.arrivalAirportCode = "SEA"
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = 0
        airlineSegment.layoverDurationMinutes = 0
        airlineSegment.elapsedDays = 0
        airlineSegment.seatClass = seatClass
        airlineSegment.bookingCode = "O"
        return airlineSegment
    }

    private fun createFlightSegment(departureCity: String, departureAirport: String, arrivalCity: String, arrivalAirport: String, layoverHrs: Int, layoverMins: Int): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.flightNumber = "51"
        airlineSegment.airplaneType = "Airbus A320"
        airlineSegment.carrier = "Virgin America"
        airlineSegment.operatingAirlineCode = ""
        airlineSegment.operatingAirlineName = ""
        airlineSegment.departureDateTimeISO = ""
        airlineSegment.arrivalDateTimeISO = ""
        airlineSegment.departureCity = departureCity
        airlineSegment.arrivalCity = arrivalCity
        airlineSegment.departureAirportCode = departureAirport
        airlineSegment.arrivalAirportCode = arrivalAirport
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = layoverHrs
        airlineSegment.layoverDurationMinutes = layoverMins
        airlineSegment.elapsedDays = 0
        airlineSegment.seatClass = "coach"
        airlineSegment.bookingCode = "O"
        return airlineSegment
    }

    private fun setupFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.legId = "testLegId"
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.80"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.80"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.formattedPrice = "$646.80"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.setAmount("646.80")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.currencyCode = "USD"
        return flightLeg
    }

    private fun getSearchParams(roundTrip: Boolean): FlightSearchParams.Builder {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
        }
        return paramsBuilder
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }

    private fun prepareFlightResultObservables(flightSearchParams: FlightSearchParams, travellerCountSubscriber: TestSubscriber<Int>) {
        val flightSelectedSubject = PublishSubject.create<FlightLeg>()
        val isRoundTripSubject = BehaviorSubject.create<Boolean>()
        val flightListAdapter = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject)
        widget.resultsPresenter.setAdapter(flightListAdapter)

        Db.setFlightSearchParams(flightSearchParams)
        val flightLegs = ArrayList<FlightLeg>()
        widget.resultsPresenter.resultsViewModel.flightResultsObservable.onNext(flightLegs)
        widget.overviewPresenter.vm.numberOfTravelers.subscribe(travellerCountSubscriber)
    }

    private fun invokeSetupComplete(): TestSubscriber<Boolean> {
        val travellerSubscriber = TestSubscriber<Boolean>()

        widget.toolbarViewModel.isOutboundSearch.subscribe(travellerSubscriber)
        widget.flightOfferViewModel = FlightOffersViewModel(activity, service)
        widget.setupComplete()
        return travellerSubscriber
    }

    private fun setupFlightSearchParams(adultCount: Int, childCount: Int): FlightSearchParams {
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
        for (childIndex in 1..childCount) {
            childList.add(2)
        }

        val checkIn = LocalDate().plusDays(2)
        val checkOut = LocalDate().plusDays(3)

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, adultCount, childList, false, null, null, null)
    }
}