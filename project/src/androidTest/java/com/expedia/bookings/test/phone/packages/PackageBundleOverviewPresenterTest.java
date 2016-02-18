package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
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

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

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
		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_hotel_widget)),
			withId(R.id.package_hotel_details_icon))).check(matches(isEnabled()));

		PackageScreen.outboundFlightInfo().check(matches(isEnabled()));
		PackageScreen.inboundFlightInfo().check(matches(isEnabled()));

		PackageScreen.outboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (DTW) Detroit")))));
		PackageScreen.inboundFlightInfo().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Flight to (SFO) San Francisco")))));

		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(R.id.package_flight_details_icon))).check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE)));
		PackageScreen.outboundFlightDetails().perform(click());

		onView(allOf(isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)),
			withId(R.id.flight_details_container))).check(matches(withEffectiveVisibility(
			ViewMatchers.Visibility.VISIBLE)));
	}
}
