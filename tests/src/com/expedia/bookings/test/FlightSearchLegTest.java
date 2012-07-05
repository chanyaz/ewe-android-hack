package com.expedia.bookings.test;

import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.FlightSearchLeg;
import junit.framework.TestCase;

public class FlightSearchLegTest extends TestCase {

	public void testDepartureAirportCodeDefault() {
		FlightSearchLeg leg = new FlightSearchLeg();

		String expectedDefault = "MSP";
		assertEquals(expectedDefault, leg.getDepartureAirportCode());
	}

	public void testDepartureAirportCode() {
		String expectedCode = "DTW";
		FlightSearchLeg leg = new FlightSearchLeg();
		leg.setDepartureAirportCode(expectedCode);

		assertEquals(expectedCode, leg.getDepartureAirportCode());
	}

	public void testArrivalAirportCodeDefault() {
		FlightSearchLeg leg = new FlightSearchLeg();

		String expectedDefault = "SMF";
		assertEquals(expectedDefault, leg.getArrivalAirportCode());
	}

	public void testArrivalAirportCode() {
		FlightSearchLeg leg = new FlightSearchLeg();

		String expectedCode = "ATL";
		leg.setArrivalAirportCode(expectedCode);
		assertEquals(expectedCode, leg.getArrivalAirportCode());
	}

	public void testDepartureDate() {
		FlightSearchLeg leg = new FlightSearchLeg();

		Date expectedDate = new Date();
		leg.setDepartureDate(expectedDate);

		assertEquals(expectedDate, leg.getDepartureDate());
	}
}
