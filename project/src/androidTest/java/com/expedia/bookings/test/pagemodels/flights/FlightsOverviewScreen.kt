package com.expedia.bookings.test.pagemodels.flights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions
import org.hamcrest.core.AllOf
import java.util.concurrent.TimeUnit

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
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.checkout_button), 30, TimeUnit.SECONDS)
        return onView(withId(R.id.checkout_button)).perform(click());
    }
    @JvmStatic fun fareFamilyWidget(): ViewInteraction {
        return onView(withId(R.id.fare_family_widget))
    }
    @JvmStatic fun fareFamilyWidgetTitle(): ViewInteraction {
        return onView(withId(R.id.fare_family_title))
    }
    @JvmStatic fun fareFamilyWidgetSubtitle(): ViewInteraction {
        return onView(withId(R.id.selected_classes))
    }
    @JvmStatic fun fareFamilyWidgetFromLabel(): ViewInteraction {
        return onView(withId(R.id.fare_family_from_label))
    }
    @JvmStatic fun fareFamilyWidgetDeltaPrice(): ViewInteraction {
        return onView(withId(R.id.upgrade_delta_price))
    }
    @JvmStatic fun fareFamilyDetailsWidgetTitle(title: String): ViewInteraction {
        return onView(withText(title))
    }
    @JvmStatic fun fareFamilyDetailsWidgetDoneBtn(): ViewInteraction {
        return onView(AllOf.allOf(withId(R.id.search_btn), isDescendantOfA(withId(R.id.fare_family_details))));
    }
    @JvmStatic fun fareFamilyDetailsWidgetLocationLabel(): ViewInteraction {
        return onView(AllOf.allOf(withId(R.id.fare_family_location), isDescendantOfA(withId(R.id.fare_family_details))));
    }
    @JvmStatic fun fareFamilyDetailsWidgetAirlinesLabel(): ViewInteraction {
        return onView(AllOf.allOf(withId(R.id.fare_family_airlines), isDescendantOfA(withId(R.id.fare_family_details))));
    }
    @JvmStatic fun fareFamilyDetailsWidgetFarelist(): ViewInteraction {
        return onView(AllOf.allOf(withId(R.id.flight_fare_family_radio_group), isDescendantOfA(withId(R.id.fare_family_details))));
    }

    @JvmStatic fun fareFamilyDetailsBundleTotalPrice(): ViewInteraction {
        return onView(AllOf.allOf(withId(R.id.bundle_total_price), isDescendantOfA(withId(R.id.upsell_total_price_widget))));
    }

    @JvmStatic fun fareFamilyDetailsAmenitiesDialog(): ViewInteraction {
        return onView(withId(R.id.fare_family_name));
    }

    @JvmStatic fun flightOverviewBundleTotalPrice(): ViewInteraction {
        return onView(AllOf.allOf(withId(R.id.bundle_total_price), isDescendantOfA(withId(R.id.total_price_widget))));
    }
}
