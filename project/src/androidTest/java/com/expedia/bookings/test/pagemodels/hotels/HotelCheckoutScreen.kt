package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.common.CVVEntryScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import org.hamcrest.Matchers.allOf

class HotelCheckoutScreen {

    companion object {

        @JvmStatic
        fun checkout(walletSupported: Boolean) {
            CheckoutScreen.waitForCheckout()
            CheckoutScreen.clickDone()
            CheckoutScreen.enterTravelerInfo()
            if (walletSupported) CheckoutScreen.enterPaymentInfoHotels()
            else CheckoutScreen.enterPaymentInfo()
        }

        @JvmStatic
        fun enterCVVAndBook() {
            CVVEntryScreen.enterCVV("123")
            CVVEntryScreen.clickBookButton()
        }

        @JvmStatic
        fun clickSignIn() {
            onView(withId(R.id.login_text_view)).perform(scrollTo(), click())
        }

        @JvmStatic
        fun clickSignOut() {
            onView(withId(R.id.account_logout_logout_button)).perform(waitForViewToDisplay())
            onView(withId(R.id.account_logout_logout_button)).perform(click())
            onView(
                    allOf<View>(withId(android.R.id.message), withText("Are you sure you want to sign out of your Expedia account?")))
                    .check(matches(isDisplayed()))
            onView(withId(android.R.id.button1)).perform(click())
        }

        @JvmStatic
        fun waitForConfirmationDisplayed() {
            onView(withId(R.id.hotel_confirmation_presenter)).perform(waitForViewToDisplay())
        }

        @JvmStatic
        fun waitForErrorDisplayed() {
            onView(withId(R.id.widget_hotel_errors)).perform(waitForViewToDisplay())
        }
    }
}
