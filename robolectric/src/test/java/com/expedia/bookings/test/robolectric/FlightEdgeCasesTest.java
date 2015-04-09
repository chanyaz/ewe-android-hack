package com.expedia.bookings.test.robolectric;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.server.TripParser;
import com.mobiata.flightlib.data.Airline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class FlightEdgeCasesTest {

	@Test
	public void flightCheckinNotification() throws Throwable {
		final String filePath = "../lib/mocked/testdata/FlightEdgeCasesTestData_TripFlight.json";
		String data = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
		JSONObject json = new JSONObject(data);

		TripParser parser = new TripParser();
		TripFlight tripFlight = parser.parseTripFlight(json);
		assertNotNull(tripFlight);

		FlightTrip flightTrip = tripFlight.getFlightTrip();
		assertNotNull(flightTrip);

		FlightLeg flightLeg = flightTrip.getLeg(0);
		assertNotNull(flightLeg);

		Set<String> primaryAirlines = flightLeg.getPrimaryAirlines();
		assertEquals(1, primaryAirlines.size());
		String primaryAirline = primaryAirlines.iterator().next();
		assertEquals("TB", primaryAirline);

		// Ensure no crashy
		String airlinesStr = flightLeg.getAirlinesFormatted();
		assertEquals("", airlinesStr);
	}

	@Test
	public void dbGetAirlineTest() {
		// Delta, should have full airline name
		Airline delta = Db.getAirline("DL");
		assertEquals("Delta Air Lines", delta.mAirlineName);

		// Jetairfly, not present in FS.db
		Airline jetflyAirways = Db.getAirline("TB");
		assertNull(jetflyAirways.mAirlineName);
	}
}
