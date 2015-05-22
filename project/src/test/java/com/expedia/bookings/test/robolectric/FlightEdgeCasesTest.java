package com.expedia.bookings.test.robolectric;

import java.io.File;
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
import okio.Okio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricRunner.class)
public class FlightEdgeCasesTest {

	/**
	 * This test mimics the important logic exercised for the flight check-in notification:
	 * (1) parsing of a trips API response (2) rendering a string for the notification. This
	 * crash has been reported on Crashlytics.
	 *
	 * @throws Throwable
	 */
	@Test
	public void flightCheckInNotification() throws Throwable {
		final String filePath = "../lib/mocked/testdata/FlightEdgeCasesTestData_TripFlight.json";
		String data = Okio.buffer(Okio.source(new File(filePath))).readUtf8();
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

		String airlinesStr = flightLeg.getPrimaryAirlineNamesFormatted();
		assertEquals("Jetairfly", airlinesStr);
	}

	@Test
	public void dbGetAirlineTest() {
		// Delta, should have full airline name
		Airline delta = Db.getAirline("DL");
		assertEquals("Delta Air Lines", delta.mAirlineName);

		// Jetairfly is listed in FS.db as its ICAO code ("JAF"), Expedia
		// API returns the code as it's IATA code ("TB")
		Airline jetflyAirways = Db.getAirline("TB");
		assertNull(jetflyAirways.mAirlineName);
	}
}
