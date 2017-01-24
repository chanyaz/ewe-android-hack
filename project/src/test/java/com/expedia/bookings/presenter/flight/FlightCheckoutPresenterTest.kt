package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.FlightAndPackagesRulesActivity
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.packages.FlightTravelersPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.testutils.Assert.assertViewIsVisible
import com.expedia.vm.test.traveler.MockTravelerProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner :: class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))

class FlightCheckoutPresenterTest {

    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var checkoutPresenter: FlightCheckoutPresenter
    val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        checkoutPresenter = LayoutInflater.from(activity).inflate(R.layout.test_flight_checkout_presenter, null) as FlightCheckoutPresenter
    }

    @Test
    fun flightCheckoutPresenterDefaultState(){
        assertNotNull(checkoutPresenter)
        val accountButton = checkoutPresenter.loginWidget
        assertViewIsVisible(accountButton)
        val travlerDefaultState = checkoutPresenter.travelerSummaryCardView
        assertViewIsVisible(travlerDefaultState)
        val scrollView = checkoutPresenter.scrollView
        assertViewIsVisible(scrollView)
        val legalInformationText = checkoutPresenter.legalInformationText
        assertViewIsVisible(legalInformationText)
    }

    @Test
    fun shouldShowFlightAndPackagesRulesActivity() {
        checkoutPresenter.legalInformationText.performClick()
        val shadowActivity = shadowOf(activity)
        val intent = shadowActivity.getNextStartedActivity()
        val shadowIntent = shadowOf(intent)
        assertEquals(FlightAndPackagesRulesActivity::class.java!!.getName(), shadowIntent.getComponent().getClassName())
    }

    @Test
    fun shouldShowTravlerEntryWidget(){
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        checkoutPresenter.travelerSummaryCardView.findViewById(R.id.traveler_default_state).performClick()
        assertEquals(FlightTravelersPresenter::class.java.name, checkoutPresenter.currentState)
    }

}