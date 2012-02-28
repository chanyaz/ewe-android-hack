package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
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
			// Check for errors
			bookingResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.CHECKOUT, response));

			if (bookingResponse.isSuccess() || bookingResponse.succeededWithErrors()) {
				bookingResponse.setConfNumber(response.optString("hotelConfirmationNumber", null));
				bookingResponse.setItineraryId(response.optString("itineraryNumber", null));
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON reservation response.", e);
			return null;
		}
		return bookingResponse;
	}
}
