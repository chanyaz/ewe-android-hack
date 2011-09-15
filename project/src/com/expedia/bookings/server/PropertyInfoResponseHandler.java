package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.PropertyInfo;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.ServerError;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;

public class PropertyInfoResponseHandler extends JsonResponseHandler<PropertyInfoResponse> {

	private Context mContext;
	
	/**
	 * This prefix helps keep track of a download for a particular property. 
	 * We use a unique key per property id to enable support for multiple properties'
	 * information to be downloaded at any given moment across activities
	 */
	public static final String DOWNLOAD_KEY_PREFIX = "PROPERTY_INFO_DOWNLOAD.";

	public PropertyInfoResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public PropertyInfoResponse handleJson(JSONObject response) {
		PropertyInfoResponse infoResponse = new PropertyInfoResponse();
		try {
			// Check for server errors
			if (ParserUtils.parseServerErrors(mContext, response, infoResponse)) {
				return infoResponse;
			}

			JSONObject jsonResponse = response.getJSONObject("body").getJSONObject("HotelInformationResponse");
			
			// Check for errors, return if found
			ServerError serverError = ParserUtils.parseEanError(mContext, jsonResponse);
			if (serverError != null) {
				infoResponse.addError(serverError);
				return infoResponse;
			}

			JSONObject hotelDetails = jsonResponse.getJSONObject("HotelDetails");
			PropertyInfo propertyInfo = new PropertyInfo();
			propertyInfo.setCheckInTime(hotelDetails.getString("checkInTime"));
			propertyInfo.setCheckOutTime(hotelDetails.getString("checkOutTime"));
			propertyInfo.setPropertyId(jsonResponse.getString("@hotelId"));

			JSONArray roomTypes = JSONUtils.getOrWrapJSONArray(jsonResponse.getJSONObject("RoomTypes"), "RoomType");
			int len = roomTypes.length();
			for (int a = 0; a < len; a++) {
				JSONObject roomType = roomTypes.getJSONObject(a);
				String roomTypeCode = roomType.getString("@roomCode");
				String longDescription = roomType.getString("descriptionLong");
				propertyInfo.addRoomLongDescription(roomTypeCode, longDescription);
			}
			infoResponse.setPropertyInfo(propertyInfo);
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON info response.", e);
			return null;
		}

		return infoResponse;
	}
}
