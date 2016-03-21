package com.expedia.bookings.test.phone.lx;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;

public class LXCreditCardTest extends LxTestCase {

	public void testPaymentCleared() throws Throwable {
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDateButton().perform(click());
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());

		final String ticketName = "2-Day";

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		LXScreen.waitForLoadingDetailsNotDisplayed();

		LXInfositeScreen.selectOffer("2-Day New York Pass").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo());
		LXInfositeScreen.bookNowButton(ticketName).perform(click());
		Common.delay(1);
		screenshot("LX Checkout Started");
		CheckoutViewModel.enterPaymentInfo();
		screenshot("LX Checkout Ready");
		Common.pressBack();
		Common.delay(1);
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo());
		LXInfositeScreen.bookNowButton(ticketName).perform(click());
		Common.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
	}
}
