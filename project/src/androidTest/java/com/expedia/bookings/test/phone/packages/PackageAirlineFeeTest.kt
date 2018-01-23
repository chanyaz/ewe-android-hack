package com.expedia.bookings.test.phone.packages

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.*
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.PackageTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import org.junit.Test
import java.util.concurrent.TimeUnit

class PackageAirlineFeeTest : PackageTestCase() {

    @Test
    fun testAirlineFees() {
        PackageScreen.searchPackage()
        HotelScreen.selectHotel("Package Happy Path")
        HotelInfoSiteScreen.bookFirstRoom()

        PackageScreen.selectFlight(0)
        PackageScreen.selectThisFlight().perform(click())

        PackageScreen.selectFlight(2)
        PackageScreen.selectThisFlight().perform(ViewActions.waitFor(isDisplayed(), 10, TimeUnit.SECONDS))
        PackageScreen.selectThisFlight().perform(click())
        PackageScreen.checkout().perform(click())

        CheckoutViewModel.clickPaymentInfo()
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")
        CardInfoScreen.assertPaymentFormCardFeeWarningShown("Payment method fee: $2.50")

        PackageScreen.completePaymentForm()
        CheckoutViewModel.clickDone()

        assertCheckoutOverviewCardFeeWarningShown()
    }

    private fun assertCheckoutOverviewCardFeeWarningShown() {
        Common.delay(2) // We wait for a short delay (in implementation) jic customer changes their card
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("A payment method fee of $2.50 is included in the trip total.")))
    }
}
