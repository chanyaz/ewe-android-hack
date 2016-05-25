package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen

class HotelCheckoutTest: HotelTestCase() {

    fun testCardNumberClearedAfterCreateTrip() {
        SearchScreen.doGenericHotelSearch()
        HotelScreen.selectHotel("happypath")
        Common.delay(1)
        HotelScreen.selectRoom()
        enterTravelerAndPaymentDetails()

        Espresso.pressBack() // nav back to details
        HotelScreen.selectRoom()

        // assert that credit card number is empty
        Common.delay(1)
        CheckoutViewModel.paymentInfo().perform(scrollTo())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Payment Method")
        CheckoutViewModel.clickPaymentInfo()
        Common.delay(1)
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "")
        Espresso.pressBack()
    }

    fun testLoggedInCustomerCanEnterNewTraveler() {
        SearchScreen.doGenericHotelSearch()
        HotelScreen.selectHotel()
        HotelScreen.selectRoom()
        CheckoutViewModel.clickDone()

        HotelScreen.doLogin()
        Common.delay(1)

        CheckoutViewModel.clickDriverInfo()
        CheckoutViewModel.clickStoredTravelerButton()
        CheckoutViewModel.selectStoredTraveler("Expedia Automation First")

        CheckoutViewModel.clickStoredTravelerButton()
        CheckoutViewModel.selectStoredTraveler("Add New Traveler")

        CheckoutViewModel.firstName().check(matches(withText("")))
        CheckoutViewModel.lastName().check(matches(withText("")))
        CheckoutViewModel.phone().check(matches(withText("")))
    }

    private fun enterTravelerAndPaymentDetails() {
        CheckoutViewModel.waitForCheckout()
        CheckoutViewModel.enterTravelerInfo()
        CheckoutViewModel.enterPaymentInfoHotels()
    }

    fun testResortFeeDisclaimerTextVisibility() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelScreen.selectHotel("Non Merchant Hotel")
        Common.delay(1)

        HotelScreen.selectRoomButton().perform(click())
        Common.delay(1)

        HotelScreen.selectRoom()

        CheckoutViewModel.resortFeeDisclaimerText().perform(scrollTo())
        Common.delay(1)

        //On Checkout page resortFeeDisclaimerText is Visible
        CheckoutViewModel.resortFeeDisclaimerText().check(matches(withText("The $3 resort fee will be collected at the hotel. The total price for your stay will be $21.08.")))
        CheckoutViewModel.clickPaymentInfo()

        //On paymentInfo page resortFeeDisclaimerText's Visibility is Gone
        CheckoutViewModel.resortFeeDisclaimerText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }
}
