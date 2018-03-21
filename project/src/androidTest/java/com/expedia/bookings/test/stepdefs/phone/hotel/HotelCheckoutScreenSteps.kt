package com.expedia.bookings.test.stepdefs.phone.hotel

import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import java.util.concurrent.TimeUnit

class HotelCheckoutScreenSteps {

    @And("^I wait for checkout to load$")
    @Throws(Throwable::class)
    fun iWaitCheckoutLoad() {
        CheckoutScreen.hintContainer().perform(ViewActions.waitFor(isDisplayed(), 10, TimeUnit.SECONDS))
    }

    @Then("^I verify fees breakdown appear$")
    @Throws(Throwable::class)
    fun iVerifyFeesBreakdownAppear() {
        HotelCheckoutScreen.verifyFeesBreakdownAppear()
    }

    @And("^I verify fees disclaimer appear$")
    @Throws(Throwable::class)
    fun iVerifyFeesDisclaimerAppear() {
        HotelCheckoutScreen.verifyFeesDisclaimerAppear()
    }
}
