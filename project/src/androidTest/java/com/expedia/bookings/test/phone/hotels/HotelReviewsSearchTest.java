package com.expedia.bookings.test.phone.hotels;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import android.view.KeyEvent;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay;
import static org.hamcrest.CoreMatchers.not;

public class HotelReviewsSearchTest extends HotelTestCase {

	@Test
	public void testHappySearch() throws Throwable {
		openSearchView();
		onView(withHint("Search reviews")).perform(typeText("PrivateBank"));
		onView(withHint("Search reviews")).perform(pressKey(KeyEvent.KEYCODE_ENTER));
		waitForViewNotYetInLayoutToDisplay(withText("Great hotel in great location"), 10, TimeUnit.SECONDS);
		onView(withHint("Search reviews")).check(matches(not(hasFocus())));
		pressBack();
		onView(withId(R.id.hotel_review_search_results)).check(matches(not(isDisplayed())));
	}

	@Test
	public void testBackStack() throws Throwable {
		openSearchView();
		pressBack();
		onView(withHint("Search reviews")).check(matches(not(hasFocus())));
		onView(withId(R.id.hotel_review_search_results)).check(matches(isDisplayed()));
		pressBack();
		onView(withId(R.id.hotel_review_search_results)).check(matches(not(isDisplayed())));
	}

	private void openSearchView() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.HotelUGCSearch);
		SearchScreenActions.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");
		HotelInfoSiteScreen.waitForDetailsLoaded();
		onView(withId(R.id.number_of_reviews)).perform(click());
		onView(withContentDescription("Search Button")).perform(click());
		onView(withId(R.id.hotel_review_search_results)).check(matches(isDisplayed()));
	}
}
