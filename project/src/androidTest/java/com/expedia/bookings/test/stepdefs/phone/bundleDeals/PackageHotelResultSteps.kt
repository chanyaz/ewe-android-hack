package com.expedia.bookings.test.stepdefs.phone.bundleDeals

import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import cucumber.api.java.en.Then

class PackageHotelResultSteps {

    @Then("^Validate unreal deal is displayed$")
    @Throws(Throwable::class)
    fun validateUnrealDealAtPosition(parameters: Map<String, String>) {
        val position = parameters["position"]!!.toInt()
        val title = parameters["title"]
        val message = parameters["message"]
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), position, R.id.unreal_deal_heading, title)
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), position, R.id.unreal_deal_message, message)
    }
}