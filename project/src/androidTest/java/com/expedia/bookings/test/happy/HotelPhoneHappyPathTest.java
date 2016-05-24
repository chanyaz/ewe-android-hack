package com.expedia.bookings.test.happy;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.mocke3.ExpediaDispatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelPhoneHappyPathTest extends HotelTestCase {

	public void testHotelPhoneHappyPath() throws Throwable {
		HotelScreen.doGenericSearch();
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

	public void testNewHotelPhoneHappyPathLoggedInCustomer() throws Throwable {
		HotelScreen.doGenericSearch();
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
