package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class PackageBundleOverviewPresenterTest extends PackageTestCase {

	public void testBundleOverviewCheckoutFlow() throws Throwable {
		PackageScreen.searchPackage();
		PackageScreen.bundleToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));
		PackageScreen.outboundFlightInfo().check(matches(not(isEnabled())));
		PackageScreen.inboundFlightInfo().check(matches(not(isEnabled())));
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Package Happy Path");
		Common.delay(1);

		HotelScreen.selectRoom();
		Common.delay(1);

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select flight to (DTW) Detroit")))));

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select flight to (SFO) San Francisco")))));

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.checkout().perform(click());
		Common.pressBack();

		PackageScreen.hotelBundle().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		PackageScreen.hotelBundle().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("1 Room, 1 Guest")))));
		PackageScreen.hotelDetailsIcon().check(matches(isEnabled()));

		PackageScreen.outboundFlightInfo().check(matches(isEnabled()));
		PackageScreen.inboundFlightInfo().check(matches(isEnabled()));

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));

		PackageScreen.outboundFlightDetailsIcon().check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE)));

		PackageScreen.outboundFlightDetailsIcon().perform(click());
		PackageScreen.outboundFlightDetailsContainer().check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE)));
	}

	public void testHotelBundleOverviewFlow() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(5);
		LocalDate endDate = LocalDate.now().plusDays(10);
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM d");
		String formattedStartString = startDate.toString(dateFormatter);
		String formattedEndString = endDate.toString(dateFormatter);


		PackageScreen.destination().perform(typeText("SFO"));
		PackageScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		PackageScreen.arrival().perform(typeText("DTW"));
		PackageScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");
		PackageScreen.selectDates(startDate, endDate);
		PackageScreen.searchButton().perform(click());
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		//Test strings and bundle state
		PackageScreen.hotelPriceWidget().perform(click());
		PackageScreen.hotelPriceWidget().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Select hotel in Detroit")))));
		PackageScreen.hotelGuestRoomInfo().check(matches(withText("1 Room, 1 Guest")));
		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));

		PackageScreen.outboundFlightCardInfo().check(matches(withText(formattedStartString + ", 1 Traveler")));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));
		PackageScreen.inboundFlightCardInfo().check(matches(withText(formattedEndString + ", 1 Traveler")));

		//Test clicking on toolbar returns to results
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.hotelBundleWidget().check(matches(not(isDisplayed())));

		//Test clicking on hotel returns to results
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);
		PackageScreen.hotelBundleWidget().check(matches(not(isDisplayed())));

		//Test back returns to results
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		Common.pressBack();
		Common.delay(1);
		PackageScreen.hotelBundleWidget().check(matches(not(isDisplayed())));
	}
}
