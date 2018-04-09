package com.expedia.bookings.test.pagemodels.trips

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isClickable
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isEnabled
import android.support.test.espresso.matcher.ViewMatchers.isFocusable
import android.support.test.espresso.matcher.ViewMatchers.withChild
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.By
import android.support.test.uiautomator.BySelector
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import android.support.test.uiautomator.Until
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed
import com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.StringEndsWith.endsWith
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit

object TripDetailsScreen {
    val device = Common.getUiDevice()
    val bodyScrollableContainer = allOf(withChild(withId(R.id.container)),
            withClassName(endsWith("ScrollView")))

    fun waitUntilLoaded() {
        waitForViewNotYetInLayoutToDisplay(bodyScrollableContainer, 30, TimeUnit.SECONDS)
    }

    private fun getFormattedDate(date: String, formatTo: String, inputFormat: String = "YYYY-MM-dd"): String {
        return DateTimeFormat.forPattern(inputFormat).parseDateTime(date).toString(formatTo)
    }

    object Toolbar {
        val toolbar = withId(R.id.widget_itin_toolbar)
        val backButton = allOf(isDescendantOfA(toolbar),
                withContentDescription("Back"))
        val title = withId(R.id.itin_toolbar_title)
        val subTitle = withId(R.id.itin_toolbar_subtitle)
        val shareButton = withId(R.id.itin_share_button)

        @JvmStatic fun verifyHotelName(hotelName: String) {
            onView(title).check(matches(withText(hotelName)))
        }

        @JvmStatic fun verifyDates(from: String, to: String) {
            onView(subTitle).check(matches(withText(
                    getFormattedDate(from, "MMM d") + " - " +
                            getFormattedDate(to, "MMM d")
            )))
        }

        @JvmStatic fun verifyShareButtonDescription(description: String) {
            onView(shareButton)
                    .perform(waitForViewToDisplay())
                    .check(matches(withContentDescription(description)))
        }

        @JvmStatic fun clickShareButton() {
            onView(shareButton).perform(click())
        }

        @JvmStatic fun clickBackButton() {
            onView(backButton).perform(click())
        }
    }

    object HotelInformation {
        val section = withId(R.id.hotel_itin_image)
        val hotelImage = withId(R.id.hotel_image)
        val hotelName = withId(R.id.hotel_name)
        val phoneNumberButton = allOf(isDescendantOfA(section), withId(R.id.summary_left_button))

        @JvmStatic fun verifyHotelName(name: String) {
            onView(hotelName).check(matches(withText(name)))
        }

        @JvmStatic fun verifyPhoneNumberText(phNumber: String) {
            onView(phoneNumberButton).check(matches(withText(phNumber)))
        }

        @JvmStatic fun verifyPhoneNumberButtonIsClickable() {
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

        @JvmStatic fun verifyCheckInDate(checkInDate: String) {
            onView(checkInDateText).check(matches(withText(getFormattedDate(checkInDate, "E, MMM d"))))
        }

        @JvmStatic fun verifyCheckOutDate(checkOutDate: String) {
            onView(checkOutDateText).check(matches(withText(getFormattedDate(checkOutDate, "E, MMM d"))))
        }

        @JvmStatic fun verifyCheckInTime(text: String) {
            onView(checkInTimeText).check(matches(withText(text)))
        }

        @JvmStatic fun verifyCheckOutTime(text: String) {
            onView(checkOutTimeText).check(matches(withText(text)))
        }
    }

    object HotelMap {
        val section = withId(R.id.widget_hotel_itin_location_details)
        val addressLine1 = withId(R.id.widget_hotel_itin_address_line_1)
        val addressLine2 = withId(R.id.widget_hotel_itin_address_line_2)

        val directionsButton = withId(R.id.expanded_map_view_hotel)
        val tripMap = withId(R.id.widget_hotel_itin_map)

        @Throws(UiObjectNotFoundException::class)
        @JvmStatic
        fun verifyMapDirectionButtonIsClickable() {
            val selector = UiSelector().className(FrameLayout::class.java)
            device.findObject(selector).isClickable
        }

        @Throws(UiObjectNotFoundException::class)
        @JvmStatic
        fun verifyMapMarkerPresent(hotelName: String) {
            device.waitForIdle(3000)
            val selector = UiSelector().descriptionContains(hotelName)
            val marker = device.findObject(selector)
            if (!marker.waitForExists(10000))
                throw UiObjectNotFoundException("marker not found")
        }

        @JvmStatic
        fun clickOnMap() {
            device.waitForIdle(3000)
            onView(tripMap).perform(scrollTo())
            onView(tripMap).perform(click())
        }

