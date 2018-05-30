package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.packages.presenter.PackageHotelPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PackageHotelPresenterTest {
    private lateinit var widget: PackageHotelPresenter
    private lateinit var activity: Activity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var params: PackageSearchParams
    val context = RuntimeEnvironment.application

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get
    lateinit var hotelResponse: BundleSearchResponse

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testPackageSearchParamsTracked() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(hotelResponse)
        widget.trackEventSubject.onNext(Unit)

        val expectedEvars = mapOf(
                47 to "PKG|1R|RT|A1|C1|YTH1|IL1|IS0|E"
        )
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testPackageSearchParamsTrackedWithNewTravelerForm() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(hotelResponse)
        widget.trackEventSubject.onNext(Unit)

        val expectedEvars = mapOf(
                47 to "PKG|1R|RT|A1|C1|YTH1|IL1|IS0|E"
        )
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testSlidingBundleVisible() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))
        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.VISIBLE)

        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(getHotelOffers())

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.DETAILS)
        assertEquals(View.GONE, widget.bundleSlidingWidget.visibility)

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.VISIBLE)
    }

    @Test
    fun testSlidingBundleNotVisibleIfBucketedInHSRPriceDisplay() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))
        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.GONE)

        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(getHotelOffers())

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.DETAILS)
        assertBundlePriceAndSliderVisibilities(View.GONE)

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.GONE)
    }

    private fun getHotelOffers(): HotelOffersResponse {
        val roomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        val hotelInfo = mockPackageServiceRule.getHotelInfo()
        return HotelOffersResponse.convertToHotelOffersResponse(hotelInfo, roomResponse.getBundleRoomResponse(), roomResponse.getHotelCheckInDate(), roomResponse.getHotelCheckOutDate())
    }

    private fun assertBundlePriceAndSliderVisibilities(expectedVisibility: Int) {
        assertEquals(expectedVisibility, widget.bundleSlidingWidget.visibility)
        assertEquals(expectedVisibility, widget.resultsPresenter.bundlePriceWidgetTop.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testBundleTotalPriceWidgetTopVisibility() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))

        assertEquals(View.VISIBLE, widget.resultsPresenter.bundlePriceWidgetTop.visibility)
        assertEquals("/person", widget.resultsPresenter.bundlePriceWidgetTop.bundlePerPersonText.text)
        assertEquals("$0.00", widget.resultsPresenter.bundlePriceWidgetTop.bundleTotalPrice.text)
        assertEquals("View your trip", widget.resultsPresenter.bundlePriceWidgetTop.bundleTitleText.text)
        assertEquals(View.VISIBLE, widget.resultsPresenter.bundlePriceWidgetTop.bundleInfoIcon.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleWidgetTapTracking() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))

        widget.bundleSlidingWidget.bundlePriceWidget.performClick()

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleWidgetTopTapTracking() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))

        widget.resultsPresenter.bundlePriceWidgetTop.performClick()

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsHappyPath() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "happy"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsRoomsCallFail() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "happy"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room_fail"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.assertValueCount(0)

        val shadowActivity = Shadows.shadowOf(activity)
        assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)

        val errorCode = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR_KEY)
        assertEquals("PACKAGE_SEARCH_ERROR", errorCode)
        assertEquals("UNKNOWN_ERROR", errorKey)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsInfoCallFail() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "happy_fail"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.assertValueCount(0)

        val shadowActivity = Shadows.shadowOf(activity)
        assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)

        val errorCode = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR_KEY)
        assertEquals("PACKAGE_SEARCH_ERROR", errorCode)
        assertEquals("UNKNOWN_ERROR", errorKey)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsBothCallsFail() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "happy_fail"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room_fail"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.assertValueCount(0)

        val shadowActivity = Shadows.shadowOf(activity)
        assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)

        val errorString = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
        assertEquals("PACKAGE_SEARCH_ERROR", errorString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsRoomCallError() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "happy"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "error"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.assertValueCount(0)

        val shadowActivity = Shadows.shadowOf(activity)
        assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)

        val errorString = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
        assertEquals("PACKAGE_SEARCH_ERROR", errorString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsInfoCallError() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "error"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.assertValueCount(0)

        val shadowActivity = Shadows.shadowOf(activity)
        assertEquals(Activity.RESULT_OK, shadowActivity.resultCode)

        val errorString = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_OFFERS_ERROR)
        assertEquals("UNKNOWN_ERROR", errorString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelRoomSelectedDbParams() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "happy"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)

        widget.detailPresenter.hotelDetailView.viewmodel.roomSelectedSubject.onNext(testSubscriber.values()[0].hotelRoomResponse[0])

        val params = Db.sharedInstance.packageParams
        val currentOfferPrice = Db.getPackageResponse().getCurrentOfferPrice()

        assertNull(params.packagePIID)
        assertEquals("flight_outbound_happy", params.latestSelectedOfferInfo.ratePlanCode)
        assertEquals("225416", params.latestSelectedOfferInfo.roomTypeCode)
        assertEquals("MERCHANT", params.latestSelectedOfferInfo.inventoryType)
        assertEquals("2018-05-07", params.latestSelectedOfferInfo.hotelCheckInDate)
        assertEquals("2018-05-10", params.latestSelectedOfferInfo.hotelCheckOutDate)

        assertNotNull(currentOfferPrice)

        assertEquals(Money("4426.96", "USD"), currentOfferPrice?.packageTotalPrice)
        assertEquals(Money("4309.75", "USD"), currentOfferPrice?.packageReferenceTotalPrice)
        assertEquals(Money("-117.21", "USD"), currentOfferPrice?.tripSavings)
        assertEquals(Money("1106.74", "USD"), currentOfferPrice?.pricePerPerson)
        assertFalse(currentOfferPrice?.showTripSavings ?: false)
    }
}
