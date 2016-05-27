package com.expedia.bookings.test.phone.lx;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
public class LXResultsPresenterTest {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.lx_result_presenter, R.style.V2_Theme_LX);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testSearchResultsList() {
		buildSearchParams();

		LXScreen.waitForSearchResultsWidgetDisplayed();
		LXScreen.searchResultsWidget().check(matches(isDisplayed()));
		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().check(matches(isDisplayed()));
		Matcher<View> identifyingMatcher = allOf
			(hasDescendant(withText(startsWith("happy"))),
				hasDescendant(withText("$130")));
		ViewInteraction searchResultItem = LXScreen.listItemView(identifyingMatcher, R.id.lx_search_results_list);

		searchResultItem.check(matches(isDisplayed()));
		searchResultItem.check(matches(hasDescendant(withId(R.id.activity_title))));
		searchResultItem.check(matches(hasDescendant(withId(R.id.activity_image))));
		searchResultItem.check(matches(hasDescendant(withId(R.id.activity_price))));
		searchResultItem.check(matches(hasDescendant(withId(R.id.activity_duration))));
	}

	@Test
	public void testToolbar() {
		String location = "New York";
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(14);
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.startDate = startDate;
		searchParams.endDate = endDate;
		searchParams.location = location;
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));

		LXScreen.waitForSearchResultsWidgetDisplayed();

		String expectedToolbarDateRange = startDate.toString("MMM d") + " - " + endDate.toString("MMM d");
		ViewInteraction searchToolbar = LXScreen.toolbar();
		searchToolbar.check(matches(isDisplayed()));
		searchToolbar.check(matches(hasDescendant(withText(expectedToolbarDateRange))));
		searchToolbar.check(matches(hasDescendant(withText(location))));
	}

	@Test
	public void testResultListAdapter() throws Throwable {
		String title = "test";
		Money price = new Money("10", "USD");
		Money originalPrice = new Money("11", "USD");
		String category = "tour";
		String duration = "2d";
		LXTicketType code = LXTicketType.Adult;
		List<String> categoriesList = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			categoriesList.add(category);
		}

		final List<LXActivity> activities = new ArrayList<>();
		LXActivity a = new LXActivity();
		a.title = title;
		a.price = price;
		a.originalPrice = originalPrice;
		a.categories = categoriesList;
		a.fromPriceTicketCode = code;
		a.duration = duration;
		activities.add(a);

		LXScreen.waitForSearchResultsWidgetDisplayed();
		onView(withId(R.id.lx_search_results_list)).perform(LXScreen.setLXActivities(activities));
		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0,
			LXScreen.performViewHolderComparison(title, price.getFormattedMoney(),
				originalPrice.getFormattedMoney(), duration)));

	}

	@Test
	public void testNoSearchResults() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "search_failure";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(14);
		searchParams.searchType = SearchType.EXPLICIT_SEARCH;
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
		Common.delay(2);
		LXScreen.searchFailed().check(matches(isDisplayed()));

		LXScreen.searchFailed().check(matches(hasDescendant(withText(R.string.error_car_search_message))));
	}

	public void buildSearchParams() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(14);
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
	}
}
