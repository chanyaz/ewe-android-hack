package com.expedia.vm.test

import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightOverviewPresenterTest{
    private var activity: PlaygroundActivity by Delegates.notNull()
    private val context = RuntimeEnvironment.application

    @Before fun before() {
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
    }

    @Test
    fun testCheckoutButtonTextInControl() {
        val flightOverviewPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        assertEquals("Checkout", flightOverviewPresenter.bottomCheckoutContainer.checkoutButton.text)
    }

    @Test
    fun testCheckoutButtonTextVariate1() {
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppCheckoutButtonText, AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal)
        val flightOverviewPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        assertEquals("Continue Booking", flightOverviewPresenter.bottomCheckoutContainer.checkoutButton.text)
    }

    @Test
    fun testCheckoutButtonTextVariate2() {
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppCheckoutButtonText, AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal)
        val flightOverviewPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        assertEquals("Next", flightOverviewPresenter.bottomCheckoutContainer.checkoutButton.text)
    }

}