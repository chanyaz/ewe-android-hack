package com.expedia.bookings.test.phone.launch;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.ui.NewPhoneLaunchActivity;
import com.mobiata.android.Log;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class NewLaunchScreenTest extends PhoneTestCase {

	public NewLaunchScreenTest() {
		super(NewPhoneLaunchActivity.class);
	}

	private static final String TAG = NewLaunchScreenTest.class.getName();

	public void testGeneralUIElements() throws Throwable {
		LaunchScreen.tripsButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.login_button);
		Log.v(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.shopButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.lobView);
		Log.v(TAG, "Shop button on Launch screen is displayed ");

		LaunchScreen.accountButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.sign_in_button);
		Log.v(TAG, "Account button on Launch screen is displayed ");

	}

	public void testScrollingBehaviourOnShopTab() throws Throwable {
		LaunchScreen.shopButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.lobView);
		Log.v(TAG, "Lob view is displayed ");

		EspressoUtils.assertViewIsNotDisplayed(R.id.fab);
		EspressoUtils.assertViewIsNotDisplayed(R.id.darkness);
		Log.v(TAG, "fab button and darkness is hidden");

		LaunchScreen.newPhoneLaunchWidget().perform(ViewActions.swipeUp());
		EspressoUtils.assertViewIsNotDisplayed(R.id.lobView);
		Log.v(TAG, "Lob view is not displayed ");
		EspressoUtils.assertViewIsDisplayed(R.id.fab);

		onView(withId(R.id.fab)).perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.lobView);
		EspressoUtils.assertViewIsDisplayed(R.id.darkness);

		// clicking on the fab button agian will hide lob and darkness
		onView(withId(R.id.fab)).perform(click());
		EspressoUtils.assertViewIsNotDisplayed(R.id.lobView);
		EspressoUtils.assertViewIsNotDisplayed(R.id.darkness);

		onView(withId(R.id.fab)).perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.lobView);
		EspressoUtils.assertViewIsDisplayed(R.id.darkness);

		// clicking on any other part of the screen will hide the lob and darkness
		onView(withId(R.id.darkness)).perform(click());
		EspressoUtils.assertViewIsNotDisplayed(R.id.lobView);
		EspressoUtils.assertViewIsNotDisplayed(R.id.darkness);

	}

}
