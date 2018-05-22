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
import android.support.test.espresso.matcher.ViewMatchers.isSelected
import android.support.test.espresso.matcher.ViewMatchers.withChild
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withParentIndex
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CalendarPickerActions
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers.withIndex
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.espresso.ViewActions.swipeDown
import com.expedia.bookings.test.espresso.ViewActions.swipeUp
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

object HotelInfoSiteScreen {
    private val hotelDetail = withId(R.id.hotel_detail)
    private val detailContainer = withId(R.id.detail_container)
    private val plusVIPContainer = withId(R.id.vip_access_message_container)
    private val plusVIPLabel = withId(R.id.vip_access_message)
    private val travelDates = withId(R.id.hotel_search_info)
    private val numberOfGuests = withId(R.id.hotel_search_info_guests)
    private val payNowPayLaterTabs = withId(R.id.pay_now_pay_later_tabs)
    private val etpInfoText = withId(R.id.etp_info_text)
    private val headerLabelText = allOf(isDescendantOfA(withId(R.id.hotel_details_toolbar)),
            withId(R.id.hotel_name_text), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
    private val selectRoomStickyBottomButton = allOf(withId(R.id.sticky_bottom_button),
            withText("Select a Room"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))

    enum class RoomIndex(val index: Int) {
        DEFAULT(0),
        ONE(0),
        TWO(2),
        THREE(4),
        FOUR(6),
        FIVE(8)
    }

    /**
     * This Class instantiates individual rooms. Every object inside belongs only to that room instance
     * The default room selection is the very first available room. Passing a RoomIndex to the constructor
     * will change the room being used.
     * When adding new objects, make sure each includes isDescendantOfA(room) or any other descendants,
     * which reference the room scope.
     * Each new item also must have the capability to handle multiple price options, if applicable, for a case when
     * a room has more than one price. This happens in cases such as when the room has non-cancellable
     * price, free cancellation price, all inclusive price, and/or others.
     *
     * Sample Usages:
     *
     * HotelInfoSiteScreen.Room().clickBookButton()
     *
     * HotelInfoSiteScreen.Room().clickBookButton(safeguardEnabled = true)
     *
     * roomIndex = HotelInfoSiteScreen.RoomIndex.ONE
     * HotelInfoSiteScreen.Room(roomIndex).clickBookButton()
     *
     * val priceOption = HotelInfoSiteScreen.Room.PriceOption.ONE
     * HotelInfoSiteScreen.Room().setOptionNumber(priceOption)
     * HotelInfoSiteScreen.Room().clickBookButton()
     */
    class Room(roomIndex: RoomIndex = RoomIndex.DEFAULT) {
        private val innerRoomInfoContainer = withId(R.id.hotel_room_info_container)
        private val room = allOf(withChild(innerRoomInfoContainer), withParentIndex(roomIndex.index))
        private val priceContainer = allOf(isDescendantOfA(room), withParent(innerRoomInfoContainer),
                withChild(withId(R.id.price_button_container)))
        private var option: Matcher<View>? = null

        fun setPriceOption(priceOption: PriceOption = PriceOption.DEFAULT) {
            this.option = allOf(
                    priceContainer,
                    withChild(withChild(withText(priceOption.option)))
            )
        }

        fun isContainingMultiplePriceOptions(): Boolean {
            try {
                onView(allOf(
                        priceContainer,
                        withChild(withChild(withText(PriceOption.ONE.option)))
                )).check(matches(isDisplayed()))
                return true
            } catch (ignored: Exception) { }
            return false
        }

        /**
         * @param:  safeguardEnabled Use this to force this method to click on a book button, regardless
         * of whether the multi-price options are displayed or not. Useful when the test doesn't care
         * for multiple options, and is looking to click on book button in order to progress to the next view
         */
        fun clickBookButton(safeguardEnabled: Boolean = false) {
            val bookButton: ViewInteraction
            val scrollable: ViewInteraction

            try {
                // If sticky select is visible, click it.
                onView(selectRoomStickyBottomButton)
                        .check(matches(isDisplayed()))
                        .perform(click())
            } catch (ignored: AssertionFailedError) { }

            if (option != null || isContainingMultiplePriceOptions()) {
                //Case when there are multiple options per room, each containing a book button
                if (option == null && safeguardEnabled) { setPriceOption(PriceOption.DEFAULT) } //Safeguard. Ideally it should be already set
                bookButton = onView(allOf(isDescendantOfA(option), withId(R.id.hotel_book_button)))
                scrollable = onView(option)
            } else {
                //Case when there is only one book button per room
                bookButton = onView(allOf(isDescendantOfA(priceContainer), withId(R.id.hotel_book_button)))
                scrollable = onView(priceContainer)
            }

            scrollable.perform(scrollTo())
            Common.delay(1)
            scrollable.perform(swipeUp())
            if (!EspressoUtils.existsOnScreen(bookButton)) {
                scrollable.perform(scrollTo(), swipeUp())
            }
            if (!EspressoUtils.existsOnScreen(bookButton)) {
                scrollable.perform(scrollTo(), swipeDown())
            }

            bookButton.perform(click())
        }

        enum class PriceOption(val option: String) {
            DEFAULT("Option 1"),
            ONE("Option 1"),
            TWO("Option 2"),
            THREE("Option 3"),
            FOUR("Option 4"),
            FIVE("Option 5")
        }
    }

    // Views
    @JvmStatic
    fun travelDates(): ViewInteraction {
        return onView(travelDates)
    }

    @JvmStatic
    fun numberOfGuests(): ViewInteraction {
        return onView(numberOfGuests)
    }

    @JvmStatic
    fun reviews(): ViewInteraction {
        return onView(withId(R.id.widget_hotel_reviews))
    }

    @JvmStatic
    fun etpAndFreeCancellationMessagingContainer(): ViewInteraction {
        return onView(withId(R.id.etp_and_free_cancellation_messaging_container))
    }

    @JvmStatic
    fun etpInfoText(): ViewInteraction {
        return onView(etpInfoText)
    }

    @JvmStatic
    fun etpInfoTextSmall(): ViewInteraction {
        return onView(withId(R.id.etp_info_text_small))
    }

    @JvmStatic
    fun freeCancellation(): ViewInteraction {
        return onView(withId(R.id.free_cancellation))
    }

    @JvmStatic
    fun freeCancellationSmall(): ViewInteraction {
        return onView(withId(R.id.free_cancellation_small))
    }

    @JvmStatic
    fun horizontalDividerBwEtpAndFreeCancellation(): ViewInteraction {
        return onView(withId(R.id.horizontal_divider_bw_etp_and_free_cancellation))
    }

    @JvmStatic
    fun etpPlaceholder(): ViewInteraction {
        return onView(payNowPayLaterTabs)
    }

    @JvmStatic
    fun payNowAndLaterOptions(): ViewInteraction {
        return onView(payNowPayLaterTabs)
    }

    @JvmStatic
    fun payNowAndLaterOptionsIsPresent(): ViewInteraction {
        return onView(payNowPayLaterTabs).check(matches(isDisplayed()))
    }

    @JvmStatic
    fun etpTextIsNotPresent(): ViewInteraction {
        return onView(etpInfoText).check(matches(not(isDisplayed())))
    }

    @JvmStatic
    fun roomsContainer(): ViewInteraction {
        return onView(withId(R.id.room_container))
    }

    @JvmStatic
    fun resortFeesText(): ViewInteraction {
        return onView(withId(R.id.resort_fees_text))
    }

    @JvmStatic
    fun ratingContainer(): ViewInteraction {
        return onView(withId(R.id.rating_container))
    }

    @JvmStatic
    fun amenityContainer(): ViewInteraction {
        return onView(withId(R.id.amenities_table_row))
    }

    @JvmStatic
    fun commonAmenitiesText(): ViewInteraction {
        return onView(withId(R.id.common_amenities_text))
    }

    @JvmStatic
    fun hotelDetailsStarRating(): ViewInteraction {
        return onView(withId(R.id.hotel_star_rating_bar))
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
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(detailContainer, 10, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun validateTravelDates(dateString: String) {
        travelDates().check(matches(withText(dateString)))
    }

    @JvmStatic
    fun validateNumberOfGuests(guestsString: String) {
        numberOfGuests().check(matches(withText(guestsString)))
    }

    @JvmStatic
    fun verifyHeaderLabelText(expectedText: String) {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(headerLabelText, 10, TimeUnit.SECONDS)
        onView(headerLabelText).check(matches(withText(expectedText)))
    }

    @JvmStatic
    fun verifyHotelFeesAppear() {
        resortFeesText().check(matches(isDisplayed()))
    }

    @JvmStatic
    fun clickHotelFeesInfo() {
        verifyHotelFeesAppear()
        resortFeesText().perform(click())
    }

    @JvmStatic
    fun clickRenoInfo() {
        onView(withId(R.id.renovation_container)).perform(scrollTo(), click())
    }

    @JvmStatic
    fun clickPayLater() {
        onView(withId(R.id.right_tab_text_view)).perform(click())
        onView(allOf(withId(R.id.right_tab_text_view), isSelected())).perform(waitForViewToDisplay())
    }

    @JvmStatic
    fun clickPayNow() {
        onView(withId(R.id.left_tab_text_view)).perform(click())
        onView(allOf(withId(R.id.left_tab_text_view), isSelected())).perform(waitForViewToDisplay())
    }

    @JvmStatic
    fun clickRatingContainer() {
        onView(withId(R.id.rating_container)).perform(scrollTo(), click())
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun clickSwPToggle() {
        onView(withId(R.id.swp_switch)).perform(click())
    }

    @JvmStatic
    fun clickVIPAccess() {
        onView(withId(R.id.vip_access_message_container)).perform(click())
    }

    @JvmStatic
    fun clickDetailsMiniMap() {
        onView(withId(R.id.map_click_container)).perform(scrollTo(), click())
    }

    @JvmStatic
    fun clickSelectARoomInFullMap() {
        onView(withId(R.id.hotel_map_select_a_room_bar)).perform(click())
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

    object MandatoryFeesAndTaxes {
        private var headerTitle = allOf(withId(R.id.info_header), withText("Mandatory Fees and Taxes"))
        private var sectionContainer = withChild(headerTitle)
        private var sectionBodyText = allOf(withParent(sectionContainer), withId(R.id.info_text))

        // View and Data Interactions
        @JvmStatic
        fun sectionBodyText(): ViewInteraction {
            return onView(sectionBodyText)
        }

        // Actions
        @JvmStatic
        fun verifyBodyText(text: String, isDisplayed: String) {
            if (isDisplayed.equals("displayed")) {
                sectionBodyText().check(matches(withText(containsString(text))))
            } else {
                sectionBodyText().check(matches(not(withText(containsString(text)))))
            }
        }
    }

    object AdditionalFeeInfo {
        private var sectionContainer = withId(R.id.hotel_detail_desc)
        private var sectionBodyText = withId(R.id.content_description)

        // View and Data Interactions
        @JvmStatic
        fun sectionContainer(): ViewInteraction {
            return onView(sectionContainer)
        }

        @JvmStatic
        fun sectionBodyText(): ViewInteraction {
            return onView(sectionBodyText)
        }

        // Actions
        @JvmStatic
        fun verifyAdditionalFeeContainerAppear() {
            sectionContainer().check(matches(isDisplayed()))
        }

        @JvmStatic
        fun verifyDepositAndResortFeesContent(depositText: String, resortFeeText: String) {
            sectionBodyText().check(matches(withText(containsString(depositText))))
            sectionBodyText().check(matches(withText(containsString(resortFeeText))))
        }
    }

    // View
    @JvmStatic
    fun stickySelectRoomButton(): ViewInteraction {
        return onView(selectRoomStickyBottomButton)
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
        Common.delay(1)
        firstCardView().perform(swipeUp())
        if (!EspressoUtils.existsOnScreen(firstBookButton())) {
            firstCardView().perform(swipeUp())
        }
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

    @JvmStatic
    fun changeDatesCalendarIsDisplayed(): Boolean {
        try {
            changeDatesCalendar().check(matches(isDisplayed()))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    @JvmStatic
    fun changeDatesCalendar(): ViewInteraction {
        return onView(withId(R.id.change_dates_picker))
    }

    @JvmStatic
    fun changeDatesCalendarDoneButton(): ViewInteraction {
        return onView(withId(R.id.change_dates_done_button))
    }

    @JvmStatic
    fun chooseDatesWithDialog(start: LocalDate, end: LocalDate?) {
        chooseDates(start, end)
        changeDatesCalendarDoneButton().perform(click())
    }

    @JvmStatic
    fun chooseDates(start: LocalDate, end: LocalDate?) {
        changeDatesCalendar().perform(CalendarPickerActions.clickDates(start, end))
    }

    // Helper
    @JvmStatic
    fun waitForDetailsLoaded() {
        waitForViewNotYetInLayoutToDisplay(hotelDetail, 10, TimeUnit.SECONDS)
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
