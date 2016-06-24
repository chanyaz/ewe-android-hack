package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R

object FlightsOverviewScreen {

    fun assertCardFeeWarningShown() {
        cardFeeWarningTextView()
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.withText("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.")))
    }

    fun cardFeeWarningTextView() = Espresso.onView(ViewMatchers.withId(R.id.card_fee_warning_text))

}
