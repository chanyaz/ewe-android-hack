package com.expedia.bookings.test.happy;

import org.junit.Test;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
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
		HotelScreen.selectHotel();
		reviews();
		launchFullMap();
		HotelScreen.selectRoom();

		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterTravelerInfo();

		CheckoutViewModel.enterPaymentInfoHotels();
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		assertICanSeeItinNumber();
		verifyTravelAdTracking();
	}

	@Test
	public void testNewHotelPhoneHappyPathLoggedInCustomer() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel();
		HotelScreen.selectRoom();
		CheckoutViewModel.clickDone();

		HotelScreen.doLogin();
		CheckoutViewModel.selectStoredTraveler();
		Common.delay(1);

		// checkout
		CheckoutViewModel.selectStoredCard(true);
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase(true);

		assertICanSeeItinNumber();
	}

	@Test
	public void testSingleStoredCard() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath");

		HotelScreen.selectRoomButton().perform(click());
		HotelScreen.clickRoom("happypath_2_night_stay_0");
		HotelScreen.clickAddRoom();

		CheckoutViewModel.enterSingleCardLoginDetails();

		CheckoutViewModel.pressDoLogin();
		CheckoutViewModel.performSlideToPurchase(true);

		onView(withId(R.id.itin_text_view)).check(matches((withText("Itinerary #184327605820"))));
		assertViewIsDisplayed(R.id.confirmation_text);
	}

	@Test
	public void testNoStoredCard() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath");

		HotelScreen.selectRoomButton().perform(click());
		HotelScreen.clickRoom("happypath_0");
		HotelScreen.clickAddRoom();

		CheckoutViewModel.clickLogin();
		CheckoutViewModel.enterUsername("nostoredcards@mobiata.com");
		CheckoutViewModel.enterPassword("password");
		CheckoutViewModel.pressDoLogin();
		Common.delay(1);
		CheckoutViewModel.paymentInfo().perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Payment Method");
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");

	}

	private void assertICanSeeItinNumber() throws Throwable {
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.itin_text_view, "Itinerary #174113329733");
	}

	private void reviews() throws Throwable {
		HotelScreen.clickRatingContainer();
		Common.delay(1);
		onView(withText(R.string.user_review_sort_button_critical)).perform(click());
		onView(withText(R.string.user_review_sort_button_favorable)).perform(click());
		onView(withText(R.string.user_review_sort_button_recent)).perform(click());
		// TO-DO: Change to close button once the functionality is implemented.
		Espresso.pressBack();
	}

	private void launchFullMap() throws Throwable {
		Common.delay(1);
		HotelScreen.clickDetailsMiniMap();
		Common.delay(1);
		HotelScreen.clickSelectARoomInFullMap();
	}

	private void verifyTravelAdTracking() {
		ExpediaDispatcher dispatcher = MockModeShim.getDispatcher();
		assertEquals(1, dispatcher.numOfTravelAdRequests("/travel"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/ads/hooklogic"));
	}
}
