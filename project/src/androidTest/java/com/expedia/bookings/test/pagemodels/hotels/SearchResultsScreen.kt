package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withParentIndex
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject
import android.support.test.uiautomator.UiSelector
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import java.util.concurrent.TimeUnit

object SearchResultsScreen {
    //Generic
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    //Individual Hotel
    private val uVIPLabel = UiSelector().resourceId("com.expedia.bookings.debug:id/vip_message").text("+VIP")
    private val vipLabel = allOf(withId(R.id.vip_message), withText("+VIP"))
    //Sort & Filter Footer
    private val uSortAndFilterContainer = UiSelector().resourceId("com.expedia.bookings.debug:id/sort_filter_button_container")

    object Toolbar {
        private val container = withId(R.id.hotel_results_toolbar)
        private val backButton = allOf(withParent(container), withClassName(containsString("ImageButton")), withContentDescription("Back"))
        private val title = allOf(withParent(container), withId(R.id.title))
        private val subTitle = allOf(withParent(container), withId(R.id.subtitle))
        private val searchButton = allOf(withParent(container), withId(R.id.menu_open_search))
    }

    object SearchResultList {
        val resultList = withId(R.id.list_view)
        val resultRoot = withId(R.id.root)
        //Search Results Header
        private val resultsDescriptionContainer = withId(R.id.results_description_container)
        private val resultsDescriptionHeader = allOf(withParent(resultsDescriptionContainer), withId(R.id.results_description_header))
        private val resultsHeaderLoyaltyPoints = allOf(withParent(resultsDescriptionContainer), withId(R.id.loyalty_points_applied_message))
        private val searchIsInProgress = allOf(resultsDescriptionHeader, withText("Searching hotels…"))
        val searchIsDone = allOf(resultsDescriptionHeader, withText(allOf(startsWith("Prices average per night"), containsString("Results"))))
        //Hotel Results Collection
        private val uHotelSearchResultsContainer = UiSelector().resourceId("com.expedia.bookings.debug:id/list_view")
        //Sort & Filter
        private val uSortAndFilterContainer = UiSelector().resourceId("com.expedia.bookings.debug:id/sort_filter_button_container")

        @JvmStatic fun scrollToResultWithName(name: String): IndividualResult {
            val container = allOf(resultRoot, hasDescendant(withText(equalTo(name))))
            onView(resultList).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(container))
            return resultWithName(name)
        }

        @JvmStatic fun resultWithName(name: String): IndividualResult {
            val container = allOf(resultRoot, hasDescendant(withText(equalTo(name))))
            return IndividualResult(container)
        }

        @JvmStatic fun scrollToResultNumber(resultNumber: Int): IndividualResult {
            onView(resultList).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(resultNumber + 1))
            return resultAtPosition(resultNumber)
        }

        @JvmStatic fun resultAtPosition(resultNumber: Int): IndividualResult {
            return IndividualResult(allOf(
                    resultRoot,
                    //Naming convention sucks, but the next line actually finds a parent, and then selects a child,
                    // at index, within a parent. So this is essentially a "withIndex" method.
                    withParentIndex(resultNumber + 1)
            ))
        }
    }

    class IndividualResult(container: Matcher<View>) {
        var container: Matcher<View>
        val pinnedHotel: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.pinned_hotel_view))
        val vipLabel: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.vip_message))
        val hotelName: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.hotel_name))
        val discountPercentage: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.discount_percentage))
        val guestRating: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.guest_rating))
        val earnPoints: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.earn_messaging))
        val strikeThroughPrice: Matcher<View>; get() = allOf(isDescendantOfA(container), withId(R.id.strike_through_price))

        init { this.container = container }

        fun verifyIsPinned() {
            onView(pinnedHotel).check(matches(isDisplayed()))
        }

        fun verifyName(name: String) {
            onView(hotelName).check(matches(withText(equalTo(name))))
        }

        fun verifyVipLabelIsVisible() {
            onView(vipLabel).check(matches(isDisplayed()))
        }

        fun click() {
            onView(container).perform(ViewActions.click())
        }
    }

    //View and Data Interactions, UiAutomator Objects
    @JvmStatic
    fun uPlusVipLabel(): UiObject {
        return device.findObject(uVIPLabel)
    }

    @JvmStatic
    fun waitForResultsToLoad() {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(SearchResultList.searchIsDone, 30, TimeUnit.SECONDS)
    }
}
