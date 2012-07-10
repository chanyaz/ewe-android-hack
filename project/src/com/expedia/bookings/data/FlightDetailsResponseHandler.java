package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.server.FlightSearchResponseHandler;
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

		if (response.has("offer")) {
			FlightTrip updatedTrip = FlightSearchResponseHandler.parseTrip(response.optJSONObject("offer"));
			details.setOffer(updatedTrip);

			if (response.has("oldOffer")) {
				FlightTrip oldTrip = FlightSearchResponseHandler.parseTrip(response.optJSONObject("oldOffer"));
				details.setOldOffer(oldTrip);
			}

			if (response.has("priceChangeAmount")) {
				Money priceChangeAmount = ParserUtils.createMoney(response.optDouble("priceChangeAmount"), updatedTrip
						.getBaseFare().getCurrency());
				details.setPriceChangeAmount(priceChangeAmount);
			}
		}

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
