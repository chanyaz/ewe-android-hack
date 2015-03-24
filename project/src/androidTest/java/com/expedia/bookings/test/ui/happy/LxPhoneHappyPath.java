package com.expedia.bookings.test.ui.happy;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.component.lx.LXViewModel.getBookNowButtonFromTicketName;
import static com.expedia.bookings.test.component.lx.LXViewModel.getTicketAddButtonViewFromTicketName;
import static com.expedia.bookings.test.component.lx.LXViewModel.itinNumberOnConfirmationScreen;
import static com.expedia.bookings.test.ui.espresso.ViewActions.clickOnFirstEnabled;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;

public class LxPhoneHappyPath extends PhoneTestCase {

	private static final String TAG = LxPhoneHappyPath.class.getSimpleName();

	public LxPhoneHappyPath() {
		super(LXBaseActivity.class);
	}

	/*
		A basic happy path
	 */
	public void testLxPhoneHappyPath() throws Throwable {
		String expectedLocationDisplayName = "San Francisco, CA";
		String ticketName = "2-Day";
		screenshot("LX_search");
		LXViewModel.location().perform(typeText("San"));
		LXViewModel.selectLocation(getInstrumentation(), expectedLocationDisplayName);
		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(LocalDate.now(), null);
		screenshot("LX_Search_Params_Entered");
		LXViewModel.searchButton().perform(click());
		onView(withId(R.id.loading_results)).perform(waitFor(10L, TimeUnit.SECONDS));
		onView(withId(R.id.lx_search_results_list)).perform(
			RecyclerViewActions
				.actionOnItemAtPosition(0, click()));
		onView(withId(R.id.loading_details)).perform(waitFor(10L, TimeUnit.SECONDS));
		LXViewModel.detailsDateContainer().perform(scrollTo(),clickOnFirstEnabled());
		LXViewModel.selectTicketsButton("2-Day New York Pass").perform(scrollTo(),click());
		getTicketAddButtonViewFromTicketName(ticketName, "Adult").perform(click());
		getBookNowButtonFromTicketName(ticketName).perform(scrollTo(),click());
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();
		itinNumberOnConfirmationScreen().check(matches(withText("7672544862")));
	}

}
