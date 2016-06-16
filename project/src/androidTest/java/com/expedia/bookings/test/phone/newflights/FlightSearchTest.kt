package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.expedia.bookings.utils.DateUtils
import org.joda.time.LocalDate
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText

class FlightSearchTest: NewFlightTestCase() {

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
        assertHeaderHasText("Prices roundtrip per person")
    }

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
        assertHeaderHasText("Prices one-way per person")
    }

    private fun assertHeaderHasText(text: String) {
        FlightsResultsScreen.headerView().check(ViewAssertions.matches(ViewMatchers.withText(text)))
    }
}
