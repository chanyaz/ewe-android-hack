package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withTextColor;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class PackageCostBreakdownTest extends PackageTestCase {

	public void testPackageCostBreakdown() throws Throwable {
		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		PackageScreen.arrival().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.searchButton().perform(click());
		Common.delay(1);

		assertBundleTotalIconVisibility("Bundle total", false);
		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		assertBundleTotalIconVisibility("View your bundle", false);
		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		assertBundleTotalIconVisibility("View your bundle", false);
		HotelScreen.selectRoom();
		Common.delay(1);

		assertBundleTotalIconVisibility("Bundle total", false);
		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundleTotalIconVisibility("Bundle total", false);
		PackageScreen.inboundFLight().perform(click());
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
		onView(allOf(withText("Hotel + Flight"), hasSibling(withText("$2,538.62")))).check(matches(isDisplayed()));
		onView(allOf(withText("Taxes & Fees Included ($278.12)"), hasSibling(withText("")))).check(matches(isDisplayed()));

		//Check savings text and color
		onView(allOf(withText("Total Savings"), hasSibling(withText("$56.50")))).check(matches(isDisplayed()));
		onView(allOf(withText("Total Savings"))).check(matches(withTextColor("#A4C639")));
		onView(allOf(withText("$56.50"))).check(matches(withTextColor("#A4C639")));

		onView(allOf(withText("Due to Expedia"), hasSibling(withText("$2,538.62")))).check(matches(isDisplayed()));
	}
}
