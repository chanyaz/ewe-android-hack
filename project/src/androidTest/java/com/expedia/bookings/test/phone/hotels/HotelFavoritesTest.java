package com.expedia.bookings.test.phone.hotels;

import org.joda.time.DateTime;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelFavoritesTest extends PhoneTestCase {

	@Test
	public void testFavoritesButtonShown() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.HotelTravelPulseLists);
		getSearchResults();
		onView(withId(R.id.menu_favorites)).perform(click());
		assertViewIsDisplayed(R.id.hotel_favorites_toolbar);
		onView(withContentDescription("Close")).perform(click());
		assertViewIsDisplayed(R.id.menu_favorites);
	}

	@Test
	public void testFavoritesButtonNotShownNoAbTest() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.HotelTravelPulseLists, AbacusVariant.CONTROL.getValue());
		getSearchResults();
		onView(withId(R.id.menu_favorites)).check(doesNotExist());
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