        fun waitForMapToLoad() {
            waitForViewNotYetInLayoutToDisplay(directionsButton, 30, TimeUnit.SECONDS)
        }
    }

    object BookedRoomDetails {
        val container = withId(R.id.hotel_itin_details_multi_room_container)
        val collapsedView = withId(R.id.itin_hotel_details_room_collapsed_view) //So far this works only with 1 room.
        val detailsText = withId(R.id.itin_hotel_details_room_details_text)
        val detailsChevron = withId(R.id.itin_hotel_room_details_chevron)
    }

    object HotelBookingInformation {
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

    object FlightConfirmation {
        val confirmationWidgetContainer = withId(R.id.itin_flight_confirmation_container)
        val confirmationLabel = allOf(isDescendantOfA(confirmationWidgetContainer),
                withId(R.id.confirmation_status_text_view)
        )
        val confirmationCodeField = allOf(isDescendantOfA(confirmationWidgetContainer),
                withId(R.id.confirmation_code_text_view)
        )

        @JvmStatic fun verifyConfirmationCode(confirmaitonCode: String) {
            onView(confirmationCodeField).check(matches(withText(confirmaitonCode)))
        }

        @JvmStatic fun assertConfirmationCodeIsClickable() {
            onView(confirmationCodeField).check(matches(isClickable()))
            onView(confirmationCodeField).check(matches(isEnabled()))
            onView(confirmationCodeField).check(matches(isFocusable()))
        }
    }

    object FlightSummary {
        //Container Hierarchy
        val summaryWidgetContainer = withId(R.id.flight_itin_summary_container)
        val departureDetailsContainer = allOf(isDescendantOfA(summaryWidgetContainer),
                withId(R.id.flight_itin_departure_details)
        )
        val arrivalDetailsContainer = allOf(isDescendantOfA(summaryWidgetContainer),
                withId(R.id.flight_itin_arrival_details)
        )
        val seatingContainer = allOf(isDescendantOfA(summaryWidgetContainer),
                withId(R.id.seating_container)
        )
        //Resulting Objects
        val airlineName = allOf(isDescendantOfA(summaryWidgetContainer), withId(R.id.flight_itin_airline_name))
        val departureAirport = allOf(isDescendantOfA(departureDetailsContainer), withId(R.id.flight_itin_departure_airport))
        val departureTerminalGate = allOf(isDescendantOfA(departureDetailsContainer), withId(R.id.flight_itin_departure_terminal_gate))
        val departureTime = allOf(isDescendantOfA(departureDetailsContainer), withId(R.id.flight_itin_departure_time))
        val arrivalAirport = allOf(isDescendantOfA(arrivalDetailsContainer), withId(R.id.flight_itin_arrival_airport))
        val arrivalTerminalGate = allOf(isDescendantOfA(arrivalDetailsContainer), withId(R.id.flight_itin_arrival_terminal_gate))
        val arrivalTime = allOf(isDescendantOfA(arrivalDetailsContainer), withId(R.id.flight_itin_arrival_time))
        val seating = allOf(isDescendantOfA(seatingContainer), withId(R.id.flight_itin_seating))
        val cabinClass = allOf(isDescendantOfA(seatingContainer), withId(R.id.flight_itin_cabin))

        @JvmStatic fun verifyAirlineName(name: String) {
            onView(airlineName).check(matches(withText(name)))
        }

        @JvmStatic fun verifyDepartureTime(time: String) {
            onView(departureTime).check(matches(withText(time)))
        }
        @JvmStatic fun verifyDepartureAirport(airportName: String) {
            onView(departureAirport).check(matches(withText(airportName)))
        }
        @JvmStatic fun verifyDepartureTerminalGate(terminalGateText: String) {
            onView(departureTerminalGate).check(matches(withText(terminalGateText)))
        }

        @JvmStatic fun verifyArrivalTime(time: String) {
            onView(arrivalTime).check(matches(withText(time)))
        }
        @JvmStatic fun verifyArrivalAirport(airportName: String) {
            onView(arrivalAirport).check(matches(withText(airportName)))
        }
        @JvmStatic fun verifyArrivalTerminalGate(terminalGateText: String) {
            onView(arrivalTerminalGate).check(matches(withText(terminalGateText)))
        }

        @JvmStatic fun verifySeating(seatingText: String) {
            onView(seating).check(matches(withText(seatingText)))
        }
        @JvmStatic fun verifyCabinClass(cabinClassText: String) {
            onView(cabinClass).check(matches(withText(cabinClassText)))
        }
    }

