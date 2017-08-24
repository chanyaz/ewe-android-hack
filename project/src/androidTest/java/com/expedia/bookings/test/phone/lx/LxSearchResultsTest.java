package com.expedia.bookings.test.phone.lx;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.utils.Strings;
import com.squareup.otto.Subscribe;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.isEmpty;
import static org.hamcrest.Matchers.containsString;

public class LxSearchResultsTest extends LxTestCase {

	List<LXActivity> mActivities;
	final SearchResultsHandler searchResultsHandler = new SearchResultsHandler();

	@Test
	public void testSearchResultPageTestCases() throws Throwable {
		searchListDisplayed(true);
		//by this time we must have all the activities loaded.
		//assert on the total number of items to show.
		LXScreen.resultList().check(matches(LXScreen.withResults(mActivities.size())));
		//loop through each and every tile of the result and validate that we have correct data on every tile
		int currentCounter = 1;
		for (LXActivity activity : mActivities) {
			String expectedTitle = Strings.escapeQuotes(activity.title);
			LXScreen.resultList().perform(RecyclerViewActions.scrollToPosition(currentCounter));
			int lxSearchResultsList = R.id.lx_search_results_list;
			if (Strings.isEmpty(activity.duration)) {
				LXScreen.getTile(expectedTitle, lxSearchResultsList).check(matches(
					hasDescendant((isEmpty()))));
			}
			else {
				LXScreen.getTile(expectedTitle, lxSearchResultsList).check(matches(
					hasDescendant(withText(containsString(activity.duration)))));
			}
			LXScreen.getTile(expectedTitle, lxSearchResultsList).check(matches(
				hasDescendant(withText(containsString(activity.price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP))))));

			// Check strikethrough price
			if (!activity.originalPrice.getAmount().equals(BigDecimal.ZERO)) {
				LXScreen.getTile(expectedTitle, lxSearchResultsList).check(matches(
						hasDescendant(withText(containsString(activity.fromOriginalPriceValue.toString().toLowerCase(
							Locale.getDefault()))))));
			}
			LXScreen.getTile(expectedTitle, lxSearchResultsList).check(matches(
				hasDescendant(withText(containsString(activity.fromPriceTicketCode.toString().toLowerCase(Locale.getDefault()))))));
			currentCounter++;
		}
		//To-Do : Since Sort and Filter functionality are under discussion, will implement the test cases when we have a clarity around them.
	}

	@Test
	public void testSearchResultsFromOverlayOnDetails() throws Throwable {
		searchListDisplayed(true);
		selectActivityAndWaitForDetailsDisplayed();
		LXScreen.searchButtonOnDetailsToolbar().perform(click());
		searchListDisplayed(false);
	}

	@Test
	public void testSearchResultsFromOverlayOnDetailsWithRecommendations()  throws Throwable {
		searchListDisplayed(true);
		selectActivityAndWaitForDetailsDisplayed();
		LXScreen.searchButtonOnDetailsToolbar().perform(click());
		searchListDisplayed(false);
	}

	private void selectActivityAndWaitForDetailsDisplayed() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
	}

	private void searchListDisplayed(boolean firstLaunch) throws Throwable {
		Events.register(searchResultsHandler);
		String expectedLocationDisplayName = "San Francisco, CA";
		if (!firstLaunch) {
			LXScreen.locationCardView().perform(click());
		}
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation(expectedLocationDisplayName);
		if (!firstLaunch) {
			LXScreen.selectDateButton().perform(click());
		}
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		LXScreen.waitForSearchListDisplayed();
		Events.unregister(searchResultsHandler);
	}

	private class SearchResultsHandler {
		@Subscribe
		public void on(Events.LXSearchResultsAvailable event) {
			mActivities = event.lxSearchResponse.activities;
		}
	}
}
