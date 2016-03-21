package com.expedia.bookings.test.phone.hotels;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

public class HotelSearchTest extends HotelTestCase {

	public void testHotelSearch() throws Throwable {

		//By default search text field should be empty
		HotelScreen.location().check(matches(withHint(R.string.search_location)));

		//tapping on "X" icon should should clear out values
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.clearButton().perform(click());
		HotelScreen.location().check(matches(withHint("Search Location")));

		// raw text search
		HotelScreen.location().perform(typeText("114 Sansome St"));
		Espresso.closeSoftKeyboard();
		Common.delay(5);
		HotelScreen.suggestionMatches(withText(equalTo("\"114 Sansome St\"")), 0);
		HotelScreen.clearButton().perform(click());

		//Trigger search on typing 3 letters
		HotelScreen.location().perform(typeText("SFO"));
		Espresso.closeSoftKeyboard();
		Common.delay(5);
		HotelScreen.suggestionMatches(withText(startsWith("San Francisco")), 1);
		//Airports will have airport icon
		HotelScreen.suggestionMatches(allOf(withId(R.id.icon_imageview),
			withImageDrawable(R.drawable.airport_suggest)), 1);

		HotelScreen.suggestionMatches(withText(startsWith("Hyatt Regency")), 4);
		//Hotel name will have a bed icon
		HotelScreen.suggestionMatches(allOf(withId(R.id.icon_imageview),
			withImageDrawable(R.drawable.hotel_suggest)), 4);

		//hotel search by name
		HotelScreen.selectLocation("Hyatt Regency San Francisco");
		LocalDate startDate = LocalDate.now().plusDays(35);
		HotelScreen.selectDates(startDate, null);
		HotelScreen.searchButton().perform(click());
		HotelScreen.waitForDetailsLoaded();
		HotelScreen.selectRoomButton().check(matches((isDisplayed())));
	}

	public void testLocationWithNoHotels() throws Throwable {
		//Search Glasgow
		HotelScreen.location().perform(typeText("Glasgow, MT"));
		Espresso.closeSoftKeyboard();
		HotelScreen.selectLocation("Glasgow, MT (GGW-Glasgow Intl.)");
		LocalDate startDate = LocalDate.now().plusDays(35);
		HotelScreen.selectDates(startDate, null);
		HotelScreen.searchButton().perform(click());

		//Test error screen
		HotelScreen.waitForErrorDisplayed();
		HotelScreen.hotelErrorToolbar().check(matches(hasDescendant(CoreMatchers.allOf(
			isDisplayed(), withText("Glasgow, MT (GGW-Glasgow Intl.)")))));
		ErrorScreen.clickOnEditSearch();

		//Assert search screen is displayed
		onView(withId(R.id.search_container)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.search_container);
	}
}
