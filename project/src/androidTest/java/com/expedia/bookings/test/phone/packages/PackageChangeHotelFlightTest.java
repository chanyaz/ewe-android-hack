package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

public class PackageChangeHotelFlightTest extends PackageTestCase {

	public void testPackageChangeHotelFlightTest() throws Throwable {
		PackageScreen.searchPackage();

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

		//change hotel room
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change hotel room")).perform(click());
		Common.delay(1);

		HotelScreen.selectRoomButton().perform(click());
		Common.delay(1);
		HotelScreen.clickRoom("change_hotel_room");
		HotelScreen.clickAddRoom();
		Common.delay(2);
		assertAfterChange();

		//change hotel
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change hotel")).check(matches(isEnabled()));
		onView(withText("Change hotel")).perform(click());
		assertBeforeChangeHotel();
		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("Price Change");
		Common.delay(1);

		HotelScreen.selectRoomButton().perform(click());
		Common.delay(1);
		HotelScreen.clickRoom("change_hotel");
		HotelScreen.clickAddRoom();
		Common.delay(1);
		assertAfterChange();

		//change flights
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change flights")).perform(click());
		assertBeforeChangeFlights();

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		PackageScreen.selectFlight(-3);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);
		PackageScreen.selectFlight(-3);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);
		assertAfterChange();
	}

	private void assertBeforeChangeHotel() {
		onView(withText("Select hotel in Detroit")).check(
			matches(isDisplayed()));
		onView(allOf(withId(R.id.hotels_room_guest_info_text), withText("1 Room, 1 Guest"))).check(matches(isDisplayed()));
		onView(withId(R.id.package_hotel_select_icon)).check(matches(isDisplayed()));
		onView(allOf(withImageDrawable(R.drawable.packages_hotel_icon), isCompletelyDisplayed())).check(matches(isDisplayed()));

		onView(withImageDrawable(R.drawable.packages_flight1_checkmark_icon)).check(matches(isDisplayed()));
		onView(withImageDrawable(R.drawable.packages_flight2_checkmark_icon)).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.package_flight_details_icon),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(isDisplayed()));
		onView(allOf(withId(R.id.package_flight_details_icon),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(isDisplayed()));
		onView(withText("Flight to (DTW) Detroit")).check(matches(isDisplayed()));
		onView(withText("Jul 10 at 9:00 am, 1 Traveler")).check(matches(isDisplayed()));
		onView(withText("Flight to (SFO) San Francisco")).check(matches(isDisplayed()));
		onView(withText("Jul 16 at 1:45 pm, 1 Traveler")).check(matches(isDisplayed()));
		onView(withId(R.id.package_bundle_outbound_flight_widget)).check(matches(not(isEnabled())));
		onView(withId(R.id.package_bundle_inbound_flight_widget)).check(matches(not(isEnabled())));

		onView(withId(R.id.checkout_button)).check(matches(not(isEnabled())));
		assertDisabledMenu();
	}

	private void assertBeforeChangeFlights() {
		onView(withText("Price Change")).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.hotels_room_guest_info_text), withText("1 Room, 1 Guest"))).check(matches(isDisplayed()));
		onView(withImageDrawable(R.drawable.packages_hotels_checkmark_icon)).check(matches(isDisplayed()));
		onView(withId(R.id.package_hotel_details_icon)).check(matches(isDisplayed()));

		onView(allOf(withImageDrawable(R.drawable.packages_flight1_icon), isCompletelyDisplayed())).check(matches(isDisplayed()));
		onView(allOf(withImageDrawable(R.drawable.packages_flight2_icon), isCompletelyDisplayed())).check(matches(isDisplayed()));
		onView(withText("Select flight to (DTW) Detroit")).check(matches(isDisplayed()));
		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		onView(allOf(withText(startDate + ", 1 Traveler"), withId(R.id.travel_info_view_text))).check(matches(isDisplayed()));
		onView(withText("Flight to (SFO) San Francisco")).check(matches(isDisplayed()));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		onView(withText(endDate + ", 1 Traveler")).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.package_flight_select_icon),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(isDisplayed()));
		onView(allOf(withId(R.id.package_flight_details_icon),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(not(isDisplayed())));
		onView(withId(R.id.package_bundle_hotel_widget)).check(matches(not(isEnabled())));
		onView(withId(R.id.row_container)).check(matches(not(isEnabled())));
		onView(withId(R.id.package_bundle_inbound_flight_widget)).check(matches(not(isEnabled())));
		onView(withId(R.id.checkout_button)).check(matches(not(isEnabled())));
		assertDisabledMenu();
	}

	private void assertAfterChange() {
		onView(withId(R.id.checkout_button)).check(matches(isEnabled()));
	}

	private void assertDisabledMenu() {
		onView(withId(R.id.package_change_menu)).check(doesNotExist());
	}

}

