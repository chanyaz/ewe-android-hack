package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Property;
import com.mobiata.android.json.JSONUtils;

public class TripHotel extends TripComponent {

	private Property mProperty;

	public TripHotel() {
		super(Type.HOTEL);
	}

	public Property getProperty() {
		return mProperty;
	}

	public void setProperty(Property property) {
		mProperty = property;
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
		return true;
	}
}
