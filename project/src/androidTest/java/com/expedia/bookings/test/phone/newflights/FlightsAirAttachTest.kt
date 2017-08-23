package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.hamcrest.Matchers
import org.joda.time.DateTime
import org.joda.time.DateTimeFieldType
import org.joda.time.LocalDate
import org.junit.Test

class FlightsAirAttachTest : NewFlightTestCase() {

    @Test
    fun testAirAttachRedirectsSingleTraveler() {
        val startDate = LocalDate.now().plusDays(5)
        val endDate = LocalDate.now().plusDays(10)
        selectFlightsProceedToCheckoutSingleGuest(startDate, endDate)
        PackageScreen.travelerInfo().perform(ViewActions.click())
        enterPrimaryTravelerInfo()
        enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()
        assertAirAttachIsShown()

        Espresso.onView(ViewMatchers.withId(R.id.hotel_cross_sell_body)).perform(ViewActions.click())
        assertRedirectToHotelActivity()
        assertValidToolbarInfoForSingleTraveler(startDate, endDate)
    }

    @Test
    fun testAirAttachRedirectsWithChildren() {
        val startDate = LocalDate.now().plusDays(10)
        val endDate = LocalDate.now().plusDays(15)
        selectFlightsProceedToCheckoutWithChildren(startDate, endDate)

        PackageScreen.travelerInfo().perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.main_traveler_container)).perform(ViewActions.click())
        enterPrimaryTravelerInfo()
        enterNonPrimaryTravelerInfo()
        enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()

        assertAirAttachIsShown()

        Espresso.onView(ViewMatchers.withId(R.id.hotel_cross_sell_body)).perform(ViewActions.click())
        assertRedirectToHotelActivity()
        assertValidToolbarInfoForTravelerWithChildren(startDate, endDate)
    }

    private fun assertRedirectToHotelActivity() {

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.stub_map),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.widget_hotel_results)),
                ViewMatchers.isDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.sort_filter_button_container),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.widget_hotel_results)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.hotel_name_text_view),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.widget_hotel_results)),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.withText("happypath")))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.sort_filter_button_container),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.widget_hotel_results)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.hotel_results_toolbar),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.widget_hotel_results)),
                ViewMatchers.isCompletelyDisplayed()))
    }

    private fun assertValidToolbarInfoForSingleTraveler(testCheckinDate: LocalDate, testCheckoutDate: LocalDate) {
        val testHotelCheckinDate = LocaleBasedDateFormatUtils.localDateToMMMd(testCheckinDate)
        val testHotelCheckoutDate = LocaleBasedDateFormatUtils.localDateToMMMd(testCheckoutDate)

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.title),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_results_toolbar)),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.withText("Detroit, MI")))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.menu_open_search),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_results_toolbar)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.subtitle),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_results_toolbar)),
                ViewMatchers.withText("${testHotelCheckinDate} - ${testHotelCheckoutDate}, 1 Guest"),
                ViewMatchers.isDisplayed()))
    }

    private fun assertValidToolbarInfoForTravelerWithChildren(testCheckinDate: LocalDate, testCheckoutDate: LocalDate) {
        val testHotelCheckinDate = LocaleBasedDateFormatUtils.localDateToMMMd(testCheckinDate)
        val testHotelCheckoutDate = LocaleBasedDateFormatUtils.localDateToMMMd(testCheckoutDate)

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.title),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_results_toolbar)),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.withText("DTW")))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.menu_open_search),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_results_toolbar)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.subtitle),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_results_toolbar)),
                ViewMatchers.withText("${testHotelCheckinDate} - ${testHotelCheckoutDate}, 3 Guests"),
                ViewMatchers.isDisplayed()))
    }

    private fun assertAirAttachIsShown() {
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.hotel_cross_sell_widget),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.confirmation_container)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.air_attach_countdown_view),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_cross_sell_widget)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.air_attach_expires_today_text_view),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.hotel_cross_sell_widget)),
                ViewMatchers.isCompletelyDisplayed()))
    }


    private fun selectFlightsProceedToCheckoutSingleGuest(startDate: LocalDate, endDate: LocalDate) {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.selectFlightOriginAndDestination()
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())

        FlightTestHelpers.assertFlightOutbound()
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())

        PackageScreen.checkout().perform(ViewActions.click())
    }

    private fun selectFlightsProceedToCheckoutWithChildren(startDate: LocalDate, endDate: LocalDate) {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.selectFlightOriginAndDestination()
        SearchScreen.selectDates(startDate, endDate)

        SearchScreen.selectTravelerText().perform(ViewActions.click())
        SearchScreen.incrementChildrenButton()
        SearchScreen.incrementChildrenButton()
        SearchScreen.searchAlertDialogDone().perform(ViewActions.click())

        SearchScreen.searchButton().perform(ViewActions.click())

        FlightTestHelpers.assertFlightOutbound()
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())

        PackageScreen.checkout().perform(ViewActions.click())
    }

    private fun enterPrimaryTravelerInfo() {
        TravelerDetails.enterFirstName("Fake")
        TravelerDetails.enterLastName("Traveler")
        TravelerDetails.enterPhoneNumber("4155554321")
        Espresso.closeSoftKeyboard()
        TravelerDetails.enterEmail("test@gmail.com")
        Espresso.closeSoftKeyboard()
        TravelerDetails.selectBirthDate(1989, 6, 9)
        TravelerDetails.selectGender("Male")
        TravelerDetails.clickAdvanced()
        TravelerDetails.enterRedressNumber("1234567")
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
    }

    private fun enterNonPrimaryTravelerInfo() {
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.primary_details_text),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.additional_traveler_container)),
                ViewMatchers.withText("Edit Traveler 2 (10 year old)"))).perform(ViewActions.click())

        TravelerDetails.enterFirstName("First")
        TravelerDetails.enterLastName("Child")
        TravelerDetails.selectBirthDate(DateTime.now().minusYears(10).get(DateTimeFieldType.year()), 6, 9)
        TravelerDetails.selectGender("Male")

        TravelerDetails.clickDone()

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.primary_details_text),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.additional_traveler_container)),
                ViewMatchers.withText("Edit Traveler 3 (10 year old)"))).perform(ViewActions.click())

        TravelerDetails.enterFirstName("Second")
        TravelerDetails.enterLastName("Child")
        TravelerDetails.selectBirthDate(DateTime.now().minusYears(10).get(DateTimeFieldType.year()), 6, 9)
        TravelerDetails.selectGender("Female")

        TravelerDetails.clickDone()
    }

    private fun enterPaymentInfo() {
        PackageScreen.clickPaymentInfo()
        PackageScreen.enterCreditCard()
        PackageScreen.completePaymentForm()
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
    }
}