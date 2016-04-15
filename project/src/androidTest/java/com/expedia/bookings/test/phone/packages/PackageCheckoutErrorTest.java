package com.expedia.bookings.test.phone.packages;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageCheckoutErrorTest extends PackageTestCase {

	public void testSearchError() throws Throwable {
		PackageScreen.searchPackageFor(1, 0);

		PackageScreen.clickHotelBundle();
		HotelScreen.selectHotel("Package Happy Path");
		HotelScreen.selectRoom();

		PackageScreen.outboundFlight().perform(click());

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.inboundFLight().perform(click());

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.checkout().perform(click());

		PackageScreen.enterTravelerInfo();
		CheckoutViewModel.clickPaymentInfo();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextCvv("666");
		CardInfoScreen.typeTextNameOnCardEditText("errorcheckoutcard");
		CardInfoScreen.typeTextEmailEditText("test@email.com");

		BillingAddressScreen.typeTextAddressLineOne("123 California Street");
		BillingAddressScreen.typeTextCity("San Francisco");
		BillingAddressScreen.typeTextState("CA");
		BillingAddressScreen.typeTextPostalCode("94105");
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();

		PackageScreen.assertErrorScreen("Edit Payment", "We're sorry, but we were unable to process your payment. Please verify that you entered your information correctly.");
		onView(withId(R.id.error_action_button)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.nameOnCardEditText().perform(clearText());
		CardInfoScreen.typeTextNameOnCardEditText("errorcheckoutunknown");

		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();

		PackageScreen.assertErrorScreen("Retry", "Sorry, we could not connect to Expedia's servers. Please try again later.");
		onView(withId(R.id.error_action_button)).perform(click());

		onView(withId(R.id.card_info_container)).perform(click());

		CardInfoScreen.nameOnCardEditText().perform(clearText());
		CardInfoScreen.typeTextNameOnCardEditText("abd def");

		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();

		onView(allOf(withId(R.id.itin_number), withText("#1126420960431 sent to test@email.com"))).check(matches(isDisplayed()));
	}

}
