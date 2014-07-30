package com.expedia.bookings.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
			Map<String, String> params = parsePostParams(request);
			String filename = params.containsKey("returnDate") ? "happy_roundtrip" : "happy_oneway";
			return makeResponse("MockResponses/api/flight/search/" + filename + ".json");
		}

		else if (request.getPath().contains("/api/flight/trip/create")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/api/flight/trip/create/" + params.get("productKey") + ".json");
		}

		else if (request.getPath().contains("/api/flight/checkout")) {
			Map<String, String> params = parsePostParams(request);
			return makeResponse("MockResponses/api/flight/checkout/" + params.get("productKey") + ".json");
		}

		return new MockResponse().setResponseCode(404);
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

	public MockResponse makeResponse(String filePath) {
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

