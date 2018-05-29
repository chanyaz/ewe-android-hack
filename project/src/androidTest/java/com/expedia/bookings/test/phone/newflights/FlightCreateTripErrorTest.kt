package com.expedia.bookings.test.phone.newflights

import com.expedia.bookings.data.ApiError
import com.mobiata.mocke3.FlightDispatcherUtils
import org.junit.Test

class FlightCreateTripErrorTest : FlightErrorTestCase() {

    @Test
    fun testCreateTripUnknownError() {
        searchFlights(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.UNKNOWN_ERROR)
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
        searchFlights(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.FLIGHT_SOLD_OUT, 5)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("We're sorry. This flight has sold out.")
        assertToolbarTitle("Flight Sold Out")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripFlightProductNotFound() {
        searchFlights(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND, 6)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("We're sorry. This flight is no longer available")
        assertToolbarTitle("Flight Unavailable")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripSessionTimeout() {
        searchFlights(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.SESSION_TIMEOUT, 7)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("Still there? Your session has expired. Please try your search again.")
        assertToolbarTitle("Session Expired")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripNoResults() {
        searchFlights(FlightDispatcherUtils.SuggestionResponseType.SEARCH_ERROR)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Edit Search")

        clickActionButton()
        assertSearchFormDisplayed()
    }
}
