package com.expedia.bookings.test.happy;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.bookings.test.pagemodels.flights.FlightsResultsScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.test.phone.newflights.FlightTestHelpers;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withNavigationContentDescription;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

public class PackagePhoneHappyPathTest extends PackageTestCase {

/*	@Test
	public void testPackagePhoneHappyPath() throws Throwable {
		SearchScreen.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.chooseDatesWithDialog(startDate, endDate);

		SearchScreen.searchButton().perform(click());

		HotelResultsScreen.mapFab().perform(click());
		Common.pressBack();
		assertHotelSRP();

		Common.pressBack();
		onView(withId(R.id.widget_bundle_overview)).perform(waitForViewToDisplay());
		PackageScreen.clickHotelBundle();

		assertFilter();

		HotelResultsScreen.selectHotel("Package Happy Path");

		assertHotelInfoSite();
		reviews();

		HotelInfoSiteScreen.bookFirstRoom();

		PackageScreen.flightList().perform(waitForViewToDisplay());
		assertBundlePrice("$1,931.69", "View your bundle");
		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$1,932");
		PackageScreen.selectThisFlight().perform(click());

		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("United");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 am - 11:12 am (5h 12m)");
		assertBundlePrice("$2,105.95", "View your bundle");

		PackageScreen.selectFlight(0);
		assertBundlePriceInFlight("$2,106");
		PackageScreen.selectThisFlight().perform(waitForViewToDisplay());
		PackageScreen.selectThisFlight().perform(click());

		PackageScreen.checkout().perform(click());

		CheckoutScreen.loginAsQAUser();
		onView(allOf(withId(R.id.card_info_name), withText("AmexTesting"))).check(matches(isDisplayed()));
		assertCheckout();
		HotelCheckoutScreen.clickSignOut();

		PackageScreen.travelerInfo().perform(scrollTo(), click());
		onView(allOf(withId(R.id.boarding_warning), withText(mRes.getString(R.string.name_must_match_warning_new)))).check(matches(isDisplayed()));
		TravelerDetails.enterFirstName("FiveStar");
		TravelerDetails.enterLastName("Bear");
		TravelerDetails.enterEmail("test@email.com");
		Espresso.closeSoftKeyboard();
		TravelerDetails.enterPhoneNumber("7732025862");
		Espresso.closeSoftKeyboard();
		TravelerDetails.selectBirthDate(1989, 6, 9);
		Espresso.closeSoftKeyboard();
		TravelerDetails.materialSelectGender("Male");
		Espresso.closeSoftKeyboard();

		TravelerDetails.clickAdvanced();
		TravelerDetails.enterRedressNumber("1234567");
		TravelerDetails.enterKnownTravelerNumber("TN12345");

		Common.closeSoftKeyboard(onView(withId(R.id.first_name_input)));
		PackageScreen.clickSpecialAssistance();
		onView(withText("Deaf with hearing dog")).perform(waitForViewToDisplay(), click());
		Common.closeSoftKeyboard(onView(withId(R.id.first_name_input)));
		PackageScreen.clickSeatPreference();
		onView(withText("Prefers: Window Seat")).perform(waitForViewToDisplay(), click());

		TravelerDetails.clickDone();
		PackageScreen.enterPaymentInfo();

		assertCheckout();
		CheckoutScreen.performSlideToPurchase();

		assertConfirmation();
	}*/

