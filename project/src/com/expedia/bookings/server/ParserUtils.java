package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;

public class ParserUtils {

	public static Money createMoney(String amount, String currencyCode) {
		if (amount == null || amount.length() == 0) {
			return null;
		}

		return createMoney(Double.parseDouble(amount), currencyCode);
	}

	public static Money createMoney(double amount, String currencyCode) {
		if (currencyCode == null || currencyCode.length() == 0) {
			return null;
		}

		Money money = new Money();
		money.setAmount(amount);
		money.setCurrency(currencyCode);
		return money;
	}

	public static boolean parseServerErrors(Context context, ApiMethod apiMethod, JSONObject jsonResponse,
			Response response) throws JSONException {

		if (jsonResponse.has("errors")) {
			JSONArray errors = jsonResponse.getJSONArray("errors");
			int len = errors.length();
			for (int a = 0; a < len; a++) {
				JSONObject error = errors.getJSONObject(a);
				ServerError serverError = new ServerError(apiMethod);
				serverError.setMessage(error.getString("msg"));
				serverError.setCode(error.getString("code"));
				response.addError(serverError);
			}
			return true;
		}

		return false;
	}

	/**
	 * Parses an error if found; otherwise returns null
	 */
	public static ServerError parseEanError(Context context, ApiMethod apiMethod, JSONObject response)
			throws JSONException {

		if (response.has("EanWsError")) {
			JSONObject error = response.getJSONObject("EanWsError");
			ServerError serverError = new ServerError(apiMethod);
			serverError.setVerboseMessage(error.optString("verboseMessage", null));
			serverError.setPresentationMessage(error.optString("presentationMessage", null));

			// For backwards compatibility with old versions of HP
			String errMsg = serverError.getVerboseMessage();
			if (errMsg != null && ServerError.ERRORS.containsKey(errMsg)) {
				errMsg = context.getString(ServerError.ERRORS.get(errMsg));
			}
			serverError.setMessage(errMsg);

			serverError.setCode("-1");
			serverError.setCategory(error.optString("category", null));
			serverError.setHandling(error.optString("handling", null));
			return serverError;
		}

		return null;
	}

	public static List<ServerError> parseErrors(Context context, ApiMethod apiMethod, JSONObject response)
			throws JSONException {

		if (response.has("errors")) {
			List<ServerError> errors = new ArrayList<ServerError>();

			JSONArray arr = response.getJSONArray("errors");
			for (int a = 0; a < arr.length(); a++) {
				JSONObject error = arr.getJSONObject(a);
				ServerError serverError = new ServerError(apiMethod);
				serverError.setCode(error.getString("errorCode"));
				serverError.setDiagnosticFullText(error.optString("diagnosticFullText"));

				JSONObject info = error.getJSONObject("errorInfo");
				serverError.setMessage(info.optString("summary", null));
				serverError.addExtra("field", info.optString("field", null));
				serverError.addExtra("itineraryBooked", info.optString("itineraryBooked", null));
				serverError.addExtra("emailSent", info.optString("emailSent", null));

				errors.add(serverError);
			}

			return errors;
		} else if (response.has("detailedStatus")) {
			List<ServerError> errors = new ArrayList<ServerError>();

			// This is for when we fail a SignIn due to bad credentials
			// We don't get a MobileError returned to us, we get these fields
			String status = response.optString("detailedStatus", null);
			if (! status.equals("Success")) {
				ServerError fakeError = new ServerError(apiMethod);
				fakeError.setCode("SIMULATED");
				fakeError.setMessage(response.optString("detailedStatusMsg", null));

				errors.add(fakeError);
			}

			return errors;
		}

		return null;
	}
}
