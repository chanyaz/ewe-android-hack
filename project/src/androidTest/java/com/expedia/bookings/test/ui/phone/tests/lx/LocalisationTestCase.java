package com.expedia.bookings.test.ui.phone.tests.lx;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;
/*
	A test that would run through all the screens of the LX flow in the Portrait mode"
*/
public class LocalisationTestCase extends PhoneTestCase {


	public LocalisationTestCase() {
		super(LXBaseActivity.class);
	}

	public void testScreenShotThroughPortraitMode() throws Throwable {
		screenshot("BEFORE_SEARCH");
		LXViewModel.location().perform(typeText("San"));
		screenshot("AFTER_SEARCH_SUGGESTIONS_POPULATE");
		LXViewModel.selectLocation(getInstrumentation(), "San Francisco, CA");
		screenshot("BEFORE_DATE_SELECTION");
		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(LocalDate.now(), null);
		screenshot("AFTER_DATE_SELECTION");
		LXViewModel.searchButton().perform(click());
		screenshot("LOADING_SCREEN");
		onView(withId(R.id.loading_results)).perform(waitFor(10L, TimeUnit.SECONDS));
		screenshot("ACTIVITY_RESULT_SCREEN");
		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		screenshot("INFO_SITE_PAGE");
	}
}
