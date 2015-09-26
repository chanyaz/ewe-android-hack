package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;

public class HotelPriceChangeTest extends HotelTestCase {

	public void testPriceChange() throws Throwable {
		doSearch();
		selectHotel("hotel_price_change");
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
		final LocalDate start = DateTime.now().toLocalDate();
		final LocalDate end = start.plusDays(3);

		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(start, end);

		screenshot("Hotel_Search_Params_Entered");
		HotelScreen.clickSearchButton();
		HotelScreen.waitForResultsDisplayed();
	}

	private void selectHotel(String name) throws Throwable {
		screenshot("Hotel_Search_Results");
		HotelScreen.selectHotelWithName(name);
		HotelScreen.waitForDetailsDisplayed();
	}

	private void selectRoom() throws Throwable {
		screenshot("Hotel_Room");
		HotelScreen.clickAddRoom();
	}

	private void viewRoom(String name) throws Throwable {
		screenshot("Hotel_Room");
		HotelScreen.clickViewRoom(name);
		HotelScreen.clickAddRoom();
	}

	private void checkout() throws Throwable {
		screenshot("Hotel_Checkout");
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfoHotels();
		CheckoutViewModel.pressClose();
	}

	private void slideToPurchase() throws Throwable {
		screenshot("Hotel_Checkout_Ready_To_Purchase");
		CheckoutViewModel.performSlideToPurchase();
		CVVEntryScreen.waitForCvvScreen();
	}

	private void enterCVV() throws Throwable {
		CVVEntryScreen.enterCVV("123");
		screenshot("Hotel_CVV");
		CVVEntryScreen.clickBookButton();
	}

	private void verifyPriceChange(String price) throws Throwable {
		screenshot("Hotel_Checkout_Verify_Price_Change");
		onView(withId(R.id.price_change_text)).perform(ViewActions.waitForViewToDisplay());
		assertViewWithTextIsDisplayed(R.id.price_change_text, price);
		assertViewIsDisplayed(R.id.price_change_container);
	}
}
