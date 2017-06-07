package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers.hasTextInputLayoutErrorText
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.joda.time.LocalDate
import org.junit.Test
import java.util.concurrent.TimeUnit

class FlightCheckoutTravelerAndPaymentInfoClearTest : NewFlightTestCase() {

    @Test
    fun testPaymentInfoClearsAndTravelerInfoPersistsAfterSelectingInboundFlight() {
        flightSearchAndGoToCheckout()

        PackageScreen.travelerInfo().perform(ViewActions.click())
        fillTravelerDetails()

        PackageScreen.clickPaymentInfo()
        fillPaymentInfo()

        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.travelerInfo().perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.first_name_input))
                .check(ViewAssertions.matches(ViewMatchers.withText("Eidur")))

        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        PackageScreen.clickPaymentInfo()
        assertPaymentInfoCleared()
    }

    @Test
    fun testTravelerErrorsClearedAfterSignIn() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        flightSearchAndGoToCheckout()

        PackageScreen.travelerInfo().perform(ViewActions.click())
        PackageScreen.enterFirstName("Eidur")
        PackageScreen.clickTravelerDone()
        onView(withId(R.id.last_name_layout_input)).check(matches(hasTextInputLayoutErrorText("Enter last name using letters only (minimum 2 characters)")))
        Common.pressBack()

        CheckoutViewModel.signInOnCheckout()
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(ViewMatchers.withId(R.id.login_widget), 10, TimeUnit.SECONDS)

        PackageScreen.travelerInfo().perform(ViewActions.click())
        onView(withId(R.id.last_name_layout_input)).check(matches(hasTextInputLayoutErrorText("")))
    }

// Disabled on April 28, 2017 for repeated flakiness - ScottW
//    @Test
//    fun testPaymentInfoCCVClear() {
//        flightSearchAndGoToCheckout()
//        CheckoutViewModel.signInOnCheckout()
//        EspressoUtils.waitForViewNotYetInLayoutToDisplay(ViewMatchers.withId(R.id.login_widget), 10, TimeUnit.SECONDS)
//
//        PackageScreen.clickPaymentInfo()
//        PaymentOptionsScreen.openCardPaymentSection()
//        fillPaymentInfo()
//
//        onView(withId(android.R.id.button1)).perform(click())
//        CheckoutViewModel.clickPaymentInfo()
//        CheckoutViewModel.selectStoredCard("Saved Expired Credit Card")
//
//        PaymentOptionsScreen.assertCardSelectionMatches("Saved Expired Credit Card", 1)
//        Common.pressBack()
//        CheckoutViewModel.performSlideToPurchase()
//        EspressoUtils.assertViewIsDisplayed(R.id.cvv)
//    }

    @Test
    fun testTravelerAndPaymentInfoClearsOnNewFlightSearch() {
        flightSearchAndGoToCheckout()

        PackageScreen.travelerInfo().perform(ViewActions.click())
        fillTravelerDetails()

        PackageScreen.clickPaymentInfo()
        fillPaymentInfo()

        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        Common.pressBack()
        Common.pressBack()
        Common.pressBack()
        Common.pressBack()

        SearchScreen.searchButton().perform(ViewActions.click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.travelerInfo().perform(ViewActions.click())
        assertTravelerInfoCleared()

        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        PackageScreen.clickPaymentInfo()
        assertPaymentInfoCleared()
    }

    private fun flightSearchAndGoToCheckout() {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.selectFlightOriginAndDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
        PackageScreen.checkout().perform(ViewActions.click())
    }

    private fun fillTravelerDetails() {
        PackageScreen.enterFirstName("Eidur")
        PackageScreen.enterLastName("Gudjohnsen")
        PackageScreen.enterEmail("eidur@eidur.com")
        Espresso.closeSoftKeyboard()
        PackageScreen.enterPhoneNumber("4155554321")
        Espresso.closeSoftKeyboard()
        PackageScreen.selectBirthDate(1989, 6, 9)
        PackageScreen.selectGender("Male")
        PackageScreen.clickTravelerAdvanced()
        PackageScreen.enterRedressNumber("1234567")
        PackageScreen.clickTravelerDone()
    }

    private fun assertTravelerInfoCleared() {
        assertEditTextWithIdIsEmpty(R.id.first_name_input)
        assertEditTextWithIdIsEmpty(R.id.last_name_input)
        assertEditTextWithIdIsEmpty(R.id.edit_email_address)
        assertEditTextWithIdIsEmpty(R.id.edit_phone_number)
        assertEditTextWithIdIsEmpty(R.id.edit_birth_date_text_btn)
    }

    private fun fillPaymentInfo() {
        PackageScreen.enterCreditCard()
        PackageScreen.completePaymentForm()
        PackageScreen.clickPaymentDone()
    }

    private fun assertPaymentInfoCleared() {
        assertEditTextWithIdIsEmpty(R.id.edit_creditcard_number)
        assertEditTextWithIdIsEmpty(R.id.edit_creditcard_exp_text_btn)
        assertEditTextWithIdIsEmpty(R.id.edit_creditcard_cvv)
        assertEditTextWithIdIsEmpty(R.id.edit_name_on_card)
        assertEditTextWithIdIsEmpty(R.id.edit_address_line_one)
        assertEditTextWithIdIsEmpty(R.id.edit_address_line_two)
        assertEditTextWithIdIsEmpty(R.id.edit_address_city)
        assertEditTextWithIdIsEmpty(R.id.edit_address_state)
        assertEditTextWithIdIsEmpty(R.id.edit_address_postal_code)
    }

    private fun assertEditTextWithIdIsEmpty(id: Int) {
        Espresso.onView(ViewMatchers.withId(id))
                .check(ViewAssertions.matches(ViewMatchers.withText("")))
    }
}