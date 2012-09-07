package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class FlightCheckoutResponseHandler extends JsonResponseHandler<FlightCheckoutResponse> {

	private Context mContext;

	public FlightCheckoutResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public FlightCheckoutResponse handleJson(JSONObject response) {
		FlightCheckoutResponse checkoutResponse = new FlightCheckoutResponse();

		try {
			// Check for errors, return if found
			checkoutResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.FLIGHT_CHECKOUT, response));
			if (!checkoutResponse.isSuccess()) {
				return checkoutResponse;
			}

			checkoutResponse.setItineraryNumber(response.optString("itineraryNumber"));
			checkoutResponse.setTravelRecordLocator(response.optString("travelRecordLocator"));
			checkoutResponse.setTripId(response.optString("tripId"));

			String currencyCode = response.optString("currencyCode");
			checkoutResponse.setTotalCharges(ParserUtils.createMoney(response.optString("totalCharges"), currencyCode));
		}
		catch (JSONException e) {
			Log.e("Could not parse flight checkout response", e);
			return null;
		}

		return checkoutResponse;
	}
}
