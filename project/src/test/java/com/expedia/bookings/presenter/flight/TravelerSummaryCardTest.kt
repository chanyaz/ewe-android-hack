package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.test.traveler.MockTravelerProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner :: class)
@RunForBrands(brands = [(MultiBrand.EXPEDIA)])
class TravelerSummaryCardTest {
    private var activity: FragmentActivity by Delegates.notNull()
    private lateinit var checkoutPresenter: FlightCheckoutPresenter
    private val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        checkoutPresenter = LayoutInflater.from(activity).inflate(R.layout.test_flight_checkout_presenter, null) as FlightCheckoutPresenter
    }

    @Test
    fun testAccessibilityOnTravelerSummaryCardWithOneTraveler() {
        assertEquals("Enter traveler details Button", checkoutPresenter.travelerSummaryCard.contentDescription)
    }

    @Test
    fun testAccessibilityOnTravelerSummaryWithMultipleTraveler() {
        mockTravelerProvider.updateDBWithMockTravelers(3, Traveler())
        checkoutPresenter.travelersPresenter.viewModel.travelersCompletenessStatus.onNext(TravelerCheckoutStatus.CLEAN)

        assertEquals("Enter traveler details Button", checkoutPresenter.travelerSummaryCard.contentDescription)
    }

    @Test
    fun testAccessibilityOnTravelerSummaryWithMultipleTravelerCompleteData() {
        mockTravelerProvider.updateDBWithMockTravelers(3, mockTravelerProvider.getCompleteMockTraveler())
        checkoutPresenter.travelersPresenter.viewModel.travelersCompletenessStatus.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals("Oscar The Grouch, + 2 additional travelers, traveler details complete. Button.", checkoutPresenter.travelerSummaryCard.contentDescription)
    }

    @Test
    fun testAccessibilityOnTravelerSummaryWithMultipleTravelerIncompleteData() {
        mockTravelerProvider.updateDBWithMockTravelers(3, mockTravelerProvider.getCompleteTraveler())
        checkoutPresenter.travelersPresenter.viewModel.travelersCompletenessStatus.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals("Oscar The Grouch Error: Enter missing traveler details. Button.", checkoutPresenter.travelerSummaryCard.contentDescription)
    }
}
