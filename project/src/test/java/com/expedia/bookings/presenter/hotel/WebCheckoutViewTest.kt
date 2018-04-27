package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.vm.HotelWebCheckoutViewViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class WebCheckoutViewTest {
    lateinit var webCheckoutView: WebCheckoutView
    lateinit var activity: Activity

    @Test
    fun testLoadingOfWebCheckoutView() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        webCheckoutView = LayoutInflater.from(activity).inflate(R.layout.web_checkout_view_stub, null) as WebCheckoutView
        webCheckoutView.viewModel = HotelWebCheckoutViewViewModel(activity, Ui.getApplication(activity).appComponent().endpointProvider())
        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "www.Google.com", null)

        assertEquals(View.VISIBLE, webCheckoutView.loadingOverlay.visibility)

        webCheckoutView.webClient.onPageFinished(webCheckoutView.webView, "www.Google.com")

        assertEquals(View.GONE, webCheckoutView.loadingOverlay.visibility)
    }
}
