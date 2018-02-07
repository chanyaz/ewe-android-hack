package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.test.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.hasTextInputLayoutAccesibilityEditText;
import static com.expedia.bookings.test.espresso.CustomMatchers.hasTextInputLayoutErrorText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class FlightsCheckoutScreenSteps {
	@And("^I open traveller details$")
	public void openTravellerDetails() throws Throwable {
		FlightsScreen.selectTravellerDetails().perform(click());
	}

	@And("^Passport field is present on the traveler info form$")
	public void checkPassportFieldVisibility() throws Throwable {
		onView(withId(R.id.passport_country_btn)).check(matches(isDisplayed()));
	}

	@And("^I fill the following details in the traveller details form:$")
	public void fillTravellersDetails(Map<String, String> parameters) throws Throwable {
		for (String key : parameters.keySet()) {
			switch (key) {
			case "firstName":
				TravelerDetails.enterFirstName(parameters.get("firstName"));
				break;
			case "lastName":
				TravelerDetails.enterLastName(parameters.get("lastName"));
				break;
			case "email":
				TravelerDetails.enterEmail(parameters.get("email"));
				break;
			case "phoneNumber":
				TravelerDetails.enterPhoneNumber(parameters.get("phoneNumber"));
				break;
			case "year":
				int year = Integer.parseInt(parameters.get("year"));
				int month = Integer.parseInt(parameters.get("month"));
				int date = Integer.parseInt(parameters.get("date"));
				Espresso.closeSoftKeyboard();
				TravelerDetails.selectBirthDate(year, month, date);
				break;
			case "gender":
				TravelerDetails.materialSelectGender(parameters.get("gender"));
				break;
			case "passport":
				selectPassport(parameters.get("passport"));
				break;
			}
		}
	}

	@And("^I save the traveller details by hitting done$")
	public void saveTravellersDetails() throws Throwable {
		TravelerDetails.clickDone();
	}

	@And("^Traveller details are not saved$")
	public void checkTheDetails() throws Throwable {
		onView(withId(R.id.passport_country_btn)).check(matches(isDisplayed()));
	}

	@And("^Passport field is shown as a mandatory field$")
	public void isPassportFieldMandatory() throws Throwable {
		onView(withText("Passport: Country")).check(matches(withCompoundDrawable(R.drawable.invalid)));
	}

	@Then("^I verify that error hint \"(.*?)\" is displayed for Passport$")
	public void isPassportFieldMandatory(String errorHint) throws Throwable {
		TravelerDetails.passportCountry()
			.check(matches(hasTextInputLayoutErrorText(errorHint)));
	}

	@Then("^I login with user having single stored card at checkout screen$")
	public void loginWithSingleStoredCard() throws Throwable {
		CheckoutScreen.enterSingleCardLoginDetails();
		CheckoutScreen.pressDoLogin();
	}

	@Then("^I login with user having multiple stored card at checkout screen$")
	public void loginWithMultipleStoredCard() throws Throwable {
		CheckoutScreen.enterLoginDetails("qa-ehcc@mobiata.com", "password");
		CheckoutScreen.pressDoLogin();
	}

	@And("^I wait for checkout screen to load$")
	public void waitForTravelerContainerToAppear() throws Throwable {
		waitForViewNotYetInLayoutToDisplay(withId(R.id.traveler_default_state), 10, TimeUnit.SECONDS);
	}

	@And("^Validate that Main traveller \"(.*?)\" is selected by default$")
	public void validateMainTravellerSelected(String travelerName) throws Throwable {
		onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
			matches(withText(travelerName)));
	}
	@And("^Validate that Credit card \"(.*?)\" is selected by default$")
	public void validateCreditCardSelected(String creditCard) throws Throwable {
		onView(allOf(withId(R.id.card_info_name), isDescendantOfA(withId(R.id.card_info_container)))).check(
			matches(withText(creditCard)));
	}
	@And("^I click on Payment Info$")
	public void clickPaymentInfo() throws Throwable {
		CheckoutScreen.clickPaymentInfo();
	}
	@And("^Validate that Credit card \"(.*?)\" is shown selected at Payment Method screen$")
	public void validateSelectedPaymentMethod(String creditCard) throws Throwable {
		onView(withText(creditCard))
			.check(matches(hasSibling(withImageDrawable(R.drawable.validated))));
	}

	@And("^I tap on payment details$")
	public void tapOnPaymentDetails() throws Throwable {
		CheckoutScreen.clickPaymentInfo();
	}

	@Then("^I verify that field to enter credit card is present on the payment details form$")
	public void verifyCreditCardFieldIsPresent() throws Throwable {
		CardInfoScreen.creditCardNumberEditText().perform(waitForViewToDisplay()).check(matches(isDisplayed()));
	}

	@Then("^I enter the card number$")
	public void enterCardNumber (Map<String, String> parameters) throws Throwable {
		CardInfoScreen.creditCardNumberEditText().perform(waitForViewToDisplay());
		PackageScreen.enterCreditCardNumber(parameters.get("card_number"));
	}

	@Then("^Validate the error message displayed$")
	public void errorMessage (Map<String, String> parameters) throws Throwable {
		PackageScreen.errorMessageWhenCardNotAccepted(parameters.get("error_message"));
	}

	@Then("^Also verify the credit card image when card is not accepted for payment$")
	public void verifyCardImageWhenNotAccepted() throws Throwable {
		onView(withId(R.id.display_credit_card_brand_icon_grey)).check(matches(withImageDrawable(R.drawable.ic_lcc_no_card_payment_entry)));
	}

	@Then("^I verify that cardholder name field is present on the payment details form$")
	public void verifyCardholderNameFieldIsPresent() throws Throwable {
		CardInfoScreen.nameOnCardEditText().perform(waitForViewToDisplay()).check(matches(isDisplayed()));
	}

	@Then("^I tap on the cardholder name field$")
	public void tapOnCardholderField() throws Throwable {
		CardInfoScreen.nameOnCardEditText().perform(waitForViewToDisplay(),click());
	}

	@Then("^I enter the first name$")
	public void enterFirstName() throws Throwable {
		CardInfoScreen.typeTextNameOnCardEditText("test");
	}

	@Then("^I tap on some other field say Address field$")
	public void tapSomeOtherField() throws Throwable {
		BillingAddressScreen.addressLineOneEditText(R.id.section_location_address).perform(closeSoftKeyboard())
			.perform((waitForViewToDisplay()), click());

	}

	@Then("^I enter the first name and last name$")
	public void enterFirstAndLastName() throws Throwable {
		CardInfoScreen.typeTextNameOnCardEditText("test test");
	}

	@Then("^I verify that no red exclamation is displayed on cardholder name$")
	public void verifyRedExclamationNotPresent() throws Throwable {
		CardInfoScreen.nameOnCardEditText()
			.check(matches(not(withCompoundDrawable(R.drawable.invalid))));
	}

	@Then("^I verify that error hint \"(.*?)\" is displayed for cardholder name$")
	public void verifyErrorHintPresentWithText(String errorHint) throws Throwable {
		CardInfoScreen.nameOnCardEditText()
			.check(matches(hasTextInputLayoutAccesibilityEditText(errorHint)));
	}

	@Then("^I verify that a red exclamation is displayed on cardholder name$")
	public void verifyRedExclamation() throws Throwable {
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.invalid)));
	}
	@Then("^I fill the payment details$")
	public void fillPaymentDetails() throws Throwable {
		onView(withId(R.id.edit_creditcard_number)).perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
		PackageScreen.enterCreditCard();
		PackageScreen.completePaymentForm();
		PackageScreen.clickPaymentDone();
	}
	private void selectPassport(String country) {
		onView(withId(R.id.passport_country_btn)).perform(click());
		onView(withText(country)).perform(click());
	}
}
