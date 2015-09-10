package com.expedia.bookings.test.phone.launch;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;

public class HotelsABTest extends PhoneTestCase {

	@Override
	public void runTest() throws Throwable {
		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();
		if (testMethodName.contains("testBucketedHotels")) {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		else {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
				AbacusUtils.DefaultVariate.CONTROL.ordinal());
		}
		super.runTest();
	}

	public void testControlHotels() {
		LaunchScreen.launchHotels();
		// Assert that old hotels search screen is launched.
		EspressoUtils.assertViewIsDisplayed(R.id.calendar_button_container);
	}

	public void testBucketedHotels() {
		LaunchScreen.launchHotels();
		// Assert that materials hotels search screen is launched.
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_presenter);
	}

}
