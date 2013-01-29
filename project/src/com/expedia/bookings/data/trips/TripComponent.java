package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.DateTime;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * Represents an individual segment of a trip.
 */
public class TripComponent implements JSONable {

	public static enum Type {
		FLIGHT,
		HOTEL,
		CAR,
		CRUISE,
		ACTIVITY;
	}

	private Type mType;

	private DateTime mStartDate;
	private DateTime mEndDate;

	private BookingStatus mBookingStatus;

	// The parent trip; do NOT serialize this, as it is just a reference
	// that should be set by the parent.
	private Trip mParent;

	public TripComponent(Type type) {
		mType = type;
	}

	public Type getType() {
		return mType;
	}

	public DateTime getStartDate() {
		return mStartDate;
	}

	public void setStartDate(DateTime startDate) {
		mStartDate = startDate;
	}

	public DateTime getEndDate() {
		return mEndDate;
	}

	public void setEndDate(DateTime endDate) {
		mEndDate = endDate;
	}

	public BookingStatus getBookingStatus() {
		return mBookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		mBookingStatus = bookingStatus;
	}

	public void setParentTrip(Trip trip) {
		mParent = trip;
	}

	public Trip getParentTrip() {
		return mParent;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			JSONUtils.putEnum(obj, "type", mType);

			JSONUtils.putJSONable(obj, "startDate", mStartDate);
			JSONUtils.putJSONable(obj, "endDate", mEndDate);

			JSONUtils.putEnum(obj, "bookingStatus", mBookingStatus);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mType = JSONUtils.getEnum(obj, "type", Type.class);

		mStartDate = JSONUtils.getJSONable(obj, "startDate", DateTime.class);
		mEndDate = JSONUtils.getJSONable(obj, "endDate", DateTime.class);
		return true;
	}
}
