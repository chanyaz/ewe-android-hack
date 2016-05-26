package com.expedia.bookings.test.happy;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.newflights.FlightTestHelpers;
import com.expedia.bookings.test.phone.newflights.FlightsResultsScreen;
import com.expedia.bookings.test.phone.packages.PackageScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackagePhoneHappyPathTest extends PackageTestCase {

	public void testPackagePhoneHappyPath() throws Throwable {
		SearchScreen.selectOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);

		SearchScreen.searchButton().perform(click());

		HotelScreen.mapFab().perform(click());
		Common.pressBack();
		assertHotelSRP();

		Common.pressBack();
		onView(allOf(withId(R.id.widget_bundle_overview))).check(matches(isDisplayed()));
		PackageScreen.clickHotelBundle();

		assertFilter();

		HotelScreen.selectHotel("Package Happy Path");

		assertHotelInfoSite();
		reviews();
		assertHotelBundlePrice("$1,027.34", "View your bundle", "$21.61 Saved");

		PackageScreen.selectRoom();

		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$3,864");
		PackageScreen.selectThisFlight().perform(click());

		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("United");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 am - 11:12 am (5h 12m)");

		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$4,212");
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.travelerInfo().perform(scrollTo(), click());
		onView(allOf(withId(R.id.boarding_warning), withText(mRes.getString(R.string.name_must_match_warning_new)))).check(matches(isDisplayed()));
		PackageScreen.enterFirstName("FiveStar");
		PackageScreen.enterLastName("Bear");
		PackageScreen.enterPhoneNumber("7732025862");
		PackageScreen.selectBirthDate(9, 6, 1989);
		PackageScreen.selectGender("Male");

		PackageScreen.clickTravelerAdvanced();
		PackageScreen.enterRedressNumber("1234567");

		PackageScreen.clickTravelerDone();
		PackageScreen.enterPaymentInfo();

		assetCheckout();
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);

		assertConfirmation();
	}

	private void assertConfirmation() {
		onView(allOf(withId(R.id.destination), withText("Detroit"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.first_row), isDescendantOfA(withId(R.id.destination_card_row)), withText("Package Happy Path"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.second_row), isDescendantOfA(withId(R.id.destination_card_row)), withText("Feb 2 - Feb 4, 1 Guest"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.first_row), isDescendantOfA(withId(R.id.outbound_flight_card)), withText("Flight to (DTW) Detroit"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.second_row), isDescendantOfA(withId(R.id.outbound_flight_card)))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.first_row), isDescendantOfA(withId(R.id.inbound_flight_card)), withText("Flight to (SFO) San Francisco"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.second_row), isDescendantOfA(withId(R.id.inbound_flight_card)))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.itin_number), withText("#1126420960431 sent to test@email.com"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.view_itin_button), withText("View Itinerary"))).check(matches(isDisplayed()));
	}

	private void assertFilter() {
		onView(withId(R.id.filter_button)).perform(click());
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(isDisplayed()));
		onView(withText(R.string.sort_and_filter)).check(matches(isDisplayed()));
		Common.pressBack();
	}

	private void assertHotelSRP() {
		HotelScreen.hotelResultsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Hotels in Detroit, MI")))));
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.hotel_name_text_view,
			"Package Happy Path");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.strike_through_price, "$538");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.price_per_night, "$526");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelScreen.hotelResultsList(), 2, R.id.unreal_deal_message, "Book this and save $110 (22%)");
	}

	private void assertHotelInfoSite() {
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		float detailsHotelRating = EspressoUtils.getStarRatingValue(HotelScreen.hotelDetailsStarRating());
		assertEquals(4.0f, detailsHotelRating);
		onView(allOf(withId(R.id.user_rating), withText("4.4"))).check(matches(isDisplayed()));
		onView(
			allOf(withId(R.id.hotel_search_info), withText("1 Room, 1 Guest")))
			.check(matches(isDisplayed()));
	}

	private void assetCheckout() {
		onView(allOf(withId(R.id.legal_information_text_view), withText(
			"By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, and the Privacy Policy.")))
			.check(matches(isDisplayed()));
		onView(allOf(withId(R.id.purchase_total_text_view), withText("Your card will be charged $2,538.62")))
			.check(matches(isDisplayed()));
	}

	private void assertBundlePrice(String price, String totalText) {
		onView(allOf(withId(R.id.bundle_total_text), withText(totalText))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_includes_text), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withText(
				"Includes taxes, fees, flights + hotel"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_price), withText(price))).check(matches(isDisplayed()));
	}

	private void assertHotelBundlePrice(String price, String totalText, String savings) {
		onView(allOf(withId(R.id.bundle_total_text), isDescendantOfA(withId(R.id.bundle_price_widget)),
			withText(totalText))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_includes_text), isDescendantOfA(withId(R.id.bundle_price_widget)),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withText(
				"Includes taxes, fees, flights + hotel"))).check(matches(isDisplayed()));
		onView(
			allOf(withId(R.id.bundle_total_price), isDescendantOfA(withId(R.id.bundle_price_widget)), withText(price)))
			.check(matches(isDisplayed()));
		onView(
			allOf(withId(R.id.bundle_total_savings), isDescendantOfA(withId(R.id.bundle_price_widget)), withText(savings)))
			.check(matches(isDisplayed()));
	}

	private void assertBundlePriceInFlight(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

	private void reviews() throws Throwable {
		HotelScreen.clickRatingContainer();
		HotelScreen.reviews().perform(ViewActions.waitForViewToDisplay());
		onView(withText(R.string.user_review_sort_button_critical)).perform(click());
		onView(withText(R.string.user_review_sort_button_favorable)).perform(click());
		onView(withText(R.string.user_review_sort_button_recent)).perform(click());
		Espresso.pressBack();
	}
}
