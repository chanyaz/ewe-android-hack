package com.expedia.bookings.test.happy;

import org.joda.time.DateTime;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;


import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;

public class HotelPriceChangeTest extends HotelTestCase {

	public void testPriceChange() throws Throwable {
		doSearch();
		selectHotel();
		selectRoom();
		//Create Trip Price Change
		verifyPriceChange("Price changed from $2,394.88");
		Espresso.pressBack();
		viewRoom("hotel_price_change_checkout");
		checkout();
		slideToPurchase();
		enterCVV();
		//Checkout Price Change
		verifyPriceChange("Price changed from $675.81");
	}

	private void doSearch() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		screenshot("Hotel_Search_Params_Entered");
		HotelScreen.searchButton().perform(click());
		Common.delay(1);
	}

	private void selectHotel() throws Throwable {
		screenshot("Hotel_Search_Results");
		HotelScreen.selectHotel(3);
		Common.delay(1);
	}

	private void selectRoom() throws Throwable {
		screenshot("Hotel_Room");
		HotelScreen.clickAddRoom();
		Common.delay(1);
	}

	private void viewRoom(String name) throws Throwable {
		screenshot("Hotel_Room");
		HotelScreen.clickViewRoom(name);
		HotelScreen.clickAddRoom();
		Common.delay(1);
	}

	private void checkout() throws Throwable {
		screenshot("Hotel_Checkout");
		CheckoutViewModel.clickCheckout();
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
		CVVEntryScreen.parseAndEnterCVV("123");
		screenshot("Hotel_CVV");
		CVVEntryScreen.clickBookButton();
	}

	private void verifyPriceChange(String price) throws Throwable {
		screenshot("Hotel_Checkout");
		assertViewWithTextIsDisplayed(R.id.price_change_text, price);
		assertViewIsDisplayed(R.id.price_change_container);
	}
}
