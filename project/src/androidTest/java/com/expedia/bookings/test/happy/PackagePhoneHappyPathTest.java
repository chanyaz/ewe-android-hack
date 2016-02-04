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
import static org.hamcrest.CoreMatchers.not;

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

		assertBundlePrice("$0", "View your bundle");
		onView(allOf(withId(R.id.per_person_text), withText("per person"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_savings))).check(matches(not(isDisplayed())));

		HotelScreen.selectHotel("packagehappypath");
		Common.delay(1);

		assertBundlePrice("$1,027.34","View your bundle");
		onView(allOf(withId(R.id.per_person_text), withText("per person"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_savings))).check(matches(not(isDisplayed())));

		HotelScreen.selectRoom();
		Common.delay(1);

		assertBundlePrice("$3,863.38", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$595.24 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$3,864");
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundlePrice("$4,211.90", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$540.62 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$4,212");
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundlePrice("$2,538.62", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$56.50 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));
		assertCheckoutOverview();

		PackageScreen.checkout().perform(click());

		PackageScreen.enterTravelerInfo();
		PackageScreen.enterPaymentInfo();
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);

		PackageScreen.itin().check(matches(withText("1126420960431")));
	}

	private void assertCheckoutOverview() {
		onView(allOf(withId(R.id.destination), withText("Detroit, USA"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.check_in_out_dates), withText("Feb 02, 2016 - Feb 04, 2016"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.step_one_text), withText("Hotel in Detroit - 1 room, 2 nights"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.step_two_text), withText("Flights - SFO to DTW, round trip"))).check(matches(isDisplayed()));
	}

	private void assertBundlePrice(String price, String totalText) {
		onView(allOf(withId(R.id.bundle_total_text), withText(totalText))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_includes_text), withText("Includes taxes, fees, flights + hotel"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_price), withText(price))).check(matches(isDisplayed()));
	}

	private void assertBundlePriceInFlight(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

}
