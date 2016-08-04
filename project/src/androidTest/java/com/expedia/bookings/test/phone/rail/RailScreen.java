package com.expedia.bookings.test.phone.rail;

import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;

public class RailScreen {

	public static ViewInteraction calendarButton() {
		return onView(withId(R.id.calendar_card));
	}

	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static void selectOneWay() {
		onView(withText(R.string.rail_one_way)).perform(click());
	}

	public static void selectRoundTrip() {
		onView(withText(R.string.rail_return)).perform(click());
	}

	public static ViewInteraction dialogDoneButton() {
		return onView(withId(android.R.id.button1));
	}

	public static void scrollToFareOptions() {
		onView(withId(R.id.details_fare_options)).perform(scrollTo());
	}

	public static ViewInteraction selectFareOption(String fareOption) {
		return onView(
			allOf(
				withId(R.id.select_button), allOf(withText(R.string.select)),
				isDescendantOfA(allOf(withId(R.id.details_fare_options))),
				hasSibling(allOf(withId(R.id.price), withText(fareOption))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickSelectFareOption() {
		selectFareOption("£196.0").perform(scrollTo(), click());
	}

	public static void clickAmenitiesLink(String fareClass) {
		selectAmenitiesLink(fareClass).perform(scrollTo(), click());
	}

	public static ViewInteraction selectAmenitiesLink(String fareClass) {
		return onView(
			allOf(
				withId(R.id.amenities_link), allOf(withText(R.string.amenities)),
				isDescendantOfA(allOf(withId(R.id.details_fare_options))),
				hasSibling(allOf(withId(R.id.fare_description), withText(fareClass))),
				withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
		);
	}

	public static void clickFareRules(String fareType, String fareDesc) {
		onView(allOf(withText(fareType), hasSibling(withText(fareDesc)))).perform(scrollTo(), click());
	}

	public static ViewInteraction checkout() {
		return onView(withId(R.id.checkout_button));
	}

	public static ViewInteraction legInfo() {
		return onView(withId(R.id.rail_leg_container));
	}

	public static ViewInteraction detailsIcon() {
		return onView(allOf(isDescendantOfA(withId(R.id.rail_leg_container)), withId(R.id.rail_leg_details_icon)));
	}

	public static ViewInteraction fareDesciptionInfo() {
		return onView(withId(R.id.fare_description_container));
	}

	public static void navigateToDetails() throws Throwable {
		SearchScreen.selectRailOriginAndDestination();
		RailScreen.calendarButton().perform(click());
		LocalDate startDate = LocalDate.now().plusDays(10);
		RailScreen.selectDates(startDate, null);
		RailScreen.dialogDoneButton().perform(click());

		SearchScreen.searchButton().perform(click());

		onView(withText("3:55 PM – 7:22 PM")).perform(waitForViewToDisplay()).check(matches(isDisplayed()))
			.perform(click());
		onView(withText("You have 43m to get from London Euston to London Paddington")).check(matches(isDisplayed()));
	}

	public static void navigateToTripOverview() throws Throwable {
		navigateToDetails();

		RailScreen.scrollToFareOptions();
		onView(withText("Any off-peak train")).check(matches(isDisplayed()));
		RailScreen.clickSelectFareOption();

		onView(withText("Outbound - Mon Aug 29")).perform(ViewActions.waitForViewToDisplay())
			.check(matches(isDisplayed()));
	}
}
