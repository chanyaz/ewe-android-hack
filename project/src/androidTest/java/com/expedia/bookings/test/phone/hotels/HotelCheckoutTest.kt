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
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import org.junit.Test

class HotelCheckoutTest : HotelTestCase() {

    @Test
    fun testCardNumberClearedAfterCreateTrip() {
        SearchScreen.doGenericHotelSearch()
        HotelResultsScreen.selectHotel("happypath")
        Common.delay(1)
        HotelInfoSiteScreen.bookFirstRoom()
        enterTravelerAndPaymentDetails()

        Espresso.pressBack() // nav back to details
        HotelInfoSiteScreen.bookFirstRoom()

        // assert that credit card number is empty
        Common.delay(1)
        CheckoutScreen.paymentInfo().perform(scrollTo())
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Enter payment details")
        CheckoutScreen.clickPaymentInfo()
        Common.delay(1)
        EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "")
        Espresso.pressBack()
    }

    @Test
    fun testLoggedInCustomerCanEnterNewTraveler() {
        SearchScreen.doGenericHotelSearch()
        HotelResultsScreen.selectHotel("happypath")
        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutScreen.clickDone()

        CheckoutScreen.loginAsQAUser()
        Common.delay(1)

        CheckoutScreen.clickTravelerInfo()
        CheckoutScreen.clickStoredTravelerButton()
        CheckoutScreen.selectStoredTraveler("Expedia Automation First")

        CheckoutScreen.clickStoredTravelerButton()
        CheckoutScreen.selectStoredTraveler("Add New Traveler")

        CheckoutScreen.firstName().check(matches(withText("")))
        CheckoutScreen.lastName().check(matches(withText("")))
        CheckoutScreen.phone().check(matches(withText("")))
    }

    private fun enterTravelerAndPaymentDetails() {
        CheckoutScreen.waitForCheckout()
        CheckoutScreen.enterTravelerInfo()
        CheckoutScreen.enterPaymentInfoHotels()
    }

    @Test
    fun testResortFeeDisclaimerTextVisibility() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelResultsScreen.selectHotel("Non Merchant Hotel")
        Common.delay(1)

        HotelInfoSiteScreen.clickStickySelectRoom()
        Common.delay(1)

        HotelInfoSiteScreen.bookFirstRoom()

        CheckoutScreen.resortFeeDisclaimerText().perform(scrollTo())
        Common.delay(1)

        //On Checkout page resortFeeDisclaimerText is Visible
        CheckoutScreen.resortFeeDisclaimerText().check(matches(withText("The $3 resort fee will be collected at the hotel. The total price for your stay will be $21.08.")))
        CheckoutScreen.clickPaymentInfo()

        //On paymentInfo page resortFeeDisclaimerText's Visibility is Gone
        CheckoutScreen.resortFeeDisclaimerText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun testFreeCancellationNotAvailableAndHiddenFromSummary() {
        SearchScreen.doGenericHotelSearch()
        HotelResultsScreen.selectHotel("happypath")

        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutScreen.freeCancellationText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun testFreeCancellationAvailableAndShownInSummary() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelResultsScreen.selectHotel("Non Merchant Hotel")

        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutScreen.freeCancellationText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testFreeCancellationTooltipAvailableAndShownInSummary() {
        SearchScreen.doGenericHotelSearch()
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFreeCancellationTooltip,
                AbacusVariant.BUCKETED.value)

        // Check to make sure non merchant shows up in result list
        HotelResultsScreen.selectHotel("Non Merchant Hotel")

        HotelInfoSiteScreen.bookFirstRoom()

        CheckoutScreen.freeCancellationText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        CheckoutScreen.freeCancellationTooltipText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        CheckoutScreen.freeCancellationTooltipText().perform(click())

        CheckoutScreen.freeCancellationWidget().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Common.pressBack()

        CheckoutScreen.freeCancellationTooltipText().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testTravelerCardContentDescription() {
        SearchScreen.doGenericHotelSearch()
        HotelResultsScreen.selectHotel("happypath")
        HotelInfoSiteScreen.bookFirstRoom()
        CheckoutScreen.waitForCheckout()
        CheckoutScreen.clickDone()

        CheckoutScreen.travelerInfo().check(matches(ViewMatchers.withContentDescription(" Error: Enter missing traveler details. Button.")))

        CheckoutScreen.enterTravelerInfo()
        CheckoutScreen.travelerInfo().check(matches(ViewMatchers.withContentDescription("FiveStar Bear, 4158675309, traveler details complete. Button.")))
    }
}
