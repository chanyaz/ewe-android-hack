package com.expedia.bookings.presenter.flight

import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.flights.utils.FlightServicesManager
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.data.flights.FlightSearchParams.TripType
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.RewardsUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.WebViewUtils
import com.expedia.ui.HotelActivity
import com.expedia.util.Optional
import com.expedia.bookings.flights.vm.FlightWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class))
class FlightCheckoutViewTest {

    lateinit var activity: Activity
    private lateinit var flightPresenter: FlightPresenter
    private lateinit var flightServices: FlightServices
    private lateinit var flightServicesManager: FlightServicesManager
    private val userAccountRefresherMock = Mockito.mock(UserAccountRefresher::class.java)
    var server: MockWebServer = MockWebServer()
        @Rule get

    var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.trampoline(), "../lib/mocked/templates")
        @Rule get

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        setupDb()
        AbacusTestUtils.resetABTests()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testOpeningOfWebCheckoutViewFromInboundPresenter() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightInboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testOpeningOfWebCheckoutViewFromOutBoundPresenter() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowFlightsWebviewNativeRateDetailsBucketed() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(activity,
                AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter

        assertFalse(flightPresenter.shouldShowWebCheckoutWithoutNativeRateDetails())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowFlightsWebviewNativeRateDetailsControl() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter

        assertTrue(flightPresenter.shouldShowWebCheckoutWithoutNativeRateDetails())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowFlightsWebviewNativeRateDetailsControlNonINPos() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter

        assertFalse(flightPresenter.shouldShowWebCheckoutWithoutNativeRateDetails())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontShowFlightsWebviewAllControl() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        assertFalse(flightPresenter.shouldShowWebCheckoutWithoutNativeRateDetails())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testOpeningOfWebCheckoutViewFromOverviewPresenter() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        val testShowWebviewSubscriber = TestObserver.create<Unit>()
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        val testUrlSubscriber = TestObserver.create<String>()
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOverviewPresenter.viewModel.showWebviewCheckoutObservable.subscribe(testShowWebviewSubscriber)
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)
        flightPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testUrlSubscriber)
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))
        flightPresenter.flightOfferViewModel.flightProductId.onNext("happy_round_trip")

        testUrlSubscriber.assertValues( "about:blank")
        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
        flightPresenter.flightOverviewPresenter.checkoutButton.performClick()

        testShowWebviewSubscriber.assertValue(Unit)
        maskWebCheckoutActivityObservable.assertValue(true)
        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
        assertFalse(flightPresenter.webCheckoutView.webView.canGoBack())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testUpdateFareFamilyClearsWebViewHistory() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()
        val testUrlObserver = TestObserver.create<String>()
        flightPresenter.webCheckoutViewModel.webViewURLObservable.subscribe(testUrlObserver)
        flightPresenter.createTripBuilder = FlightCreateTripParams.Builder()

        flightPresenter.flightOverviewPresenter.fareFamilyCardView.viewModel.updateTripObserver
                .onNext(Pair("test", getFareFamilyDetails(className = "coach")))

        testUrlObserver.assertValue("about:blank")
        assertFalse(flightPresenter.webCheckoutView.webView.canGoBack())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testUpdateFareFamilyClearsWebViewHistoryInEuPos() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckoutInEUPos)
        SettingUtils.save(activity, R.string.PointOfSaleKey, PointOfSaleId.FRANCE.id.toString())
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()
        val testUrlObserver = TestObserver.create<String>()
        flightPresenter.webCheckoutViewModel.webViewURLObservable.subscribe(testUrlObserver)
        flightPresenter.createTripBuilder = FlightCreateTripParams.Builder()

        flightPresenter.flightOverviewPresenter.fareFamilyCardView.viewModel.updateTripObserver
                .onNext(Pair("test", getFareFamilyDetails(className = "coach")))

        testUrlObserver.assertValue("about:blank")
        assertFalse(flightPresenter.webCheckoutView.webView.canGoBack())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testConfirmationUrlForNonINPosBucketedNativeRateDetailsWebview() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))

        val bookingTripIDSubscriber = TestObserver<String>()
        val fetchTripIDSubscriber = TestObserver<String>()
        val webviewVisibilitySubscriber = TestObserver<Boolean>()
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fetchTripIDSubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).showWebViewObservable.subscribe(webviewVisibilitySubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        setupTestToOpenInFlightOutboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")
        bookingTripIDSubscriber.assertValueCount(0)
        fetchTripIDSubscriber.assertValueCount(0)
        webviewVisibilitySubscriber.assertValueCount(0)
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()
        val tripID = "testing-for-confirmation"

        flightPresenter.flightOverviewPresenter.checkoutButton.performClick()
        webviewVisibilitySubscriber.assertValue(true)

        flightPresenter.webCheckoutView.onWebPageStarted(flightPresenter.webCheckoutView.webView, activity.getString(R.string.flight_confirmation_url_tag) + "?tripid=$tripID", null)
        Mockito.verify(userAccountRefresherMock, Mockito.times(1)).forceAccountRefreshForWebView()
        bookingTripIDSubscriber.assertValueCount(1)
        webviewVisibilitySubscriber.assertValues(true, false)

        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fetchTripIDSubscriber.assertValueCount(1)
        fetchTripIDSubscriber.assertValue(tripID)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testClosingWebviewWithWebviewCko() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")
        val testBlankUrlSubscriber = TestObserver.create<String>()
        flightPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testBlankUrlSubscriber)

        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).closeView.onNext(Unit)

        assertTrue(flightPresenter.webCheckoutView.clearHistory)
        testBlankUrlSubscriber.assertValue("about:blank")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testNativeRateDetailsWebviewCkoBlankView() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))
        flightPresenter.flightOverviewPresenter.checkoutButton.performClick()

        assertTrue(flightPresenter.webCheckoutView.loadingOverlay.visibility == View.VISIBLE)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).blankViewObservable.onNext(Unit)

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
        assertTrue(flightPresenter.webCheckoutView.loadingOverlay.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMaskActivityWhenGoingToWebViewFromOutboundPresenter() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)

        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        maskWebCheckoutActivityObservable.assertValue(true)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMaskActivityWhenGoingToWebViewFromInboundPresenter() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)

        setupTestToOpenInFlightInboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        maskWebCheckoutActivityObservable.assertValue(true)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontMaskActivityWhenGoingFromWebViewToOutboundPresenter() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)

        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        flightPresenter.show(flightPresenter.outBoundPresenter)
        flightPresenter.webCheckoutView.viewModel.backObservable.onNext(Unit)
        flightPresenter.show(flightPresenter.outBoundPresenter)

        maskWebCheckoutActivityObservable.assertValues(true, false)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontMaskActivityWhenGoingFromWebViewToInboundPresenter() {
        setPOSToIndia()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)

        setupTestToOpenInFlightInboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        flightPresenter.show(flightPresenter.inboundPresenter)
        flightPresenter.webCheckoutView.viewModel.backObservable.onNext(Unit)
        flightPresenter.show(flightPresenter.inboundPresenter)

        maskWebCheckoutActivityObservable.assertValues(true, false)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testOpeningOfFlightOverviewPresenterFromOutBoundPresenter() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testOpeningOfFlightOverviewPresenterFromInboundPresenter() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightInboundPresenter()

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testProductIdFiresCreateTrip() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val tripResponseSubscriber = TestObserver<TripResponse>()
        val testPerformCreateTripSubscriber = TestObserver<Unit>()
        val testShowLoadingSubscriber = TestObserver<Unit>()
        val testUrlSubscriber = TestObserver<String>()
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
        assertEquals("happy_round_trip", (tripResponseSubscriber.values()[0] as FlightCreateTripResponse).newTrip?.tripId)
        testUrlSubscriber.assertValue(webCheckoutViewModel.endpointProvider.getE3EndpointUrlWithPath("FlightCheckout?tripid=happy_round_trip"))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webViewTripIDOnSuccessfulBooking() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val bookingTripIDSubscriber = TestObserver<String>()
        val fetchTripIDSubscriber = TestObserver<String>()
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fetchTripIDSubscriber)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        setupTestToOpenInFlightOutboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("happy_round_trip")
        bookingTripIDSubscriber.assertValueCount(0)
        fetchTripIDSubscriber.assertValueCount(0)
        val tripID = "testing-for-confirmation"
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()

        flightPresenter.webCheckoutView.onWebPageStarted(flightPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL + "?tripid=$tripID", null)
        Mockito.verify(userAccountRefresherMock, Mockito.times(1)).forceAccountRefreshForWebView()
        bookingTripIDSubscriber.assertValueCount(1)
        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fetchTripIDSubscriber.assertValueCount(1)
        fetchTripIDSubscriber.assertValue(tripID)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testOpeningOfErrorPresenterFromWebCheckoutView() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        flightPresenter.flightCreateTripViewModel.tripParams.onNext(createTripParams("custom_error_create_trip"))
        flightPresenter.flightOfferViewModel.flightProductId.onNext("custom_error_create_trip")

        assertTrue(flightPresenter.errorPresenter.visibility == View.VISIBLE)

        flightPresenter.flightCreateTripViewModel.tripParams.onNext(createTripParams("create_trip_price_increase"))
        flightPresenter.errorPresenter.viewmodel.fireRetryCreateTrip.onNext(Unit)

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFireRetryCreateTripNativeRateDetailsBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()
        val testUrlSubscriber = TestObserver.create<String>()
        flightPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testUrlSubscriber)

        flightPresenter.flightCreateTripViewModel.tripParams.onNext(createTripParams("custom_error_create_trip"))
        flightPresenter.flightOfferViewModel.flightProductId.onNext("custom_error_create_trip")

        testUrlSubscriber.assertValuesAndClear("about:blank")
        assertTrue(flightPresenter.errorPresenter.visibility == View.VISIBLE)

        flightPresenter.flightCreateTripViewModel.tripParams.onNext(createTripParams("create_trip_price_increase"))
        flightPresenter.errorPresenter.viewmodel.fireRetryCreateTrip.onNext(Unit)
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.onNext(true)

        testUrlSubscriber.assertValues("about:blank", "https://www.expedia.com/FlightCheckout?tripid=happy_one_way")
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
        assertFalse(flightPresenter.webCheckoutView.webView.canGoBack())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testSingleFlightOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("flight_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val confirmationPresenter = flightPresenter.confirmationPresenter
        assertEquals("#7238007847306 sent to gbalachandran@expedia.com", flightPresenter.confirmationPresenter.itinNumber.text)
        assertEquals("Las Vegas", confirmationPresenter.destination.text)

        val outboundFlightCard = confirmationPresenter.outboundFlightCard
        assertEquals("SFO to LAS", outboundFlightCard.title.text)
        assertEquals("20:00:00 - 21:33:00 · Nonstop", outboundFlightCard.subTitle.text)

        val inboundFlightCard = confirmationPresenter.inboundFlightCard
        assertTrue(inboundFlightCard.visibility == View.GONE)

        val flightSummary = confirmationPresenter.flightSummary
        assertEquals("1 traveler", flightSummary.numberOfTravelers.text)
        assertEquals("$60.20", flightSummary.tripPrice.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testRoundTripFlightOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("flight_trip_details_multi_segment", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val inboundFlightCard = flightPresenter.confirmationPresenter.inboundFlightCard
        assertTrue(inboundFlightCard.visibility == View.VISIBLE)
        assertEquals("PBI to SFO", inboundFlightCard.title.text)
        assertEquals("08:50:00 - 00:03:00 · 2 Stops", inboundFlightCard.subTitle.text)
        assertEquals("#79010216932 sent to your email.", flightPresenter.confirmationPresenter.itinNumber.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithRewardsConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val testRewardsSubscriber: TestObserver<Optional<String>> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        flightPresenter.confirmationPresenter.viewModel.setRewardsPoints.subscribe(testRewardsSubscriber)

        serviceRule.services!!.getTripDetails("flight_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertEquals("13 points earned", RewardsUtil.buildRewardText(activity,
                testRewardsSubscriber.values()[0].value ?: "",
                ProductFlavorFeatureConfiguration.getInstance(), isFlights = true))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithNoRewardsOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val testRewardsSubscriber: TestObserver<Optional<String>> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        flightPresenter.confirmationPresenter.viewModel.setRewardsPoints.subscribe(testRewardsSubscriber)

        serviceRule.services!!.getTripDetails("flight_trip_with_no_rewards_no_air_attach", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertEquals("", RewardsUtil.buildRewardText(activity,
                testRewardsSubscriber.values()[0].value ?: "",
                ProductFlavorFeatureConfiguration.getInstance(), isFlights = true))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testCrossSellDaysRemainingOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("flight_trip_details_multi_segment", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val hotelCrossSell = flightPresenter.confirmationPresenter.hotelCrossSell
        assertTrue(hotelCrossSell.airAttachCountDownView.visibility == View.VISIBLE)
        assertTrue(hotelCrossSell.airAttachExpirationTodayTextView.visibility == View.GONE)
        assertEquals("2 days", hotelCrossSell.airattachExpirationDaysRemainingTextView.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testHotelCrossSellClickOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()
        val shadowActivity = Shadows.shadowOf(activity)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("flight_trip_details_multi_segment", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        flightPresenter.confirmationPresenter.hotelCrossSell.viewModel.searchParamsObservable.onNext(setupFlightSearchParams())

        flightPresenter.confirmationPresenter.hotelCrossSell.airAttachContainer.callOnClick()

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertTrue(intent.component == ComponentName(activity, HotelActivity::class.java))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithCrossSellExpirationOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("flight_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val hotelCrossSell = flightPresenter.confirmationPresenter.hotelCrossSell
        assertTrue(hotelCrossSell.airAttachCountDownView.visibility == View.GONE)
        assertTrue(hotelCrossSell.airAttachExpirationTodayTextView.visibility == View.VISIBLE)
        assertEquals("", hotelCrossSell.airattachExpirationDaysRemainingTextView.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithNoAirAttachOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("flight_trip_with_no_rewards_no_air_attach", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val hotelCrossSell = flightPresenter.confirmationPresenter.hotelCrossSell
        assertTrue(hotelCrossSell.airAttachCountDownView.visibility == View.GONE)
        assertTrue(hotelCrossSell.airAttachExpirationTodayTextView.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithInsuranceOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()

        serviceRule.services!!.getTripDetails("flight_trip_details", makeItinResponseObserver)

        assertTrue(flightPresenter.confirmationPresenter.tripProtectionLabel.visibility == View.VISIBLE)
        assertTrue(flightPresenter.confirmationPresenter.tripProtectionDivider.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithNoInsuranceOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()

        serviceRule.services!!.getTripDetails("flight_trip_details_multi_segment", makeItinResponseObserver)

        assertTrue(flightPresenter.confirmationPresenter.tripProtectionLabel.visibility == View.GONE)
        assertTrue(flightPresenter.confirmationPresenter.tripProtectionDivider.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testFlightWithNoEmailOnConfirmationFromWebCheckout() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("flight_trip_details_multi_segment", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertEquals("#79010216932 sent to your email.", flightPresenter.confirmationPresenter.itinNumber.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShowBookingSuccessDialogOnItinResponseErrorFinishesActivity() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))

        makeAnItinsCall("should_error")

        assertBookingSuccessDialogDisplayedAndFinishesActivityOnClick()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBookingSuccessDialogDisplayedOnItinResponseContainingErrors() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))

        makeAnItinsCall("error_trip_details_response")

        assertBookingSuccessDialogDisplayedAndFinishesActivityOnClick()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldNotMaskScreenAfterWebCheckoutToNativeConfirmation() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val shouldMaskScreenTestObserver = TestObserver.create<Boolean>()
        flightPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(shouldMaskScreenTestObserver)

        (flightPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("happy_round_trip")
        val tripID = "testing-for-confirmation"
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()
        flightPresenter.webCheckoutView.onWebPageStarted(flightPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL + "?tripid=$tripID", null)

        shouldMaskScreenTestObserver.assertValues(true, false)
    }

    private fun assertBookingSuccessDialogDisplayedAndFinishesActivityOnClick() {
        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertTrue(alertDialog.title.contains("Booking Successful!"))
        assertTrue(alertDialog.message.contains("Please check your email for the itinerary."))

        flightPresenter.bookingSuccessDialog.getButton(Dialog.BUTTON_POSITIVE).performClick()

        val activityShadow = Shadows.shadowOf(activity)
        assertTrue(activityShadow.isFinishing)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShowBookingSuccessDialogDoesNotFinishActivityIfWebCheckoutNotShown() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))

        flightPresenter.bookingSuccessDialog.show()

        flightPresenter.bookingSuccessDialog.getButton(Dialog.BUTTON_POSITIVE).performClick()

        val activityShadow = Shadows.shadowOf(activity)
        assertFalse(activityShadow.isFinishing)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    @Config(qualifiers = "sw600dp")
    fun testUserAgentStringHasTabletInfo() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()

        flightPresenter.webCheckoutView.addNewWebViewToWidget(activity)

        assertEquals("Android " + WebViewUtils.userAgentString + " app.webview.tablet", flightPresenter.webCheckoutView.webView.settings.userAgentString)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testUserAgentStringHasPhoneInfo() {
        setPOSToIndia()
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        flightPresenter.webCheckoutView.addNewWebViewToWidget(activity)

        assertEquals("Android " + WebViewUtils.userAgentString + " app.webview.phone", flightPresenter.webCheckoutView.webView.settings.userAgentString)
    }

    @Test
    fun testShowNativeSignInFromWebViewError() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))
        val testShowNativeObserver = TestObserver.create<Unit>()
        val testShowWebViewObserver = TestObserver.create<Boolean>()
        val testUrlSubscriber = TestObserver.create<String>()
        flightPresenter.webCheckoutViewModel.showWebViewObservable.subscribe(testShowWebViewObserver)
        flightPresenter.webCheckoutViewModel.showNativeSearchObservable.subscribe(testShowNativeObserver)
        flightPresenter.webCheckoutViewModel.webViewURLObservable.subscribe(testUrlSubscriber)
        flightPresenter.show(flightPresenter.flightOverviewPresenter)
        flightPresenter.show(flightPresenter.webCheckoutView)

        flightPresenter.webCheckoutView.goToSearchAndClearWebView()

        testShowNativeObserver.assertValueCount(1)
        testShowWebViewObserver.assertValues(true, false)
        testUrlSubscriber.assertValue("about:blank")
        assertFalse(flightPresenter.webCheckoutView.webView.canGoBack())
        assertTrue(flightPresenter.searchPresenter.visibility == View.VISIBLE)
        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
    }

    @Test
    fun testShowNativeSignInOnBackFromWebViewErrorUrl() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()
        setupTestToOpenInFlightOutboundPresenter()
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))
        val testShowNativeObserver = TestObserver.create<Unit>()
        val testShowWebViewObserver = TestObserver.create<Boolean>()
        val testUrlSubscriber = TestObserver.create<String>()
        flightPresenter.webCheckoutViewModel.showWebViewObservable.subscribe(testShowWebViewObserver)
        flightPresenter.webCheckoutViewModel.showNativeSearchObservable.subscribe(testShowNativeObserver)
        flightPresenter.webCheckoutViewModel.webViewURLObservable.subscribe(testUrlSubscriber)
        flightPresenter.show(flightPresenter.flightOverviewPresenter)
        flightPresenter.show(flightPresenter.webCheckoutView)

        flightPresenter.webCheckoutView.onWebPageStarted(flightPresenter.webCheckoutView.webView, "https://www.expedia.com/FlightCheckoutError", null)
        flightPresenter.webCheckoutView.back()

        testShowNativeObserver.assertValueCount(1)
        testShowWebViewObserver.assertValues(true, false)
        testUrlSubscriber.assertValues("about:blank")
        assertFalse(flightPresenter.webCheckoutView.webView.canGoBack())
        assertTrue(flightPresenter.searchPresenter.visibility == View.VISIBLE)
        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
    }

    @Test
    fun testSearchParamsReachesKrazyglueViewModel() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsKrazyglue)
        createMockFlightServices()
        setFlightPresenterAndFlightServices()

        val testSearchParamsSubscriber = TestObserver<FlightSearchParams>()
        flightPresenter.confirmationPresenter.viewModel.flightSearchParamsObservable.subscribe(testSearchParamsSubscriber)
        val searchParams = setupFlightSearchParams()
        flightPresenter.searchViewModel.searchParamsObservable.onNext(searchParams)

        testSearchParamsSubscriber.assertValueCount(1)
        testSearchParamsSubscriber.assertValue(searchParams)
    }

    private fun setFlightPresenterAndFlightServices() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        flightPresenter.flightServicesManager = flightServicesManager
        (flightPresenter.webCheckoutView.viewModel as FlightWebCheckoutViewViewModel).flightCreateTripViewModel.flightServices = flightServices
    }

    private fun setupTestToOpenInFlightInboundPresenter() {
        flightPresenter.flightOfferViewModel.tripTypeSearchSubject.onNext(TripType.ONE_WAY)
        flightPresenter.show(flightPresenter.inboundPresenter)
    }

    private fun setupTestToOpenInFlightOutboundPresenter() {
        flightPresenter.flightOfferViewModel.tripTypeSearchSubject.onNext(TripType.RETURN)
        flightPresenter.show(flightPresenter.outBoundPresenter)
    }

    private fun setPOSToIndia() {
        val pointOfSale = PointOfSaleId.INDIA
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
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

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null, null, null, null)
    }

    private fun makeAnItinsCall(tripID: String) {
        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = flightPresenter.makeNewItinResponseObserver()
        flightPresenter.show(flightPresenter.webCheckoutView)
        flightPresenter.bookingSuccessDialog.show()
        flightPresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails(tripID, makeItinResponseObserver)
        testObserver.assertValueCount(0)
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
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline())
        flightServicesManager = FlightServicesManager(flightServices)
    }

    private fun createTripParams(productKey: String): FlightCreateTripParams {
        val builder = FlightCreateTripParams.Builder()
        return builder.productKey(productKey).build()
    }

    private fun getFareFamilyDetails(className: String): FlightTripResponse.FareFamilyDetails {
        return FlightTripResponse.FareFamilyDetails(className, className, className,
                Money("210.00", "USD"), Money(1, "USD"), true, HashMap())
    }
}
