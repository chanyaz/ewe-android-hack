package com.expedia.bookings.test.robolectric;

import com.expedia.bookings.data.FlightFilter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricRunner.class)
public class FlightFilterTest {

	@Test
	public void testNewFlightFilterDefaultsSortToPrice() {
		FlightFilter flightFilter = new FlightFilter();
		Assert.assertEquals(FlightFilter.Sort.PRICE, flightFilter.getSort());
	}
}
