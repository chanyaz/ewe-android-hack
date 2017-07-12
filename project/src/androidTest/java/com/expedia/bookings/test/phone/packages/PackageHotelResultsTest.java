package com.expedia.bookings.test.phone.packages;

import org.hamcrest.CoreMatchers;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.CustomMatchers;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;
import com.expedia.bookings.test.pagemodels.packages.PackageScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;
import static org.hamcrest.Matchers.not;

public class PackageHotelResultsTest extends PackageTestCase {

	@Test
	public void testResultsHeader() throws Throwable {
		PackageScreen.searchPackage();
		PackageScreen.hotelResultsHeader().check(matches(withText("48 Results")));
	}

	@Test
	public void testFilterBtn() throws Throwable {
		PackageScreen.searchPackage();
		onView(withId(R.id.filter_text)).check(matches(not(isDisplayed())));
		onView(withId(R.id.menu_open_search)).check(doesNotExist());
		onView(withId(R.id.filter_count_text)).check(matches(not(isDisplayed())));
		onView(withId(R.id.filter_btn)).perform(click());
		onView(withId(R.id.hotel_filter_rating_four)).perform(click());
		pressBack();
		assertViewWithTextIsDisplayed(R.id.filter_count_text, "1");
		onView(withId(R.id.filter_text)).check(matches(not(isDisplayed())));
		HotelScreen.mapFab().perform(click());
		onView(withId(R.id.filter_text)).check(matches(isDisplayed()));
		assertViewWithTextIsDisplayed(R.id.filter_count_text, "1");
	}

	@Test
	public void testToolbarText() throws Throwable {
		PackageScreen.searchPackageFor(2, 1);
		PackageScreen.hotelResultsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(
			isDisplayed(), withText("Hotels in Detroit, MI")))));

		String startDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(3));
		String endDate = DateUtils.localDateToMMMd(LocalDate.now().plusDays(8));
		PackageScreen.hotelResultsToolbar().check(matches(hasDescendant(CoreMatchers.allOf(
			isDisplayed(), withText(startDate + " - " + endDate + ", 3 Guests")))));
	}

	@Test
	public void testUIElements() throws Throwable {
		PackageScreen.searchPackage();
		onView(withId(R.id.list_view))
			.check(matches(CustomMatchers.atPosition(2, hasDescendant(withText("Includes flights + hotel")))));
	}
}
