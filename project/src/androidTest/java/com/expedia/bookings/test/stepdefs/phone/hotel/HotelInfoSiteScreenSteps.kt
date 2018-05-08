package com.expedia.bookings.test.stepdefs.phone.hotel

import android.support.test.espresso.action.ViewActions.scrollTo
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen.VIPAccess
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen.MandatoryFeesAndTaxes
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen.AdditionalFeeInfo
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.joda.time.LocalDate

class HotelInfoSiteScreenSteps {
    @Then("^I verify the VIP Access header text$")
    @Throws(Throwable::class)
    fun iVerifyLabelOnInfositeVIPAccessPage(cucumberTable: Map<String, String>) {
        VIPAccess.verifyHeaderText(cucumberTable["headerText"].toString())
    }

    @And("^I verify the body text on VIP Access page$")
    @Throws(Throwable::class)
    fun iVerifyTheBodyTextOnVIPAccessPage(cucumberTable: Map<String, String>) {
        VIPAccess.verifyBodyText(cucumberTable["bodyText"].toString())
    }

    @Then("^I verify that VIP Access label is present on hotel infosite page$")
    @Throws(Throwable::class)
    fun iVerifyThatVIPAccessLabelIsPresent() {
        HotelInfoSiteScreen.verifyVIPAccessLabelIsPresent()
    }

    @And("^I click on a VIP Access label on hotel infosite page$")
    @Throws(Throwable::class)
    fun iClickOnAVIPAccessLabelOnTheHotelInfositePage() {
        HotelInfoSiteScreen.clickOnVIPAccess()
        VIPAccess.waitForViewToLoad()
    }

    @Then("^I close the VIP Access page$")
    @Throws(Throwable::class)
    fun iCloseTheVIPAccessPage() {
        VIPAccess.clickHeaderCloseButton()
    }

    @And("^I verify the hotel label text is \"(.*?)\"$")
    @Throws(Throwable::class)
    fun iVerifyHotelLabelText(headerLabelText: String) {
        HotelInfoSiteScreen.verifyHeaderLabelText(headerLabelText)
    }

    @When("^I click select a room$")
    @Throws(Throwable::class)
    fun iClickSelectARoom() {
        HotelInfoSiteScreen.clickStickySelectRoom()
    }

    @And("^I change date if hotel is sold out$")
    @Throws(Throwable::class)
    fun iChangeDateIfSoldOut() {
        var startDateOffset = 7
        var currentAttempt = 0
        val maxAttempt = 3
        while (HotelInfoSiteScreen.changeDatesCalendarIsDisplayed() && currentAttempt < maxAttempt) {
            startDateOffset++
            val startDate = LocalDate.now().plusDays(startDateOffset)
            val endDate = LocalDate.now().plusDays(startDateOffset + 1)
            HotelInfoSiteScreen.chooseDatesWithDialog(startDate, endDate)
            HotelInfoSiteScreen.waitForDetailsLoaded()
            HotelInfoSiteScreen.clickStickySelectRoom()
            currentAttempt++
        }
    }

    @Then("^I verify hotel fees appear$")
    @Throws(Throwable::class)
    fun iSeeHotelFees() {
        HotelInfoSiteScreen.verifyHotelFeesAppear()
    }

    @Then("^I click on hotel fees info icon$")
    @Throws(Throwable::class)
    fun clickHotelResortFeesInfo() {
        HotelInfoSiteScreen.clickHotelFeesInfo()
    }

    @Then("^I verify additional hotel fees screen is displayed$")
    @Throws(Throwable::class)
    fun iVerifyHotelFeesScreenAppear() {
        AdditionalFeeInfo.verifyAdditionalFeeContainerAppear()
    }

    @Then("^I verify deposit and resort fees on additional hotel fees screen$")
    @Throws(Throwable::class)
    fun iVerifyDepositAndResortFeeOnFeesScreen() {
        AdditionalFeeInfo.verifyDepositAndResortFeesContent("Deposit: USD ", "Resort fee: USD ")
    }

    @When("^I book first room$")
    @Throws(Throwable::class)
    fun iBookFirstRoom() {
        HotelInfoSiteScreen.waitForPageToLoad()
        HotelInfoSiteScreen.Room().clickBookButton(true)
    }

    @And("^I verify PayNowPayLater Tab is Present$")
    @Throws(Throwable::class)
    fun verifyPayNowPayLaterTabIsPresent() {
        HotelInfoSiteScreen.payNowAndLaterOptions().perform(scrollTo(), waitForViewToDisplay())
        HotelInfoSiteScreen.payNowAndLaterOptionsIsPresent()
    }

    @And("^I click on Pay Now Button$")
    @Throws(Throwable::class)
    fun clickPayNow() {
        HotelInfoSiteScreen.clickPayNow()
    }

    @And("^I verify ETP Text is not Displayed$")
    @Throws(Throwable::class)
    fun verifyETPTextIsNotDisplayed() {
        HotelInfoSiteScreen.etpTextIsNotPresent()
    }

    @Then("^I verify the (deposit terms|resort fees) text is (displayed|not displayed)$")
    @Throws(Throwable::class)
    fun iVerifyIfTermsAndFeeAreDisplayed(feeType: String, isDisplayed: String) {
        if (feeType.equals("deposit terms")) {
            MandatoryFeesAndTaxes.verifyBodyText("Deposit: USD ", isDisplayed)
        } else {
            MandatoryFeesAndTaxes.verifyBodyText("Resort fee: USD ", isDisplayed)
        }
    }
}
