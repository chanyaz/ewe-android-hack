package com.expedia.bookings.test.happy;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
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

		assertBundlePrice("$0.00", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$0.00 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		assertBundlePrice("$0", "View your bundle");
		onView(allOf(withId(R.id.per_person_text), withText("per person"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_savings))).check(matches(not(isDisplayed())));

		HotelScreen.mapFab().perform(click());
		assertHotelMap();
		Common.pressBack();
		Common.delay(1);

		assertHotelSRP();

		Common.pressBack();
		Common.delay(1);
		onView(allOf(withId(R.id.widget_bundle_overview))).check(matches(isDisplayed()));
		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("packagehappypath");
		Common.delay(1);

		assertHotelInfoSite();
		assertBundlePrice("$1,027", "View your bundle");
		onView(allOf(withId(R.id.per_person_text), withText("per person"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_savings))).check(matches(not(isDisplayed())));

		HotelScreen.selectRoom();
		Common.delay(1);

		assertBundlePrice("$3,863.38", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$595.24 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));

		PackageScreen.outboundFlight().perform(click());
		Common.delay(1);

		assertFlightOutbound();
		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$3,864");
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundlePrice("$4,211.90", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$540.62 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));

		PackageScreen.inboundFLight().perform(click());
		Common.delay(1);

		assertFlightInOutbound();
		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$4,212");
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		assertBundlePrice("$2,538.62", "Bundle total");
		onView(allOf(withId(R.id.bundle_total_savings), withText("$56.50 Saved"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.per_person_text))).check(matches(not(isDisplayed())));
		assertCheckoutOverview();

		PackageScreen.checkout().perform(click());

		assetCheckout();
		PackageScreen.enterTravelerInfo();
		PackageScreen.enterPaymentInfo();
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);

		PackageScreen.itin().check(matches(withText("1126420960431")));
	}

	private void assertHotelSRP() {
		HotelScreen.hotelResultsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Hotels in Detroit, MI")))));
		assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.hotel_name_text_view,
			"packagehappypath");
		assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.strike_through_price, "$1,076");
		assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.price_per_night, "$526");

	}

	private void assertHotelInfoSite() {
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("packagehappypath")))));
		float detailsHotelRating = EspressoUtils.getStarRatingValue(HotelScreen.hotelDetailsStarRating());
		assertEquals(4.0f, detailsHotelRating);
		onView(allOf(withId(R.id.user_rating), withText("4.4"))).check(matches(isDisplayed()));
		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		onView(
			allOf(withId(R.id.hotel_search_info), withText("1 Guest, 1 room")))
			.check(matches(isDisplayed()));
	}

	private void assertHotelMap() {
		onView(allOf(withId(R.id.package_map_price_messaging), withText("Price includes taxes, fees, flights + hotel per person"))).check(matches(isDisplayed()));
		assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelCarousel(), 0, R.id.hotel_strike_through_price, "$1,142");
		assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelCarousel(), 0, R.id.hotel_price_per_night, "$562");
	}

	private void assertFlightOutbound() {
		assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.flight_time_detail_text_view,
			"9:00 am - 11:12 am");
		assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.flight_duration_text_view, "5h 12m (Nonstop)");
		assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.price_text_view, "+$0");
	}

	private void assertFlightInOutbound() {
		assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.flight_time_detail_text_view, "1:45 pm - 10:00 pm");
		assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.flight_duration_text_view, "5h 15m (Nonstop)");
		assertViewWithTextIsDisplayedAtPosition(PackageScreen.flightList(), 0, R.id.price_text_view, "+$0");
	}

	private void assertCheckoutOverview() {
		onView(allOf(withId(R.id.destination), withText("Detroit, United States of America"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.check_in_out_dates), withText("Tue Feb 02, 2016 - Thu Feb 04, 2016"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.travelers), withText("1 Traveler"))).check(matches(isDisplayed()));

		onView(allOf(withId(R.id.step_one_text),
			withText("Hotel in Detroit - 1 room, 2 nights"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.step_two_text), withText("Flights - SFO to DTW, round trip"))).check(
			matches(isDisplayed()));
		onView(allOf(withId(R.id.hotels_card_view_text))).check(matches(withText("packagehappypath")));
		onView(allOf(withId(R.id.hotels_room_guest_info_text))).check(matches(withText("1 Guest")));

		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(withText("Flight to Detroit, MI")));
		onView(allOf(withId(R.id.travel_info_view_text),
			isDescendantOfA(withId(R.id.package_bundle_outbound_flight_widget)))).check(
			matches(withText("Jul 10 at 9:00 am, 1 Traveler")));
		onView(allOf(withId(R.id.flight_card_view_text),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(withText("Flight to San Francisco, CA")));
		onView(allOf(withId(R.id.travel_info_view_text),
			isDescendantOfA(withId(R.id.package_bundle_inbound_flight_widget)))).check(
			matches(withText("Jul 16 at 1:45 pm, 1 Traveler")));
	}

	private void assetCheckout() {
		onView(allOf(withId(R.id.legal_information_text_view), withText("By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, and the Privacy Policy."))).check(matches(isDisplayed()));
	}

	private void assertBundlePrice(String price, String totalText) {
		onView(allOf(withId(R.id.bundle_total_text), withText(totalText))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_includes_text), withText(
			"Includes taxes, fees, flights + hotel"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_price), withText(price))).check(matches(isDisplayed()));
	}

	private void assertBundlePriceInFlight(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

	private void assertViewWithTextIsDisplayedAtPosition(ViewInteraction viewInteraction, int position, int id, String text) {
		viewInteraction.check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed(), withText(text)))));
	}


}
