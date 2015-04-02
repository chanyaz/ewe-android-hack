package com.mobiata.mocke3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

// Mocks out various mobile Expedia APIs
public class ExpediaDispatcher extends Dispatcher {

	protected FileOpener mFileOpener;

	public ExpediaDispatcher(FileOpener fileOpener) {
		mFileOpener = fileOpener;
	}

	@Override
	public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

		// Hotels API
		if (request.getPath().startsWith("/m/api/hotel") || request.getPath().startsWith("/api/m/trip/coupon")) {
			return dispatchHotel(request);
		}

		// Flights API
		if (request.getPath().contains("/api/flight")) {
			return dispatchFlight(request);
		}

		// Cars API
		if (request.getPath().contains("/m/api/cars")) {
			return dispatchCar(request);
		}

		// LX API
		if (request.getPath().contains("/lx/api") || request.getPath().contains("m/api/lx")) {
			return dispatchLX(request);
		}

		// Abacus API
		if (request.getPath().contains("/AB/layout")) {
			return makeResponse("/AB/layout/happy.json");
		}

		// Trips API
		if (request.getPath().startsWith("/api/trips")) {
			return makeResponse("/api/trips/happy.json");
		}

		// Expedia Suggest
		if (request.getPath().startsWith("/hint/es")) {
			return dispatchSuggest(request);
		}

		// User API
		if (request.getPath().contains("/api/user/sign-in")) {
			return dispatchSignIn(request);
		}

		// Omniture
		if (request.getPath().startsWith("/b/ss")) {
			return makeEmptyResponse();
		}

		// Omniture
		if (request.getPath().startsWith("/static")) {
			return dispatchStaticContent(request);
		}

		// User Profile/Stored Traveler info
		if (request.getPath().startsWith("/api/user/profile")) {
			return dispatchUserProfile(request);
		}

