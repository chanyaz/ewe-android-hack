package com.expedia.bookings.test.happy;

import java.util.concurrent.TimeUnit;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.lx.LXInfositeScreen;
import com.expedia.bookings.test.phone.lx.LXScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class GTPhoneHappyPathTest extends PhoneTestCase {

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

	public void goToGT() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchGroundTransport();
	}

	public void testGTPhoneHappyPath() throws Throwable {
		goToGT();
		LXScreen.goToSearchResults(getLxIdlingResource());
		validateRestHappyFlow();
	}

	public void testGTPhoneHappyPathLoggedInCustomer() throws Throwable {
		goToGT();
		LXScreen.goToSearchResults(getLxIdlingResource());

		selectOffer();
		doLogin();

		purchaseActivity(false);
		verifyBooking();
	}

	private void doLogin() throws Throwable {
		onView(withId(R.id.login_widget)).perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
		CheckoutViewModel.enterSingleCardLoginDetails();
		CheckoutViewModel.pressDoLogin();
	}

	private void purchaseActivity(boolean isAmex) throws Throwable {
		CheckoutViewModel.waitForPaymentInfoDisplayed();
		CheckoutViewModel.performSlideToPurchase();
		CVVEntryScreen.enterCVV(isAmex ? "6286" : "111");
		CVVEntryScreen.clickBookButton();
	}

	private void verifyBooking() {
		LXScreen.itinNumberOnConfirmationScreen().check(matches(withText(containsString("7672544863"))));
	}


	private void validateRestHappyFlow() throws Throwable {
		selectOffer();
		screenshot("GT Checkout Started");
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		screenshot("GT Checkout Ready");
		CheckoutViewModel.performSlideToPurchase();

		CVVEntryScreen.enterCVV("111");
		screenshot("GT CVV");
		CVVEntryScreen.clickBookButton();

		screenshot("GT Checkout Started");
		verifyBooking();
	}

	private void selectOffer() throws Throwable {
		final String ticketName = "Roundtrip to San Francisco";

		LXScreen.sortAndFilterButton().check(matches(not(isDisplayed())));
		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

		LXInfositeScreen.selectOffer("Roundtrip to San Francisco").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Traveler").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo());
		LXInfositeScreen.bookNowButton(ticketName).perform(click());
	}

}
