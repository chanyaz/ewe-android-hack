package com.expedia.bookings.test.stepdefs.phone.bundleDeals

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.stepdefs.phone.TestUtil
import cucumber.api.java.en.Then
import org.hamcrest.Matchers

class PackageFlightDetailsSteps {

    @Then("^validate package flight detail screen is displayed with following travel dates and travelers$")
    @Throws(Throwable::class)
    fun validateTravelDetailsFD(parameters: Map<String, String>) {
        validateHotelTravelDatesFD(TestUtil.getDateInEEMMMddyyyy(parameters["travel_date"]) + ", " + parameters["Total_Travelers"] + " travelers")
    }

    private fun validateHotelTravelDatesFD(dateString: String) {
        onView(Matchers.allOf<View>(isDescendantOfA(withId(R.id.flights_toolbar)), withText(Matchers.containsString("travelers"))))
                .check(matches(withText(Matchers.containsString(dateString))))
    }
}
