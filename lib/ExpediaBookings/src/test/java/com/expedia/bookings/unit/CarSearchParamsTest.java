package com.expedia.bookings.unit;

import org.joda.time.DateTime;
import org.junit.Test;

import com.expedia.bookings.data.cars.CarSearchParams;

import static junit.framework.Assert.assertEquals;

public class CarSearchParamsTest {

	@Test
	public void testClone() {
		CarSearchParams ogParams = new CarSearchParams();
		DateTime ogStart = DateTime.now().withTimeAtStartOfDay();
		DateTime ogEnd = ogStart.plusDays(3);
		String ogLocation = "SFO";

		ogParams.startTime = ogStart;
		ogParams.endTime = ogEnd;
		ogParams.origin = ogLocation;

		CarSearchParams otherParams = ogParams.clone();
		assertEquals(ogStart, otherParams.startTime);
		assertEquals(ogEnd, otherParams.endTime);
		assertEquals(ogLocation, otherParams.origin);

		String newLocation = "DTW";
		ogParams.startTime = DateTime.now().plusDays(2);
		ogParams.endTime = DateTime.now().plusDays(2);
		ogParams.origin = newLocation;

		assertEquals(ogStart, otherParams.startTime);
		assertEquals(ogEnd, otherParams.endTime);
		assertEquals(ogLocation, otherParams.origin);
	}
}
