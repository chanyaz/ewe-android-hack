package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
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

		ParserUtils.logActivityId(response);

		try {
			// Check for errors, return if found
			checkoutResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.FLIGHT_CHECKOUT, response));
			if (!checkoutResponse.isSuccess()) {
				// Some errors require special parsing
				JSONObject detailResponse = response.optJSONObject("flightDetailResponse");
				if (detailResponse != null && detailResponse.has("offer")) {
					FlightTrip newOffer = FlightSearchResponseHandler.parseTrip(detailResponse.optJSONObject("offer"));
					checkoutResponse.setNewOffer(newOffer);
				}
			}

			// Continue parsing other fields even if we got an error.  This is
			// important when we get a TRIP_ALREADY_BOOKED error.
			checkoutResponse.setOrderId(response.optString("orderId", null));

			String currencyCode = response.optString("currencyCode");
			if (!TextUtils.isEmpty(currencyCode) && response.has("totalCharges")) {
				checkoutResponse.setTotalCharges(ParserUtils.createMoney(response.optString("totalCharges"),
						currencyCode));
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse flight checkout response", e);
			return null;
		}

		return checkoutResponse;
	}
}
