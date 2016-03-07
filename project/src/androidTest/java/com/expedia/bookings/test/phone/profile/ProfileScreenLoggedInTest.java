package com.expedia.bookings.test.phone.profile;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountSettingsActivity;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ProfileScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.CustomMatchers.sameBitmap;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsGone;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;

@RunWith(AndroidJUnit4.class)
public class ProfileScreenLoggedInTest {

	@Rule
	public ActivityTestRule<AccountSettingsActivity> activityRule = new ActivityTestRule<>(AccountSettingsActivity.class, true);

	@Test
	public void notRewardsMember() {
		signInAsUser("qa-ehcc@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Mock Web Server");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "qa-ehcc@mobiata.com");
		assertViewIsGone(R.id.toolbar_loyalty_tier_text);
		assertViewIsGone(R.id.available_points);
		assertViewIsGone(R.id.pending_points);
		assertViewIsGone(R.id.country);
	}

	@Test
	public void blueStatus() {
		signInAsUser("singlecard@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "single card");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "singlecard@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.plus_blue);
		assertViewWithTextIsDisplayed(R.id.available_points, "1,802");
		assertViewWithTextIsDisplayed(R.id.pending_points, "0 pending");
		assertViewWithTextIsDisplayed(R.id.country, "USA");
		assertTextViewHasCompoundDrawableFlag(R.id.country, R.drawable.ic_flag_us);
	}

	@Test
	public void silverStatus() {
		signInAsUser("silverstatus@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Silver Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "silverstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.plus_silver);
		assertViewWithTextIsDisplayed(R.id.available_points, "22,996");
		assertViewWithTextIsDisplayed(R.id.pending_points, "965 pending");
		assertViewWithTextIsDisplayed(R.id.country, "USA");
		assertTextViewHasCompoundDrawableFlag(R.id.country, R.drawable.ic_flag_us);
	}

	@Test
	public void goldStatus() {
		signInAsUser("goldstatus@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Gold Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "goldstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.plus_gold);
		assertViewWithTextIsDisplayed(R.id.available_points, "54,206");
		assertViewWithTextIsDisplayed(R.id.pending_points, "5,601 pending");
		assertViewWithTextIsDisplayed(R.id.country, "USA");
		assertTextViewHasCompoundDrawableFlag(R.id.country, R.drawable.ic_flag_us);
	}

	// only spot checking a few countries as OOM issues are preventing testing all

	@Test
	public void argentina() {
		doCountryTest("Argentina", "ARG", R.drawable.ic_flag_ar);
	}

	@Test
	public void canada() {
		doCountryTest("Canada", "CAN", R.drawable.ic_flag_ca);
	}

	@Test
	public void hongKong() {
		doCountryTest("Hong Kong", "HKG", R.drawable.ic_flag_hk);
	}

	@Test
	public void korea() {
		doCountryTest("Korea", "KOR", R.drawable.ic_flag_kr);
	}

	@Test
	public void unitedKingdom() {
		doCountryTest("United Kingdom", "GBR", R.drawable.ic_flag_gb);
	}

	private void doCountryTest(String countryName, String country3LetterCode, @DrawableRes int flagResId) {
		ProfileScreen.clickCountry();
		ProfileScreen.clickCountryInList(countryName);
		ProfileScreen.clickOK();

		signInAsUser("goldstatus@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.country, country3LetterCode);
		assertTextViewHasCompoundDrawableFlag(R.id.country, flagResId);
	}

	private void signInAsUser(String email) {
		ProfileScreen.clickSignInButton();
		LogInScreen.typeTextEmailEditText(email);
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
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
