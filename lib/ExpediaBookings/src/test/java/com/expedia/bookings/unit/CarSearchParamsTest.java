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

		ogParams.startDateTime = ogStart;
		ogParams.endDateTime = ogEnd;
		ogParams.origin = ogLocation;

		CarSearchParams otherParams = ogParams.clone();
		assertEquals(ogStart, otherParams.startDateTime);
		assertEquals(ogEnd, otherParams.endDateTime);
		assertEquals(ogLocation, otherParams.origin);

		String newLocation = "DTW";
		ogParams.startDateTime = DateTime.now().plusDays(2);
		ogParams.endDateTime = DateTime.now().plusDays(2);
		ogParams.origin = newLocation;

		assertEquals(ogStart, otherParams.startDateTime);
		assertEquals(ogEnd, otherParams.endDateTime);
		assertEquals(ogLocation, otherParams.origin);
	}
}
