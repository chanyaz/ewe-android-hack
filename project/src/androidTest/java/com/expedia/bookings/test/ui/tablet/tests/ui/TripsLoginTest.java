package com.expedia.bookings.test.ui.tablet.tests.ui;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.TripsScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Itin;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.HotelsUserData;
import com.expedia.bookings.test.ui.utils.TabletTestCase;
import com.mobiata.android.util.SettingUtils;
import com.squareup.phrase.Phrase;

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
	private HotelsUserData mUser;
	private Context mContext;

	public void verifyUserLogsOutCorrectly(int stringId) {

		TripsScreen.clickOnLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		assertTrue(User.isLoggedIn(mContext));
		Common.pressBack();

		// After having logged in, selecting a new POS, but declining
		// to actually switch to that POS means you're still logged in
		LaunchScreen.openMenuDropDown();
		LaunchScreen.pressSettings();

		SettingsScreen.clickCountryString();
		ScreenActions.enterLog(TAG, "Selecting different POS.");
		onView(withText(mRes.getString(stringId))).perform(click());
		SettingsScreen.clickCancelString();
		assertTrue(User.isLoggedIn(mContext));
		ScreenActions.enterLog(TAG, "Declined to actually change the POS and user is still logged in");

		Common.pressBack();

		LaunchScreen.openMenuDropDown();
		// After having logged in, selecting a new POS, and pressing
		// the affirmative dialog button does, in fact, log you out.
		LaunchScreen.pressSettings();
		SettingsScreen.clickCountryString();
		ScreenActions.enterLog(TAG, "Selecting different POS.");
		onView(withText(mRes.getString(stringId))).perform(click());
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			SettingsScreen.clickacceptString();
		}
		assertFalse(User.isLoggedIn(mContext));
		ScreenActions.enterLog(TAG, "Pressed the affirmative button on the changing POS dialog and user is logged out.");

		Common.pressBack();

		Itin.clickTripsMenuButton();
		TripsScreen.logInButton().check(matches(isDisplayed()));

	}

	public void verifyClearDataWorksCorrectly() {
		TripsScreen.clickOnLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();
		assertTrue(User.isLoggedIn(mContext));
		Common.pressBack();

		// After having logged in, selecting a new POS, but declining
		// to actually switch to that POS means you're still logged in
		LaunchScreen.openMenuDropDown();
		LaunchScreen.pressSettings();

		SettingsScreen.clickClearPrivateData();
		SettingsScreen.clickCancelString();
		assertTrue(User.isLoggedIn(mContext));
		ScreenActions.enterLog(TAG, "Declined to actually clear private data and user is still logged in");

		Common.pressBack();

		LaunchScreen.openMenuDropDown();
		// After having logged in, selecting clear private data, and pressing
		// the affirmative dialog button does, in fact, log you out.
		LaunchScreen.pressSettings();
		SettingsScreen.clickClearPrivateData();
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			SettingsScreen.clickacceptString();
		}
		EspressoUtils.assertViewWithTextIsDisplayed(
			Phrase.from(mContext, R.string.dialog_message_signed_out_and_cleared_private_data_TEMPLATE).put("brand",
				BuildConfig.brand).format().toString());
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			SettingsScreen.clickacceptString();
		}

		assertFalse(User.isLoggedIn(mContext));
		ScreenActions.enterLog(TAG, "Pressed the affirmative button on clear private data and user is logged out.");

		Common.pressBack();

		Itin.clickTripsMenuButton();
		TripsScreen.logInButton().check(matches(isDisplayed()));

	}

	public void testChangingPOSLogsUserOut() throws Exception {
		mUser = new HotelsUserData(getInstrumentation());
		mContext = getInstrumentation().getTargetContext();
		Itin.clickTripsMenuButton();

		/*
		* Changing POS from US to Australia logs user out correctly
		 */

		verifyUserLogsOutCorrectly(R.string.country_au);

		/*
		* Test clear private data works correctly on change of Country
		 */

		verifyClearDataWorksCorrectly();

		/*
		*  Change to Canada POS from Australia POS and test everything again.
		 */
		verifyUserLogsOutCorrectly(R.string.country_ca);

		/*
		* Test clear private data works correctly on change of Country
		 */

		verifyClearDataWorksCorrectly();

		/*
		*  Change to Brazil POS from Canada POS and test everything again.
		 */

		verifyUserLogsOutCorrectly(R.string.country_br);

		/*
		* Test clear private data works correctly on change of Country
		 */

		verifyClearDataWorksCorrectly();

		//Changing back the point of sale
		SettingUtils.save(mContext, R.string.PointOfSaleKey, "29");
	}
}

