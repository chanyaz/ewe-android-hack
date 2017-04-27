package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay;
import static org.hamcrest.Matchers.allOf;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static org.hamcrest.Matchers.not;

public class FlightsCheckoutScreenSteps {
	@And("^I open traveller details$")
	public void openTravellerDetails() throws Throwable {
		//Checkout.clickOnTravelerDetails();
		FlightsScreen.selectTravellerDetails().perform(click());
	}

	@And("^Passport field is present on the traveler info form$")
	public void checkPassportFieldVisibility() throws Throwable {
		onView(withId(R.id.passport_country_spinner)).check(matches(isDisplayed()));
	}

	@And("^I fill the following details in the traveller details form:$")
	public void fillTravellersDetails(Map<String, String> parameters) throws Throwable {
		PackageScreen.enterFirstName(parameters.get("firstName"));
		PackageScreen.enterLastName(parameters.get("lastName"));
		PackageScreen.enterEmail(parameters.get("email"));
		PackageScreen.enterPhoneNumber(parameters.get("phoneNumber"));
		int year = Integer.parseInt(parameters.get("year"));
		int month = Integer.parseInt(parameters.get("month"));
		int date = Integer.parseInt(parameters.get("date"));
		PackageScreen.selectBirthDate(year,month,date);
		PackageScreen.selectGender(parameters.get("gender"));
	}

	@And("^I save the traveller details by hitting done$")
	public void saveTravellersDetails() throws Throwable {
		PackageScreen.clickTravelerDone();
	}

	@And("^Traveller details are not saved$")
	public void checkTheDetails() throws Throwable {
		onView(withId(R.id.passport_country_spinner)).check(matches(isDisplayed()));
	}

	@And("^Passport field is shown as a mandatory field$")
	public void isPassportFieldMandatory() throws Throwable {
		onView(withText("Passport: Country")).check(matches(withCompoundDrawable(R.drawable.invalid)));
	}

	@Then("^I login with user having single stored card at checkout screen$")
	public void loginWithSingleStoredCard() throws Throwable {
		CheckoutViewModel.enterSingleCardLoginDetails();
		CheckoutViewModel.pressDoLogin();
	}

	@Then("^I login with user having multiple stored card at checkout screen$")
	public void loginWithMultipleStoredCard() throws Throwable {
		CheckoutViewModel.enterLoginDetails("qa-ehcc@mobiata.com", "password");
		CheckoutViewModel.pressDoLogin();
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
		CheckoutViewModel.clickPaymentInfo();
	}
	@And("^Validate that Credit card \"(.*?)\" is shown selected at Payment Method screen$")
	public void validateSelectedPaymentMethod(String creditCard) throws Throwable {
		onView(withText(creditCard))
			.check(matches(hasSibling(withImageDrawable(R.drawable.validated))));
	}

	@And("^I tap on payment details$")
	public void tapOnPaymentDetails() throws Throwable {
		CheckoutViewModel.clickPaymentInfo();
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

	@Then("^I verify that a red exclamation is displayed on cardholder name$")
	public void verifyRedExclamation() throws Throwable {
		CardInfoScreen.nameOnCardEditText().check(matches(withCompoundDrawable(R.drawable.invalid)));
	}
}
