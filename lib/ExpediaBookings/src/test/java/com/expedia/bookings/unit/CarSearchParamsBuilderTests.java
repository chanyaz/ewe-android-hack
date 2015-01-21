package com.expedia.bookings.unit;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;

public class CarSearchParamsBuilderTests {

	@Test
	public void testStartDateAndEndDate() {
		DateTime now = DateTime.now().withTimeAtStartOfDay();
		LocalDate today = now.toLocalDate();
		LocalDate tomorrow = today.plusDays(1);

		CarSearchParamsBuilder builder = new CarSearchParamsBuilder();
		CarSearchParams params = null;

		// Test nulls
		params = builder.build();
		Assert.assertEquals(null, params.startDateTime);
		Assert.assertEquals(null, params.endDateTime);

		// Test start
		builder.startDate(today);
		params = builder.build();
		Assert.assertEquals(now, params.startDateTime);
		Assert.assertEquals(null, params.endDateTime);

		// Test both
		builder = new CarSearchParamsBuilder()
			.startDate(today)
			.endDate(tomorrow);

		params = builder.build();
		Assert.assertEquals(now, params.startDateTime);
		Assert.assertEquals(now.plusDays(1), params.endDateTime);
	}

	@Test
	public void testStartAndEndDatesWithMillis() {
		DateTime now = DateTime.now().withTimeAtStartOfDay();
		LocalDate today = now.toLocalDate();
		LocalDate tomorrow = today.plusDays(1);

		CarSearchParamsBuilder builder = new CarSearchParamsBuilder();
		CarSearchParams params;

		//Test start time with millis set
		//Set to 12:30 am
		int millis = 30 * 60 * 1000;
		builder.startDate(today)
			.startMillis(millis);
		params = builder.build();
		Assert.assertEquals(now.plusMillis(millis), params.startDateTime);

		//Test end time with millis set
		millis = millis * 2;
		builder.endDate(tomorrow)
			.endMillis(millis);
		params = builder.build();
		Assert.assertEquals(now.plusDays(1).plusMillis(millis), params.endDateTime);
	}

}
