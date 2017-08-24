package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;

public class BookingResponseHandler extends JsonResponseHandler<HotelBookingResponse> {

	private final Context mContext;

	public BookingResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public HotelBookingResponse handleJson(JSONObject response) {
		HotelBookingResponse bookingResponse = new HotelBookingResponse();
		try {
			// Check for errors
			bookingResponse.addErrors(ParserUtils.parseErrors(ApiMethod.CHECKOUT, response));

			if (bookingResponse.isSuccess() || bookingResponse.succeededWithErrors()) {

				JSONObject checkoutJSON = response.getJSONObject("checkoutResponse");
				JSONObject bookingJSON = checkoutJSON.getJSONObject("bookingResponse");

				bookingResponse.setHotelConfNumber(bookingJSON.optString("hotelConfirmationNumber", null));
				bookingResponse.setItineraryId(bookingJSON.optString("itineraryNumber", null));
				bookingResponse.setPhoneNumber(bookingJSON.optString("hotelNumber", null));

				bookingResponse.setOrderNumber(response.optString("orderId", null));
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON reservation response.", e);
			return null;
		}
		return bookingResponse;
	}
}
