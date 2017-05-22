package com.expedia.bookings.test.phone.flights;

import org.junit.Test;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class FlightSupportPOSTest extends PhoneTestCase {

	private void launchFlights() throws Throwable {
		NewLaunchScreen.flightLaunchButton().perform(click());
		onView(withText("Sorry, but mobile flight booking is not yet available in your location.")).check(matches(isDisplayed()));
		onView(withId(android.R.id.button1)).perform(click());
	}

	@Test
	public void testIndiaFlights() throws Throwable {
		Common.setPOS(PointOfSaleId.INDIA);
		Common.delay(1);
		launchFlights();
	}

	@Test
	public void testArgentinaFlights() throws Throwable {
		Common.setPOS(PointOfSaleId.ARGENTINA);
		Common.delay(1);
		launchFlights();
	}

	@Test
	public void testVietnamFlights() throws Throwable {
		Common.setPOS(PointOfSaleId.VIETNAM);
		Common.delay(1);
		launchFlights();
	}
}
