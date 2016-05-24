package com.expedia.bookings.test.phone.lx;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.LxTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class LXCreateTripErrorTest extends LxTestCase {

	private void goToCheckout(int productPosition, String ticketName) throws Throwable {
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDateButton().perform(click());
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(productPosition, click()));

		LXInfositeScreen.selectOffer(ticketName).perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo(), click());
	}

	public void testCreateTripError() throws Throwable {
		goToCheckout(1, "2-Day New York Pass");

		onView(withId(R.id.lx_checkout_error_widget)).check(matches(isCompletelyDisplayed()));
	}

	public void testCreateTripPriceChange() throws Throwable {
		goToCheckout(2, "2-Day New York Pass");

		onView(withId(R.id.price_change_text)).check(matches(allOf(isCompletelyDisplayed(), withText("Price changed from $130"))));
	}
}
