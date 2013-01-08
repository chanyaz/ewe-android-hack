package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.DateTime;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * 
 * Possible TODO:
 * - Add sorting methods for trip components
 * - Flesh out trip components into different types
 */
public class Trip implements JSONable {

	public static enum TimePeriod {
		UPCOMING,
		INPROGRESS,
		COMPLETED
	}

	private String mTripId;
	private String mTripNumber;

	private String mTitle;
	private String mDescription;

	private DateTime mStartDate;
	private DateTime mEndDate;

	private BookingStatus mBookingStatus;
	private TimePeriod mTimePeriod;

	private List<TripComponent> mTripComponents = new ArrayList<TripComponent>();

	public String getTripId() {
		return mTripId;
	}

	public void setTripId(String tripId) {
		mTripId = tripId;
	}

	public String getTripNumber() {
		return mTripNumber;
	}

	public void setTripNumber(String tripNumber) {
		mTripNumber = tripNumber;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
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

	public TimePeriod getTimePeriod() {
		return mTimePeriod;
	}

	public void setTimePeriod(TimePeriod timePeriod) {
		mTimePeriod = timePeriod;
	}

	public void addTripComponent(TripComponent tripComponent) {
		mTripComponents.add(tripComponent);
	}

	public List<TripComponent> getTripComponents() {
		return mTripComponents;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			obj.putOpt("tripId", mTripId);
			obj.putOpt("tripNumber", mTripNumber);

			obj.putOpt("title", mTitle);
			obj.putOpt("description", mDescription);

			JSONUtils.putJSONable(obj, "startDate", mStartDate);
			JSONUtils.putJSONable(obj, "endDate", mEndDate);

			JSONUtils.putEnum(obj, "bookingStatus", mBookingStatus);
			JSONUtils.putEnum(obj, "timePeriod", mTimePeriod);

			JSONUtils.putJSONableList(obj, "tripComponents", mTripComponents);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mTripId = obj.optString("tripId");
		mTripNumber = obj.optString("tripNumber");

		mTitle = obj.optString("title");
		mDescription = obj.optString("description");

		mStartDate = JSONUtils.getJSONable(obj, "startDate", DateTime.class);
		mEndDate = JSONUtils.getJSONable(obj, "endDate", DateTime.class);

		mBookingStatus = JSONUtils.getEnum(obj, "bookingStatus", BookingStatus.class);
		mTimePeriod = JSONUtils.getEnum(obj, "timePeriod", TimePeriod.class);

		mTripComponents = JSONUtils.getJSONableList(obj, "tripComponents", TripComponent.class);

		return true;
	}
}
