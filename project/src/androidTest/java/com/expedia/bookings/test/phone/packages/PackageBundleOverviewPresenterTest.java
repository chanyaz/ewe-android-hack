package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.utils.DateUtils;

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
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackageBundleOverviewPresenterTest extends PackageTestCase {

	public void testBundleOverviewCheckoutFlow() throws Throwable {
		PackageScreen.searchPackage();
		PackageScreen.bundleToolbar().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Trip to Detroit, MI")))));

		checkBundleOverviewHotelContentDescription(true);

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));
		PackageScreen.outboundFlightInfo().check(matches(not(isEnabled())));
		PackageScreen.inboundFlightInfo().check(matches(not(isEnabled())));
		PackageScreen.clickHotelBundle();

		HotelScreen.selectHotel("Package Happy Path");

		PackageScreen.selectRoom();

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select flight to (DTW) Detroit")))));

		PackageScreen.outboundFlight().perform(click());

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select flight to (SFO) San Francisco")))));

		PackageScreen.inboundFLight().perform(click());

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.checkout().perform(waitForViewToDisplay());
		PackageScreen.checkout().perform(click());
		Common.pressBack();

		PackageScreen.hotelBundle().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Package Happy Path")))));
		PackageScreen.hotelBundle().check(matches(hasDescendant(
			allOf(isDisplayed(), withText(
				PackageScreen.getDatesGuestInfoText(LocalDate.now().plusDays(3), LocalDate.now().plusDays(8)))))));
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
	}

	public void testHotelBundleOverviewFlow() throws Throwable {
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM d");
		String formattedStartString = startDate.toString(dateFormatter);
		String formattedEndString = endDate.toString(dateFormatter);

		PackageScreen.searchPackage();
		PackageScreen.clickHotelBundle();

		//Test strings and bundle state
		PackageScreen.hotelPriceWidget().perform(waitForViewToDisplay());
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.hotelPriceWidget().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Trip to Detroit, MI")))));
		PackageScreen.hotelInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Select hotel in Detroit")))));

		PackageScreen.hotelDatesRoomInfo().check(matches(withText(PackageScreen.getDatesGuestInfoText(startDate, endDate))));
		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));

		PackageScreen.outboundFlightCardInfo().check(matches(withText(formattedStartString + ", 1 Traveler")));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));
		PackageScreen.inboundFlightCardInfo().check(matches(withText(formattedEndString + ", 1 Traveler")));

		//Test clicking on toolbar returns to results
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.hotelBundleWidget().check(matches(not(isCompletelyDisplayed())));

		//Test clicking on hotel returns to results
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		PackageScreen.clickHotelBundle();
		PackageScreen.hotelBundleWidget().check(matches(not(isCompletelyDisplayed())));

		//Test back returns to results
		PackageScreen.hotelPriceWidget().perform(click());
		Common.delay(1);
		Common.pressBack();
		PackageScreen.hotelBundleWidget().check(matches(not(isCompletelyDisplayed())));
	}

	public void testHotelOverview() throws Throwable {
		PackageScreen.searchPackage();
		PackageScreen.hotelBundle().perform(click());
		HotelScreen.selectHotel("Package Happy Path");

		HotelScreen.clickRoom("happy_outbound_flight");
		PackageScreen.clickAddRoom();

		//HotelScreen.selectRoom();

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

}
