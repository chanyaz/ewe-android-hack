package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.RewardsInfo;
import com.expedia.bookings.data.Rule;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.utils.CurrencyUtils;
import com.mobiata.android.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateItineraryResponseHandler extends JsonResponseHandler<CreateItineraryResponse> {

	private final Context mContext;

	public CreateItineraryResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public CreateItineraryResponse handleJson(JSONObject response) {
		CreateItineraryResponse createItinerary = new CreateItineraryResponse();

		ParserUtils.logActivityId(response);

		try {
			createItinerary.addErrors(ParserUtils.parseErrors(ApiMethod.CREATE_FLIGHT_ITINERARY, response));
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
		if (itineraryJson == null) {
			Log.e("CreateItineraryResponseHandler: Did not get trip info from server");
			return null;
		}

		Itinerary itinerary = new Itinerary();
		itinerary.setItineraryNumber(itineraryJson.optString("itineraryNumber"));
		itinerary.setTravelRecordLocator(itineraryJson.optString("travelRecordLocator"));
		itinerary.setTripId(itineraryJson.optString("tripId"));
		itinerary.setTealeafId(response.optString("tealeafTransactionId"));
		createItinerary.setItinerary(itinerary);

		// Parse details
		JSONObject detailsJson = response.optJSONObject("details");
		FlightTrip offer = FlightSearchResponseHandler.parseTrip(detailsJson.optJSONObject("offer"));
		if (detailsJson.has("priceChangeAmount")) {
			Money priceChangeAmount = ParserUtils.createMoney(detailsJson.optString("priceChangeAmount"), offer
					.getBaseFare().getCurrency());
			offer.setPriceChangeAmount(priceChangeAmount);
		}

		JSONObject totalPrice = response.optJSONObject("totalPrice");
		Money money = ParserUtils.createMoney(totalPrice.optString("amount"), totalPrice.optString("currencyCode"));
		money.formattedPrice = totalPrice.optString("formattedPrice");
		offer.setTotalPrice(money);

		createItinerary.setOffer(offer);

		boolean isSplitTicket = detailsJson.optBoolean("isSplitTicket", false);
		createItinerary.setIsSplitTicket(isSplitTicket);

		//Rewards points
		String rewardsPoints = response.optString("rewardsPoints");
		if (!TextUtils.isEmpty(rewardsPoints)) {
			offer.setRewardsPoints(rewardsPoints);
		}

		JSONObject rewards = response.optJSONObject("rewards");
		if (rewards != null) {
			RewardsInfo rewardsInfo = new RewardsInfo();
			String totalPointsToEarn = rewards.optString("totalPointsToEarn");
			if (!TextUtils.isEmpty(totalPointsToEarn)) {
				rewardsInfo.setTotalPointsToEarn(Float.parseFloat(totalPointsToEarn));
			}
			JSONObject totalAmountToEarn = rewards.optJSONObject("totalAmountToEarn");
			if (totalAmountToEarn != null) {
				String amount = totalAmountToEarn.optString("amount");
				String currencyCode = totalAmountToEarn.optString("currencyCode");
				rewardsInfo.setTotalAmountToEarn(ParserUtils.createMoney(amount, currencyCode));
			}
			offer.setRewards(rewardsInfo);
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

		List<ValidPayment> payments = parseValidPayments(response);
		createItinerary.setValidPayments(payments);

		// Link the offer/itinerary
		offer.setItineraryNumber(itinerary.getItineraryNumber());
		itinerary.addProductKey(offer.getProductKey());

		return createItinerary;
	}

	public static List<ValidPayment> parseValidPayments(JSONObject obj) {
		List<ValidPayment> payments = new ArrayList<>();

		JSONArray paymentsJson = obj.optJSONArray("validFormsOfPayment");
		if (paymentsJson != null) {
			for (int i = 0; i < paymentsJson.length(); i++) {
				JSONObject paymentJson = paymentsJson.optJSONObject(i);
				ValidPayment validPayment = new ValidPayment();

				final String name = paymentJson.optString("name");
				if (!TextUtils.isEmpty(name)) {
					validPayment.setPaymentType(CurrencyUtils.parsePaymentType(name));
				}

				final String currencyCode = paymentJson.optString("feeCurrencyCode");
				final String fee = paymentJson.optString("fee");
				if (!TextUtils.isEmpty(currencyCode) && !TextUtils.isEmpty(fee)) {
					validPayment.setFee(ParserUtils.createMoney(fee, currencyCode));
				}

				payments.add(validPayment);
			}
		}

		return payments;
	}
}
