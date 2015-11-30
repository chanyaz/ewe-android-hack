package com.expedia.bookings.test.phone.sweep;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelReceiptModel;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 1/26/15.
 */
public class ETPScreenshotSweep extends PhoneTestCase {

	public void testBookHotelNonZeroRoom() throws Throwable {
		Common.setLocale(getLocale());
		Common.setPOS(PointOfSaleId.valueOf(getPOS(getLocale())));

		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Trump International Hotel Las Vegas");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickHotelWithName("Trump International Hotel Las Vegas");
		screenshot("Hotels_Details");
		HotelsDetailsScreen.bookNowButton().perform(scrollTo());
		screenshot("Fees");
		onView(withId(R.id.pay_later_info_text)).perform(scrollTo());
		Common.delay(1);
		HotelsDetailsScreen.clickBookNowPayLater();
		screenshot("Book_Now_Pay_Later");
		HotelsRoomsRatesScreen.clickSelectRoomButton();
		screenshot("Hotel_rooms_rates");
		HotelsRoomsRatesScreen.clickPayLaterButton();
		screenshot("Pay_later");
		HotelsRoomsRatesScreen.selectETPRoomItem(2);
		screenshot("Hotel_checkout");
		try {
			Common.clickOkString();
		}
		catch (Exception e) {
			//No Great news pop-up
		}
		HotelReceiptModel.clickGrandTotalTextView();
		screenshot("Grand_total_summary");
		Common.pressBack();
		HotelsCheckoutScreen.clickCheckoutButton();
		screenshot("Checkout");

	}
}
