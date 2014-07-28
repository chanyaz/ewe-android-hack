package com.expedia.bookings.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Instrumentation;
import android.content.Context;

import com.mobiata.android.Log;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

/**
 * Created by dmadan on 7/22/14.
 */
public class CustomDispatcher extends Dispatcher {

	protected static Context mContext;

	public CustomDispatcher(Instrumentation instrumentation) {
		mContext = instrumentation.getContext();
	}

	@Override
	public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

		if (request.getPath().contains("hint/es/v3/ac/en_US")) {
			return makeResponse("MockResponses/hint/es/v3/ac/en_US/suggestion.json");
		}

		else if (request.getPath().contains("/m/api/hotel/search")) {
			return makeResponse("MockResponses/m/api/hotel/search/happy.json");
		}

		else if (request.getPath().contains("/m/api/hotel/offers")) {
			return makeResponse("MockResponses/m/api/hotel/offers/happy_path.json");
		}

		else if (request.getPath().contains("/m/api/hotel/product")) {
			return makeResponse("MockResponses/m/api/hotel/product/happy_path_0.json");
		}

		else if (request.getPath().contains("/m/api/hotel/trip/create")) {
			return makeResponse("MockResponses/m/api/hotel/trip/create/happy_path_0.json");
		}

		else if (request.getPath().contains("/m/api/hotel/trip/checkout")) {
			return makeResponse("MockResponses/m/api/hotel/trip/checkout/happy_path_0.json");
		}

		else if (request.getPath().contains("/api/flight/search")) {
			String flightFileName;

			//set filename for one-way and two-way flights
			if (request.getUtf8Body().contains("returnDate")) {
				flightFileName = "happy_roundtrip";
			}
			else {
				flightFileName = "happy_oneway";
			}
			return makeResponse("MockResponses/api/flight/search/" + flightFileName + ".json");
		}

		else if (request.getPath().contains("/api/flight/trip/create")) {
			String productKey = getValueOf("productKey", request.getUtf8Body());
			return makeResponse("MockResponses/api/flight/trip/create/" + productKey + ".json");
		}

		else if (request.getPath().contains("/api/flight/checkout")) {
			String productKey = getValueOf("productKey", request.getUtf8Body());
			return makeResponse("MockResponses/api/flight/checkout/" + productKey + ".json");
		}
		return new MockResponse().setResponseCode(404);
	}


	private String getValueOf(String key, String body) {
		Map<String, String> map = null;
		try {
			map = parseParameters(body);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return map.get(key);
	}

	public static Map<String, String> parseParameters(String body) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = body.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	public static MockResponse makeResponse(String filePath) {
		MockResponse resp = new MockResponse();
		try {
			String body = getResponse(filePath);
			resp.setBody(body);
			resp.setHeader("Content-Type", "application/json");
		}
		catch (Exception e) {
			resp.setResponseCode(404);
		}
		return resp;
	}

	//read the json responses from tests/assets/
	static String getResponse(String filename) throws IOException {
		InputStream inputStream = mContext.getResources().getAssets().open(filename);
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

