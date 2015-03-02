package com.expedia.bookings.test.component.lx;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import android.app.Instrumentation;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class LXViewModel {
	public static ViewInteraction calendar() {
		return onView(withId(R.id.search_calendar));
	}

	public static ViewInteraction location() {
		return onView(withId(R.id.search_location));
	}

	public static void selectLocation(Instrumentation instrumentation, String location) throws Throwable {
		ScreenActions.delay(1);
		onView(withText(location))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(instrumentation).getWindow().getDecorView()))))
			.perform(click());
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_dates));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction alertDialogMessage() {
		return onView(withId(android.R.id.message));
	}

	public static ViewInteraction alertDialogNeutralButton() {
		return onView(withId(android.R.id.button3));
	}

	public static ViewInteraction progress() {
		return onView(withId(R.id.loading_results));
	}

	public static ViewInteraction searchResultsWidget() {
		return onView(withId(R.id.lx_search_results_widget));
	}

	public static ViewInteraction searchFailed() {
		return onView(withId(R.id.lx_search_failure));
	}

	public static ViewInteraction searchList() {
		return onView(recyclerView(R.id.lx_search_results_list));
	}

	public static Matcher<View> recyclerView(int viewId) {
		return allOf(isAssignableFrom(RecyclerView.class), withId(viewId));
	}

	public static ViewInteraction recyclerItemView(Matcher<View> identifyingMatcher, int recyclerViewId) {
		Matcher<View> itemView = allOf(withParent(recyclerView(recyclerViewId)),
			withChild(identifyingMatcher));
		return Espresso.onView(itemView);
	}

	public static ViewInteraction progressDetails() {
		return onView(withId(R.id.loading_details));
	}

	public static ViewInteraction detailsWidget() {
		return onView(withId(R.id.activity_details));
	}

	public static Matcher<View> recyclerGalleryMatcher() {
		return withId(R.id.activity_gallery);
	}

	public static ViewInteraction infoContainer() {
		return onView(withId(R.id.activity_info_container));
	}

	public static ViewInteraction withOfferText(String offerText) {
		return onView(withChild(withChild(withText(startsWith(offerText)))));
	}

	public static ViewInteraction selectTicketsButton(String offerText) {
		return onView(allOf(withId(R.id.select_tickets), hasSibling(withChild(withText(startsWith(offerText))))));
	}

	public static ViewInteraction ticketPicker(String offerText) {
		return onView(allOf(withId(R.id.offer_tickets_picker),
			hasSibling(withChild(withChild(withText(startsWith(offerText)))))));
	}

	public static ViewInteraction toolbar() {
		return onView(withId(R.id.toolbar));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.menu_search));
	}

	public static ViewInteraction detailsDate(String dateText) {
		return onView(allOf(withParent(withId(R.id.offer_dates_container)), withText(endsWith(dateText))));
	}

}
