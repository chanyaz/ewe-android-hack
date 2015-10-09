package com.expedia.bookings.test.phone.lx;

import java.math.BigDecimal;
import java.util.List;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.utils.Strings;
import com.squareup.otto.Subscribe;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.isEmpty;
import static org.hamcrest.Matchers.containsString;

public class LxSearchResultsTest extends LxTestCase {

	List<LXActivity> mActivities;
	SearchResultsHandler searchResultsHandler = new SearchResultsHandler();

	public void testSearchResultPageTestCases() throws Throwable {
		Events.register(searchResultsHandler);

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(Matchers
				.allOf(withId(R.id.error_action_button), withText(
					R.string.edit_search))).perform(click());

			String expectedLocationDisplayName = "San Francisco, CA";
			LXScreen.location().perform(typeText("San"));
			LXScreen.selectLocation(expectedLocationDisplayName);
			LXScreen.selectDateButton().perform(click());
			LXScreen.selectDates(LocalDate.now(), null);
			LXScreen.searchButton().perform(click());
		}
		LXScreen.waitForSearchListDisplayed();
		//by this time we must have all the activities loaded.
		//assert on the total number of items to show.
		LXScreen.resultList().check(matches(LXScreen.withResults(mActivities.size())));
		//loop through each and every tile of the result and validate that we have correct data on every tile
		int currentCounter = 1;
		for (LXActivity activity : mActivities) {
			String expectedTitle = Strings.escapeQuotes(activity.title);
			LXScreen.resultList().perform(RecyclerViewActions.scrollToPosition(currentCounter));
			if (Strings.isEmpty(activity.duration)) {
				LXScreen.getTile(expectedTitle).check(matches(
					hasDescendant((isEmpty()))));
			}
			else {
				LXScreen.getTile(expectedTitle).check(matches(
					hasDescendant(withText(containsString(activity.duration)))));
			}
			LXScreen.getTile(expectedTitle).check(matches(
				hasDescendant(withText(containsString(activity.price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP))))));

			// Check strikethrough price
			if (!activity.originalPrice.getAmount().equals(BigDecimal.ZERO)) {
				LXScreen.getTile(expectedTitle).check(matches(
						hasDescendant(withText(containsString(activity.fromOriginalPriceValue.toString().toLowerCase())))));
			}
			LXScreen.getTile(expectedTitle).check(matches(
				hasDescendant(withText(containsString(activity.fromPriceTicketCode.toString().toLowerCase())))));
			currentCounter++;
		}
		//To-Do : Since Sort and Filter functionality are under discussion, will implement the test cases when we have a clarity around them.
		Events.unregister(searchResultsHandler);
	}

	private class SearchResultsHandler {
		@Subscribe
		public void on(Events.LXSearchResultsAvailable event) {
			mActivities = event.lxSearchResponse.activities;
		}
	}
}
