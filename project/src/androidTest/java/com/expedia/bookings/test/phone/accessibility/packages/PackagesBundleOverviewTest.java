package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackagesBundleOverviewTest extends PackageTestCase {

	public void testBundleOverviewFlow() throws Throwable {
		PackageScreen.searchPackage();

		Common.pressBack();
		PackageScreen.bundleToolbar().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Trip to Detroit, MI")))));

		checkBundleOverviewHotelContentDescription(true);
		checkBundleTotalWidgetContentDescription("$0.00", "$0.00");

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));
		PackageScreen.outboundFlightInfo().check(matches(not(isEnabled())));
		PackageScreen.inboundFlightInfo().check(matches(not(isEnabled())));

		PackageScreen.clickHotelBundle();
		openCloseSlidingBundleWidget("$0.00", "$0.00");

		HotelScreen.selectHotel("Package Happy Path");
		openCloseSlidingBundleWidget("$1,027.34", "$21.61");

		PackageScreen.selectRoom();

		Common.pressBack();
		checkBundleOverviewHotelContentDescription("Package Happy Path");
		checkBundleTotalWidgetContentDescription("$3,863.38", "$595.24");
	}

	private void checkBundleOverviewHotelContentDescription(boolean searchCompleted) {
		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		if (searchCompleted) {
			PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription(
				"Search completed, please press this button to select a hotel in Detroit from " + startDate + " to " + endDate + ", for 1 Guest")));
		}
		else {
			PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription("" +
				"Searching for hotels in Detroit from " + startDate + " to " + endDate + ", for 1 Guest. Please wait")));
		}
	}

	private void checkBundleOverviewHotelContentDescription(String selectedHotelName) {
		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription("" +
			"You have selected hotel " + selectedHotelName + " from " + startDate + " to " + endDate + ", for 1 Guest. Button to toggle hotel details.")));
	}

	private void checkBundleTotalWidgetContentDescription(String totalPrice, String totalSaved) {
		PackageScreen.bundleTotalFooterWidget().check((matches(withContentDescription("Bundle total is " + totalPrice + ". This price includes taxes, fees for both flights and hotel. " + totalSaved + " Saved"))));
	}

	private void checkBundleSlidingWidgetContentDescription(String totalPrice, String totalSaved, boolean isOpened) {
		String str;
		if (isOpened) {
			str = "Showing bundle details. Button to close.";
		}
		else {
			str = "Bundle total is " + totalPrice + ". This price includes taxes, fees for both flights and hotel. " + totalSaved + " Saved. Button to view bundle.";
		}
		PackageScreen.bundleTotalSlidingWidget().check((matches(withContentDescription(str))));
	}

	private void openCloseSlidingBundleWidget(String totalPrice, String totalSaved) {
		checkBundleSlidingWidgetContentDescription(totalPrice, totalSaved, false);
		PackageScreen.bundleTotalSlidingWidget().perform(click());
		Common.delay(1);
		checkBundleTotalWidgetContentDescription(totalPrice, totalSaved);
		checkBundleSlidingWidgetContentDescription(totalPrice, totalSaved, true);
		PackageScreen.bundleTotalSlidingWidget().perform(click());
		Common.delay(1);
	}
}
