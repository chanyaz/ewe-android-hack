package com.expedia.bookings.test.ui.phone.tests.lx;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.component.lx.pagemodels.LXInfositePageModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.LxTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LXCreditCardTests extends LxTestCase {

	public void testPaymentCleared() throws Throwable {

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(Matchers.allOf(withId(R.id.error_action_button), withText(R.string.edit_search))).perform(click());
			LXViewModel.location().perform(typeText("San"));
			LXViewModel.selectLocation(getInstrumentation(), "San Francisco, CA");
			LXViewModel.selectDateButton().perform(click());
			LXViewModel.selectDates(LocalDate.now(), null);
			LXViewModel.searchButton().perform(click());
		}

		final String ticketName = "2-Day";

		LXViewModel.waitForSearchListDisplayed();
		LXViewModel.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		LXViewModel.waitForLoadingDetailsNotDisplayed();

		LXInfositePageModel.selectOffer("2-Day New York Pass").perform(scrollTo(), click());
		LXInfositePageModel.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositePageModel.bookNowButton(ticketName).perform(scrollTo());
		LXInfositePageModel.bookNowButton(ticketName).perform(click());
		ScreenActions.delay(1);
		screenshot("LX Checkout Started");
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		screenshot("LX Checkout Ready");
		Common.pressBack();
		ScreenActions.delay(1);
		LXInfositePageModel.bookNowButton(ticketName).perform(click());
		ScreenActions.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		ScreenActions.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
	}
}
