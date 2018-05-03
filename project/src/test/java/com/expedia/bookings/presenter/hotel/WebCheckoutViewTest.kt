package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.vm.HotelWebCheckoutViewViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class WebCheckoutViewTest {
    lateinit var webCheckoutView: WebCheckoutView
    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        webCheckoutView = LayoutInflater.from(activity).inflate(R.layout.web_checkout_view_stub, null) as WebCheckoutView
        webCheckoutView.viewModel = HotelWebCheckoutViewViewModel(activity, Ui.getApplication(activity).appComponent().endpointProvider())
        webCheckoutView.visibility = View.VISIBLE
    }

    @Test
    fun testLoadingOfWebCheckoutView() {

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "www.Google.com", null)

        assertEquals(View.VISIBLE, webCheckoutView.loadingOverlay.visibility)

        webCheckoutView.webClient.onPageFinished(webCheckoutView.webView, "www.Google.com")

        assertEquals(View.GONE, webCheckoutView.loadingOverlay.visibility)
    }

    @Test
    fun testOnWebPageStartedHidesWebViewIfPopUpAvailable() {
        webCheckoutView.webViewPopUp = WebView(activity)

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "www.Google.com", null)

        assertEquals(View.GONE, webCheckoutView.webView.visibility)

        assertEquals(View.VISIBLE, webCheckoutView.webViewPopUp!!.visibility)
    }

    @Test
    fun testOnWebPageStartedShowsWebViewIfPopUpNotAvailable() {
        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "www.Google.com", null)
        assertEquals(View.VISIBLE, webCheckoutView.webView.visibility)
    }

    @Test
    fun testCheckoutErrorState() {
        val testShowNativeSearchObservable = TestObserver.create<Unit>()
        webCheckoutView.viewModel.showNativeSearchObservable.subscribe(testShowNativeSearchObservable)
        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "www.expedia.com/HotelCheckoutError", null)

        assertTrue(webCheckoutView.checkoutErrorState)

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "www.expedia.com/Checkout", null)

        assertEquals(1, testShowNativeSearchObservable.valueCount())
        assertFalse(webCheckoutView.checkoutErrorState)
    }
}
