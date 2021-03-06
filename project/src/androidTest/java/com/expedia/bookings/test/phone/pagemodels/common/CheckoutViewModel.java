package com.expedia.bookings.test.phone.pagemodels.common;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.CustomMatchers;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CheckoutViewModel {
	// Checkout

	public static ViewInteraction driverInfo() {
		return onView(withId(R.id.main_contact_info_card_view));
	}

	public static ViewInteraction paymentInfo() {
		return onView(withId(R.id.payment_info_card_view));
	}

	public static ViewInteraction addCreditCard() {
		return onView(withId(R.id.payment_option_credit_debit));
	}

	public static void clickDriverInfo() {
		driverInfo().perform(scrollTo(), click());
	}

	public static void clickPaymentInfo() {
		paymentInfo().perform(scrollTo(), click());
	}

	public static void clickAddCreditCard() {
		addCreditCard().perform(scrollTo(), click());
	}

	public static ViewInteraction userName() {
		return onView(allOf(withId(R.id.input_text), withParent(withId(R.id.email_address_sign_in))));
	}

	public static void enterUsername(String name) {
		userName().perform(typeText(name));
	}

	public static ViewInteraction password() {
		return onView(allOf(withId(R.id.input_text), withParent(withId(R.id.password))));
	}

	public static void enterPassword(String name) {
		password().perform(typeText(name));
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
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.select_traveler_button)).perform(click());
	}

	public static void selectStoredTraveler(String travelername) throws Throwable {
		Espresso.closeSoftKeyboard();
		onView(withText(travelername))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static void clickStoredCardButton(boolean isHotelsPath) {
		Espresso.closeSoftKeyboard();
		if (isHotelsPath) {
			onView(allOf(withParent(withId(R.id.payment_button_v2)), withId(R.id.select_payment_button)))
				.perform(click());
		}
		else {
			onView(withId(R.id.select_payment_button)).perform(click());
		}
	}

	public static void selectStoredCard(String cardname) throws Throwable {
		Espresso.closeSoftKeyboard();
		onView(withText(cardname))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static ViewInteraction performSlideToPurchase() {
		return performSlideToPurchase(false);
	}

	public static ViewInteraction performSlideToPurchase(boolean isStoredCard) {
		onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.waitForViewToDisplay());
		ViewInteraction viewInteraction = onView(withId(R.id.slide_to_purchase_widget))
			.perform(ViewActions.swipeRight());
		Common.delay(1);
		if (isStoredCard) {
			CVVEntryScreen.enterCVV("6286");
			CVVEntryScreen.clickBookButton();
		}
		return viewInteraction;
	}

	public static void pressDoLogin() {
		Common.closeSoftKeyboard(CheckoutViewModel.password());
		Common.delay(1);
		onView(withId(R.id.sign_in_button)).perform(click());
	}

	public static void clickLogin() {
		onView(withId(R.id.login_widget)).perform(click());
	}

	public static void clickDone() {
		onView(withId(R.id.menu_done)).perform(click());
	}

	public static void enterLoginDetails() {
		clickLogin();
		enterUsername("qa-ehcc@mobiata.com");
		enterPassword("password");
	}

	public static void enterTravelerInfo() {
		Common.delay(2);
		clickDriverInfo();
		Common.delay(1);
		enterFirstName("FiveStar");
		enterLastName("Bear");
		Common.closeSoftKeyboard(CheckoutViewModel.lastName());
		Common.delay(1);
		enterEmail("noah@mobiata.com");
		Common.closeSoftKeyboard(CheckoutViewModel.email());
		Common.delay(1);
		enterPhoneNumber("4158675309");
		clickDone();
		Common.delay(2);
	}

	public static void selectStoredTraveler() throws Throwable {
		CheckoutViewModel.clickDriverInfo();
		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");
		CheckoutViewModel.pressClose();
	}

	public static void enterPaymentInfo() {
		Common.delay(2);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		enterPaymentDetails();
	}

	public static void enterPaymentInfo(boolean defaultSelection) {
		if (defaultSelection) {
			return;
		}
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		PaymentOptionsScreen.enterCardInfo();
	}

	public static void enterPaymentInfoHotels() {
		Common.delay(2);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		enterPaymentDetails();
	}

	public static void selectStoredCard(boolean isHotelsPath) throws Throwable {
		clickPaymentInfo();
		clickStoredCardButton(isHotelsPath);
		selectStoredCard("AmexTesting");
	}

	public static void enterPaymentDetails() {
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("66666");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		Common.delay(2);
	}

	public static void waitForSlideToPurchase() {
		onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.waitForViewToDisplay());
	}

	public static void waitForCheckout() {
		Matcher<View> displayedAndFilled = allOf(isDisplayed(), CustomMatchers.withAtLeastChildCount(1));
		onView(withId(R.id.summary_container)).perform(ViewActions.waitFor(displayedAndFilled, 10, TimeUnit.SECONDS));
		onView(withId(R.id.hint_container)).perform(ViewActions.waitForViewToDisplay());
	}

	public static void applyCoupon(String coupon) {
		clickCoupon();
		Common.delay(2);
		couponEditText().perform(typeText(coupon), closeSoftKeyboard());
		clickDone();
		Common.delay(2);
	}

	public static ViewInteraction coupon() {
		return onView(withText("Enter coupons or promotional code"));
	}

	public static void clickCoupon() {
		coupon().perform(scrollTo(), click());
	}

	public static ViewInteraction couponEditText() {
		return onView(withId(R.id.edit_coupon_code));
	}

	public static ViewInteraction scrollView() {
		return onView(withId(R.id.checkout_scroll));
	}

	public static void scrollToPriceChangeMessage() {
		onView(withId(R.id.price_change_container)).perform(scrollTo());
	}
}
