package com.expedia.bookings.test.ui.happy;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;

public class LxPhoneHappyPath extends PhoneTestCase {
	public LxPhoneHappyPath() {
		super(LXBaseActivity.class);
	}

	public void testLxPhoneHappyPath() throws Throwable {
		final LocalDate startDateTime = LocalDate.now();
		String expectedLocationDisplayName = "San Francisco, CA";
		screenshot("LX_search");
		LXViewModel.location().perform(typeText("San"));

		LXViewModel.selectLocation(getInstrumentation(), expectedLocationDisplayName);

		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(startDateTime, null);
		screenshot("LX_Search_Params_Entered");
		LXViewModel.searchButton().perform(click());
		onView(withId(R.id.loading_results)).perform(waitFor(10L, TimeUnit.SECONDS, ProgressBar.class));
		screenshot("On_Search_Screen");
		assertTrue("Atleast one result must appear", (EspressoUtils.getListCount(LXViewModel.searchList()) >= 1));
	}

}
