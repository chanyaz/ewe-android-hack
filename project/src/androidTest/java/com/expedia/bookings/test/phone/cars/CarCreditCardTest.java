package com.expedia.bookings.test.phone.cars;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.mobiata.android.util.IoUtils;

import static android.support.test.espresso.action.ViewActions.clearText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class CarCreditCardTest extends CarTestCase {

	public void testPaymentInfo() throws Throwable {
		goToCheckout();
		screenshot("Car Checkout");
		verifyInvalidCardMessaging();
		screenshot("Car Payment Info");
		verifyCreditCardCleared();
	}

	private void verifyInvalidCardMessaging() throws Throwable {
		CheckoutViewModel.paymentInfo().perform(waitForViewToDisplay());
		Common.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
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

	private void verifyCreditCardCleared() throws Throwable {
		Common.pressBack();
		Common.delay(1);
		goToCheckout();
		Common.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
	}

	private void goToCheckout() throws Throwable {
		String createFileName = "SearchCarOffer_CCRequired.json";
		SearchCarOffer searchCarOffer;
		Gson gson = CarServices.generateGson();
		String offerStr = IoUtils.convertStreamToString(
			getInstrumentation().getContext().getAssets().open(createFileName));
		searchCarOffer = gson.fromJson(offerStr, SearchCarOffer.class);
		Events.post(new Events.CarsShowCheckout(searchCarOffer.productKey, searchCarOffer.fare.total,
			searchCarOffer.isInsuranceIncluded,
			new LatLng(searchCarOffer.pickUpLocation.latitude, searchCarOffer.pickUpLocation.longitude)));
	}

}
