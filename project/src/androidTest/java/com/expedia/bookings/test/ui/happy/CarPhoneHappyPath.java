package com.expedia.bookings.test.ui.happy;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarPhoneHappyPath extends PhoneTestCase {

	private final static String CATEGORY = "Standard";
	private final static int CREDIT_CARD_NOT_REQUIRED = 0;
	private final static int CREDIT_CARD_REQUIRED = 1;

	private void goToCarDetails() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();

		screenshot("Car_Search");
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		screenshot("Car_Search_Params_Entered");
		CarViewModel.searchButton().perform(click());
		ScreenActions.delay(1);

		screenshot("Car_Search_Results");
		CarViewModel.selectCarCategory(CATEGORY);
		ScreenActions.delay(1);
	}

	private void selectCarOffer(int carOfferNum) throws Throwable {
		screenshot("Car_Offers");
		//Selecting an already expanded offer opens google maps
		if (carOfferNum != 0) {
			CarViewModel.expandCarOffer(carOfferNum);
		}
		CarViewModel.selectCarOffer();
		ScreenActions.delay(1);

		screenshot("Car_Checkout");
	}

	private void doLogin() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.login_widget);
		CarViewModel.clickCarLogin();
		CarViewModel.enterUsername("username");
		CarViewModel.enterPassword("password");
		ScreenActions.delay(1);
		screenshot("Car_LoginScreen");
		CarViewModel.pressDoLogin();
		ScreenActions.delay(1);
		screenshot("Car_Login_Success");
	}

	private void enterDriverInfo() {
		CarViewModel.clickDriverInfo();
		CarViewModel.enterFirstName("FiveStar");
		CarViewModel.enterLastName("Bear");
		Common.closeSoftKeyboard(CarViewModel.lastName());
		ScreenActions.delay(1);
		CarViewModel.enterEmail("noah@mobiata.com");
		Common.closeSoftKeyboard(CarViewModel.email());
		ScreenActions.delay(1);
		CarViewModel.enterPhoneNumber("4158675309");
		CarViewModel.pressClose();
	}

	private void enterPaymentInfo() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CarViewModel.clickPaymentInfo();

		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("666");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		screenshot("Car_Checkout_Payment_Entered");
		CarViewModel.pressClose();
	}

	private void slideToPurchase() throws Throwable {
		screenshot("Car_Checkout_Ready_To_Purchase");
		CarViewModel.performSlideToPurchase();
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
		selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		EspressoUtils.assertViewIsNotDisplayed(R.id.payment_info_card_view);
		enterDriverInfo();

		slideToPurchase();
		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
	}

	public void testCarPhoneCCRequiredHappyPath() throws Throwable {
		goToCarDetails();
		selectCarOffer(CREDIT_CARD_REQUIRED);
		enterDriverInfo();
		screenshot("Car_Checkout_Driver_Entered");

		enterPaymentInfo();

		slideToPurchase();
		enterCVV("111");
	}

	public void testCarPhoneLoggedInHappyPath() throws Throwable {
		goToCarDetails();
		selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		doLogin();

		slideToPurchase();
		EspressoUtils.assertViewIsNotDisplayed(R.id.cvv);
	}

	public void testCarPhoneLoggedInCCRequiredHappyPath() throws Throwable {
		goToCarDetails();
		selectCarOffer(CREDIT_CARD_REQUIRED);
		doLogin();

		CarViewModel.clickPaymentInfo();
		CarViewModel.clickStoredCardButton();
		CarViewModel.selectStoredCard(getInstrumentation(), "AmexTesting");
		slideToPurchase();
		enterCVV("6286");
	}


	public void testCarPhoneLoggedInStoredTravelerCC() throws Throwable {
		goToCarDetails();
		selectCarOffer(CREDIT_CARD_REQUIRED);
		doLogin();

		CarViewModel.clickDriverInfo();
		CarViewModel.clickStoredTravelerButton();
		CarViewModel.selectStoredTraveler(getInstrumentation(), "Expedia Automation First");
		CarViewModel.pressClose();
		screenshot("Car_Checkout_Driver_Entered");

		CarViewModel.clickPaymentInfo();
		CarViewModel.clickStoredCardButton();
		CarViewModel.selectStoredCard(getInstrumentation(), "AmexTesting");
		slideToPurchase();
		enterCVV("6286");
	}

}
