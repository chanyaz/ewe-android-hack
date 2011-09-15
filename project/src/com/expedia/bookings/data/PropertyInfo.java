package com.expedia.bookings.data;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class PropertyInfo implements JSONable {

	// Map from roomCode (aka roomTypeCode) to the description
	//
	// FUTURE NOTE: If we ever need more than this from the room, make a Room object
	// and store that instead.  Since all we need is the long description, this is
	// kept simple for now.
	private Map<String, String> mRoomLongDescriptions;
	private String mCheckInTime;
	private String mCheckOutTime;
	private String mPropertyId;

	public PropertyInfo() {
		mRoomLongDescriptions = new HashMap<String, String>();
	}

	public void addRoomLongDescription(String roomTypeCode, String description) {
		mRoomLongDescriptions.put(roomTypeCode, description);
	}

	public String getRoomLongDescription(Rate rate) {
		return getRoomLongDescription(rate.getRoomTypeCode());
	}

	public String getRoomLongDescription(String roomTypeCode) {
		if (roomTypeCode == null) {
			return null;
		}
		return mRoomLongDescriptions.get(roomTypeCode);
	}
	
	public void setCheckInTime(String checkIn) {
		mCheckInTime = checkIn;
	}
	
	public String getCheckInTime() {
		return mCheckInTime;
	}
	
	public void setCheckOutTime(String checkOut) {
		mCheckOutTime = checkOut;
	}
	
	public String getCheckOutTime() {
		return mCheckOutTime;
	}
	
	public void setPropertyId(String propertyId) {
		mPropertyId = propertyId;
	}

	public String getPropertyId() {
		return mPropertyId;
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			JSONUtils.putStringMap(obj, "roomLongDescriptions", mRoomLongDescriptions);
			obj.putOpt("checkInTime", mCheckInTime);
			obj.putOpt("checkOutTime", mCheckOutTime);
			obj.putOpt("propertyId", mPropertyId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert PropertyInfo object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mRoomLongDescriptions = JSONUtils.getStringMap(obj, "roomLongDescriptions");
		mCheckInTime = obj.optString("checkInTime", null);
		mCheckOutTime = obj.optString("checkOutTime", null);
		mPropertyId = obj.optString("propertyId", null);

		return true;
	}
}
