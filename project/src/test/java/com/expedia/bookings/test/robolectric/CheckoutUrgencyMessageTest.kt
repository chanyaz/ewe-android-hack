package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CheckoutUrgencyMessageTest {

    private val context = RuntimeEnvironment.application
    private lateinit var flightOverviewPresenter: FlightOverviewPresenter

    @Before
    fun setup() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppSeatsLeftUrgencyMessaging)
        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(isRoundTrip = false))
        val validator = Ui.getApplication(context).travelerComponent().travelerValidator()
        validator.updateForNewSearch(FlightTestUtil.getFlightSearchParams(isRoundTrip = false))
        flightOverviewPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        flightOverviewPresenter.viewModel.outboundSelectedAndTotalLegRank = Pair(0, 0)
    }

    @Test
    fun testDontShowUrgencyMessageInOverview() {
        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = false)
        flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())

        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.urgencyMessageContainer.visibility == View.GONE)
    }

    @Test
    fun testShowUrgencyMessageWhenGoingToCheckout() {
        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = true)

        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.urgencyMessageContainer.visibility == View.VISIBLE)
    }

    @Test
    fun testDontShowUrgencyMessageWhenGoingToCheckoutWithABTestOff() {
        turnOffABTest()

        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = true)

        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.urgencyMessageContainer.visibility == View.GONE)
    }

    @Test
    fun testDontShowUrgencyMessageWhenGoingToCheckout() {
        setupCreateTripAndGoToCheckout(remainingSeats = 6, goToCheckout = true)

        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.urgencyMessageContainer.visibility == View.GONE)
    }

    @Test
    fun testCorrectUrgencyMessage() {
        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = true)

        assertEquals("We have 2 seats left at this price", flightOverviewPresenter.bottomCheckoutContainer.urgencyMessage.text.toString())
    }

    @Test
    fun testCorrectUrgencyMessageWhenOneSeatLeft() {
        setupCreateTripAndGoToCheckout(remainingSeats = 1, goToCheckout = true)

        assertEquals("We have 1 seat left at this price", flightOverviewPresenter.bottomCheckoutContainer.urgencyMessage.text.toString())
    }

//    @Test
//    fun testShowUrgencyMessageWhenSlideToPurchaseIsVisible() {
//        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = false)
//        val traveler = Traveler()
//        TravelerTestUtils.completeTraveler(traveler)
//        flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
//        flightOverviewPresenter.getCheckoutPresenter().flightCheckoutViewModel.travelerCompleted.onNext(arrayListOf(traveler))
//        flightOverviewPresenter.getCheckoutPresenter().flightCheckoutViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(context))
//        flightOverviewPresenter.show(flightOverviewPresenter.getCheckoutPresenter())
//        flightOverviewPresenter.checkoutButton.performClick()
//
//        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.slideToPurchase.visibility == View.VISIBLE)
//        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.urgencyMessageContainer.visibility == View.VISIBLE)
//    }

    @Test
    fun testDontShowUrgencyMessageWhenGoingFromCheckoutBackToOverview() {
        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = true)
        flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())

        assertTrue(flightOverviewPresenter.bottomCheckoutContainer.urgencyMessageContainer.visibility == View.GONE)
    }

    @Test
    fun testUrgencyMessageDisplayedTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        setupCreateTripAndGoToCheckout(remainingSeats = 2, goToCheckout = false)
        flightOverviewPresenter.getCheckoutPresenter().flightCheckoutViewModel.toCheckoutTransitionObservable.onNext(true)
        flightOverviewPresenter.getCheckoutPresenter().flightCheckoutViewModel.bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)

        val controlEvar = mapOf(28 to "App.CKO.Urgency.Shown")
        OmnitureTestUtils.assertLinkTracked("Universal Checkout", "App.CKO.Urgency.Shown", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testUrgencyMessageDisplayedNotTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        setupCreateTripAndGoToCheckout(remainingSeats = 6, goToCheckout = false)
        flightOverviewPresenter.getCheckoutPresenter().flightCheckoutViewModel.toCheckoutTransitionObservable.onNext(true)
        flightOverviewPresenter.getCheckoutPresenter().flightCheckoutViewModel.bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)

        val controlEvar = mapOf(28 to "App.CKO.Urgency.Shown")
        OmnitureTestUtils.assertLinkNotTracked("Universal Checkout", "App.CKO.Urgency.Shown", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    private fun setupCreateTripAndGoToCheckout(remainingSeats: Int, goToCheckout: Boolean) {
        val createTripResponse = FlightTestUtil.getFlightCreateTripResponse(remainingSeats)
        Db.getTripBucket().add(TripBucketItemFlightV2(createTripResponse))
        flightOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().createTripResponseObservable.onNext(Optional(createTripResponse))
        if (goToCheckout) {
            flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
            flightOverviewPresenter.show(flightOverviewPresenter.getCheckoutPresenter())
            flightOverviewPresenter.checkoutButton.performClick()
        }
    }

    private fun turnOffABTest() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppSeatsLeftUrgencyMessaging)
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(isRoundTrip = false))
        val validator = Ui.getApplication(context).travelerComponent().travelerValidator()
        validator.updateForNewSearch(FlightTestUtil.getFlightSearchParams(isRoundTrip = false))
        flightOverviewPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        flightOverviewPresenter.viewModel.outboundSelectedAndTotalLegRank = Pair(0, 0)
    }
}
