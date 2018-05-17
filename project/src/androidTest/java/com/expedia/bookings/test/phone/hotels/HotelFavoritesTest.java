package com.expedia.bookings.test.phone.hotels;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions;
import com.expedia.bookings.test.stepdefs.phone.HomeScreenSteps;
import com.expedia.bookings.test.support.User;
import org.hamcrest.Matchers;

import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.view.autofill.AutofillManager;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsNotDisplayed;

public class HotelFavoritesTest extends PhoneTestCase {

	@Test
	public void testFavoritesButtonShown() {
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
	public void testFavoritesButtonNotShownNoAbTest() {
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
		disableAutoFill();
		LaunchScreen.waitForLOBHeaderToBeDisplayed();
		LaunchScreen.accountButton().perform(click());
		HomeScreenSteps.logInToTheApp(new User("goldstatus@mobiata.com", "password", "expedia"));
		LaunchScreen.shopButton().perform(click());
	}

	private void getSearchResults() {
		LaunchScreen.waitForLOBHeaderToBeDisplayed();
		LaunchScreen.shopButton().perform(click());
		LaunchScreen.hotelsLaunchButton().perform(click());
		SearchScreenActions.doGenericHotelSearch();
	}

	private void disableAutoFill() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Context context = InstrumentationRegistry.getTargetContext();
			AutofillManager autofillManager = context.getSystemService(AutofillManager.class);
			if (autofillManager != null) {
				autofillManager.cancel();
				autofillManager.disableAutofillServices();
			}
		}
	}
}
