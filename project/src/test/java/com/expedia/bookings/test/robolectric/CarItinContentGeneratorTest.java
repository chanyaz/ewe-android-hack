package com.expedia.bookings.test.robolectric;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripCar;
import com.expedia.bookings.widget.itin.CarItinContentGenerator;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class CarItinContentGeneratorTest {

	DateTime checkIn;

	@Before
	public void before() {
		checkIn = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
	}

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	private CarItinContentGenerator getCarItinGenerator(DateTime checkIn, DateTime checkOut) {
		TripCar trip = new TripCar();
		trip.setStartDate(checkIn);
		trip.setEndDate(checkOut);
		Car car = new Car();
		car.setCategory(CarCategory.FULLSIZE);
		car.setType(CarType.SUV);
		trip.setCar(car);
		return new CarItinContentGenerator(getContext(), new ItinCardDataCar(trip));
	}

	@Test
	public void testCarCategoryType() {
		CarItinContentGenerator generator = getCarItinGenerator(checkIn, checkIn.plusDays(5));
		assertEquals("Full-size SUV", generator.getCarCategoryType(getContext()));
	}
}
