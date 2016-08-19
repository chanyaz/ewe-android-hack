package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.FailureHandler
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.mobiata.android.Log
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test

class FlightCheckoutOverviewAcceptTermsTest : NewFlightTestCase() {

    @Test
    fun testAcceptTermsPromptStaysHiddenDuringCheckoutFlow() {
        Common.setPOS(PointOfSaleId.FRANCE)
        selectFlightsProceedToCheckout()

        assertAcceptTermsWidgetIsNotInflated()

        enterTravelerInfo()
        assertAcceptTermsWidgetIsInflatedButGone()

        enterPaymentInfo()
        assertAcceptTermsWidgetIsShown()
    }

    @Test
    fun testCanConfirmPaymentAfterAccepting() {
        Common.setPOS(PointOfSaleId.GERMANY)
        selectFlightsProceedToCheckout()
        enterTravelerInfo()
        enterPaymentInfo()

        Espresso.onView(ViewMatchers.withId(R.id.i_accept_terms_button)).perform(ViewActions.click())
        CheckoutViewModel.performSlideToPurchase()

        assertConfirmationViewIsShown()
    }

    @Test
    fun testAcceptTermsStaysHiddenAfterAccepting() {
        Common.setPOS(PointOfSaleId.SWEDEN)
        selectFlightsProceedToCheckout()
        enterTravelerInfo()
        enterPaymentInfo()

        Espresso.onView(ViewMatchers.withId(R.id.i_accept_terms_button)).perform(ViewActions.click())

        assertSlideToPurchaseWidgetIsVisible()
        assertAcceptTermsWidgetIsInflatedButGone()

        PackageScreen.clickPaymentInfo()
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        assertSlideToPurchaseWidgetIsVisible()
        assertAcceptTermsWidgetIsInflatedButGone()

        PackageScreen.travelerInfo().perform(ViewActions.click())
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

        assertSlideToPurchaseWidgetIsVisible()
        assertAcceptTermsWidgetIsInflatedButGone()

        Common.pressBack()
        PackageScreen.checkout().perform(ViewActions.click())

        assertSlideToPurchaseWidgetIsVisible()
        assertAcceptTermsWidgetIsInflatedButGone()
    }

    @Test
    fun testAcceptTermsPromptNotVisible() {
        Common.setPOS(PointOfSaleId.UNITED_STATES)
        selectFlightsProceedToCheckout()

        assertSlideToPurchaseWidgetIsVisible()
        assertAcceptTermsWidgetIsNotInflated()
    }

    private fun assertAcceptTermsWidgetIsShown() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.accept_terms_widget),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.i_accept_terms_button),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.isClickable(),
                ViewMatchers.withText("I Accept")))

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.do_you_accept_label),
                ViewMatchers.isCompletelyDisplayed(),
                ViewMatchers.withText("Do you accept all of the conditions outlined above?")))
    }

    private fun assertSlideToPurchaseWidgetIsVisible() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.slide_to_purchase_layout),
                ViewMatchers.isCompletelyDisplayed()))
    }

    private fun assertAcceptTermsWidgetIsInflatedButGone() {

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.accept_terms_widget)))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.i_accept_terms_button)))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.do_you_accept_label)))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun assertAcceptTermsWidgetIsNotInflated() {
        var viewIsNotInflated = false
        Espresso.onView(ViewMatchers.withId(R.id.accept_terms_widget)).check { view, noMatchingViewException ->
            if (noMatchingViewException != null) {
                viewIsNotInflated = true
            }
        }
        junit.framework.Assert.assertTrue(viewIsNotInflated)
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
        PackageScreen.travelerInfo().perform(ViewActions.click())
        PackageScreen.enterFirstName("Eidur")
        PackageScreen.enterLastName("Gudjohnsen")
        PackageScreen.enterPhoneNumber("4155554321")
        Espresso.closeSoftKeyboard()
        PackageScreen.enterEmail("test@gmail.com")
        Espresso.closeSoftKeyboard()
        PackageScreen.selectBirthDate(1989, 6, 9)
        PackageScreen.selectGender("Male")
        PackageScreen.clickTravelerAdvanced()
        PackageScreen.enterRedressNumber("1234567")
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())

    }

    private fun enterPaymentInfo() {
        PackageScreen.clickPaymentInfo()
        PackageScreen.enterCreditCard()
        PackageScreen.completePaymentForm()
        PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(ViewActions.click())
    }


    private fun assertConfirmationViewIsShown() {
        Espresso.onView(ViewMatchers.withId(R.id.confirmation_container)).perform(com.expedia.bookings.test.espresso
                .ViewActions.waitForViewToDisplay())
        Espresso.onView(Matchers.allOf(ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.destination),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.confirmation_container)),
                ViewMatchers.withText("Detroit")))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withText(R.id.expedia_points),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.confirmation_container)),
                ViewMatchers.isCompletelyDisplayed()))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.first_row),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.outbound_flight_card)),
                ViewMatchers.withText("Flight to (DTW) Detroit")))

        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(R.id.first_row),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.inbound_flight_card)),
                ViewMatchers.withText("Flight to (SFO) San Francisco")))

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
}