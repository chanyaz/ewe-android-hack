package com.expedia.bookings.test.tests.pageModels.tablet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static com.expedia.bookings.test.utils.EspressoUtils.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withContentDescription;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class Checkout {
	public static void clickOnTravelerDetails() {
		onView(withText("Traveler details")).perform(click());
	}

	public static ViewInteraction firstName() {
		return onView(withId(R.id.edit_first_name));
	}

	public static ViewInteraction lastName() {
		return onView(withId(R.id.edit_last_name));
	}

	public static ViewInteraction phoneNumber() {
		return onView(withId(R.id.edit_phone_number));
	}

	public static ViewInteraction emailAddress() {
		return onView(allOf(withId(R.id.edit_email_address), isDisplayed()));
	}

	public static ViewInteraction dateOfBirth() {
		return onView(withId(R.id.edit_birth_date_text_btn));
	}

	public static ViewInteraction address1() {
		return onView(withId(R.id.edit_address_line_one));
	}

	public static ViewInteraction address2() {
		return onView(withId(R.id.edit_address_line_two));
	}

	public static ViewInteraction addressCity() {
		return onView(withId(R.id.edit_address_city));
	}

	public static ViewInteraction addressState() {
		return onView(withId(R.id.edit_address_state));
	}

	public static void enterFirstName(String text) {
		firstName().perform(scrollTo(), click(), typeText(text));
	}

	public static void enterLastName(String text) {
		lastName().perform(scrollTo(), click(), typeText(text));
	}

	public static void enterPhoneNumber(String text) {
		phoneNumber().perform(scrollTo(), click(), typeText(text));
	}

	public static void enterEmailAddress(String text) {
		emailAddress().perform(scrollTo(), click(), typeText(text));
	}

	public static void enterDateOfBirth(int year, int month, int day) {
		//TODO: use year/month/day
		dateOfBirth().perform(scrollTo(), click());
		onView(withText("Done")).perform(click());
	}

	public static void clickOnEnterPaymentInformation() {
		onView(withText("Enter payment information")).perform(scrollTo(), click());
	}

	public static ViewInteraction creditCardNumber() {
		return onView(withId(R.id.edit_creditcard_number));
	}

	public static ViewInteraction nameOnCard() {
		return onView(withId(R.id.edit_name_on_card));
	}

	public static ViewInteraction postalCode() {
		return onView(withId(R.id.edit_address_postal_code));
	}

	public static void enterAddress1(String text) {
		address1().perform(scrollTo());
		address1().perform(click());
		address1().perform(typeText(text));
	}

	public static void enterAddress2(String text) {
		address2().perform(scrollTo());
		address2().perform(click());
		address2().perform(typeText(text));
	}

	public static void enterCity(String text) {
		addressCity().perform(scrollTo());
		addressCity().perform(click());
		addressCity().perform(typeText(text));
	}

	public static void enterState(String text) {
		addressState().perform(scrollTo());
		addressState().perform(click());
		addressState().perform(clearText());
		addressState().perform(typeText(text));
	}

	public static void enterCreditCardNumber(String text) {
		creditCardNumber().perform(scrollTo());
		creditCardNumber().perform(click());
		creditCardNumber().perform(typeText(text));
	}

	public static void setExpirationDate(int year, int month) {
		// TODO use year and month
		onView(withId(R.id.edit_creditcard_exp_text_btn)).perform(click());
		onView(withId(R.id.year_up)).perform(click());
		onView(withText("Set")).perform(click());
	}

	public static void enterNameOnCard(String text) {
		nameOnCard().perform(scrollTo());
		nameOnCard().perform(click());
		nameOnCard().perform(typeText(text));
	}

	public static void enterPostalCode(String text) {
		postalCode().perform(scrollTo());
		postalCode().perform(click());
		postalCode().perform(typeText(text));
	}

	public static void clickOnDone() {
		onView(allOf(withId(R.id.header_text_button_tv), withText("Done"), isDisplayed())).perform(click());
	}

	public static void slideToPurchase() {
		onView(withId(R.id.slide_to_purchase_widget)).perform(swipeRight());
	}

	public static void enterCvv(String text) {
		// TODO use cvv passed to us
		// Right now this just enters 111
		onView(withId(R.id.one_button)).perform(click());
		onView(withId(R.id.one_button)).perform(click());
		onView(withId(R.id.one_button)).perform(click());
	}

	public static void clickBookButton() {
		onView(allOf(withId(R.id.book_button), withText("Book"))).perform(click());
	}

	public static void clickDoneBooking() {
		onView(allOf(withId(R.id.done_booking), withText("Done Booking"))).perform(click());
	}

	public static void clickBookNextItem() {
		onView(allOf(withId(R.id.book_next), withText("Book Next Item"))).perform(click());
	}

}
