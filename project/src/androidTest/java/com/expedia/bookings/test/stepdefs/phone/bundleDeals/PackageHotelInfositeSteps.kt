package com.expedia.bookings.test.stepdefs.phone.bundleDeals

import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSite.validateTravelDates
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSite.validateNumberOfGuests
import com.expedia.bookings.test.stepdefs.phone.TestUtil
import cucumber.api.java.en.Then

class PackageHotelInfositeSteps {

    @Then("^validate HIS screen is displayed with following travel dates and travelers$")
    @Throws(Throwable::class)
    fun validateHISTravelDetails(parameters: Map<String, String>) {
        validateTravelDates(TestUtil.getFormattedDateString(parameters["start_date"], parameters["end_date"]))
        validateNumberOfGuests(parameters["total_guests"]!!)
    }
}