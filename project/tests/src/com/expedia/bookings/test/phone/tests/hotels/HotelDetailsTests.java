package com.expedia.bookings.test.phone.tests.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.PhoneTestCase;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.Espresso;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/14/14.
 */
public class HotelDetailsTests extends PhoneTestCase {

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

		int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView());
		for (int i = 1; i < totalHotels - 2; i++) {
			HotelsSearchScreen.clickListItem(i);
			String hotelName = EspressoUtils.getText(R.id.title);
			ScreenActions.enterLog(TAG, "Verifying VIP Dialog for hotel: " + hotelName);
			try {
				SettingsScreen.clickOKString();
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
	public void testDetailsUIElements() throws Exception {
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

		int totalHotels = EspressoUtils.getListCount(HotelsSearchScreen.hotelResultsListView()) - 1;
		for (int i = 1; i < totalHotels; i = i + 2) {
			DataInteraction searchResultRow = HotelsSearchScreen.hotelListItem().atPosition(i);
			String rowHotelName = EspressoUtils.getListItemValues(searchResultRow, R.id.name_text_view);
			searchResultRow.perform(click());
			try {
				ScreenActions.enterLog(TAG, "Verifying UI elements for details of: " + rowHotelName);
				if (!rowHotelName.isEmpty() && !rowHotelName.contains("...")) {
					String detailHotelsName = EspressoUtils.getText(R.id.title);
					ScreenActions.enterLog(TAG, "Testing that the hotel name: " + rowHotelName + " matches " + detailHotelsName);
					assertEquals(rowHotelName, detailHotelsName);
				}
				HotelsDetailsScreen.ratingBar().check(matches(isDisplayed()));
				HotelsDetailsScreen.hotelGallery().check(matches(isDisplayed()));
				HotelsDetailsScreen.bookNowButton().perform(scrollTo()).check(matches(isDisplayed()));
			}
			catch (Exception e) {
				CVVEntryScreen.clickOkButton();
			}
			Espresso.pressBack();
		}
	}
}
