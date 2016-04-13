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
		/**
		 * TODO right now as account screen using Trip screen that reason we getting multiple views for log in button.
		 * Have to update once we have correct account screen.
		 *
		 */
		EspressoUtils.assertMultipleViewsWithSameIdIsDisplayed(R.id.login_button);
		Log.v(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.shopButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.lobView);
		Log.v(TAG, "Shop button on Launch screen is displayed ");

		//TODO check the account button click
	}

}
