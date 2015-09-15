package com.expedia.bookings.test.phone.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public class HotelDetailsTest extends PhoneTestCase {

	/*
	* #206 eb_tp plan
	 */

	// Verify that the correct dialog appears after clicking the VIP Access image in
	// on the image gallery
	public void testVIPAccessDialog() throws Throwable {
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

		HotelsSearchScreen.clickHotelWithName("vip_hotel");

		//Verifying VIP Dialog for vip_hotel
		HotelsDetailsScreen.clickVIPImageView();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.vip_access_message));
		CVVEntryScreen.clickOkButton();
		Espresso.pressBack();
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

		DataInteraction searchResultRow = HotelsSearchScreen.hotelListItem().atPosition(1);
		String rowHotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view);
		searchResultRow.perform(click());

		//Verifying UI elements on details screen matches the views on results screen
		String detailHotelsName = EspressoUtils.getText(R.id.title);
		assertEquals(rowHotelName, detailHotelsName);

		HotelsDetailsScreen.hotelGallery().check(matches(isDisplayed()));

		HotelsDetailsScreen.ratingBar().check(matches(isDisplayed()));

		EspressoUtils.assertViewIsDisplayed(R.id.rate_text_view);

		HotelsDetailsScreen.bookNowButton().perform(scrollTo()).check(matches(isDisplayed()));

		Espresso.pressBack();

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