	public void testPackagePhoneHappyPathSignedIn() throws Throwable {
		SearchScreenActions.selectPackageOriginAndDestination();
		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreenActions.chooseDatesWithDialog(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		HotelResultsScreen.mapFab().perform(click());
		Common.pressBack();
		Common.pressBack();
		onView(withId(R.id.widget_bundle_overview)).perform(waitForViewToDisplay());
		onView(withId(R.id.widget_bundle_overview)).check(matches(isDisplayed()));
		PackageScreen.clickHotelBundle();
		HotelResultsScreen.selectHotel("Package Happy Path");
		HotelInfoSiteScreen.bookFirstRoom();
		Common.delay(1);
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(click());
		FlightTestHelpers.assertDockedOutboundFlightSelectionWidget();
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("Outbound");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("United");
		FlightsResultsScreen.dockedOutboundFlightSelectionWidgetContainsText("9:00 am - 11:12 am (5h 12m)");
		PackageScreen.selectFlight(0);
		PackageScreen.selectThisFlight().perform(waitForViewToDisplay());
		PackageScreen.selectThisFlight().perform(click());
		PackageScreen.checkout().perform(click());
		CheckoutScreen.loginAsQAUser();
		onView(allOf(withId(R.id.card_info_name), withText("AmexTesting"))).check(matches(isDisplayed()));
		assertCheckout();
		PackageScreen.travelerInfo().perform(scrollTo(), click());
		onView(withId(R.id.select_traveler_button)).perform(click());
		Common.delay(1);
		onView(withText("Add New Traveler")).perform(click());
		Common.delay(1);
		TravelerDetails.enterFirstName("FiveStar");
		TravelerDetails.enterLastName("Bear");
		Espresso.closeSoftKeyboard();
		TravelerDetails.enterPhoneNumber("7732025862");
		Espresso.closeSoftKeyboard();

		TravelerDetails.selectBirthDate(1989, 6, 9);
		Espresso.closeSoftKeyboard();
		TravelerDetails.materialSelectGender("Male");
		Espresso.closeSoftKeyboard();
		TravelerDetails.clickDone();

		onView(withText(R.string.no_thanks)).perform(click());

		CheckoutScreen.performSlideToPurchase(true);
		onView(allOf(withId(R.id.destination), withText("Detroit"))).check(matches(isDisplayed()));
	}

	private void assertConfirmation() {
		onView(allOf(withId(R.id.destination), withText("Detroit"))).perform(waitForViewToDisplay());
		onView(allOf(withId(R.id.confirmation_title), isDescendantOfA(withId(R.id.destination_card_row)), withText("Package Happy Path"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_subtitle), isDescendantOfA(withId(R.id.destination_card_row)), withText("Feb 2 - Feb 4, 1 guest"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_title), isDescendantOfA(withId(R.id.outbound_flight_card)), withText("Flight to (DTW) Detroit"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_subtitle), isDescendantOfA(withId(R.id.outbound_flight_card)))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_title), isDescendantOfA(withId(R.id.inbound_flight_card)), withText("Flight to (SFO) San Francisco"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.confirmation_subtitle), isDescendantOfA(withId(R.id.inbound_flight_card)))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.itin_number), withText("#1126420960431 sent to test@email.com"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.view_itin_button), withText("View Itinerary"))).check(matches(isDisplayed()));
	}

	private void assertFilter() {
		onView(withId(R.id.filter_btn)).perform(longClick());
		onView(withText(R.string.sort_and_filter)).
			inRoot(RootMatchers.withDecorView(not(getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
		onView(withId(R.id.filter_btn)).perform(click());
		onView(withId(R.id.sort_by_selection_spinner)).check(matches(isDisplayed()));
		onView(withText(R.string.sort_and_filter)).check(matches(isDisplayed()));
		Common.pressBack();
	}

	private void assertHotelSRP() {
		HotelResultsScreen.hotelResultsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Hotels in Detroit, MI")))));
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelResultsScreen.hotelResultsList(), 2, R.id.hotel_name,
			"Package Happy Path");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelResultsScreen.hotelResultsList(), 2, R.id.strike_through_price, "$538");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelResultsScreen.hotelResultsList(), 2, R.id.price_per_night, "$526");
		EspressoUtils.assertViewWithTextIsDisplayedAtPosition(HotelResultsScreen.hotelResultsList(), 2, R.id.unreal_deal_message, "Book this and save $110 (22%)");
	}

	private void assertHotelInfoSite() {
		PackageScreen.hotelDetailsToolbar().check(matches(hasDescendant(
			CoreMatchers.allOf(isDisplayed(), withText("Package Happy Path")))));
		float detailsHotelRating = EspressoUtils.getStarRatingValue(HotelInfoSiteScreen.hotelDetailsStarRating());
		assertEquals(4.0f, detailsHotelRating);
		onView(allOf(withId(R.id.user_rating), withText("4.4"))).check(matches(isDisplayed()));
		onView(withId(R.id.hotel_search_info)).check(matches(withText("Feb 3 - Feb 4")));
		onView(withId(R.id.hotel_search_info_guests)).check(matches(withText("1 guest")));
	}

	private void assertCheckout() {
		onView(allOf(withId(R.id.legal_information_text_view), withText(
			"By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, the Privacy Policy, and Fare Information.")))
			.perform(waitForViewToDisplay())
			.check(matches(isDisplayed()));
	}

	private void assertBundlePrice(String price, String totalText) {
		onView(allOf(withId(R.id.bundle_total_text), withText(totalText))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_includes_text), isDescendantOfA(withId(R.id.bundle_price_widget)),
			withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
			withText(
				"includes hotel and flights"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_total_price), withText(price))).check(matches(isDisplayed()));
	}

	private void assertBundlePriceInFlight(String price) {
		onView(allOf(withId(R.id.bundle_price_label), withText("Bundle Total"))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.bundle_price), withText(price + "/person"))).check(matches(isDisplayed()));
	}

	private void reviews() throws Throwable {
		HotelInfoSiteScreen.clickRatingContainer();
		HotelInfoSiteScreen.reviews().perform(waitForViewToDisplay());
		onView(withId(R.id.hotel_reviews_toolbar)).check(matches(withNavigationContentDescription("Close")));
		onView(withText(R.string.user_review_sort_button_critical)).perform(click());
		onView(withText(R.string.user_review_sort_button_favorable)).perform(click());
		onView(withText(R.string.user_review_sort_button_recent)).perform(click());
		Espresso.pressBack();
	}
}
