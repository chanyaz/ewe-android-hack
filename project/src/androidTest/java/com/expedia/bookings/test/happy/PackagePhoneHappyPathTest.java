package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackagePhoneHappyPathTest extends PackageTestCase {

	public void testPackagePhoneHappyPath() throws Throwable {
		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		PackageScreen.arrival().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		PackageScreen.selectDates(startDate, endDate);

		PackageScreen.searchButton().perform(click());
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		assertBundlePrice("0.00","per person");

		HotelScreen.selectHotel("packagehappypath");
		Common.delay(1);

		assertBundlePrice("172.0","per person");

		HotelScreen.selectRoom();
		Common.delay(1);

		assertBundlePrice("$3,864","$595 Saved");

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundlePrice("$4,212","$540 Saved");

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundlePrice("$2,539","$56.50 Saved");

		PackageScreen.checkout().perform(click());

		PackageScreen.enterTravelerInfo();
		PackageScreen.enterPaymentInfo();
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);

		PackageScreen.itin().check(matches(withText("1126420960431")));
	}

	private void assertBundlePrice(String price, String savings) {
		onView(allOf(withId(R.id.bundle_total_text), withText("Bundle Overview"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_includes_text), withText("Includes taxes, fees, flights + hotel"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.bundle_total_price), withText(price))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_savings), withText(savings))).check(matches(isDisplayed()));
	}

}
