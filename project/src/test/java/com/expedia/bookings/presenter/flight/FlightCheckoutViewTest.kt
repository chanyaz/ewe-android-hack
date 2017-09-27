package com.expedia.bookings.presenter.flight

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.vm.FlightWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Rule
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class))
class FlightCheckoutViewTest {

    lateinit var activity: Activity
    lateinit var flightPresenter: FlightPresenter
    lateinit private var flightServices: FlightServices
    val userAccountRefresherMock = Mockito.mock(UserAccountRefresher::class.java)
    var server: MockWebServer = MockWebServer()
        @Rule get

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        setupDb()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfWebCheckoutViewFromInboundPresenter() {
        setPOSToIndia()
        turnOnABTestAndFeatureToggle()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightInboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfWebCheckoutViewFromOutBoundPresenter() {
        setPOSToIndia()
        turnOnABTestAndFeatureToggle()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfFlightOverviewPresenterFromOutBoundPresenter() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfFlightOverviewPresenterFromInboundPresenter() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightInboundPresenter()

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testProductIdFiresCreateTrip() {
        setPOSToIndia()
        turnOnABTestAndFeatureToggle()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        val testPerformCreateTripSubscriber = TestSubscriber<Unit>()
        val testShowLoadingSubscriber = TestSubscriber<Unit>()
        val testUrlSubscriber = TestSubscriber<String>()
        val webCheckoutViewModel = flightPresenter.webCheckoutView.viewModel as FlightWebCheckoutViewViewModel
        webCheckoutViewModel.flightCreateTripViewModel.createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)
        webCheckoutViewModel.flightCreateTripViewModel.performCreateTrip.subscribe(testPerformCreateTripSubscriber)
        webCheckoutViewModel.showLoadingObservable.subscribe(testShowLoadingSubscriber)
        webCheckoutViewModel.webViewURLObservable.subscribe(testUrlSubscriber)

        setupTestToOpenInFlightOutboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("happy_round_trip")
        testShowLoadingSubscriber.assertValueCount(1)
        testPerformCreateTripSubscriber.assertValueCount(1)
        tripResponseSubscriber.assertValueCount(1)
        assertEquals("happy_round_trip", (tripResponseSubscriber.onNextEvents[0] as FlightCreateTripResponse).newTrip?.tripId)
        testUrlSubscriber.assertValue("${PointOfSale.getPointOfSale().flightsWebCheckoutUrl}?tripid=happy_round_trip")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun webViewTripIDOnSuccessfulBooking() {
        setPOSToIndia()
        turnOnABTestAndFeatureToggle()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val bookingTripIDSubscriber = TestSubscriber<String>()
        val fectchTripIDSubscriber = TestSubscriber<String>()
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fectchTripIDSubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        setupTestToOpenInFlightOutboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("happy_round_trip")
        bookingTripIDSubscriber.assertValueCount(0)
        fectchTripIDSubscriber.assertValueCount(0)
        val tripID = "testing-for-confirmation"
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()

        flightPresenter.webCheckoutView.onWebPageStarted(flightPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL+ "?tripid=$tripID", null)
        Mockito.verify(userAccountRefresherMock, Mockito.times(1)).forceAccountRefreshForWebView()
        bookingTripIDSubscriber.assertValueCount(1)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fectchTripIDSubscriber.assertValueCount(1)
        fectchTripIDSubscriber.assertValue(tripID)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    private fun setFlightPresenterAndFlightServices() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        flightPresenter.flightServices = flightServices
        (flightPresenter.webCheckoutView.viewModel as FlightWebCheckoutViewViewModel).flightCreateTripViewModel.flightServices = flightServices
    }

    private fun setupTestToOpenInFlightInboundPresenter() {
        flightPresenter.flightOfferViewModel.isRoundTripSearchSubject.onNext(false)
        flightPresenter.show(flightPresenter.inboundPresenter)
    }

    private fun setupTestToOpenInFlightOutboundPresenter() {
        flightPresenter.flightOfferViewModel.isRoundTripSearchSubject.onNext(true)
        flightPresenter.show(flightPresenter.outBoundPresenter)
    }

    private fun setPOSToIndia() {
        val pointOfSale = PointOfSaleId.INDIA
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun turnOnABTestAndFeatureToggle() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        SettingUtils.save(activity.applicationContext, R.string.preference_show_flights_checkout_webview, true)
    }

    private fun setupDb() {
        Db.setFlightSearchParams(setupFlightSearchParams())
    }

    private fun setupFlightSearchParams(): FlightSearchParams {
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

        val arrivalSuggestion = SuggestionV4()
        arrivalSuggestion.gaiaId = "5678"
        val arrivalRegionNames = SuggestionV4.RegionNames()
        arrivalRegionNames.displayName = "Los Angeles"
        arrivalRegionNames.shortName = "LAX"
        arrivalRegionNames.fullName = "LAX - Los Angeles"
        arrivalSuggestion.regionNames = arrivalRegionNames

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        childList.add(4)
        val checkIn = LocalDate().plusDays(2)
        val checkOut = LocalDate().plusDays(3)

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null,null)
    }

    private fun createMockFlightServices() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        flightServices = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

}