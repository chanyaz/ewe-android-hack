package com.expedia.bookings.test.ui.phone.tests.ui;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 11/17/14.
 */

@RunWith(AndroidJUnit4.class)
public class LaunchScreenTest {

	private static final String TAG = LaunchScreenTest.class.getName();

	/*
	*  #164 eb_tp test for launcher screen general UI elements in phone.
	*/
	@Rule
	public ActivityTestRule<PhoneLaunchActivity> activityRule = new ActivityTestRule<>(PhoneLaunchActivity.class);

	@Rule
	public TestRule posRule = new TestRule() {
		@Override
		public Statement apply(final Statement base, org.junit.runner.Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					PointOfSaleId[] pointsOfSale = new PointOfSaleId[] {
						PointOfSaleId.ARGENTINA,
						PointOfSaleId.AUSTRALIA,
						PointOfSaleId.AUSTRIA,
						PointOfSaleId.BELGIUM,
						PointOfSaleId.BRAZIL,
						PointOfSaleId.CANADA,
						PointOfSaleId.DENMARK,
						PointOfSaleId.FRANCE,
						PointOfSaleId.GERMANY,
						PointOfSaleId.HONG_KONG,
						PointOfSaleId.INDIA,
						PointOfSaleId.INDONESIA,
						PointOfSaleId.IRELAND,
						PointOfSaleId.ITALY,
						PointOfSaleId.JAPAN,
						PointOfSaleId.SOUTH_KOREA,
						PointOfSaleId.MALAYSIA,
						PointOfSaleId.MEXICO,
						PointOfSaleId.NETHERLANDS,
						PointOfSaleId.NEW_ZEALND,
						PointOfSaleId.NORWAY,
						PointOfSaleId.PHILIPPINES,
						PointOfSaleId.SINGAPORE,
						PointOfSaleId.SPAIN,
						PointOfSaleId.SWEDEN,
						PointOfSaleId.TAIWAN,
						PointOfSaleId.THAILAND,
						PointOfSaleId.UNITED_KINGDOM,
						PointOfSaleId.UNITED_STATES,
						PointOfSaleId.VIETNAM,
					};

					for (PointOfSaleId pos : pointsOfSale) {
						setPOS(pos);
						base.evaluate();
					}
				}
			};
		}
	};

	public void setPOS(PointOfSaleId pos) {
		Common.enterLog(TAG, "POS Set:" + pos.toString());
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.PointOfSaleKey,
			String.valueOf(pos.getId()));
		PointOfSale.onPointOfSaleChanged(InstrumentationRegistry.getTargetContext());
	}


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


	@After
	public void tearDown() {
		setPOS(PointOfSaleId.UNITED_STATES);
	}
}

