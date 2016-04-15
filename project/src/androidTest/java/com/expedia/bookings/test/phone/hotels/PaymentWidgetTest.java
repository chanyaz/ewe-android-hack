package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.TripsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

public class PaymentWidgetTest extends PhoneTestCase {

	public void testPaymentWidgetFlow() throws Throwable {
		goToCheckout();
		assertSavedCardSelected();
		assertTempCardRemoved();
	}

	private void assertSavedCardSelected()  throws Throwable {
		onView(withId(R.id.card_info_name)).perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Visa 1111");
	}

	private void assertTempCardRemoved() throws Throwable {
		enterPaymentDetails();
		CheckoutViewModel.dialogOkayButton().perform(ViewActions.waitForViewToDisplay());
		CheckoutViewModel.dialogOkayButton().perform(click());
		Common.pressBack();
		enterPaymentDetails();
		CheckoutViewModel.dialogCancelButton().perform(ViewActions.waitForViewToDisplay());
		CheckoutViewModel.dialogCancelButton().perform(click());
		onView(withId(R.id.filled_in_card_details_mini_view)).perform(ViewActions.waitForViewToDisplay());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.filled_in_card_details_mini_view, "Visa …1111");
		CheckoutViewModel.selectStoredCard("Saved Visa …1111");
		Common.pressBack();
		CheckoutViewModel.clickPaymentInfo();
		onView(withId(R.id.filled_in_card_details_mini_container)).check(matches(not(isDisplayed())));
	}

	private void goToCheckout() throws Throwable {
		LaunchScreen.tripsButton().perform(click());
		TripsScreen.clickOnLogInButton();
		HotelScreen.signIn("singlecard@mobiata.com");
		LaunchScreen.shopButton().perform(click());
		LaunchScreen.launchHotels();
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("happypath");
		HotelScreen.selectRoom();
	}

	private void enterPaymentDetails() {
		CheckoutViewModel.paymentInfo().perform(ViewActions.waitForViewToDisplay());
		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.addCreditCard().perform(ViewActions.waitForViewToDisplay());
		CheckoutViewModel.clickAddCreditCard();
		CardInfoScreen.creditCardNumberEditText().perform(ViewActions.waitForViewToDisplay());
		CheckoutViewModel.enterPaymentDetails();
		CheckoutViewModel.clickDone();
	}
}
