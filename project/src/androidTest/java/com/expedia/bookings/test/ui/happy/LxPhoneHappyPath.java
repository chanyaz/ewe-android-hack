package com.expedia.bookings.test.ui.happy;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.component.lx.pagemodels.LXInfositePageModel;
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
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;
import static org.hamcrest.Matchers.containsString;

public class LxPhoneHappyPath extends PhoneTestCase {

	public LxPhoneHappyPath() {
		super(LXBaseActivity.class);
	}

	public void testLxPhoneHappyPathViaDefaultSearch() throws Throwable {

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(Matchers
				.allOf(withId(R.id.error_action_button), withText(
					R.string.edit_search))).perform(click());

			String expectedLocationDisplayName = "San Francisco, CA";
			LXViewModel.location().perform(typeText("San"));
			LXViewModel.selectLocation(getInstrumentation(), expectedLocationDisplayName);
			LXViewModel.selectDateButton().perform(click());
			LXViewModel.selectDates(LocalDate.now(), null);
			LXViewModel.searchButton().perform(click());
		}
		validateRestHappyFlow();
	}

	public void testLxPhoneHappyPathViaExplicitSearch() throws Throwable {
		LXViewModel.searchButtonInSRPToolbar().perform(click());
		screenshot("LX Search");
		LXViewModel.location().perform(typeText("San"));
		LXViewModel.selectLocation(getInstrumentation(), "San Francisco, CA");
		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(LocalDate.now(), null);
		screenshot("LX Search Params Entered");
		LXViewModel.searchButton().perform(click());
		validateRestHappyFlow();
	}

	private void validateRestHappyFlow() throws Throwable {

		final String ticketName = "2-Day";
		screenshot("LX Search Results");

		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		onView(withId(R.id.loading_details)).perform(waitFor(10L, TimeUnit.SECONDS));
		screenshot("LX Details");

		LXInfositePageModel.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositePageModel.bookNowButton(ticketName).perform(scrollTo());
		screenshot("LX Ticket Selection");
		LXInfositePageModel.bookNowButton(ticketName).perform(click());

		screenshot("LX Checkout Started");
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		screenshot("LX Checkout Ready");
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.parseAndEnterCVV("111");
		screenshot("LX CVV");
		CVVEntryScreen.clickBookButton();

		screenshot("LX Checkout Started");
		LXViewModel.itinNumberOnConfirmationScreen().check(matches(withText(containsString("7672544862"))));
	}

}
