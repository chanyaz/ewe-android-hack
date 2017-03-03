package com.expedia.bookings.test.phone.profile;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.DrawableRes;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ProfileScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.sameBitmap;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertContainsImageDrawable;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsGone;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
public class ProfileScreenLoggedInTest {

	@Rule
	public ActivityTestRule<NewPhoneLaunchActivity> activityRule = new ActivityTestRule<>(NewPhoneLaunchActivity.class, true);

	@Before
	public void setup() {
		NewLaunchScreen.accountButton().perform(click());
	}

	@Test
	public void notRewardsMember() {
		signInAsUser("qa-ehcc@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Mock Web Server");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "qa-ehcc@mobiata.com");
		assertViewIsGone(R.id.toolbar_loyalty_tier_text);
		assertViewIsGone(R.id.available_points);
		assertViewIsGone(R.id.pending_points);
		getFirstRowCountry().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		assertViewIsGone(R.id.points_monetary_value_label);
		assertViewIsGone(R.id.points_monetary_value);
		assertViewIsGone(R.id.currency_label);
		assertViewIsGone(R.id.currency);
	}

	@Test
	public void blueStatus() {
		signInAsUser("singlecard@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "single card");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "singlecard@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.reward_base_tier_name_short);
		assertViewWithTextIsDisplayed(R.id.available_points, "1,802");
		assertViewIsGone(R.id.pending_points); // 0 pending points == hide the view
		ViewInteraction countryView = getFirstRowCountry();
		countryView.check(matches(withText("USA")));
		assertContainsImageDrawable(R.id.flagView, R.drawable.ic_flag_us_icon);
		assertViewIsGone(R.id.points_monetary_value_label);
		assertViewIsGone(R.id.points_monetary_value);
		assertViewIsGone(R.id.currency_label);
		assertViewIsGone(R.id.currency);
	}

	@Test
	public void silverStatus() {
		signInAsUser("silverstatus@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Silver Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "silverstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.reward_middle_tier_name_short);
		assertViewWithTextIsDisplayed(R.id.available_points, "22,996");
		assertViewWithTextIsDisplayed(R.id.pending_points, "965 pending");
		ViewInteraction countryView = getSecondRowCountry();
		countryView.check(matches(withText("USA")));
		assertContainsImageDrawable(R.id.flagView, R.drawable.ic_flag_us_icon);
		assertViewWithTextIsDisplayed(R.id.currency_label, "Currency");
		assertViewWithTextIsDisplayed(R.id.currency, "USD");
		assertViewWithTextIsDisplayed(R.id.points_monetary_value_label, "Points value");
		assertViewWithTextIsDisplayed(R.id.points_monetary_value, "$220,597.75");
	}

	@Test
	public void goldStatus() {
		signInAsUser("goldstatus@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Gold Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "goldstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.reward_top_tier_name_short);
		assertViewWithTextIsDisplayed(R.id.available_points, "54,206");
		assertViewWithTextIsDisplayed(R.id.pending_points, "5,601 pending");
		ViewInteraction countryView = getSecondRowCountry();
		countryView.check(matches(withText("USA")));
		assertContainsImageDrawable(R.id.flagView, R.drawable.ic_flag_us_icon);
		assertViewWithTextIsDisplayed(R.id.currency_label, "Currency");
		assertViewWithTextIsDisplayed(R.id.currency, "USD");
		assertViewWithTextIsDisplayed(R.id.points_monetary_value_label, "Points value");
		assertViewWithTextIsDisplayed(R.id.points_monetary_value, "$42.00");
	}

	@Test
	public void testOldV1SignInResponse() {
		// Tests old sign in response before loyalty points refactor
		signInAsUser("goldstatus_v1@mobiata.com");

		assertViewWithTextIsDisplayed(R.id.toolbar_name, "Gold Status");
		assertViewWithTextIsDisplayed(R.id.toolbar_email, "goldstatus@mobiata.com");
		assertViewWithTextIsDisplayed(R.id.toolbar_loyalty_tier_text, R.string.reward_top_tier_name_short);
		assertViewWithTextIsDisplayed(R.id.available_points, "54,206");
		assertViewWithTextIsDisplayed(R.id.pending_points, "5,601 pending");
		ViewInteraction countryView = getSecondRowCountry();
		countryView.check(matches(withText("USA")));
		assertContainsImageDrawable(R.id.flagView, R.drawable.ic_flag_us_icon);
	}

	// only spot checking a few countries as OOM issues are preventing testing all

	@Test
	public void argentina() {
		doCountryTest("Argentina", "ARG", R.drawable.ic_flag_ar_icon);
	}

	@Test
	public void canada() {
		doCountryTest("Canada", "CAN", R.drawable.ic_flag_ca_icon);
	}

	@Test
	public void hongKong() {
		doCountryTest("Hong Kong", "HKG", R.drawable.ic_flag_hk_icon);
	}

	@Test
	public void korea() {
		doCountryTest("Korea", "KOR", R.drawable.ic_flag_kr_icon);
	}

	@Test
	public void unitedKingdom() {
		doCountryTest("United Kingdom", "GBR", R.drawable.ic_flag_uk_icon);
	}

	private void doCountryTest(String countryName, String country3LetterCode, @DrawableRes int flagResId) {
		switchCountry(countryName);
		onView(withId(R.id.sign_in_button)).perform(scrollTo());
		signInAsUser("goldstatus@mobiata.com");

		ViewInteraction countryView = getSecondRowCountry();
		countryView.check(matches(withText(country3LetterCode)));
		assertContainsImageDrawable(R.id.flagView, flagResId);
	}

	private void switchCountry(String countryName) {
		ProfileScreen.clickCountry();
		ProfileScreen.clickCountryInList(countryName);
		ProfileScreen.clickOK();
	}

	private void signInAsUser(String email) {
		ProfileScreen.clickSignInButton();
		LogInScreen.typeTextEmailEditText(email);
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
	}

	private static void assertTextViewHasCompoundDrawableFlag(ViewInteraction viewInteraction, @DrawableRes int drawableId) {
		viewInteraction.check(matches(withCompoundDrawableInLayer(drawableId, 0)));
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

	private ViewInteraction getFirstRowCountry() {
		return onView(allOf(isDescendantOfA(withId(R.id.first_row_country)), withId(R.id.country)));
	}

	private ViewInteraction getSecondRowCountry() {
		return onView(allOf(isDescendantOfA(withId(R.id.second_row_country)), withId(R.id.country)));
	}
}
