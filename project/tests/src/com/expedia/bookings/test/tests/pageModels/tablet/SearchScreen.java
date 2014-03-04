package com.expedia.bookings.test.tests.pageModels.tablet;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.contrib.CountingIdlingResource;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

import static com.expedia.bookings.test.utils.ViewActions.clickDates;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SearchScreen {

	public SearchScreen() {
	}

	// Object access

	public static ViewInteraction startSearchButton() {
		return onView(withId(R.id.search_status_text_view));
	}

	public static ViewInteraction cancelButton() {
		return onView(withId(R.id.cancel_button));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.search_button));
	}

	public static ViewInteraction destinationEditText() {
		return onView(withId(R.id.destination_edit_text));
	}

	public static ViewInteraction searchDatesTextView() {
		return onView(withId(R.id.search_dates_text_view));
	}

	public static ViewInteraction guestsTextView() {
		return onView(withId(R.id.guests_text_view));
	}

	public static ViewInteraction calendarPicker() {
		return onView(withId(R.id.calendar_picker));
	}

	// Object interaction

	public static void clickSuggestion(String text) {
		onData(allOf(is(instanceOf(String.class)), equalTo(text))).usingAdapterViewProtocol(SuggestionAdapterViewProtocol.getInstance()).perform(click());
	}

	public static void clickToStartSearch() {
		startSearchButton().perform(click());
	}

	public static void clickCancelButton() {
		cancelButton().perform(click());
	}

	public static void clickSearchButton() {
		searchButton().perform(click());
	}

	public static void clickDestinationEditText() {
		destinationEditText().perform(click());
	}

	public static void clearDestinationEditText() {
		destinationEditText().perform(clearText());
	}

	public static void typeInDestinationEditText(String text) {
		destinationEditText().perform(typeText(text));
	}

	public static void clickGuestsButton() {
		guestsTextView().perform(click());
	}

	public static void clickDate(final LocalDate start, final LocalDate end) {
		calendarPicker().perform(clickDates(start, end));
	}

	// Idling Resources

	private static final SuggestionResource sSuggestionResource = new SuggestionResource();

	public static void registerSuggestionResource() {
		Events.register(sSuggestionResource);
		Espresso.registerIdlingResources(sSuggestionResource.getIdlingResource());
	}

	public static void unregisterSuggestionResource() {
		Events.unregister(sSuggestionResource);
	}

	private static class SuggestionResource {
		private CountingIdlingResource mIdlingResource;

		public SuggestionResource() {
			mIdlingResource = new CountingIdlingResource("SuggestionResource");
		}

		public CountingIdlingResource getIdlingResource() {
			return mIdlingResource;
		}

		@Subscribe
		public void on(Events.SuggestionQueryStarted event) {
			mIdlingResource.increment();
		}

		@Subscribe
		public void on(Events.SuggestionResultsDelivered event) {
			mIdlingResource.decrement();
		}
	}

}
