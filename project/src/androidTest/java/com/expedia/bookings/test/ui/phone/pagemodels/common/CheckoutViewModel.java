package com.expedia.bookings.test.ui.phone.pagemodels.common;

import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CheckoutViewModel {
	// Checkout

	public static ViewInteraction driverInfo() {
		return onView(withId(R.id.main_contact_info_card_view));
	}

	public static void clickDriverInfo() {
		driverInfo().perform(scrollTo(), click());
	}

	public static void clickPaymentInfo() {
		onView(withId(R.id.payment_info_card_view)).perform(scrollTo(), click());
	}

	public static ViewInteraction userName() {
		return onView(withId(R.id.username_edit_text));
	}

	public static void enterUsername(String name) {
		userName().perform(scrollTo(), typeText(name));
	}

	public static ViewInteraction password() {
		return onView(withId(R.id.password_edit_text));
	}

	public static void enterPassword(String name) {
		password().perform(scrollTo(), typeText(name));
	}

	public static ViewInteraction firstName() {
		return onView(withId(R.id.edit_first_name));
	}

	public static void enterFirstName(String name) {
		firstName().perform(scrollTo(), typeText(name));
	}

	public static ViewInteraction lastName() {
		return onView(withId(R.id.edit_last_name));
	}

	public static void enterLastName(String name) {
		lastName().perform(scrollTo(), typeText(name));
	}

	public static ViewInteraction email() {
		return onView(withId(R.id.edit_email_address));
	}

	public static void enterEmail(String email) {
		email().perform(scrollTo(), typeText(email));
	}

	public static ViewInteraction phone() {
		return onView(withId(R.id.edit_phone_number));
	}

	public static void enterPhoneNumber(String number) {
		phone().perform(scrollTo(), typeText(number));
	}

	public static void pressClose() {
		onView(withId(R.id.checkout_toolbar)).perform(ViewActions.getChildViewButton(0));
	}

	public static void clickStoredTravelerButton() {
		onView(withId(R.id.select_traveler_button)).perform(click());
	}

	public static void selectStoredTraveler(Instrumentation instrumentation, String travelername) throws Throwable {
		onView(withText(travelername))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static void clickStoredCardButton() {
		onView(withId(R.id.select_payment_button)).perform(click());
	}

	public static void selectStoredCard(Instrumentation instrumentation, String cardname) throws Throwable {
		onView(withText(cardname))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static ViewInteraction performSlideToPurchase() {
		return onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.swipeRight());
	}

	public static void pressDoLogin() {
		onView(withId(R.id.log_in_btn)).perform(click());
	}

	public static void clickLogin() {
		onView(withId(R.id.login_widget)).perform(click());
	}

	public static void clickDone() {
		onView(withId(R.id.menu_done)).perform(click());
	}

	public static void enterLoginDetails() {
		clickLogin();
		enterUsername("username");
		enterPassword("password");
	}

	public static void enterTravelerInfo() {
		ScreenActions.delay(1);
		clickDriverInfo();
		ScreenActions.delay(1);
		enterFirstName("FiveStar");
		enterLastName("Bear");
		Common.closeSoftKeyboard(CheckoutViewModel.lastName());
		ScreenActions.delay(1);
		enterEmail("noah@mobiata.com");
		Common.closeSoftKeyboard(CheckoutViewModel.email());
		ScreenActions.delay(1);
		enterPhoneNumber("4158675309");
		clickDone();
	}

	public static void enterPaymentInfo() {
		ScreenActions.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		ScreenActions.delay(1);
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		//Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("666");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
	}
}
