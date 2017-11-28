package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import org.junit.Test

class HotelCheckoutTest: HotelTestCase() {

    @Test
    fun testCardNumberClearedAfterCreateTrip() {
        SearchScreen.doGenericHotelSearch()
        HotelScreen.selectHotel("happypath")
        Common.delay(1)
        HotelInfoSiteScreen.bookFirstRoom()
        enterTravelerAndPaymentDetails()

        Espresso.pressBack() // nav back to details
        HotelInfoSiteScreen.bookFirstRoom()

        // assert that credit card number is empty
        Common.delay(1)
        CheckoutViewModel.paymentInfo().perform(scrollTo())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Enter payment details")
        CheckoutViewModel.clickPaymentInfo()
        Common.delay(1)
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "")
        Espresso.pressBack()
    }

    @Test
    fun testLoggedInCustomerCanEnterNewTraveler() {
        SearchScreen.doGenericHotelSearch()
        HotelScreen.selectHotel()
        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutViewModel.clickDone()

        HotelScreen.doLogin()
        Common.delay(1)

        CheckoutViewModel.clickTravelerInfo()
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

    @Test
    fun testResortFeeDisclaimerTextVisibility() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelScreen.selectHotel("Non Merchant Hotel")
        Common.delay(1)

        HotelInfoSiteScreen.clickStickySelectRoom()
        Common.delay(1)

        HotelInfoSiteScreen.bookFirstRoom()

        CheckoutViewModel.resortFeeDisclaimerText().perform(scrollTo())
        Common.delay(1)

        //On Checkout page resortFeeDisclaimerText is Visible
        CheckoutViewModel.resortFeeDisclaimerText().check(matches(withText("The $3 resort fee will be collected at the hotel. The total price for your stay will be $21.08.")))
        CheckoutViewModel.clickPaymentInfo()

        //On paymentInfo page resortFeeDisclaimerText's Visibility is Gone
        CheckoutViewModel.resortFeeDisclaimerText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun testFreeCancellationNotAvailableAndHiddenFromSummary() {
        SearchScreen.doGenericHotelSearch()
        HotelScreen.selectHotel("happypath")

        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutViewModel.freeCancellationText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun testFreeCancellationAvailableAndShownInSummary() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelScreen.selectHotel("Non Merchant Hotel")

        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutViewModel.freeCancellationText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testFreeCancellationTooltipAvailableAndShownInSummary() {
        SearchScreen.doGenericHotelSearch()
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFreeCancellationTooltip,
                AbacusUtils.DefaultVariant.BUCKETED.ordinal)

        // Check to make sure non merchant shows up in result list
        HotelScreen.selectHotel("Non Merchant Hotel")

        HotelInfoSiteScreen.bookFirstRoom()

        CheckoutViewModel.freeCancellationText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        CheckoutViewModel.freeCancellationTooltipText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        CheckoutViewModel.freeCancellationTooltipText().perform(click())

        CheckoutViewModel.freeCancellationWidget().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Common.pressBack()
        
        CheckoutViewModel.freeCancellationTooltipText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testTravelerCardContentDescription() {
        SearchScreen.doGenericHotelSearch()
        HotelScreen.selectHotel("happypath")
        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutViewModel.waitForCheckout()
        CheckoutViewModel.clickDone()

        CheckoutViewModel.travelerInfo().check(matches(ViewMatchers.withContentDescription(" Error: Enter missing traveler details. Button.")))

        CheckoutViewModel.enterTravelerInfo()
        CheckoutViewModel.travelerInfo().check(matches(ViewMatchers.withContentDescription("FiveStar Bear, 4158675309, traveler details complete. Button.")))
    }
}
