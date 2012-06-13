package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.server.ParserUtils;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class FlightDetailsResponseHandler extends JsonResponseHandler<FlightDetailsResponse> {

	private Context mContext;

	public FlightDetailsResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public FlightDetailsResponse handleJson(JSONObject response) {
		FlightDetailsResponse details = new FlightDetailsResponse();

		try {
			details.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.FLIGHT_DETAILS, response));
			if (!details.isSuccess()) {
				return details;
			}
		}
		catch (JSONException e) {
			Log.e("Error parsing flight details response JSON", e);
			return null;
		}

		// TODO: It is currently assumed that there is a single currency code for all things, but
		// need to make sure this assumption is ultimately correct with Rob Meyer.
		String currencyCode = response.optString("currency", "USD");

		if (response.has("flightRules")) {
			details.setIsChangeAllowed(response.optBoolean("isChangeAllowed"));
			details.setIsEnrouteChangeAllowed(response.optBoolean("isEnrouteChangeAllowed"));
			details.setIsEnrouteRefundAllowed(response.optBoolean("isEnrouteRefundAllowed"));
			details.setIsRefundable(response.optBoolean("isRefundable"));
			details.setChangePenaltyAmount(ParserUtils.createMoney(response.optDouble("changePenaltyAmount"),
					currencyCode));
		}

		return details;
	}
}
