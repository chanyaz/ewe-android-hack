package com.expedia.bookings.test.phone.packages;


import org.junit.Test;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageCheckoutTravelerAndPaymentInfoClearTest extends PackageTestCase {

	@Test
	public void testPackageChangeFlightClearsPaymentInfoTest() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.checkout().perform(click());
		PackageScreen.enterTravelerInfo();
		PackageScreen.enterPaymentInfo();
		pressBack();

		//change flights
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change flights")).perform(click());
		Common.delay(1);

		onView(withId(R.id.flight_results_price_header)).check(matches(isDisplayed()));
		EspressoUtils.assertViewWithIdIsNotDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.package_best_flight);

		PackageScreen.selectFlight(-2);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(-2);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.checkout().perform(click());
		onView(withId(R.id.traveler_default_state)).perform(scrollTo(), click());
		assertTravelerInfoCleared();

		PackageScreen.toolbarNavigationUp(R.id.checkout_toolbar).perform(click());

		CheckoutViewModel.waitForPaymentInfoDisplayed();
		CheckoutViewModel.clickPaymentInfo();
		assertPaymentInfoCleared();
	}

	@Test
	public void testPackageCcNumberAndCvvClearsAfterPaymentError() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.checkout().perform(click());

		PackageScreen.enterTravelerInfo();
		CheckoutViewModel.clickPaymentInfo();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextCvv("666");
		CardInfoScreen.typeTextNameOnCardEditText("malcolm nguyen");

		int addressSectionParentId = R.id.section_location_address;
		BillingAddressScreen.typeTextAddressLineOne("123 California Street", addressSectionParentId);
		BillingAddressScreen.typeTextCity("errorcheckoutcard", addressSectionParentId);
		BillingAddressScreen.typeTextState("CA", addressSectionParentId);
		BillingAddressScreen.typeTextPostalCode("94105", addressSectionParentId);
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();

		PackageScreen.assertErrorScreen("Edit Payment", "We're sorry, but we were unable to process your payment. Please verify that you entered your information correctly.");
		onView(withId(R.id.error_action_button)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_cvv, "");
	}

	@Test
	public void testPaymentInfoChangedAfterUserLoggedIn() throws Throwable {
		PackageScreen.doPackageSearch();
		PackageScreen.checkout().perform(click());
		PackageScreen.enterTravelerInfo();

		CheckoutViewModel.clickPaymentInfo();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextCvv("666");
		CardInfoScreen.typeTextNameOnCardEditText("malcolm nguyen");

		int addressSectionParentId = R.id.section_location_address;
		BillingAddressScreen.typeTextAddressLineOne("123 California Street", addressSectionParentId);
		BillingAddressScreen.typeTextCity("errorcheckoutcard", addressSectionParentId);
		BillingAddressScreen.typeTextState("CA", addressSectionParentId);
		BillingAddressScreen.typeTextPostalCode("94105", addressSectionParentId);
		CheckoutViewModel.clickDone();

		CheckoutViewModel.paymentInfo().check(matches(hasDescendant(withText("Visa …1111"))));
		Espresso.onView(ViewMatchers.withId(R.id.slide_to_purchase_widget)).check(ViewAssertions.matches(isCompletelyDisplayed()));

		CheckoutViewModel.signInOnCheckout();
		CheckoutViewModel.waitForPaymentInfoDisplayed();

		CheckoutViewModel.paymentInfo().check(matches(hasDescendant(withText("AmexTesting"))));
		CheckoutViewModel.waitForSlideToPurchase();
		Espresso.onView(ViewMatchers.withId(R.id.slide_to_purchase_widget)).check(ViewAssertions.matches(isCompletelyDisplayed()));
	}

	private void assertTravelerInfoCleared() {
		assertEditTextWithIdIsEmpty(R.id.first_name_input);
		assertEditTextWithIdIsEmpty(R.id.last_name_input);
		assertEditTextWithIdIsEmpty(R.id.edit_phone_number);
		assertEditTextWithIdIsEmpty(R.id.edit_birth_date_text_btn);
	}

	private void assertPaymentInfoCleared() {
		assertEditTextWithIdIsEmpty(R.id.edit_creditcard_number);
		assertEditTextWithIdIsEmpty(R.id.edit_creditcard_exp_text_btn);
		assertEditTextWithIdIsEmpty(R.id.edit_creditcard_cvv);
		assertEditTextWithIdIsEmpty(R.id.edit_name_on_card);
		assertEditTextWithIdIsEmpty(R.id.edit_address_line_one);
		assertEditTextWithIdIsEmpty(R.id.edit_address_line_two);
		assertEditTextWithIdIsEmpty(R.id.edit_address_city);
		assertEditTextWithIdIsEmpty(R.id.edit_address_state);
		assertEditTextWithIdIsEmpty(R.id.edit_address_postal_code);
	}

	private void assertEditTextWithIdIsEmpty(int id) {
		Espresso.onView(ViewMatchers.withId(id))
				.check(ViewAssertions.matches(ViewMatchers.withText("")));
	}
}
