package com.expedia.bookings.test.component.lx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
public class LXResultsPresenterTests {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.lx_result_presenter);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testSearchResultsList() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(14);
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));

		onView(isRoot()).perform(waitFor(allOf
			(hasDescendant(withText(startsWith("New York Pass"))),
				hasDescendant(withText("$130"))), 10L, TimeUnit.SECONDS));


		LXViewModel.searchResultsWidget().check(matches(isDisplayed()));
		LXViewModel.searchList().check(matches(isDisplayed()));
		ViewInteraction searchResultItem = LXViewModel.recyclerItemView(allOf
				(hasDescendant(withText(startsWith("New York Pass"))),
					hasDescendant(withText("$130"))),
			R.id.lx_search_results_list);

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

		onView(isRoot()).perform(waitFor(allOf
			(hasDescendant(withText(startsWith("New York Pass"))),
				hasDescendant(withText("$130"))), 10L, TimeUnit.SECONDS));

		String expectedToolbarDateRange = startDate.toString("MMM dd") + " - " + endDate.toString("MMM dd");
		ViewInteraction searchToolbar = LXViewModel.toolbar();
		searchToolbar.check(matches(isDisplayed()));
		searchToolbar.check(matches(hasDescendant(withText(expectedToolbarDateRange))));
		searchToolbar.check(matches(hasDescendant(withText(location))));
	}

	@Test
	public void testResultListAdapter() throws Throwable {
		String title = "test";
		Money price = new Money("10", "USD");
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
		a.categories = categoriesList;
		a.fromPriceTicketCode = code;
		a.duration = duration;
		activities.add(a);

		onView(withId(R.id.lx_search_results_list)).perform(LXViewModel.setLXActivities(activities));
		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0,
			LXViewModel.performViewHolderComparison(title, price.getFormattedMoney(), duration, categoriesList)));

	}

	@Test
	public void testNoSearchResults() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "search_failure";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(14);
		searchParams.searchType = SearchType.EXPLICIT_SEARCH;
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
		ScreenActions.delay(2);
		LXViewModel.searchFailed().check(matches(isDisplayed()));
		LXViewModel.searchFailed().check(matches(hasDescendant(withText(R.string.error_car_search_message))));
	}

	@Test
	public void testSortAndFilterWidgetOpenAndClose() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "New York";
		searchParams.startDate = LocalDate.now();
		searchParams.endDate = LocalDate.now().plusDays(14);
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));

		onView(isRoot()).perform(waitFor(allOf
			(hasDescendant(withText(startsWith("New York Pass"))),
				hasDescendant(withText("$130"))), 10L, TimeUnit.SECONDS));

		LXViewModel.searchResultsWidget().check(matches(isDisplayed()));
		LXViewModel.searchList().check(matches(isDisplayed()));
		LXViewModel.sortAndFilterButton().check(matches(isDisplayed()));
		LXViewModel.sortAndFilterButton().perform(click());
		LXViewModel.sortAndFilterWidget().check(matches(isDisplayed()));
		LXViewModel.closeFilter().check(matches(isDisplayed()));
		LXViewModel.closeFilter().perform(click());
		LXViewModel.sortAndFilterWidget().check(matches(not(isDisplayed())));
	}
}
