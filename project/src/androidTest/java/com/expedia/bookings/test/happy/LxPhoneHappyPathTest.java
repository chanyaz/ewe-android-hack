package com.expedia.bookings.test.happy;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.lx.LXInfositeScreen;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.test.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.clickWhenEnabled;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.containsString;

public class LxPhoneHappyPathTest extends PhoneTestCase {

	private LxIdlingResource mLxIdlingResource;

	@Override
	public void runTest() throws Throwable {
		mLxIdlingResource = new LxIdlingResource();
		mLxIdlingResource.register();
		super.runTest();
	}

	@Override
	public void tearDown() throws Exception {
		mLxIdlingResource.unregister();
		mLxIdlingResource = null;
		super.tearDown();
	}

	@Test
	public void testLxPhoneHappyPathLoggedInCustomer() throws Throwable {
		goToLxSearchResults();
		LXScreen.goToSearchResults(mLxIdlingResource);

		selectActivity();
		selectOffers();
		doLogin();
		selectStoredCard();
		purchaseActivity(true);
		verifyBooking();
	}

	@Test
	public void testLxPhoneHappyPathLoggedInCustomerCanSelectNewTraveler() throws Throwable {
		goToLxSearchResults();
		LXScreen.goToSearchResults(mLxIdlingResource);

		selectActivity();
		selectOffers();
		doLogin();

		CheckoutScreen.clickTravelerInfo();
		CheckoutScreen.clickStoredTravelerButton();
		CheckoutScreen.selectStoredTraveler("Expedia Automation First");

		CheckoutScreen.clickStoredTravelerButton();
		CheckoutScreen.selectStoredTraveler("Add New Traveler");

		CheckoutScreen.firstName().check(matches(withText("")));
		CheckoutScreen.lastName().check(matches(withText("")));
		CheckoutScreen.phone().check(matches(withText("")));
	}

	@Test
	public void testLxPhoneHappyPathViaDefaultSearch() throws Throwable {
		goToLxSearchResults();
		LXScreen.goToSearchResults(mLxIdlingResource);
		selectActivity();
		validateRestHappyFlow();
	}

	@Test
	public void testLxPhoneHappyPathViaExplicitSearch() throws Throwable {
		goToLxSearchResults();
		LXScreen.location().perform(waitForViewToDisplay(), typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		selectActivity();
		validateRestHappyFlow();
	}

	private void goToLxSearchResults() throws Throwable {
		waitForLaunchScreenToDisplay();
		LaunchScreen.activitiesLaunchButton().perform(click());
	}

	private void waitForLaunchScreenToDisplay() {
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.launch_toolbar), 10, TimeUnit.SECONDS);
	}

	private void doLogin() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.login_widget);
		CheckoutScreen.enterLoginDetails();
		CheckoutScreen.pressDoLogin();
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS);
	}

	private void selectStoredCard() throws Throwable {
		CheckoutScreen.clickPaymentInfo();
		CheckoutScreen.selectStoredCard("Saved AmexTesting");
		CheckoutScreen.clickDone();
	}

	private void validateRestHappyFlow() throws Throwable {
		selectOffers();
		manuallyEnterTravelerInfo();
		purchaseActivity(false);
		verifyBooking();
	}

	private void selectActivity() throws Throwable {
		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
	}

	private void selectOffers() throws Throwable {
		final String ticketName = "2-Day";
		LXInfositeScreen.selectOffer("2-Day New York Pass").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(clickWhenEnabled());
		LXInfositeScreen.bookNowButton(ticketName).perform(clickWhenEnabled());
		Espresso.onView(withId(R.id.login_widget)).perform(waitForViewToDisplay());
	}

	private void manuallyEnterTravelerInfo() throws Throwable {
		CheckoutScreen.enterTravelerInfo();
		CheckoutScreen.enterPaymentInfo();
	}

	private void purchaseActivity(boolean isAmex) throws Throwable {
		CheckoutScreen.performSlideToPurchase();
		CVVEntryScreen.enterCVV(isAmex ? "6286" : "111");
		CVVEntryScreen.clickBookButton();
	}

	private void verifyBooking() {
		LXScreen.itinNumberOnConfirmationScreen().check(matches(withText(containsString("7672544862"))));
	}
}
