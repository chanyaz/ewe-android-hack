package com.expedia.bookings.test.phone.lx;

import org.junit.Test;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.LxTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.clickWhenEnabled;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToCompletelyDisplay;

public class LXCreateTripErrorTest extends LxTestCase {

	private void goToCheckout(int productPosition, String ticketName) throws Throwable {
		LXScreen.goToSearchResults(getLxIdlingResource());
		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(productPosition, click()));

		LXInfositeScreen.selectOffer(ticketName).perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(clickWhenEnabled());
	}

	@Test
	public void testCreateTripError() throws Throwable {
		goToCheckout(1, "2-Day New York Pass");

		onView(withId(R.id.lx_checkout_error_widget)).perform(waitForViewToCompletelyDisplay());
	}

	@Test
	public void testCreateTripPriceChange() throws Throwable {
		goToCheckout(2, "2-Day New York Pass");

		onView(withId(R.id.price_change_text)).perform(waitForViewToCompletelyDisplay()).check(matches(withText("Price changed from $130")));
	}
}
