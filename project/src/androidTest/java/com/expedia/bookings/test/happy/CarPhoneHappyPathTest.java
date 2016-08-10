package com.expedia.bookings.test.happy;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.cars.CarScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withNavigationContentDescription;

public class CarPhoneHappyPathTest extends PhoneTestCase {

	private final static String CATEGORY = "Standard";
	private final static int CREDIT_CARD_NOT_REQUIRED = 0;
	private final static int CREDIT_CARD_REQUIRED = 1;

	private void goToCarDetails() throws Throwable {
		LaunchScreen.launchCars();

		SearchScreen.doGenericCarSearch();
		Common.delay(1);

		CarScreen.selectCarCategory(CATEGORY);
		Common.delay(1);
	}

	private void doLogin() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.login_widget);
		CheckoutViewModel.enterLoginDetails();
		CheckoutViewModel.pressDoLogin();
		Common.delay(1);
	}

	private void enterPaymentInfoWithScreenshot() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterPaymentInfo();
	}

	private void slideToPurchase() throws Throwable {
		CheckoutViewModel.performSlideToPurchase();
		Common.delay(1);
	}

	private void enterCVV(String cvv) throws Throwable {
		CVVEntryScreen.enterCVV(cvv);
		CVVEntryScreen.clickBookButton();
	}

	public void testCarPhoneHappyPath() throws Throwable {
		goToCarDetails();
		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		EspressoUtils.assertViewIsNotDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterTravelerInfo();
		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Back")));
		slideToPurchase();
		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
	}

	public void testCarPhoneCCRequiredHappyPath() throws Throwable {
		goToCarDetails();
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		CheckoutViewModel.enterTravelerInfo();

		enterPaymentInfoWithScreenshot();

		slideToPurchase();
		enterCVV("111");
	}

	public void testCarPhoneLoggedInHappyPath() throws Throwable {
		goToCarDetails();
		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		doLogin();

		slideToPurchase();
		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
	}

	public void testCarPhoneLoggedInCCRequiredHappyPath() throws Throwable {
		goToCarDetails();
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		doLogin();

		selectSavedCreditCard();
		slideToPurchase();
		enterCVV("6286");
	}

	private void selectSavedCreditCard() throws Throwable {
		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.selectStoredCard("Saved AmexTesting");
		CheckoutViewModel.clickDone();
	}


	public void testCarPhoneLoggedInStoredTravelerCC() throws Throwable {
		goToCarDetails();
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		doLogin();

		CheckoutViewModel.clickTravelerInfo();
		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Close")));
		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");
		CheckoutViewModel.pressClose();

		selectSavedCreditCard();
		slideToPurchase();
		enterCVV("6286");
	}

	public void testCarPhoneSignedInCustomerCanEnterNewTraveler() throws Throwable {
		goToCarDetails();
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		doLogin();

		CheckoutViewModel.clickTravelerInfo();
		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Close")));
		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");

		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler("Add New Traveler");

		CheckoutViewModel.firstName().check(matches(withText("")));
		CheckoutViewModel.lastName().check(matches(withText("")));
		CheckoutViewModel.phone().check(matches(withText("")));
	}
}
