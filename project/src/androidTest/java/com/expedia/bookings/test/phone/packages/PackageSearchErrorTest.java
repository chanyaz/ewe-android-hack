package com.expedia.bookings.test.phone.packages;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
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
		SearchScreen.searchEditText().perform(typeText("GGW"));
		Common.closeSoftKeyboard(SearchScreen.destination());
		SearchScreen.selectLocation("Glasgow, MT (GGW-Glasgow Intl.)");
		Common.delay(1);
		SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay());
		SearchScreen.searchEditText().perform(typeText("DTW"));
		SearchScreen.selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)");

		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());

		PackageScreen.assertErrorScreen("Edit Search", "We were unable to find any results. Please adjust your search.");
		onView(withId(R.id.error_action_button)).perform(click());
		SearchScreen.searchButton().check(matches(isDisplayed()));
	}

	public void testHotelOffersSearchError() throws Throwable {
		SearchScreen.selectOriginAndDestination();

		LocalDate startDate = LocalDate.now().plusDays(3);
		LocalDate endDate = LocalDate.now().plusDays(8);
		SearchScreen.selectDates(startDate, endDate);
		SearchScreen.searchButton().perform(click());
		PackageScreen.clickHotelBundle();
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("hotel offers error")), click()));

		PackageScreen.assertErrorScreen("Edit Search", "We were unable to find any results. Please adjust your search.");
		onView(withId(R.id.error_action_button)).perform(click());

		//Error action button takes back to search screen
		SearchScreen.searchButton().perform(click());
		PackageScreen.clickHotelBundle();
		HotelScreen.selectHotel("Package Happy Path");
		PackageScreen.selectRoom();

	}
}
