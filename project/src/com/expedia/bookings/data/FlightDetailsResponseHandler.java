package com.expedia.bookings.data;

import org.json.JSONObject;

import com.expedia.bookings.server.ParserUtils;
import com.mobiata.android.net.JsonResponseHandler;

public class FlightDetailsResponseHandler extends JsonResponseHandler<FlightDetailsResponse> {

	@Override
	public FlightDetailsResponse handleJson(JSONObject response) {
		if (response.has("error")) {
			// TODO: BETTER ERROR HANDLING
			return null;
		}

		FlightDetailsResponse details = new FlightDetailsResponse();

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
