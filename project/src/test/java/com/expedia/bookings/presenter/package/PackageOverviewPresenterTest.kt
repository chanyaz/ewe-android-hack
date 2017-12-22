package com.expedia.bookings.presenter.`package`

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.PackageWebCheckoutViewViewModel
import com.expedia.vm.packages.BundleOverviewViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertTrue
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.test.robolectric.PackageTestUtil

@RunWith(RobolectricRunner :: class)
class PackageOverviewPresenterTest {
    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var overviewPresenter: PackageOverviewPresenter

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultPackageComponents()
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalTextBeforeCreateTripForJapanPOS() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.JAPAN)
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter

        assertEquals("Trip total (with taxes & fee)", overviewPresenter.totalPriceWidget.bundleTotalText.text.toString())
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutViewOpenedWithMIDCheckoutEnabled() {
        setupOverviewPresenter()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)

        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        overviewPresenter.checkoutButton.performClick()

        assertEquals(View.VISIBLE, overviewPresenter.webCheckoutView.visibility)
        assertEquals(View.GONE, overviewPresenter.getCheckoutPresenter().visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutViewOpenedWithMIDCheckoutDisabled() {
        setUpPackageDb()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.checkoutButton.performClick()

        assertEquals(View.VISIBLE, overviewPresenter.getCheckoutPresenter().visibility)
        assertEquals(View.GONE, overviewPresenter.webCheckoutView.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageBundleWidgetTitleTextFromMultiItemResponse() {
        val stepOneTestSubscriber = TestSubscriber<String>()
        val stepTwoTestSubscriber = TestSubscriber<String>()
        val stepThreeTestSubscriber = TestSubscriber<String>()

        setUpPackageDb()
        val hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        setupOverviewPresenter()
        overviewPresenter.bundleWidget.viewModel.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        overviewPresenter.bundleWidget.viewModel.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        overviewPresenter.bundleWidget.viewModel.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().multiItemResponseSubject.onNext(MultiItemApiCreateTripResponse())

        assertEquals("Hotel in Detroit - 1 room, 1 night", stepOneTestSubscriber.onNextEvents[0])
        assertEquals("Flights - SEA to SFO, round trip", stepTwoTestSubscriber.onNextEvents[0])
        assertEquals("", stepThreeTestSubscriber.onNextEvents[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalTextAfterCreateTrip() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        val testSubscriber = TestSubscriber.create<PackageCreateTripResponse>()
        val params = PackageCreateTripParams("create_trip", "1234", 1, false, emptyList())
        packageServiceRule.services!!.createTrip(params).subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])

        assertEquals("Bundle total", overviewPresenter.totalPriceWidget.bundleTotalText.text)

        setPointOfSale(PointOfSaleId.JAPAN)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])

        assertEquals("Trip total", overviewPresenter.totalPriceWidget.bundleTotalText.text)
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testWebURLForCreateTripWithMIDTurnedOn() {
        val testSubscriber = TestSubscriber.create<String>()
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        setupOverviewPresenter()
        overviewPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        assertEquals("fd713193-3ec1-4773-9f0d-4ff51cc8c19f", (overviewPresenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).multiItemCreateTripResponse?.tripId)

        overviewPresenter.checkoutButton.performClick()

        assertEquals("https://www.expedia.com/MultiItemCheckout?tripid=fd713193-3ec1-4773-9f0d-4ff51cc8c19f", testSubscriber.onNextEvents[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageOverviewHeaderFromMultiItemResponse() {
        setUpPackageDb()
        val hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        setupOverviewPresenter()
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().multiItemResponseSubject.onNext(MultiItemApiCreateTripResponse())

        assertTrue(overviewPresenter.bundleOverviewHeader.isExpandable)

        assertEquals("Thu Dec 07, 2017 - Fri Dec 08, 2017", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.checkInOutDates.text)
        assertEquals("Detroit, MI", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.destinationText.text)
        assertEquals("2 travelers", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.travelers.text)
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(activity, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }

    private fun setUpPackageDb() {
        PackageTestUtil.setDbPackageSelectedHotel()
        val outboundFlight = FlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        val createTripResponse = mockPackageServiceRule.getPSSCreateTripResponse("create_trip")
        Db.getTripBucket().add(TripBucketItemPackages(createTripResponse))
    }

    private fun setupOverviewPresenter() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
        overviewPresenter.bundleWidget.viewModel = BundleOverviewViewModel(activity, packageServiceRule.services!!)
    }
}
