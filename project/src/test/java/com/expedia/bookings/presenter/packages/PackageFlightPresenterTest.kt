package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.activity.PackageFlightActivity
import com.expedia.bookings.packages.presenter.PackageFlightPresenter
import com.expedia.bookings.packages.widget.SlidingBundleWidget
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.flights.DockedOutboundFlightSelectionView
import com.expedia.util.Optional
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageFlightPresenterTest {
    private lateinit var presenter: PackageFlightPresenter
    private lateinit var activity: Activity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var params: PackageSearchParams
    val context = RuntimeEnvironment.application

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    val thrown: ExpectedException = ExpectedException.none()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testFlightsSortedByPriceByDefault() {
        mockPackageServiceRule.getMIDFlightsResponse()

        presenter = getPackageFlightPresenter()
        val testSubscriber = TestObserver<List<FlightLeg>>()
        presenter.resultsPresenter.resultsViewModel.flightResultsObservable.subscribe(testSubscriber)
        val testSubscriberResult = testSubscriber.values()[0]

        for (i in 2..testSubscriberResult.size - 1) {
            val current = testSubscriberResult[i].packageOfferModel.price.packageTotalPrice.amount
            val previous = testSubscriberResult[i - 1].packageOfferModel.price.packageTotalPrice.amount
            assertTrue(current.compareTo(previous) >= 0, "Expected $current >= $previous")
        }
    }

    @Test
    fun testOutboundFlightResponseLoadedFromResponseStaticFile() {
        val expectedPackageSearchResponse = mockPackageServiceRule.getMIDFlightsResponse()
        val latch = CountDownLatch(1)
        PackageResponseUtils.savePackageResponse(context, expectedPackageSearchResponse, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE, { _ ->
            latch.countDown()
        })
        latch.await(2, TimeUnit.SECONDS)
        val outboundFlight = PackageTestUtil.getDummyPackageFlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)

        fireActivityIntent(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)
        assertEquals(expectedPackageSearchResponse, Db.getPackageResponse())
    }

    @Test
    fun testInboundFlightResponseLoadedFromResponseStaticFile() {
        val expectedPackageSearchResponse = mockPackageServiceRule.getMIDFlightsResponse()
        val latch = CountDownLatch(1)
        PackageResponseUtils.savePackageResponse(context, expectedPackageSearchResponse, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE, { _ ->
            latch.countDown()
        })
        latch.await(2, TimeUnit.SECONDS)
        val outboundFlight = PackageTestUtil.getDummyPackageFlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)
        Db.setPackageFlightBundle(outboundFlight, PackageTestUtil.getDummyPackageFlightLeg())

        fireActivityIntent(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)
        assertEquals(expectedPackageSearchResponse, Db.getPackageResponse())
    }

    private fun fireActivityIntent(requestConstant: String) {
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.putExtra(requestConstant, true)
        Robolectric.buildActivity(PackageFlightActivity::class.java, intent).create().get()
    }

    @Test
    fun testOutboundFlightsToolbarText() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(true)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = getDummyRegionNames()
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Step 2: Flight to Bengaluru", presenter.toolbar.title.toString())
    }

    @Test
    fun testInboundFlightsToolbarText() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(false)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = getDummyRegionNames()
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Step 3: Flight to Bengaluru", presenter.toolbar.title.toString())
    }

    @Test
    fun testPaymentLegalMessageOnResultsPage() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json")
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        assertEquals("There may be an additional fee based on your payment method.",
                presenter.resultsPresenter.getAirlinePaymentFeesTextView().text)
        assertEquals(View.VISIBLE, presenter.resultsPresenter.getAirlinePaymentFeesTextView().visibility)
    }

    @Test
    fun testCustomExceptionWasThrownWhenTransitionIsNotSet() {
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        thrown.expect(PackageFlightPresenter.PackageFlightMissingTransitionException::class.java)
        thrown.expectMessage("No Transition defined for Test screen 1 to Test screen 2")
        presenter.getTransition("Test screen 1", "Test screen 2")
    }

    @Test
    fun testTrackShowBaggageFee() {
        val testSubscriber = TestObserver<String>()
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        presenter.detailsPresenter.baggageFeeShowSubject.subscribe(testSubscriber)
        presenter.detailsPresenter.baggageFeeShowSubject.onNext("https://www.expedia.com/BaggageFees")

        assertEquals(View.VISIBLE, presenter.detailsPresenter.selectFlightButton.visibility)
        assertEquals("Select this flight", presenter.detailsPresenter.selectFlightButton.text)
        OmnitureTestUtils.assertLinkTracked("Flight Baggage Fee", "App.Package.Flight.Search.BaggageFee", mockAnalyticsProvider)
    }

    @Test
    fun testFlightOverviewSelectedWhenInbound() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        val params = PackageTestUtil.getMIDPackageSearchParams()
        params.currentFlights = arrayOf("leg1", "leg2")
        Db.setPackageParams(params)
        val flightLeg = PackageTestUtil.getDummyPackageFlightLeg()
        presenter.detailsPresenter.vm.selectedFlightClickedSubject.onNext(flightLeg)
        assertEquals(flightLeg.legId, Db.sharedInstance.packageParams.currentFlights[1])
        assertEquals(flightLeg.packageOfferModel.price, Db.getPackageResponse().getCurrentOfferPrice())
    }

    @Test
    fun testFlightOverviewSelectedWhenOutbound() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        val params = PackageTestUtil.getMIDPackageSearchParams()
        params.currentFlights = arrayOf("leg1", "leg2")
        Db.setPackageParams(params)
        val flightLeg = PackageTestUtil.getDummyPackageFlightLeg()
        flightLeg.outbound = true
        presenter.detailsPresenter.vm.selectedFlightClickedSubject.onNext(flightLeg)
        assertEquals(flightLeg.legId, Db.sharedInstance.packageParams.currentFlights[0])
        assertEquals(flightLeg.packageOfferModel.price, Db.getPackageResponse().getCurrentOfferPrice())
    }

    private fun getDummyRegionNames(): SuggestionV4.RegionNames {
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "Bengaluru, India (BLR - Kempegowda Intl.)"
        regionName.displayName = "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India"
        return regionName
    }

    private fun getPackageFlightPresenter(): PackageFlightPresenter {
        return LayoutInflater.from(activity).inflate(R.layout.package_flight_activity, null, false)
                as PackageFlightPresenter
    }

    @Test
    fun testDisableSlidingWidgetWhenDisabled() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        presenter.overviewTransition.startTransition(true)
        assertEquals(false, presenter.bundleSlidingWidget.bundlePriceWidget.isClickable)
    }

    @Test
    fun testDisableSlidingWidgetWhenEnabled() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        presenter.overviewTransition.startTransition(false)
        assertEquals(true, presenter.bundleSlidingWidget.bundlePriceWidget.isClickable)
    }

    @Test
    fun testPresenterIsNotSlidingBundleWidget() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        assertEquals(false, presenter.isShowingBundle())
    }

    @Test
    fun testUpdateOverviewAnimationDuration() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        presenter.updateOverviewAnimationDuration(50)
        assertEquals(50, presenter.resultsToOverview.animationDuration)
    }

    @Test
    fun testBundlePriceWidgetClick() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        presenter.bundleSlidingWidget.bundlePriceWidget.performClick()
        OmnitureTestUtils.assertLinkTracked("Bundle Widget Tap", "App.Package.BundleWidget.Tap", mockAnalyticsProvider)
        assertEquals(SlidingBundleWidget::class.java.name, presenter.currentState)
        assertEquals(true, presenter.isShowingBundle())
    }

    @Test
    fun testTrackFlightSortFilterLoad() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        presenter = getPackageFlightPresenter()
        presenter.trackFlightSortFilterLoad()
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTrackedNumTimes("App.Package.Flight.Search.Filter", OmnitureMatchers.withEvars(controlEvar), 1, mockAnalyticsProvider)
    }

    @Test
    fun testOutboundDockedFlightSelection() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        val outboundFlight = PackageTestUtil.getDummyPackageFlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)
        val params = PackageTestUtil.getMIDPackageSearchParams()
        Db.setPackageParams(params)
        presenter = getPackageFlightPresenter()
        assertEquals(DockedOutboundFlightSelectionView::class.java, presenter.resultsPresenter.dockedOutboundFlightSelection::class.java)
    }

    @Test
    fun testMoveBestFlightToFirstPlace() {
        val testSubscriber = TestObserver<List<FlightLeg>>()
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        val flightLeg = Db.getPackageResponse().getFlightLegs().get(2)
        flightLeg.isBestFlight = true
        presenter = getPackageFlightPresenter()
        presenter.resultsPresenter.resultsViewModel.flightResultsObservable.subscribe(testSubscriber)
        getPackageFlightPresenter()
        val flightLegs = testSubscriber.values()[0] as List<FlightLeg>
        assertEquals(true, flightLegs[0].isBestFlight)
    }
}
