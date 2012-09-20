package com.expedia.bookings.test.tests.unit;

import java.util.LinkedList;

import junit.framework.TestCase;

import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.FlightSearchLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Location;

public class FlightSearchParamsTest extends TestCase {

	public void testNumAdults() {
		FlightSearchParams searchParams = new FlightSearchParams();
		int expectedNumAdults = 2;

		searchParams.setNumAdults(expectedNumAdults);

		assertEquals(expectedNumAdults, searchParams.getNumAdults());
	}

	public void testNumChildren() {
		FlightSearchParams searchParams = new FlightSearchParams();
		LinkedList<Integer> children = new LinkedList<Integer>();

		int expectedNumChildren = 2;
		children.add(3);
		children.add(5);

		searchParams.setChildren(children);

		assertEquals(expectedNumChildren, searchParams.getNumChildren());
	}

	public void testAddQueryLeg() {
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.addQueryLeg(new FlightSearchLeg());
		searchParams.addQueryLeg(new FlightSearchLeg());

		int expectedNumQueryLegs = 3; // by default, a new FlightSearchParams has 1 FlightSearchLeg

		assertEquals(expectedNumQueryLegs, searchParams.getQueryLegCount());
	}

	public void testResetCreatesOneFlightSearchLeg() {
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.reset();

		int expectedNumQueryLegs = 1;
		assertEquals(expectedNumQueryLegs, searchParams.getQueryLegCount());
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods

	public void testIsRoundTripFalse() {
		FlightSearchParams searchParams = new FlightSearchParams();
		assertFalse(searchParams.isRoundTrip());
	}

	public void testIsRoundTripTrue() {
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.addQueryLeg(new FlightSearchLeg());
		assertTrue(searchParams.isRoundTrip());
	}

	public void testSetReturnDateTriggersRoundTripMode() {
		FlightSearchParams searchParams = new FlightSearchParams();

		searchParams.setDepartureDate(new Date());
		searchParams.setReturnDate(new Date());

		assertTrue(searchParams.isRoundTrip());
	}

	public void testDepartureAirportCode() {
		String airport = "DTW";
		Location location = new Location();
		location.setDestinationId(airport);
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setDepartureLocation(location);

		assertEquals(airport, searchParams.getDepartureLocation().getDestinationId());
	}

	public void testSetReturnDateTriggersEnsureRoundTripMode() {
		String airport1 = "DTW";
		String airport2 = "SFO";
		Location location1 = new Location();
		location1.setDestinationId(airport1);
		Location location2 = new Location();
		location2.setDestinationId(airport2);
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setDepartureLocation(location1);
		searchParams.setArrivalLocation(location2);

		searchParams.setDepartureDate(new Date());
		searchParams.setReturnDate(new Date());

		assertEquals(airport1, searchParams.getQueryLeg(1).getArrivalLocation().getDestinationId());
		assertEquals(airport2, searchParams.getQueryLeg(1).getDepartureLocation().getDestinationId());
	}

	public void testDepartureDate() {
		Date date = new Date();
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setDepartureDate(date);

		assertEquals(date, searchParams.getDepartureDate());
	}

	public void testArrivalAirportCode() {
		String airport = "SFO";
		Location location = new Location();
		location.setDestinationId(airport);
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setArrivalLocation(location);

		assertEquals(airport, searchParams.getArrivalLocation().getDestinationId());
	}

}