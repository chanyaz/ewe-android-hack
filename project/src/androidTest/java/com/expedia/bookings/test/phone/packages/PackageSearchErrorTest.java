package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.TestValues;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PackageSearchErrorTest extends PackageTestCase {

	public void testSearchError() throws Throwable {
		SearchScreen.origin().perform(click());
		SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_GGW));
		Common.closeSoftKeyboard(SearchScreen.origin());
		SearchScreenActions.selectLocation(TestValues.ORIGIN_LOCATION_GGW);
		Common.delay(1);
		SearchScreen.waitForSearchEditText().perform(typeText(TestValues.DESTINATION_LOCATION_DTW));
		SearchScreenActions.selectLocation(TestValues.DESTINATION_LOCATION_DTW);

		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreenActions.chooseDatesWithDialog(startDate, endDate);
		SearchScreen.searchButton().perform(click());

		PackageScreen.assertErrorScreen("Edit Search", "Sorry, we were unable to find any results. Please modify your search criteria and try again.");
		onView(withId(R.id.error_action_button)).perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
	}

	public void testHotelOffersSearchError() throws Throwable {
		SearchScreenActions.selectPackageOriginAndDestination();

		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreenActions.chooseDatesWithDialog(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		HotelResultsScreen.hotelResultsList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("hotel offers error")), click()));

		PackageScreen.assertErrorScreen("Edit Search", "Sorry, we were unable to find any results. Please modify your search criteria and try again.");
		onView(withId(R.id.error_action_button)).perform(click());

		//Error action button takes back to search screen
		SearchScreen.searchButton().perform(click());
		HotelResultsScreen.selectHotel("Package Happy Path");
		HotelInfoSiteScreen.bookFirstRoom();
	}
}
