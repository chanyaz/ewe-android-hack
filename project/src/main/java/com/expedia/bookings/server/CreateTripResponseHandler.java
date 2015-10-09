package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ValidPayment;
import com.expedia.bookings.enums.MerchandiseSpam;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;

public class CreateTripResponseHandler extends JsonResponseHandler<CreateTripResponse> {

	private Context mContext;
	private HotelSearchParams mSearchParams;
	private Property mProperty;

	public CreateTripResponseHandler(Context context, HotelSearchParams searchParams, Property property) {
		mContext = context;
		mSearchParams = searchParams;
		mProperty = property;
	}

	@Override
	public CreateTripResponse handleJson(JSONObject response) {
		CreateTripResponse createTripResponse = new CreateTripResponse();
		try {
			List<ServerError> errors = ParserUtils.parseErrors(ServerError.ApiMethod.CREATE_TRIP, response);
			List<ServerError> warnings = ParserUtils.parseWarnings(ServerError.ApiMethod.CREATE_TRIP, response);
			List<ServerError> allErrors = new ArrayList<ServerError>();
			if (errors != null) {
				allErrors.addAll(errors);
			}
			if (warnings != null) {
				allErrors.addAll(warnings);
			}
			createTripResponse.addErrors(allErrors);

			if (createTripResponse.hasErrors()) {
				return createTripResponse;
			}

			createTripResponse.setTripId(response.optString("tripId", null));
			createTripResponse.setUserId(response.optString("userId", null));
			createTripResponse.setTealeafId(response.optString("tealeafTransactionId", null));
			String merchandiseSpamString = response.optString("guestUserPromoEmailOptInStatus", null);
			if (Strings.isNotEmpty(merchandiseSpamString)) {
				createTripResponse.setMerchandiseSpam(MerchandiseSpam.valueOf(merchandiseSpamString));
			}

			JSONObject newHotelResponse = response.getJSONObject("newHotelProductResponse");
			JSONObject originalHotelResponse = response.getJSONObject("originalHotelProductResponse");

			int newNumberOfNights = newHotelResponse.getInt("numberOfNights");

			HotelOffersResponseHandler availHandler = new HotelOffersResponseHandler(mContext, mSearchParams);
			Rate newRate = availHandler.parseJsonHotelOffer(newHotelResponse.getJSONObject("hotelRoomResponse"), newNumberOfNights);

			Rate originalRate = null;
			// "originalHotelProductResponse" is empty if we don't have a price change.
			if (originalHotelResponse.length() != 0) {
				int origNumberOfNights = originalHotelResponse.getInt("numberOfNights");
				originalRate = availHandler.parseJsonHotelOffer(originalHotelResponse.getJSONObject("hotelRoomResponse"), origNumberOfNights);
			}

			createTripResponse.setNewRate(newRate);
			createTripResponse.setOriginalRate(originalRate);
			createTripResponse.setSupplierType(newHotelResponse.getString("supplierType"));

			List<ValidPayment> payments = CreateItineraryResponseHandler.parseValidPayments(response);
			createTripResponse.setValidPayments(payments);

			// Air Attach response - optional
			JSONObject airAttachHotelResponse = response.optJSONObject("airAttachedProductResponse");
			if (airAttachHotelResponse != null && airAttachHotelResponse.has("hotelRoomResponse")) {
				int nights = airAttachHotelResponse.getInt("numberOfNights");
				Rate airAttachRate = availHandler.parseJsonHotelOffer(airAttachHotelResponse.getJSONObject("hotelRoomResponse"), nights);
				airAttachRate.setAirAttached(true);
				createTripResponse.setAirAttachRate(airAttachRate);
			}

			//Hotel loyalty rewards
			String points = availHandler.parseRewardPoints(response);
			createTripResponse.setRewardsPoints(points);
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON CreateTrip response.", e);
			return null;
		}

		return createTripResponse;
	}
}
