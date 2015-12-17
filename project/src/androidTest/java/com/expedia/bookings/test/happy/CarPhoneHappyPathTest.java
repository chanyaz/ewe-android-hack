package com.expedia.bookings.test.happy;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.cars.CarScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarPhoneHappyPathTest extends PhoneTestCase {

	private final static String CATEGORY = "Standard";
	private final static int CREDIT_CARD_NOT_REQUIRED = 0;
	private final static int CREDIT_CARD_REQUIRED = 1;

	private void goToCarDetails() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();

		screenshot("Car_Search");
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarScreen.pickupLocation().perform(typeText("SFO"));
		CarScreen.selectPickupLocation("San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		screenshot("Car_Search_Params_Entered");
		CarScreen.searchButton().perform(click());
		ScreenActions.delay(1);

		screenshot("Car_Search_Results");
		CarScreen.selectCarCategory(CATEGORY);
		ScreenActions.delay(1);
	}

	private void doLogin() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.login_widget);
		CheckoutViewModel.enterLoginDetails();
		ScreenActions.delay(1);
		screenshot("Car_LoginScreen");
		CheckoutViewModel.pressDoLogin();
		ScreenActions.delay(1);
		screenshot("Car_Login_Success");
	}


	private void enterPaymentInfoWithScreenshot() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterPaymentInfo();
		screenshot("Car_Checkout_Payment_Entered");
		CheckoutViewModel.pressClose();
	}

	private void slideToPurchase() throws Throwable {
		screenshot("Car_Checkout_Ready_To_Purchase");
		CheckoutViewModel.performSlideToPurchase();
		ScreenActions.delay(1);

		screenshot("Car_Confirmation");
	}

	private void enterCVV(String cvv) throws Throwable {
		CVVEntryScreen.parseAndEnterCVV(cvv);
		CVVEntryScreen.clickBookButton();
		screenshot("Car_CVV");
	}

	public void testCarPhoneHappyPath() throws Throwable {
		goToCarDetails();
		screenshot("Car Offers");
		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		screenshot("Car Checkout");
		EspressoUtils.assertViewIsNotDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterTravelerInfo();

		slideToPurchase();
		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
	}

	public void testCarPhoneCCRequiredHappyPath() throws Throwable {
		goToCarDetails();
		screenshot("Car Offers");
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		screenshot("Car Checkout");
		CheckoutViewModel.enterTravelerInfo();
		screenshot("Car_Checkout_Driver_Entered");

		enterPaymentInfoWithScreenshot();

		slideToPurchase();
		enterCVV("111");
	}

	public void testCarPhoneLoggedInHappyPath() throws Throwable {
		goToCarDetails();
		screenshot("Car Offers");
		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		screenshot("Car Checkout");
		doLogin();

		slideToPurchase();
		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
	}

	public void testCarPhoneLoggedInCCRequiredHappyPath() throws Throwable {
		goToCarDetails();
		screenshot("Car Offers");
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		screenshot("Car Checkout");
		doLogin();

		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.clickStoredCardButton();
		CheckoutViewModel.selectStoredCard(getInstrumentation(), "AmexTesting");
		slideToPurchase();
		enterCVV("6286");
	}


	public void testCarPhoneLoggedInStoredTravelerCC() throws Throwable {
		goToCarDetails();
		screenshot("Car Offers");
		CarScreen.selectCarOffer(CREDIT_CARD_REQUIRED);
		screenshot("Car Checkout");
		doLogin();

		CheckoutViewModel.clickDriverInfo();
		CheckoutViewModel.clickStoredTravelerButton();
		CheckoutViewModel.selectStoredTraveler(getInstrumentation(), "Expedia Automation First");
		CheckoutViewModel.pressClose();
		screenshot("Car_Checkout_Driver_Entered");

		CheckoutViewModel.clickPaymentInfo();
		CheckoutViewModel.clickStoredCardButton();
		CheckoutViewModel.selectStoredCard(getInstrumentation(), "AmexTesting");
		slideToPurchase();
		enterCVV("6286");
	}

}
