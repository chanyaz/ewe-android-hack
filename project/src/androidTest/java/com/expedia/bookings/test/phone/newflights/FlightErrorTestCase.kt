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

open class FlightErrorTestCase : NewFlightTestCase() {


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

    fun searchForFlights(errorType: ApiError.Code, testType: TestType) {
        val originIndex = when (errorType) {
            ApiError.Code.UNKNOWN_ERROR -> if (testType == TestType.CREATE_TRIP) 8 else 12
            ApiError.Code.FLIGHT_SOLD_OUT -> 9
            ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND -> 10
            ApiError.Code.SESSION_TIMEOUT -> if (testType == TestType.CREATE_TRIP) 11 else 14
            ApiError.Code.PAYMENT_FAILED -> 13
            ApiError.Code.TRIP_ALREADY_BOOKED -> 15
            ApiError.Code.FLIGHT_SEARCH_NO_RESULTS -> 16

            else -> throw IllegalArgumentException("I do not support testing ${errorType.name} error types")
        }
        SearchScreen.selectFlightOriginAndDestination(originIndex, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())
    }

    protected fun selectFirstInboundFlight() {
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
    }

    protected fun selectFirstOutboundFlight() {
        FlightTestHelpers.assertFlightOutbound()
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
    }

    enum class TestType {
        CHECKOUT,
        CREATE_TRIP
    }
}
