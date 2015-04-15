package com.expedia.bookings.test.component.lx.pagemodels;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.component.lx.models.TileDataModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LXSearchResultsPageModel {
	static final int ACTIVITY_TITLE = R.id.activity_title;
	static final int ACTIVITY_CATEGORY = R.id.activity_category;
	static final int ACTIVITY_PRICE = R.id.activity_price;
	static final int ACTIVITY_PER_TICKET_TYPE = R.id.activity_from_price_ticket_type;

	public static ViewInteraction resultList() {
		return onView(withId(R.id.lx_search_results_list));
	}

	public static TileDataModel getTileDataAtIndex(int index) {
		TileDataModel tileDataModel = new TileDataModel();
		resultList().perform(
			RecyclerViewActions.actionOnItemAtPosition(index,getDataFromTile(tileDataModel)));
		return tileDataModel;
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

	private static String[] getCategories(TextView categoryTextView) {
		return categoryTextView.getText().toString().split(",");
	}

	private static ViewAction getDataFromTile(final TileDataModel data) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(TextView.class));
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void perform(UiController uiController, View view) {
				TextView activityTitle = (TextView) view.findViewById(ACTIVITY_TITLE);
				data.activityName = activityTitle.getText().toString();
				TextView activityCategory = (TextView) view.findViewById(ACTIVITY_CATEGORY);
				for (String category : getCategories(activityCategory)) {
					data.categories.add(category);
				}
				TextView activityPrice = (TextView) view.findViewById(ACTIVITY_PRICE);
				data.activityPrice = activityPrice.getText().toString();
				TextView activityPriceTicketType = (TextView) view.findViewById(ACTIVITY_PER_TICKET_TYPE);
				// from the per adult just take the adult as on the infosite page we just have adult text
				data.activityTravellerType = activityPriceTicketType.getText().toString().split(" ")[1];
			}
		};
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
