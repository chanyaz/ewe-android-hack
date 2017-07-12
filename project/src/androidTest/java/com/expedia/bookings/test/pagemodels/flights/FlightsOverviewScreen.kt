package com.expedia.bookings.test.pagemodels.flights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.ViewActions

object FlightsOverviewScreen {

    fun assertCardFeeWarningShown() {
        cardFeeWarningTextView()
                .check(matches(isDisplayed()))
                .check(matches(withText("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.")))
    }

    fun assertPriceChangeShown(priceChangeText: String) {
        priceChangeTextView().perform(ViewActions.waitForViewToDisplay())
        onView(withText(priceChangeText)).check(matches(isDisplayed()))
    }

    fun cardFeeWarningTextView() = onView(withId(R.id.card_fee_warning_text))

    fun priceChangeTextView() = onView(withText(R.string.price_change_text))

    @JvmStatic fun clickOnCheckoutButton(): ViewInteraction {
        return onView(withId(R.id.checkout_button)).perform(click());
    }
}
