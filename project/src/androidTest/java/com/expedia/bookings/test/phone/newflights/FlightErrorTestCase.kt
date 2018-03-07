package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.expedia.bookings.widget.TextView
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.hamcrest.Matchers
import org.joda.time.LocalDate

open class FlightErrorTestCase : NewFlightTestCase() {

    protected fun searchFlights(suggestionResponseType: FlightApiMockResponseGenerator.SuggestionResponseType, isOneWay: Boolean = true) {
        if (isOneWay) {
            Espresso.onView(Matchers.allOf(withText("One way"),
                    isDescendantOfA(withId(R.id.tabs)))).perform(ViewActions.click())
        }
        SearchScreenActions.selectFlightOriginAndDestination(suggestionResponseType, 0)
        val startDate = LocalDate.now().plusDays(3)
        if (isOneWay) {
            SearchScreenActions.chooseDatesWithDialog(startDate, null)
        } else {
            SearchScreenActions.chooseDatesWithDialog(startDate, startDate.plusDays(5))
        }
        SearchScreen.searchButton().perform(ViewActions.click())
    }

    protected fun assertConfirmationViewIsDisplayed() {
        onView(withId(R.id.confirmation_container))
                .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay())
                .check(matches(isDisplayed()))
    }

    protected fun assertPaymentFormIsDisplayed() {
        onView(withId(R.id.payment_info_card_view))
                .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay())
                .check(matches(isDisplayed()))
    }

    protected fun assertSearchFormDisplayed() {
        SearchScreen.origin()
                .perform(waitForViewToDisplay())
                .check(matches(isDisplayed()))
    }

    protected fun clickActionButton() {
        onView(withId(R.id.error_action_button)).perform(ViewActions.click())
    }

    protected fun assertGenericErrorShown() {
        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Retry")
        assertErrorTextDisplayed("Whoops. Let's try that again.")
        assertToolbarTitle("Error")
    }

    protected fun assertToolbarTitle(title: String) {
        onView(Matchers.allOf(isDescendantOfA(withId(R.id.error_toolbar)),
                withClassName(Matchers.`is`(TextView::class.java.name)),
                withText(title)))
    }

    protected fun assertErrorTextDisplayed(errorText: String) {
        onView(withId(R.id.error_text))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText(errorText)))
    }

    protected fun assertButtonDisplayedWithText(buttonText: String) {
        onView(withId(R.id.error_action_button))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText(buttonText)))
    }

    protected fun assertFlightErrorPresenterDisplayed() {
        onView(withId(R.id.error_presenter))
                .perform(waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
    }

    protected fun selectOutboundFlight(searchResultsResponseType: FlightApiMockResponseGenerator.SearchResultsResponseType) {
        selectOutboundFlight(searchResultsResponseType.responseName)
    }

    protected fun selectOutboundFlight(code: ApiError.Code) {
        selectOutboundFlight(code.toString())
    }

    private fun selectOutboundFlight(flight: String) {
        FlightsScreen.outboundFlightList().perform(waitForViewToDisplay())
        FlightsScreen.outboundFlightList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(flight)), ViewActions.click()))
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
    }

    protected fun selectFirstInboundFlight() {
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
    }
}
