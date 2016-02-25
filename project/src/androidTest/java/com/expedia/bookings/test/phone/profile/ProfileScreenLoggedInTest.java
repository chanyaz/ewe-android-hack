package com.expedia.bookings.test.phone.profile;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;

import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsGone;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;

public class ProfileScreenLoggedInTest extends PhoneTestCase {

	public void testNotRewardsMember() {
		LaunchScreen.launchSignIn(getActivity());
		LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		LaunchScreen.launchProfileScreen(getActivity());
		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Mock Web Server");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "qa-ehcc@mobiata.com");
		assertViewIsGone(R.id.toolbar_loyalty_tier_text);
		assertViewIsGone(R.id.available_points);
		assertViewIsGone(R.id.pending_points);
		assertViewIsGone(R.id.country);
	}

	public void testBlueStatus() {
		LaunchScreen.launchSignIn(getActivity());
		LogInScreen.typeTextEmailEditText("singlecard@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		LaunchScreen.launchProfileScreen(getActivity());
		assertViewWithTextIsDisplayed(R.id.toolbar_name, "single card");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "singlecard@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.plus_blue);
		assertViewWithTextIsDisplayed(R.id.available_points, "1,802");
		assertViewWithTextIsDisplayed(R.id.pending_points, "0 pending");
		assertViewWithTextIsDisplayed(R.id.country, "United States");
	}

	public void testSilverStatus() {
		LaunchScreen.launchSignIn(getActivity());
		LogInScreen.typeTextEmailEditText("silverstatus@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		LaunchScreen.launchProfileScreen(getActivity());
		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Silver Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "silverstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.plus_silver);
		assertViewWithTextIsDisplayed(R.id.available_points, "22,996");
		assertViewWithTextIsDisplayed(R.id.pending_points, "965 pending");
		assertViewWithTextIsDisplayed(R.id.country, "United States");
	}

	public void testGoldStatus() {
		LaunchScreen.launchSignIn(getActivity());
		LogInScreen.typeTextEmailEditText("goldstatus@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		LaunchScreen.launchProfileScreen(getActivity());
		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Gold Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "goldstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.plus_gold);
		assertViewWithTextIsDisplayed(R.id.available_points, "54,206");
		assertViewWithTextIsDisplayed(R.id.pending_points, "5,601 pending");
		assertViewWithTextIsDisplayed(R.id.country, "United States");
	}
}