    object FlightDuration {
        val durationWidgetContainer = withId(R.id.widget_itin_flight_total_duration_cardview)
        val durationText = allOf(isDescendantOfA(durationWidgetContainer), withId(R.id.itin_duration_text))

        @JvmStatic fun verifyFlightDuration(text: String) {
            onView(durationText).check(matches(withText(text)))
        }
    }

    object FlightBaggageInformation {
        val baggageInformationContainer = withId(R.id.widget_itin_webview_info_cardview)
    }

    object FlightMap {
        val container = withId(R.id.widget_itin_flight_map)
        val actionButtonsContainer = allOf(isDescendantOfA(container), withId(R.id.itinActionButtons))

        val map = allOf(isDescendantOfA(container), withId(R.id.google_maps_lite_mapview))
        val terminalMapsButton = allOf(isDescendantOfA(actionButtonsContainer),
                withId(R.id.itin_action_button_left),
                withContentDescription("Terminal maps Button")
        )
        val directionsButton = allOf(isDescendantOfA(actionButtonsContainer),
                withId(R.id.itin_action_button_right),
                withContentDescription("Directions Button")
        )
        val terminalMapsButtonLabel = allOf(isDescendantOfA(terminalMapsButton), withId(R.id.itin_action_button_left_text))
        val directionsButtonLabel = allOf(isDescendantOfA(directionsButton), withId(R.id.itin_action_button_right_text))

        @JvmStatic fun assertMapIsDisplayed() {
            assertViewIsDisplayed(map)
        }

        @JvmStatic fun verifyTerminalMapsButtonLabel(text: String) {
            onView(terminalMapsButtonLabel).check(matches(withText(text)))
        }

        @JvmStatic fun clickTerminalMaps() {
            onView(terminalMapsButton).perform(click())
        }

        @JvmStatic fun verifyDirectionsButtonLabel(text: String) {
            onView(directionsButtonLabel).check(matches(withText(text)))
        }

        @JvmStatic fun clickDirections() {
            onView(directionsButton).perform(click())
        }
    }

    object FlightBookingInformation {
        val container = withId(R.id.widget_flight_itin_booking_details)
        val manageBooking = withId(R.id.itin_flight_manage_booking_card_view)
        val manageBookingHeading = allOf(isDescendantOfA(manageBooking),
                withId(R.id.link_off_card_heading))
        val manageBookingSubHeading = allOf(isDescendantOfA(manageBooking),
                withId(R.id.link_off_card_subheading))
        val travelerInformation = withId(R.id.itin_flight_traveler_info_card_view)
        val travelerInformationHeading = allOf(isDescendantOfA(travelerInformation),
                withId(R.id.link_off_card_heading))
        val travelerInformationSubHeading = allOf(isDescendantOfA(travelerInformation),
                withId(R.id.link_off_card_subheading))
        val priceSummary = withId(R.id.itin_flight_price_summary_card_view)
        val priceSummaryHeading = allOf(isDescendantOfA(priceSummary),
                withId(R.id.link_off_card_heading))
        val additionalInformation = withId(R.id.itin_flight_additional_info_card_view)
        val additionalInformationHeading = allOf(isDescendantOfA(additionalInformation),
                withId(R.id.link_off_card_heading))

        @JvmStatic fun verifyTravelerInformationHeading(text: String) {
            onView(travelerInformationHeading).check(matches(withText(text)))
        }

        @JvmStatic fun verifyTravelerInformationSubheading(text: String) {
            onView(travelerInformationSubHeading).check(matches(withText(text)))
        }

        @JvmStatic fun scrollToAdditionalInformation() {
            onView(additionalInformation).perform(scrollTo())
        }

        @JvmStatic fun verifyAdditionalInformationIsDisplayed() {
            assertViewIsDisplayed(additionalInformation)
        }
    }

    object AndroidNativeShareOptions {
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

        @JvmStatic fun waitForShareSuggestionsListToLoad() {
            device.wait(Until.findObject(optionsListSelector), timeout)
        }

        @JvmStatic fun clickOnIconWithText(appName: String) {
            device.findObject(By.res("android:id/text1").text(appName)).click()
        }

        @JvmStatic fun waitForAppToLoad(appName: String) {
            device.wait(Until.findObject(getBySelectorForAppName(appName.toUpperCase())), timeout)
        }

        @JvmStatic fun getBySelectorForAppName(appName: String): BySelector {
            return appList.get(appName.toUpperCase())?.bySelector!!
        }

        @JvmStatic fun getPackageNameForAppName(appName: String): String {
            return appList.get(appName.toUpperCase())?.packageName!!
        }

        private data class AppInfo(val packageName: String, val bySelector: BySelector)
    }
}
