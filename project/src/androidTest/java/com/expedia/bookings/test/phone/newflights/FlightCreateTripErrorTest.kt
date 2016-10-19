package com.expedia.bookings.test.phone.newflights

import com.expedia.bookings.data.ApiError
import org.junit.Test

class FlightCreateTripErrorTest: FlightErrorTestCase() {

    @Test
    fun testCreateTripUnknownError() {
        searchForFlights(ApiError.Code.UNKNOWN_ERROR, FlightErrorTestCase.TestType.CREATE_TRIP)
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
        searchForFlights(ApiError.Code.FLIGHT_SOLD_OUT, FlightErrorTestCase.TestType.CREATE_TRIP)
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
        searchForFlights(ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND, FlightErrorTestCase.TestType.CREATE_TRIP)
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
        searchForFlights(ApiError.Code.SESSION_TIMEOUT, FlightErrorTestCase.TestType.CREATE_TRIP)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("Still there? Your session has expired. Please try your search again.")
        assertToolbarTitle("Session Expired")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCreateTripNoResults() {
        searchForFlights(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS, FlightErrorTestCase.TestType.CREATE_TRIP)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Edit Search")

        clickActionButton()
        assertSearchFormDisplayed()
    }


}