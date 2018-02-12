package com.expedia.bookings.test.stepdefs.phone

import com.expedia.bookings.test.pagemodels.common.TripsScreen
import cucumber.api.java.en.And

class TripsScreenSteps {
    @And("^I verify that hotel with name \"(.*?)\" is present")
    @Throws(Throwable::class)
    fun verifyHotelName(hotelName: String) {
        TripsScreen.verifyTripItemWithNameIsPresent(hotelName)
    }
}
