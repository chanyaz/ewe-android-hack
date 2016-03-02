package com.expedia.bookings.test.phone.profile;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ProfileScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.CustomMatchers.sameBitmap;
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
		assertViewWithTextIsDisplayed(R.id.country, "USA");
		assertTextViewHasCompoundDrawableFlag(R.id.country, R.drawable.ic_flag_us);
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
		assertViewWithTextIsDisplayed(R.id.country, "USA");
		assertTextViewHasCompoundDrawableFlag(R.id.country, R.drawable.ic_flag_us);
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
		assertViewWithTextIsDisplayed(R.id.country, "USA");
		assertTextViewHasCompoundDrawableFlag(R.id.country, R.drawable.ic_flag_us);
	}

	// only spot checking a few countries as OOM issues are preventing testing all

	public void testArgentina() {
		doCountryTest("Argentina", "ARG", R.drawable.ic_flag_ar);
	}

	public void testCanada() {
		doCountryTest("Canada", "CAN", R.drawable.ic_flag_ca);
	}

	public void testHongKong() {
		doCountryTest("Hong Kong", "HKG", R.drawable.ic_flag_hk);
	}

	public void testKorea() {
		doCountryTest("Korea", "KOR", R.drawable.ic_flag_kr);
	}

	public void testUnitedKingdom() {
		doCountryTest("United Kingdom", "GBR", R.drawable.ic_flag_gb);
	}

	private void doCountryTest(String countryName, String country3LetterCode, @DrawableRes int flagResId) {
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
		assertViewWithTextIsDisplayed(R.id.country, country3LetterCode);
		assertTextViewHasCompoundDrawableFlag(R.id.country, flagResId);
	}

	private static void assertTextViewHasCompoundDrawableFlag(@IdRes int viewId, @DrawableRes int drawableId) {
		onView(withId(viewId)).check(matches(withCompoundDrawableInLayer(drawableId, 0)));
	}

	private static Matcher<View> withCompoundDrawableInLayer(final @DrawableRes int resId, final int layerIndex) {
		return new BoundedMatcher<View, TextView>(TextView.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("has compound drawable layer with resource " + resId);
			}

			@Override
			public boolean matchesSafely(TextView textView) {
				for (Drawable drawable : textView.getCompoundDrawables()) {
					if (drawable instanceof LayerDrawable) {
						drawable = ((LayerDrawable) drawable).getDrawable(layerIndex);
						if (sameBitmap(textView.getContext(), drawable, resId)) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}

}
