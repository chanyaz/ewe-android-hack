package com.expedia.bookings.test.ui.phone.tests.cars;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.CarTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.google.gson.Gson;
import com.mobiata.android.util.IoUtils;

public class CarCreditCardTests extends CarTestCase {

	public void testPaymentFailed() throws Throwable {
		goToCheckout();
		screenshot("Car Checkout");
		enterPaymentInfoWithScreenshot();
		screenshot("Car Payment Info");
		Common.pressBack();
		ScreenActions.delay(1);
		goToCheckout();
		ScreenActions.delay(1);
		CheckoutViewModel.clickPaymentInfo();
		ScreenActions.delay(1);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "");
	}

	private void goToCheckout() throws Throwable {
		String createFileName = "CarCreateTripResponse_CCRequired.json";
		CarCreateTripResponse carCreateTripResponse;
		Gson gson = CarServices.generateGson();
		String createStr = IoUtils.convertStreamToString(
			getInstrumentation().getContext().getAssets().open(createFileName));
		carCreateTripResponse = gson.fromJson(createStr, CarCreateTripResponse.class);
		Events.post(new Events.CarsShowCheckout(carCreateTripResponse));
	}

	private void enterPaymentInfoWithScreenshot() throws Throwable {
		EspressoUtils.assertViewIsDisplayed(R.id.payment_info_card_view);
		CheckoutViewModel.enterPaymentInfo();
		screenshot("Car Checkout Payment Entered");
		CheckoutViewModel.clickDone();
	}

}
