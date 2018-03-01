package com.expedia.bookings.test.phone.profile;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.account.Config;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.ProfileScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertIntentFiredToStartActivityWithExtra;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class ProfileScreenTest {

	@Rule
	public IntentsTestRule<PhoneLaunchActivity> intentRule = new IntentsTestRule<>(PhoneLaunchActivity.class);

	@Before
	public void stubAllExternalIntents() {
		// By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
		// every test run. In this case all external Intents will be blocked.
		intending(anyIntent())
			.respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
	}

	@Test
	public void clearPrivateData() {
		LaunchScreen.accountButton().perform(click());

		ProfileScreen.clickClearPrivateData();
		assertViewWithTextIsDisplayed(R.string.dialog_clear_private_data_title);
		assertViewWithTextIsDisplayed(R.string.dialog_clear_private_data_msg);
		ProfileScreen.clickCancel();
	}

	@Test
	public void signInButtons() {
		LaunchScreen.accountButton().perform(click());

		ProfileScreen.clickSignInButton();
		assertIntentFiredToStartSignInWithInitialState(Config.InitialState.SignIn);

	}

	@Test
	public void changeCountry() {
		Context context = intentRule.getActivity();
		LaunchScreen.accountButton().perform(click());

		List<PointOfSale> poses = PointOfSale.getAllPointsOfSale(context);
		for (PointOfSale pos : poses) {
			ProfileScreen.clickCountry();
			ProfileScreen.clickCountryInList(context.getString(pos.getCountryNameResId()));
			assertViewWithTextIsDisplayed(R.string.dialog_clear_private_data_title);
			assertViewWithTextIsDisplayed(R.string.dialog_clear_private_data_msg);
			ProfileScreen.clickOK();

			String country = context.getString(pos.getCountryNameResId());
			String url = pos.getUrl();
			onView(withText(country + " - " + url)).perform(scrollTo()).check(matches(isDisplayed()));

			if (pos.showAtolInfo()) {
				ProfileScreen.atolInformation().perform(scrollTo(), click());
				assertIntentFiredToStartWebViewWithRawHtmlContaining(
						context.getString(R.string.lawyer_label_atol_long_message));
			}
			else {
				ProfileScreen.atolInformation().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
			}

			if (pos.displayFlightDropDownRoutes()) {
				// add a little delay to make sure that the routes finish downloading; help reduce test flakiness
				Common.delay(3);
			}
		}

		ProfileScreen.clickCountry();
		ProfileScreen.clickCountryInList("United States");
		ProfileScreen.clickOK();
	}

	private static void assertIntentFiredToStartWebViewWithRawHtmlContaining(String html) {
		assertIntentFiredToStartActivityWithExtra(WebViewActivity.class, equalTo("ARG_HTML_DATA"), containsString(html));
	}

	private static void assertIntentFiredToStartSignInWithInitialState(Config.InitialState initialState) {
		intended(allOf(
				hasComponent(AccountLibActivity.class.getName()),
				hasInitialState(initialState)
		));

	}

	private static Matcher<Intent> hasInitialState(final Config.InitialState initialState) {
		return new TypeSafeMatcher<Intent>() {
			@Override
			protected boolean matchesSafely(Intent intent) {
				Bundle bundle = intent.getBundleExtra("ARG_BUNDLE");
				return bundle != null && initialState.name().equals(bundle.getString("ARG_INITIAL_STATE"));
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has initialState: " + initialState.name());
			}
		};
	}
}
