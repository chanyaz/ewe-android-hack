package com.expedia.bookings.test.pagemodels.flights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

object FlightsResultsScreen {

    @JvmStatic fun headerView(): ViewInteraction {
        return onView(withId(R.id.flight_results_price_header))
    }

    @JvmStatic fun dockedOutboundFlightSelection(): ViewInteraction {
        return onView(allOf(withId(R.id.docked_outbound_flight_selection), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @JvmStatic fun dockedOutboundFlightSelectionWidgetContainsText(text: String) {
        EspressoUtils.viewHasDescendantsWithText(R.id.docked_outbound_flight_selection, text)
    }

    @JvmStatic fun assertDeltaPrice(price: String) {
        onView(allOf(withId(R.id.price_per_person), withText(price))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @JvmStatic fun assertPaymentFeesMessageShowing(resultsView: Matcher<View>) {
        paymentFeeMessageTextView(resultsView).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.withText("There may be an additional fee based on your payment method.")))
    }

    fun paymentFeeMessageTextView(resultsView: Matcher<View>) = onView(allOf(withId(R.id.airline_payment_fees_header), ViewMatchers.isDescendantOfA(resultsView)))

    fun assertAirlineChargesFeesHeadingShown(resultsView: Matcher<View>, id: Int) {
        val airlineFeesHeaderView = onView(allOf(withId(R.id.airline_charges_fees_header), ViewMatchers.isDescendantOfA(resultsView)))
        airlineFeesHeaderView.perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(withText(id)))
    }
}
