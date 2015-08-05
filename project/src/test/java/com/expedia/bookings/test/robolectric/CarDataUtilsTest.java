package com.expedia.bookings.test.robolectric;

import java.io.File;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.net.Uri;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.server.TripParser;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Interval;

import okio.Okio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricRunner.class)
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
		String data = Okio.buffer(Okio.source(new File(filePath))).readUtf8();
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
			checkOutDate = checkInDate.plusDays(3);
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

	@Test
	public void testFromDeeplinkWithAirportCode() {
		final String expectedURL = "expda://carSearch?pickupLocation=SFO&pickupDateTime=2015-06-26T09:00:00&dropoffDateTime=2015-06-27T09:00:00&originDescription=SFO-San Francisco International Airport";
		final String pickupLocation = "SFO";
		final String pickupDateTime = "2015-06-26T09:00:00";
		final String dropoffDateTime = "2015-06-27T09:00:00";
		final String originDescription = "SFO-San Francisco International Airport";

		CarSearchParams obtainedCarSearchParams = getCarSearchParamsFromDeeplink(expectedURL);

		CarSearchParams expectedCarSearchParams = new CarSearchParams();
		expectedCarSearchParams.startDateTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(pickupDateTime, DateTime.now());
		expectedCarSearchParams.endDateTime = DateUtils
			.yyyyMMddTHHmmssToDateTimeSafe(dropoffDateTime, expectedCarSearchParams.startDateTime.plusDays(3));

		expectedCarSearchParams.origin = pickupLocation;
		expectedCarSearchParams.originDescription = originDescription;

		assertEquals(expectedCarSearchParams.startDateTime, obtainedCarSearchParams.startDateTime);
		assertEquals(expectedCarSearchParams.endDateTime, obtainedCarSearchParams.endDateTime);
		assertEquals(expectedCarSearchParams.origin, obtainedCarSearchParams.origin);
		assertEquals(expectedCarSearchParams.originDescription, obtainedCarSearchParams.originDescription);
	}

	@Test
	public void testFromDeeplinkWithLocationLatLng() {
		final String expectedURL = "expda://carSearch?pickupLocationLat=32.71444&pickupLocationLng=-117.16237&pickupDateTime=2015-06-26T09:00:00&dropoffDateTime=2015-06-27T09:00:00&originDescription=SFO-San Francisco International Airport";
		final double pickupLocationLat = 32.71444d;
		final double pickupLocationLng = -117.16237d;
		final String pickupDateTime = "2015-06-26T09:00:00";
		final String dropoffDateTime = "2015-06-27T09:00:00";
		final String originDescription = "SFO-San Francisco International Airport";

		CarSearchParams obtainedCarSearchParams = getCarSearchParamsFromDeeplink(expectedURL);

		CarSearchParams expectedCarSearchParams = new CarSearchParams();
		expectedCarSearchParams.startDateTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(pickupDateTime, DateTime.now());
		expectedCarSearchParams.endDateTime = DateUtils
			.yyyyMMddTHHmmssToDateTimeSafe(dropoffDateTime, expectedCarSearchParams.startDateTime.plusDays(3));

		expectedCarSearchParams.pickupLocationLatLng = new LatLong(pickupLocationLat, pickupLocationLng);
		expectedCarSearchParams.originDescription = originDescription;

		assertEquals(expectedCarSearchParams.startDateTime, obtainedCarSearchParams.startDateTime);
		assertEquals(expectedCarSearchParams.endDateTime, obtainedCarSearchParams.endDateTime);
		assertEquals(expectedCarSearchParams.pickupLocationLatLng.lat, obtainedCarSearchParams.pickupLocationLatLng.lat, 1e-10);
		assertEquals(expectedCarSearchParams.pickupLocationLatLng.lng, obtainedCarSearchParams.pickupLocationLatLng.lng, 1e-10);
		assertEquals(expectedCarSearchParams.originDescription, obtainedCarSearchParams.originDescription);
	}

	private CarSearchParams getCarSearchParamsFromDeeplink(String expectedURL) {
		Uri data = Uri.parse(expectedURL);
		Set<String> queryData = data.getQueryParameterNames();
		return CarDataUtils.fromDeepLink(data, queryData);
	}
}
