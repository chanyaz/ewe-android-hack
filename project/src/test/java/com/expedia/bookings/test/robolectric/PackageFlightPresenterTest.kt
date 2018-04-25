package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.presenter.PackageFlightPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.BundleTotalPriceTopWidget
import com.expedia.bookings.packages.activity.PackageFlightActivity
import com.expedia.util.Optional
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.expedia.bookings.data.Db
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class PackageFlightPresenterTest {
    private lateinit var presenter: PackageFlightPresenter
    private lateinit var activity: Activity
    lateinit var params: PackageSearchParams
    val context = RuntimeEnvironment.application

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
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
    fun testBundleWidgetTopVisibleOutboundFlights() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        assertEquals(View.VISIBLE, presenter.bundlePriceWidgetTop.visibility)
    }

    @Test
    fun testBundleWidgetTopNotVisibleOutboundFlights() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        assertNull(presenter.findViewById<BundleTotalPriceTopWidget>(R.id.bundle_total_top_view))
    }

    @Test
    fun testBundleWidgetTopVisibleInboundFlights() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        presenter.resultsPresenter.outboundFlightSelectedSubject.onNext(PackageTestUtil.getDummyPackageFlightLeg())

        assertEquals(View.VISIBLE, presenter.bundlePriceWidgetTop.visibility)
    }

    @Test
    fun testBundleWidgetTopNotVisibleInboundFlights() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        presenter.resultsPresenter.outboundFlightSelectedSubject.onNext(PackageTestUtil.getDummyPackageFlightLeg())
        assertNull(presenter.findViewById<BundleTotalPriceTopWidget>(R.id.bundle_total_top_view))
    }

    @Test
    fun testOutboundFlightsToolbarText() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(true)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = getDummyRegionNames()
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Outbound to Bengaluru, India (BLR)", presenter.toolbar.title.toString())
    }

    @Test
    fun testInboundFlightsToolbarText() {
        mockPackageServiceRule.getMIDHotelResponse()
        mockPackageServiceRule.getMIDFlightsResponse()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(false)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = getDummyRegionNames()
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Inbound to Bengaluru, India (BLR)", presenter.toolbar.title.toString())
    }

    @Test
    fun testOutboundFlightsToolbarWithBreadcrumbsText() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
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
    fun testInboundFlightsToolbarWithBreadcrumbsText() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
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
}
