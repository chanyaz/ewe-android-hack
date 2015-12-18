package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.lx.LXScreen;
import com.expedia.bookings.test.phone.lx.LXInfositeScreen;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class LxPhoneHappyPathTest extends PhoneTestCase {

	private LxIdlingResource mLxIdlingResource;

	public LxIdlingResource getLxIdlingResource() {
		return mLxIdlingResource;
	}

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

	public void goToLxSearchResults() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchActivities();
		screenshot("LX_Search_Results");
	}

	public void testLxPhoneHappyPathViaDefaultSearch() throws Throwable {
		goToLxSearchResults();

		if (getLxIdlingResource().isInSearchEditMode()) {
			onView(allOf(withId(R.id.error_action_button), withText(R.string.edit_search),
				isDescendantOfA(withId(R.id.lx_search_error_widget))))
				.perform(click());
			LXScreen.location().perform(typeText("San"));
			LXScreen.selectLocation("San Francisco, CA");
			LXScreen.selectDateButton().perform(click());
			LXScreen.selectDates(LocalDate.now(), null);
			LXScreen.searchButton().perform(click());
		}
		validateRestHappyFlow();
	}

	public void testLxPhoneHappyPathViaExplicitSearch() throws Throwable {
		goToLxSearchResults();
		LXScreen.searchButtonInSRPToolbar().perform(click());
		screenshot("LX Search");
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDateButton().perform(click());
		LXScreen.selectDates(LocalDate.now(), null);
		screenshot("LX Search Params Entered");
		LXScreen.searchButton().perform(click());
		validateRestHappyFlow();
	}

	private void validateRestHappyFlow() throws Throwable {
		final String ticketName = "2-Day";
		screenshot("LX Search Results");

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		LXScreen.waitForLoadingDetailsNotDisplayed();
		screenshot("LX Details");

		LXInfositeScreen.selectOffer("2-Day New York Pass").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo());
		screenshot("LX Ticket Selection");
		LXInfositeScreen.bookNowButton(ticketName).perform(click());
		Common.delay(1);
		screenshot("LX Checkout Started");
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		screenshot("LX Checkout Ready");
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.enterCVV("111");
		screenshot("LX CVV");
		CVVEntryScreen.clickBookButton();

		screenshot("LX Checkout Started");
		LXScreen.itinNumberOnConfirmationScreen().check(matches(withText(containsString("7672544862"))));
	}

}
