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
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.packages.presenter.PackageHotelPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.tracking.ApiCallFailing
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
import org.robolectric.shadows.ShadowActivity
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

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
    fun testPackageHotelFilterTracked() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.isFilteredResponse = true

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(hotelResponse)
        widget.trackEventSubject.onNext(Unit)
        val expectedEvars = mapOf(18 to "App.Package.Hotels.Search.Filtered")
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testPackageHotelDataAvailableForTrackingFilters() {
        val testFilterResponseDataAvailableSubscriber = TestObserver<BundleSearchResponse>()

        val filterResponse = mockPackageServiceRule.getMIDHotelResponse()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.isFilteredResponse = true
        widget.dataAvailableSubject.subscribe(testFilterResponseDataAvailableSubscriber)

        widget.resultsPresenter.viewModel.filterResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(filterResponse, true))
        assertNotNull(testFilterResponseDataAvailableSubscriber.values()[0])
        assertEquals(filterResponse, testFilterResponseDataAvailableSubscriber.values()[0])
    }

    @Test
    fun testSlidingBundleVisible() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse, false))
        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.VISIBLE)

        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(getHotelOffers())

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.DETAILS)
        assertEquals(View.GONE, widget.bundleSlidingWidget.visibility)

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.VISIBLE)
    }

    @Test
    fun testHotelSelectedSubject() {
        setPackageSearchParamsInDb()
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse, false))
        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)

        widget.bundleSlidingWidget.bundlePriceWidget.setOnClickListener { }

        widget.resultsPresenter.hotelSelectedSubject.onNext(hotelResponse.getHotels()[0])

        assertEquals(hotelResponse.getHotels()[0].hotelId, Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId)
        assertEquals(Db.getPackageResponse().getFlightPIIDFromSelectedHotel(hotelResponse.getHotels()[0].hotelPid), Db.sharedInstance.packageParams.latestSelectedOfferInfo.flightPIID)
        assertEquals(hotelResponse.getHotels()[0].packageOfferModel.price, Db.sharedInstance.packageParams.latestSelectedOfferInfo.productOfferPrice)

        assertEquals(View.VISIBLE, widget.loadingOverlay.visibility)
        assertFalse(widget.bundleSlidingWidget.bundlePriceWidget.hasOnClickListeners())
    }

    @Test
    fun testSlidingBundleNotVisibleIfBucketedInHSRPriceDisplay() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse, false))
        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.GONE)

        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(getHotelOffers())

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.DETAILS)
        assertBundlePriceAndSliderVisibilities(View.GONE)

        widget.defaultTransitionObserver.onNext(PackageHotelActivity.Screen.RESULTS)
        assertBundlePriceAndSliderVisibilities(View.GONE)
    }

    @Test
    fun testFilterSearchResponseError() {
        setPackageSearchParamsInDb()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        val errorPair = getErrorPair(PackageApiError.Code.mid_no_offers_post_filtering.toString(), PackageApiError.Code.mid_no_offers_post_filtering)

        widget.resultsPresenter.viewModel.filterSearchErrorDetailsObservable.onNext(errorPair)

        val shadowActivity = Shadows.shadowOf(activity)
        assertErrorKeyAndCode(shadowActivity, "mid_no_offers_post_filtering")
    }

    @Test
    fun testFilterSearchResponseUnknownError() {
        setPackageSearchParamsInDb()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        val errorPair = getErrorPair(PackageApiError.Code.mid_no_offers_post_filtering.toString(), PackageApiError.Code.pkg_unknown_error)

        widget.resultsPresenter.viewModel.filterSearchErrorDetailsObservable.onNext(errorPair)

        val shadowActivity = Shadows.shadowOf(activity)
        assertErrorKeyAndCode(shadowActivity, "pkg_unknown_error")
    }

    private fun setPackageSearchParamsInDb() {
        val searchParams = PackageTestUtil.getPackageSearchParams()
        Db.setPackageParams(searchParams)
    }

    private fun assertErrorKeyAndCode(shadowActivity: ShadowActivity, expectedErrorCode: String) {
        assertEquals(Constants.PACKAGE_HOTEL_FILTER_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorCode = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_FILTER_SEARCH_ERROR)
        assertEquals(expectedErrorCode, errorCode)
    }

    private fun getErrorPair(apiCallFailingCode: String, apiErrorCode: PackageApiError.Code): Pair<PackageApiError.Code, ApiCallFailing> {
        val apiFailing = ApiCallFailing.PackageHotelRoom(apiCallFailingCode)
        return Pair(apiErrorCode, apiFailing)
    }

    private fun getHotelOffers(): HotelOffersResponse {
        val roomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        val hotelInfo = mockPackageServiceRule.getHotelInfo()
        return HotelOffersResponse.convertToHotelOffersResponse(hotelInfo, roomResponse.getBundleRoomResponse(), roomResponse.getHotelCheckInDate(), roomResponse.getHotelCheckOutDate())
    }

    private fun assertBundlePriceAndSliderVisibilities(expectedVisibility: Int) {
        assertEquals(expectedVisibility, widget.bundleSlidingWidget.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleWidgetTapTracking() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse, false))

        widget.bundleSlidingWidget.bundlePriceWidget.performClick()

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
        assertEquals(Constants.PACKAGE_HOTEL_OFFERS_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorCode = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
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
        assertEquals(Constants.PACKAGE_HOTEL_OFFERS_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorCode = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
        assertEquals("PACKAGE_SEARCH_ERROR", errorCode)
        assertEquals("UNKNOWN_ERROR", errorKey)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageHotelDetailsInfoCallFailWithStatusOK() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)

        hotelResponse.getHotels()[0].hotelId = "fail"
        Db.sharedInstance.packageParams.latestSelectedOfferInfo.hotelId = "happy_room"

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.packageServices = mockPackageServiceRule.services!!

        val testSubscriber = TestObserver<HotelOffersResponse>()
        widget.detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.subscribe(testSubscriber)
        widget.hotelSelectedObserver.onNext(hotelResponse.getHotels()[0])

        testSubscriber.assertValueCount(0)

        val shadowActivity = Shadows.shadowOf(activity)
        assertEquals(Constants.PACKAGE_HOTEL_INFOSITE_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorCode = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
        assertEquals("UNKNOWN_ERROR", errorCode)
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
        assertEquals(Constants.PACKAGE_HOTEL_OFFERS_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
        val errorString = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        assertEquals("UNKNOWN_ERROR", errorKey)
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
        assertEquals(Constants.PACKAGE_HOTEL_OFFERS_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
        val errorString = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        assertEquals("MIS_INVALID_REQUEST", errorKey)
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
        assertEquals(Constants.PACKAGE_HOTEL_INFOSITE_API_ERROR_RESULT_CODE, shadowActivity.resultCode)

        val errorKey = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR_KEY)
        val errorString = shadowActivity.resultIntent.extras?.getString(Constants.PACKAGE_HOTEL_API_ERROR)
        assertEquals("UNKNOWN_ERROR", errorKey)
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
