package com.expedia.bookings.test.ui.phone.tests.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.data.User;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchActionBar;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.TripsScreen;
import com.expedia.bookings.test.ui.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/23/14.
 */
public class TripsLoginTestsE extends ActivityInstrumentationTestCase2<PhoneLaunchActivity> {
	public TripsLoginTestsE() {
		super(PhoneLaunchActivity.class);
	}

	private static final String TAG = TripsLoginTestsE.class.getName();

	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		mUser = new HotelsUserData(getInstrumentation());
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	public void testChangingPOSLogsUserOut() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST: Changing POS logs user out");
		boolean userHasTrips = false;
		LaunchActionBar.pressTrips();
		TripsScreen.clickOnLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		ScreenActions.delay(5);
		assertTrue("User wasn't logged in despite having logged in.", User.isLoggedIn(mContext));
		try {
			assertFalse(withText("Error fetching itineraries").matches(isDisplayed()));
		}
		catch (Exception e) {
			Espresso.pressBack();
			ScreenActions.enterLog(TAG, "User was logged in, but there was an error fetching itineraries.");
		}
		try {
			assertFalse(withText("No upcoming trips.").matches(isDisplayed()));
			userHasTrips = true;
			ScreenActions.enterLog(TAG, "User has trips, so we'll check for trips later.");
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No upcoming trips");
		}

		// After having logged in, selecting a new POS, but declining
		// to actually switch to that POS means you're still logged in
		LaunchScreen.openMenuDropDown();
		LaunchScreen.pressSettings();

		SettingsScreen.clickCountryString();
		ScreenActions.enterLog(TAG, "Selecting different POS.");
		onView(withText(mRes.getString(R.string.country_ar))).perform(click());
		SettingsScreen.clickCancelString();
		ScreenActions.enterLog(TAG, "Declined to actually change the POS");
		assertTrue("User didn't remain logged in, despite declining to actually change the POS", User.isLoggedIn(mContext));
		Espresso.pressBack();
		// If user had trips before, that user should still have trips
		if (userHasTrips) {
			ScreenActions.enterLog(TAG, "User had trips before. We didn't log out, so the user should still have trips.");
			assertFalse(withText("No upcoming trips").matches(isDisplayed()));
		}
		LaunchScreen.openMenuDropDown();

		// After having logged in, selecting a new POS, and pressing
		// the affirmative dialog button does, in fact, log you out.
		LaunchScreen.pressSettings();
		SettingsScreen.clickCountryString();
		ScreenActions.enterLog(TAG, "Selecting different POS.");
		onView(withText(mRes.getString(R.string.country_ar))).perform(click());
		try {
			SettingsScreen.clickOKString();
		}
		catch (Exception e) {
			SettingsScreen.clickAcceptString();
		}
		ScreenActions.enterLog(TAG, "Pressed the affirmative button on the changing POS dialog.");
		assertFalse("User should no longer have been logged in after the POS was changed.", User.isLoggedIn(mContext));
		Espresso.pressBack();
		TripsScreen.logInButton().check(matches(isDisplayed()));
		//Changing back the point of sale
		SettingUtils.save(mContext, R.string.PointOfSaleKey, "28");
	}
}
