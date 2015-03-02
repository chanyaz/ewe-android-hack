package com.expedia.bookings.test.component.lx;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;
import com.expedia.bookings.widget.LXResultsListAdapter;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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
		searchParams.location = "happy";
		searchParams.startDate = LocalDate.now();
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
		LXViewModel.progress().check(matches(isDisplayed()));
		ScreenActions.delay(2);

		LXViewModel.searchResultsWidget().check(matches(isDisplayed()));
		LXViewModel.searchList().check(matches(isDisplayed()));
		ViewInteraction searchResultItem = LXViewModel.recyclerItemView(
			withChild(withText(startsWith("New York Pass"))),
			R.id.lx_search_results_list);

		searchResultItem.check(matches(isDisplayed()));
		searchResultItem.check(matches(hasDescendant(withId(R.id.activity_title))));
		searchResultItem.check(matches(hasDescendant(withId(R.id.activity_image))));
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
		ScreenActions.delay(2);

		String expectedToolbarDateRange = startDate.toString("MMM dd") + " - " + endDate.toString("MMM dd");
		ViewInteraction searchToolbar = LXViewModel.toolbar();
		searchToolbar.check(matches(isDisplayed()));
		searchToolbar.check(matches(hasDescendant(withText(expectedToolbarDateRange))));
		searchToolbar.check(matches(hasDescendant(withText(location))));
	}

	@Test
	public void testResultListAdapter() throws Throwable {
		final RecyclerView rv = (RecyclerView) playground.getRoot().findViewById(R.id.lx_search_results_list);

		String title = "test";
		final List<LXActivity> activities = new ArrayList<>();
		LXActivity a = new LXActivity();
		a.title = title;
		activities.add(a);

		final LXResultsListAdapter adapter = (LXResultsListAdapter)rv.getAdapter();
		final RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(rv, 0);
		SpoonScreenshotUtils.getCurrentActivity(playground.instrumentation()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.setActivities(activities);
				rv.getAdapter().onBindViewHolder(viewHolder, 0);
			}
		});
		ScreenActions.delay(2);
		Assert.assertEquals(activities.size(), rv.getAdapter().getItemCount());
		TextView tv = (TextView) viewHolder.itemView.findViewById(R.id.activity_title);
		Assert.assertEquals(title, tv.getText());
	}

	@Test
	public void testNoSearchResults() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = "search_failure";
		searchParams.startDate = LocalDate.now();
		Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
		ScreenActions.delay(2);
		LXViewModel.searchFailed().check(matches(isDisplayed()));
		LXViewModel.searchFailed().check(matches(hasDescendant(withText(R.string.error_car_search_message))));
	}

}
