package com.expedia.bookings.test.phone.launch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.rules.PointOfSaleRule;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class LaunchScreenTest {

	/*
	*  #164 eb_tp test for launcher screen general UI elements in phone.
	*/
	@Rule
	public ActivityTestRule<PhoneLaunchActivity> activity = new ActivityTestRule<>(PhoneLaunchActivity.class);

	@Rule
	public PointOfSaleRule pos = new PointOfSaleRule();

	@Test
	public void testCarLxGtSupport() {
		boolean carsEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.CARS);
		boolean lxEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.LX);
		boolean gtEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.TRANSPORT);

		boolean lobSingleRowShouldDisplay = !(carsEnabled && lxEnabled);
		boolean lobDoubleRowShouldDisplay = (carsEnabled && lxEnabled && !gtEnabled);
		boolean lobDoubleRowFiveShouldDisplay = (carsEnabled && lxEnabled && gtEnabled);

		LaunchScreen.lobSingleRowWidget()
			.check(matches(lobSingleRowShouldDisplay ? isDisplayed() : not(isDisplayed())));
		LaunchScreen.lobDoubleRowWidget()
			.check(matches(lobDoubleRowShouldDisplay ? isDisplayed() : not(isDisplayed())));
		LaunchScreen.fiveLOBDoubleRowWidget()
			.check(matches(lobDoubleRowFiveShouldDisplay ? isDisplayed() : not(isDisplayed())));

		LaunchScreen.carLaunchButtonInSingleRow()
			.check(matches((lobSingleRowShouldDisplay && carsEnabled) ? isDisplayed() : not(isDisplayed())));
		LaunchScreen.lxLaunchButtonInSingleRow()
			.check(matches((lobSingleRowShouldDisplay && lxEnabled) ? isDisplayed() : not(isDisplayed())));

		LaunchScreen.carLaunchButtonInDoubleRow()
			.check(matches(lobDoubleRowShouldDisplay ? isDisplayed() : not(isDisplayed())));
		LaunchScreen.lxLaunchButtonInDoubleRow()
			.check(matches(lobDoubleRowShouldDisplay ? isDisplayed() : not(isDisplayed())));

		LaunchScreen.carLaunchButtonInDoubleRowFive()
			.check(matches(lobDoubleRowFiveShouldDisplay ? isDisplayed() : not(isDisplayed())));
		LaunchScreen.lxLaunchButtonInDoubleRowFive()
			.check(matches(lobDoubleRowFiveShouldDisplay ? isDisplayed() : not(isDisplayed())));
		LaunchScreen.gtLaunchButtonInDoubleRowFive()
			.check(matches(lobDoubleRowFiveShouldDisplay ? isDisplayed() : not(isDisplayed())));
	}
}

