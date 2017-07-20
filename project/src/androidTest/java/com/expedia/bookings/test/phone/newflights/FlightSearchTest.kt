package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsResultsScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.expedia.bookings.utils.DateUtils
import org.joda.time.LocalDate
import org.junit.Test

class FlightSearchTest: NewFlightTestCase() {

    @Test
    fun testAirportDoesNotAutoAdvanceSecondTime() {
        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOriginAndDestination()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToMMMd(endDate)
        SearchScreen.selectDateButton().check(matches(withText("$expectedStartDate - $expectedEndDate")))

        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOrigin()
        SearchScreen.origin().check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testReturnSearch() {
        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOriginAndDestination()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToMMMd(endDate)
        SearchScreen.selectDateButton().check(matches(withText("$expectedStartDate - $expectedEndDate")))

        SearchScreen.searchButton().perform(click())

        FlightsScreen.outboundFlightList().check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        assertHeaderHasText("Prices roundtrip, per person.")
    }

    @Test
    fun testOriginSameAsDestination() {
        SearchScreen.origin().perform(click())
        SearchScreen.selectSameFlightOriginAndDestination()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToMMMd(endDate)
        SearchScreen.selectDateButton().check(matches(withText("$expectedStartDate - $expectedEndDate")))

        SearchScreen.searchButton().perform(click())
        SearchScreen.errorDialog("Please make sure your departure and arrival cities are in different places.").check(matches(isDisplayed()));
    }

    @Test
    fun testSameDayReturnSearch() {
        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOriginAndDestination()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(3)
        SearchScreen.selectDates(startDate, endDate)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToMMMd(endDate)
        SearchScreen.selectDateButton().check(matches(withText("$expectedStartDate - $expectedEndDate")))
    }

    @Test
    fun testOneWaySearch() {
        FlightsScreen.selectOneWay()
        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOriginAndDestination()

        val startDate = LocalDate.now().plusDays(3)
        FlightsScreen.selectDate(startDate)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        SearchScreen.selectDateButton().check(matches(withText("$expectedStartDate (One Way)")))

        SearchScreen.searchButton().perform(click())

        FlightsScreen.outboundFlightList().check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        assertHeaderHasText("Prices one-way, per person.")
    }

    private fun assertHeaderHasText(text: String) {
        FlightsResultsScreen.headerView().check(ViewAssertions.matches(ViewMatchers.withText(text)))
    }
}
