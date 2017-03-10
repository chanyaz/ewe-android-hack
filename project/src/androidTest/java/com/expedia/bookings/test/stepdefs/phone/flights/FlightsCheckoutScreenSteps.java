package com.expedia.bookings.test.stepdefs.phone.flights;

import java.util.Map;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.newflights.FlightsScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;

import cucumber.api.java.en.And;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
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
}
