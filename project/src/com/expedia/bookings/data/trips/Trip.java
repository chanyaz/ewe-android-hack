package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Trip implements JSONable, Comparable<Trip> {

	public static enum TimePeriod {
		UPCOMING,
		INPROGRESS,
		COMPLETED
	}

	public static enum LevelOfDetail {
		NONE, // Not loaded by API yet
		SUMMARY, // Loaded by API, only summary
		SUMMARY_FALLBACK, // Got summary, but for some reason can't get full details
		FULL, // Received full details at some point
	}

	private LevelOfDetail mLevelOfDetail = LevelOfDetail.NONE;

	// This is only filled when this is a guest trip; if it was acquired through
	// the logged in user, it will be null/empty.
	private String mGuestEmailAddress;

	/*
	 * For reference:
	 * tripId == Huge GUID (e.g. 1af310ef-cfa9-442a-96ca-669d67a2fb1d)
	 * tripNumber == Customer facing number (e.g. 11239187496)
	 * 
	 * A trip is not valid until it has a tripId (e.g., if it's an invalid guest itin
	 * then it'll have a trip number but the trip id will never get filled in). 
	 */
	private String mTripId;
	private String mTripNumber;

	private String mTitle;
	private String mDescription;

	private String mDetailsUrl;

	private DateTime mStartDate;
	private DateTime mEndDate;

	private BookingStatus mBookingStatus;
	private TimePeriod mTimePeriod;

	private List<TripComponent> mTripComponents = new ArrayList<TripComponent>();
	private List<Insurance> mTripInsurance = new ArrayList<Insurance>();

	// There are two levels of details - a quick, cached copy from the API and
	// a full, up-to-date copy.  Thus, two possible update times.
	private long mLastCachedUpdate;
	private long mLastFullUpdate;

	public Trip() {
		// Default constructor
	}

	public Trip(String guestEmailAddress, String tripNumber) {
		mGuestEmailAddress = guestEmailAddress;
		mTripNumber = tripNumber;
	}

	public void setLevelOfDetail(LevelOfDetail levelOfDetail) {
		mLevelOfDetail = levelOfDetail;
	}

	public LevelOfDetail getLevelOfDetail() {
		return mLevelOfDetail;
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

	/**
	 * The API can use either tripNumber or tripId.  It is preferable to use tripId,
	 * as it is easier on the server.
	 */
	public String getTripIdentifierForApi() {
		if (!TextUtils.isEmpty(mTripId)) {
			return mTripId;
		}
		return mTripNumber;
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

	public String getDetailsUrl() {
		return mDetailsUrl;
	}

	public void setDetailsUrl(String url) {
		mDetailsUrl = url;
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
		tripComponent.setParentTrip(this);
	}

	public void addInsurance(Insurance insurance) {
		mTripInsurance.add(insurance);
	}

	public List<TripComponent> getTripComponents() {
		return mTripComponents;
	}

	public List<Insurance> getTripInsurance() {
		return mTripInsurance;
	}

	// Call whenever mTripComponents is updated (outside of addTripComponent())
	private void associateTripWithComponents() {
		for (TripComponent component : mTripComponents) {
			component.setParentTrip(this);
		}
	}

	public long getLastCachedUpdateMillis() {
		return mLastCachedUpdate;
	}

	public long getLastFullUpdateMillis() {
		return mLastFullUpdate;
	}

	public void updateFrom(Trip other) {
		// For now, we assume that updateFrom() will always have more details than
		// we have now, so we blow away most current data.  This may not be true
		// once the API is fully fleshed otu.

		mTripId = other.mTripId;
		mTripNumber = other.mTripNumber;

		mTitle = other.mTitle;
		mDescription = other.mDescription;

		mStartDate = other.mStartDate;
		mEndDate = other.mEndDate;

		mBookingStatus = other.mBookingStatus;
		mTimePeriod = other.mTimePeriod;

		mTripComponents = other.mTripComponents;
		associateTripWithComponents();

		mTripInsurance = other.getTripInsurance();
	}

	public void markUpdated(boolean isFullUpdate) {
		long updateTime = Calendar.getInstance().getTimeInMillis();

		// A full update also counts as a cached update (since it has
		// more data)
		mLastCachedUpdate = updateTime;

		if (isFullUpdate) {
			mLastFullUpdate = updateTime;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			JSONUtils.putEnum(obj, "levelOfDetail", mLevelOfDetail);

			obj.putOpt("guestEmailAddress", mGuestEmailAddress);

			obj.putOpt("tripId", mTripId);
			obj.putOpt("tripNumber", mTripNumber);

			obj.putOpt("title", mTitle);
			obj.putOpt("description", mDescription);
			obj.putOpt("webDetailsURL", mDetailsUrl);

			JSONUtils.putJSONable(obj, "startDate", mStartDate);
			JSONUtils.putJSONable(obj, "endDate", mEndDate);

			JSONUtils.putEnum(obj, "bookingStatus", mBookingStatus);
			JSONUtils.putEnum(obj, "timePeriod", mTimePeriod);

			JSONUtils.putJSONableList(obj, "tripComponents", mTripComponents);
			JSONUtils.putJSONableList(obj, "insurance", mTripInsurance);

			obj.putOpt("lastCachedUpdate", mLastCachedUpdate);
			obj.putOpt("lastFullUpdate", mLastFullUpdate);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mLevelOfDetail = JSONUtils.getEnum(obj, "levelOfDetail", LevelOfDetail.class);

		mGuestEmailAddress = obj.optString("guestEmailAddress", null);

		mTripId = obj.optString("tripId");
		mTripNumber = obj.optString("tripNumber");

		mTitle = obj.optString("title");
		mDescription = obj.optString("description");
		mDetailsUrl = obj.optString("webDetailsURL");

		mStartDate = JSONUtils.getJSONable(obj, "startDate", DateTime.class);
		mEndDate = JSONUtils.getJSONable(obj, "endDate", DateTime.class);

		mBookingStatus = JSONUtils.getEnum(obj, "bookingStatus", BookingStatus.class);
		mTimePeriod = JSONUtils.getEnum(obj, "timePeriod", TimePeriod.class);

		// We have to load trip components manually here; otherwise they are all loaded as
		// TripComponent instead of as the individual classes they are (TripFlight, TripHotel, etc)
		mTripComponents.clear();
		JSONArray tripComponents = obj.optJSONArray("tripComponents");
		if (tripComponents != null) {
			for (int a = 0; a < tripComponents.length(); a++) {
				JSONObject tripComponent = tripComponents.optJSONObject(a);
				Type type = JSONUtils.getEnum(tripComponent, "type", Type.class);
				Class<? extends TripComponent> clz;
				switch (type) {
				case ACTIVITY:
					clz = TripActivity.class;
					break;
				case CAR:
					clz = TripCar.class;
					break;
				case CRUISE:
					clz = TripCruise.class;
					break;
				case FLIGHT:
					clz = TripFlight.class;
					break;
				case HOTEL:
					clz = TripHotel.class;
					break;
				default:
					clz = TripComponent.class;
					break;
				}

				mTripComponents.add(JSONUtils.getJSONable(tripComponents, a, clz));
			}
		}

		associateTripWithComponents();

		if (obj.has("insurance")) {
			mTripInsurance = JSONUtils.getJSONableList(obj, "insurance", Insurance.class);
		}

		mLastCachedUpdate = obj.optLong("lastCachedUpdate");
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

		// Compare tripId, then tripNumber if lacking that
		if (!TextUtils.equals(mTripId, another.mTripId)) {
			if (mTripId == null) {
				return -1;
			}

			return mTripId.compareTo(another.mTripId);
		}

		if (!TextUtils.equals(mTripNumber, another.mTripNumber)) {
			if (mTripNumber == null) {
				return -1;
			}

			return mTripNumber.compareTo(another.mTripNumber);
		}

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Trip)) {
			return false;
		}

		return compareTo((Trip) o) == 0;
	}
}
