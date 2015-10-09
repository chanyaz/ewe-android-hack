package com.expedia.bookings.test.ui.phone.tests.hotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

public class HotelCheckoutPriceChangeTests extends PhoneTestCase {

	private static final String TAG = HotelCheckoutPriceChangeTests.class.getSimpleName();

	public void testCheckHotels() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "New York, NY");
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		HotelsSearchScreen.clickListItem(2);
		HotelsDetailsScreen.clickSelectButton();
		try {
			onView(withText("OK")).perform(click());
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Not available right now");
		}
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoomItemWithPriceChange(0);
		verifyPriceChangeDecrease();
		HotelsRoomsRatesScreen.dismissPriceChange();
		Espresso.pressBack();
		HotelsRoomsRatesScreen.selectRoomItemWithPriceChange(1);
		verifyPriceChangeIncrease();
		HotelsRoomsRatesScreen.dismissPriceChange();
		Espresso.pressBack();
		verifyPriceUpdated();
	}

	private void verifyPriceChangeDecrease() {
		EspressoUtils.assertViewWithSubstringIsDisplayed("We've got great news!");
	}

	private void verifyPriceChangeIncrease() {
		EspressoUtils.assertViewWithSubstringIsDisplayed("Sorry, but it looks like the hotel");
	}

	private void verifyPriceUpdated() {
		// Verify the new price is shown on the rooms and rates.
		HotelsRoomsRatesScreen.getRoomRow(2).onChildView(withId(R.id.price_text_view))
			.check(matches(withText(containsString("$1,229"))));
	}

}
