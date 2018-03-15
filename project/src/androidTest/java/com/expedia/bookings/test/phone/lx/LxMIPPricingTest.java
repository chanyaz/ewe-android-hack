package com.expedia.bookings.test.phone.lx;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.pagemodels.trips.TripsScreen;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;

import org.joda.time.LocalDate;
import org.junit.Test;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class LxMIPPricingTest extends PhoneTestCase {

	public LxMIPPricingTest() {
		super(PhoneLaunchActivity.class);
	}

	@Test
	public void testMipOnSRP() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidLXMIP, AbacusVariant.BUCKETED.getValue());
		loginAndNavigateToActivites();
		searchListDisplayed(true);
		LXScreen.getMipBanner().check(matches(ViewMatchers.isDisplayed()));
		LXScreen.getTile("happy", R.id.lx_search_results_list)
				.check(matches(
						hasDescendant(allOf(withText("Member Pricing"), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
		LXScreen.getTile("happy", R.id.lx_search_results_list)
			.check(matches(
				hasDescendant(allOf(withId(R.id.mip_srp_tile_image), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))));
	}

	@Test
	public void testMipOnSRPHiddenWhenAbacusOff() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidLXMIP, AbacusVariant.CONTROL.getValue());
		loginAndNavigateToActivites();
		searchListDisplayed(true);
		LXScreen.getMipBanner().check(matches(not(ViewMatchers.isDisplayed())));
		LXScreen.getTile("happy", R.id.lx_search_results_list)
			.check(matches(
				hasDescendant(allOf(withId(R.id.mip_srp_tile_image), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
	}

	@Test
	public void testMipOnInfosite() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidLXMIP, AbacusVariant.BUCKETED.getValue());
		loginAndNavigateToActivites();
		searchListDisplayed(true);
		LXScreen.getTile("happy", R.id.lx_search_results_list).perform(click());
		LXScreen.getMipInfositeBanner().check(matches(ViewMatchers.isDisplayed()));
		LXScreen.getMipInfositeBanner().check(matches(
			hasDescendant(allOf(withText("Book now and get up to 7% off"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))));
	}

	@Test
	public void testMipOnInfositeHiddenWhenAbacusOff() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidLXMIP, AbacusVariant.CONTROL.getValue());
		loginAndNavigateToActivites();
		searchListDisplayed(true);
		LXScreen.getTile("happy", R.id.lx_search_results_list).perform(click());
		LXScreen.getMipInfositeBanner().check(matches(not(ViewMatchers.isDisplayed())));
		LXScreen.getMipInfositeBanner().check(matches(
			hasDescendant(allOf(withId(R.id.mip_infosite_image), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
		LXScreen.getMipInfositeBanner().check(matches(
			hasDescendant(allOf(withId(R.id.mip_infosite_discount), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
	}

	private void searchListDisplayed(boolean firstLaunch) throws Throwable {
		String expectedLocationDisplayName = "SanFranciscoMip";
		if (!firstLaunch) {
			LXScreen.locationCardView().perform(click());
		}
		LXScreen.location().perform(typeText("SanFra"));
		LXScreen.selectLocationForLxMip(expectedLocationDisplayName);
		if (!firstLaunch) {
			LXScreen.selectDateButton().perform(click());
		}
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		LXScreen.waitForSearchListDisplayed();
	}

	private void loginAndNavigateToActivites() {
		LaunchScreen.tripsButton().perform(click());

		TripsScreen.clickOnLogInButton();

		LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();
		Common.delay(2);

		LaunchScreen.shopButton().perform(click());
		LaunchScreen.waitForLOBHeaderToBeDisplayed();
		Common.delay(1);
		LaunchScreen.activitiesLaunchButton().perform(click());
	}
}

