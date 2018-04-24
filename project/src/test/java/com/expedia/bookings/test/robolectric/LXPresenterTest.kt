package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.Offer
import com.expedia.bookings.data.lx.Ticket
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.lx.LXPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.LXWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LXPresenterTest {
    lateinit var lxPresenter: LXPresenter
    lateinit var activity: Activity

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

    private fun showWebCheckoutView() {
        lxPresenter.show(lxPresenter.resultsPresenter)
        lxPresenter.show(lxPresenter.detailsPresenter)
        lxPresenter.show(lxPresenter.webCheckoutView)
        Events.post(Events.LXOfferBooked(Offer(), listOf(getAdultTicket())))
    }

    private fun setupPresenterAndBucketWebviewTest() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppLxWebCheckoutView)
        lxPresenter = LayoutInflater.from(activity).inflate(R.layout.lx_base_layout, null) as LXPresenter
        Events.register(lxPresenter)
    }

    private fun getLxCheckoutUrl(tripId: String): String {
        return (lxPresenter.webCheckoutView.viewModel as LXWebCheckoutViewViewModel)
                .endpointProvider.getE3EndpointUrlWithPath("${PointOfSale.getPointOfSale().lxWebCheckoutPath}?tripid=$tripId")
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
