package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.WebViewUtils
import com.expedia.vm.HotelWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelWebCheckoutViewTest {

    lateinit var hotelPresenter: HotelPresenter
    lateinit var activity: Activity
    lateinit var webCheckoutViewObservable: TestObserver<Unit>
    var userAccountRefresherMock = Mockito.mock(UserAccountRefresher::class.java)

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
    }

    private fun getDummyHotelSearchParams(): HotelSearchParams {
        return HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                .destination(getDummySuggestion())
                .adults(2)
                .children(listOf(10, 10, 10))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as HotelSearchParams
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webCheckoutUsed() {
        getToWebCheckoutView()
        webCheckoutViewObservable.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webCheckoutNotUsedOnUnsupportedPOS() {
        givenHotelDetailsScreen(setPOSWithWebCheckoutABTestEnabled = false)
                .whenHotelRoomSelected()
                .thenTheWebViewDoesNotBecomeVisible()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webCheckoutNotUsedWhenNotBucketed() {
        givenHotelDetailsScreen(bucketWebCheckoutABTest = false, setPOSWithWebCheckoutABTestEnabled = true)
                .whenHotelRoomSelected()
                .thenTheWebViewDoesNotBecomeVisible()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webCheckoutUsedOnSupportedPOSWhenBucketed() {
        givenHotelDetailsScreen(bucketWebCheckoutABTest = true, setPOSWithWebCheckoutABTestEnabled = true)
                .whenHotelRoomSelected()
                .thenTheWebViewBecomesVisible()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webCheckoutUsedWhenABTestBucketed() {
        bucketWebCheckoutABTest(true)
        setPOSWithWebCheckoutABTestEnabled(false)
        setUpTestToStartAtDetailsScreen()
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
    }

    private fun bucketWebCheckoutABTest(enable: Boolean) {
        if (enable) {
            RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppHotelsWebCheckout)
        } else {
            RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppHotelsWebCheckout)
        }
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webViewTripIDOnSuccessfulBooking() {
        val bookingTripIDSubscriber = TestObserver<String>()
        val fetchTripIDSubscriber = TestObserver<String>()
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fetchTripIDSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
        bookingTripIDSubscriber.assertValueCount(0)
        fetchTripIDSubscriber.assertValueCount(0)
        val tripID = "testing-for-confirmation"
        verify(userAccountRefresherMock, times(0)).forceAccountRefreshForWebView()

        hotelPresenter.webCheckoutView.onWebPageStarted(hotelPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL + "?tripid=$tripID", null)
        verify(userAccountRefresherMock, times(1)).forceAccountRefreshForWebView()
        bookingTripIDSubscriber.assertValueCount(1)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fetchTripIDSubscriber.assertValueCount(1)
        fetchTripIDSubscriber.assertValue(tripID)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testWebviewDoesNotFetchTripIdWithoutValidConfirmationUrl() {
        val bookingTripIDSubscriber = TestObserver<String>()
        val fetchTripIDSubscriber = TestObserver<String>()
        val closeViewSubscriber = TestObserver<Unit>()

        setPOSWithWebCheckoutABTestEnabled(true)
        bucketWebCheckoutABTest(true)
        setUpTestToStartAtDetailsScreen()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fetchTripIDSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).closeView.subscribe(closeViewSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        assertFalse(PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL.isNullOrBlank())
        assertTrue(PointOfSale.getPointOfSale().flightsWebBookingConfirmationURL.isNullOrBlank())
        selectHotelRoom()

        webCheckoutViewObservable.assertValueCount(1)
        verify(userAccountRefresherMock, times(0)).forceAccountRefreshForWebView()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        bookingTripIDSubscriber.assertValueCount(0)
        fetchTripIDSubscriber.assertValueCount(0)
        closeViewSubscriber.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webViewRefreshUserOnBackPress() {
        val closeViewSubscriber = TestObserver<Unit>()
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).closeView.subscribe(closeViewSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
        closeViewSubscriber.assertValueCount(0)
        verify(userAccountRefresherMock, times(0)).forceAccountRefreshForWebView()

        hotelPresenter.webCheckoutView.back()
        verify(userAccountRefresherMock, times(1)).forceAccountRefreshForWebView()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        closeViewSubscriber.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webViewURLWithoutParamsContainsAppVID() {
        getToWebCheckoutView()
        val testURL = "abcdefg"
        hotelPresenter.webCheckoutView.viewModel.webViewURLObservable.onNext(testURL)
        assert((shadowOf(hotelPresenter.webCheckoutView.webView).lastLoadedUrl).startsWith("$testURL?adobe_mc="))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webViewURLWithParamsContainsAppVID() {
        getToWebCheckoutView()
        val testURL = "abcdefg?ab=c"
        hotelPresenter.webCheckoutView.viewModel.webViewURLObservable.onNext(testURL)
        assert((shadowOf(hotelPresenter.webCheckoutView.webView).lastLoadedUrl).startsWith("$testURL&adobe_mc="))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testTelephoneUrlLoadsPhoneActivity() {
        getToWebCheckoutView()
        val phoneUrl = "tel:800-423-5498"
        hotelPresenter.webCheckoutView.webClient().onPageStarted(hotelPresenter.webCheckoutView.webView, phoneUrl, null)

        val shadowActivity = Shadows.shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult().intent

        Assert.assertEquals(Uri.parse(phoneUrl), intent.data)
        Assert.assertEquals(Intent.ACTION_VIEW, intent.action)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun webViewClearsPageGoingBack() {
        val closeViewSubscriber = TestObserver<Unit>()
        val urlSubscriber = TestObserver<String>()
        getToWebCheckoutView()

        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).webViewURLObservable.subscribe(urlSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).closeView.subscribe(closeViewSubscriber)
        hotelPresenter.webCheckoutView.back()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()

        closeViewSubscriber.assertValueCount(1)
        urlSubscriber.assertValueCount(1)
        urlSubscriber.assertValue("about:blank")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testLoginStateChangedReloadsUrlWithLoggedInUser() {
        getToWebCheckoutView()
        loginMockUser()
        val testReloadSubscriber = TestObserver<Unit>()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).reloadUrlObservable.subscribe(testReloadSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userLoginStateChangedModel.userLoginStateChanged.onNext(true)

        testReloadSubscriber.assertValueCount(1)
        assertFalse(hotelPresenter.webCheckoutView.webView.canGoBack())
    }

    @Test
    fun testDoNotReloadUrlUntilStatusChangedToTrueAndLoggedIn() {
        getToWebCheckoutView()
        val testReloadSubscriber = TestObserver<Unit>()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).reloadUrlObservable.subscribe(testReloadSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userLoginStateChangedModel.userLoginStateChanged.onNext(false)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userLoginStateChangedModel.userLoginStateChanged.onNext(true)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userLoginStateChangedModel.userLoginStateChanged.onNext(false)

        testReloadSubscriber.assertNoValues()

        loginMockUser()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userLoginStateChangedModel.userLoginStateChanged.onNext(true)
        testReloadSubscriber.assertValueCount(1)
    }

    @Test
    fun testShowSearchScreenAfterWebCheckoutErrorRedirect() {
        getToWebCheckoutView()
        val (maskWebCheckoutActivityObservable, testUrlObservable, testShowNativeObserver) = setupShowNativeTestObservers()
        hotelPresenter.show(hotelPresenter.webCheckoutView)
        hotelPresenter.webCheckoutView.goToSearchAndClearWebView()

        assertNativeHomeScreenShown(testShowNativeObserver, maskWebCheckoutActivityObservable, testUrlObservable)
    }

    @Test
    fun testShowSearchScreenAfterWebCheckoutErrorBack() {
        getToWebCheckoutView()
        val (maskWebCheckoutActivityObservable, testUrlObservable, testShowNativeObserver) = setupShowNativeTestObservers()

        hotelPresenter.show(hotelPresenter.webCheckoutView)
        hotelPresenter.webCheckoutView.onWebPageStarted(hotelPresenter.webCheckoutView.webView, "https://www.expedia.com/HotelCheckoutError", null)
        hotelPresenter.webCheckoutView.back()

        assertNativeHomeScreenShown(testShowNativeObserver, maskWebCheckoutActivityObservable, testUrlObservable)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    @Config(qualifiers = "sw600dp")
    fun testUserAgentStringHasTabletInfo() {
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        hotelPresenter.webCheckoutView.addNewWebViewToWidget(activity)
        assertEquals("Android " + WebViewUtils.userAgentString + " app.webview.tablet", hotelPresenter.webCheckoutView.webView.settings.userAgentString)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testUserAgentStringHasPhoneInfo() {
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        hotelPresenter.webCheckoutView.addNewWebViewToWidget(activity)
        assertEquals("Android " + WebViewUtils.userAgentString + " app.webview.phone", hotelPresenter.webCheckoutView.webView.settings.userAgentString)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testMaskActivityWhenGoingToWebView() {
        getToWebCheckoutView()
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        hotelPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)
        hotelPresenter.show(hotelPresenter.webCheckoutView)
        maskWebCheckoutActivityObservable.assertValue(true)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDontMaskActivityWhenGoingFromWebViewBackToHotelDetails() {
        getToWebCheckoutView()
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        hotelPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)
        hotelPresenter.show(hotelPresenter.webCheckoutView)

        // Transition doesn't happen if not done like this
        hotelPresenter.show(hotelPresenter.detailPresenter)
        hotelPresenter.webCheckoutView.viewModel.backObservable.onNext(Unit)
        hotelPresenter.show(hotelPresenter.detailPresenter)
        maskWebCheckoutActivityObservable.assertValues(true, false)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldNotMaskScreenAfterWebCheckoutToNativeConfirmation() {
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()

        val shouldMaskScreenTestObserver = TestObserver.create<Boolean>()
        hotelPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(shouldMaskScreenTestObserver)

        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        selectHotelRoom()
        val tripID = "testing-for-confirmation"
        verify(userAccountRefresherMock, times(0)).forceAccountRefreshForWebView()
        hotelPresenter.webCheckoutView.onWebPageStarted(hotelPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL + "?tripid=$tripID", null)
        verify(userAccountRefresherMock, times(1)).forceAccountRefreshForWebView()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()

        shouldMaskScreenTestObserver.assertValue(false)
    }

    private fun getToWebCheckoutView() {
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        selectHotelRoom()
    }

    private fun loginMockUser() {
        val userStateManager = Ui.getApplication(activity).appComponent().userStateManager()
        val testUser = UserLoginTestUtil.mockUser()
        testUser.primaryTraveler.email = "test@expedia.com"
        userStateManager.addUserToAccountManager(testUser)
        UserLoginTestUtil.setupUserAndMockLogin(testUser)
    }

    private fun selectHotelRoom() {
        val hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelPresenter.hotelDetailViewModel.roomSelectedSubject.onNext(hotelRoomResponse)
    }

    private fun setPOSWithWebCheckoutEnabled(enable: Boolean) {
        val pointOfSale = if (enable) PointOfSaleId.INDIA else PointOfSaleId.UNITED_STATES
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun setPOSWithWebCheckoutABTestEnabled(enable: Boolean) {
        val pointOfSale = if (enable) PointOfSaleId.MALAYSIA else PointOfSaleId.UNITED_STATES
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        return suggestion
    }

    private fun setUpTestToStartAtDetailsScreen() {
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams()
        hotelPresenter.show(hotelPresenter.detailPresenter)
        webCheckoutViewObservable = TestObserver<Unit>()
        (hotelPresenter.webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).fireCreateTripObservable.subscribe(webCheckoutViewObservable)
    }

    private fun givenHotelDetailsScreen(bucketWebCheckoutABTest: Boolean = false, setPOSWithWebCheckoutABTestEnabled: Boolean = false): Action {
        bucketWebCheckoutABTest(bucketWebCheckoutABTest)
        setPOSWithWebCheckoutABTestEnabled(setPOSWithWebCheckoutABTestEnabled)
        setUpTestToStartAtDetailsScreen()
        return Action()
    }

    private fun assertNativeHomeScreenShown(testShowNativeObserver: TestObserver<Unit>,
                                            maskWebCheckoutActivityObservable: TestObserver<Boolean>,
                                            testUrlObservable: TestObserver<String>) {
        testShowNativeObserver.assertValueCount(1)
        maskWebCheckoutActivityObservable.assertValues(true, false)
        testUrlObservable.assertValue("about:blank")
    }

    private fun setupShowNativeTestObservers(): Triple<TestObserver<Boolean>, TestObserver<String>, TestObserver<Unit>> {
        val maskWebCheckoutActivityObservable = TestObserver.create<Boolean>()
        val testUrlObservable = TestObserver.create<String>()
        val testShowNativeObserver = TestObserver.create<Unit>()
        hotelPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(maskWebCheckoutActivityObservable)
        hotelPresenter.webCheckoutView.viewModel.showNativeSearchObservable.subscribe(testShowNativeObserver)
        hotelPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testUrlObservable)
        return Triple(maskWebCheckoutActivityObservable, testUrlObservable, testShowNativeObserver)
    }

    private inner class Action {

        fun whenHotelRoomSelected(): Action {
            val hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
            hotelPresenter.hotelDetailViewModel.roomSelectedSubject.onNext(hotelRoomResponse)
            return this
        }

        fun thenTheWebViewDoesNotBecomeVisible() {
            webCheckoutViewObservable.assertValueCount(0)
        }

        fun thenTheWebViewBecomesVisible() {
            webCheckoutViewObservable.assertValueCount(1)
        }
    }
}
