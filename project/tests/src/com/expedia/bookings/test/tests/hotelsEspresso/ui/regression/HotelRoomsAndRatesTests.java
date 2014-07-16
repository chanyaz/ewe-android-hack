package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.PhoneTestCase;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by napandey on 7/10/2014.
 */
public class HotelRoomsAndRatesTests extends PhoneTestCase {

	public void testRatesAndRHeaderInfo() throws Exception {
		// initial Search
		initiateSearch();
		//fetch the total hotel count
		final int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());

		//iterate through each hotel till end of the hotel list
		for (int hotelPosition=1; hotelPosition < totalHotels; hotelPosition++) {
			//click on the hotel
			HotelsSearchScreen.clickListItem(hotelPosition);
			// fetching the hotelName
			String hotelName = EspressoUtils.getText(R.id.title);
			// get the ratings as displayed on HotelDetailsScreen
			float detailsHotelRating = EspressoUtils.getRatingValue(HotelsDetailsScreen.ratingBar());

			//select the hotel
			HotelsDetailsScreen.clickSelectButton();

			// check if the hotel rooms and rates screen matches the hotel name, previously captured
			HotelsRoomsRatesScreen.hotelNameTextView().check(matches(withText(hotelName)));

			//fetch the ratings value from the Hotel Room and Rates Screen
			float roomsRatesHotelRating = EspressoUtils.getRatingValue(HotelsRoomsRatesScreen.hotelRatingBar());

			assertEquals(detailsHotelRating, roomsRatesHotelRating);

			checkAdditionalFees();

			checkRenovationNotice();

			//fetch the number of options available
			int typesOfRoomsAvailable = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;

			//iterate over the options and check if the option as well as price is present
			for (int ratePosition = 0; ratePosition < typesOfRoomsAvailable; ratePosition++) {
				DataInteraction roomsRatesRow = HotelsRoomsRatesScreen.listItem().atPosition(ratePosition);
				//fetch the roomOptionText
				String roomOptionText = EspressoUtils.getListItemValues(roomsRatesRow, R.id.room_description_text_view);
				//fetch the priceText
				String priceText = EspressoUtils.getListItemValues(roomsRatesRow, R.id.price_text_view);
				//assert that they must not be ""
				assertFalse("On Hotel Name : " + hotelName + ", item number : " + ratePosition + ", room option :" + roomOptionText, roomOptionText.equals(""));
				assertFalse("On Hotel Name : " + hotelName + ", item number : " + ratePosition + ", price :" + priceText, priceText.equals(""));
			}
			HotelsRoomsRatesScreen.clickBackButton();
			HotelsDetailsScreen.clickBackButton();
			ScreenActions.enterLog(TAG, "_________[" + hotelPosition + "] hotels Processed , remaining [" + (totalHotels - hotelPosition) + "]");
		}
	}

	private static final String TAG = HotelRoomsAndRatesTests.class.getSimpleName();

	private void checkAdditionalFees() {
		try {
			HotelsRoomsRatesScreen.additionalFeesInfoButton().check(matches(isDisplayed()));
			HotelsRoomsRatesScreen.clickAdditionalFeesInfoButton();
			Espresso.pressBack();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Additional Info button is not present: ");
		}
	}

	private void checkRenovationNotice() {
		try {
			HotelsRoomsRatesScreen.renovationInfoButton().check(matches(isDisplayed()));
			HotelsRoomsRatesScreen.clickRenovationInfoButton();
			Espresso.pressBack();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Renovation Info button is not present: ");
		}
	}

	private void initiateSearch() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("San Francisco, CA");
		HotelsSearchScreen.clickSuggestion(getActivity(), "San Francisco, CA");
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
	}

}