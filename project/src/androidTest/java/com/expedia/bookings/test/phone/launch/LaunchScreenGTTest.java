package com.expedia.bookings.test.phone.launch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.rules.PointOfSaleRule;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class LaunchScreenGTTest {

	@Rule
	public ActivityTestRule<PhoneLaunchActivity> activity = new ActivityTestRule<>(PhoneLaunchActivity.class);

	@Rule
	public PointOfSaleRule pos = new PointOfSaleRule() {
		@Override
		protected void beforeTest() {
			if (PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)) {
				AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppSplitGTandActivities,
					AbacusUtils.DefaultVariate.BUCKETED.ordinal());
			}
		}
	};

	@Test
	public void testGTSupport() {
		boolean carsEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.CARS);
		boolean lxEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.LX);

		LaunchScreen.fiveLOBDoubleRowWidget().check(matches((carsEnabled && lxEnabled) ? withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE) : withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
		LaunchScreen.fiveLOBDoubleRowWidget()
			.check(matches((carsEnabled && lxEnabled) ? hasDescendant(withId(R.id.transport_button))
				: hasDescendant(withId(R.id.activities_button))));
	}
}
