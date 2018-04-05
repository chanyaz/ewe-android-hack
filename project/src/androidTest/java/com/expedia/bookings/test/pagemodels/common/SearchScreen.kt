package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.AppCompatImageButton
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils
import com.expedia.bookings.test.espresso.ViewActions
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is.`is`

object SearchScreen {
    private val suggestionList = withId(R.id.suggestion_list)
    @JvmField val didYouMeanAlert = allOf(
            isDescendantOfA(withId(R.id.action_bar_root)), withId(R.id.parentPanel),
            hasDescendant(withId(R.id.alertTitle)), hasDescendant(withText("Did you mean…"))
    )

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

    @JvmStatic fun incrementAdultsButton(): ViewInteraction {
        return onView(withId(R.id.adults_plus))
    }

    @JvmStatic fun removeAdultsButton(): ViewInteraction {
        return onView(withId(R.id.adults_minus))
    }

    @JvmStatic fun incrementChildButton(): ViewInteraction {
        return onView(withId(R.id.children_plus))
    }

    @JvmStatic fun removeChildButton(): ViewInteraction {
        return onView(withId(R.id.children_minus))
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
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.adult_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementYouthTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.youth_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementChildTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.child_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementInfantTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.infant_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_plus)))
    }

    @JvmStatic fun decrementAdultTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.adult_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementYouthTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.youth_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementChildTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.child_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementInfantTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(
                withId(R.id.infant_count_selector)))),
                withClassName(`is`(AppCompatImageButton::class.java.name)),
                withId(R.id.traveler_minus)))
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

    @JvmStatic fun didYouMeanAlertSuggestion(suggestionHasText: String): ViewInteraction {
        return onView(allOf(
                isDescendantOfA(didYouMeanAlert),
                withId(R.id.select_dialog_listview),
                hasDescendant(withText(containsString(suggestionHasText)))
        ))
    }
}
