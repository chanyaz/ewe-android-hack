package com.expedia.bookings.test.tests.unit;

import junit.framework.TestCase;

import com.expedia.bookings.data.Date;
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

	public void testArrivalAirportCodeDefault() {
		FlightSearchLeg leg = new FlightSearchLeg();

		String expectedDefault = "SMF";
		assertEquals(expectedDefault, leg.getArrivalLocation());
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

		Date expectedDate = new Date();
		leg.setDepartureDate(expectedDate);

		assertEquals(expectedDate, leg.getDepartureDate());
	}
}
