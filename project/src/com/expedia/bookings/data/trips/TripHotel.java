package com.expedia.bookings.data.trips;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Property;
import com.mobiata.android.json.JSONUtils;

public class TripHotel extends TripComponent {

	private Property mProperty;
	private String mCheckInTime;
	private int mGuests;
	private Set<String> mConfirmationNumbers;

	public TripHotel() {
		super(Type.HOTEL);
	}

	public Property getProperty() {
		return mProperty;
	}

	public void setProperty(Property property) {
		mProperty = property;
	}

	public String getCheckInTime() {
		return mCheckInTime;
	}

	public void setCheckInTime(String checkInTime) {
		mCheckInTime = checkInTime;
	}

	public int getGuests() {
		return mGuests;
	}

	public void setGuests(int guests) {
		mGuests = guests;
	}

	public Set<String> getConfirmationNumbers() {
		return mConfirmationNumbers;
	}

	public void addConfirmationNumber(String confirmationNumber) {
		if (mConfirmationNumbers == null) {
			mConfirmationNumbers = new HashSet<String>();
		}

		mConfirmationNumbers.add(confirmationNumber);
	}

	public void setConfirmationNumbers(Set<String> confirmationNumbers) {
		mConfirmationNumbers = confirmationNumbers;
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
