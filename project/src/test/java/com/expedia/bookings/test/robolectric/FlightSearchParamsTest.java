package com.expedia.bookings.test.robolectric;

import java.util.LinkedList;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightSearchLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Location;

@RunWith(RobolectricRunner.class)
public class FlightSearchParamsTest {

	@Test
	public void testNumAdults() {
		FlightSearchParams searchParams = new FlightSearchParams();
		int expectedNumAdults = 2;

		searchParams.setNumAdults(expectedNumAdults);

		Assert.assertEquals(expectedNumAdults, searchParams.getNumAdults());
	}

	@Test
	public void testNumChildren() {
		FlightSearchParams searchParams = new FlightSearchParams();
		LinkedList<ChildTraveler> children = new LinkedList<ChildTraveler>();

		int expectedNumChildren = 2;
		children.add(new ChildTraveler(3, true));
		children.add(new ChildTraveler(5, true));

		searchParams.setChildren(children);

		Assert.assertEquals(expectedNumChildren, searchParams.getNumChildren());
	}

	@Test
	public void testAddQueryLeg() {
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.addQueryLeg(new FlightSearchLeg());
		searchParams.addQueryLeg(new FlightSearchLeg());

		int expectedNumQueryLegs = 3; // by default, a new FlightSearchParams has 1 FlightSearchLeg

		Assert.assertEquals(expectedNumQueryLegs, searchParams.getQueryLegCount());
	}

	@Test
	public void testResetCreatesOneFlightSearchLeg() {
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.reset();

		int expectedNumQueryLegs = 1;
		Assert.assertEquals(expectedNumQueryLegs, searchParams.getQueryLegCount());
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods

	@Test
	public void testIsRoundTripFalse() {
		FlightSearchParams searchParams = new FlightSearchParams();
		Assert.assertFalse(searchParams.isRoundTrip());
	}

	@Test
	public void testIsRoundTripTrue() {
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.addQueryLeg(new FlightSearchLeg());
		Assert.assertTrue(searchParams.isRoundTrip());
	}

	@Test
	public void testSetReturnDateTriggersRoundTripMode() {
		FlightSearchParams searchParams = new FlightSearchParams();

		searchParams.setDepartureDate(new LocalDate());
		searchParams.setReturnDate(new LocalDate());

		Assert.assertTrue(searchParams.isRoundTrip());
	}

	@Test
	public void testDepartureAirportCode() {
		String airport = "DTW";
		Location location = new Location();
		location.setDestinationId(airport);
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setDepartureLocation(location);

		Assert.assertEquals(airport, searchParams.getDepartureLocation().getDestinationId());
	}

	@Test
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

		searchParams.setDepartureDate(new LocalDate());
		searchParams.setReturnDate(new LocalDate());

		Assert.assertEquals(airport1, searchParams.getQueryLeg(1).getArrivalLocation().getDestinationId());
		Assert.assertEquals(airport2, searchParams.getQueryLeg(1).getDepartureLocation().getDestinationId());
	}

	@Test
	public void testDepartureDate() {
		LocalDate date = new LocalDate();
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setDepartureDate(date);

		Assert.assertEquals(date, searchParams.getDepartureDate());
	}

	@Test
	public void testArrivalAirportCode() {
		String airport = "SFO";
		Location location = new Location();
		location.setDestinationId(airport);
		FlightSearchParams searchParams = new FlightSearchParams();
		searchParams.setArrivalLocation(location);

		Assert.assertEquals(airport, searchParams.getArrivalLocation().getDestinationId());
	}

}
