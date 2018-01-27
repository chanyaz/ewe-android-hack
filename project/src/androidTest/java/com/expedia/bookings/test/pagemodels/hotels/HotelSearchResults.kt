package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CustomMatchers.withIndex
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions.swipeUntilUiObjectIsVisible
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.startsWith
import org.junit.Assert.assertTrue
import java.util.concurrent.TimeUnit

object HotelSearchResults {
    //Generic
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    //Page
    private val page = withId(R.id.widget_hotel_results)
    //Page Header
    private val pageHeader = allOf(withParent(page), withId(R.id.hotel_results_toolbar))
    private val pageHeaderBackButton = allOf(withParent(pageHeader), instanceOf<Any>(android.widget.ImageButton::class.java), withContentDescription("Back"))
    private val pageHeaderTitle = allOf(withParent(pageHeader), withId(R.id.title))
    private val pageHeaderSubTitle = allOf(withParent(pageHeader), withId(R.id.subtitle))
    private val pageHeaderSearchButton = allOf(withParent(pageHeader), withId(R.id.menu_open_search))
    //Map
    //TBD X button in the header
    //TBD Return to results button, bottom right
    //Search Results Header
    private val resultsDescriptionContainer = withId(R.id.results_description_container)
    private val resultsDescriptionHeader = allOf(withParent(resultsDescriptionContainer), withId(R.id.results_description_header))
    private val resultsHeaderInfoIcon = allOf(withParent(resultsDescriptionContainer), withId(R.id.results_header_info_icon))
    private val resultsHeaderLoyaltyPoints = allOf(withParent(resultsDescriptionContainer), withId(R.id.loyalty_points_applied_message))
    private val searchIsInProgress = allOf(resultsDescriptionHeader, withText("Searching hotels…"))
    private val searchIsDone = allOf(resultsDescriptionHeader, withText(allOf(startsWith("Prices average per night"), endsWith("Results"))))
    //Hotel Results Collection
    private val hotelSearchResultsContainer = withId(R.id.list_view)
    private val uHotelSearchResultsContainer = UiSelector().resourceId("com.expedia.bookings.debug:id/list_view")
    //Individual Hotel
    private val uVIPLabel = UiSelector().resourceId("com.expedia.bookings.debug:id/vip_message").text("+VIP")
    private val vipLabel = allOf(withId(R.id.vip_message), withText("+VIP"))
    //Sort & Filter Footer
    private val sortAndFilterContainer = withId(R.id.sort_filter_button_container)
    private val uSortAndFilterContainer = UiSelector().resourceId("com.expedia.bookings.debug:id/sort_filter_button_container")
    private val sortAndFilterButton = allOf(withParent(sortAndFilterContainer), withId(R.id.sort_filter_button))
    private val sortAndFilterIcon = allOf(withParent(sortAndFilterContainer), withId(R.id.filter_icon))
    private val sortAndFilterText = allOf(withParent(sortAndFilterContainer), withId(R.id.filter_text))

    //View and Data Interactions, UiAutomator Objects
    @JvmStatic
    fun uPlusVipLabel(): UiObject {
        return device.findObject(uVIPLabel)
    }

    @JvmStatic
    fun uHotelSearchResultList(): UiObject {
        return device.findObject(uHotelSearchResultsContainer)
    }

    @JvmStatic
    fun uSortAndFilterContainer(): UiObject {
        return device.findObject(uSortAndFilterContainer)
    }

    @JvmStatic
    fun resultsDescriptionHeader(): ViewInteraction {
        return onView(resultsDescriptionHeader)
    }

    //Actions and Logic
    @JvmStatic
    @Throws(UiObjectNotFoundException::class)
    fun verifyVipLabelIsPresentInResultList() {
        swipeUntilVipLabelIsVisible(10)
        assertTrue("Verify Vip Label is present in the result list", uPlusVipLabel().exists())
    }

    @JvmStatic
    fun swipeUntilVipLabelIsVisible(maxSwipes: Int) {
        swipeUntilUiObjectIsVisible(maxSwipes, uPlusVipLabel(), uSortAndFilterContainer(), withIndex(vipLabel, 0), hotelSearchResultsContainer)
    }

    @JvmStatic
    fun waitForResultsToLoad() {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(searchIsDone, 30, TimeUnit.SECONDS)
    }
}
