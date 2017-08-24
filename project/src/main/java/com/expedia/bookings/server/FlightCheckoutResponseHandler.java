package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;

public class FlightCheckoutResponseHandler extends JsonResponseHandler<FlightCheckoutResponse> {

	private final Context mContext;

	public FlightCheckoutResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public FlightCheckoutResponse handleJson(JSONObject response) {
		FlightCheckoutResponse checkoutResponse = new FlightCheckoutResponse();

		ParserUtils.logActivityId(response);

		try {
			// Check for errors, return if found
			checkoutResponse.addErrors(ParserUtils.parseErrors(ApiMethod.FLIGHT_CHECKOUT, response));
			if (!checkoutResponse.isSuccess()) {
				// Some errors require special parsing
				JSONObject detailResponse = response.optJSONObject("flightDetailResponse");
				if (detailResponse != null && detailResponse.has("offer")) {
					FlightTrip newOffer = FlightSearchResponseHandler.parseTrip(detailResponse.optJSONObject("offer"));

					// Online booking fees parsing
					String obFeeTotalAmount = detailResponse.optString("obFeeTotalAmount", null);
					if (!TextUtils.isEmpty(obFeeTotalAmount)) {
						String currency = newOffer.getBaseFare().getCurrency();
						if (Strings.isEmpty(currency)) {
							String countryCode = PointOfSale.getPointOfSale().getThreeLetterCountryCode();
							currency = CurrencyUtils.currencyForLocale(countryCode);
						}
						Money obFees = ParserUtils.createMoney(obFeeTotalAmount, currency);
						if (!obFees.isZero()) {
							newOffer.setOnlineBookingFeesAmount(obFees);
						}
					}

					checkoutResponse.setNewOffer(newOffer);
				}
			}
			else {
				// Region id for cross-sell
				JSONObject aggregatedResponse = response.optJSONObject("flightAggregatedResponse");
				JSONArray detailArray = aggregatedResponse.optJSONArray("flightsDetailResponse");
				if (detailArray != null && detailArray.length() > 0) {
					JSONObject detailObject = detailArray.optJSONObject(0);
					checkoutResponse.setDestinationRegionId(detailObject.optString("destinationRegionId"));
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

			// Air Attach - Booking a flight tends to qualify the user for a discounted hotel room
			JSONObject airAttachJson = response.optJSONObject("mobileAirAttachQualifier");
			if (airAttachJson != null) {
				AirAttach airAttach = new AirAttach(airAttachJson);

				if (airAttach.isAirAttachQualified()) {
					if (Db.getTripBucket().setAirAttach(airAttach)) {
						Db.saveTripBucket(mContext);
					}
				}
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse flight checkout response", e);
			return null;
		}

		return checkoutResponse;
	}
}
