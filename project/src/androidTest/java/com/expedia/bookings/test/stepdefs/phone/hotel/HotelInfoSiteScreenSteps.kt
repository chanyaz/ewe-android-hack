package com.expedia.bookings.test.stepdefs.phone.hotel

import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen.VIPAccess

import cucumber.api.java.en.And
import cucumber.api.java.en.Then

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

    @And("^I click on Select a Room Button$")
    @Throws(Throwable::class)
    fun performClickOnViewSelectARoomButton() {
        HotelInfoSiteScreen.clickStickySelectRoom()
    }

    @Then("^I book first Room$")
    @Throws(Throwable::class)
    fun selectFirstRoom() {
        HotelInfoSiteScreen.bookFirstRoom()
    }
}
