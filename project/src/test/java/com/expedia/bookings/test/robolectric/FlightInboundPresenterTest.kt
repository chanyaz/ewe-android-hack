package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.presenter.flight.FlightInboundPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.FlightListAdapter
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
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(application = ExpediaBookingApp::class)
class FlightInboundPresenterTest {
    lateinit private var service: FlightServices
    lateinit private var activity: Activity
    lateinit private var flightInboundPresenter: FlightInboundPresenter

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()

        val server: MockWebServer = MockWebServer()
        val logger = HttpLoggingInterceptor()
        val interceptor = MockInterceptor()
        service = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        flightInboundPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_inbound_stub, null) as FlightInboundPresenter
    }

    @Test
    fun widgetVisibilityTest() {
        val toolbar = flightInboundPresenter.findViewById(R.id.flights_toolbar) as Toolbar
        assertEquals(toolbar.visibility, View.VISIBLE)
    }

    @Test
    fun testFlightInboundTitle() {
        flightInboundPresenter.toolbarViewModel.refreshToolBar.onNext(true)
        flightInboundPresenter.toolbarViewModel.isOutboundSearch.onNext(false)
        flightInboundPresenter.toolbarViewModel.travelers.onNext(1)
        flightInboundPresenter.toolbarViewModel.date.onNext(LocalDate.now())
        flightInboundPresenter.toolbarViewModel.city.onNext("Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India")
        assertEquals("Select return flight", flightInboundPresenter.toolbar.title.toString())
    }

    @Test
    fun testResultsArePopulated() {
        val isOutboundSearchSubscriber = TestSubscriber<Boolean>()
        flightInboundPresenter.toolbarViewModel.isOutboundSearch.subscribe(isOutboundSearchSubscriber)
        invokeSetupComplete()

        isOutboundSearchSubscriber.assertValue(false)
        assertNotNull(flightInboundPresenter.resultsPresenter.recyclerView.adapter)
    }

    @Test
    fun testSearchParamsObservable() {
        invokeSetupComplete()
        flightInboundPresenter.flightOfferViewModel.searchParamsObservable.onNext(getSearchParams(true).build())
        assertEquals(View.GONE, flightInboundPresenter.resultsPresenter.filterButton.visibility)
    }

    @Test
    fun testLobAndIsInboundFlightPresenter() {
        assertEquals(LineOfBusiness.FLIGHTS_V2, flightInboundPresenter.getLineOfBusiness())
        assertEquals(false, flightInboundPresenter.isOutboundResultsPresenter())
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
        assertNotNull(flightInboundPresenter.baggageFeeInfoWebView)
        assertNotNull(flightInboundPresenter.baggageFeeInfoWebView.viewModel)
    }

    @Test
    fun testFilterCountObserver() {
        val filterNoText = flightInboundPresenter.resultsPresenter.filterButton.findViewById(R.id.filter_number_text) as TextView
        val filterIcon = flightInboundPresenter.resultsPresenter.filterButton.findViewById(R.id.filter_icon)
        flightInboundPresenter.filterCountObserver.onNext(2)

        assertEquals("2", filterNoText.text)
        assertEquals(View.VISIBLE, filterNoText.visibility)
        assertEquals(View.GONE, filterIcon.visibility)
        flightInboundPresenter.filterCountObserver.onNext(0)
        assertEquals(View.GONE, filterNoText.visibility)
        assertEquals(View.VISIBLE, filterIcon.visibility)
    }

    @Test
    fun testToolbar() {
        assertNotNull(flightInboundPresenter.toolbarViewModel)
        assertEquals(flightInboundPresenter.toolbar.navigationIcon, flightInboundPresenter.navIcon)
        assertEquals(flightInboundPresenter.toolbar.background, ContextCompat.getDrawable(activity, R.color.packages_primary_color))

        flightInboundPresenter.toolbarViewModel.menuVisibilitySubject.onNext(true)
        assertTrue(flightInboundPresenter.menuSearch.isVisible)
        flightInboundPresenter.toolbarViewModel.menuVisibilitySubject.onNext(false)
        assertFalse(flightInboundPresenter.menuSearch.isVisible)

        invokeSetupComplete()
        assertFalse(flightInboundPresenter.toolbarViewModel.isOutboundSearch.value)

        flightInboundPresenter.toolbarViewModel.refreshToolBar.onNext(true)
        flightInboundPresenter.toolbarViewModel.refreshToolBar.onNext(true)
        flightInboundPresenter.toolbarViewModel.isOutboundSearch.onNext(false)
        flightInboundPresenter.toolbarViewModel.travelers.onNext(1)
        val currentTime = LocalDate.now()
        flightInboundPresenter.toolbarViewModel.date.onNext(currentTime)
        flightInboundPresenter.toolbarViewModel.city.onNext("Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India")

        val travelDate = DateFormatUtils.formatLocalDateToShortDayAndDate(currentTime)
        assertEquals(View.VISIBLE,flightInboundPresenter.toolbar.visibility)
        assertEquals(travelDate+", 1 Traveler",flightInboundPresenter.toolbar.subtitle)
        assertEquals("Select return flight", flightInboundPresenter.toolbar.title.toString())

        flightInboundPresenter.toolbarViewModel.travelers.onNext(2)
        assertEquals(View.VISIBLE,flightInboundPresenter.toolbar.visibility)
        assertEquals("Select return flight", flightInboundPresenter.toolbar.title.toString())
        assertEquals(travelDate+", 2 Travelers",flightInboundPresenter.toolbar.subtitle)
    }

    @Test
    fun validateBaggageFeeAndSelectFlightButton() {
        invokeSetupComplete()
        assertEquals(View.VISIBLE, flightInboundPresenter.overviewPresenter.selectFlightButton.visibility)
        assertTrue(flightInboundPresenter.overviewPresenter.selectFlightButton.isEnabled)
        assertEquals("Select this Flight", flightInboundPresenter.overviewPresenter.selectFlightButton.text)

        assertEquals(View.VISIBLE, flightInboundPresenter.overviewPresenter.showBaggageFeesButton.visibility)
        assertTrue(flightInboundPresenter.overviewPresenter.showBaggageFeesButton.isEnabled)
        assertEquals("Baggage fee info", flightInboundPresenter.overviewPresenter.showBaggageFeesButton.text)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightUrgencyMessage() {
        val flightLeg = setupFlightLeg()

        flightInboundPresenter.overviewPresenter.urgencyMessagingText
        val urgencyTextView = flightInboundPresenter.overviewPresenter.findViewById(R.id.flight_overview_urgency_messaging)
        flightInboundPresenter.overviewPresenter.vm.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 1
        flightLeg.flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightLeg.flightSegments.add(createFlightSegment("San Francisco", "SFO", "Honolulu", "HNL", 1, 34))
        flightLeg.flightSegments.add(createFlightSegment("Honolulu", "HNL", "Tokyo", "NRT", 1, 34))
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("1 seat left, $646.80", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)

        flightInboundPresenter.overviewPresenter.vm.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 2
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("2 seats left, $646.80 per person", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)

        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 6
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.80 per person", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)

        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 0
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.80 per person", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)

        flightInboundPresenter.overviewPresenter.vm.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 3
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("3 seats left, $646.80", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)

        flightInboundPresenter.overviewPresenter.vm.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 2
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("2 seats left, $646.80 per person", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)

        flightInboundPresenter.overviewPresenter.vm.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 6
        flightInboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.80 per person", flightInboundPresenter.overviewPresenter.vm.urgencyMessagingSubject.value)
        assertEquals(View.VISIBLE, urgencyTextView.visibility)
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
        flightInboundPresenter.resultsPresenter.setAdapter(flightListAdapter)

        Db.setFlightSearchParams(flightSearchParams)
        val flightLegs = ArrayList<FlightLeg>()
        flightInboundPresenter.resultsPresenter.resultsViewModel.flightResultsObservable.onNext(flightLegs)
        flightInboundPresenter.overviewPresenter.vm.numberOfTravelers.subscribe(travellerCountSubscriber)
    }

    private fun invokeSetupComplete() {
        flightInboundPresenter.flightOfferViewModel = FlightOffersViewModel(activity, service)
        flightInboundPresenter.setupComplete()
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