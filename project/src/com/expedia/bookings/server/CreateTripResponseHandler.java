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
			List<ServerError> errors = ParserUtils.parseErrors(mContext, ServerError.ApiMethod.CREATE_TRIP, response);
			List<ServerError> warnings = ParserUtils.parseWarnings(mContext, ServerError.ApiMethod.CREATE_TRIP, response);
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

			JSONObject newHotelResponse = response.getJSONObject("newHotelProductResponse");
			int numberOfNights = newHotelResponse.getInt("numberOfNights");

			HotelOffersResponseHandler availHandler = new HotelOffersResponseHandler(mContext, mSearchParams, mProperty);
			Rate newRate = availHandler.parseJsonHotelOffer(newHotelResponse.getJSONObject("hotelRoomResponse"), numberOfNights, null);

			List<ValidPayment> payments = CreateItineraryResponseHandler.parseValidPayments(response);
			newRate.addValidPayments(payments);

			createTripResponse.setNewRate(newRate);
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON CreateTrip response.", e);
			return null;
		}

		return createTripResponse;
	}
}
