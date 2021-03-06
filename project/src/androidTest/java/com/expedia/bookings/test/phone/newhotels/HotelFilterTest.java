package com.expedia.bookings.test.phone.newhotels;

import android.support.test.espresso.contrib.RecyclerViewActions;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doneButton;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.filterHotelName;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.filterResultsSnackBar;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.filterResultsSnackBarCounter;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.filterVip;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.sortFilter;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class HotelFilterTest extends HotelTestCase {

	//clear filter, all select item deselected
	public void testClearFilter() throws Throwable {
		doGenericSearch();
		sortFilter();

		filterHotelName().perform(typeText("Hilton"));
		Common.closeSoftKeyboard(filterHotelName());
		onView(withId(R.id.filter_vip_container)).perform(click());
		onView(withId(R.id.rating_one_background)).perform(click());
		onView(withId(R.id.collapsed_container)).perform(scrollTo());
		onView(withText("Civic Center")).perform(click());

		HotelScreen.clearFilter().perform(click());
		filterHotelName().check(matches(withText("")));
		filterVip().check(matches(isNotChecked()));
		onView(allOf(hasSibling(withText("Civic Center")), withId(R.id.neighborhood_check_box))).check(matches(isNotChecked()));
	}

	public void testFilterSnackBar() throws Throwable {
		doGenericSearch();
		sortFilter();
		//initially, results snack bar hides
		filterResultsSnackBar().check(matches(not(isDisplayed())));
		//click filters, results snack bar shows
		onView(withId(R.id.filter_vip_container)).perform(click());
		filterResultsSnackBarCounter().check(matches(withText("1 Result")));

		onView(withId(R.id.rating_one_background)).perform(click());
		filterResultsSnackBar().check(matches(isDisplayed()));
		filterResultsSnackBarCounter().check(matches(withText("0 Results")));

		//deselect the filters, results snack bar hides
		onView(withId(R.id.rating_one_background)).perform(click());
		onView(withId(R.id.filter_vip_container)).perform(click());
		filterResultsSnackBar().check(matches(not(isDisplayed())));

		//clear filter,results snack bar hides
		onView(withId(R.id.rating_one_background)).perform(click());
		HotelScreen.clearFilter().perform(click());
		filterResultsSnackBar().check(matches(not(isDisplayed())));
	}


	public void testFilterReturnToResult() throws Throwable {
		//zero results, done button is disabled
		doGenericSearch();
		sortFilter();
		filterHotelName().perform(typeText("Hilton"));
		Common.closeSoftKeyboard(filterHotelName());
		onView(withId(R.id.rating_one_background)).perform(click());
		doneButton().perform(click());
		doneButton().check(matches(isDisplayed()));

		//from list to filter, return to result list
		HotelScreen.clearFilter().perform(click());
		onView(withId(R.id.rating_four_background)).perform(click());
		doneButton().perform(click());
		HotelScreen.waitForResultsLoaded();

		Common.delay(2);
		//from map to filter, return to result map
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(4));
		HotelScreen.mapFab().perform(click());
		Common.delay(2);
		onView(withId(R.id.filter_count_text)).perform(click());

		HotelScreen.waitForFilterDisplayed();
		doneButton().check(matches(isDisplayed()));
		Common.delay(2);
		doneButton().perform(click());
		HotelScreen.waitForMapDisplayed();
	}

	public void testNeighborhood() throws Throwable {
		doGenericSearch();
		sortFilter();
		Common.delay(2);

		//click show more. show less
		onView(withId(R.id.show_more_less_text)).check(matches(withText("SHOW MORE")));
		onView(withId(R.id.collapsed_container)).perform(scrollTo());
		onView(withId(R.id.collapsed_container)).perform(click());
		onView(withId(R.id.show_more_less_text)).check(matches(withText("SHOW LESS")));
		Common.delay(2);
		onView(withId(R.id.collapsed_container)).perform(scrollTo());
		onView(withId(R.id.collapsed_container)).perform(click());
		onView(withId(R.id.show_more_less_text)).check(matches(withText("SHOW MORE")));

		//select and deselect one row
		onView(withText("Civic Center")).perform(click());
		filterResultsSnackBar().check(matches(isDisplayed()));

		onView(withText("Civic Center")).perform(click());
		filterResultsSnackBar().check(matches(not(isDisplayed())));
	}
}
