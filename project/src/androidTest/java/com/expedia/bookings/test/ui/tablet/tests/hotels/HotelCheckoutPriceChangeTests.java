package com.expedia.bookings.test.ui.tablet.tests.hotels;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class HotelCheckoutPriceChangeTests extends TabletTestCase {

	private static final String TAG = HotelCheckoutPriceChangeTests.class.getSimpleName();

	public void testHotelHeaderInfo() throws Exception {
		Common.enterLog(TAG, "START: HOTEL PRICE CHANGE TEST");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();
		Results.swipeUpHotelList();

		Results.clickHotelAtIndex(1);
		HotelDetails.clickSelectHotelWithRoomDescription("hotel_price_change_up");
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		verifyPriceChange();
		Espresso.pressBack();
		verifyPriceUpdated();
	}

	private void verifyPriceChange() {
		EspressoUtils.assertViewWithSubstringIsDisplayed("Price changed from");
	}

	private void verifyPriceUpdated() {
		// Verify the new price is shown on the rooms and rates.
		onView(allOf(withId(R.id.trip_bucket_price_text), isDisplayed())).check(
			matches(withText(containsString("$3,395"))));
	}

}
