package com.expedia.bookings.test.phone.lx;

import org.junit.Test;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.action.ViewActions.click;
import static com.expedia.bookings.test.espresso.ViewActions.clickWhenEnabled;

public class LXCreditCardTest extends LxTestCase {

	@Test
	public void testPaymentCleared() throws Throwable {
		LXScreen.goToSearchResults(getLxIdlingResource());
		final String ticketName = "2-Day";

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

		LXInfositeScreen.selectOffer("2-Day New York Pass").perform(clickWhenEnabled());
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(clickWhenEnabled());
		LXInfositeScreen.bookNowButton(ticketName).perform(clickWhenEnabled());
		Common.delay(1);
		screenshot("LX Checkout Started");
		CheckoutViewModel.enterPaymentInfo();
		screenshot("LX Checkout Ready");
		Common.pressBack();
		Common.delay(1);
		LXInfositeScreen.bookNowButton(ticketName).perform(clickWhenEnabled());
		Common.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
	}
}
