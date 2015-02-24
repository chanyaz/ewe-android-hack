package com.expedia.bookings.test.component.lx;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.widget.LXSearchParamsWidget;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LXSearchParamsTest {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.widget_lx_search_params);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testViewVisibilities() {
		LXViewModel.doneButton().check(matches(isDisplayed()));
		LXViewModel.closeButton().check(matches(isDisplayed()));
		LXViewModel.header().check(matches(isDisplayed()));
		LXViewModel.location().check(matches(isDisplayed()));
		LXViewModel.selectDateButton().check(matches(isDisplayed()));
		LXViewModel.calendar().check(matches(not(isDisplayed())));
		LXViewModel.selectDateButton().perform(click());
		LXViewModel.calendar().check(matches(isDisplayed()));
	}

	@Test
	public void testInputParams() throws Throwable {
		String typedLocationText = "San";
		String expectedLocationDisplayName = "San Francisco, CA";
		String expectedLocationFullName = "San Francisco (and vicinity), California, United States of America";

		LocalDate start = LocalDate.now();
		LocalDate end = LocalDate.now().plusDays(14);
		LXSearchParams expected = new LXSearchParams();
		expected.startDate = start;
		expected.endDate = end;
		expected.location = expectedLocationFullName;

		LXSearchParamsWidget searchParamsWidget = (LXSearchParamsWidget) playground.getRoot();

		LXViewModel.location().perform(typeText(typedLocationText));
		LXViewModel.selectLocation(playground.instrumentation(), expectedLocationDisplayName);
		LXViewModel.selectDateButton().check(matches(withText(R.string.select_lx_search_dates)));
		LXViewModel.selectDateButton().perform(click());

		// Select only start date
		LXViewModel.selectDates(start, null);
		String expectedDateText = JodaUtils.format(LocalDate.now(), "MMM dd");
		LXViewModel.selectDateButton().check(matches(withText(expectedDateText)));
		LXViewModel.doneButton().perform(click());
		LXSearchParams actual = searchParamsWidget.getCurrentParams();
		assertEquals(expected.startDate, actual.startDate);
		assertEquals(null, actual.endDate);

		// Select start date and end date
		LXViewModel.selectDates(start, end);
		LXViewModel.doneButton().perform(click());
		actual = searchParamsWidget.getCurrentParams();
		assertEquals(expected.startDate, actual.startDate);
		assertEquals(expected.endDate, actual.endDate);
		assertEquals(expected.location, actual.location);
	}

	@Test
	public void testRequiredParamsFilled() {
		// Nothing entered
		LXViewModel.doneButton().perform(click());
		LXViewModel.alertDialogMessage().check(matches(withText(R.string.lx_error_missing_location)));
		LXViewModel.alertDialogNeutralButton().check(matches(isDisplayed()));
		LXViewModel.alertDialogNeutralButton().perform(click());

		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(LocalDate.now(), LocalDate.now().plusDays(1));

		LXViewModel.doneButton().perform(click());
		LXViewModel.alertDialogMessage().check(matches(withText(R.string.lx_error_missing_location)));
	}
}
