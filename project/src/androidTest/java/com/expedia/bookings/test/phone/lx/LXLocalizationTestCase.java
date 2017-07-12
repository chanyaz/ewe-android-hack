package com.expedia.bookings.test.phone.lx;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
/*
	A test that would run through all the screens of the LX flow in the Portrait mode"
*/
public class LXLocalizationTestCase extends PhoneTestCase {

	public LXLocalizationTestCase() {
		super(LXBaseActivity.class);
	}

	@Test
	public void testScreenShotThroughPortraitMode() throws Throwable {
		screenshot("BEFORE_SEARCH");
		LXScreen.location().perform(ViewActions.waitForViewToDisplay(), typeText("San"));
		screenshot("AFTER_SEARCH_SUGGESTIONS_POPULATE");
		LXScreen.selectLocation("San Francisco, CA");
		screenshot("BEFORE_DATE_SELECTION");
		LXScreen.selectDates(LocalDate.now(), null);
		screenshot("AFTER_DATE_SELECTION");
		LXScreen.searchButton().perform(click());
		screenshot("ACTIVITY_RESULT_SCREEN");
		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		screenshot("INFO_SITE_PAGE");
	}
}
