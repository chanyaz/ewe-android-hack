package com.expedia.bookings.server;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rule;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.utils.CurrencyUtils;
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

		ParserUtils.logActivityId(response);

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
			Money priceChangeAmount = ParserUtils.createMoney(detailsJson.optString("priceChangeAmount"), offer
					.getBaseFare().getCurrency());
			offer.setPriceChangeAmount(priceChangeAmount);
		}
		createItinerary.setOffer(offer);

		//Rewards points
		String rewardsPoints = response.optString("rewardsPoints");
		if (!TextUtils.isEmpty(rewardsPoints)) {
			offer.setRewardsPoints(rewardsPoints);
		}

		// Parse rules
		JSONObject rulesJson = response.optJSONObject("rules");
		if (rulesJson != null) {
			JSONObject rulesTextMap = rulesJson.optJSONObject("RuleToTextMap");
			JSONObject rulesUrlMap = rulesJson.optJSONObject("RuleToUrlMap");

			@SuppressWarnings("unchecked")
			Iterator<String> keys = rulesTextMap.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Rule rule = new Rule();
				rule.setName(key);
				rule.setText(rulesTextMap.optString(key));
				rule.setUrl(rulesUrlMap.optString(key, null));
				offer.addRule(rule);
			}
		}

		// Parse the validFormsOfPayment
		JSONArray paymentsJson = response.optJSONArray("validFormsOfPayment");
		if (paymentsJson != null) {
			for (int i = 0; i < paymentsJson.length(); i++) {
				JSONObject paymentJson = paymentsJson.optJSONObject(i);
				ValidPayment validPayment = new ValidPayment();

				String name = paymentJson.optString("name");
				if (!TextUtils.isEmpty(name)) {
					validPayment.setCreditCardType(CurrencyUtils.parseCardType(name));
				}
				String currencyCode = paymentJson.optString("feeCurrencyCode");
				validPayment.setFee(ParserUtils.createMoney(paymentJson.optString("fee"), currencyCode));

				offer.addValidPayment(validPayment);
			}
		}

		// Link the offer/itinerary
		offer.setItineraryNumber(itinerary.getItineraryNumber());
		itinerary.addProductKey(offer.getProductKey());

		return createItinerary;
	}
}
