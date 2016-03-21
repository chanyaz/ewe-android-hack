package com.expedia.bookings.test.phone.flights;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class FlightSupportPOSTest extends PhoneTestCase {

	public void launchFlights() throws Throwable {
		LaunchScreen.launchFlights();
		onView(allOf(withId(R.id.message_text_view), withText("Sorry, but mobile flight booking is not yet available in your location.")))
			.check(matches(isDisplayed()));
	}

	public void testIndiaFlights() throws Throwable {
		Common.setPOS(PointOfSaleId.INDIA);
		Common.delay(1);
		launchFlights();
	}

	public void testArgentinaFlights() throws Throwable {
		Common.setPOS(PointOfSaleId.ARGENTINA);
		Common.delay(1);
		launchFlights();
	}

	public void testVietnamFlights() throws Throwable {
		Common.setPOS(PointOfSaleId.VIETNAM);
		Common.delay(1);
		launchFlights();
	}
}
