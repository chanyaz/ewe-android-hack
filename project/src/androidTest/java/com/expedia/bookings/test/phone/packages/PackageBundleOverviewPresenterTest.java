package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackageBundleOverviewPresenterTest extends PackageTestCase {

	@Test
	public void testBundleOverviewCheckoutFlow() throws Throwable {
		PackageScreen.searchPackage();

		Common.pressBack();
		PackageScreen.bundleToolbar().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Trip to Detroit, MI")))));

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to San Francisco")))));
		PackageScreen.outboundFlightInfo().check(matches(not(isEnabled())));
		PackageScreen.inboundFlightInfo().check(matches(not(isEnabled())));

		PackageScreen.clickHotelBundle();

		HotelScreen.selectHotel("Package Happy Path");

		PackageScreen.selectRoom();

		Common.pressBack();

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select flight to Detroit")))));

		PackageScreen.outboundFlight().perform(click());

		Common.delay(1);
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		Common.pressBack(); // auto advance so need to back up.

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select flight to San Francisco")))));

		PackageScreen.inboundFLight().perform(click());

		Common.delay(1);
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.hotelBundle().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Package Happy Path")))));

		final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		PackageScreen.hotelBundle().check(matches(hasDescendant(
			allOf(isDisplayed(), withText(PackageScreen.getDatesGuestInfoText(dtf.parseLocalDate("2016-02-02"), dtf.parseLocalDate("2016-02-04")))))));
		PackageScreen.hotelDetailsIcon().check(matches(isEnabled()));

		PackageScreen.outboundFlightInfo().check(matches(isEnabled()));
		PackageScreen.inboundFlightInfo().check(matches(isEnabled()));

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));

		PackageScreen.outboundFlightDetailsIcon().check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE)));

		PackageScreen.outboundFlightDetailsIcon().perform(click());
		PackageScreen.outboundFlightDetailsContainer().check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE)));

		PackageScreen.clickHotelBundle();
		PackageScreen.hotelRoomImageView().check(matches(isDisplayed()));
		PackageScreen.outboundFlightDetailsContainer().check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.GONE)));
	}

	@Test
	public void testHotelBundleOverviewFlow() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM d");
		String formattedStartString = startDate.toString(dateFormatter);
		String formattedEndString = endDate.toString(dateFormatter);

		PackageScreen.searchPackage();

		//Test strings and bundle state
		PackageScreen.bundlePriceWidget().perform(waitForViewToDisplay());
		PackageScreen.bundlePriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.bundlePriceWidget().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select hotel in Detroit")))));
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		PackageScreen.hotelDatesRoomInfo().
			check(matches(withText(PackageScreen.getDatesGuestInfoText(dtf.parseLocalDate("2016-02-03"), dtf.parseLocalDate("2016-02-04")))));
		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to Detroit")))));

		PackageScreen.outboundFlightCardInfo().check(matches(withText(formattedStartString + ", 1 Traveler")));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to San Francisco")))));
		PackageScreen.inboundFlightCardInfo().check(matches(withText(formattedEndString + ", 1 Traveler")));

		//Test clicking on toolbar returns to results
		PackageScreen.bundlePriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.hotelBundleWidget().check(matches(not(isCompletelyDisplayed())));

		//Test clicking on hotel returns to results
		PackageScreen.bundlePriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.clickHotelBundle();
		PackageScreen.hotelBundleWidget().check(matches(not(isCompletelyDisplayed())));

		//Test back returns to results
		PackageScreen.bundlePriceWidget().perform(click());
		Common.delay(1);
		Common.pressBack();
		PackageScreen.hotelBundleWidget().check(matches(not(isCompletelyDisplayed())));
	}

	@Test
	public void testHotelOverview() throws Throwable {
		PackageScreen.searchPackage();
		HotelScreen.selectHotel("Package Happy Path");

		HotelScreen.clickRoom("happy_outbound_flight");
		PackageScreen.clickAddRoom();

		//HotelScreen.selectRoom();

		Common.delay(1);
		Common.pressBack();
		Common.delay(1);
		//expand
		PackageScreen.clickHotelBundle();

		onView(withId(R.id.hotel_room_info)).check(matches(isDisplayed()));
		onView(withId(R.id.hotel_room_type)).check(matches(isDisplayed()));

		onView(withId(R.id.hotel_address)).check(matches(isDisplayed()));
		onView(withId(R.id.hotel_city)).check(matches(isDisplayed()));

		onView(withId(R.id.hotel_free_cancellation)).check(matches(isDisplayed()));
		onView(withId(R.id.hotel_promo_text)).check(matches(isDisplayed()));

		PackageScreen.hotelRoomImageView().check(matches(isDisplayed()));

		//collapse
		PackageScreen.hotelBundleContainer().perform(click());
		PackageScreen.hotelRoomImageView().check(matches(CoreMatchers.not(isDisplayed())));
	}

	@Test
	public void testFlightOverview() throws Throwable {
		PackageScreen.doPackageSearch();

		//test outbound flight details visible when expanded
		PackageScreen.outboundFlight().perform(click());
		PackageScreen.outboundFlightDetailsContainer().check(matches(isDisplayed()));
		PackageScreen.outboundFlightDetailsContainer().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("(SFO) San Francisco - (HNL) Honolulu")))));

		//test outbound flight details collapsed and no longer visible
		PackageScreen.outboundFlightBundleContainer().perform(click());
		PackageScreen.outboundFlightDetailsContainer().check(matches(CoreMatchers.not(isCompletelyDisplayed())));

		//test inbound flight details visible when expanded
		PackageScreen.inboundFLight().perform(click());
		PackageScreen.inboundFlightDetailsContainer().check(matches(isDisplayed()));
		PackageScreen.inboundFlightDetailsContainer().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("(HNL) Honolulu - (SFO) San Francisco")))));

		//test inbound flight details collapsed and no longer visible
		PackageScreen.inboundFlightBundleContainer().perform(click());
		PackageScreen.inboundFlightDetailsContainer().check(matches(CoreMatchers.not(isCompletelyDisplayed())));
	}


}
