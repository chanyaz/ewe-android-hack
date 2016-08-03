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

		CarSearchParamsBuilder spBuilder = new CarSearchParamsBuilder();
		CarSearchParams params = null;
		CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder();

		// Test nulls
		params = spBuilder.origin("SFO").build();
		Assert.assertEquals(null, params.startDateTime);
		Assert.assertEquals(null, params.endDateTime);

		// Test start
		dateTimeBuilder.startDate(today);
		spBuilder.dateTimeBuilder(dateTimeBuilder);
		params = spBuilder.build();
		Assert.assertEquals(now, params.startDateTime);
		Assert.assertEquals(null, params.endDateTime);

		// Test both
		dateTimeBuilder.endDate(tomorrow);
		spBuilder = new CarSearchParamsBuilder()
			.dateTimeBuilder(dateTimeBuilder);

		params = spBuilder.origin("SFO").build();
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
		CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder();

		//Test start time with millis set
		//Set to 12:30 am
		int millis = 30 * 60 * 1000;
		dateTimeBuilder.startDate(today).startMillis(millis);
		builder.dateTimeBuilder(dateTimeBuilder);
		params = builder.origin("SFO").build();
		Assert.assertEquals(now.plusMillis(millis), params.startDateTime);

		//Test end time with millis set
		millis = millis * 2;
		dateTimeBuilder.endDate(tomorrow).endMillis(millis);
		builder.dateTimeBuilder(dateTimeBuilder);
		params = builder.build();
		Assert.assertEquals(now.plusDays(1).plusMillis(millis), params.endDateTime);
	}

	@Test
	public void testOrigin() {
		String origin = "SFO";
		CarSearchParamsBuilder builder = new CarSearchParamsBuilder();
		CarSearchParams params = builder.origin(origin).build();
		Assert.assertEquals(origin, params.origin);
	}

	@Test
	public void testRequiredParamsFilled() {
		String origin = "SFO";
		LocalDate now = LocalDate.now();
		CarSearchParamsBuilder builder = new CarSearchParamsBuilder();
		CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder();

		Assert.assertFalse(builder.areRequiredParamsFilled());
		// Fill only origin
		builder.origin(origin);
		Assert.assertFalse(builder.areRequiredParamsFilled());
		// Empty DateTimeBuilder
		builder.dateTimeBuilder(dateTimeBuilder);
		Assert.assertFalse(builder.areRequiredParamsFilled());
		// Fill end date also
		dateTimeBuilder.endDate(now);
		builder.dateTimeBuilder(dateTimeBuilder);
		Assert.assertFalse(builder.areRequiredParamsFilled());
		// Fill start date also
		dateTimeBuilder.startDate(now);
		builder.dateTimeBuilder(dateTimeBuilder);
		Assert.assertTrue(builder.areRequiredParamsFilled());
		dateTimeBuilder.endDate(null);
		builder.dateTimeBuilder(null);
		Assert.assertFalse(builder.areRequiredParamsFilled());
	}
}
