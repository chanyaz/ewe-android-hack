package com.expedia.bookings.test.happy;

import org.junit.Test;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.mocke3.ExpediaDispatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelPhoneHappyPathTest extends HotelTestCase {

	@Test
	public void testHotelPhoneHappyPath() throws Throwable {
		SearchScreen.searchEditText().check(ViewAssertions.matches(withHint("Enter Destination")));
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");
		reviews();
		launchFullMap();
		HotelInfoSiteScreen.bookFirstRoom();

		CheckoutScreen.clickDone();
		CheckoutScreen.enterTravelerInfo();

		CheckoutScreen.enterPaymentInfoHotels();
		CheckoutScreen.performSlideToPurchase(false);
		HotelCheckoutScreen.enterCVVAndBook();
		assertICanSeeItinNumber();
		verifyTravelAdTracking();
	}

	@Test
	public void testNewHotelPhoneHappyPathLoggedInCustomer() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");
		HotelInfoSiteScreen.bookFirstRoom();
		CheckoutScreen.clickDone();

		CheckoutScreen.loginAsQAUser();

		// checkout
		CheckoutScreen.selectStoredCard(true);
		CheckoutScreen.clickDone();
		CheckoutScreen.selectStoredTraveler();
		Common.delay(1);

		CheckoutScreen.performSlideToPurchase(true);

		assertICanSeeItinNumber();
	}

	@Test
	public void testSingleStoredCard() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");

		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("happypath_2_night_stay_0");

		CheckoutScreen.enterSingleCardLoginDetails();

		CheckoutScreen.pressDoLogin();
		CheckoutScreen.performSlideToPurchase(true);

		onView(withId(R.id.itin_text_view)).check(matches((withText("Itinerary #184327605820"))));
		assertViewIsDisplayed(R.id.confirmation_text);
	}

	@Test
	public void testNoStoredCard() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");

		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("happypath_0");

		CheckoutScreen.clickLogin();
		CheckoutScreen.enterUsername("nostoredcards@mobiata.com");
		CheckoutScreen.enterPassword("password");
		CheckoutScreen.pressDoLogin();
		Common.delay(1);
		CheckoutScreen.paymentInfo().perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Enter payment details");
		CheckoutScreen.clickPaymentInfo();
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");

	}

	private void assertICanSeeItinNumber() throws Throwable {
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.itin_text_view, "Itinerary #174113329733");
	}

	private void reviews() throws Throwable {
		HotelInfoSiteScreen.clickRatingContainer();
		Common.delay(1);
		onView(withText(R.string.user_review_sort_button_critical)).perform(click());
		onView(withText(R.string.user_review_sort_button_favorable)).perform(click());
		onView(withText(R.string.user_review_sort_button_recent)).perform(click());
		// TO-DO: Change to close button once the functionality is implemented.
		Espresso.pressBack();
	}

	private void launchFullMap() throws Throwable {
		Common.delay(1);
		HotelInfoSiteScreen.clickDetailsMiniMap();
		Common.delay(1);
		HotelInfoSiteScreen.clickSelectARoomInFullMap();
	}

	private void verifyTravelAdTracking() {
		ExpediaDispatcher dispatcher = MockModeShim.getDispatcher();
		assertEquals(1, dispatcher.numOfTravelAdRequests("/travel"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/ads/hooklogic"));
	}
}
