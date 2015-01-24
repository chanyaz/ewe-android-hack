package com.expedia.bookings.test.ui.happy;

import org.joda.time.DateTime;

import com.expedia.bookings.activity.CarsActivity;
import com.expedia.bookings.test.component.cars.CarSearchParamsModel;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;

public class CarPhoneHappyPath extends PhoneTestCase {

	public CarPhoneHappyPath() {
		super(CarsActivity.class);
	}

	public void testCarPhoneHappyPath() throws Throwable {
		final String pickupAirportCode = "SFO";
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		screenshot("Car_Search");
		CarSearchParamsModel.pickupLocation().perform(typeText(pickupAirportCode));
		CarSearchParamsModel.selectDate().perform(click());
		CarSearchParamsModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		screenshot("Car_Search_Params_Entered");
	}

}
