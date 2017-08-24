package com.expedia.bookings.test.phone.lx;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.lob.lx.ui.viewmodel.LXSearchViewModel;
import com.expedia.bookings.presenter.lx.LXSearchPresenter;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.JodaUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LXSearchParamsTest {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.test_lx_search_presenter, R.style.V2_Theme_LX);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	LXSearchPresenter searchParamsWidget;
	final LocalDate start = LocalDate.now();

	@Before
	public void before() {
		searchParamsWidget = (LXSearchPresenter) playground.getRoot();
		searchParamsWidget.setSearchViewModel(new LXSearchViewModel(searchParamsWidget.getContext()));
	}

	@Test
	public void testRequiredParamsFilled() throws Throwable {
		// Nothing entered
		SearchScreen.searchButton().perform(click());
		LXScreen.didNotGoToResults();

		//Calendar opens when we enter the location
		SearchScreen.destination().perform(click());
		SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA");
		SearchScreen.calendar().check(matches(isDisplayed()));

		//Checking the date label after entering the activity start date
		SearchScreen.selectDates(start, null);
		String expectedDateText = JodaUtils.format(LocalDate.now(), "MMMM d");
		SearchScreen.selectDateButton().check(matches(withText(expectedDateText)));
	}
}
