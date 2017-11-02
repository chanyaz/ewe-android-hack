package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.presenter.BottomCheckoutContainer
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class BottomCheckoutContainerTest {

    private val context = RuntimeEnvironment.application

    lateinit private var bottomContainer: BottomCheckoutContainer

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        val widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        bottomContainer = widget.bottomCheckoutContainer
    }

    @Test
    fun testSliderPurchaseTotalText() {
        val sliderPurchaseTotalTestTestSubscriber = TestObserver<CharSequence>()
        bottomContainer.viewModel.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTestTestSubscriber)

        val totalChargedString = "Your card will be charged $50.00"
        bottomContainer.viewModel.sliderPurchaseTotalText.onNext(totalChargedString)
        assertEquals(totalChargedString, sliderPurchaseTotalTestTestSubscriber.values()[0])
    }

    @Test
    fun testSliderPurchaseNullText() {
        bottomContainer.viewModel.sliderPurchaseTotalText.onNext("")
        bottomContainer.toggleSlideToPurchaseText(true)

        assertEquals(View.VISIBLE, bottomContainer.slideToPurchaseSpace.visibility)
        assertEquals(View.GONE, bottomContainer.slideTotalText.visibility)
    }

    @Test
    fun testToggleBottomContainerViews() {
        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.BUNDLE, false)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.VISIBLE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.GONE)

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.CHECKOUT, true)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.GONE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.VISIBLE)

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.CHECKOUT, false)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.VISIBLE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.GONE)

        bottomContainer.toggleBottomContainerViews(TwoScreenOverviewState.OTHER, false)
        assertTrue(bottomContainer.checkoutButtonContainer.visibility == View.GONE)
        assertTrue(bottomContainer.slideToPurchase.visibility == View.GONE)
    }

    @Test
    fun testBottomContainerState() {
        val testSubscriber = TestObserver.create<Boolean>()
        bottomContainer.viewModel.checkoutButtonEnableObservable.subscribe(testSubscriber)
        bottomContainer.viewModel.resetPriceWidgetObservable.onNext(Unit)
        testSubscriber.assertValueCount(1)
        assertFalse(bottomContainer.checkoutButton.isEnabled)
        bottomContainer.viewModel.checkoutButtonEnableObservable.onNext(true)
        assertTrue(bottomContainer.checkoutButton.isEnabled)
    }
}