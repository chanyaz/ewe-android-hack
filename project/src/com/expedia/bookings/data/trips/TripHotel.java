package com.expedia.bookings.data.trips;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Property;
import com.mobiata.android.json.JSONUtils;

public class TripHotel extends TripComponent {

	private Property mProperty;
	private Date mCheckinTime;
	private int mGuests;

	public TripHotel() {
		super(Type.HOTEL);
	}

	public Property getProperty() {
		return mProperty;
	}

	public void setProperty(Property property) {
		mProperty = property;
	}

	public Date getCheckinTime() {
		return mCheckinTime;
	}

	public void setCheckinTime(Date checkinTime) {
		mCheckinTime = checkinTime;
	}

	public int getGuests() {
		return mGuests;
	}

	public void setGuests(int guests) {
		mGuests = guests;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "property", mProperty);
			obj.put("guests", mGuests);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mProperty = JSONUtils.getJSONable(obj, "property", Property.class);
		mGuests = obj.optInt("guests");
		return true;
	}
}
