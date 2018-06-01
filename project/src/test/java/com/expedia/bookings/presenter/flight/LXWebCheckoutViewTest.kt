package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.bookings.lx.vm.LXWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.expedia.bookings.lx.vm.LXCreateTripViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner :: class)
class LXWebCheckoutViewTest {
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
        val testBookedTripIdSubscriber = TestObserver.create<String>()

        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppLxWebCheckoutView)
        webCheckoutView.viewModel = LXWebCheckoutViewViewModel(activity, Ui.getApplication(activity).appComponent().endpointProvider(), LXCreateTripViewModel(activity))

        (webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(testBookedTripIdSubscriber)

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "https://wwwexpediacouk.trunk.sb.karmalab.net/MultiItemBookingConfirmation/790170356787?tripnumber=790170356787&itinerarynumber=790170356787&tripid=f93e7d80-8a9f-4394-b1b6-af35fee7821a&frdr=true", null)

        assertEquals(1, testBookedTripIdSubscriber.values().size)

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "https://wwwexpediacom.trunk.sb.karmalab.net/MultiItemBookingConfirmation/790170356787?tripnumber=790170356787&itinerarynumber=790170356787&tripid=f93e7d80-8a9f-4394-b1b6-af35fee7821a&frdr=true", null)

        assertEquals(2, testBookedTripIdSubscriber.values().size)
    }

    @Test
    fun testWebCheckoutViewHiddenOnHittingLXConfirmationURL() {
        val testShowWebViewObservableIdSubscriber = TestObserver.create<Boolean>()

        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppLxWebCheckoutView)
        webCheckoutView.viewModel = LXWebCheckoutViewViewModel(activity, Ui.getApplication(activity).appComponent().endpointProvider(), LXCreateTripViewModel(activity))

        (webCheckoutView.viewModel as WebCheckoutViewViewModel).showWebViewObservable.subscribe(testShowWebViewObservableIdSubscriber)

        webCheckoutView.onWebPageStarted(webCheckoutView.webView, "https://wwwexpediacom.trunk.sb.karmalab.net/MultiItemBookingConfirmation", null)

        (webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        assertEquals(1, testShowWebViewObservableIdSubscriber.values().size)
        assertFalse(testShowWebViewObservableIdSubscriber.values()[0])
    }
}
