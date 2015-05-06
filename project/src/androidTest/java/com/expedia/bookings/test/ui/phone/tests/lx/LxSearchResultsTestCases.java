package com.expedia.bookings.test.ui.phone.tests.lx;

import java.util.List;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.component.lx.pagemodels.LXSearchResultsPageModel;
import com.expedia.bookings.test.ui.utils.LxTestCase;
import com.expedia.bookings.utils.Strings;
import com.squareup.otto.Subscribe;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.isEmpty;
import static org.hamcrest.Matchers.containsString;

public class LxSearchResultsTestCases extends LxTestCase {

	List<LXActivity> mActivities;
	SearchResultsHandler searchResultsHandler = new SearchResultsHandler();

	public void testSearchResultPageTestCases() throws Throwable {
		Events.register(searchResultsHandler);

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(Matchers
				.allOf(withId(R.id.error_action_button), withText(
					R.string.edit_search))).perform(click());

			String expectedLocationDisplayName = "San Francisco, CA";
			LXViewModel.location().perform(typeText("San"));
			LXViewModel.selectLocation(getInstrumentation(), expectedLocationDisplayName);
			LXViewModel.selectDateButton().perform(click());
			LXViewModel.selectDates(LocalDate.now(), null);
			LXViewModel.searchButton().perform(click());
		}
		LXViewModel.waitForSearchListDisplayed();
		//by this time we must have all the activities loaded.
		//assert on the total number of items to show.
		LXSearchResultsPageModel.resultList().check(matches(LXSearchResultsPageModel.withResults(mActivities.size())));
		//loop through each and every tile of the result and validate that we have correct data on every tile
		int currentCounter = 1;
		for (LXActivity activity : mActivities) {
			String expectedTitle = Strings.escapeQuotes(activity.title);
			LXSearchResultsPageModel.resultList().perform(RecyclerViewActions.scrollToPosition(currentCounter));
			if (Strings.isEmpty(activity.duration)) {
				LXSearchResultsPageModel.getTile(expectedTitle).check(matches(
					hasDescendant((isEmpty()))));
			}
			else {
				LXSearchResultsPageModel.getTile(expectedTitle).check(matches(
					hasDescendant(withText(containsString(activity.duration)))));
			}
			LXSearchResultsPageModel.getTile(expectedTitle).check(matches(
				hasDescendant(withText(containsString(activity.price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP))))));
			LXSearchResultsPageModel.getTile(expectedTitle).check(matches(
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
