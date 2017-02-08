package com.expedia.bookings.test.tablet.pagemodels;

import java.util.concurrent.atomic.AtomicReference;

import android.app.Instrumentation;
import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.getNameMatchWarningView;
import static com.expedia.bookings.test.espresso.ViewActions.swipeRight;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class Checkout {
	public static void clickOnEmptyTravelerDetails() {
		onView(withId(R.id.empty_traveler_container)).perform(click());
	}

	public static void clickOnTravelerDetails() {
		onView(withId(R.id.traveler_section_container)).perform(click());
	}

	public static ViewInteraction nameMustMatchTextView() {
		final AtomicReference<String> value = new AtomicReference<String>();
		onView(allOf(withId(R.id.header_container), withParent(withParent(withParent(withId(R.id.traveler_form_container)))))).perform(getNameMatchWarningView(value));
		String filterValue = value.get();
		return onView(allOf(withId(R.id.header_name_match_message), hasSibling(withText(filterValue))));
	}

	public static ViewInteraction loginButton() {
		return onView(withId(R.id.login_text_view));
	}

	public static ViewInteraction firstName() {
		return onView(withId(R.id.edit_first_name));
	}

	public static ViewInteraction lastName() {
		return onView(withId(R.id.edit_last_name));
	}

	public static ViewInteraction middleName() {
		return onView(withId(R.id.edit_middle_name));
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

	public static void clickLoginButton() {
		loginButton().perform(click());
	}

	public static void clickLogOutButton() {
		onView(withId(R.id.account_logout_logout_button)).perform(click());
	}

	public static ViewInteraction redressNumber() {
		return onView(withId(R.id.edit_redress_number));
	}

	public static void clickRedressNumberButton() {
		onView(withId(R.id.redress_btn)).perform(click());
	}

	public static void enterRedressNumber(String text) {
		redressNumber().perform(typeText(text));
	}

	public static void enterFirstName(String text) {
		firstName().perform(scrollTo(), typeText(text));
	}

	public static void enterLastName(String text) {
		lastName().perform(scrollTo(), typeText(text));
	}

	public static void enterPhoneNumber(String text) {
		phoneNumber().perform(scrollTo(), typeText(text));
	}

	public static void enterEmailAddress(String text) {
		emailAddress().perform(scrollTo(), typeText(text));
	}

	public static void enterDateOfBirth(int year, int month, int day) {
		//TODO: use year/month/day
		dateOfBirth().perform(scrollTo(), click());
		onView(withId(android.R.id.button1)).perform(click());
	}

	public static void clickOnEnterPaymentInformation() {
		onView(withId(R.id.payment_info_btn)).perform(scrollTo(), click());
	}

	public static void clickOnEmptyStoredCCSpinnerButton() {
		onView(withId(R.id.empty_saved_creditcard_fake_spinner)).perform(click());
	}

	public static void clickOnStoredCCEditButton() {
		onView(withId(R.id.stored_creditcard_edit_button)).perform(click());
	}

	public static void clickOnRemoveStoredCCButton() {
		onView(withId(R.id.remove_stored_card_button)).perform(click());
	}

	public static void selectStoredCard(Instrumentation instrumentation, String cardname) throws Throwable {
		onView(withText(cardname))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static void selectStoredTraveler(Instrumentation instrumentation, String travelername) throws Throwable {
		onView(withText(travelername))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static ViewInteraction creditCardNumber() {
		return onView(withId(R.id.edit_creditcard_number));
	}

	public static ViewInteraction termsConditions() {
		return onView(withId(R.id.terms_and_conditions));
	}

	public static ViewInteraction privacyPolicy() {
		return onView(withId(R.id.privacy_policy));
	}

	public static ViewInteraction bestPriceGuarantee() {
		return onView(withId(R.id.best_price_guarantee));
	}

	public static ViewInteraction cancellationPolicy() {
		return onView(withId(R.id.cancellation_policy_header_text_view));
	}

	public static ViewInteraction nameOnCard() {
		return onView(withId(R.id.edit_name_on_card));
	}

	public static ViewInteraction postalCode() {
		return onView(withId(R.id.edit_address_postal_code));
	}

	public static void enterAddress1(String text) {
		address1().perform(scrollTo());
		address1().perform(typeText(text));
	}

	public static void enterAddress2(String text) {
		address2().perform(scrollTo());
		address2().perform(typeText(text));
	}

	public static void enterCity(String text) {
		addressCity().perform(scrollTo());
		addressCity().perform(typeText(text));
	}

	public static void enterState(String text) {
		addressState().perform(scrollTo());
		addressState().perform(clearText());
		addressState().perform(typeText(text));
	}

	public static void enterCreditCardNumber(String text) {
		creditCardNumber().perform(scrollTo());
		creditCardNumber().perform(typeText(text));
	}

	public static ViewInteraction expirationDate() {
		return onView(withId(R.id.edit_creditcard_exp_text_btn));
	}

	public static void setExpirationDate(int year, int month) {
		// TODO use year and month
		onView(withId(R.id.edit_creditcard_exp_text_btn)).perform(scrollTo());
		onView(withId(R.id.edit_creditcard_exp_text_btn)).perform(click());
		onView(withId(R.id.year_up)).perform(click());
		onView(withId(R.id.positive_button)).perform(click());
	}

	public static void enterNameOnCard(String text) {
		nameOnCard().perform(scrollTo());
		nameOnCard().perform(typeText(text));
	}

	public static void enterPostalCode(String text) {
		postalCode().perform(scrollTo());
		postalCode().perform(typeText(text));
	}

	public static void clickOnDone() {
		onView(allOf(withId(R.id.header_text_button_tv), isDisplayed())).perform(click());
	}

	public static void clickCreditCardSection() {
		onView(withId(R.id.creditcard_section_button)).perform(click());
	}

	public static void slideToPurchase() {
		onView(withId(R.id.slide_to_purchase_widget)).perform(swipeRight());
	}

	public static void enterCvv(String text) {
		for (int i = 0; i < text.length(); i++) {
			switch (text.charAt(i)) {
			case '1':
				onView(withId(R.id.one_button)).perform(click());
				break;
			case '2':
				onView(withId(R.id.two_button)).perform(click());
				break;
			case '3':
				onView(withId(R.id.three_button)).perform(click());
				break;
			case '4':
				onView(withId(R.id.four_button)).perform(click());
				break;
			case '5':
				onView(withId(R.id.five_button)).perform(click());
				break;
			case '6':
				onView(withId(R.id.six_button)).perform(click());
				break;
			case '7':
				onView(withId(R.id.seven_button)).perform(click());
				break;
			case '8':
				onView(withId(R.id.eight_button)).perform(click());
				break;
			case '9':
				onView(withId(R.id.nine_button)).perform(click());
				break;
			default:
				throw new NumberFormatException();
			}
		}
	}

	public static void clickBookButton() {
		onView(allOf(withId(R.id.finish_booking_button))).perform(click());
	}

	public static void clickDoneBooking() {
		onView(allOf(withId(R.id.done_booking_standalone))).perform(click());
	}

	public static void clickBookNextItem() {
		onView(allOf(withId(R.id.book_next), withText("Book Next Item"))).perform(click());
	}

	public static void clickLegalTextView() {
		onView(withId(R.id.legal_blurb)).perform(scrollTo(), click());
	}

	public static void clickLogOutString() {
		onView(withText("Log Out")).perform(click());
	}

	public static void clickGrandTotalTextView() {
		onView(allOf(withId(R.id.price_expanded_bucket_text_view), isDisplayed())).perform(click());
	}

	public static ViewInteraction costSummaryText() {
		return onView(withId(R.id.title_text_view));
	}

	public static void clickIAcceptButton() {
		onView(withId(R.id.layout_i_accept)).perform(click());
	}

	public static void clickOKButton() {
		onView(withId(android.R.id.button3)).perform(click());
	}

	public static void clickNegativeButton() {
		onView(withId(android.R.id.button2)).perform(click());
	}

	public static ViewInteraction tripDateRange() {
		return onView(withId(R.id.trip_date_range));
	}

	public static ViewInteraction phoneCountryCodeText() {
		return onView(withParent(withId(R.id.edit_phone_number_country_code_spinner)));
	}

	public static ViewInteraction passportCountryText() {
		return onView(withParent(withId(R.id.edit_passport_country_spinner)));
	}
}
