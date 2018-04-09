package com.expedia.bookings.test.stepdefs.phone

import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen.HotelMap.clickOnMap
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen.HotelMap.verifyMapDirectionButtonIsClickable
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen.HotelMap.verifyMapMarkerPresent
import com.expedia.bookings.test.pagemodels.trips.TripDetailsScreen.HotelMap.waitForMapToLoad
import com.expedia.bookings.test.pagemodels.trips.TripsScreen
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps.switchToTab
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

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
        TripDetailsScreen.AndroidNativeShareOptions.waitForShareSuggestionsListToLoad()
    }

    @Then("^I tap on (Facebook|Gmail|KakaoTalk|LINE) and verify the app has opened")
    @Throws(Throwable::class)
    fun iTapOnSpecificAppIconAndVerifyAppOpened(appName: String) {
        TripDetailsScreen.AndroidNativeShareOptions.clickOnIconWithText(appName)
        TripDetailsScreen.AndroidNativeShareOptions.waitForAppToLoad(appName)
    }

    @When("^I tap on trip with the name \"(.*?)\"")
    @Throws(Throwable::class)
    fun iTapTrip(itemName: String) {
        switchToTab("Trips")
        TripsScreen.waitForTripsViewToLoad()
        TripsScreen.clickOnTripItemWithName(itemName)
        TripDetailsScreen.waitUntilLoaded()
    }

    @Then("^I force-stop process of (Facebook|Gmail|KakaoTalk|LINE) app")
    @Throws(Throwable::class)
    fun iForceStopProcessOfApp(appName: String) {
        val packageName = TripDetailsScreen.AndroidNativeShareOptions.getPackageNameForAppName(appName)
        Common.forceStopProcess(packageName)
    }

    @When("^I tap on the map")
    @Throws(Throwable::class)
    fun iTapOnMap() {
        clickOnMap()
        waitForMapToLoad()
    }

    @When("^I verify marker exists with name \"(.*?)\"")
    @Throws(Throwable::class)
    fun iVerifyMarkerExists(hotelName: String) {
        verifyMapMarkerPresent(hotelName)
    }

    @When("^I verify the map direction button is clickable")
    @Throws(Throwable::class)
    fun iVerifyMapDirectionButtonIsClickable() {
        verifyMapDirectionButtonIsClickable()
    }
}
