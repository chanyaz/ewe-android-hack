package com.expedia.bookings.test.stepdefs.phone

import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen
import com.expedia.bookings.test.pagemodels.trips.TripsScreen
import cucumber.api.java.en.And
import cucumber.api.java.en.Then

class TripsScreenSteps {
    @Then("^I verify that trip item with name \"(.*?)\" is present")
    @Throws(Throwable::class)
    fun verifyHotelName(hotelName: String) {
        TripsScreen.verifyTripItemWithNameIsPresent(hotelName)
    }

    @And("^I tap on trip item with name \"(.*?)\"")
    @Throws(Throwable::class)
    fun tapHotelName(itemName: String) {
        TripsScreen.clickOnTripItemWithName(itemName)
        TripDetailsScreen.waitUntilLoaded()
    }

    @And("^I wait for trips screen to load")
    @Throws(Throwable::class)
    fun waitForTripsScreenToLoad(hotelName: String) {
        TripsScreen.waitForTripsViewToLoad()
    }
}
