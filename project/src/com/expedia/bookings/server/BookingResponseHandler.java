package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class BookingResponseHandler extends JsonResponseHandler<BookingResponse> {

	private Context mContext;

	public BookingResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public BookingResponse handleJson(JSONObject response) {
		BookingResponse bookingResponse = new BookingResponse();
		try {
			// Check for HP server errors
			if (ParserUtils.parseServerErrors(mContext, response, bookingResponse)) {
				return bookingResponse;
			}

			JSONObject jsonResponse = response.getJSONObject("body").getJSONObject("HotelRoomReservationResponse");

			String sessionId = jsonResponse.optString("customerSessionId", null);
			if (sessionId != null && sessionId.length() > 0) {
				bookingResponse.setSession(new Session(sessionId));
			}

			// Check for errors, return if found
			ServerError serverError = ParserUtils.parseEanError(mContext, jsonResponse);
			if (serverError != null) {
				bookingResponse.addError(serverError);
				return bookingResponse;
			}

			bookingResponse.setConfNumber(jsonResponse.getString("confirmationNumbers"));
			bookingResponse.setItineraryId(jsonResponse.getString("itineraryId"));
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON reservation response.", e);
			return null;
		}
		return bookingResponse;

	}
}
