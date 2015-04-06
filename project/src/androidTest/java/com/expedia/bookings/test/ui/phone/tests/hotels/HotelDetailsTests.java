package com.expedia.bookings.test.ui.phone.tests.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/14/14.
 */
public class HotelDetailsTests extends PhoneTestCase {

	/*
	* #206 eb_tp plan
	 */
	private static final String TAG = HotelDetailsTests.class.getName();

	// Verify that the correct dialog appears after clicking the VIP Access image in
	// on the image gallery
	public void testVIPAccessDialog() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickOnFilterButton();
		HotelsSearchScreen.filterMenu().clickVIPAccessFilterButton();
		Espresso.pressBack();

		for (int i = 1; i < 5; i++) {
			HotelsSearchScreen.clickListItem(i);

			String hotelName = EspressoUtils.getText(R.id.title);
			ScreenActions.enterLog(TAG, "Verifying VIP Dialog for hotel: " + hotelName);
			try {
				SettingsScreen.clickOkString();
				HotelsGuestPicker.searchButton().check(matches(isDisplayed()));
				ScreenActions.enterLog(TAG, "Room sold out popup was displayed");
			}
			catch (Exception e) {
				HotelsDetailsScreen.clickVIPImageView();
				EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.vip_access_message));
				CVVEntryScreen.clickOkButton();
				Espresso.pressBack();
			}
		}
	}

	// Verify that some UI Elements are present on the hotel details screen
	public void verifyDetailsUIElements() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		for (int i = 1; i < 5; i++) {
			DataInteraction searchResultRow = HotelsSearchScreen.hotelListItem().atPosition(i);
			String rowHotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view);
			searchResultRow.perform(click());
			ScreenActions.enterLog(TAG, "Verifying UI elements for details of: " + rowHotelName);
			if (!rowHotelName.isEmpty() && !rowHotelName.contains("...")) {
				String detailHotelsName = EspressoUtils.getText(R.id.title);
				ScreenActions.enterLog(TAG, "Testing that the hotel name: " + rowHotelName + " matches " + detailHotelsName);
				assertEquals(rowHotelName, detailHotelsName);
			}
			HotelsDetailsScreen.hotelGallery().check(matches(isDisplayed()));
			ScreenActions.enterLog(TAG, "the photo gallery is displayed");

			HotelsDetailsScreen.ratingBar().check(matches(isDisplayed()));
			ScreenActions.enterLog(TAG, "user reviews is displayed");

			EspressoUtils.assertViewIsDisplayed(R.id.rate_text_view);
			ScreenActions.enterLog(TAG, "hotel price is displayed");

			HotelsDetailsScreen.bookNowButton().perform(scrollTo()).check(matches(isDisplayed()));
			ScreenActions.enterLog(TAG, "Book now button is displayed");

			Espresso.pressBack();
		}
	}

	public void testBookingInfoUSPOS() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		verifyDetailsUIElements();
	}

	public void testBookingInfoBrazilPOS() throws Throwable {
		setPOS(PointOfSaleId.BRAZIL);
		verifyDetailsUIElements();
	}

	public void testBookingInfoCanadaPOS() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		verifyDetailsUIElements();
	}
}
