package com.expedia.bookings.test.phone.accessibility.packages;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.CustomMatchers.withNavigationContentDescription;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackagesBundleOverviewTest extends PackageTestCase {

	public void testBundleOverviewFlow() throws Throwable {
		PackageScreen.searchPackage();

		Common.pressBack();
		PackageScreen.bundleToolbar().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Trip to Detroit, MI")))));

		checkBundleOverviewHotelContentDescription(true);
		checkBundleTotalWidgetContentDescription("$0.00", "$0.00", false);

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to San Francisco")))));
		PackageScreen.outboundFlightInfo().check(matches(not(isEnabled())));
		checkBundleOverviewFlightContentDescription(PackageScreen.outboundFlightInfoRowContainer(), "(DTW) Detroit", false, true);
		PackageScreen.inboundFlightInfo().check(matches(not(isEnabled())));
		checkBundleOverviewFlightContentDescription(PackageScreen.inboundFlightInfoRowContainer(), "(SFO) San Francisco", true, true);

		PackageScreen.clickHotelBundle();
		openCloseSlidingBundleWidget("$0.00", "$0.00", "$0.00");

		onView(withId(R.id.hotel_results_toolbar)).check(matches(withNavigationContentDescription("Back")));
		HotelScreen.selectHotel("Package Happy Path");
		openCloseSlidingBundleWidget("$1,027.34", "$21.61", "$2,054.67");

		onView(withId(R.id.hotel_star_rating_bar)).check(matches(hasContentDescription()));

		PackageScreen.selectRoom();

		Common.pressBack();
		checkBundleOverviewHotelContentDescription("Package Happy Path");
		checkBundleTotalWidgetContentDescription("$3,863.38", "$595.24", false);
		onView(withId(R.id.toolbar)).check(matches(withNavigationContentDescription("Back")));

		PackageScreen.outboundFlight().perform(click());
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.pressBack();
		checkBundleTotalWidgetContentDescription("$4,211.90", "$540.62", false);
		checkBundleOverviewFlightContentDescription(PackageScreen.outboundFlightInfoRowContainer(), "Jul 10 at 9:00 am", "(DTW) Detroit", false, false);

		PackageScreen.inboundFLight().perform(click());
		onView(withId(R.id.bundle_price_widget)).perform(click());
		checkBundleOverviewFlightContentDescription(PackageScreen.outboundFlightInfoRowContainer(), "Jul 10 at 9:00 am", "(DTW) Detroit", false, false);
		PackageScreen.inboundFLight().perform(click());
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(ViewActions.waitForViewToDisplay());
		PackageScreen.selectThisFlight().perform(click());
		checkBundleTotalWidgetContentDescription("$2,538.62", "$56.50", true);
		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Back")));
		PackageScreen.checkout().perform(click());
		onView(withId(R.id.view_trip_details)).check(matches(hasContentDescription()));
		checkBundleOverviewFlightContentDescription(PackageScreen.inboundFlightInfoRowContainer(), "Jul 16 at 1:45 pm", "(SFO) San Francisco", true, false);

		HotelScreen.doLogin();
		onView(withId(R.id.card_icon)).check(matches(hasContentDescription()));
		onView(withId(R.id.account_top_textview)).check(matches(hasContentDescription()));
		onView(withId(R.id.account_logout_logout_button)).check(matches(hasContentDescription()));
		onView(withId(R.id.account_logout_logout_button)).perform(click());
	}

	private void checkBundleOverviewHotelContentDescription(boolean searchCompleted) {
		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		if (searchCompleted) {
			PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription(
				"Search completed. Select a hotel in Detroit from " + startDate + " to " + endDate
					+ ", for 1 Guest. Button")));
		}
		else {
			PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription("" +
				"Searching for hotels in Detroit from " + startDate + " to " + endDate + ", for 1 Guest. Please wait")));
		}
	}

	private void checkBundleOverviewFlightContentDescription(ViewInteraction view, String flightTo, boolean isInboundFlight, boolean isDisabled) {
		checkBundleOverviewFlightContentDescription(view, null, flightTo, isInboundFlight, isDisabled);
	}

	private void checkBundleOverviewFlightContentDescription(ViewInteraction view, String dateTime, String flightTo, boolean isInboundFlight, boolean isDisabled) {
		String date = (dateTime != null) ? dateTime : isInboundFlight ? DateUtils.localDateToMMMd(LocalDate.now().plusDays(8)) : DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String previous = isInboundFlight ? "Outbound Flight" : "Hotel";
		if (isDisabled) {
			view.check(matches(withContentDescription(
				"Flight to " + flightTo + " on " + date + " for 1 Traveler. Please select " + previous + " first. Button.")));
		}
		else {
			view.check(matches(withContentDescription(
				"You have selected flight to " + flightTo + " on " + date + ", 1 Traveler. Button to expand flight details.")));
		}
	}

	private void checkBundleOverviewHotelContentDescription(String selectedHotelName) {
		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription("" +
			"You have selected hotel " + selectedHotelName + " from " + startDate + " to " + endDate + ", for 1 Guest. Button to expand hotel details.")));
		PackageScreen.hotelDetailsIcon().perform(click());
		PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription("" +
			"You have selected hotel " + selectedHotelName + " from " + startDate + " to " + endDate + ", for 1 Guest. Button to collapse hotel details.")));
		PackageScreen.hotelDetailsIcon().perform(click());
		PackageScreen.bundleOverviewHotelRowContainer().check(matches(withContentDescription("" +
			"You have selected hotel " + selectedHotelName + " from " + startDate + " to " + endDate + ", for 1 Guest. Button to expand hotel details.")));
	}

	private void checkBundleTotalWidgetContentDescription(String totalPrice, String totalSaved,
														  boolean isCostBreakdownEnabled) {
		if (isCostBreakdownEnabled) {
			PackageScreen.bundleTotalFooterWidget().check((matches(withContentDescription(
				"Bundle total is " + totalPrice + ". This price includes taxes, fees for both flights and hotel. "
					+ totalSaved + " Saved. Cost Breakdown dialog. Button."))));
		}
		else if (!totalSaved.equalsIgnoreCase("$0.00")) {
			PackageScreen.bundleTotalFooterWidget().check((matches(withContentDescription(
				"Bundle total is " + totalPrice + ". This price includes taxes, fees for both flights and hotel. "
					+ totalSaved + " Saved"))));
		}
		else {
			PackageScreen.bundleTotalFooterWidget().check((matches(withContentDescription(
				"Bundle total is " + totalPrice + ". This price includes taxes, fees for both flights and hotel. "))));
		}
	}

	private void checkBundleSlidingWidgetContentDescription(String pricePerPerson, boolean isOpened) {
		String str;
		if (isOpened) {
			str = "Showing bundle details. Button to close.";
		}
		else {
			str = "Bundle price is " + pricePerPerson + " per person. This price includes taxes, fees for both flights and hotel. Button to view bundle.";
		}
		PackageScreen.bundleTotalSlidingWidget().check((matches(withContentDescription(str))));
	}

	private void openCloseSlidingBundleWidget(String pricePerPerson, String totalSaved, String packageTotalPrice) {
		checkBundleSlidingWidgetContentDescription(pricePerPerson, false);
		PackageScreen.bundleTotalSlidingWidget().perform(click());
		Common.delay(1);
		checkBundleTotalWidgetContentDescription(packageTotalPrice, totalSaved, false);
		checkBundleSlidingWidgetContentDescription(pricePerPerson, true);
		PackageScreen.bundleTotalSlidingWidget().perform(click());
		Common.delay(1);
	}
}
