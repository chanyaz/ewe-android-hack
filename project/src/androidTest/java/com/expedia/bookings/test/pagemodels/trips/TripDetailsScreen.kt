package com.expedia.bookings.test.pagemodels.trips

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isClickable
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withChild
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.By
import android.support.test.uiautomator.BySelector
import android.support.test.uiautomator.Until
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.StringEndsWith.endsWith
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit

object TripDetailsScreen {
    val bodyScrollableContainer = allOf(withChild(withId(R.id.container)),
            withClassName(endsWith("ScrollView")))

    fun waitUntilLoaded() {
        waitForViewNotYetInLayoutToDisplay(bodyScrollableContainer, 30, TimeUnit.SECONDS)
    }

    private fun getFormattedDate(date: String, formatTo: String, inputFormat: String = "YYYY-MM-dd"): String {
        return DateTimeFormat.forPattern(inputFormat).parseDateTime(date).toString(formatTo)
    }

    object Toolbar {
        val toolbar = withId(R.id.widget_hotel_itin_toolbar)
        val backButton = allOf(isDescendantOfA(toolbar),
                withContentDescription("Back"))
        val title = withId(R.id.itin_toolbar_title)
        val subTitle = withId(R.id.itin_toolbar_subtitle)
        val shareButton = withId(R.id.itin_share_button)

        fun verifyHotelName(hotelName: String) {
            onView(title).check(matches(withText(hotelName)))
        }

        fun verifyDates(from: String, to: String) {
            onView(subTitle).check(matches(withText(
                    getFormattedDate(from, "MMM d") + " - " +
                            getFormattedDate(to, "MMM d")
            )))
        }

        fun clickShareButton() {
            onView(shareButton).perform(click())
        }
    }

    object HotelInformation {
        val section = withId(R.id.hotel_itin_image)
        val hotelImage = withId(R.id.hotel_image)
        val hotelName = withId(R.id.hotel_name)
        val phoneNumberButton = allOf(isDescendantOfA(section), withId(R.id.summary_left_button))

        fun verifyHotelName(name: String) {
            onView(hotelName).check(matches(withText(name)))
        }

        fun verifyPhoneNumberText(phNumber: String) {
            onView(phoneNumberButton).check(matches(withText(phNumber)))
        }

        fun verifyPhoneNumberButtonIsClickable() {
            onView(phoneNumberButton).check(matches(isClickable()))
        }
    }

    object CheckInCheckOut {
        val checkInCheckOutDetails = withId(R.id.widget_hotel_itin_checkin_checkout_details)
        val checkInDateText = allOf(isDescendantOfA(checkInCheckOutDetails),
                withId(R.id.hotel_itin_checkin_date_text))
        val checkInTimeText = allOf(isDescendantOfA(checkInCheckOutDetails),
                withId(R.id.hotel_itin_checkin_time_text))
        val checkOutDateText = allOf(isDescendantOfA(checkInCheckOutDetails),
                withId(R.id.hotel_itin_checkout_date_text))
        val checkOutTimeText = allOf(isDescendantOfA(checkInCheckOutDetails),
                withId(R.id.hotel_itin_checkout_time_text))
        val checkInPolicies = allOf(isDescendantOfA(checkInCheckOutDetails),
                withId(R.id.hotel_itin_check_in_check_out_policies_container))

        fun verifyCheckInDate(checkInDate: String) {
            onView(checkInDateText).check(matches(withText(getFormattedDate(checkInDate, "E, MMM d"))))
        }

        fun verifyCheckOutDate(checkOutDate: String) {
            onView(checkOutDateText).check(matches(withText(getFormattedDate(checkOutDate, "E, MMM d"))))
        }

        fun verifyCheckInTime(text: String) {
            onView(checkInTimeText).check(matches(withText(text)))
        }

        fun verifyCheckOutTime(text: String) {
            onView(checkOutTimeText).check(matches(withText(text)))
        }
    }

    object HotelMap {
        val section = withId(R.id.widget_hotel_itin_location_details)
        val map = withId(R.id.widget_hotel_itin_map)
        val addressLine1 = withId(R.id.widget_hotel_itin_address_line_1)
        val addressLine2 = withId(R.id.widget_hotel_itin_address_line_2)
    }

    object BookedRoomDetails {
        val container = withId(R.id.hotel_itin_details_multi_room_container)
        val collapsedView = withId(R.id.itin_hotel_details_room_collapsed_view) //So far this works only with 1 room.
        val detailsText = withId(R.id.itin_hotel_details_room_details_text)
        val detailsChevron = withId(R.id.itin_hotel_room_details_chevron)
    }

    object BookingInformation {
        val container = withId(R.id.widget_hotel_itin_booking_details)
        val manageBooking = withId(R.id.itin_hotel_manage_booking_card_view)
        val manageBookingHeading = allOf(isDescendantOfA(manageBooking),
                withId(R.id.link_off_card_heading))
        val manageBookingSubHeading = allOf(isDescendantOfA(manageBooking),
                withId(R.id.link_off_card_subheading))
        val priceSummary = withId(R.id.itin_hotel_price_summary_card_view)
        val priceSummaryHeading = allOf(isDescendantOfA(priceSummary),
                withId(R.id.link_off_card_heading))
        val additionalInformation = withId(R.id.itin_hotel_additional_info_card_view)
        val additionalInformationHeading = allOf(isDescendantOfA(additionalInformation),
                withId(R.id.link_off_card_heading))
    }

    object ShareOptions {
        val device = Common.getUiDevice()
        private val timeout: Long = 30000
        private val optionsListSelector = By.res("android:id/resolver_list")
        private val appList = HashMap<String, AppInfo>()

        init {
            appList.set("GMAIL", AppInfo(packageName = "com.google.android.gm",
                    bySelector = By.pkg("com.google.android.gm").text("Compose")))
            appList.set("KAKAOTALK", AppInfo(packageName = "com.kakao.talk",
                    bySelector = By.pkg("com.kakao.talk")
                            .text("KakaoTalk needs access to the following:")))
            appList.set("LINE", AppInfo(packageName = "jp.naver.line.android",
                    bySelector = By.pkg("jp.naver.line.android")
                            .desc("Sign up for LINE, Free messaging")))
        }

        fun waitForShareSuggestionsListToLoad() {
            device.wait(Until.findObject(optionsListSelector), timeout)
        }

        fun clickOnIconWithText(appName: String) {
            device.findObject(By.res("android:id/text1").text(appName)).click()
        }

        fun waitForAppToLoad(appName: String) {
            device.wait(Until.findObject(getBySelectorForAppName(appName.toUpperCase())), timeout)
        }

        fun getBySelectorForAppName(appName: String): BySelector {
            return appList.get(appName.toUpperCase())?.bySelector!!
        }

        fun getPackageNameForAppName(appName: String): String {
            return appList.get(appName.toUpperCase())?.packageName!!
        }

        private data class AppInfo(val packageName: String, val bySelector: BySelector)
    }
}
