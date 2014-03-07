package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;

public class HotelProductResponseHandler extends JsonResponseHandler<HotelProductResponse> {

	private Context mContext;
	private HotelSearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;

	public HotelProductResponseHandler(Context context, HotelSearchParams searchParams, Property property, Rate rate) {
		mContext = context;
		mSearchParams = searchParams;
		mProperty = property;
		mRate = rate;
	}

	@Override
	public HotelProductResponse handleJson(JSONObject response) {
		HotelProductResponse prodResponse = new HotelProductResponse(mRate.getRateKey());
		try {
			prodResponse.addErrors(ParserUtils.parseErrors(mContext, ApiMethod.HOTEL_PRODUCT, response));

			if (!prodResponse.isSuccess()) {
				return prodResponse;
			}

			JSONObject jsonRate = response.getJSONObject("hotelRoomResponse");
			HotelOffersResponseHandler availHandler = new HotelOffersResponseHandler(mContext, mSearchParams, mProperty);
			Rate rate = availHandler.parseJsonHotelOffer(jsonRate, mSearchParams.getStayDuration(), null);
			prodResponse.setRate(rate);
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON hotel product response.", e);
			return null;
		}

		return prodResponse;
	}
}
