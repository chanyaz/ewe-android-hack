package com.expedia.bookings.test.robolectric;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.server.TripParser;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class CarDataUtilsTest {

	@Test
	public void testGetInfoCount() {
		Interval interval = new Interval();
		String s = CarDataUtils.getInfoCount(interval);
		assertNull(s);

		interval.addIgnoreZero(0);
		s = CarDataUtils.getInfoCount(interval);
		assertNull(s);

		// 1, 1 maps to "1"
		interval.addIgnoreZero(1);
		s = CarDataUtils.getInfoCount(interval);
		assertEquals("1", s);

		// 1, 4 maps to "1-4"
		interval.addIgnoreZero(4);
		s = CarDataUtils.getInfoCount(interval);
		assertEquals("1-4", s);
	}

	@Test
	public void testfromFlightParams() throws Throwable {
		final String filePath = "../lib/mocked/testdata/FlightEdgeCasesTestData_TripFlight.json";
		String data = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
		JSONObject json = new JSONObject(data);

		TripParser parser = new TripParser();
		TripFlight tripFlight = parser.parseTripFlight(json);
		assertNotNull(tripFlight);

		FlightTrip flightTrip = tripFlight.getFlightTrip();

		FlightLeg firstLeg = flightTrip.getLeg(0);
		FlightLeg secondLeg = flightTrip.getLegCount() > 1 ? flightTrip.getLeg(1) : null;
		LocalDate checkInDate = new LocalDate(firstLeg.getLastWaypoint().getBestSearchDateTime());

		LocalDate checkOutDate;
		if (secondLeg == null) {
			// 1-way flight
			checkOutDate = checkInDate.plusDays(1);
		}
		else {
			// Round-trip flight
			checkOutDate = new LocalDate(secondLeg.getFirstWaypoint()
				.getMostRelevantDateTime());
		}

		CarSearchParams carSearchParams = CarDataUtils.fromFlightParams(flightTrip);

		assertEquals(carSearchParams.startDateTime.toLocalDate(), checkInDate);
		assertEquals(carSearchParams.endDateTime.toLocalDate(), checkOutDate);
		assertEquals(carSearchParams.origin, firstLeg.getAirport(false).mAirportCode);
		assertEquals(carSearchParams.originDescription, firstLeg.getAirport(false).mName);


	}
}
