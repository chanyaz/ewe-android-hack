package com.expedia.bookings.test.phone.hotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.mobiata.android.Log;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

public class HotelCheckoutPriceChangeTest extends PhoneTestCase {

	private static final String TAG = HotelCheckoutPriceChangeTest.class.getSimpleName();

	public void testCheckHotels() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		Log.v(TAG, "Setting hotel search city to: " + "New York, NY");
		HotelsSearchScreen.enterSearchText("SFO");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "San Francisco, CA (SFO-San Francisco Intl.)");
		HotelsSearchScreen.clickHotelWithName("hotel_price_change");
		HotelsDetailsScreen.clickSelectButton();
		Log.v(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoomItemWithPriceChange(0);
		verifyPriceChangeDecrease();
		HotelsRoomsRatesScreen.dismissPriceChange();
		verifyPriceUpdated("$1,395.88");
		Espresso.pressBack();
		HotelsRoomsRatesScreen.selectRoomItemWithPriceChange(1);
		verifyPriceChangeIncrease();
		HotelsRoomsRatesScreen.dismissPriceChange();
		verifyPriceUpdated("$3,395.88");
	}

	private void verifyPriceChangeDecrease() {
		EspressoUtils.assertViewWithSubstringIsDisplayed("We've got great news!");
	}

	private void verifyPriceChangeIncrease() {
		EspressoUtils.assertViewWithSubstringIsDisplayed("Sorry, but it looks like the hotel");
	}

	private void verifyPriceUpdated(String price) {
		// Verify the new price is shown on the trip overview page.
		onView(withId(R.id.price_text)).check(matches(withText(containsString(price))));
	}

}
