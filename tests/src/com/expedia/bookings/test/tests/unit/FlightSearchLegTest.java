package com.expedia.bookings.test.tests.unit;

import junit.framework.TestCase;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.FlightSearchLeg;
import com.expedia.bookings.data.Location;

public class FlightSearchLegTest extends TestCase {

	public void testDepartureAirportCode() {
		String expectedCode = "DTW";
		FlightSearchLeg leg = new FlightSearchLeg();
		Location location = new Location();
		location.setDestinationId(expectedCode);
		leg.setDepartureLocation(location);

		assertEquals(expectedCode, leg.getDepartureLocation().getDestinationId());
	}

	public void testArrivalAirportCode() {
		FlightSearchLeg leg = new FlightSearchLeg();
		String expectedCode = "ATL";
		Location location = new Location();
		location.setDestinationId(expectedCode);
		leg.setArrivalLocation(location);

		assertEquals(expectedCode, leg.getArrivalLocation().getDestinationId());
	}

	public void testDepartureDate() {
		FlightSearchLeg leg = new FlightSearchLeg();

		LocalDate expectedDate = new LocalDate();
		leg.setDepartureDate(expectedDate);

		assertEquals(expectedDate, leg.getDepartureDate());
	}
}
