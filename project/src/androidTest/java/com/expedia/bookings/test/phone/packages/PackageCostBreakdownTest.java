package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static com.expedia.bookings.test.espresso.CustomMatchers.withTextColor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class PackageCostBreakdownTest extends PackageTestCase {

	@Test
	public void testPackageCostBreakdown() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		Common.delay(1);

		Common.delay(1);

		assertBundleTotalIconVisibility("View your bundle", false);
		HotelResultsScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		HotelInfoSiteScreen.bookFirstRoom();
		Common.delay(1);

		Common.delay(1);
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		Common.delay(1);
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundleTotalIconVisibility("Bundle total", true);

		onView(withId(R.id.bundle_total_text)).perform(click());
		Common.delay(1);
		assertCostBreakdownValues();
	}

	private void assertBundleTotalIconVisibility(String labelText, boolean shown) {
		onView(allOf(withId(R.id.bundle_total_text), withText(labelText))).check(matches(isDisplayed()));
		if (shown) {
			onView(allOf(withId(R.id.bundle_total_text), withText(labelText))).check(matches(withCompoundDrawable(R.drawable.ic_checkout_info)));
		}
		else {
			onView(allOf(withId(R.id.bundle_total_text), withText(labelText))).check(matches(not(withCompoundDrawable(R.drawable.ic_checkout_info))));
		}
	}

	private void assertCostBreakdownValues() {
		onView(allOf(withText("Hotel + Flights"), hasSibling(withText("$2,595.12")))).check(matches(isDisplayed()));
		onView(allOf(withText("1 room, 2 nights, 1 guest"), hasSibling(withText("")))).check(matches(isDisplayed()));
		onView(allOf(withText("Taxes & Fees Included ($278.12)"), hasSibling(withText("")))).check(matches(isDisplayed()));

		//Check savings text and color
		onView(allOf(withText("Bundle Discount"), hasSibling(withText("-$56.50")))).check(matches(isDisplayed()));
		onView(allOf(withText("Bundle Discount"))).check(matches(withTextColor("#0f7800")));
		onView(allOf(withText("-$56.50"))).check(matches(withTextColor("#0f7800")));

		onView(allOf(withText("Bundle total"), hasSibling(withText("$2,538.62")))).check(matches(isDisplayed()));
	}
}
