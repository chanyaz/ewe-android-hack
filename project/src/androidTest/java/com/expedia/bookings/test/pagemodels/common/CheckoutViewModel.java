package com.expedia.bookings.test.pagemodels.common;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.CustomMatchers;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.widget.CheckoutToolbar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CheckoutViewModel {
	// Checkout

	public static ViewInteraction travelerInfo() {
		return onView(withId(R.id.main_contact_info_card_view));
	}

	public static ViewInteraction paymentInfo() {
		return onView(withId(R.id.payment_info_card_view));
	}

	public static ViewInteraction addCreditCard() {
		return onView(withId(R.id.payment_option_credit_debit));
	}
	public static ViewInteraction purchaseTextAboveSlider() {
		return onView(withId(R.id.purchase_total_text_view));
	}
	public static ViewInteraction accountEarnPointText() {
		return onView(withId(R.id.account_rewards_textview));
	}
	public static ViewInteraction cardInfoName() {
		return onView(withId(R.id.card_info_name));
	}

	public static void clickTravelerInfo() {
		travelerInfo().perform(scrollTo(), click());
	}

	public static void clickPaymentInfo() {
		paymentInfo().perform(scrollTo(), click());
	}

	public static void waitForPaymentInfoDisplayed() {
		paymentInfo().perform(ViewActions.waitForViewToDisplay());
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
		onView(toolBarMatcher()).perform(waitForViewToDisplay());
		onView(toolBarMatcher()).perform(click());
	}

	public static Matcher<View> toolBarMatcher() {
		return allOf(
			withParent(withClassName(is(CheckoutToolbar.class.getName()))),
			withClassName(is(AppCompatImageButton.class.getName()))
		);
	}

	public static void clickStoredTravelerButton() {
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.select_traveler_button)).perform(waitForViewToDisplay());
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
	}

	public static void selectStoredCard(String cardname) throws Throwable {
		Espresso.closeSoftKeyboard();
		onView(Matchers.allOf(withId(R.id.text1), withText(cardname),
			isDescendantOfA(withId(R.id.stored_card_list)))).perform(scrollTo());
		onView(Matchers.allOf(withId(R.id.text1), withText(cardname),
			isDescendantOfA(withId(R.id.stored_card_list)))).perform(click());
	}

	public static ViewInteraction performSlideToPurchase() {
		return performSlideToPurchase(false);
	}

	public static ViewInteraction performSlideToPurchase(boolean isStoredCard) {
		onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.waitForViewToDisplay());
		Common.delay(1);	//Even though waitForViewToDisplay() is used, there is still some more delay that is needed before the swipe operation can be performed
		ViewInteraction viewInteraction = onView(withId(R.id.slide_to_purchase_widget))
			.perform(ViewActions.swipeRight());
		Common.delay(1);
		if (isStoredCard) {
			CVVEntryScreen.waitForCvvScreen();
			CVVEntryScreen.enterCVV("6286");
			CVVEntryScreen.clickBookButton();
		}
		return viewInteraction;
	}

	public static void pressDoLogin() {
		ViewInteraction signInButton = onView(withId(R.id.sign_in_button));
		signInButton.perform(waitForViewToDisplay());
		Common.closeSoftKeyboard(CheckoutViewModel.password());
		signInButton.perform(click());
		waitForCheckoutToolbar();
	}

	private static void waitForCheckoutToolbar() {
		waitForViewNotYetInLayoutToDisplay((withId(R.id.checkout_toolbar)), 10, TimeUnit.SECONDS);
	}

	public static void clickLogin() {
		onView(withId(R.id.login_widget)).perform(scrollTo(), click());
	}

	public static void signInOnCheckout() {
		enterLoginDetails();
		pressDoLogin();
	}
	public static void signInOnCheckout(String username,String password) {
		enterLoginDetails(username,password);
		pressDoLogin();
	}

	public static void clickDone() {
		onView(withId(R.id.menu_done)).perform(click());
	}

	public static void isDoneEnabled() {
		onView(withId(R.id.menu_done)).check(matches(isEnabled()));

	}

	public static void enterLoginDetails() {
		clickLogin();
		enterUsername("qa-ehcc@mobiata.com");
		enterPassword("password");
	}

	public static void enterSingleCardLoginDetails() {
		clickLogin();
		enterUsername("singlecard@mobiata.com");
		enterPassword("password");
	}

	public static void enterLoginDetails(String username,String password) {
		clickLogin();
		enterUsername(username);
		enterPassword(password);
	}

	public static void enterTravelerInfo() {
		Common.delay(2);
		clickTravelerInfo();
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
		CheckoutViewModel.clickTravelerInfo();
		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");
		CheckoutViewModel.pressClose();
	}

	public static void enterPaymentInfo() {
		Common.delay(2);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		enterPaymentDetails();
		CheckoutViewModel.clickDone();
		Common.delay(1);
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
		CheckoutViewModel.clickDone();
		Common.delay(1);
	}

	public static ViewInteraction dialogOkayButton() {
		Common.delay(1);
		return onView(withId(android.R.id.button1));
	}

	public static ViewInteraction dialogCancelButton() {
		Common.delay(1);
		return onView(withId(android.R.id.button2));
	}

	public static void selectStoredCard(boolean isHotelsPath) throws Throwable {
		clickPaymentInfo();
		selectStoredCard("Saved AmexTesting");
	}

	public static void selectStoredCardWithName(String storedCardName) throws Throwable {
		clickPaymentInfo();
		selectStoredCard(storedCardName);
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
	public static void assertSlideToPurchaseDisplayed() {
		onView(withId(R.id.touch_target)).check(matches(isDisplayed()));
		onView(withId(R.id.slider_text)).check(matches(withText("Slide to purchase")));
		onView(withId(R.id.slide_to_purchase_widget)).check(matches(isDisplayed()));
	}
	public static void waitForCheckout() {
		Matcher<View> displayedAndFilled = allOf(isDisplayed(), CustomMatchers.withAtLeastChildCount(1));
		onView(withId(R.id.summary_container)).perform(ViewActions.waitFor(displayedAndFilled, 10, TimeUnit.SECONDS));
		onView(withId(R.id.hint_container)).perform(scrollTo(), ViewActions.waitForViewToDisplay());
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

	public static  void assertPurchaseTotalText(String purchaseText) {
		purchaseTextAboveSlider().check(matches(withText(purchaseText)));
	}
	public static  void assertCardInfoText(String cardTest) {
		cardInfoName().check(matches(withText(cardTest)));
	}
	public static  void assertEarnPointsText(String earnPoints) {
		accountEarnPointText().check(matches(isDisplayed())).check(matches(withText(containsString(earnPoints))));
	}

	public static ViewInteraction resortFeeDisclaimerText() {
		return onView(withId(R.id.disclaimer_text));
	}

	public static ViewInteraction freeCancellationText() {
		return onView(withId(R.id.free_cancellation_text));
	}
	public static ViewInteraction freeCancellationTooltipText() {
		return onView(withId(R.id.free_cancellation_tooltip_text));
	}

	public static ViewInteraction freeCancellationWidget() {
		return onView(withId(R.id.free_cancellation_view));
	}

	public static ViewInteraction freeCancellationDescription() {
		return onView(withId(R.id.free_cancellation_description));
	}

}
