package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.scrollTo
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch
import com.expedia.bookings.test.phone.newhotels.HotelScreen.selectHotel
import com.expedia.bookings.test.phone.newhotels.HotelScreen.selectRoom
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.newhotels.HotelScreen.signIn
import com.expedia.bookings.test.phone.newhotels.HotelScreen.clickSignIn
import org.junit.Assert

public class HotelCheckoutTest: HotelTestCase() {

    fun testCardNumberClearedAfterCreateTrip() {
        doGenericSearch()
        selectHotel("happypath")
        selectRoom()
        checkout()

        Espresso.pressBack() // nav back to checkout
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
        CheckoutViewModel.enterTravelerInfo()
        CheckoutViewModel.enterPaymentInfoHotels()
    }

    fun testTealeafIDClearedAfterSignIn() {
        doGenericSearch()
        selectHotel("tealeaf_id")
        selectRoom()
        Assert.assertEquals(Db.getTripBucket().hotelV2.mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id")
        clickSignIn()
        signIn()
        Assert.assertEquals(Db.getTripBucket().hotelV2.mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id_signed_in")
    }
    
}
