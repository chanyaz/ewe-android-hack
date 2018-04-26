package com.expedia.bookings.test.stepdefs.phone.bundleDeals

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.hasSibling
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isEnabled
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CustomMatchers
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.stepdefs.phone.TestUtil
import com.expedia.bookings.test.stepdefs.phone.TestUtil.getDateInEEMMMddyyyy
import com.expedia.bookings.test.stepdefs.phone.TestUtil.getDateInMMMdd
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.hamcrest.core.AllOf
import org.hamcrest.core.AllOf.allOf
import java.util.concurrent.TimeUnit

class PackageOverviewScreen {

    @And("^I click on View your bundle$")
    @Throws(Throwable::class)
    fun performClickOnViewYourBundle() {
        onView(withText(getViewYourBundleText(BuildConfig.brand))).perform(click())
    }

    @And("^I click to close sliding bundle$")
    @Throws(Throwable::class)
    fun performClickOnViewYourBundleToClose() {
        onView(allOf<View>(withId(R.id.bundle_price_widget), isDescendantOfA(withId(R.id.sliding_bundle_widget)))).perform(click())
    }

    @And("^on POS Validate that Package Overview Screen is displayed$")
    @Throws(Throwable::class)
    fun validatePackageOverviewScreen() {
        onView(allOf<View>(withId(R.id.bundle_widget), isDescendantOfA(withId(R.id.sliding_bundle_widget))))
                .perform(waitForViewToDisplay())
                .check(matches(isDisplayed()))
    }

    @Then("^on Package Overview Screen validate the toolbar$")
    @Throws(Throwable::class)
    fun validateToolbarOnPOS(expParameters: Map<String, String>) {
        val startDate = getDateInMMMdd(expParameters["start_date"])
        val endDate = getDateInMMMdd(expParameters["end_date"])
        onView(allOf<View>(withId(R.id.bundle_title), isDescendantOfA(withId(R.id.bundle_price_widget))))
                .check(matches(withText("Trip to " + expParameters["destination"])))
        onView(allOf<View>(withId(R.id.bundle_subtitle), isDescendantOfA(withId(R.id.bundle_price_widget))))
                .check(matches(withText(startDate + " - " + endDate + ", " + expParameters["totalTravelers"])))
    }

    @Then("^on Package Overview Screen validate the toolbar when hotel, outbound and inbound flight is selected$")
    @Throws(Throwable::class)
    fun validateToolbarOnPOSInboundFlight(expParameters: Map<String, String>) {
        val startDate = getDateInEEMMMddyyyy(expParameters["start_date"])
        val endDate = getDateInEEMMMddyyyy(expParameters["end_date"])
        if (expParameters["destination"] != null) {
            onView(allOf<View>(withId(R.id.destination), isDescendantOfA(withId(R.id.checkout_overview_floating_toolbar))))
                    .check(matches(withText(expParameters["destination"])))
        }
        onView(allOf<View>(withId(R.id.check_in_out_dates), isDescendantOfA(withId(R.id.checkout_overview_floating_toolbar))))
                .check(matches(withText(startDate + " - " + endDate)))
        onView(allOf<View>(withId(R.id.travelers), isDescendantOfA(withId(R.id.checkout_overview_floating_toolbar))))
                .check(matches(withText(expParameters["totalTravelers"])))
    }

