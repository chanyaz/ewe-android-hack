package com.expedia.bookings.test.robolectric;

import java.io.File;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.net.Uri;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.deeplink.CarDeepLink;
import com.expedia.bookings.deeplink.DeepLink;
import com.expedia.bookings.deeplink.DeepLinkParser;
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

	final DeepLinkParser deepLinkParser = new DeepLinkParser(RuntimeEnvironment.application.getAssets());

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

		CarSearchParam carSearchParams = CarDataUtils.fromFlightParams(flightTrip);

		assertEquals(carSearchParams.getStartDateTime().toLocalDate(), checkInDate);
		assertEquals(carSearchParams.getEndDateTime().toLocalDate(), checkOutDate);
		assertEquals(carSearchParams.getOriginLocation(), firstLeg.getAirport(false).mAirportCode);
		assertEquals(carSearchParams.getOriginDescription(), firstLeg.getAirport(false).mName);
	}

	@Test
	public void testFromDeeplinkWithAirportCode() {
		final String expectedURL = "expda://carSearch?pickupLocation=SFO&pickupDateTime=2015-06-26T09:00:00&dropoffDateTime=2015-06-27T09:00:00&originDescription=SFO-San Francisco International Airport";
		final String pickupLocation = "SFO";
		final DateTime pickupDateTime = new DateTime(2015, 6, 26, 9, 0, 0);
		final DateTime dropoffDateTime = new DateTime(2015, 6, 27, 9, 0, 0);
		final String originDescription = "SFO-San Francisco International Airport";

		CarSearchParam obtainedCarSearchParams = getCarSearchParamsFromDeeplink(expectedURL);
		CarSearchParam expectedCarSearchParams = (CarSearchParam) new CarSearchParam.Builder()
			.startDateTime(pickupDateTime).endDateTime(dropoffDateTime)
			.origin(CarDataUtils.getSuggestionFromLocation(pickupLocation, null, originDescription)).build();

		assertEquals(expectedCarSearchParams.getStartDateTime(), obtainedCarSearchParams.getStartDateTime());
		assertEquals(expectedCarSearchParams.getEndDateTime(), obtainedCarSearchParams.getEndDateTime());
		assertEquals(expectedCarSearchParams.getOriginLocation(), obtainedCarSearchParams.getOriginLocation());
		assertEquals(null, obtainedCarSearchParams.getPickupLocationLatLng());
		assertEquals(expectedCarSearchParams.getOriginDescription(), obtainedCarSearchParams.getOriginDescription());
	}

	@Test
	public void testFromDeeplinkWithLocationLatLng() {
		final String expectedURL = "expda://carSearch?pickupLocationLat=32.71444&pickupLocationLng=-117.16237&pickupDateTime=2015-06-26T09:00:00&dropoffDateTime=2015-06-27T09:00:00&originDescription=SFO-San Francisco International Airport";
		final double pickupLocationLat = 32.71444d;
		final double pickupLocationLng = -117.16237d;
		final String pickupDateTime = "2015-06-26T09:00:00";
		final String dropoffDateTime = "2015-06-27T09:00:00";
		final String originDescription = "SFO-San Francisco International Airport";

		CarSearchParam obtainedCarSearchParams = getCarSearchParamsFromDeeplink(expectedURL);
		DateTime startDateTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(pickupDateTime, DateTime.now());
		DateTime endDateTime = DateUtils
			.yyyyMMddTHHmmssToDateTimeSafe(dropoffDateTime, startDateTime.plusDays(3));

		CarSearchParam expectedCarSearchParams = (CarSearchParam) new CarSearchParam.Builder().pickupLocationLatLng(new LatLong(pickupLocationLat, pickupLocationLng))
			.startDateTime(startDateTime).endDateTime(endDateTime)
			.origin(CarDataUtils
				.getSuggestionFromLocation(null, new LatLong(pickupLocationLat, pickupLocationLng), originDescription))
			.build();

		assertEquals(expectedCarSearchParams.getStartDateTime(), obtainedCarSearchParams.getStartDateTime());
		assertEquals(expectedCarSearchParams.getEndDateTime(), obtainedCarSearchParams.getEndDateTime());
		assertEquals(null, obtainedCarSearchParams.getOriginLocation());
		assertEquals(expectedCarSearchParams.getPickupLocationLatLng().lat, obtainedCarSearchParams.getPickupLocationLatLng().lat, 1e-10);
		assertEquals(expectedCarSearchParams.getPickupLocationLatLng().lng, obtainedCarSearchParams.getPickupLocationLatLng().lng, 1e-10);
		assertEquals(expectedCarSearchParams.getOriginDescription(), obtainedCarSearchParams.getOriginDescription());
	}

	private CarSearchParam getCarSearchParamsFromDeeplink(String expectedURL) {
		DeepLink deepLink = deepLinkParser.parseDeepLink(Uri.parse(expectedURL));
		return CarDataUtils.fromDeepLink((CarDeepLink) deepLink);
	}
}
