package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.vm.LXWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.vm.lx.LXCreateTripViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner :: class)
class WebCheckoutViewTest {
    private var activity: FragmentActivity by Delegates.notNull()
    private lateinit var webCheckoutView: WebCheckoutView

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        webCheckoutView = LayoutInflater.from(activity).inflate(R.layout.web_checkout_view_stub, null) as WebCheckoutView

    }
    @Test
    fun testLXWebConfirmationPageInterceptedBeforeLoading() {
        val testSubscriber = TestObserver.create<String>()
        webCheckoutView.viewModel = LXWebCheckoutViewViewModel(activity, Ui.getApplication(activity).appComponent().endpointProvider(), LXCreateTripViewModel(activity))

        (webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(testSubscriber)
        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "https://wwwexpediacouk.trunk.sb.karmalab.net/trips/790170356787?tripnumber=790170356787&itinerarynumber=790170356787&tripid=f93e7d80-8a9f-4394-b1b6-af35fee7821a&frdr=true", null)

        assertEquals(0, testSubscriber.values().size)

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "https://wwwexpediacom.trunk.sb.karmalab.net/MultiItemBookingConfirmation/790170356787?tripnumber=790170356787&itinerarynumber=790170356787&tripid=f93e7d80-8a9f-4394-b1b6-af35fee7821a&frdr=true", null)

        assertEquals(0, testSubscriber.values().size)
    }
}

