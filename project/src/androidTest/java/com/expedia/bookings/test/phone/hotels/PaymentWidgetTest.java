package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.TripsScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;

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

	private void goToCheckout(String hotel) throws Throwable {
		NewLaunchScreen.tripsButton().perform(click());
		TripsScreen.clickOnLogInButton();
		HotelScreen.signIn("singlecard@mobiata.com");
		NewLaunchScreen.shopButton().perform(click());
		NewLaunchScreen.hotelsLaunchButton().perform(click());
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel(hotel);
		HotelScreen.selectFirstRoom();
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
