package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.hasSibling
import android.support.test.espresso.matcher.ViewMatchers.isClickable
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withChild
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CustomMatchers.withIndex
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.espresso.ViewActions.swipeDown
import com.expedia.bookings.test.espresso.ViewActions.swipeUp
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not

object HotelInfoSiteScreen {
    //Matchers
    //Containers
    private val detailContainer = withId(R.id.detail_container)
    //Individual Objects
    private val plusVIPContainer = withId(R.id.vip_access_message_container)
    private val plusVIPLabel = withId(R.id.vip_access_message)
    private val travelDates = withId(R.id.hotel_search_info)
    private val numberOfGuests = withId(R.id.hotel_search_info_guests)

    // Views
    @JvmStatic
    fun infositeDetailContainer(): ViewInteraction {
        return onView(detailContainer)
    }

    @JvmStatic
    fun plusVIPlabel(): ViewInteraction {
        return onView(plusVIPLabel)
    }

    @JvmStatic
    fun travelDates(): ViewInteraction {
        return onView(travelDates)
    }

    @JvmStatic
    fun numberOfGuests(): ViewInteraction {
        return onView(numberOfGuests)
    }

    // Actions
    @JvmStatic
    fun clickOnVIPAccess() {
        onView(plusVIPContainer).perform(click())
    }

    @JvmStatic
    fun verifyVIPAccessLabelIsPresent() {
        onView(plusVIPLabel).check(matches(isDisplayed()))
        onView(plusVIPLabel).check(matches(not(withText(isEmptyString()))))
    }

    @JvmStatic
    fun waitForPageToLoad() {
        infositeDetailContainer().perform(ViewActions.waitForViewToDisplay())
    }

    @JvmStatic
    fun validateTravelDates(dateString: String) {
        travelDates().check(matches(withText(dateString)))
    }

    @JvmStatic
    fun validateNumberOfGuests(guestsString: String) {
        numberOfGuests().check(matches(withText(guestsString)))
    }

    object VIPAccess {
        private val vipAccessPage = withId(R.id.hotel_vip_access_info)
        private val header = allOf(withId(R.id.toolbar), isDisplayed())
        private val headerCloseButton = allOf(withParent(header), instanceOf<Any>(android.widget.ImageButton::class.java), isClickable())
        private val headerLabel = allOf(withParent(header), instanceOf<Any>(android.widget.TextView::class.java))
        private val viewBody = allOf(withParent(withId(R.id.container)), instanceOf<Any>(android.widget.ScrollView::class.java), isDisplayed())
        private val viewBodyText = allOf(withParent(viewBody), instanceOf<Any>(android.widget.TextView::class.java))

        // View and Data Interactions
        @JvmStatic
        fun headerCloseButton(): ViewInteraction {
            return onView(headerCloseButton)
        }

        @JvmStatic
        fun headerLabel(): ViewInteraction {
            return onView(headerLabel)
        }

        @JvmStatic
        fun vipAccessPage(): ViewInteraction {
            return onView(vipAccessPage)
        }

        @JvmStatic
        fun viewBody(): ViewInteraction {
            return onView(viewBody)
        }

        @JvmStatic
        fun viewBodyText(): ViewInteraction {
            return onView(viewBodyText)
        }

        // Actions
        @JvmStatic
        fun clickHeaderCloseButton() {
            headerCloseButton().perform(click())
        }

        @JvmStatic
        fun waitForViewToLoad() {
            vipAccessPage().perform(ViewActions.waitForViewToDisplay())
        }

        @JvmStatic
        fun verifyHeaderText(text: String) {
            headerLabel().check(matches(withText(text)))
        }

        @JvmStatic
        fun verifyBodyText(text: String) {
            viewBodyText().check(matches(allOf(withText(text), isDisplayed())))
        }
    }