		// Not found
		return make404();
	}

	/////////////////////////////////////////////////////////////////////////////
	// Path dispatching

	private MockResponse dispatchHotel(RecordedRequest request) {
		if (request.getPath().startsWith("/m/api/hotel/search")) {
			return makeResponse("m/api/hotel/search/happy.json");
		}
		else if (request.getPath().startsWith("/m/api/hotel/offers")) {
			Map<String, String> params = parseRequest(request);
			return makeResponse("m/api/hotel/offers/" + params.get("hotelId") + ".json", params);
		}
		else if (request.getPath().startsWith("/m/api/hotel/product")) {
			Map<String, String> params = parseRequest(request);
			if (params.get("productKey").startsWith("hotel_coupon_errors")) {
				params.put("productKey", "hotel_coupon_errors");
			}
			return makeResponse("m/api/hotel/product/" + params.get("productKey") + ".json", params);
		}
		else if (request.getPath().startsWith("/m/api/hotel/trip/create")) {
			Map<String, String> params = parseRequest(request);
			String filename = "m/api/hotel/trip/create/" + params.get("productKey") + ".json";
			if (params.get("productKey").startsWith("hotel_coupon_errors")) {
				filename = "m/api/hotel/trip/create/hotel_coupon_errors.json";
			}
			return makeResponse(filename, params);
		}
		else if (request.getPath().startsWith("/api/m/trip/coupon")) {
			Map<String, String> params = parseRequest(request);
			return makeResponse("api/m/trip/coupon/" + params.get("tripId") + ".json", params);
		}
		else if (request.getPath().startsWith("/m/api/hotel/trip/checkout")) {
			Map<String, String> params = parseRequest(request);
			String filename = "m/api/hotel/trip/checkout/" + params.get("tripId") + ".json";
			if (params.get("tripId").startsWith("hotel_coupon_errors")) {
				filename = "m/api/hotel/trip/create/hotel_coupon_errors.json";
			}
			return makeResponse(filename, params);
		}
		return make404();
	}

	private MockResponse dispatchFlight(RecordedRequest request) {
		if (request.getPath().startsWith("/api/flight/search")) {
			Map<String, String> params = parseRequest(request);
			String filename = "happy_oneway";

			Calendar departCalTakeoff = parseYearMonthDay(params.get("departureDate"), 10, 0);
			Calendar departCalLanding = parseYearMonthDay(params.get("departureDate"), 12 + 4, 0);
			params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.getTimeInMillis() / 1000));
			params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.getTimeInMillis() / 1000));

			if (params.containsKey("returnDate")) {
				filename = "happy_roundtrip";
				Calendar returnCalTakeoff = parseYearMonthDay(params.get("returnDate"), 10, 0);
				Calendar returnCalLanding = parseYearMonthDay(params.get("returnDate"), 12 + 4, 0);
				params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.getTimeInMillis() / 1000));
				params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.getTimeInMillis() / 1000));
			}

			params.put("tzOffsetSeconds",
				"" + (departCalTakeoff.getTimeZone().getOffset(departCalTakeoff.getTimeInMillis()) / 1000));

			return makeResponse("api/flight/search/" + filename + ".json", params);
		}
		else if (request.getPath().startsWith("/api/flight/trip/create")) {
			Map<String, String> params = parseRequest(request);
			return makeResponse("api/flight/trip/create/" + params.get("productKey") + ".json", params);
		}
		else if (request.getPath().startsWith("/api/flight/checkout")) {
			Map<String, String> params = parseRequest(request);

			if (params.get("tripId").startsWith("air_attach_0")) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.DATE, 10);
				long millisFromEpoch = (c.getTimeInMillis() / 1000);
				int tzOffsetSeconds = (c.getTimeZone().getOffset(c.getTimeInMillis()) / 1000);

				params.put("airAttachEpochSeconds", "" + millisFromEpoch);
				params.put("airAttachTimeZoneOffsetSeconds", "" + tzOffsetSeconds);
			}
			return makeResponse("api/flight/checkout/" + params.get("tripId") + ".json", params);
		}
		return make404();
	}

	private MockResponse dispatchCar(RecordedRequest request) {
		if (request.getPath().contains("/search/airport")) {
			Map<String, String> params = parseRequest(request);
			String airportCode = params.get("airportCode");
			if ("KTM".equals(airportCode)) {
				return makeResponse("m/api/cars/search/airport/ktm_no_product.json");
			}
			else if ("DTW".equals(airportCode)) {
				return makeResponse("m/api/cars/search/airport/dtw_invalid_input.json");
			}
			else {
				return makeResponse("m/api/cars/search/airport/happy.json");
			}
		}
		else if (request.getPath().contains("/trip/create")) {
			Map<String, String> params = parseRequest(request);
			return makeResponse("m/api/cars/trip/create/" + params.get("productKey") + ".json", params);
		}
		else if (request.getPath().contains("/trip/checkout")) {
			Map<String, String> params = parseRequest(request);
			switch (params.get("mainMobileTraveler.firstName")) {
			case "AlreadyBooked":
				return makeResponse("m/api/cars/trip/checkout/trip_already_booked.json");
			case "PriceChange":
				return makeResponse("m/api/cars/trip/checkout/price_change.json");
			case "PaymentFailed":
				return makeResponse("m/api/cars/trip/checkout/payment_failed.json");
			case "UnknownError":
				return makeResponse("m/api/cars/trip/checkout/unknown_error.json");
			case "SessionTimeout":
				return makeResponse("m/api/cars/trip/checkout/session_timeout.json");
			case "InvalidInput":
				return makeResponse("m/api/cars/trip/checkout/invalid_input.json");
			default:
				return makeResponse("m/api/cars/trip/checkout/happy.json");
			}
		}
		return make404();
	}

	private MockResponse dispatchSuggest(RecordedRequest request) {
		String type = "";
		Map<String, String> params = parseRequest(request);
		if (params.containsKey("type")) {
			type = params.get("type");
		}

		if (request.getPath().startsWith("/hint/es/v2/ac/en_US")) {
			String requestPath = request.getPath();
			String filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'));
			return makeResponse("hint/es/v2/ac/en_US/" + unUrlEscape(filename) + ".json");
		}
		else if (request.getPath().startsWith("/hint/es/v3/ac/en_US")) {
			if (type.equals("14")) {
				return makeResponse("/hint/es/v3/ac/en_US/suggestion_city.json");
			}
			else {
				return makeResponse("/hint/es/v3/ac/en_US/suggestion.json");
			}
		}
		else if (request.getPath().startsWith("/hint/es/v1/nearby/en_US")) {
			// City
			if (type.equals("14")) {
				return makeResponse("/hint/es/v1/nearby/en_US/suggestion_city.json");
			}
			else {
				return makeResponse("/hint/es/v1/nearby/en_US/suggestion.json");
			}
		}
		return make404();
	}

	private MockResponse dispatchSignIn(RecordedRequest request) {
		// TODO Handle the case when there's no email parameter in 2nd sign-in request
		Map<String, String> params = parseRequest(request);
		params.put("email", "qa-ehcc@mobiata.com");
		return makeResponse("api/user/sign-in/login.json", params);
	}

	private MockResponse dispatchLX(RecordedRequest request) {
		if (request.getPath().startsWith("/lx/api/search")) {
			Map<String, String> params = parseRequest(request);
			String location = params.get("location");
			// Return happy path response if not testing for special cases.
			if (location.equals("search_failure")) {
				return makeResponse("lx/api/search/" + location + ".json");
			}
			else {
				return makeResponse("lx/api/search/happy.json");
			}
		}
		else if (request.getPath().startsWith("/lx/api/activity")) {
			Map<String, String> params = parseRequest(request);
			final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
			final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
			final DateTime endDateTime = startDateTime.plusDays(5);
			// supply the dates to the response
			params.put("startDate",startDateTime.toString(DATE_TIME_PATTERN));
			for (int iPlusDays = 1; iPlusDays < 14; iPlusDays++) {
				params.put("startDatePlus" + iPlusDays, startDateTime.plusDays(iPlusDays).toString(DATE_TIME_PATTERN));
			}
			return makeResponse("lx/api/activity/happy.json", params);
		}

		else if (request.getPath().contains("/trip/create")) {

			if (request.getUtf8Body().contains("error_activity_id")) {
				return makeResponse("m/api/lx/trip/create/error_create_trip.json");
			}
			return makeResponse("m/api/lx/trip/create/happy.json");
		}

		else if (request.getPath().contains("/trip/checkout")) {
			Map<String, String> params = parseRequest(request);
			String firstName = params.get("firstName");
			String tripId = params.get("tripId");

			if (firstName != null) {
				if (firstName.equals("InvalidInput")) {
					return makeResponse("m/api/lx/trip/checkout/invalid_input.json");
				}
			}
			return makeResponse("m/api/lx/trip/checkout/" + tripId + ".json");
		}
		return make404();
	}

	private MockResponse dispatchStaticContent(RecordedRequest request) {
		return makeResponse(request.getPath());
	}

	private MockResponse dispatchUserProfile(RecordedRequest request) {
		Map<String, String> params = parseRequest(request);
		return makeResponse("api/user/profile/user_profile_" + params.get("tuid") + ".json");
	}

	/////////////////////////////////////////////////////////////////////////////
	// Utilities

	private String unUrlEscape(String str) {
		return str.replace("%20", " ");
	}

	private Calendar parseYearMonthDay(String ymd, int hour, int minute) {
		String[] parts = ymd.split("-");
		int year = Integer.parseInt(parts[0]);
		int month = Integer.parseInt(parts[1]) - 1;
		int day = Integer.parseInt(parts[2]);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, minute);
		return cal;
	}

	private static Map<String, String> parseRequest(RecordedRequest request) {
		if ("GET".equals(request.getMethod()) && request.getRequestLine().contains("?")) {
			String requestLine = request.getRequestLine().split("\\?")[1];
			// Replace "HTTP version" from request line.
			requestLine = requestLine.substring(0, requestLine.lastIndexOf(" "));
			return constructParamsFromVarArray(requestLine);
		}
		else if ("POST".equals(request.getMethod())) {
			return constructParamsFromVarArray(request.getUtf8Body());
		}
		return new LinkedHashMap<>();
	}

	private static Map<String, String> constructParamsFromVarArray(String requestStr) {
		String[] requestVariablePairs = requestStr.split("&");
		Map<String, String> params = new LinkedHashMap<>();
		for (String pair : requestVariablePairs) {
			final int idx = pair.indexOf("=");
			try {
				final String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
				final String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
				params.put(key, value);
			}
			catch (UnsupportedEncodingException e) {
				// ignore - just skip the pair
			}
		}
		return params;
	}

	private MockResponse makeEmptyResponse() {
		MockResponse resp = new MockResponse();
		resp.setResponseCode(200);
		return resp;
	}

	private MockResponse make404() {
		return new MockResponse().setResponseCode(404);
	}

	private MockResponse makeResponse(String filePath) {
		return makeResponse(filePath, null);
	}

	private MockResponse makeResponse(String filePath, Map<String, String> params) {
		// Handle all FileOpener implementations
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}

		MockResponse resp = new MockResponse();
		try {
			String body = getResponse(filePath);
			if (params != null) {
				Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> entry = it.next();
					final String key = "${" + entry.getKey() + "}";
					if (body.contains(entry.getKey())) {
						body = body.replace(key, entry.getValue());
					}
				}
			}
			resp.setBody(body);
			resp.setHeader("Content-Type", "application/json");
		}
		catch (Exception e) {
			resp.setResponseCode(404);
		}
		return resp;
	}

	// Read the json responses from the fileopener
	private String getResponse(String filename) throws IOException {
		InputStream inputStream = mFileOpener.openFile(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			return sb.toString();
		}
		finally {
			br.close();
		}
	}
}

