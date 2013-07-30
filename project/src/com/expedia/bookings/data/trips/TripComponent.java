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

	// Order matters here, we sort the cards based on Type.ordinal()
	public static enum Type {
		FLIGHT,
		CAR,
		ACTIVITY,
		HOTEL,
		CRUISE,
		PACKAGE,
		FALLBACK;
	}

	private Type mType;

	private String mUniqueId;

	private DateTime mStartDate;
	private DateTime mEndDate;

	private BookingStatus mBookingStatus;

	// The parent trip/package; do NOT serialize this, as it is just a reference
	// that should be set by the parent.
	private Trip mParent;
	private TripPackage mParentPackage;

	public TripComponent() {
		// Empty constructor for JSONable
	}

	public TripComponent(Type type) {
		mType = type;
	}

	public Type getType() {
		return mType;
	}

	public void setUniqueId(String uniqueId) {
		mUniqueId = uniqueId;
	}

	public String getUniqueId() {
		return mUniqueId;
	}

	public DateTime getStartDate() {
		// If we have no start date, fallback to parent start date
		if (mStartDate == null) {
			Trip parent = getParentTrip();
			if (parent != null) {
				return parent.getStartDate();
			}
		}

		return mStartDate;
	}

	public void setStartDate(DateTime startDate) {
		mStartDate = startDate;
	}

	public DateTime getEndDate() {
		// If we have no end date, fallback to overall parent end date
		if (mEndDate == null) {
			Trip parent = getParentTrip();
			if (parent != null) {
				return parent.getEndDate();
			}
		}

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
		if (mParentPackage != null) {
			return mParentPackage.getParentTrip();
		}

		return mParent;
	}

	public void setParentPackage(TripPackage tripPackage) {
		mParentPackage = tripPackage;
	}

	public TripPackage getParentPackage() {
		return mParentPackage;
	}

	public boolean isInPackage() {
		return mParentPackage != null;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			JSONUtils.putEnum(obj, "type", mType);

			obj.putOpt("uniqueId", mUniqueId);

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

		mUniqueId = obj.optString("uniqueId", null);

		mStartDate = JSONUtils.getJSONable(obj, "startDate", DateTime.class);
		mEndDate = JSONUtils.getJSONable(obj, "endDate", DateTime.class);

		mBookingStatus = JSONUtils.getEnum(obj, "bookingStatus", BookingStatus.class);

		return true;
	}

}
