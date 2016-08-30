package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.joda.time.LocalDate
import org.junit.Test

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
        assertTravelerInfoFilled()

        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        PackageScreen.clickPaymentInfo()
        assertPaymentInfoCleared()
    }

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

    private fun assertTravelerInfoFilled() {
        assertEditTextWithIdIsFilledWithString(R.id.first_name_input, "Eidur")
        assertEditTextWithIdIsFilledWithString(R.id.last_name_input, "Gudjohnsen")
        assertEditTextWithIdIsFilledWithString(R.id.edit_email_address, "eidur@eidur.com")
        assertEditTextWithIdIsFilledWithString(R.id.edit_phone_number, "4155554321")
        assertEditTextWithIdIsFilledWithString(R.id.edit_birth_date_text_btn, "Jun 9, 1989")
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

    private fun assertEditTextWithIdIsFilledWithString(id: Int, string: String) {
        Espresso.onView(ViewMatchers.withId(id))
                .check(ViewAssertions.matches(ViewMatchers.withText(string)))
    }
}