package com.expedia.bookings.test.tests.itins.ui.regression;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.data.User;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class TripsLoginTests extends CustomActivityInstrumentationTestCase<LaunchActivity> {

	private static final String TAG = TripsLoginTests.class.getSimpleName();

	public TripsLoginTests() {
		super(LaunchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testChangingPOSLogsUserOut() throws Exception {
		mDriver.enterLog(TAG, "START TEST: Changing POS logs user out");
		boolean userHasTrips = false;
		mDriver.launchScreen().swipeToTripsScreen();
		mDriver.tripsScreen().clickOnLogInButton();
		mDriver.logInScreen().typeTextEmailEditText(mUser.getLoginEmail());
		mDriver.logInScreen().typeTextPasswordEditText(mUser.getLoginPassword());
		mDriver.logInScreen().clickOnLoginButton();
		mDriver.waitForStringToBeGone(mDriver.logInScreen().fetchingYourItineraries(), 60);
		assertTrue("User wasn't logged in despite having logged in.", User.isLoggedIn(mContext));
		if (mDriver.searchText(mDriver.logInScreen().errorFetchingYourItineraries(), 1, false)) {
			mDriver.goBack();
			mDriver.enterLog(TAG, "User was logged in, but there was an error fetching itineraries.");
		}
		else if (!mDriver.searchText(mDriver.logInScreen().noUpcomingTrips(), 1, false)) {
			userHasTrips = true;
			mDriver.enterLog(TAG, "User has trips, so we'll check for trips later.");
		}

		// After having logged in, selecting a new POS, but declining
		// to actually switch to that POS means you're still logged in
		mDriver.tripsScreen().openMenuDropDown();
		mDriver.tripsScreen().pressSettings();
		mDriver.settingsScreen().clickCountryString();
		mDriver.enterLog(TAG, "Selecting different POS.");
		mDriver.clickOnText(mRes.getString(R.string.country_ar));
		mDriver.settingsScreen().clickCancelString();
		mDriver.enterLog(TAG, "Declined to actually change the POS");
		assertTrue("User didn't remain logged in, despite declining to actually change the POS",
				User.isLoggedIn(mContext));
		mDriver.goBack();
		// If user had trips before, that user should still have trips
		if (userHasTrips) {
			mDriver.enterLog(TAG, "User had trips before. We didn't log out, so the user should still have trips.");
			assertFalse(mDriver.searchText(mDriver.logInScreen().noUpcomingTrips(), 1, false));
		}
		mDriver.tripsScreen().openMenuDropDown();
		assertTrue("The log out string wasn't present in the menu dropdown, despite not previously logging out",
				mDriver.searchText(mDriver.tripsScreen().logOutString(), true));

		// After having logged in, selecting a new POS, and pressing
		// the affirmative dialog button does, in fact, log you out.
		mDriver.tripsScreen().pressSettings();
		mDriver.settingsScreen().clickCountryString();
		mDriver.enterLog(TAG, "Selecting different POS.");
		mDriver.clickOnText(mRes.getString(R.string.country_ar));
		if (mDriver.searchText(mDriver.settingsScreen().OKString())) {
			mDriver.settingsScreen().clickOKString();
		}
		else {
			mDriver.settingsScreen().clickAcceptString();
		}
		mDriver.enterLog(TAG, "Pressed the affirmative button on the changing POS dialog.");
		assertFalse("User should no longer have been logged in after the POS was changed.", User.isLoggedIn(mContext));
		mDriver.goBack();
		assertTrue(mDriver.tripsScreen().logInButton().isShown());

		// Set the POS back to US because other tests might depend on that.
		mDriver.tripsScreen().openMenuDropDown();
		mDriver.tripsScreen().pressSettings();
		mDriver.settingsScreen().clickCountryString();
		mDriver.clickOnText(mRes.getString(R.string.country_us));
		if (mDriver.searchText(mDriver.settingsScreen().OKString())) {
			mDriver.settingsScreen().clickOKString();
		}
		else {
			mDriver.settingsScreen().clickAcceptString();
		}
		mDriver.goBack();
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
