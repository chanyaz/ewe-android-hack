package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.ActivityAvailabilities
import com.expedia.bookings.data.lx.AvailabilityInfo
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.lx.Offer
import com.expedia.bookings.data.lx.Ticket
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.lx.LXPresenter
import com.expedia.bookings.presenter.lx.LXSearchPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.TuneUtils
import com.expedia.bookings.utils.TuneUtilsTests
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.util.Optional
import com.expedia.bookings.lx.vm.LXWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LXPresenterTest {
    lateinit var lxPresenter: LXPresenter
    lateinit var activity: Activity

    val thrown: ExpectedException = ExpectedException.none()
        @Rule get

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultLXComponents()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
    }

    @Test
    fun testDisplayOfLXWebCheckoutView() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()

        assertEquals(View.VISIBLE, lxPresenter.webCheckoutView.visibility)
        assertEquals(View.GONE, lxPresenter.detailsPresenter.visibility)
    }

    @Test
    fun testLXErrorDialogDisplayedOnNetworkLoss() {
        setupPresenterAndBucketWebviewTest()
        setupLxState()
        showWebCheckoutView()

        (lxPresenter.webCheckoutViewViewModel as LXWebCheckoutViewViewModel).lxCreateTripViewModel.noNetworkObservable.onNext(Unit)

        val errorDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(true, errorDialog.isShowing)

        val shadowErrorDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())

        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", shadowErrorDialog.message)
    }

    @Test
    fun testCloseWebviewOnNext() {
        setupPresenterAndBucketWebviewTest()
        val testClearWebviewSubscriber = TestObserver.create<String>()
        (lxPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).webViewURLObservable.subscribe(testClearWebviewSubscriber)
        showWebCheckoutView()

        val testUrl = getLxCheckoutUrl("happy_path")
        lxPresenter.webCheckoutView.webView.loadUrl(testUrl)
        lxPresenter.onCloseWebView.onNext(Unit)
        lxPresenter.webCheckoutView.onPageFinished(testUrl)

        testClearWebviewSubscriber.assertValue("about:blank")
        assertFalse(lxPresenter.webCheckoutView.webView.canGoBack())
    }

    @Test
    fun testOnBackClickObserver() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        lxPresenter.webCheckoutView.webView.loadUrl(getLxCheckoutUrl("happy_path"))
        lxPresenter.webCheckoutView.webView.loadUrl(getLxCheckoutUrl("test_path"))
        lxPresenter.onBackClickObserver.onNext(Unit)

        assertEquals(getLxCheckoutUrl("happy_path"), lxPresenter.webCheckoutView.webView.url)
    }

    @Test
    fun testBlankViewObserver() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        assertEquals(3, lxPresenter.backStack.size)
        lxPresenter.blankViewObserver.onNext(Unit)

        assertEquals(2, lxPresenter.backStack.size)
    }

    @Test
    fun testLxPresenterBackWebViewState() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        lxPresenter.webCheckoutView.webView.loadUrl(getLxCheckoutUrl("happy_path"))
        lxPresenter.webCheckoutView.webView.loadUrl(getLxCheckoutUrl("test_path"))
        lxPresenter.webCheckoutView.webView.loadUrl(getLxCheckoutUrl("back_path"))
        assertEquals(3, lxPresenter.backStack.size)

        assertTrue(lxPresenter.back())

        assertEquals(View.VISIBLE, lxPresenter.webCheckoutView.visibility)
        assertEquals(View.GONE, lxPresenter.detailsPresenter.visibility)
        assertTrue(lxPresenter.webCheckoutView.webView.canGoBack())
        assertEquals(3, lxPresenter.backStack.size)
    }

    @Test
    fun testLxPresenterBackLoadingGone() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        lxPresenter.show(lxPresenter.detailsPresenter)
        lxPresenter.loadingOverlay.visibility = View.GONE

        assertTrue(lxPresenter.back())
    }

    @Test
    fun testLxPresenterBackNoWebviewLoadingVisible() {
        setupPresenterAndBucketWebviewTest()
        lxPresenter.show(lxPresenter.detailsPresenter)
        lxPresenter.show(lxPresenter.webCheckoutView)
        lxPresenter.loadingOverlay.visibility = View.VISIBLE

        assertTrue(lxPresenter.back())
    }

    @Test
    fun testLxPresenterShownOnWebCheckoutViewError() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        val webView = lxPresenter.webCheckoutView

        val testShowNativeSearchObserver = TestObserver.create<Unit>()
        val testWebViewURLObserver = TestObserver.create<String>()
        val testShowWebViewObservable = TestObserver.create<Boolean>()
        webView.viewModel.showNativeSearchObservable.subscribe(testShowNativeSearchObserver)
        webView.viewModel.webViewURLObservable.subscribe(testWebViewURLObserver)
        webView.viewModel.showWebViewObservable.subscribe(testShowWebViewObservable)

        lxPresenter.webCheckoutView.goToSearchAndClearWebView()

        testShowNativeSearchObserver.assertValueCount(1)
        testWebViewURLObserver.assertValue("about:blank")
        testShowWebViewObservable.assertValue(false)
        assertTrue(lxPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(lxPresenter.searchParamsWidget.visibility == View.VISIBLE)
    }

    @Test
    fun testMaskScreenWhenGoingFromDetailsToWebCko() {
        setupPresenterAndBucketWebviewTest()
        val testShouldMaskScreenObservable = TestObserver.create<Boolean>()
        lxPresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(testShouldMaskScreenObservable)
        showWebCheckoutView()
        testShouldMaskScreenObservable.assertValue(true)
    }

    @Test
    fun testWebViewToSearchUpdateTransition() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        val webViewToSearchTransition = lxPresenter.getTransition(WebCheckoutView::class.java.name, LXSearchPresenter::class.java.name).transition
        val searchPresenter = lxPresenter.searchParamsWidget
        searchPresenter.toolbarTitleTop = 1
        webViewToSearchTransition.updateTransition(1f, true)

        assertEquals(searchPresenter.background, ContextCompat.getDrawable(activity, R.color.white))
        assertEquals(1.0f, searchPresenter.navIcon.parameter)
        assertEquals(1.0f, searchPresenter.toolBarTitle.alpha)
        assertEquals(1.0f, searchPresenter.alpha)
        assertEquals(0f, searchPresenter.toolBarTitle.translationY)
        assertEquals(0f, searchPresenter.searchButton.translationY)
        assertEquals(0f, searchPresenter.searchContainer.translationY)
    }

    @Test
    fun testWebViewToSearchFinalizeTransition() {
        setupPresenterAndBucketWebviewTest()
        showWebCheckoutView()
        val webViewToSearchTransition = lxPresenter.getTransition(WebCheckoutView::class.java.name, LXSearchPresenter::class.java.name).transition
        val searchPresenter = lxPresenter.searchParamsWidget
        searchPresenter.navIcon.parameter = 0f

        webViewToSearchTransition.endTransition(true)

        assertEquals(ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type.toFloat(), searchPresenter.navIcon.parameter)
        assertTrue(searchPresenter.toolbar.findFocus() is ImageButton)
    }

    @Test
    fun testDoNotShowNativeCheckoutWithWebviewEnabled() {
        setupPresenterAndBucketWebviewTest()
        Events.register(lxPresenter.checkoutPresenter)
        lxPresenter.show(lxPresenter.resultsPresenter)
        lxPresenter.show(lxPresenter.detailsPresenter)
        Events.post(Events.LXOfferBooked(Offer(), listOf(getAdultTicket())))

        assertFalse(lxPresenter.backStack.contains(lxPresenter.checkoutPresenter))
    }

    @Test
    fun testCustomExceptionWasThrownWhenTransitionIsNotSet() {
        lxPresenter = LayoutInflater.from(activity).inflate(R.layout.lx_base_layout, null) as LXPresenter
        thrown.expect(LXPresenter.LXMissingTransitionException::class.java)
        thrown.expectMessage("No Transition defined for Test screen 1 to Test screen 2")
        lxPresenter.getTransition("Test screen 1", "Test screen 2")
    }

    @Test
    fun testTuneTrackedOnWebViewShown() {
        setupPresenterAndBucketWebviewTest()
        val tune = TuneUtilsTests.TestTuneTrackingProviderImpl()
        TuneUtils.init(tune)
        showWebCheckoutView()
        setupLxState()
        val createTripResponse = LXCreateTripResponseV2()

        lxPresenter.createTripResponseObserver.onNext(Optional(createTripResponse))

        assertNotNull(tune.trackedEvent)
    }

    private fun setupLxState() {
        val activity = LXActivity()
        activity.location = "Tahoe"
        activity.categories = listOf("Fun", "Outdoors", "Family")
        activity.title = "Ziplining in Tahoe"
        activity.regionId = "12345"
        activity.id = "9876"
        activity.destination = "Tahoe, CA"
        lxPresenter.lxState.activity = activity

        lxPresenter.lxState.offer = getOffer()
        lxPresenter.lxState.searchParams = LxSearchParams.Builder()
                .location("Tahoe")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as LxSearchParams
    }

    private fun getOffer(): Offer {
        val offer = Offer()
        offer.availabilityInfoOfSelectedDate = AvailabilityInfo()
        offer.availabilityInfoOfSelectedDate.availabilities = ActivityAvailabilities()
        offer.availabilityInfoOfSelectedDate.availabilities.valueDate = "2025-12-10 12:30:00"
        return offer
    }

    private fun showWebCheckoutView() {
        lxPresenter.show(lxPresenter.resultsPresenter)
        lxPresenter.show(lxPresenter.detailsPresenter)
        lxPresenter.show(lxPresenter.webCheckoutView)
        Events.post(Events.LXOfferBooked(getOffer(), listOf(getAdultTicket())))
    }

    private fun setupPresenterAndBucketWebviewTest() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppLxWebCheckoutView)
        lxPresenter = LayoutInflater.from(activity).inflate(R.layout.lx_base_layout, null) as LXPresenter
        Events.register(lxPresenter)
    }

    private fun getLxCheckoutUrl(tripId: String): String {
        return (lxPresenter.webCheckoutView.viewModel as LXWebCheckoutViewViewModel)
                .endpointProvider.getE3EndpointUrlWithPath("MultiItemCheckout?tripid=$tripId")
    }

    private fun getAdultTicket(): Ticket {
        val gson = GsonBuilder().create()
        val adultTicket = gson.fromJson(
                "{\"code\": \"Adult\",\"count\": \"3 \", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"$145\", \"amount\": \"130\", \"originalAmount\": \"145\", \"displayName\": null, \"defaultTicketCount\": 2 }",
                Ticket::class.java)
        adultTicket.money = Money("100", "USD")
        return adultTicket
    }
}
