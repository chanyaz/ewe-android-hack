package com.expedia.bookings.test.robolectric

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.presenter.BottomCheckoutContainer
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class BottomCheckoutContainerTest {

    private val context = RuntimeEnvironment.application

    lateinit private var bottomContainer: BottomCheckoutContainer

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        val widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        bottomContainer = widget.bottomCheckoutContainer
    }

    @Test
    fun testSliderPurchaseTotalText() {
        val sliderPurchaseTotalTestTestSubscriber = TestSubscriber<CharSequence>()
        bottomContainer.viewModel.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTestTestSubscriber)

        val totalChargedString = "Your card will be charged $50.00"
        bottomContainer.viewModel.sliderPurchaseTotalText.onNext(totalChargedString)
        assertEquals(totalChargedString, sliderPurchaseTotalTestTestSubscriber.onNextEvents[0])
    }

    @Test
    fun testToggleBottomContainerViews() {
        val disabledSTPStateEnabled = true

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.BUNDLE, false, disabledSTPStateEnabled)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.VISIBLE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.GONE)

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.CHECKOUT, true, disabledSTPStateEnabled)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.GONE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.VISIBLE)

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.CHECKOUT, false, disabledSTPStateEnabled)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.VISIBLE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.GONE)

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.OTHER, false, disabledSTPStateEnabled)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.GONE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.GONE)
    }
}