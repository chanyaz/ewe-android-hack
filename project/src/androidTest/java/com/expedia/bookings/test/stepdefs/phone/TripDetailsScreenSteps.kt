package com.expedia.bookings.test.stepdefs.phone

import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen
import cucumber.api.java.en.Then

class TripDetailsScreenSteps {
    @Then("^I verify the hotel name in the tool bar is \"(.*?)\"")
    @Throws(Throwable::class)
    fun verifyHotelNameInTheToolBar(hotelName: String) {
        TripDetailsScreen.Toolbar.verifyHotelName(hotelName)
    }

    @Then("^I verify the dates in the tool bar are from \"(.*?)\" to \"(.*?)\"")
    @Throws(Throwable::class)
    fun verifyDatesInTheToolBar(from: String, to: String) {
        TripDetailsScreen.Toolbar.verifyDates(from, to)
    }

    @Then("^I verify the (hotel name|phone number) in the hotel information section is \"(.*?)\"")
    @Throws(Throwable::class)
    fun verifyTextInHotelInformation(verificationType: String, verifyString: String) {
        when (verificationType) {
            "hotel name" -> TripDetailsScreen.HotelInformation.verifyHotelName(verifyString)
            "phone number" -> TripDetailsScreen.HotelInformation.verifyPhoneNumberText(verifyString)
        }
    }

    @Then("^I verify the phone number button in the hotel information section is clickable")
    @Throws(Throwable::class)
    fun verifyPhoneNumberButtonInHotelInformationIsClickable() {
        TripDetailsScreen.HotelInformation.verifyPhoneNumberButtonIsClickable()
    }

    @Then("^I verify the (Check-In Date|Check-Out Date|Check-In Time|Check-Out Time) is: \"(.*?)\"")
    @Throws(Throwable::class)
    fun verifyCheckInCheckOutDateTime(fieldToVerify: String, verifyString: String) {
        when (fieldToVerify) {
            "Check-In Date" -> TripDetailsScreen.CheckInCheckOut.verifyCheckInDate(verifyString)
            "Check-Out Date" -> TripDetailsScreen.CheckInCheckOut.verifyCheckOutDate(verifyString)
            "Check-In Time" -> TripDetailsScreen.CheckInCheckOut.verifyCheckInTime(verifyString)
            "Check-Out Time" -> TripDetailsScreen.CheckInCheckOut.verifyCheckOutTime(verifyString)
        }
    }

    @Then("^I tap on Share icon")
    @Throws(Throwable::class)
    fun iTapOnShareIcon() {
        TripDetailsScreen.Toolbar.clickShareButton()
        TripDetailsScreen.ShareOptions.waitForShareSuggestionsListToLoad()
    }

    @Then("^I tap on (Facebook|Gmail|KakaoTalk|LINE) and verify the app has opened")
    @Throws(Throwable::class)
    fun iTapOnSpecificAppIconAndVerifyAppOpened(appName: String) {
        TripDetailsScreen.ShareOptions.clickOnIconWithText(appName)
        TripDetailsScreen.ShareOptions.waitForAppToLoad(appName)
    }

    @Then("^I kill process and wipe data of (Facebook|Gmail|KakaoTalk|LINE) app")
    @Throws(Throwable::class)
    fun iClearDataAndKillProcessOfApp(appName: String) {
        val packageName = TripDetailsScreen.ShareOptions.getPackageNameForAppName(appName)
        Common.killProcess(packageName)
        Common.clearProcessCacheData(packageName)
    }
}
