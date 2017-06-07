package com.expedia.bookings.test.phone.itin;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.TripsScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public class ItinPhoneRefreshErrorTest extends PhoneTestCase {

	@Test
	public void testErrorItineraries() throws Throwable {

		NewLaunchScreen.tripsButton().perform(click());

		TripsScreen.clickOnLogInButton();

		LogInScreen.typeTextEmailEditText("trip_error@mobiata.com");
		LogInScreen.typeTextPasswordEditText("password");
		LogInScreen.clickOnLoginButton();

		TripsScreen.refreshTripsButton().check(matches(isDisplayed()));
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.no_upcoming_trips, "We were unable to fetch your itineraries at this time. Please try again.");
	}
}
