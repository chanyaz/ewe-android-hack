package com.expedia.bookings.test.phone.hotels;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps;
import com.expedia.bookings.test.support.User;
import org.hamcrest.Matchers;

import android.view.View;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsNotDisplayed;

public class HotelFavoritesTest extends PhoneTestCase {

	@Test
	public void testFavoritesButtonShown() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.HotelShortlist);
		getSearchResults();
		onView(withId(R.id.menu_favorites)).perform(click());
		assertViewIsDisplayed(R.id.hotel_favorites_toolbar);
		assertViewIsDisplayed(R.id.hotel_favorites_empty_container);
		assertViewIsNotDisplayed(R.id.hotel_favorites_recycler_view);
		onView(withContentDescription("Close")).perform(click());
		assertViewIsDisplayed(R.id.menu_favorites);
	}

	@Test
	public void testFavoritesButtonNotShownNoAbTest() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.HotelShortlist, AbacusVariant.CONTROL.getValue());
		getSearchResults();
		onView(withId(R.id.menu_favorites)).check(doesNotExist());
	}

	@Test
	public void testFavoritesListShown() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.HotelShortlist);
		login();
		getSearchResults();

		onView(withId(R.id.menu_favorites)).perform(click());
		assertViewIsNotDisplayed(R.id.hotel_favorites_empty_container);
		Matcher<View> favoritesListMatcher = hasDescendant(Matchers.allOf(withId(R.id.hotel_name), isDisplayed()));
		onView(withId(R.id.hotel_favorites_recycler_view)).perform(ViewActions.waitFor(favoritesListMatcher, 10, TimeUnit.SECONDS));
	}

	private void login() throws Throwable {
		HomeScreenSteps.switchToTab("Account");
		HomeScreenSteps.logInToTheApp(new User("goldstatus@mobiata.com", "password", "expedia"));
		HomeScreenSteps.switchToTab("Shop Travel");
	}

	private void getSearchResults() throws Throwable {
		LaunchScreen.hotelsLaunchButton().perform(click());
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		SearchScreen.waitForSearchEditText().perform(typeText("SFO"));
		SearchScreenActions.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		SearchScreenActions.chooseDatesWithDialog(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		SearchScreen.searchButton().perform(click());
	}
}
