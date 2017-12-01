package com.expedia.bookings.test.stepdefs.phone.bundleDeals

import com.expedia.bookings.R
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import cucumber.api.java.en.Then
import android.support.test.espresso.Espresso.onView
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import android.support.test.espresso.matcher.ViewMatchers.withId

class PackageHotelDetailsSteps {

    @Then("^I select room at position (\\d+)$")
    @Throws(Throwable::class)
    fun selectRoomAtPosition(position: Int) {
        onView(withId(R.id.hotel_details_toolbar)).perform(waitForViewToDisplay())
        HotelInfoSiteScreen.bookRoomAtIndex(position - 1)
    }
}
