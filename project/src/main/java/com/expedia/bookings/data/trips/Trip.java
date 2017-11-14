package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.expedia.bookings.data.AirAttach;
import com.expedia.bookings.data.trips.ItinShareInfo.ItinSharable;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Trip implements JSONable, Comparable<Trip>, ItinSharable {

	public enum LevelOfDetail {
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
	private ItinShareInfo mShareInfo = new ItinShareInfo();

	private DateTime mStartDate;
	private DateTime mEndDate;

	private BookingStatus mBookingStatus;

	private CustomerSupport mCustomerSupport;
	private AirAttach mAirAttach;

	private List<TripComponent> mTripComponents = new ArrayList<TripComponent>();
	private List<Insurance> mTripInsurance = new ArrayList<Insurance>();

	// There are two levels of details - a quick, cached copy from the API and
	// a full, up-to-date copy.  Thus, two possible update times.
	private long mLastCachedUpdate;
	private long mLastFullUpdate;

	// To identify if it's a shared itin
	private boolean mIsShared;
	private boolean mIsTripUpgradable;

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

	public AirAttach getAirAttach() {
		return mAirAttach;
	}

	public void setAirAttach(AirAttach airAttach) {
		mAirAttach = airAttach;
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

	@Nullable
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

	public CustomerSupport getCustomerSupport() {
		return mCustomerSupport;
	}

	public void setCustomerSupport(CustomerSupport support) {
		mCustomerSupport = support;
	}

	public void addTripComponent(TripComponent tripComponent) {
		mTripComponents.add(tripComponent);
		tripComponent.setParentTrip(this);
	}

	public void addTripComponents(List<TripComponent> tripComponents) {
		mTripComponents.addAll(tripComponents);
		associateTripWithComponents();
	}

	public void addInsurance(Insurance insurance) {
		mTripInsurance.add(insurance);
	}

	public List<TripComponent> getTripComponents() {
		return mTripComponents;
	}

	public boolean isShared() {
		return mIsShared;
	}

	public void setIsShared(boolean isShared) {
		this.mIsShared = isShared;
	}

	public boolean isTripUpgradable() {
		return mIsTripUpgradable;
	}

	public void setIsTripUpgradable(boolean mIsTripUpgradable) {
		this.mIsTripUpgradable = mIsTripUpgradable;
	}

	public String getItineraryKey() {
		if (isShared()) {
			return getShareInfo().getSharableDetailsUrl();
		}
		else {
			return mTripNumber;
		}
	}

	public boolean hasExpired(int hours) {
		DateTime pastCutOffDateTime = DateTime.now().minusHours(hours);
		return getEndDate() != null && getEndDate().isBefore(pastCutOffDateTime);
	}

	/**
	 * Returns all trip components.  If you want sub components, it will automatically
	 * unroll TripPackages into their constituent parts.
	 */
	public List<TripComponent> getTripComponents(boolean includeSubComponents) {
		if (includeSubComponents) {
			List<TripComponent> components = new ArrayList<TripComponent>();
			for (TripComponent component : mTripComponents) {
				if (component.getType() == Type.PACKAGE) {
					components.addAll(((TripPackage) component).getTripComponents());
				}
				else {
					components.add(component);
				}
			}
			return components;
		}
		else {
			return mTripComponents;
		}
	}

	public boolean hasHotel () {
		for (TripComponent component : getTripComponents(true)) {
			if (component.getType() == Type.HOTEL) {
				return true;
			}
		}
		return false;
	}

	public boolean hasFlight () {
		for (TripComponent component : getTripComponents(true)) {
			if (component.getType() == Type.FLIGHT) {
				return true;
			}
		}
		return false;
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

	public void updateFrom(Trip updatedTrip) {
		// For now, we assume that updateFrom() will always have more details than
		// we have now, so we blow away most current data.  This may not be true
		// once the API is fully fleshed otu.

		mLevelOfDetail = updatedTrip.mLevelOfDetail;

		mTripId = updatedTrip.mTripId;

		mTitle = updatedTrip.mTitle;
		mDescription = updatedTrip.mDescription;

		mDetailsUrl = updatedTrip.mDetailsUrl;

		if (mShareInfo.hasSharableDetailsUrl() && updatedTrip.getShareInfo().hasSharableDetailsUrl()
				&& !mShareInfo.getSharableDetailsUrl().equals(updatedTrip.getShareInfo().getSharableDetailsUrl())) {
			//The sharable details url has changed, so our shortened sharable details url is no longer valid.
			mShareInfo.setShortSharableDetailsUrl(null);
		}

		if (!TextUtils.isEmpty(updatedTrip.getShareInfo().getSharableDetailsUrl())) {
			mShareInfo.setSharableDetailsUrl(updatedTrip.getShareInfo().getSharableDetailsUrl());
		}

		//We dont squash the shortened url, if we dont have a new value for it
		mShareInfo.setShortSharableDetailsUrl(updatedTrip.getShareInfo().hasShortSharableDetailsUrl() ? updatedTrip.getShareInfo()
				.getShortSharableDetailsUrl() : getShareInfo().getShortSharableDetailsUrl());

		mStartDate = updatedTrip.mStartDate;
		mEndDate = updatedTrip.mEndDate;

		mCustomerSupport = updatedTrip.mCustomerSupport;

		mBookingStatus = updatedTrip.mBookingStatus;

		mTripComponents = updatedTrip.mTripComponents;
		associateTripWithComponents();

		mTripInsurance = updatedTrip.getTripInsurance();
		mAirAttach = updatedTrip.getAirAttach();
	}

	public void markUpdated(boolean isFullUpdate) {
		long updateTime = DateTime.now().getMillis();

		// A full update also counts as a cached update (since it has
		// more data)
		mLastCachedUpdate = updateTime;

		if (isFullUpdate) {
			mLastFullUpdate = updateTime;
		}
	}

	/**
	 * Used to compare guest itineraries by email/number for tracking purposes
	 */
	public boolean isSameGuest(Trip other) {
		if (mGuestEmailAddress == null || mTripNumber == null || other == null) {
			return false;
		}

		boolean sameEmail = other.getGuestEmailAddress() != null
				&& mGuestEmailAddress.equals(other.getGuestEmailAddress());
		boolean sameTripNumber = other.getTripNumber() != null && mTripNumber.equals(other.getTripNumber());
		return sameEmail && sameTripNumber;
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
			JSONUtils.putJSONable(obj, "shareInfo", mShareInfo);

			JSONUtils.putJSONable(obj, "customerSupport", mCustomerSupport);

			JodaUtils.putDateTimeInJson(obj, "startDateTime", mStartDate);
			JodaUtils.putDateTimeInJson(obj, "endDateTime", mEndDate);

			JSONUtils.putEnum(obj, "bookingStatus", mBookingStatus);

			TripUtils.putTripComponents(obj, mTripComponents);
			JSONUtils.putJSONableList(obj, "insurance", mTripInsurance);

			obj.putOpt("lastCachedUpdate", mLastCachedUpdate);
			obj.putOpt("lastFullUpdate", mLastFullUpdate);

			obj.putOpt("isShared", mIsShared);
			obj.putOpt("isTripUpgradable", mIsTripUpgradable);

			if (mAirAttach != null) {
				obj.putOpt("airAttach", mAirAttach.toJson());
			}

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

		mShareInfo = JSONUtils.getJSONable(obj, "shareInfo", ItinShareInfo.class);
		mShareInfo = mShareInfo == null ? new ItinShareInfo() : mShareInfo;

		mCustomerSupport = JSONUtils.getJSONable(obj, "customerSupport", CustomerSupport.class);

		mStartDate = JodaUtils.getDateTimeFromJsonBackCompat(obj, "startDateTime", "startDate");
		mEndDate = JodaUtils.getDateTimeFromJsonBackCompat(obj, "endDateTime", "endDate");

		mBookingStatus = JSONUtils.getEnum(obj, "bookingStatus", BookingStatus.class);

		mTripComponents.clear();
		mTripComponents.addAll(TripUtils.getTripComponents(obj));
		associateTripWithComponents();

		if (obj.has("insurance")) {
			mTripInsurance = JSONUtils.getJSONableList(obj, "insurance", Insurance.class);
		}

		mLastCachedUpdate = obj.optLong("lastCachedUpdate");
		mLastFullUpdate = obj.optLong("lastFullUpdate");

		mIsShared = obj.optBoolean("isShared");
		mIsTripUpgradable = obj.optBoolean("isTripUpgradable");

		if (obj.has("airAttach")) {
			JSONObject airAttachJson = obj.optJSONObject("airAttach");
			if (airAttachJson != null) {
				mAirAttach = new AirAttach(airAttachJson);
			}
		}

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
		if (!Strings.equals(mTripId, another.mTripId)) {
			if (mTripId == null) {
				return -1;
			}

			return Strings.compareTo(mTripId, another.mTripId);
		}

		if (!Strings.equals(mTripNumber, another.mTripNumber)) {
			if (mTripNumber == null) {
				return -1;
			}

			return Strings.compareTo(mTripNumber, another.mTripNumber);
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

	//////////////////////////////////////////////////////////////////////////
	// ItinSharable

	@Override
	public ItinShareInfo getShareInfo() {
		return mShareInfo;
	}

	@Override
	public boolean getSharingEnabled() {
		return true;
	}
}
