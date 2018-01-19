package com.expedia.bookings.presenter.`package`

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
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
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.ui.PackageHotelActivity
import com.expedia.vm.PackageWebCheckoutViewViewModel
import com.expedia.vm.packages.BundleOverviewViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultPackageComponents()
        Db.setCachedPackageResponse(null)
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
    fun testMenuItemsWhenBackflowIsBucketed() {
        val testSubscriber = TestSubscriber.create<PackageCreateTripResponse>()
        val params = PackageCreateTripParams("create_trip", "1234", 1, false, emptyList())
        AbacusTestUtils.bucketTests(AbacusUtils.PackagesBackFlowFromOverview)
        setupOverviewPresenter()
        packageServiceRule.services!!.createTrip(params).subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])
        assertEquals(overviewPresenter.bundleOverviewHeader.toolbar.menu.size(), 5)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMenuItemsWhenBackflowIsControlled() {
        val testSubscriber = TestSubscriber.create<PackageCreateTripResponse>()
        val params = PackageCreateTripParams("create_trip", "1234", 1, false, emptyList())
        setupOverviewPresenter()
        packageServiceRule.services!!.createTrip(params).subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])
        assertEquals(overviewPresenter.bundleOverviewHeader.toolbar.menu.size(), 4)
    }


    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutViewOpenedWithMIDCheckoutEnabled() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
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
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
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
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.bundleWidget.viewModel.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        overviewPresenter.bundleWidget.viewModel.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        overviewPresenter.bundleWidget.viewModel.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertEquals("Hotel in Detroit - 1 room, 3 nights", stepOneTestSubscriber.onNextEvents[0])
        assertEquals("Flights - SEA to SFO, round trip", stepTwoTestSubscriber.onNextEvents[0])
        assertEquals("", stepThreeTestSubscriber.onNextEvents[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalTextAfterCreateTrip() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        val testSubscriber = TestSubscriber.create<PackageCreateTripResponse>()
        val params = PackageCreateTripParams("create_trip", "1234", 1, false, emptyList())
        setupOverviewPresenter()
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
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        val currentURLIndex = testSubscriber.onNextEvents.size - 1
        assertEquals("https://www.expedia.com/MultiItemCheckout?tripid=fd713193-3ec1-4773-9f0d-4ff51cc8c19f", testSubscriber.onNextEvents[currentURLIndex])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageOverviewHeaderFromMultiItemResponse() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertTrue(overviewPresenter.bundleOverviewHeader.isExpandable)
        assertEquals("Thu Dec 07, 2017 - Fri Dec 08, 2017", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.checkInOutDates.text)
        assertEquals("San Francisco", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.destinationText.text)
        assertEquals("2 travelers", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.travelers.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalPriceDisplayedForMIDWithMandatoryDisplayTypeTotal() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertEquals("$250", overviewPresenter.bottomCheckoutContainer.totalPriceWidget.bundleTotalPrice.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalPriceDisplayedForMIDWithMandatoryDisplayTypeDaily() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter(false)
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertEquals("$350", overviewPresenter.bottomCheckoutContainer.totalPriceWidget.bundleTotalPrice.text)
    }
    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCacheResponseUsedOnChangeHotelRoomBack() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())

        assertTrue(overviewPresenter.getCheckoutPresenter().visibility == View.VISIBLE)

        (overviewPresenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).closeView.onNext(Unit)

        assertTrue(overviewPresenter.getCheckoutPresenter().visibility == View.VISIBLE)

        assertNull(Db.getCachedPackageResponse())

        overviewPresenter.onChangeHotelRoomClicked()

        assertNotNull(Db.getCachedPackageResponse())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCacheResponseUsedOnChangeHotelBack() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())

        assertTrue(overviewPresenter.getCheckoutPresenter().visibility == View.VISIBLE)

        (overviewPresenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).closeView.onNext(Unit)

        assertTrue(overviewPresenter.getCheckoutPresenter().visibility == View.VISIBLE)

        assertNull(Db.getCachedPackageResponse())

        overviewPresenter.onChangeHotelClicked()

        val actualCachedResponse = Db.getCachedPackageResponse()
        assertNotNull(actualCachedResponse)

        Db.setPackageResponse(null)

        val newIntent= Shadows.shadowOf(activity).peekNextStartedActivity()
        val packageHotelActivity = Robolectric.buildActivity(PackageHotelActivity::class.java, newIntent).create().get()

        packageHotelActivity.onBackPressed()

        assertNotNull(Db.getPackageResponse())
        assertNull(Db.getCachedPackageResponse())

    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMidCheckoutViewShowsAfterGoingBackFromOverviewScreenToCheckout() {
        val testSubscriber = TestSubscriber.create<String>()
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())

        overviewPresenter.checkoutButton.performClick()
        overviewPresenter.webCheckoutView.viewModel.backObservable.onNext(Unit)
        overviewPresenter.checkoutButton.performClick()

        assert((Shadows.shadowOf(overviewPresenter.webCheckoutView.webView).lastLoadedUrl).contains("https://www.${ PointOfSale.getPointOfSale().url}/MultiItemCheckout?tripid="))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDCloseWebViewOnOverviewScreenDoesNothing() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        setupOverviewPresenter()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())

        assertTrue(overviewPresenter.getCheckoutPresenter().visibility == View.VISIBLE)

        (overviewPresenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).closeView.onNext(Unit)

        assertTrue(overviewPresenter.getCheckoutPresenter().visibility == View.VISIBLE)
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(activity, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }

    private fun setUpPackageDb() {
        PackageTestUtil.setDbPackageSelectedHotel()
        val outboundFlight = PackageTestUtil.getDummyPackageFlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)
        Db.setPackageFlightBundle(outboundFlight, PackageTestUtil.getDummyPackageFlightLeg())
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        val createTripResponse = mockPackageServiceRule.getPSSCreateTripResponse("create_trip")
        Db.getTripBucket().add(TripBucketItemPackages(createTripResponse))
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())

        val baseMidResponse = PackageTestUtil.getMockMIDResponse(offers = emptyList(),
                hotels = mapOf("1" to PackageTestUtil.dummyMidHotelRoomOffer()))
        baseMidResponse.setCurrentOfferPrice(setPackagePrice())
        Db.setPackageResponse(baseMidResponse)
    }

    private fun setupOverviewPresenter(mandatoryTotalDisplayType: Boolean = true) {
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
        overviewPresenter.bundleWidget.viewModel = BundleOverviewViewModel(activity, packageServiceRule.services!!)
        setUpPackageDb()
        PackageTestUtil.setDbPackageSelectedHotel(mandatoryTotalDisplayType)
    }

    private fun setPackagePrice(): PackageOfferModel.PackagePrice {
        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money("200", "USD")
        return packagePrice
    }
}
