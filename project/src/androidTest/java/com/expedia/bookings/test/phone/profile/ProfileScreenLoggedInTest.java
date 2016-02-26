package com.expedia.bookings.test.phone.profile;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ProfileScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
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
		assertTextViewHasCompoundDrawable(R.id.country, R.drawable.ic_flag_us);
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
		assertTextViewHasCompoundDrawable(R.id.country, R.drawable.ic_flag_us);
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
		assertTextViewHasCompoundDrawable(R.id.country, R.drawable.ic_flag_us);
	}

	// only spot checking a few countries as OOM issues are preventing testing all

	public void testArgentina() {
		doCountryTest("Argentina", R.drawable.ic_flag_ar);
	}

	public void testCanada() {
		doCountryTest("Canada", R.drawable.ic_flag_ca);
	}

	public void testHongKong() {
		doCountryTest("Hong Kong", R.drawable.ic_flag_hk);
	}

	public void testKorea() {
		doCountryTest("Korea", R.drawable.ic_flag_kr);
	}

	public void testUnitedKingdom() {
		doCountryTest("United Kingdom", R.drawable.ic_flag_gb);
	}

	private void doCountryTest(String countryName, @DrawableRes int flagResId) {
		LaunchScreen.launchProfileScreen(getActivity());
		ProfileScreen.clickCountry();
		ProfileScreen.clickCountryInList(countryName);
		ProfileScreen.clickOK();
		Espresso.pressBack();

		LaunchScreen.launchSignIn(getActivity());
		LogInScreen.typeTextEmailEditText("goldstatus@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		LaunchScreen.launchProfileScreen(getActivity());
		assertViewWithTextIsDisplayed(R.id.country, countryName);
		assertTextViewHasCompoundDrawable(R.id.country, flagResId);
	}

	private static void assertTextViewHasCompoundDrawable(@IdRes int viewId, @DrawableRes int drawableId) {
		onView(withId(viewId)).check(matches(withCompoundDrawable(drawableId)));
	}
}
