package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackagePriceChangeTest extends PackageTestCase {

	public void testPackagePriceChangeUp() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.searchButton().perform(click());
		Common.delay(1);

		Common.delay(1);

		HotelScreen.selectHotel("Price Change");
		Common.delay(1);

		PackageScreen.selectRoom();
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);


		//price change up
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertPriceChangeUp();
	}

	public void testPackagePriceChangeDown() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.searchButton().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Price Change");
		Common.delay(1);

		PackageScreen.selectRoom();
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		//price change down
		PackageScreen.selectFlight(1);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertPriceChangeDown();
	}

	private void assertPriceChangeUp() {
		onView(allOf(withId(R.id.price_change_text), withText("Price changed from $749.52"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_price), withText("$751.52"))).check(matches(isDisplayed()));
	}

	private void assertPriceChangeDown() {
		onView(allOf(withId(R.id.price_change_text), withText("Price dropped from $749.52"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_price), withText("$651.52"))).check(matches(isDisplayed()));
	}

}
