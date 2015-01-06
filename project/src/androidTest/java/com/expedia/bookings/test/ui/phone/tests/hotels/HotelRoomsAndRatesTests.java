package com.expedia.bookings.test.ui.phone.tests.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAssertion;

import static com.expedia.bookings.test.ui.espresso.CustomMatchers.isEmpty;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.withRating;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

public class HotelRoomsAndRatesTests extends PhoneTestCase {

	private static final String TAG = HotelRoomsAndRatesTests.class.getSimpleName();

	public void testRoomsAndRatesHeaderInfo() throws Exception {
		initiateSearch();
		final int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());

		for (int hotelPosition = 1; hotelPosition < totalHotels; hotelPosition++) {
			HotelsSearchScreen.clickListItem(hotelPosition);

			String hotelName = EspressoUtils.getText(R.id.title);
			final float detailsHotelRating = EspressoUtils.getRatingValue(HotelsDetailsScreen.ratingBar());

			HotelsDetailsScreen.clickSelectButton();

			HotelsRoomsRatesScreen.hotelNameTextView().check(matches(withText(hotelName)));

			final float roomsRatesHotelRating = EspressoUtils.getRatingValue(HotelsRoomsRatesScreen.hotelRatingBar());

			HotelsRoomsRatesScreen.hotelRatingBar().check(matches(withRating(detailsHotelRating)));

			checkAdditionalFees();

			checkRenovationNotice();

			//fetch the number of options available
			int typesOfRoomsAvailable = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;

			//iterate over the options and check if the option as well as price is present
			for (int ratePosition = 0; ratePosition < typesOfRoomsAvailable; ratePosition++) {
				ViewAssertion priceNotEmpty = selectedDescendantsMatch(withId(R.id.price_text_view), not(isEmpty()));
				ViewAssertion roomNameNotEmpty = selectedDescendantsMatch(withId(R.id.room_description_text_view), not(isEmpty()));

				HotelsRoomsRatesScreen.listItem().atPosition(ratePosition).check(priceNotEmpty);
				HotelsRoomsRatesScreen.listItem().atPosition(ratePosition).check(roomNameNotEmpty);
			}
			HotelsRoomsRatesScreen.clickBackButton();
			HotelsDetailsScreen.clickBackButton();
			ScreenActions.enterLog(TAG, "_________[" + hotelPosition + "] hotels Processed , remaining [" + (totalHotels - hotelPosition) + "]");
		}
	}

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
		HotelsSearchScreen.enterSearchText("SFO");
		HotelsSearchScreen.clickSuggestionAtIndex(getActivity(), 1);
	}

}
