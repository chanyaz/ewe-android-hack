package com.expedia.bookings.test.stepdefs.phone.hotel;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;

import cucumber.api.java.en.And;

import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public class CheckoutScreenSteps {

	@And("^I verify resort fee disclaimer text is displayed$")
	public void verifyResortFeeDisclaimerText() throws Throwable {
		Common.delay(2);
		CheckoutScreen.resortFeeDisclaimerText().perform(scrollTo());
		CheckoutScreen.resortFeeDisclaimerText().check(matches(isDisplayed()));
	}
}
