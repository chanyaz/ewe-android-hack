package com.expedia.bookings.packages.presenter

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.multiitem.MandatoryFees
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageCostSummaryBreakdownModel
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.packages.util.PackageServicesManager
import com.expedia.bookings.packages.vm.BundleOverviewViewModel
import com.expedia.bookings.packages.vm.PackageCostSummaryBreakdownViewModel
import com.expedia.bookings.packages.vm.PackageWebCheckoutViewViewModel
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RoboTestHelper.getContext
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowObjectAnimator
import com.expedia.bookings.test.robolectric.shadows.ShadowValueAnimator
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowValueAnimator::class, ShadowObjectAnimator::class))
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
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleTotalTextBeforeCreateTripForJapanPOS() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.JAPAN)
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter

        assertEquals("Trip total (with taxes & fee)", overviewPresenter.totalPriceWidget.bundleTotalText.text.toString())
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMenuItems() {
        val testSubscriber = TestObserver.create<MultiItemApiCreateTripResponse>()
        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money()
        val params = MultiItemCreateTripParams("mid_create_trip", "", "", "", "", packagePrice, "", "", 0, null, null)
        setupOverviewPresenter()
        packageServiceRule.services!!.multiItemCreateTrip(params).subscribe(testSubscriber)
        assertEquals(overviewPresenter.bundleOverviewHeader.toolbar.menu.size(), 4)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
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
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPackageCheckoutViewOpenedWithMIDCheckoutDisabled() {
        setupOverviewPresenter()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.checkoutButton.performClick()

        assertEquals(View.VISIBLE, overviewPresenter.getCheckoutPresenter().visibility)
        assertEquals(View.GONE, overviewPresenter.webCheckoutView.visibility)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPackageBundleWidgetTitleTextFromMultiItemResponse() {
        val stepOneTestSubscriber = TestObserver<String>()
        val stepTwoTestSubscriber = TestObserver<String>()
        val stepThreeTestSubscriber = TestObserver<String>()
        setupOverviewPresenter()
        overviewPresenter.bundleWidget.viewModel.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        overviewPresenter.bundleWidget.viewModel.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        overviewPresenter.bundleWidget.viewModel.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertEquals("Hotel in Detroit - 1 room, 3 nights", stepOneTestSubscriber.values()[0])
        assertEquals("Flights - SEA to SFO, round trip", stepTwoTestSubscriber.values()[0])
        assertEquals("", stepThreeTestSubscriber.values()[0])
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleHotelWidgetDatesAndGuestTextForMID() {
        setupOverviewPresenter()

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        assertEquals(overviewPresenter.bundleWidget.bundleHotelWidget.hotelsDatesGuestInfoText.text, "Sep 7 - Sep 10, 2 guests")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleBetterSavingsBottomBar() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        setupOverviewPresenter()

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        val totalPriceWidget = overviewPresenter.totalPriceWidget
        assertEquals(View.VISIBLE, totalPriceWidget.betterSavingContainer.visibility)
        assertEquals("$100.23", totalPriceWidget.betterSavingView.text)
        assertEquals("$200", totalPriceWidget.bundleTotalPrice.text)
        assertEquals(View.VISIBLE, totalPriceWidget.bundleReferenceTotalPrice.visibility)
        assertEquals("$300.23", totalPriceWidget.bundleReferenceTotalPrice.text)
        assertEquals(View.GONE, totalPriceWidget.bundleSavings.visibility)
        assertNotNull(totalPriceWidget.bundleTotalText.compoundDrawables[2])
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleBetterSavingsBottomBarSavingsFalse() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        setupOverviewPresenter()
        Db.getPackageResponse().getCurrentOfferPrice()?.showTripSavings = false

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        val totalPriceWidget = overviewPresenter.totalPriceWidget
        assertEquals(View.GONE, totalPriceWidget.betterSavingContainer.visibility)
        assertEquals("$200", totalPriceWidget.bundleTotalPrice.text)
        assertEquals(View.GONE, totalPriceWidget.bundleReferenceTotalPrice.visibility)
        assertEquals(View.GONE, totalPriceWidget.bundleSavings.visibility)
        assertNull(totalPriceWidget.bundleTotalText.compoundDrawables[2])
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testWithoutBundleBetterSavingsBottomBar() {
        setupOverviewPresenter()

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        val totalPriceWidget = overviewPresenter.totalPriceWidget
        assertEquals(View.GONE, totalPriceWidget.betterSavingContainer.visibility)
        assertEquals("$200", totalPriceWidget.bundleTotalPrice.text)
        assertEquals(View.GONE, totalPriceWidget.bundleReferenceTotalPrice.visibility)
        assertEquals(View.VISIBLE, totalPriceWidget.bundleSavings.visibility)
        assertEquals("$100.23 Saved", totalPriceWidget.bundleSavings.text)
        assertNull(totalPriceWidget.bundleTotalText.compoundDrawables[2])
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleTotalTextAfterCreateTrip() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        val testSubscriber = TestObserver.create<MultiItemApiCreateTripResponse>()
        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money()
        val params = MultiItemCreateTripParams("mid_create_trip", "", "", "", "", packagePrice, "", "", 0, null, null)

        setupOverviewPresenter()
        packageServiceRule.services!!.multiItemCreateTrip(params).subscribe(testSubscriber)
        assertEquals("Bundle total", overviewPresenter.totalPriceWidget.bundleTotalText.text)

        setPointOfSale(PointOfSaleId.JAPAN)
        setupOverviewPresenter()
        packageServiceRule.services!!.multiItemCreateTrip(params).subscribe(testSubscriber)
        assertEquals("Trip total (with taxes & fee)", overviewPresenter.totalPriceWidget.bundleTotalText.text.toString())
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testWebURLForCreateTripWithMIDTurnedOn() {
        val testSubscriber = TestObserver.create<String>()
        setupOverviewPresenter()
        overviewPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)
        overviewPresenter.webCheckoutView.viewModel.showWebViewObservable.onNext(true)

        val currentURLIndex = testSubscriber.valueCount() - 1
        assertEquals("https://www.expedia.com/MultiItemCheckout?tripid=859b3288-4dcf-46e5-a545-8e9daaa3be45", testSubscriber.values()[currentURLIndex])
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPackageOverviewHeaderFromMultiItemResponse() {
        setupOverviewPresenter()
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertTrue(overviewPresenter.bundleOverviewHeader.isExpandable)
        assertEquals("Thu Dec 07, 2017 - Fri Dec 08, 2017", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.checkInOutDates.text)
        assertEquals("San Francisco", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.destinationText.text)
        assertEquals("2 travelers", overviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.travelers.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPackageCostSummaryBreakdown() {
        val testSubscriber = TestObserver.create<PackageCostSummaryBreakdownModel>()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        setupOverviewPresenter()
        val vm = overviewPresenter.totalPriceWidget.breakdown.viewmodel as PackageCostSummaryBreakdownViewModel
        vm.packageCostSummaryObservable.subscribe(testSubscriber)

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertEquals(PackageCostSummaryBreakdownModel(null, null, "$300.23", "$100.23", "$200"), testSubscriber.values()[0])
    }

    private fun assertTotalPriceDisplayedForMID(displayType: MandatoryFees.DisplayType,
                                                displayCurrency: MandatoryFees.DisplayCurrency,
                                                expectedTotalPrice: String) {
        setupOverviewPresenter(displayType, displayCurrency)
        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        assertEquals(expectedTotalPrice, overviewPresenter.bottomCheckoutContainer.totalPriceWidget.bundleTotalPrice.text)
        assertEquals("$100.23 Saved", overviewPresenter.bottomCheckoutContainer.totalPriceWidget.bundleSavings.text)
    }
/*
    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalPriceMIDNonePointOfSale() {
        assertTotalPriceDisplayedForMID(MandatoryFees.DisplayType.NONE,
                MandatoryFees.DisplayCurrency.POINT_OF_SALE,
                "$250")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalPriceMIDNonePointOfSupply() {
        assertTotalPriceDisplayedForMID(MandatoryFees.DisplayType.NONE,
                MandatoryFees.DisplayCurrency.POINT_OF_SUPPLY,
                "$200")
    }*/

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleTotalPriceMIDDailyPointOfSale() {
        assertTotalPriceDisplayedForMID(MandatoryFees.DisplayType.DAILY,
                MandatoryFees.DisplayCurrency.POINT_OF_SALE,
                "$200")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleTotalPriceMIDDailyPointOfSupply() {
        assertTotalPriceDisplayedForMID(MandatoryFees.DisplayType.DAILY,
                MandatoryFees.DisplayCurrency.POINT_OF_SUPPLY,
                "$200")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleTotalPriceMIDTotalPointOfSale() {
        assertTotalPriceDisplayedForMID(MandatoryFees.DisplayType.TOTAL,
                MandatoryFees.DisplayCurrency.POINT_OF_SALE,
                "$200")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testBundleTotalPriceMIDTotalPointOfSupply() {
        assertTotalPriceDisplayedForMID(MandatoryFees.DisplayType.TOTAL,
                MandatoryFees.DisplayCurrency.POINT_OF_SUPPLY,
                "$200")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testCacheResponseUsedOnChangeHotelRoomBack() {
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
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testCacheResponseUsedOnChangeHotelBack() {
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

        val newIntent = Shadows.shadowOf(activity).peekNextStartedActivity()
        val packageHotelActivity = Robolectric.buildActivity(PackageHotelActivity::class.java, newIntent).create().get()

        packageHotelActivity.onBackPressed()

        assertNotNull(Db.getPackageResponse())
        assertNull(Db.getCachedPackageResponse())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMidCheckoutViewShowsAfterGoingBackFromOverviewScreenToCheckout() {
        setupOverviewPresenter()
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())

        overviewPresenter.checkoutButton.performClick()
        overviewPresenter.webCheckoutView.viewModel.backObservable.onNext(Unit)
        overviewPresenter.checkoutButton.performClick()

        assert((Shadows.shadowOf(overviewPresenter.webCheckoutView.webView).lastLoadedUrl).contains("https://www.${PointOfSale.getPointOfSale().url}/MultiItemCheckout?tripid="))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMaskActivityWhenGoingFromOverviewToWebView() {
        setupOverviewPresenter()
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        overviewPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        overviewPresenter.checkoutButton.performClick()

        maskWebCheckoutActivityObservable.assertValue(true)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontMaskActivityWhenGoingFromWebViewToOverview() {
        setupOverviewPresenter()
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        overviewPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!

        overviewPresenter.performMIDCreateTripSubject.onNext(Unit)

        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        overviewPresenter.checkoutButton.performClick()

        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.webCheckoutView.viewModel.backObservable.onNext(Unit)
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())

        maskWebCheckoutActivityObservable.assertValues(true, false)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMIDCloseWebViewOnOverviewScreenDoesNothing() {
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
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())

        val baseMidResponse = PackageTestUtil.getMockMIDResponse(offers = emptyList(),
                hotels = mapOf("1" to PackageTestUtil.dummyMidHotelRoomOffer()))
        baseMidResponse.setCurrentOfferPrice(setPackagePrice())
        Db.setPackageResponse(baseMidResponse)
    }

    private fun setupOverviewPresenter(displayType: MandatoryFees.DisplayType = MandatoryFees.DisplayType.NONE,
                                       displayCurrency: MandatoryFees.DisplayCurrency = MandatoryFees.DisplayCurrency.POINT_OF_SALE) {
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
        overviewPresenter.bundleWidget.viewModel = BundleOverviewViewModel(activity, PackageServicesManager(activity, packageServiceRule.services!!))
        setUpPackageDb()
        PackageTestUtil.setDbPackageSelectedHotel(displayType, displayCurrency)
    }

    private fun setPackagePrice(): PackageOfferModel.PackagePrice {
        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money("200", "USD")
        packagePrice.packageReferenceTotalPrice = Money("300.23", "USD")
        packagePrice.tripSavings = Money("100.23", "USD")
        packagePrice.showTripSavings = true
        return packagePrice
    }
}
