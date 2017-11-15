
package com.expedia.bookings.test.phone.lx;

//import android.support.test.espresso.matcher.ViewMatchers;
//
//import com.expedia.bookings.R;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
//import com.expedia.bookings.test.espresso.AbacusTestUtils;
//import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
//import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
//import com.expedia.bookings.test.pagemodels.common.LogInScreen;
//import com.expedia.bookings.test.pagemodels.common.TripsScreen;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;

import org.joda.time.LocalDate;
//import org.junit.Test;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
//import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;
//import static com.expedia.bookings.data.abacus.AbacusUtils.EBAndroidLXMOD;
//import static org.hamcrest.Matchers.allOf;

public class LXMemberPricingTest extends PhoneTestCase {

	public LXMemberPricingTest() {
		super(PhoneLaunchActivity.class);
	}


	//Removing the tests until the feature gets completely production ready

//	@Test
//	public void testModMessagingVisible() throws Throwable {
//		AbacusTestUtils.bucketTests(EBAndroidLXMOD);
//		LaunchScreen.tripsButton().perform(click());
//
//		TripsScreen.clickOnLogInButton();
//		LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com");
//		LogInScreen.typeTextPasswordEditText("password");
//		LogInScreen.clickOnLoginButton();
//		Common.delay(2);
//
//		LaunchScreen.shopButton().perform(click());
//		LaunchScreen.waitForLOBHeaderToBeDisplayed();
//		Common.delay(1);
//		LaunchScreen.activitiesLaunchButton().perform(click());
//		searchListDisplayed(true);
//
//		LXScreen.getTile("happy", R.id.lx_search_results_list)
//				.check(matches(
//						hasDescendant(allOf(withText("Member Pricing"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))));
//
//		LXScreen.getTile("error_create_trip", R.id.lx_search_results_list)
//				.check(matches(
//						hasDescendant(allOf(withText("Member Pricing"), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
//	}

//	@Test
//	public void testModMessagingHiddenWhenAbacusOff() throws Throwable {
//		LaunchScreen.tripsButton().perform(click());
//
//		TripsScreen.clickOnLogInButton();
//
//		LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com");
//		LogInScreen.typeTextPasswordEditText("password");
//		LogInScreen.clickOnLoginButton();
//		Common.delay(2);
//
//		LaunchScreen.shopButton().perform(click());
//		LaunchScreen.waitForLOBHeaderToBeDisplayed();
//		Common.delay(1);
//		LaunchScreen.activitiesLaunchButton().perform(click());
//		searchListDisplayed(true);
//
//		LXScreen.getTile("happy", R.id.lx_search_results_list)
//				.check(matches(
//						hasDescendant(allOf(withText("Member Pricing"), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
//	}

//	@Test
//	public void testModMessagingHiddenWhenNonLoggedIn() throws Throwable {
//
//		LaunchScreen.waitForLOBHeaderToBeDisplayed();
//		Common.delay(2);
//		LaunchScreen.activitiesLaunchButton().perform(click());
//		searchListDisplayed(true);
//
//		LXScreen.getTile("happy", R.id.lx_search_results_list)
//				.check(matches(
//						hasDescendant(allOf(withText("Member Pricing"), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
//	}

	private void searchListDisplayed(boolean firstLaunch) throws Throwable {
		String expectedLocationDisplayName = "San Francisco, CA";
		if (!firstLaunch) {
			LXScreen.locationCardView().perform(click());
		}
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation(expectedLocationDisplayName);
		if (!firstLaunch) {
			LXScreen.selectDateButton().perform(click());
		}
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		LXScreen.waitForSearchListDisplayed();
	}
}
