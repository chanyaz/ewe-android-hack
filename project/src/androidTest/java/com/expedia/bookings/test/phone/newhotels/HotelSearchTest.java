package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

public class HotelSearchTest extends HotelTestCase {

	public void testHotelSearch() throws Throwable {

		//By default search text field should be empty
		HotelScreen.location().check(matches(withHint(R.string.search_location)));

		//tapping on "X" icon should should clear out values
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.clearButton().perform(click());
		HotelScreen.location().check(matches(withHint("Search Location")));

		//Trigger search on typing 3 letters
		HotelScreen.location().perform(typeText("SFO"));
		Espresso.closeSoftKeyboard();
		Common.delay(5);
		HotelScreen.suggestionMatches(withText(startsWith("San Francisco")), 0);

		//Airports will have airport icon
		HotelScreen.suggestionMatches(allOf(withId(R.id.icon_imageview),
			withImageDrawable(R.drawable.airport_suggest)), 0);

		//Hotel name will have a bed icon
		HotelScreen.suggestionMatches(withText(startsWith("Hyatt Regency")), 7);
		HotelScreen.suggestionMatches(allOf(withId(R.id.icon_imageview),
			withImageDrawable(R.drawable.hotel_suggest)), 7);

		//hotel search by name
		HotelScreen.selectLocation("Hyatt Regency San Francisco");
		LocalDate startDate = LocalDate.now().plusDays(35);
		HotelScreen.selectDates(startDate, null);
		HotelScreen.searchButton().perform(click());
		HotelScreen.waitForDetailsDisplayed();
		HotelScreen.selectRoomButton().check(matches((isDisplayed())));
	}
}
