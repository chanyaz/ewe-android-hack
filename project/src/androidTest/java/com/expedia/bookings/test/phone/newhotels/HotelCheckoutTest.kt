package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import org.junit.Assert

public class HotelCheckoutTest: HotelTestCase() {

    fun testCardNumberClearedAfterCreateTrip() {
        HotelScreen.doGenericSearch()
        HotelScreen.selectHotel("happypath")
        Common.delay(1)
        HotelScreen.selectRoom()
        enterTravelerAndPaymentDetails()

        Espresso.pressBack() // nav back to details
        HotelScreen.selectRoom()

        // assert that credit card number is empty
        Common.delay(1)
        CheckoutViewModel.paymentInfo().perform(scrollTo())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Payment Details")
        CheckoutViewModel.clickPaymentInfo()
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

    fun testCouponIsClearedEachCreateTrip() {
        HotelScreen.doGenericSearch()
        HotelScreen.selectHotel("happypath")
        Common.delay(1)
        HotelScreen.selectRoom()
        CheckoutViewModel.waitForCheckout()
        CheckoutViewModel.clickDone()
        CheckoutViewModel.applyCoupon("hotel_coupon_success")
        // Coupon was applied
        CheckoutViewModel.scrollView().perform(ViewActions.swipeDown())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$114.76")

        // Nav back to rooms and rates
        Espresso.pressBack()
        Espresso.pressBack()
        HotelScreen.selectHotel("happypath")
        Common.delay(1)
        HotelScreen.selectRoomButton().perform(click())
        Common.delay(1)
        HotelScreen.clickRoom("happypath_2_night_stay_0")
        HotelScreen.clickAddRoom()
        Common.delay(1)

        // Pick a different room, should refresh createTrip with a new price
        CheckoutViewModel.scrollView().perform(ViewActions.swipeDown())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$2,394.88")
    }
}
