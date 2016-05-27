package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.lx.LXInfositeScreen;
import com.expedia.bookings.test.phone.lx.LXScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class GTPhoneHappyPathTest extends PhoneTestCase {

	private LxIdlingResource mLxIdlingResource;

	@Override
	public void runTest() throws Throwable {
		if (Common.isPhone()) {
			mLxIdlingResource = new LxIdlingResource();
			mLxIdlingResource.register();
		}
		super.runTest();
	}

	@Override
	public void tearDown() throws Exception {
		if (Common.isPhone()) {
			mLxIdlingResource.unregister();
			mLxIdlingResource = null;
		}
		super.tearDown();
	}

	public void goToGTSearchResults() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchGroundTransport();
	}

	public void testGTPhoneHappyPath() throws Throwable {
		goToGTSearchResults();
		Common.delay(2);
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDateButton().perform(click());
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		validateRestHappyFlow();
	}

	private void validateRestHappyFlow() throws Throwable {
		final String ticketName = "Roundtrip to San Francisco";

		LXScreen.sortAndFilterButton().check(matches(not(isDisplayed())));
		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

		LXInfositeScreen.selectOffer("Roundtrip to San Francisco").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Traveler").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo());
		LXInfositeScreen.bookNowButton(ticketName).perform(click());
		Common.delay(1);
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.enterCVV("111");
		CVVEntryScreen.clickBookButton();

		LXScreen.itinNumberOnConfirmationScreen().check(matches(withText(containsString("7672544863"))));
	}

}
