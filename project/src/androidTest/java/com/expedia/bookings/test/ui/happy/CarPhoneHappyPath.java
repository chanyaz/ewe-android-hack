package com.expedia.bookings.test.ui.happy;

import org.joda.time.DateTime;

import com.expedia.bookings.activity.CarsActivity;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;

public class CarPhoneHappyPath extends PhoneTestCase {

	public CarPhoneHappyPath() {
		super(CarsActivity.class);
	}

	public void testCarPhoneHappyPath() throws Throwable {
		final String pickupAirportCode = "SFO";
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		screenshot("Car_Search");
		CarViewModel.selectPickupLocation(getInstrumentation(), pickupAirportCode);
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		screenshot("Car_Search_Params_Entered");

		CarViewModel.searchButton().perform(click());
		screenshot("Car_Search_Clicked_Search");

		ScreenActions.delay(1);

		CarViewModel.selectCarCategory(0);
		screenshot("Car_Search_Selected_Category");

		ScreenActions.delay(1);

		CarViewModel.selectCarOffer(0);
		screenshot("Car_Search_Selected_Offer");
	}

}
