package com.expedia.bookings.test.happy;

import org.joda.time.DateTime;

import android.support.test.espresso.Espresso;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsReviewsScreen;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.mocke3.ExpediaDispatcher;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class NewHotelPhoneHappyPathTest extends HotelTestCase {

	public void testNewHotelPhoneHappyPath() throws Throwable {
		doSearch();
		selectHotel();
		reviews();
		launchFullMap();
		selectRoom();
		checkout();
		slideToPurchase();
		enterCVV();
		confirmation();
		verifyTravelAdTracking();
	}

	private void launchFullMap() throws Throwable {
		Common.delay(1);
		HotelScreen.clickDetailsMiniMap();
		Common.delay(1);
		screenshot("Hotel_Detail_Map");
		HotelScreen.clickSelectARoomInFullMap();
	}

	private void reviews() throws Throwable {
		HotelScreen.clickRatingContainer();
		Common.delay(1);
		screenshot("Hotel_Reviews");
		HotelsReviewsScreen.clickCriticalTab();
		HotelsReviewsScreen.clickFavorableTab();
		HotelsReviewsScreen.clickRecentTab();
		// TO-DO: Change to close button once the functionality is implemented.
		Espresso.pressBack();
	}

	private void doSearch() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		screenshot("Hotel_Search_Params_Entered");
		HotelScreen.searchButton().perform(click());
		Common.delay(1);
	}

	private void selectHotel() throws Throwable {
		screenshot("Hotel_Search_Results");
		HotelScreen.selectHotel("happypath");
		Common.delay(1);
	}

	private void selectRoom() throws Throwable {
		screenshot("Hotel_Room");
		HotelScreen.clickAddRoom();
		Common.delay(1);
	}

	private void checkout() throws Throwable {
		screenshot("Hotel_Checkout");
		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfoHotels();
		CheckoutViewModel.pressClose();
	}

	private void slideToPurchase() throws Throwable {
		screenshot("Hotel_Checkout_Ready_To_Purchase");
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);
	}

	private void enterCVV() throws Throwable {
		CVVEntryScreen.enterCVV("123");
		screenshot("Hotel_CVV");
		CVVEntryScreen.clickBookButton();
	}

	private void confirmation() throws Throwable {
		screenshot("Hotel_Confirmation");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.itin_text_view, "Itinerary #174113329733");
	}

	private void verifyTravelAdTracking() {
		ExpediaDispatcher dispatcher = MockModeShim.getDispatcher();
		assertEquals(1, dispatcher.numOfTravelAdRequests("/travel"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/ads/hooklogic"));
	}
}
