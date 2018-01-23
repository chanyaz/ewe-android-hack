package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Test

class FlightCheckoutToolbarTest : NewFlightTestCase() {

    @Test
    fun testToolbarMenuButtonsDuringIncompleteCheckoutUsingImageButton() {
        selectFlightsProceedToCheckout()
        assertToolbarMenuButtonNotVisible()

        PackageScreen.travelerInfo().perform(ViewActions.click())
        assertToolbarMenuButtonSaysNext()

        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
        assertToolbarMenuButtonNotVisible()

        PackageScreen.clickPaymentInfo()
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
        assertToolbarMenuButtonNotVisible()
    }

    @Test
    fun testToolbarMenuButtonsDuringIncompleteCheckoutUsingBackButton() {
        selectFlightsProceedToCheckout()
        PackageScreen.travelerInfo().perform(ViewActions.click())

        assertToolbarMenuButtonSaysNext()

        Espresso.closeSoftKeyboard()
        Common.pressBack()

        assertToolbarMenuButtonNotVisible()

        PackageScreen.clickPaymentInfo()
        waitForPaymentInfoCardView()
        assertToolbarMenuButtonSaysDone()

        Espresso.closeSoftKeyboard()
        Common.pressBack()

        assertToolbarMenuButtonNotVisible()
    }
// TODO: uncomment out these 2 tests once bug causing these to fail is fixed
//
//    @Test
//    fun testToolbarMenuButtonsDuringCheckoutUsingDoneButton() {
//        selectFlightsProceedToCheckout()
//
//        PackageScreen.travelerInfo().perform(ViewActions.click())
//
//        enterTravelerInfo()
//        assertToolbarMenuButtonSaysDone()
//
//        PackageScreen.clickTravelerDone()
//        assertToolbarMenuButtonNotVisible()
//
//        PackageScreen.clickPaymentInfo()
//        waitForPaymentInfoCardView()
//
//        assertToolbarMenuButtonSaysDone()
//        enterPaymentInfo()
//
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.clickPaymentDone()
//
//        assertToolbarMenuButtonNotVisible()
//
//        PackageScreen.travelerInfo().perform(ViewActions.scrollTo(), ViewActions.click())
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.clickTravelerDone()
//
//        assertToolbarMenuButtonNotVisible()
//
//        PackageScreen.clickPaymentInfo()
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.clickPaymentDone()
//
//        assertToolbarMenuButtonNotVisible()
//    }
//
//    @Test
//    fun testToolbarMenuButtonsDuringCheckoutUsingImageButton() {
//        selectFlightsProceedToCheckout()
//
//        PackageScreen.travelerInfo().perform(ViewActions.scrollTo(), ViewActions.click())
//        enterTravelerInfo()
//
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
//
//        assertToolbarMenuButtonNotVisible()
//
//        PackageScreen.clickPaymentInfo()
//        waitForPaymentInfoCardView()
//        assertToolbarMenuButtonSaysDone()
//
//        enterPaymentInfo()
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
//
//        assertToolbarMenuButtonNotVisible()
//
//        PackageScreen.travelerInfo().perform(ViewActions.scrollTo(), ViewActions.click())
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
//
//        assertToolbarMenuButtonNotVisible()
//
//        PackageScreen.clickPaymentInfo()
//        waitForPaymentInfoCardView()
//        assertToolbarMenuButtonSaysDone()
//        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
//
//        assertToolbarMenuButtonNotVisible()
//
//    }

    @Test
    fun testToolbarMenuButtonsDuringCheckoutUsingBackButton() {
        selectFlightsProceedToCheckout()

        PackageScreen.travelerInfo().perform(ViewActions.scrollTo(), ViewActions.click())
        enterTravelerInfo()

        assertToolbarMenuButtonSaysDone()
        Espresso.closeSoftKeyboard()
        Common.pressBack()

        assertToolbarMenuButtonNotVisible()

        PackageScreen.clickPaymentInfo()
        waitForPaymentInfoCardView()
        assertToolbarMenuButtonSaysDone()

        enterPaymentInfo()
        assertToolbarMenuButtonSaysDone()

        Espresso.closeSoftKeyboard()
        Common.pressBack()

        assertToolbarMenuButtonNotVisible()

        PackageScreen.travelerInfo().perform(ViewActions.scrollTo(), ViewActions.click())

        assertToolbarMenuButtonSaysDone()

        Espresso.closeSoftKeyboard()
        Common.pressBack()

        assertToolbarMenuButtonNotVisible()

        PackageScreen.clickPaymentInfo()
        waitForPaymentInfoCardView()

        assertToolbarMenuButtonSaysDone()

        Espresso.closeSoftKeyboard()
        Common.pressBack()

        assertToolbarMenuButtonNotVisible()
    }

    private fun selectFlightsProceedToCheckout() {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.selectFlightOriginAndDestination()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
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

    private fun enterTravelerInfo() {
        TravelerDetails.enterFirstName("Eidur")
        TravelerDetails.enterLastName("Gudjohnsen")
        TravelerDetails.enterPhoneNumber("4155554321")
        Espresso.closeSoftKeyboard()
        TravelerDetails.enterEmail("test@gmail.com")
        Espresso.closeSoftKeyboard()
        TravelerDetails.selectBirthDate(1989, 6, 9)
        TravelerDetails.materialSelectGender("Male")
        TravelerDetails.clickAdvanced()
        TravelerDetails.enterRedressNumber("1234567")
    }

    private fun enterPaymentInfo() {
        PackageScreen.enterCreditCard()
        PackageScreen.completePaymentForm()
    }

    private fun assertToolbarMenuButtonNotVisible() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.menu_done),
                ViewMatchers.withParent(ViewMatchers.withId(R.id.checkout_toolbar)),
                Matchers.not(ViewMatchers.isCompletelyDisplayed()),
                Matchers.not(ViewMatchers.isClickable()),
                Matchers.not(ViewMatchers.withText("Next")),
                Matchers.not(ViewMatchers.withText("Done"))))
    }
    private fun assertToolbarMenuButtonSaysNext() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.menu_done),
                ViewMatchers.withParent(ViewMatchers.withId(R.id.checkout_toolbar)),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.isClickable(),
                ViewMatchers.withText("Next")))
    }

    private fun waitForPaymentInfoCardView() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.payment_info_card_view), ViewMatchers.isCompletelyDisplayed()))
    }

    private fun assertToolbarMenuButtonSaysDone() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.menu_done),
                ViewMatchers.withParent(ViewMatchers.withId(R.id.checkout_toolbar)),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.isClickable(),
                ViewMatchers.withText("Done")))
    }
}
