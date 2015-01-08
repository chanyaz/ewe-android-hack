package com.expedia.bookings.test.ui.tablet.tests.ui;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.TripsScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Itin;
import com.expedia.bookings.test.ui.utils.HotelsUserData;
import com.expedia.bookings.test.ui.utils.TabletTestCase;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 1/8/15.
 */
public class TripsLoginTest extends TabletTestCase {

	private static final String TAG = TripsLoginTest.class.getName();

	public void testChangingPOSLogsUserOut() throws Exception {
		HotelsUserData user = new HotelsUserData(getInstrumentation());
		Context context = getInstrumentation().getTargetContext();

		ScreenActions.enterLog(TAG, "START TEST: Changing POS logs user out");
		Itin.clickTripsMenuButton();
		TripsScreen.clickOnLogInButton();
		LogInScreen.typeTextEmailEditText(user.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(user.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		assertTrue(User.isLoggedIn(context));
		Common.pressBack();

		// After having logged in, selecting a new POS, but declining
		// to actually switch to that POS means you're still logged in
		LaunchScreen.openMenuDropDown();
		LaunchScreen.pressSettings();

		SettingsScreen.clickCountryString();
		ScreenActions.enterLog(TAG, "Selecting different POS.");
		onView(withText(mRes.getString(R.string.country_ar))).perform(click());
		SettingsScreen.clickCancelString();
		assertTrue(User.isLoggedIn(context));
		ScreenActions.enterLog(TAG, "Declined to actually change the POS and user is still logged in");

		Common.pressBack();

		LaunchScreen.openMenuDropDown();
		// After having logged in, selecting a new POS, and pressing
		// the affirmative dialog button does, in fact, log you out.
		LaunchScreen.pressSettings();
		SettingsScreen.clickCountryString();
		ScreenActions.enterLog(TAG, "Selecting different POS.");
		onView(withText(mRes.getString(R.string.country_ar))).perform(click());
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			SettingsScreen.clickacceptString();
		}
		assertFalse(User.isLoggedIn(context));
		ScreenActions.enterLog(TAG, "Pressed the affirmative button on the changing POS dialog and user is logged out.");

		Common.pressBack();

		Itin.clickTripsMenuButton();
		TripsScreen.logInButton().check(matches(isDisplayed()));

		//Changing back the point of sale
		SettingUtils.save(context, R.string.PointOfSaleKey, "29");
	}
}

