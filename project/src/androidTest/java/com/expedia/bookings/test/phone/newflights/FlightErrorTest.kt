package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.expedia.bookings.widget.TextView
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Test

class FlightErrorTest: NewFlightTestCase() {

    @Test
    fun testCreateTripUnknownError() {
        searchForFlights(ApiError.Code.UNKNOWN_ERROR)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()
        assertGenericErrorShown()

        // assert createTrip retry called twice
        // fallback to search form on third attempt
        clickActionButton()
        assertGenericErrorShown()
        clickActionButton()
        assertGenericErrorShown()

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripFlightSoldOut() {
        searchForFlights(ApiError.Code.FLIGHT_SOLD_OUT)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("We're sorry. This flight has sold out.")
        assertToolbarTitle("Flight Sold Out")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripFlightProductNotFound() {
        searchForFlights(ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("We're sorry. This flight is no longer available")
        assertToolbarTitle("Flight Unavailable")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripSessionTimeout() {
        searchForFlights(ApiError.Code.SESSION_TIMEOUT)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("Still there? Your session has expired. Please try your search again.")
        assertToolbarTitle("Session Expired")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    private fun assertSearchFormDisplayed() {
        SearchScreen.origin()
                .perform(waitForViewToDisplay())
                .check(matches(isDisplayed()))
    }

    private fun clickActionButton() {
        onView(withId(R.id.error_action_button)).perform(ViewActions.click())
    }

    private fun assertGenericErrorShown() {
        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Retry")
        assertErrorTextDisplayed("Whoops. Let's try that again.")
        assertToolbarTitle("Error")
    }

    private fun assertToolbarTitle(title: String) {
        onView(Matchers.allOf(isDescendantOfA(withId(R.id.error_toolbar)),
                            withClassName(Matchers.`is`(TextView::class.java.name)),
                            withText(title)))
    }

    private fun assertErrorTextDisplayed(errorText: String) {
        onView(withId(R.id.error_text))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText(errorText)))
    }

    private fun assertButtonDisplayedWithText(buttonText: String) {
        onView(withId(R.id.error_action_button))
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText(buttonText)))
    }

    private fun assertFlightErrorPresenterDisplayed() {
        onView(withId(R.id.error_presenter))
                .perform(waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
    }

    private fun searchForFlights(errorType: ApiError.Code) {
        val originIndex = when (errorType) {
            ApiError.Code.UNKNOWN_ERROR -> 8
            ApiError.Code.FLIGHT_SOLD_OUT -> 9
            ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND -> 10
            ApiError.Code.SESSION_TIMEOUT -> 11

            else -> throw IllegalArgumentException("I do not support testing ${errorType.name} error types")
        }
        SearchScreen.selectFlightOriginAndDestination(originIndex, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())
    }

    private fun selectFirstInboundFlight() {
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
    }

    private fun selectFirstOutboundFlight() {
        FlightTestHelpers.assertFlightOutbound()
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
    }
}