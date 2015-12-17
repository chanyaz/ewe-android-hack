package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.FlightSearchLeg;
import com.expedia.bookings.data.Location;

@RunWith(RobolectricRunner.class)
public class FlightSearchLegTest {

	@Test
	public void testDepartureAirportCode() {
		String expectedCode = "DTW";
		FlightSearchLeg leg = new FlightSearchLeg();
		Location location = new Location();
		location.setDestinationId(expectedCode);
		leg.setDepartureLocation(location);

		Assert.assertEquals(expectedCode, leg.getDepartureLocation().getDestinationId());
	}

	@Test
	public void testArrivalAirportCode() {
		FlightSearchLeg leg = new FlightSearchLeg();
		String expectedCode = "ATL";
		Location location = new Location();
		location.setDestinationId(expectedCode);
		leg.setArrivalLocation(location);

		Assert.assertEquals(expectedCode, leg.getArrivalLocation().getDestinationId());
	}

	@Test
	public void testDepartureDate() {
		FlightSearchLeg leg = new FlightSearchLeg();

		LocalDate expectedDate = new LocalDate();
		leg.setDepartureDate(expectedDate);

		Assert.assertEquals(expectedDate, leg.getDepartureDate());
	}
}