    // View
    @JvmStatic
    fun stickySelectRoomButton(): ViewInteraction {
        return onView(allOf(withId(R.id.sticky_bottom_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @JvmStatic
    fun firstBookButton(): ViewInteraction {
        return onView(withIndex(withId(R.id.hotel_book_button), 0))
    }

    @JvmStatic
    fun getBookButtonAtIndex(index: Int): ViewInteraction {
        return onView(withIndex(withId(R.id.hotel_book_button), index))
    }

    @JvmStatic
    fun firstCardView(): ViewInteraction {
        return onView(withIndex(withChild(withChild(withId(R.id.room_type_text_view))), 0))
    }

    @JvmStatic
    fun getCardViewAtIndex(index: Int): ViewInteraction {
        return onView(withIndex(withChild(withChild(withId(R.id.room_type_text_view))), index))
    }

    @JvmStatic
    fun getRootView(): ViewInteraction {
        return onView(withId(R.id.detail_container))
    }

    @JvmStatic
    fun roomCardViewForRoomType(roomType: String): ViewInteraction {
        return onView(
                withChild(withChild(allOf(
                        withId(R.id.room_type_text_view),
                        withText(roomType)
                )))
        )
    }

    @JvmStatic
    fun bookButtonForRoomType(roomType: String): ViewInteraction {
        return onView(withIndex(allOf(
                withId(R.id.hotel_book_button),
                withParent(allOf(
                        withId(R.id.hotel_room_row_button),
                        withParent(allOf(
                                withId(R.id.price_button_container),
                                descendantOfSameGroupRoom(roomType)
                        ))
                ))
        ), 0))
    }

    @JvmStatic
    fun bedTypeViewForRoomType(roomType: String): ViewInteraction {
        return onView(withIndex(allOf(
                withId(R.id.bed_type_text_view),
                hasSibling(allOf(
                        withId(R.id.room_type_text_view),
                        withText(roomType)
                ))
        ), 0))
    }

    @JvmStatic
    fun freeCancellationViewForRoomType(roomType: String): ViewInteraction {
        return onView(withIndex(allOf(
                withId(R.id.cancellation_text_view),
                withParent(allOf(
                        withId(R.id.value_adds_point_fee_container),
                        descendantOfSameGroupRoom(roomType)
                ))
        ), 0))
    }

    @JvmStatic
    fun pricePerDescriptorViewWithTextAndPrice(perDescriptorString: String, priceString: String): ViewInteraction {
        return onView(allOf(
                withId(R.id.price_per_descriptor_text_view),
                withText(perDescriptorString),
                hasSibling(allOf(
                        withId(R.id.price_text_view),
                        withText(priceString)
                ))
        ))
    }

    // Action
    @JvmStatic
    fun clickStickySelectRoom() {
        waitForDetailsLoaded()
        stickySelectRoomButton().perform(click())
    }

    @JvmStatic
    fun bookFirstRoom() {
        firstCardView().perform(scrollTo())
        firstCardView().perform(swipeUp())
        if (!EspressoUtils.existsOnScreen(firstBookButton())) {
            getRootView().perform(swipeDown())
        }
        firstBookButton().perform(click())
    }

    @JvmStatic
    fun bookRoomAtIndex(index: Int) {
        getCardViewAtIndex(index).perform(scrollTo())
        getCardViewAtIndex(index).perform(swipeUp())
        getBookButtonAtIndex(index).perform(click())
    }

    @JvmStatic
    fun bookRoomType(roomType: String) {
        waitForDetailsLoaded()
        roomCardViewForRoomType(roomType).perform(scrollTo(), swipeUp())
        bookButtonForRoomType(roomType).perform(scrollTo())
        bookButtonForRoomType(roomType).perform(click())
    }

    // Helper
    @JvmStatic
    fun waitForDetailsLoaded() {
        onView(withId(R.id.hotel_detail)).perform(waitForViewToDisplay())
    }

    @JvmStatic
    fun descendantOfSameGroupRoom(roomType: String): Matcher<View> {
        return isDescendantOfA(hasSibling(withChild(allOf(withId(R.id.room_type_text_view), withText(roomType)))))
    }

    @JvmStatic
    fun descendantOfSameGroupRoomWithBed(roomType: String, bedType: String): Matcher<View> {
        return isDescendantOfA(hasSibling(allOf(
                withChild(allOf(
                        withId(R.id.room_type_text_view),
                        withText(roomType)
                )),
                withChild(allOf(
                        withId(R.id.bed_type_text_view),
                        withText(bedType)
                ))
        )))
    }
}
