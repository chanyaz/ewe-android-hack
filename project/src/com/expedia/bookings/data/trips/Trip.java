package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.DateTime;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Trip implements JSONable, Comparable<Trip> {

	public static enum TimePeriod {
		UPCOMING,
		INPROGRESS,
		COMPLETED
	}

	// This is only filled when this is a guest trip; if it was acquired through
	// the logged in user, it will be null/empty.
	private String mGuestEmailAddress;

	private String mTripId;
	private String mTripNumber;

	private String mTitle;
	private String mDescription;

	private DateTime mStartDate;
	private DateTime mEndDate;

	private BookingStatus mBookingStatus;
	private TimePeriod mTimePeriod;

	private List<TripComponent> mTripComponents = new ArrayList<TripComponent>();

	// There are two levels of details - a quick, cached copy from the API and
	// a full, up-to-date copy.  Thus, two possible update times.
	private long mLastQuickUpdate;
	private long mLastFullUpdate;

	public Trip() {
		// Default constructor
	}

	public Trip(String guestEmailAddress, String tripId) {
		mGuestEmailAddress = guestEmailAddress;
		mTripId = tripId;
	}

	public String getGuestEmailAddress() {
		return mGuestEmailAddress;
	}

	public boolean isGuest() {
		return !TextUtils.isEmpty(mGuestEmailAddress);
	}

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

	public long getLastQuickUpdateMillis() {
		return mLastQuickUpdate;
	}

	public long getLastFullUpdateMillis() {
		return mLastFullUpdate;
	}

	// A trip is not valid until it's at least had some data loaded;
	// you can add an invalid guest trip.
	public boolean isValidTrip() {
		return mStartDate != null;
	}

	public void updateFrom(Trip other, boolean isFullUpdate) {
		// For now, we assume that updateFrom() will always have more details than
		// we have now, so we blow away most current data.  This may not be true
		// once the API is fully fleshed otu.

		mTripNumber = other.mTripNumber;

		mTitle = other.mTitle;
		mDescription = other.mDescription;

		mStartDate = other.mStartDate;
		mEndDate = other.mEndDate;

		mBookingStatus = other.mBookingStatus;
		mTimePeriod = other.mTimePeriod;

		mTripComponents = other.mTripComponents;

		long updateTime = Calendar.getInstance().getTimeInMillis();
		if (isFullUpdate) {
			mLastFullUpdate = updateTime;
		}
		else {
			mLastQuickUpdate = updateTime;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			obj.putOpt("guestEmailAddress", mGuestEmailAddress);

			obj.putOpt("tripId", mTripId);
			obj.putOpt("tripNumber", mTripNumber);

			obj.putOpt("title", mTitle);
			obj.putOpt("description", mDescription);

			JSONUtils.putJSONable(obj, "startDate", mStartDate);
			JSONUtils.putJSONable(obj, "endDate", mEndDate);

			JSONUtils.putEnum(obj, "bookingStatus", mBookingStatus);
			JSONUtils.putEnum(obj, "timePeriod", mTimePeriod);

			JSONUtils.putJSONableList(obj, "tripComponents", mTripComponents);

			obj.putOpt("lastQuickUpdate", mLastQuickUpdate);
			obj.putOpt("lastFullUpdate", mLastFullUpdate);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mGuestEmailAddress = obj.optString("guestEmailAddress", null);

		mTripId = obj.optString("tripId");
		mTripNumber = obj.optString("tripNumber");

		mTitle = obj.optString("title");
		mDescription = obj.optString("description");

		mStartDate = JSONUtils.getJSONable(obj, "startDate", DateTime.class);
		mEndDate = JSONUtils.getJSONable(obj, "endDate", DateTime.class);

		mBookingStatus = JSONUtils.getEnum(obj, "bookingStatus", BookingStatus.class);
		mTimePeriod = JSONUtils.getEnum(obj, "timePeriod", TimePeriod.class);

		mTripComponents = JSONUtils.getJSONableList(obj, "tripComponents", TripComponent.class);

		mLastQuickUpdate = obj.optLong("lastQuickUpdate");
		mLastFullUpdate = obj.optLong("lastFullUpdate");

		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Comparable

	@Override
	public int compareTo(Trip another) {
		// TODO: Come up with something better later, but for now, let's just
		// compare based on start date (and trip id lacking that)

		if (mStartDate != null && another.mStartDate != null) {
			int result = mStartDate.compareTo(another.mStartDate);
			if (result != 0) {
				return result;
			}
		}

		if (mEndDate != null && another.mEndDate != null) {
			int result = mEndDate.compareTo(another.mEndDate);
			if (result != 0) {
				return result;
			}
		}

		return mTripId.compareTo(another.mTripId);
	}
}
