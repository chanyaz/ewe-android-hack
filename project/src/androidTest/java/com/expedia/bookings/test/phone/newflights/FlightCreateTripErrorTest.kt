package com.expedia.bookings.test.phone.newflights

import com.expedia.bookings.data.ApiError
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.junit.Test

class FlightCreateTripErrorTest : FlightErrorTestCase() {

    @Test
    fun testCreateTripUnknownError() {
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH)
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
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.FLIGHT_SOLD_OUT)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("We're sorry. This flight has sold out.")
        assertToolbarTitle("Flight Sold Out")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripFlightProductNotFound() {
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("We're sorry. This flight is no longer available")
        assertToolbarTitle("Flight Unavailable")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripSessionTimeout() {
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(ApiError.Code.SESSION_TIMEOUT)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("Still there? Your session has expired. Please try your search again.")
        assertToolbarTitle("Session Expired")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripNoResults() {
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.SEARCH_ERROR)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Edit Search")

        clickActionButton()
        assertSearchFormDisplayed()
    }
}
