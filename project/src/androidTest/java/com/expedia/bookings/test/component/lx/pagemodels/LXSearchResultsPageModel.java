package com.expedia.bookings.test.component.lx.pagemodels;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.lx.LXViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LXSearchResultsPageModel {

	public static ViewInteraction resultList() {
		return onView(withId(R.id.lx_search_results_list));
	}

	public static void clickOnTileAtIndex(int index) {
		resultList().perform(
			RecyclerViewActions
				.actionOnItemAtPosition(index, click()));
	}

	public static ViewInteraction getTile(String activityTitle) {
		return LXViewModel.recyclerItemView(
			withChild(withChild(withText(activityTitle))),
			R.id.lx_search_results_list);
	}

	public static Matcher<View> withResults(final int expectedResultsCount) {
		return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
			@Override
			public boolean matchesSafely(RecyclerView view) {
				return view.getAdapter().getItemCount() == expectedResultsCount;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The total number of results must match");
			}
		};
	}
}
