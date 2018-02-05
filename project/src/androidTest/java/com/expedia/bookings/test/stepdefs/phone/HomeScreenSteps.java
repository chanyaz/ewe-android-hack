package com.expedia.bookings.test.stepdefs.phone;

import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.support.Users;
import com.expedia.bookings.test.support.Users.User;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static com.expedia.bookings.test.pagemodels.common.LaunchScreen.waitForLOBHeaderToBeDisplayed;

public class HomeScreenSteps {
	@And("^I tap on \"(Shop|Trip|Account)\" tab$")
	public void homeScreenSwitchToTab(String tab) throws Throwable {
		switch (tab) {
			case "Account":
				EspressoUtils.waitForViewNotYetInLayoutToDisplay(LaunchScreen.getAccountButton(), 10, TimeUnit.SECONDS);
				LaunchScreen.accountButton().perform(click());
				break;
			case "Trips":
				EspressoUtils.waitForViewNotYetInLayoutToDisplay(LaunchScreen.getTripsButton(), 10, TimeUnit.SECONDS);
				LaunchScreen.tripsButton().perform(click());
				break;
			case "Shop":
				EspressoUtils.waitForViewNotYetInLayoutToDisplay(LaunchScreen.getShopButton(), 10, TimeUnit.SECONDS);
				LaunchScreen.shopButton().perform(click());
				break;
		}
	}

	@And("^I login with \"(.*?)\" tier user$")
	public void logInGivenUserTier(String userTier) throws Throwable {
		User user = new Users().findUser("tier", userTier);
		logInToTheApp(user);
	}

	@And("^I login with user, which has$")
	public void logInGivenParameters(Map<String, String> searchParams) throws Throwable {
		User user = new Users().findUser(searchParams);
		logInToTheApp(user);
	}

	@And("^I login to Expedia with credentials \"(.*?)\":\"(.*?)\" $")
	public void logInToTheApp(User user) throws Throwable {
		if (user.getType().toLowerCase().equals("facebook")) {
			LogInScreen.signInWithFacebookButton().perform(click());
			LogInScreen.FacebookSignIn.waitForViewToLoad();
			LogInScreen.FacebookSignIn.typeInEmail(user.getEmail());
			LogInScreen.FacebookSignIn.typeInPassword(user.getPassword());
			LogInScreen.FacebookSignIn.clickLogIn();
			LogInScreen.FacebookConfirmLogin.waitForViewToLoad();
			LogInScreen.FacebookConfirmLogin.clickContinue();

		}
		else if (user.getType().toLowerCase().equals("expedia")) {
			LogInScreen.signInButton().perform(waitForViewToDisplay(), click());
			LogInScreen.typeTextEmailEditText(user.getEmail());
			LogInScreen.typeTextPasswordEditText(user.getPassword());
			LogInScreen.clickOnLoginButton();
		}
	}

	@And("^I launch \"(.*?)\" LOB$")
	public void launchLOB(String lob) throws Throwable {
		switch (lob) {
		case "Hotels":
			LaunchScreen.hotelsLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Flights":
			LaunchScreen.flightLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		case "Bundle Deals":
			LaunchScreen.packagesLaunchButton().perform(waitForViewToDisplay(), click());
			break;
		}
	}

	@And("^I launch Package LOB with \"(.*?)\" POS$")
	public void launchPackage(String pos) throws Throwable {
		LaunchScreen.packagesLaunchButtonForPOS(pos).perform(waitForViewToDisplay(), click());
	}

	@Then("^Validate that Launch screen is displayed$")
	public void validateLaunchPage() {
		waitForLOBHeaderToBeDisplayed();
	}
}


