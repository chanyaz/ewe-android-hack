package com.expedia.bookings.test.ui.happy;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.CarsActivity;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.EspressoTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarPhoneHappyPath extends EspressoTestCase {

	public CarPhoneHappyPath() {
		super(CarsActivity.class);
	}

	public void testCarPhoneHappyPath() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		screenshot("Car_Search");
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		screenshot("Car_Search_Params_Entered");

		CarViewModel.searchButton().perform(click());
		ScreenActions.delay(1);
		screenshot("Car_Search_Results");

		CarViewModel.selectCarCategory(0);
		ScreenActions.delay(1);
		screenshot("Car_Offers");

		CarViewModel.selectCarOffer(0);
		ScreenActions.delay(1);
		screenshot("Car_Checkout");

		EspressoUtils.assertViewIsNotDisplayed(R.id.payment_info);
		CarViewModel.clickDriverInfo();
		CarViewModel.enterFirstName("FiveStar");
		CarViewModel.enterLastName("Bear");
		CarViewModel.enterEmail("noah@mobiata.com");
		CarViewModel.enterPhoneNumber("4158675309");
		CarViewModel.pressDone();

		screenshot("Car_Checkout_Ready_To_Purchase");
		CarViewModel.performSlideToPurchase();
		ScreenActions.delay(1);

		screenshot("Car_Confirmation");
	}

}
