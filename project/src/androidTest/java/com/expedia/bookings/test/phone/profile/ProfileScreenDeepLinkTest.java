package com.expedia.bookings.test.phone.profile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountSettingsActivity;
import com.expedia.bookings.data.User;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ProfileScreen;

import static android.support.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class ProfileScreenDeepLinkTest {

	Context context;

	@Rule
	public IntentsTestRule intentRule = new IntentsTestRule(AccountSettingsActivity.class);

	@Before
	public void setup() {
		context = intentRule.getActivity();
		NewLaunchScreen.accountButton().perform(click());
	}

	@Test
	public void testSignedInDeepLinkAsLoggedIn() {
		if (!User.isLoggedIn(context)) {
			signInAsUser("singlecard@mobiata.com");
		}
		Intent intent = new Intent();
		Uri deepLinkText = Uri.parse("expda://signIn");
		intent.setData(deepLinkText);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
				"com.expedia.bookings.activity.DeepLinkRouterActivity"));
		context.startActivity(intent);
		ProfileScreen.waitForAccountPagerDisplay();
		EspressoUtils.assertViewIsDisplayed(R.id.scroll_container);
	}

	@Test
	public void testSignedInDeepLinkAsLoggedOut() {
		if (User.isLoggedIn(context)) {
			signOutUser();
		}
		Intent intent = new Intent();
		Uri deepLinkText = Uri.parse("expda://signIn");
		intent.setData(deepLinkText);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
			"com.expedia.bookings.activity.DeepLinkRouterActivity"));
		context.startActivity(intent);
		ProfileScreen.waitForAccountViewDisplay();
		EspressoUtils.assertViewIsDisplayed(R.id.account_view);
	}

	private void signInAsUser(String email) {
		ProfileScreen.clickSignInButton();
		LogInScreen.typeTextEmailEditText(email);
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
	}

	private void signOutUser() {
		ProfileScreen.clickSignOutButton();
	}

}
