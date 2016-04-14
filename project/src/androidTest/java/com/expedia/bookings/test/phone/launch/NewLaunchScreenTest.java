package com.expedia.bookings.test.phone.launch;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.ui.NewPhoneLaunchActivity;
import com.mobiata.android.Log;

import static android.support.test.espresso.action.ViewActions.click;

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

}
