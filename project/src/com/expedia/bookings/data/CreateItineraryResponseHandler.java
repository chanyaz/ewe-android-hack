package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.server.FlightSearchResponseHandler;
import com.expedia.bookings.server.ParserUtils;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class CreateItineraryResponseHandler extends JsonResponseHandler<CreateItineraryResponse> {

	private Context mContext;

	public CreateItineraryResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public CreateItineraryResponse handleJson(JSONObject response) {
		CreateItineraryResponse createItinerary = new CreateItineraryResponse();

		try {
			createItinerary.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.CREATE_FLIGHT_ITINERARY, response));
			if (!createItinerary.isSuccess()) {
				return createItinerary;
			}
		}
		catch (JSONException e) {
			Log.e("Error parsing create flight itinerary response JSON", e);
			return null;
		}

		// Parse itinerary
		JSONObject itineraryJson = response.optJSONObject("newTrip");
		Itinerary itinerary = new Itinerary();
		itinerary.setItineraryNumber(itineraryJson.optString("itineraryNumber"));
		itinerary.setTravelRecordLocator(itineraryJson.optString("travelRecordLocator"));
		itinerary.setTripId(itineraryJson.optString("tripId"));
		createItinerary.setItinerary(itinerary);

		// Parse details
		JSONObject detailsJson = response.optJSONObject("details");
		FlightTrip offer = FlightSearchResponseHandler.parseTrip(detailsJson.optJSONObject("offer"));
		if (detailsJson.has("priceChangeAmount")) {
			Money priceChangeAmount = ParserUtils.createMoney(detailsJson.optDouble("priceChangeAmount"), offer
					.getBaseFare().getCurrency());
			offer.setPriceChangeAmount(priceChangeAmount);
		}
		createItinerary.setOffer(offer);

		// Link the offer/itinerary
		offer.setItineraryNumber(itinerary.getItineraryNumber());
		itinerary.addProductKey(offer.getProductKey());

		return createItinerary;
	}
}
