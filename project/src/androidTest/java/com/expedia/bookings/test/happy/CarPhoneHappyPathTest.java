package com.expedia.bookings.test.happy;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.cars.CarScreen;
import com.expedia.bookings.test.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import static android.support.test.espresso.action.ViewActions.click;

public class CarPhoneHappyPathTest extends PhoneTestCase {

	private final static String CATEGORY = "Standard";
	private final static int CREDIT_CARD_NOT_REQUIRED = 0;
	private final static int CREDIT_CARD_REQUIRED = 1;

	private void goToCarDetails() throws Throwable {
		AbacusTestUtils.updateABTest(PointOfSale.getPointOfSale().getCarsWebViewABTestID(), 0);
		ViewInteraction carsLaunchButton = NewLaunchScreen.carsLaunchButton();
		Common.delay(1);
		carsLaunchButton.perform(click());
		CarScreen.waitForSearchScreen();
		SearchScreen.doGenericCarSearch();
		Common.delay(1);

		CarScreen.selectCarCategory(CATEGORY);
		Common.delay(1);
	}

	private void doLogin() throws Throwable {
		CheckoutViewModel.waitForCheckout();
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

	// 29-Aug-2017 : Disabling car UI tests since car is now a webview
//	@Test
//	public void testCarPhoneHappyPath() throws Throwable {
//		goToCarDetails();
//		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
//		EspressoUtils.assertViewIsNotDisplayed(R.id.payment_info_card_view);
//		CheckoutViewModel.enterTravelerInfo();
//		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Back")));
//		slideToPurchase();
//		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
//	}
//
//	@Test
//	public void testCarPhoneCCRequiredHappyPath() throws Throwable {
//		goToCarDetails();
//		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
//		CheckoutViewModel.enterTravelerInfo();
//
//		enterPaymentInfoWithScreenshot();
//
//		slideToPurchase();
//		enterCVV("111");
//	}
//
//	@Test
//	public void testCarPhoneLoggedInHappyPath() throws Throwable {
//		goToCarDetails();
//		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
//		doLogin();
//
//		slideToPurchase();
//		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
//	}
//
//	@Test
//	public void testCarPhoneLoggedInCCRequiredHappyPath() throws Throwable {
//		goToCarDetails();
//		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
//		doLogin();
//
//		selectSavedCreditCard();
//		slideToPurchase();
//		enterCVV("6286");
//	}

	private void selectSavedCreditCard() throws Throwable {
		CheckoutViewModel.waitForPaymentInfoDisplayed();
		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.selectStoredCard("Saved AmexTesting");
		CheckoutViewModel.clickDone();
	}


//	@Test
//	public void testCarPhoneLoggedInStoredTravelerCC() throws Throwable {
//		goToCarDetails();
//		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
//		doLogin();
//
//		CheckoutViewModel.clickTravelerInfo();
//		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Back")));
//		CheckoutViewModel.clickStoredTravelerButton();
//		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");
//		CheckoutViewModel.pressClose();
//
//		selectSavedCreditCard();
//		slideToPurchase();
//		enterCVV("6286");
//	}
//
//	@Test
//	public void testCarPhoneSignedInCustomerCanEnterNewTraveler() throws Throwable {
//		goToCarDetails();
//		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
//		doLogin();
//
//		CheckoutViewModel.clickTravelerInfo();
//		onView(withId(R.id.checkout_toolbar)).check(matches(withNavigationContentDescription("Back")));
//		CheckoutViewModel.clickStoredTravelerButton();
//		CheckoutViewModel.selectStoredTraveler("Expedia Automation First");
//
//		CheckoutViewModel.clickStoredTravelerButton();
//		CheckoutViewModel.selectStoredTraveler("Add New Traveler");
//
//		CheckoutViewModel.firstName().check(matches(withText("")));
//		CheckoutViewModel.lastName().check(matches(withText("")));
//		CheckoutViewModel.phone().check(matches(withText("")));
//	}
}
