package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils
import com.expedia.bookings.test.espresso.ViewActions
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is.`is`

object SearchScreen {
    private val suggestionList = withId(R.id.suggestion_list)

    @JvmStatic fun suggestionList(): ViewInteraction {
        return onView(suggestionList)
    }
    @JvmStatic fun waitForSearchEditText(): ViewInteraction {
        return onView(withId(R.id.search_src_text)).perform(ViewActions.waitForViewToDisplay())
    }

    @JvmStatic fun origin(): ViewInteraction {
        return onView(withId(R.id.origin_card))
    }

    @JvmStatic fun destination(): ViewInteraction {
        return onView(withId(R.id.destination_card))
    }

    @JvmStatic fun calendar(): ViewInteraction {
        return onView(withId(R.id.calendar))
                .inRoot(withDecorView(not<View>(`is`<View>(SpoonScreenshotUtils.getCurrentActivity().window.decorView))))
    }

    @JvmStatic fun flightClass(): ViewInteraction {
        return onView(withId(R.id.flight_cabin_class_widget))
    }

    @JvmStatic fun searchAlertDialogDone(): ViewInteraction {
        return onView(withId(android.R.id.button1))
    }

    @JvmStatic fun searchButton(): ViewInteraction {
        return onView(allOf(withId(R.id.search_btn), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @JvmStatic fun waitForSearchButton(): ViewInteraction {
        return onView(withId(R.id.search_btn)).perform(ViewActions.waitForViewToDisplay())
    }

    @JvmStatic fun selectDateButton(): ViewInteraction {
        return onView(withId(R.id.calendar_card))
    }

    @JvmStatic fun selectDestinationTextView(): ViewInteraction {
        return onView(withId(R.id.destination_card))
    }

    @JvmStatic fun selectGuestsButton(): ViewInteraction {
        return onView(withId(R.id.traveler_card))
    }

    @JvmStatic fun calendarSubtitle(): ViewInteraction {
        return onView(withId(R.id.instructions))
    }

    @JvmStatic fun nextMonthButton(): ViewInteraction {
        return onView(withId(R.id.next_month))
    }

    @JvmStatic fun previousMonthButton(): ViewInteraction {
        return onView(withId(R.id.previous_month))
    }

    @JvmStatic fun incrementAdultTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.adult_count_selector)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementYouthTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.youth_count_selector)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementChildTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.child_count_selector)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementInfantTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.infant_count_selector)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun decrementAdultTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.adult_count_selector)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementYouthTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.youth_count_selector)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementChildTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.child_count_selector)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementInfantTravelerButton(): ViewInteraction {
        return onView(allOf(isDescendantOfA(withId(R.id.infant_count_selector)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun childAgeDropDown(childNumber: Int): ViewInteraction? {
        return when (childNumber) {
            1 -> onView(withId(R.id.child_spinner_1))
            2 -> onView(withId(R.id.child_spinner_2))
            3 -> onView(withId(R.id.child_spinner_3))
            4 -> onView(withId(R.id.child_spinner_4))
            else -> null
        }
    }

    @Throws(Throwable::class)
    @JvmStatic fun errorDialog(text: String): ViewInteraction {
        return onView(withText(text))
                .inRoot(withDecorView(not(`is`(SpoonScreenshotUtils.getCurrentActivity().window.decorView))))
    }
}
