package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import org.junit.Assert

public class HotelCheckoutTest: HotelTestCase() {

    fun testCardNumberClearedAfterCreateTrip() {
        HotelScreen.doGenericSearch()
        HotelScreen.selectHotel("happypath")
        Common.delay(1)
        HotelScreen.selectRoom()
        enterTravelerAndPaymentDetails()

        Espresso.pressBack() // nav back to checkout
        Espresso.pressBack() // nav back to details
        HotelScreen.selectRoom()

        // assert that credit card number is empty
        Common.delay(1)
        CheckoutViewModel.paymentInfo().perform(scrollTo())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Payment Details")
        CheckoutViewModel.clickPaymentInfo()
        Common.delay(1)
        CheckoutViewModel.clickAddCreditCard()
        Common.delay(1)
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "")
        Espresso.pressBack()
    }

    fun testLoggedInCustomerCanEnterNewTraveler() {
        HotelScreen.doGenericSearch()
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

    fun testTealeafIDClearedAfterSignIn() {
        HotelScreen.doGenericSearch()
        HotelScreen.selectHotel("tealeaf_id")
        Common.delay(1)
        HotelScreen.selectRoom()
        Assert.assertEquals(Db.getTripBucket().hotelV2.mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id")
        HotelScreen.clickSignIn()
        HotelScreen.signIn()
        Assert.assertEquals(Db.getTripBucket().hotelV2.mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id_signed_in")
    }

    private fun enterTravelerAndPaymentDetails() {
        CheckoutViewModel.waitForCheckout()
        CheckoutViewModel.enterTravelerInfo()
        CheckoutViewModel.enterPaymentInfoHotels()
    }
}
