package com.expedia.bookings.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.utils.Images;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mobiata.android.Log;

public class ParserUtils {

	public static Money createMoney(String amount, String currencyCode) {
		if (amount == null || amount.length() == 0) {
			return null;
		}

		Money money = new Money();
		money.setAmount(amount);
		money.setCurrency(currencyCode);
		return money;
	}

	/**
	 * Often times when parsing URLs are not prefixed with the Expedia URL,
	 * which is good but we need to fix it.
	 */
	public static HotelMedia parseUrl(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}

		if (!TextUtils.isEmpty(url) && !url.startsWith("http://")) {
			url = Images.getMediaHost() + url;
		}

		return new HotelMedia(url);
	}

	/**
	 * activityId is useful for debugging API issues
	 */
	public static void logActivityId(JSONObject jsonResponse) {
		if (jsonResponse == null) {
			Log.d("activityId: Null response, thus no activityId.");
		}
		else {
			logActivityId(jsonResponse.optString("activityId", null));
		}
	}

	public static void logActivityId(String activityId) {
		if (TextUtils.isEmpty(activityId)) {
			Log.d("activityId: Response had no activityId");
		}
		else {
			Log.d("activityId: " + activityId);
		}
	}

	public static List<ServerError> parseErrors(ApiMethod apiMethod, JSONObject response)
		throws JSONException {

		if (response.has("errors")) {
			List<ServerError> errors = new ArrayList<ServerError>();

			JSONArray arr = response.getJSONArray("errors");
			for (int a = 0; a < arr.length(); a++) {
				JSONObject error = arr.getJSONObject(a);
				errors.add(getServerError(error, apiMethod));
			}

			return errors;
		}
		else if (response.has("detailedStatus")) {
			List<ServerError> errors = new ArrayList<ServerError>();

			// This is for when we fail a SignIn due to bad credentials
			// We don't get a MobileError returned to us, we get these fields
			String status = response.optString("detailedStatus", null);
			if (!status.equals("Success")) {
				ServerError error = new ServerError(apiMethod);
				error.setCode("NOT_AUTHENTICATED");
				error.setMessage(response.optString("detailedStatusMsg", null));

				errors.add(error);
			}

			return errors;
		}
		else if (response.has("errorCode")) {
			List<ServerError> errors = new ArrayList<ServerError>();

			ServerError error = new ServerError(apiMethod);
			int code = response.getInt("errorCode");
			switch (code) {
			case 400:
				error.setCode("INVALID_INPUT");
				break;
			case 500:
				// Server error
				error.setCode("UNKNOWN_ERROR");
				break;
			default:
				error.setCode("UNKNOWN_ERROR");
			}

			error.setMessage(response.optString("message", null));

			errors.add(error);

			return errors;
		}
		else if (response.has("responseCode") && TextUtils.equals("Failure", response.getString("responseCode"))) {
			List<ServerError> errors = new ArrayList<ServerError>();
			if (response.has("errorMessages")) {
				JSONArray arr = response.getJSONArray("errorMessages");
				for (int a = 0; a < arr.length(); a++) {
					String msg = arr.getString(a);
					ServerError error = new ServerError(apiMethod);
					error.setMessage(msg);
					errors.add(error);
				}
			}
			else {
				errors.add(new ServerError(apiMethod));
			}

			return errors;
		}

		return null;
	}

	public static List<ServerError> parseWarnings(ApiMethod apiMethod, JSONObject response)
		throws JSONException {

		if (response.has("warnings")) {
			List<ServerError> errors = new ArrayList<ServerError>();

			JSONArray arr = response.getJSONArray("warnings");
			for (int a = 0; a < arr.length(); a++) {
				JSONObject error = arr.getJSONObject(a);
				errors.add(getServerError(error, apiMethod));
			}

			return errors;
		}

		return null;
	}

	private static ServerError getServerError(JSONObject error, ApiMethod apiMethod)
		throws JSONException {

		ServerError serverError = new ServerError(apiMethod);
		final String code = error.getString("errorCode");
		if ("400".equals(code)) {
			serverError.setCode("INVALID_INPUT");
		}
		else if ("403".equals(code)) {
			serverError.setCode("NOT_AUTHENTICATED");
		}
		else {
			serverError.setCode(code);
		}
		serverError.setDiagnosticFullText(error.optString("diagnosticFullText"));

		if (error.has("errorInfo")) {
			JSONObject info = error.getJSONObject("errorInfo");
			serverError.setMessage(info.optString("summary", null));
			serverError.addExtra("field", info.optString("field", null));
			serverError.addExtra("itineraryBooked", info.optString("itineraryBooked", null));
			serverError.addExtra("emailSent", info.optString("emailSent", null));
			serverError.setCouponErrorType(info.optString("couponErrorType", null));
		}

		if (error.has("message")) {
			serverError.setMessage(error.getString("message"));
		}

		return serverError;
	}

	public static void readServerErrors(JsonReader reader, Response response, ApiMethod apiMethod) throws IOException {
		reader.beginArray();
		while (!reader.peek().equals(JsonToken.END_ARRAY)) {
			readServerError(reader, response, apiMethod);
		}
		reader.endArray();
	}

	public static void readServerError(JsonReader reader, Response response, ApiMethod apiMethod) throws IOException {
		ServerError serverError = new ServerError(apiMethod);

		if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
			throw new IOException("Expected readServerError() to start with an Object, started with "
				+ reader.peek() + " instead.");
		}

		// TODO: FIGURE OUT MESSAGE TO DISPLAY TO USER ON ERROR

		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			String name = reader.nextName();

			if (name.equals("errorCode")) {
				serverError.setCode(reader.nextString());
			}
			else if (name.equals("errorInfo")) {
				if (!reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
					throw new IOException("Expected errorInfo to start with an Object, started with "
						+ reader.peek() + " instead.");
				}
				reader.beginObject();
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					String name2 = reader.nextName();
					if (name2.equals("field")) {
						serverError.addExtra("field", reader.nextString());
					}
					else if (name2.equals("summary")) {
						serverError.setMessage(reader.nextString());
					}
					else {
						reader.skipValue();
					}
				}
				reader.endObject();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		response.addError(serverError);
	}
}
