package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.trips.TripsScreen;
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

public class PaymentWidgetTest extends PhoneTestCase {

	@Test
	public void testHappyPaymentWidgetFlow() throws Throwable {
		goToCheckout("happypath");
		assertSavedCardSelected();
		assertTempCardRemoved();
	}

	private void assertSavedCardNotSelected() {
		onView(withId(R.id.card_info_container)).perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_expiration, R.string.checkout_hotelsv2_enter_payment_details_line2);
	}

	private void assertSavedCardSelected()  throws Throwable {
		onView(withId(R.id.card_info_name)).perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Visa 1111");
	}

	private void assertTempCardRemoved() throws Throwable {
		enterPaymentDetails();
		CheckoutScreen.dialogOkayButton().perform(ViewActions.waitForViewToDisplay());
		CheckoutScreen.dialogOkayButton().perform(click());
		Common.pressBack();
		enterPaymentDetails();
		CheckoutScreen.dialogCancelButton().perform(ViewActions.waitForViewToDisplay());
		CheckoutScreen.dialogCancelButton().perform(click());
		onView(withId(R.id.filled_in_card_details_mini_view)).perform(ViewActions.waitForViewToDisplay());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.filled_in_card_details_mini_view, "Visa …1111");
		CheckoutScreen.selectStoredCard("Saved Visa …1111");
		Common.pressBack();
		CheckoutScreen.clickPaymentInfo();
		onView(withId(R.id.filled_in_card_details_mini_container)).check(matches(not(isDisplayed())));
	}

	private void goToCheckout(String hotel) throws Throwable {
		HomeScreenSteps.switchToTab("Trips");
		TripsScreen.clickOnLogInButton();
		LogInScreen.signIn("singlecard@mobiata.com", "password");
		HomeScreenSteps.switchToTab("Shop Travel");
		LaunchScreen.hotelsLaunchButton().perform(click());
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel(hotel);
		HotelInfoSiteScreen.bookFirstRoom();
	}

	private void enterPaymentDetails() {
		CheckoutScreen.paymentInfo().perform(ViewActions.waitForViewToDisplay());
		CheckoutScreen.clickPaymentInfo();
		CheckoutScreen.addCreditCard().perform(ViewActions.waitForViewToDisplay());
		CheckoutScreen.clickAddCreditCard();
		CardInfoScreen.creditCardNumberEditText().perform(ViewActions.waitForViewToDisplay());
		CheckoutScreen.enterPaymentDetails();
		CheckoutScreen.clickDone();
	}
}
