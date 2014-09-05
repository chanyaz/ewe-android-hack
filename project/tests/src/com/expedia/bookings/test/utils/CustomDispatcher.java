package com.expedia.bookings.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

/**
 * Created by dmadan on 7/22/14.
 */
public class CustomDispatcher extends Dispatcher {

	protected FileOpener mFileOpener;

	public CustomDispatcher(FileOpener fileOpener) {
		mFileOpener = fileOpener;
	}

	@Override
	public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
		if (request.getPath().startsWith("/b/ss")) {
			return makeEmptyResponse();
		}

		else if (request.getPath().contains("hint/es/v3/ac/en_US")) {
			return makeResponse("MockResponses/hint/es/v3/ac/en_US/suggestion.json");
		}

		else if (request.getPath().contains("hint/es/v2/ac/en_US")) {
			String requestPath = request.getPath();
			String filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'));
			return makeResponse("MockResponses/hint/es/v2/ac/en_US/" + cleanFilename(filename) + ".json");
		}

		else if (request.getPath().contains("/m/api/hotel/search")) {
			return makeResponse("MockResponses/m/api/hotel/search/happy.json");
		}

		else if (request.getPath().contains("/m/api/hotel/offers")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/m/api/hotel/offers/" + params.get("hotelId") + ".json", params);
		}

		else if (request.getPath().contains("/m/api/hotel/product")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/m/api/hotel/product/" + params.get("productKey") + ".json", params);
		}

		else if (request.getPath().contains("/m/api/hotel/trip/create")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/m/api/hotel/trip/create/" + params.get("productKey") + ".json", params);
		}

		else if (request.getPath().contains("/m/api/hotel/trip/checkout")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/m/api/hotel/trip/checkout/" + params.get("tripId") + ".json", params);
		}

		else if (request.getPath().contains("/api/flight/search")) {
			Map<String, String> params = parsePostParams(request);
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

			params.put("tzOffsetSeconds", "" + (departCalTakeoff.getTimeZone().getOffset(departCalTakeoff.getTimeInMillis()) / 1000));

			return makeResponse("MockResponses/api/flight/search/" + filename + ".json", params);
		}

		else if (request.getPath().contains("/api/flight/trip/create")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/api/flight/trip/create/" + params.get("productKey") + ".json");
		}

		else if (request.getPath().contains("/api/flight/checkout")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/api/flight/checkout/" + params.get("productKey") + ".json");
		}

		else if (request.getPath().contains("/api/user/sign-in")) {
			return makeResponse("MockResponses/api/user/sign-in/login.json");
		}

		return new MockResponse().setResponseCode(404);
	}

	private String cleanFilename(String filename) {
		return filename.replace("%20", " ");
	}

	public Calendar parseYearMonthDay(String ymd, int hour, int minute) {
		String[] parts = ymd.split("-");
		int year = Integer.parseInt(parts[0]);
		int month = Integer.parseInt(parts[1]) - 1;
		int day = Integer.parseInt(parts[2]);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, minute);
		return cal;
	}

	public static Map<String, String> parsePostParams(RecordedRequest request) {
		final String body = request.getUtf8Body();
		Map<String, String> params = new LinkedHashMap<String, String>();

		String[] pairs = body.split("&");
		for (String pair : pairs) {
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

	public MockResponse makeEmptyResponse() {
		MockResponse resp = new MockResponse();
		resp.setResponseCode(200);
		return resp;
	}

	public MockResponse makeResponse(String filePath) {
		return makeResponse(filePath, null);
	}

	public MockResponse makeResponse(String filePath, Map<String, String> params) {
		MockResponse resp = new MockResponse();
		try {
			String body = getResponse(filePath);
			if (params != null) {
				Iterator it = params.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
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

	//read the json responses from tests/assets/
	public String getResponse(String filename) throws IOException {
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

