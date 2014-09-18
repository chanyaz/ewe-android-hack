package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class HotelBookingResponse extends Response implements JSONable {

	private String mHotelConfNumber;

	// For Expedia
	private String mItineraryId;
	private String mOrderNumber;

	private String mPhoneNumber;

	// Not from Response, but stored here for code organization's sake
	private Property mProperty;

	public boolean succeededWithErrors() {
		if (!hasErrors()) {
			return false;
		}

		for (ServerError error : getErrors()) {
			if (error.succeededWithErrors()) {
				return true;
			}
		}

		return false;
	}

	public String getHotelConfNumber() {
		return mHotelConfNumber;
	}

	public void setHotelConfNumber(String hotelConfNumber) {
		this.mHotelConfNumber = hotelConfNumber;
	}

	public String getItineraryId() {
		return mItineraryId;
	}

	public void setItineraryId(String itineraryId) {
		this.mItineraryId = itineraryId;
	}

	public String getOrderNumber() {
		return mOrderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		mOrderNumber = orderNumber;
	}

	public void setPhoneNumber(String number) {
		mPhoneNumber = number;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setProperty(Property property) {
		mProperty = property;
	}

	public Property getProperty() {
		return mProperty;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = super.toJson();
			obj.putOpt("hotelConfNumber", mHotelConfNumber);
			obj.putOpt("itineraryId", mItineraryId);
			obj.putOpt("orderNumber", mOrderNumber);
			obj.putOpt("phoneNumber", mPhoneNumber);
			JSONUtils.putJSONable(obj, "property", mProperty);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert HotelBookingResponse object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mHotelConfNumber = obj.optString("hotelConfNumber", null);
		mItineraryId = obj.optString("itineraryId", null);
		mOrderNumber = obj.optString("orderNumber", null);
		mPhoneNumber = obj.optString("phoneNumber", null);
		mProperty = JSONUtils.getJSONable(obj, "property", Property.class);
		return true;
	}
}
