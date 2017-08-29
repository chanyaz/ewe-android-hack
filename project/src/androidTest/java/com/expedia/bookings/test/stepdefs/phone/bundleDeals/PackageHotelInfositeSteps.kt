package com.expedia.bookings.test.stepdefs.phone.bundleDeals

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.stepdefs.phone.TestUtil
import cucumber.api.java.en.Then
import org.hamcrest.Matchers

class PackageHotelInfositeSteps {

    @Then("^validate HIS screen is displayed with following travel dates and travelers$")
    @Throws(Throwable::class)
    fun validateTravelDetailsHIS(parameters: Map<String, String>) {
        validateHotelTravelDatesHIS(TestUtil.getFormattedDateString(parameters["start_date"], parameters["end_date"]) + ", " + parameters["total_guests"])
    }

    private fun validateHotelTravelDatesHIS(dateString: String) {
        onView(Matchers.allOf<View>(withId(R.id.hotel_search_info), withText(Matchers.containsString("guests"))))
                .check(matches(withText(Matchers.containsString(dateString))))
    }
}