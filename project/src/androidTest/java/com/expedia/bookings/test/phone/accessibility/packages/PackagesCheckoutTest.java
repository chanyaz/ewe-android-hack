package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.CustomMatchers.withInfoText;

public class PackagesCheckoutTest extends PackageTestCase {

	@Test
	public void testTravelerWidget() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.checkout().perform(click());

		PackageScreen.travelerInfo().perform(scrollTo(), click());
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.first_name_input)).check(matches(withInfoText(" First Name, , ")));
		onView(withId(R.id.last_name_input)).check(matches(withInfoText(" Last Name, , ")));
		onView(withId(R.id.edit_email_address)).check(matches(withInfoText(" Email Address (for confirmation), , ")));
		onView(withId(R.id.edit_phone_number)).check(matches(withInfoText(" Phone Number, , ")));
		onView(withId(R.id.edit_birth_date_text_btn)).check(matches(withInfoText(" Date of Birth, , ")));
		PackageScreen.clickTravelerAdvanced();
		onView(withId(R.id.redress_number)).check(matches(withInfoText(" Redress # (if applicable), , ")));
		PackageScreen.clickTravelerAdvanced();
		Common.pressBack();

		PackageScreen.enterTravelerInfo();
		onView(withId(R.id.first_name_input)).check(matches(withInfoText(" First Name, FiveStar, ")));
		onView(withId(R.id.last_name_input)).check(matches(withInfoText(" Last Name, Bear, ")));
		onView(withId(R.id.edit_email_address))
			.check(matches(withInfoText(" Email Address (for confirmation), noah@mobiata.com, ")));
		onView(withId(R.id.edit_phone_number)).check(matches(withInfoText(" Phone Number, 7732025862, ")));
		onView(withId(R.id.edit_birth_date_text_btn)).check(matches(withInfoText(" Date of Birth, Jan 1, 1900, ")));
		onView(withId(R.id.redress_number)).check(matches(withInfoText(" Redress # (if applicable), 1234567, ")));
		Common.pressBack();
	}

	@Test
	public void testPaymentWidget() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.checkout().perform(click());

		PackageScreen.clickPaymentInfo();
		Espresso.closeSoftKeyboard();
		onView(withId(R.id.edit_creditcard_number)).check(matches(withInfoText("Enter new Debit/Credit Card")));
		onView(withId(R.id.edit_creditcard_exp_text_btn))
			.check(matches(withInfoText(" Expiration Date, Opens dialog")));
		onView(withId(R.id.edit_creditcard_cvv)).check(matches(withInfoText("CVV")));
		onView(withId(R.id.edit_name_on_card)).check(matches(withInfoText("Cardholder name")));
		onView(withId(R.id.edit_address_line_one)).check(matches(withInfoText("Address line 1")));
		onView(withId(R.id.edit_address_line_two)).check(matches(withInfoText("Address line 2 (optional)")));
		onView(withId(R.id.edit_address_city)).check(matches(withInfoText("City")));
		onView(withId(R.id.edit_address_state)).check(matches(withInfoText("State")));
		onView(withId(R.id.edit_address_postal_code)).check(matches(withInfoText("Zip Code")));
		Common.pressBack();

		PackageScreen.clickPaymentInfo();
		Espresso.closeSoftKeyboard();
		PackageScreen.enterCreditCard();
		PackageScreen.completePaymentForm();
		onView(withId(R.id.edit_creditcard_number))
			.check(matches(withInfoText("Enter new Debit/Credit Card, 4111111111111111")));
		LocalDate monthDate = LocalDate.now();
		LocalDate yearDate = LocalDate.now();
		yearDate = yearDate.plusYears(1);
		monthDate = monthDate.plusMonths(1);
		String dateString = monthDate.toString("MM") + "/" + yearDate.toString("yy");
		onView(withId(R.id.edit_creditcard_exp_text_btn))
			.check(matches(withInfoText(" Expiration Date, " + dateString + ", Opens dialog")));
		onView(withId(R.id.edit_creditcard_cvv)).check(matches(withInfoText("CVV")));
		onView(withId(R.id.edit_name_on_card)).check(matches(withInfoText("Cardholder name, Mobiata Auto")));
		onView(withId(R.id.edit_address_line_one))
			.check(matches(withInfoText("Address line 1, 123 California Street")));
		onView(withId(R.id.edit_address_line_two)).check(matches(withInfoText("Address line 2 (optional)")));
		onView(withId(R.id.edit_address_city)).check(matches(withInfoText("City, San Francisco")));
		onView(withId(R.id.edit_address_state)).check(matches(withInfoText("State, CA")));
		onView(withId(R.id.edit_address_postal_code)).check(matches(withInfoText("Zip Code, 94105")));
		Common.pressBack();
	}
}
