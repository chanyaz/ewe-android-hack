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
	public void testCarLXSupport() {
		boolean carsEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.CARS);
		boolean lxEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.LX);

		LaunchScreen.lobSingleRowWidget()
			.check(matches(((carsEnabled && lxEnabled) ? not(isDisplayed()) : isDisplayed())));
		LaunchScreen.lobDoubleRowWidget()
			.check(matches((!(carsEnabled && lxEnabled) ? not(isDisplayed()) : isDisplayed())));
		LaunchScreen.carLaunchButtonInSingleRow()
			.check(matches(((carsEnabled && !lxEnabled) ? isDisplayed() : not(isDisplayed()))));
		LaunchScreen.lxLaunchButtonInSingleRow()
			.check(matches(((!carsEnabled && lxEnabled) ? isDisplayed() : not(isDisplayed()))));
		LaunchScreen.carLaunchButtonInDoubleRow()
			.check(matches(((carsEnabled && lxEnabled) ? isDisplayed() : not(isDisplayed()))));
		LaunchScreen.lxLaunchButtonInDoubleRow()
			.check(matches(((carsEnabled && lxEnabled) ? isDisplayed() : not(isDisplayed()))));
	}
}

