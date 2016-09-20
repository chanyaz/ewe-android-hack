package com.expedia.bookings.test.happy;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.lx.LXInfositeScreen;
import com.expedia.bookings.test.phone.lx.LXScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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

	public void testLxPhoneHappyPathLoggedInCustomer() throws Throwable {
		goToLxSearchResults();
		LXScreen.goToSearchResults(getLxIdlingResource());

		selectActivity();
		selectOffers();
		doLogin();
		Common.delay(2);
		selectStoredCard();
		purchaseActivity(true);
		verifyBooking();
	}

	public void testLxPhoneHappyPathLoggedInCustomerCanSelectNewTraveler() throws Throwable {
		goToLxSearchResults();
		LXScreen.goToSearchResults(getLxIdlingResource());

		selectActivity();
		selectOffers();
		doLogin();
		Common.delay(2);

		CheckoutViewModel.clickTravelerInfo();
		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");

		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Add New Traveler");

		CheckoutViewModel.firstName().check(matches(withText("")));
		CheckoutViewModel.lastName().check(matches(withText("")));
		CheckoutViewModel.phone().check(matches(withText("")));
	}

	public void testLxPhoneHappyPathViaDefaultSearch() throws Throwable {
		goToLxSearchResults();
		LXScreen.goToSearchResults(getLxIdlingResource());
		selectActivity();
		validateRestHappyFlow();
	}

	public void testLxPhoneHappyPathViaExplicitSearch() throws Throwable {
		goToLxSearchResults();
		LXScreen.location().perform(ViewActions.waitForViewToDisplay(), typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		selectActivity();
		validateRestHappyFlow();
	}

	public void testLxPhoneHappyWithRecommendedActivity() throws Throwable {
		bucketAndSelectRecommendations();
		Common.pressBack();
		Common.delay(1);
		validateRestHappyFlow();
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppLXRecommendedActivitiesTest,
			AbacusUtils.DefaultVariate.CONTROL.ordinal());
	}


	public void testLxPhoneHappyWithRecommendationsFromTheBackFlow() throws Throwable {
		bucketAndSelectRecommendations();
		final String ticketName = "2-Day";
		LXInfositeScreen.selectCalendarOnRecommendations().perform(scrollTo());
		LXInfositeScreen.selectOfferOnRecommendations("2-Day New York Pass").perform(scrollTo(), click());
		Common.delay(1);
		LXInfositeScreen.ticketAddButtonOnRecommendations(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButtonOnRecommendations(ticketName).perform(scrollTo(), click());
		Common.delay(1);

		manuallyEnterTravelerInfo();
		purchaseActivity(false);
		verifyBooking();
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppLXRecommendedActivitiesTest,
			AbacusUtils.DefaultVariate.CONTROL.ordinal());
	}

	private void goToLxSearchResults() throws Throwable {
		NewLaunchScreen.activitiesLaunchButton().perform(click());
	}

	private void doLogin() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.login_widget);
		CheckoutViewModel.enterLoginDetails();
		CheckoutViewModel.pressDoLogin();
		Common.delay(1);
	}

	private void selectStoredCard() throws Throwable {
		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.selectStoredCard("Saved AmexTesting");
		CheckoutViewModel.clickDone();
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
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo());
		LXInfositeScreen.bookNowButton(ticketName).perform(click());
		Common.delay(1);
	}

	private void selectRecommendation() throws Throwable {
		final String activityName = "Alcatraz Package: Hop-On Hop-Off Cruise & City Tour by Big Bus";

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

		LXInfositeScreen.selectRecommendation(activityName).perform(
			scrollTo(), click());
	}

	private void manuallyEnterTravelerInfo() throws Throwable {
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
	}

	private void purchaseActivity(boolean isAmex) throws Throwable {
		CheckoutViewModel.performSlideToPurchase();
		CVVEntryScreen.enterCVV(isAmex ? "6286" : "111");
		CVVEntryScreen.clickBookButton();
	}

	private void verifyBooking() {
		LXScreen.itinNumberOnConfirmationScreen().check(matches(withText(containsString("7672544862"))));
	}

	private void bucketAndSelectRecommendations() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppLXRecommendedActivitiesTest,
			AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		goToLxSearchResults();
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());
		selectRecommendation();
	}
}
