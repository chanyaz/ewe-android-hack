package com.expedia.bookings.test.stepdefs.phone.hotel

import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelsCheckoutWebViewScreen
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

    @And("^I wait for checkout webview to load$")
    @Throws(Throwable::class)
    fun iWaitCheckoutWebViewLoad() {
        HotelsCheckoutWebViewScreen.waitForViewToLoad()
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

    @And("^I populate contact name field with '(.*?)'$")
    @Throws(Throwable::class)
    fun iPopulateContactName(string: String) {
        HotelsCheckoutWebViewScreen.Preferences.scrollToRoomPreferences()
        HotelsCheckoutWebViewScreen.Preferences.enterContactName(string)
    }
    @Then("^I verify contact name field contains '(.*?)'$")
    @Throws(Throwable::class)
    fun iVerifyContactName(string: String) {
        HotelsCheckoutWebViewScreen.Preferences.scrollToRoomPreferences()
        HotelsCheckoutWebViewScreen.Preferences.verifyContactNameFieldValue(string)
    }
    @And("^I (accept|decline) insurance$")
    @Throws(Throwable::class)
    fun iAcceptOrDeclineInsurance(string: String) {
        HotelsCheckoutWebViewScreen.Insurance.scrollToInsuranceContainer()
        when (string) {
            "decline" -> HotelsCheckoutWebViewScreen.Insurance.clickDeclineInsurance()
            else -> throw IllegalArgumentException("'$string' option has not been developed")
        }
    }
    @Then("^I verify insurance has been (accepted|declined)$")
    @Throws(Throwable::class)
    fun iVerifyInsuranceHasBeenAcceptedOrDeclined(string: String) {
        HotelsCheckoutWebViewScreen.Insurance.scrollToInsuranceContainer()
        when (string) {
            "declined" -> HotelsCheckoutWebViewScreen.Insurance.verifyTripNotProtected()
            else -> throw IllegalArgumentException("'$string' option has not been developed")
        }
    }
}
