package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.ServerError;

import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class CreateTripResponseHandler extends JsonResponseHandler<CreateTripResponse> {

	private Context mContext;
	private SearchParams mSearchParams;
	private Property mProperty;

	public CreateTripResponseHandler(Context context, SearchParams searchParams, Property property) {
		mContext = context;
		mSearchParams = searchParams;
		mProperty = property;
	}

	@Override
	public CreateTripResponse handleJson(JSONObject response) {
		CreateTripResponse createTripResponse = new CreateTripResponse();
		try {
			createTripResponse.addErrors(ParserUtils.parseErrors(mContext, ServerError.ApiMethod.CREATE_TRIP, response));

			createTripResponse.setTripId(response.optString("tripId", null));
			createTripResponse.setUserId(response.optString("userId", null));

			JSONObject newHotelResponse = response.getJSONObject("newHotelProductResponse");
			int numberOfNights = newHotelResponse.getInt("numberOfNights");

			AvailabilityResponseHandler availHandler = new AvailabilityResponseHandler(mContext, mSearchParams, mProperty);
			Rate newRate = availHandler.parseJsonHotelOffer(newHotelResponse.getJSONObject("hotelRoomResponse"), numberOfNights, null);

			createTripResponse.setNewRate(newRate);
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON CreateTrip response.", e);
			return null;
		}

		return createTripResponse;
	}
}
