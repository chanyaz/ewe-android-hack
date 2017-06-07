package com.expedia.bookings.test.phone.packages;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageChangeHotelFlightTest extends PackageTestCase {

	@Test
	public void testPackageChangeHotelFlightTest() throws Throwable {
		PackageScreen.doPackageSearch();

		//change hotel room
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change hotel room")).perform(click());
		Common.delay(1);

		HotelScreen.selectRoomButton().perform(click());
		Common.delay(1);
		HotelScreen.clickRoom("change_hotel_room");
		PackageScreen.clickAddRoom();
		Common.delay(2);
		assertAfterChange();

		//change hotel
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change hotel")).check(matches(isEnabled()));
		onView(withText("Change hotel")).perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Price Change");
		Common.delay(1);

		HotelScreen.selectRoomButton().perform(click());
		Common.delay(1);
		HotelScreen.clickRoom("change_hotel");
		PackageScreen.clickAddRoom();
		Common.delay(1);
		assertAfterChange();

		//change flights
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change flights")).perform(click());
		Common.delay(1);

		onView(withId(R.id.flight_results_price_header)).check(matches(isDisplayed()));
		EspressoUtils.assertViewWithIdIsNotDisplayedAtPosition(PackageScreen.flightList(), 1, R.id.package_best_flight);

		PackageScreen.selectFlight(-2);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(-2);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);
		assertAfterChange();
	}

	private void assertAfterChange() {
		onView(withId(R.id.checkout_button)).check(matches(isEnabled()));

		final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		PackageScreen.hotelBundle().check(matches(hasDescendant(
			allOf(isDisplayed(), withText(PackageScreen.getDatesGuestInfoText(dtf.parseLocalDate("2016-02-02"), dtf.parseLocalDate("2016-02-04")))))));
		PackageScreen.hotelDetailsIcon().check(matches(isEnabled()));

		PackageScreen.outboundFlight().check(matches(isEnabled()));
		PackageScreen.inboundFLight().check(matches(isEnabled()));

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
}
