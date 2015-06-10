package com.expedia.bookings.test.ui.phone.tests.cars;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.CarTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.google.gson.Gson;
import com.mobiata.android.util.IoUtils;

import static android.support.test.espresso.action.ViewActions.clearText;

public class CarCreditCardTests extends CarTestCase {

	public void testPaymentInfo() throws Throwable {
		goToCheckout();
		screenshot("Car Checkout");
		verifiyInvalidCardMessaging();
		screenshot("Car Payment Info");
		verifiyCreditCardCleared();
	}

	private void verifiyInvalidCardMessaging() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		ScreenActions.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		ScreenActions.delay(1);
		CardInfoScreen.typeTextCreditCardEditText("6711111111111111");
		EspressoUtils.assertViewIsDisplayed(R.id.invalid_payment_container);
		CardInfoScreen.creditCardNumberEditText().perform(clearText());
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("666");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		EspressoUtils.assertViewIsNotDisplayed(R.id.invalid_payment_container);
		screenshot("Car Checkout Payment Entered");
		CheckoutViewModel.clickDone();
	}

	private void verifiyCreditCardCleared() throws Throwable {
		Common.pressBack();
		ScreenActions.delay(1);
		goToCheckout();
		ScreenActions.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		ScreenActions.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
	}

	private void goToCheckout() throws Throwable {
		String createFileName = "SearchCarOffer_CCRequired.json";
		SearchCarOffer searchCarOffer;
		Gson gson = CarServices.generateGson();
		String offerStr = IoUtils.convertStreamToString(
			getInstrumentation().getContext().getAssets().open(createFileName));
		searchCarOffer = gson.fromJson(offerStr, SearchCarOffer.class);
		Events.post(new Events.CarsShowCheckout(searchCarOffer));
	}

}
