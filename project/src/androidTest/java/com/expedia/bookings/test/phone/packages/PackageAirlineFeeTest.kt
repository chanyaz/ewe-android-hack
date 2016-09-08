package com.expedia.bookings.test.phone.packages

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.*
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.PackageTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.phone.hotels.HotelScreen
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import org.junit.Test
import java.util.concurrent.TimeUnit

class PackageAirlineFeeTest: PackageTestCase() {

    @Test
    fun testAirlineFees() {
        PackageScreen.searchPackage()
        HotelScreen.selectHotel("Package Happy Path")
        PackageScreen.selectRoom()

        PackageScreen.selectFlight(0)
        PackageScreen.selectThisFlight().perform(click())

        PackageScreen.selectFlight(2)
        PackageScreen.selectThisFlight().perform(ViewActions.waitFor(isDisplayed(), 10, TimeUnit.SECONDS))
        PackageScreen.selectThisFlight().perform(click())
        PackageScreen.checkout().perform(click())

        CheckoutViewModel.clickPaymentInfo()
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")
        assertPaymentFormCardFeeWarningShown()

        PackageScreen.completePaymentForm()
        CheckoutViewModel.clickDone()

        assertCheckoutOverviewCardFeeWarningShown()
    }

    private fun assertCheckoutOverviewCardFeeWarningShown() {
        Common.delay(2) // We wait for a short delay (in implementation) jic customer changes their card
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("The airline charges a processing fee of $2.50 for using this card (cost included in the trip total).")))
    }

    private fun assertPaymentFormCardFeeWarningShown() {
        Common.delay(1)
        onView(withId(R.id.card_processing_fee)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("Airline processing fee for this card: $2.50")))
    }
}