    @Then("^validate hotel selection step label$")
    fun validateHotelSelectionLabelIsDisplayed(expParameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.step_one_text), isDescendantOfA(withId(R.id.bundle_widget))))
                .perform(waitForViewToDisplay())
                .check(matches(withText(expParameters["info_text"])))
    }

    @Then("^validate hotel widget data on hotel overview widget$")
    @Throws(Throwable::class)
    fun validateHotelWidgetDataIsDisplayed(expParameters: Map<String, String>) {
        onView(allOf<View>(withParent(withParent(withParent(withParent(withId(R.id.package_bundle_hotel_widget))))),
                withId(R.id.hotels_dates_guest_info_text)))
                .check(matches(withText(expParameters["info_text"])))
    }

    @And("^validate hotel widget luggage image icon is checked$")
    fun validateHotelWidgetLuggageIcon() {
        onView(allOf<View>(withId(R.id.package_hotel_luggage_icon), isDescendantOfA(withId(R.id.package_bundle_hotel_widget))))
                .check(matches(CustomMatchers.withImageDrawable(R.drawable.packages_hotels_checkmark_icon)))
    }

    @Then("^I click on hotel widget details icon$")
    fun clickHotelWidgetDetailIcon() {
        onView(allOf<View>(withId(R.id.package_hotel_details_icon), isDescendantOfA(withId(R.id.package_bundle_hotel_widget))))
                .perform(click())
    }

    @And("^verify hotel widget detail view is displayed$")
    fun validateHotelWidgetDetailIsDisplayed() {
        onView(allOf<View>(withId(R.id.main_container), isDescendantOfA(withId(R.id.package_bundle_hotel_widget))))
                .perform(waitForViewToDisplay())
                .check(matches(isDisplayed()))
    }

    @Then("^verify hotel widget detail data$")
    @Throws(Throwable::class)
    fun validateHotelWidgetDetail(expParameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.hotel_room_info), isDescendantOfA(withId(R.id.expanded_hotel_container))))
                .check(matches(withText(expParameters["room_info"])))
        onView(allOf<View>(withId(R.id.hotel_room_type), isDescendantOfA(withId(R.id.expanded_hotel_container))))
                .check(matches(withText(expParameters["room_type"])))
        onView(allOf<View>(withId(R.id.hotel_address), isDescendantOfA(withId(R.id.expanded_hotel_container))))
                .check(matches(withText(expParameters["hotel_address"])))
        onView(allOf<View>(withId(R.id.hotel_city), isDescendantOfA(withId(R.id.expanded_hotel_container))))
                .check(matches(withText(expParameters["hotel_city"])))
        onView(allOf<View>(withId(R.id.hotel_free_cancellation), isDescendantOfA(withId(R.id.expanded_hotel_container))))
                .check(matches(withText(expParameters["cancellation_status"])))
        onView(allOf<View>(withId(R.id.hotel_promo_text), isDescendantOfA(withId(R.id.expanded_hotel_container))))
                .check(matches(withText(expParameters["sale_status"])))
    }

    @Then("^validate outbound flight selection label$")
    @Throws(Throwable::class)
    fun validateOutboundFlightLabel(expParameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.step_two_text), isDescendantOfA(withId(R.id.bundle_widget))))
                .perform(waitForViewToDisplay())
                .check(matches(withText(expParameters["info_text"])))
    }

    @Then("^I click on package flight details icon")
    fun clickPackageFlightDetailsIcon() {
        onView(allOf<View>(withId(R.id.package_flight_details_icon),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .perform(click())
    }

    @Then("^I click on package flight inbound details icon")
    fun clickPackageFlightInboundDetailsIcon() {
        onView(allOf<View>(withId(R.id.package_flight_details_icon),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .perform(click())
    }

    @Then("^validate package outbound flight icon is checked")
    fun validatePackageOutboundFlightIcon() {
        onView(allOf<View>(withId(R.id.package_flight_icon), isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(CustomMatchers.withImageDrawable(R.drawable.packages_flight1_checkmark_icon)))
    }

    @Then("^verify package outbound flight widget view is displayed : (true|false)")
    fun validateOutboundFlightWidgetDetailIsDisplayed(check: Boolean) {
        if (!check) {
            onView(allOf<View>(withId(R.id.flight_details_container),
                    isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                    .check(matches(not(isDisplayed())))
        } else {
            onView(allOf<View>(withId(R.id.flight_details_container),
                    isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                    .perform(waitForViewToDisplay())
                    .check(matches(isDisplayed()))
        }
    }

    @Then("^verify package inbound flight widget view is displayed : (true|false)")
    fun validateInboundFlightWidgetDetailIsDisplayed(check: Boolean) {
        if (!check) {
            onView(allOf<View>(withId(R.id.flight_details_container),
                    isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                    .check(matches(not(isDisplayed())))
        } else {
            onView(allOf<View>(withId(R.id.flight_details_container),
                    isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                    .perform(waitForViewToDisplay())
                    .check(matches(isDisplayed()))
        }
    }

    @Then("^verify package outbound flight icon is displayed")
    fun validateOutboundFlightIcon() {
        onView(allOf<View>(withId(R.id.imageView),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(CustomMatchers.withImageDrawable(R.drawable.flights_details_icon_flight)))
    }

    @Then("^verify package inbound flight icon is displayed")
    fun validateInboundFlightIcon1() {
        onView(allOf<View>(withId(R.id.imageView),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(CustomMatchers.withImageDrawable(R.drawable.flights_details_icon_flight)))
    }

    @Then("^validate package outbound flight data on package outbound flight widget")
    fun validateOutboundFlightWidgetDataIsDisplayed(expParameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.flight_card_view_text),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(expParameters["flight_to"])))
        onView(allOf<View>(withId(R.id.travel_info_view_text),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(expParameters["info_text"])))
    }

    @Then("^validate package inbound flight data on package inbound flight widget")
    fun validateInboundFlightWidgetDataIsDisplayed(expParameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.flight_card_view_text),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(expParameters["flight_from"])))
        onView(allOf<View>(withId(R.id.travel_info_view_text),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(expParameters["info_text_inbound"])))
    }

    @Then("^validate package outbound flight details$")
    fun validateOutboundFlightData(parameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.departure_arrival_time),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(parameters["departure_arrival_time"])))
        onView(allOf<View>(withId(R.id.departure_arrival_airport),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(parameters["departure_arrival_airport"])))
        onView(allOf<View>(withId(R.id.airline_airplane_type),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(containsString(parameters["airline"]))))
        onView(allOf<View>(withId(R.id.airline_airplane_type),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(containsString(parameters["airplane_type"]))))
        onView(allOf<View>(withId(R.id.flight_duration),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(parameters["flight_duration"])))
    }

    @Then("^validate package outbound flight details seating class$")
    fun validateOutboundFlightDataSeatingClass(parameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.flight_seat_class_booking_code),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(parameters["seatingClass"])))
    }

    @Then("^validate package inbound flight details")
    fun validateInboundFlightData(parameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.departure_arrival_time),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(parameters["departure_arrival_time"])))
        onView(allOf<View>(withId(R.id.departure_arrival_airport),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(parameters["departure_arrival_airport"])))
        onView(allOf<View>(withId(R.id.airline_airplane_type),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(containsString(parameters["airline"]))))
        onView(allOf<View>(withId(R.id.airline_airplane_type),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(containsString(parameters["airplane_type"]))))
        onView(allOf<View>(withId(R.id.flight_duration),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check((matches(withText(parameters["flight_duration"]))))
    }

    @Then("^validate package outbound flight total duration")
    fun validateOutboundFlightTotalDuration(parameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.flight_total_duration),
                isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                .check(matches(withText(parameters["flight_duration"])))
    }

    @Then("^validate package inbound flight total duration")
    fun validateInboundFlightTotalDuration(parameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.flight_total_duration),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(parameters["flight_duration"])))
    }

    @Then("^validate bundle total widget")
    fun validateBundleTotalWidget(parameters: Map<String, String>) {
        onView(allOf<View>(withId(R.id.bundle_total_text),
                isDescendantOfA(withId(R.id.total_price_widget))))
                .check(matches(withText(getViewYourBundleTotalText(BuildConfig.brand))))
        onView(allOf<View>(withId(R.id.bundle_total_includes_text),
                isDescendantOfA(withId(R.id.total_price_widget))))
                .check(matches(withText(parameters["additional_text"])))
        onView(allOf<View>(withId(R.id.bundle_total_price),
                isDescendantOfA(withId(R.id.total_price_widget))))
                .check(matches(withText(parameters["bundle_total_value"])))
        if (parameters["savings"] != null) {
            onView(allOf<View>(withId(R.id.bundle_total_savings),
                    isDescendantOfA(withId(R.id.total_price_widget))))
                    .check(matches(withText(parameters["savings"])))
        }
    }

    @Then("^validate package inbound flight widget view")
    fun validateInboundFlightWidgetView(parameters: Map<String, String>) {
        val endDate = getDateInMMMdd(parameters["end_date"])
        onView(allOf<View>(withId(R.id.flight_card_view_text),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(parameters["header"])))
        onView(allOf<View>(withId(R.id.travel_info_view_text),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(withText(endDate + ", " + parameters["travelers"])))
    }

    @Then("^validate package inbound flight icon is unchecked")
    fun validateInboundFlightIcon() {
        onView(allOf<View>(withId(R.id.package_flight_icon), isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(matches(CustomMatchers.withImageDrawable(R.drawable.packages_flight2_icon)))
    }

    @Then("^validate package inbound flight icon is checked")
    fun validateInboundFlightIconChecked() {
        onView(allOf<View>(withId(R.id.package_flight_icon), isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(matches(CustomMatchers.withImageDrawable(R.drawable.packages_flight2_checkmark_icon)))
    }

    @Then("^validate package inbound flight next icon is displayed")
    fun validateInboundFlightNextIcon() {
        onView(allOf<View>(withId(R.id.flight_forward_arrow_icon),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .check(matches(isDisplayed()))
    }

    @Then("^I click on package inbound flight next icon")
    fun clickInboundFlightNextIcon() {
        onView(allOf<View>(withId(R.id.flight_forward_arrow_icon),
                isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                .perform(click())
    }

    @Then("^following information on the bundle overview screen isDisplayed: (true|false)$")
    @Throws(Throwable::class)
    fun verifyDetailsOverviewScreen(isDisplayed: Boolean, info: Map<String, String>) {
        validateDisplayedOnBundleOverview(R.id.step_one_text, info["Step 1 Text"], isDisplayed)

        validateDisplayedOnBundleOverview(R.id.hotels_card_view_text, info["Hotel Bar - Hotel text"],
                isDisplayed)
        validateDateOnBundleOverview(R.id.hotels_dates_guest_info_text, info["Hotel Bar - Date"])
        validateTravelersOnBundleOverview(R.id.package_bundle_hotel_widget, R.id.hotels_dates_guest_info_text,
                info["Hotel Bar - travelers"])
        validateDrawableDisplayedOnBundleOverview(R.id.package_hotel_luggage_icon, info["Hotel Image"])

        validateDisplayedOnBundleOverview(R.id.step_two_text, info["Step 2 Text"],
                isDisplayed)

        validateDisplayedOnBundleOverview(R.id.flight_card_view_text, info["Outbound Flight Bar - Flight Text"],
                isDisplayed)
        validateDateOnBundleOverview(R.id.travel_info_view_text, info["Outbound Flight Bar - date"])
        validateTravelersOnBundleOverview(R.id.package_bundle_outbound_flight_widget, R.id.travel_info_view_text,
                info["Outbound Flight Bar - traveler"])
        validateDrawableDisplayedOnBundleOverview(R.id.package_flight_icon, info["Flight Outbound Image"])

        validateDisplayedOnBundleOverview(R.id.flight_card_view_text, info["Inbound Flight Bar - Flight Text"],
                isDisplayed)
        validateDateOnBundleOverview(R.id.travel_info_view_text, info["Inbound Flight Bar - date"])
        validateTravelersOnBundleOverview(R.id.package_bundle_inbound_flight_widget, R.id.travel_info_view_text,
                info["Inbound Flight Bar - traveler"])
        validateDrawableDisplayedOnBundleOverview(R.id.package_flight_icon, info["Flight Inbound Image"])
    }

    @And("^Wait for checkout button to display")
    fun waitForCheckoutButtonToDisplay() {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.checkout_button), 30, TimeUnit.SECONDS)
    }

    private fun validateDrawableDisplayedOnBundleOverview(resId: Int, value: String?) {
        if (value!!.contains("Hotel")) {
            onView(allOf<View>(withId(resId), isDescendantOfA(withId(R.id.package_bundle_hotel_widget))))
                    .check(matches(isDisplayed()))
        } else if (value.contains("Flight Outbound")) {
            onView(allOf<View>(withId(resId), isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget))))
                    .check(matches(isDisplayed()))
        } else if (value.contains("Flight Inbound")) {
            onView(allOf<View>(withId(resId), isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget))))
                    .check(matches(isDisplayed()))
        }
    }

    private fun validateDateOnBundleOverview(resId: Int, value: String?) {
        if (value!!.contains("-")) {
            val dateString = TestUtil.getDateRangeInMMMdd(value)
            onView(withId(resId)).check(matches(withText(containsString(dateString))))
        } else {
            onView(allOf(withId(resId), withText(containsString(TestUtil.getDateInMMMdd(value)))))
                    .check(matches(isDisplayed()))
        }
    }

    private fun validateTravelersOnBundleOverview(parentId: Int, resId: Int, value: String?) {
        onView(allOf(withId(resId),
                isDescendantOfA(withId(parentId))))
                .check(matches(withText(containsString(value))))
    }

    @Throws(Throwable::class)
    private fun validateDisplayedOnBundleOverview(resId: Int, value: String?, shouldBeDisplayed: Boolean) {
        onView(allOf(withId(resId), withText(containsString(value))))
                .check(matches(if (shouldBeDisplayed) isDisplayed() else not(isDisplayed())))
    }

    @And("^\"(.*?)\" on bundle overview isDisabled: (true|false)$")
    fun verifyDiabledViewsOnOverviewScreen(barName: String, isDisabled: Boolean) {
        if (barName.contains("Outbound")) {
            PackageScreen.outboundFlightInfo()
                    .check(matches(if (isDisabled) not<View>(isEnabled()) else isEnabled()))
        } else if (barName.contains("Inbound")) {
            PackageScreen.inboundFlightInfo()
                    .check(matches(if (isDisabled) not<View>(isEnabled()) else isEnabled()))
        }
    }

    @When("^I tap on hotels bar on bundle overview screen$")
    @Throws(Throwable::class)
    fun launchHotelsFromBO() {
        PackageScreen.hotelBundleContainer().perform(click())
    }

    @Then("^Bundle Overview screen is displayed$")
    @Throws(Throwable::class)
    fun bundleOverviewDisplayed() {
        PackageScreen.hotelBundleContainer().check(matches(isDisplayed()))
    }

    @And("^Validate that collapsed outbound flight details are present$")
    @Throws(Throwable::class)
    fun flightCollapsedOutboundView() {
        onView(allOf(withParent(withId(R.id.bundle_widget)),
                withId(R.id.package_bundle_outbound_flight_widget))).check(matches(isDisplayed()))
    }

    @And("^Validate that collapsed inbound flight details are present$")
    @Throws(Throwable::class)
    fun flightCollapsedInboundView() {
        onView(allOf(withParent(withId(R.id.bundle_widget)),
                withId(R.id.package_bundle_inbound_flight_widget))).check(matches(isDisplayed()))
    }

    @And("^I expand the hotel card view$")
    @Throws(Throwable::class)
    fun expandHotelCardView() {
        onView(
                allOf(withParent(withId(R.id.bundle_widget)), withId(R.id.package_bundle_hotel_widget)))
                .perform(click())
    }

    @And("^I collapse the hotel card view")
    @Throws(Throwable::class)
    fun collapseHotelCardView() {
        onView(allOf(withParent(withId(R.id.bundle_widget)), withId(R.id.package_bundle_hotel_widget)))
                .perform(click())
    }

    @And("^I expand the flight outbound card view$")
    @Throws(Throwable::class)
    fun expandFlightOutboundCardView() {
        onView(allOf(withParent(withId(R.id.package_bundle_outbound_flight_widget)), withId(R.id.package_flight_details_icon)))
                .perform(click())
    }

    @And("^I collapse the flight outbound card view$")
    @Throws(Throwable::class)
    fun collapseFlightOutboundCardView() {
        onView(allOf(withParent(withId(R.id.bundle_widget)), withId(R.id.package_bundle_outbound_flight_widget)))
                .perform(click())
    }

    @And("^I expand the flight inbound card view$")
    @Throws(Throwable::class)
    fun expandFlightInboundCardView() {
        onView(allOf(withParent(withId(R.id.bundle_widget)), withId(R.id.package_bundle_inbound_flight_widget)))
                .perform(click())
    }

    @And("^Validate hotel widget of overview screen with following details$")
    @Throws(Throwable::class)
    fun validateInformationOnOverviewScreenForHotels(parameters: Map<String, String>) {
        validateHotelOverviewWidget(R.id.hotels_card_view_text, parameters["hotel name"])
        validateHotelOverviewWidget(R.id.hotels_dates_guest_info_text, parameters["travel date and traveler"])
        validateHotelOverviewWidget(R.id.hotel_room_info, parameters["hotel room info"])
        validateHotelOverviewWidget(R.id.hotel_room_type, parameters["hotel room type"])
        validateHotelOverviewWidget(R.id.hotel_address, parameters["hotel address"])
        validateHotelOverviewWidget(R.id.hotel_city, parameters["hotel city"])
        validateHotelOverviewWidget(R.id.hotel_free_cancellation, parameters["hotel free cancellation"])
        validateHotelOverviewWidget(R.id.hotel_promo_text, parameters["hotel promo text"])
    }

    @Throws(Throwable::class)
    private fun validateHotelOverviewWidget(resId: Int, value: String?) {
        onView(AllOf.allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)),
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE), withId(resId)))
                .check(matches(withText(CoreMatchers.containsString(value))))
    }

    @Then("^validate total savings is \"([^\"]*)\"$")
    @Throws(Throwable::class)
    fun validateTotalSavings(savings: String) {
        onView(withId(R.id.bundle_total_savings)).check(matches(withText(savings)))
    }

    @Then("^validate following detailed information is present on bundle overview cost summary screen$")
    @Throws(Throwable::class)
    fun validateCostSummaryPopup(params: Map<String, String>) {
        validateCostSummaryPriceDetails("Hotel + Flights", params.get("Hotel + Flights"))
        validateCostSummaryPriceDetails("room", params.get("room"))
        validateCostSummaryPriceDetails("Taxes", params.get("Taxes"))
        validateCostSummaryPriceDetails(getViewYourBundleDiscountText(BuildConfig.brand), params.get("Bundle Discount"))
        validateCostSummaryPriceDetails(getViewYourBundleTotalText(BuildConfig.brand), params.get("Bundle total"))
    }

    @Throws(Throwable::class)
    private fun validateSummaryPopup(guestDetails: String, taxDetails: String) {

        onView(AllOf.allOf(withId(R.id.price_type_text_view), withText(containsString(guestDetails)))).check(matches(withText(containsString(taxDetails))))
    }

    @Throws(Throwable::class)
    private fun validateCostSummaryPriceDetails(priceType: String, priceAmt: String?) {
        onView(AllOf.allOf(isDescendantOfA(withId(R.id.breakdown_container)), withId(R.id.price_type_text_view),
                withText(containsString(priceType)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(AllOf.allOf(withId(R.id.price_type_text_view),
                        withText(containsString(priceType)), hasSibling(withText(priceAmt)), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )))
    }

    @And("^Validate that close icon is present on top left$")
    @Throws(Throwable::class)
    fun validateCloseIcon() {
        onView(allOf(withParent(withId(R.id.checkout_toolbar)), withContentDescription("Close")))
                .check(matches(isDisplayed()))
    }

    @And("^Validate that back icon is present on top left$")
    @Throws(Throwable::class)
    fun validateBackIcon() {
        onView(allOf(withParent(withId(R.id.checkout_toolbar)), withContentDescription("Back")))
                .check(matches(isDisplayed()))
    }

    @And("^I click on start over button to modify search$")
    @Throws(Throwable::class)
    fun clickOnStartOverButton() {
        onView((withId(android.R.id.button1))).perform(click())
    }

    @And("^Validate that edit icon is present on top right$")
    @Throws(Throwable::class)
    fun validateEditIcon() {
        onView(allOf(withParent(withParent(withId(R.id.checkout_toolbar))),
                withContentDescription("More options")))
                .check(matches(isDisplayed()))
    }

    fun getViewYourBundleText(brand: String): String {
        return when (brand) {
            "Orbitz", "CheapTickets", "Travelocity" -> "View your package"
            "ebookers" -> "View your Flight + Hotel"
            else -> {
                "View your bundle"
            }
        }
    }

    fun getViewYourBundleDiscountText(brand: String): String {
        return when (brand) {
            "Orbitz", "CheapTickets", "Travelocity" -> "Package Discount"
            "ebookers" -> "Flight + Hotel Discount"
            else -> {
                "Bundle Discount"
            }
        }
    }

    fun getViewYourBundleTotalText(brand: String): String {
        return when (brand) {
            "Orbitz", "CheapTickets", "Travelocity" -> "Package total"
            "ebookers" -> "Flight + Hotel total"
            else -> {
                "Bundle total"
            }
        }
    }
}
