package com.expedia.bookings.test.phone.lx;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.presenter.lx.LXSearchParamsPresenter;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.JodaUtils;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LXSearchParamsTest {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.test_lx_search_presenter, R.style.V2_Theme_LX);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testViewVisibilities() throws Throwable {
		LXScreen.toolbar().check(matches(isDisplayed()));
		LXScreen.toolbar().check(matches(hasDescendant(isAssignableFrom(Button.class))));
		LXScreen.searchButton().check(matches(isDisplayed()));
		LXScreen.toolbar().check(matches(hasDescendant(withText(R.string.lx_search_title))));
		LXScreen.location().check(matches(isDisplayed()));
		LXScreen.selectDateButton().check(matches(isDisplayed()));
		LXScreen.calendar().check(matches(not(isDisplayed())));
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDateButton().perform(click());
		LXScreen.calendar().check(matches(isDisplayed()));
	}

	@Test
	public void testInputParams() throws Throwable {
		String typedLocationText = "San";
		String expectedLocationDisplayName = "San Francisco, CA";
		String expectedLocationFullName = "San Francisco (and vicinity), California, United States of America";

		LocalDate start = LocalDate.now();
		LocalDate end = start.plusDays(playground.getActivity().getResources().getInteger(R.integer.lx_default_search_range));
		LXSearchParams expected = new LXSearchParams();
		expected.startDate = start;
		expected.location = expectedLocationFullName;

		LXSearchParamsPresenter searchParamsWidget = (LXSearchParamsPresenter) playground.getRoot();

		LXScreen.location().perform(typeText(typedLocationText));
		LXScreen.selectLocation(expectedLocationDisplayName);
		LXScreen.selectDateButton().check(matches(withText(R.string.select_lx_search_dates)));
		LXScreen.selectDateButton().perform(click());

		// Select start date
		LXScreen.selectDates(start, null);
		String expectedDateText = JodaUtils.format(LocalDate.now(), "MMMM d");
		LXScreen.selectDateButton().check(matches(withText(expectedDateText)));
		LXScreen.searchButton().perform(click());
		LXSearchParams actual = searchParamsWidget.getCurrentParams();
		assertEquals(expected.startDate, actual.startDate);
		assertEquals(end, actual.endDate);

	}

	@Test
	public void testRequiredParamsFilled() throws Throwable {
		// Nothing entered
		LXScreen.searchButton().perform(click());
		LXScreen.didNotGoToResults();

		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.didNotGoToResults();

		LXScreen.location().perform(clearText());
		LXScreen.didNotGoToResults();
	}
}
