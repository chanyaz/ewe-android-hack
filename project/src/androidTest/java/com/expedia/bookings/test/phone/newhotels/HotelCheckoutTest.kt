package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.scrollTo
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.phone.newhotels.HotelScreen.doSearch
import com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel
import com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel

public class HotelCheckoutTest: HotelTestCase() {

    fun testCardNumberClearedAfterCreateTrip() {
        doSearch()
        selectHotel("happypath")
        selectRoom()
        checkout()

        CheckoutViewModel.pressClose()
        Espresso.pressBack() // nav back to details
        selectRoom()

        // assert that credit card number is empty
        Common.delay(1)
        CheckoutViewModel.paymentInfo().perform(scrollTo())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Payment Details")
        CheckoutViewModel.clickPaymentInfo()
        Common.delay(1)
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "")
        Espresso.pressBack()
    }

    private fun checkout() {
        CheckoutViewModel.waitForCheckout()
        CheckoutViewModel.clickDone()
        CheckoutViewModel.enterTravelerInfo()
        CheckoutViewModel.enterPaymentInfoHotels()
    }
}
