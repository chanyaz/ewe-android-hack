package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class HotelSearchParams implements JSONable {

	private static final String SEARCH_PARAMS_KEY = "searchParams";
	private SearchType mSearchType = SearchType.MY_LOCATION;

	public enum SearchType {
		MY_LOCATION(true, false),
		ADDRESS(true, true),
		POI(false, true),
		CITY(false, false),
		VISIBLE_MAP_AREA(false, false),
		FREEFORM(false, false),
		HOTEL(true, true),;

		private boolean mShouldShowDistance;
		private boolean mShouldShowExactLocation;

		SearchType(boolean shouldShowDistance, boolean shouldShowExactLocation) {
			mShouldShowDistance = shouldShowDistance;
			mShouldShowExactLocation = shouldShowExactLocation;
		}

		public boolean shouldShowDistance() {
			return mShouldShowDistance;
		}

		public boolean shouldShowExactLocation() {
			return mShouldShowExactLocation;
		}
	}

	private String mQuery;
	private LocalDate mCheckInDate;
	private LocalDate mCheckOutDate;
	private int mNumAdults;
	private List<ChildTraveler> mChildren;
	private int mMctc;

	private String mSortType;
	// These may be out of sync with freeform location; make sure to sync before
	// using.
	private double mSearchLatitude;
	private double mSearchLongitude;
	private boolean mSearchLatLonUpToDate;
	private String mCorrespondingAirportCode;

	public String hotelId;
	// This might get filled as a result of an autosuggestion
	private String mRegionId;

	// This will get set if the HotelSearchParams object was created from the widget
	private boolean mIsFromWidget;

	/**
	 *  The variables below are just for analytics
	 *  mUserFreeformLocation is what the user entered for a freeform location.  We may disambiguate or
	 *  figure out a better string when we ultimately do a search.
	 */
	private String mUserQuery;

	public HotelSearchParams() {
		init();
	}

	private void init() {
		mSearchLatLonUpToDate = false;

		setDefaultStay();

		// Setup default adults/children
		mNumAdults = 1;
		mChildren = null;

		// Setup default number of results
		mQuery = null;
	}

	public void setDefaultStay() {
		mCheckInDate = getDefaultCheckInDate();
		mCheckOutDate = getDefaultCheckOutDate();
	}

	public boolean isDefaultStay() {
		return mCheckInDate != null && mCheckInDate.equals(getDefaultCheckInDate()) && mCheckOutDate != null
				&& mCheckOutDate.equals(getDefaultCheckOutDate());
	}

	private LocalDate getDefaultCheckInDate() {
		return new LocalDate();
	}

	private LocalDate getDefaultCheckOutDate() {
		return new LocalDate().plusDays(1);
	}

	public HotelSearchParams(JSONObject obj) {
		if (obj != null) {
			fromJson(obj);
		}
	}

	public HotelSearchParams copy() {
		return new HotelSearchParams(toJson());
	}

	public SearchType getSearchType() {
		return mSearchType;
	}

	/**
	 * Sets the type of search for this HotelSearchParams. Returns true if the type has changed.
	 * @param searchType
	 * @return
	 */
	public boolean setSearchType(SearchType searchType) {
		boolean changed = mSearchType != searchType;
		if (changed && (searchType == SearchType.MY_LOCATION || searchType == SearchType.VISIBLE_MAP_AREA)) {
			clearQuery();
		}
		mSearchType = searchType;
		return changed;
	}

	/**
	 * Sets the location query for this HotelSearchParams object. Also marks the (latitude, longitude)
	 * position as not up to date and clears the regionId. Returns false if the location passed was
	 * the same as before.
	 * @param query
	 * @return
	 */
	public boolean setQuery(String query, boolean resetRegionId) {
		if (mQuery != null && mQuery.equals(query)) {
			Log.v("Not resetting freeform location; already searching this spot: " + query);
			return false;
		}

		mQuery = query;
		mSearchLatLonUpToDate = false;
		if (resetRegionId) {
			mRegionId = null;
		}
		return true;
	}

	public boolean setQuery(String query) {
		return setQuery(query, true);
	}

	public void clearQuery() {
		setQuery(null);
	}

	public String getQuery() {
		return mQuery;
	}

	public boolean hasQuery() {
		return !TextUtils.isEmpty(mQuery);
	}

	/**
	 * Set the original query as typed by the user (it may be overridden by a geocoding response).
	 * @param userQuery
	 */
	public void setUserQuery(String userQuery) {
		mUserQuery = userQuery;
	}

	/**
	 * get the original query as typed by the user.
	 * @param userQuery
	 */
	public String getUserQuery() {
		return mUserQuery;
	}

	/**
	 * Returns whether this HotelSearchParams object has enough information to query E3.
	 * It has enough information if it:
	 * 1. Has a regionId
	 * -or-
	 * 2. Has Lat/Lng
	 * @return
	 */
	public boolean hasEnoughToSearch() {
		return hasRegionId() || hasSearchLatLon();
	}

	/**
	 * @return the search string to display to user based on current params
	 */
	public String getSearchDisplayText(Context context) {
		switch (mSearchType) {
		case CITY:
		case ADDRESS:
		case POI:
		case HOTEL:
		case FREEFORM:
			return mQuery;
		case MY_LOCATION:
			return context.getString(R.string.current_location);
		case VISIBLE_MAP_AREA:
			return context.getString(R.string.visible_map_area);
		default:
			return null;
		}
	}

	public void fillFromHotelSearchParams(HotelSearchParams hotelSearchParams) {
		setSearchType(hotelSearchParams.getSearchType());
		setQuery(hotelSearchParams.getQuery());
		setRegionId(hotelSearchParams.getRegionId());
		setSearchLatLon(hotelSearchParams.getSearchLatitude(), hotelSearchParams.getSearchLongitude());
	}

	public boolean hasValidCheckInDate() {
		// #1562 - Check for null
		if (mCheckInDate == null) {
			return false;
		}

		return JodaUtils.isBeforeOrEquals(LocalDate.now(), mCheckInDate);
	}

	public void ensureValidCheckInDate() {
		if (!hasValidCheckInDate()) {
			Log.d("Search params had a checkin date previous to today, resetting checkin/checkout dates.");
			setDefaultStay();
		}
	}

	public void ensureDatesSet() {
		if (mCheckInDate == null || (mCheckInDate == null && mCheckOutDate == null)) {
			setDefaultStay();
		}
		else if (mCheckOutDate == null) {
			if (hasValidCheckInDate()) {
				mCheckOutDate = mCheckInDate.plusDays(1);
			}
			else {
				ensureValidCheckInDate();
			}
		}
		else {
			ensureValidCheckInDate();
			if (mCheckInDate.isAfter(mCheckOutDate)) {
				LocalDate tmpDate = mCheckInDate;
				mCheckInDate = mCheckOutDate;
				mCheckOutDate = tmpDate;
			}
		}
	}

	/**
	 * Ensures that the check in date is not AFTER the check out date. Keeps the
	 * duration of the stay the same.
	 */
	public void setCheckInDate(LocalDate date) {
		if (date != null && mCheckInDate != null && mCheckOutDate != null
				&& JodaUtils.isAfterOrEquals(date, mCheckOutDate)) {
			int stayDuration = getStayDuration();
			mCheckOutDate = date.plusDays(stayDuration);
		}

		mCheckInDate = date;
	}

	public LocalDate getCheckInDate() {
		return mCheckInDate;
	}

	/**
	 * Ensures that the check out date is not BEFORE the check in date. Keeps
	 * the duration of the stay the same.
	 */
	public void setCheckOutDate(LocalDate date) {
		if (date != null && mCheckInDate != null && mCheckOutDate != null
				&& JodaUtils.isBeforeOrEquals(date, mCheckInDate)) {
			int stayDuration = getStayDuration();
			mCheckInDate = date.minusDays(stayDuration);
		}

		mCheckOutDate = date;
	}

	/**
	 * @return the current stay duration in days; 0 if check in or check out not
	 *         specified yet.
	 */
	public int getStayDuration() {
		if (mCheckInDate == null || mCheckOutDate == null) {
			return 0;
		}

		return JodaUtils.daysBetween(mCheckInDate, mCheckOutDate);
	}

	public LocalDate getCheckOutDate() {
		return mCheckOutDate;
	}

	public void setNumAdults(int numAdults) {
		// We don't allow someone to set zero adults.
		if (numAdults < 1) {
			numAdults = 1;
		}
		mNumAdults = numAdults;
	}

	public void setSortType(String sortType) {
		mSortType = sortType;
	}

	public int getNumAdults() {
		return mNumAdults;
	}

	public void setChildren(List<ChildTraveler> children) {
		mChildren = children;
	}

	public List<ChildTraveler> getChildren() {
		if (mChildren == null) {
			mChildren = new ArrayList<ChildTraveler>();
		}

		return mChildren;
	}

	public int getNumTravelers() {
		return getNumAdults() + getNumChildren();
	}

	public String getSortType() {
		return mSortType;
	}

	public int getNumChildren() {
		return mChildren == null ? 0 : mChildren.size();
	}

	public void setSearchLatLon(double latitude, double longitude) {
		mSearchLatitude = latitude;
		mSearchLongitude = longitude;
		mSearchLatLonUpToDate = true;
	}

	public void setSearchLatLonUpToDate() {
		mSearchLatLonUpToDate = true;
	}

	public double getSearchLatitude() {
		return mSearchLatitude;
	}

	public double getSearchLongitude() {
		return mSearchLongitude;
	}

	public boolean hasSearchLatLon() {
		return mSearchLatLonUpToDate;
	}

	public void setRegionId(String regionId) {
		mRegionId = regionId;
	}

	public String getRegionId() {
		return mRegionId;
	}

	public void setMctc (int mctc) {
		mMctc = mctc;
	}

	public int getMctc() {
		return mMctc;
	}

	public void setCorrespondingAirportCode(String code) {
		mCorrespondingAirportCode = code;
	}

	public String getCorrespondingAirportCode() {
		return mCorrespondingAirportCode;
	}

	public boolean hasRegionId() {
		return mRegionId != null && !mRegionId.equals("0");
	}

	public static HotelSearchParams fromFlightParams(TripBucketItemFlight flight) {
		FlightTrip trip = flight.getFlightTrip();
		FlightLeg firstLeg = trip.getLeg(0);
		FlightLeg secondLeg = trip.getLegCount() > 1 ? trip.getLeg(1) : null;
		FlightSearchParams params = flight.getFlightSearchParams();
		int numFlightTravelers = params.getNumAdults();
		List<ChildTraveler> childTravelers = params.getChildren();
		String regionId = flight.getCheckoutResponse().getDestinationRegionId();
		return fromFlightParams(regionId, firstLeg, secondLeg, numFlightTravelers, childTravelers);
	}

	/**
	 * Creates a HotelSearchParams which can be used to search for hotels
	 * related to a booked Flight product.
	 *
	 * @param regionId the destination region id to perform the search
	 * @param firstLeg the first leg of the trip; required
	 * @param secondLeg the second leg of a trip (if round trip); optional
	 * @param numFlightTravelers
	 * @param childTravelers
	 * @return a HotelSearchParams for those flight parameters
	 */
	public static HotelSearchParams fromFlightParams(String regionId, FlightLeg firstLeg, FlightLeg secondLeg, int numFlightTravelers, List<ChildTraveler> childTravelers) {
		HotelSearchParams hotelParams = new HotelSearchParams();

		// Where //
		// Because we are adding regionId, it doesn't matter too much if our query isn't perfect
		String cityStr = StrUtils.getWaypointCodeOrCityStateString(firstLeg.getLastWaypoint());
		hotelParams.setSearchType(SearchType.CITY);
		hotelParams.setUserQuery(cityStr);
		hotelParams.setQuery(cityStr);
		hotelParams.setRegionId(regionId); // Note: must be last, otherwise will get over-written.

		// When //
		LocalDate checkInDate = new LocalDate(firstLeg.getLastWaypoint().getBestSearchDateTime());
		hotelParams.setCheckInDate(checkInDate);

		if (secondLeg == null) {
			// 1-way flight
			hotelParams.setCheckOutDate(checkInDate.plusDays(1));
		}
		else {
			// Round-trip flight
			LocalDate checkOutDate = new LocalDate(secondLeg.getFirstWaypoint()
					.getMostRelevantDateTime());
			hotelParams.setCheckOutDate(checkOutDate);
			ensureMaxStayTwentyEightDays(hotelParams);
		}

		// Who //
		hotelParams.setChildren(childTravelers);
		int numHotelAdults = Math.min(GuestsPickerUtils.getMaxAdults(0), numFlightTravelers);
		numHotelAdults = Math.max(numHotelAdults, GuestsPickerUtils.MIN_ADULTS); // just in case default...
		hotelParams.setNumAdults(numHotelAdults);
		hotelParams.setCorrespondingAirportCode(firstLeg.getAirport(false).mAirportCode);
		return hotelParams;
	}

	public static HotelSearchParams fromCarParams(CreateTripCarOffer offer) {
		HotelSearchParams hotelParams = new HotelSearchParams();

		// Where //
		hotelParams.setSearchType(SearchType.CITY);

		// Because we are adding a lat/lon parameter, it doesn't matter too much if our query isn't perfect
		String cityStr = offer.pickUpLocation.cityName;
		hotelParams.setUserQuery(cityStr);
		hotelParams.setQuery(cityStr);

		if (Strings.isNotEmpty(offer.pickUpLocation.regionId)) {
			hotelParams.setRegionId(offer.pickUpLocation.regionId);
		}

		double latitude = offer.pickUpLocation.latitude;
		double longitude = offer.pickUpLocation.longitude;
		hotelParams.setSearchLatLon(latitude, longitude);

		hotelParams.setCheckInDate(LocalDate.fromCalendarFields(offer.getPickupTime().toCalendar(Locale.US)));
		hotelParams.setCheckOutDate(LocalDate.fromCalendarFields(offer.getDropOffTime().toCalendar(Locale.US)));
		ensureMaxStayTwentyEightDays(hotelParams);

		return hotelParams;
	}

	private static void ensureMaxStayTwentyEightDays(HotelSearchParams params) {
		// Make sure the stay is no longer than 28 days
		LocalDate checkInDate = params.getCheckInDate();
		LocalDate checkOutDate = params.getCheckOutDate();
		LocalDate maxCheckOutDate = checkInDate.plusDays(28);
		checkOutDate = checkOutDate.isAfter(maxCheckOutDate) ? maxCheckOutDate : checkOutDate;
		int stayDuration = JodaUtils.daysBetween(checkInDate, checkOutDate);
		if (stayDuration == 0) {
			params.setCheckOutDate(checkOutDate.plusDays(1));
		}
		else {
			params.setCheckOutDate(checkOutDate);
		}
	}

	public boolean fromJson(JSONObject obj) {
		mQuery = obj.optString("freeformLocation", null);
		mSearchLatLonUpToDate = obj.optBoolean("hasLatLon", false);
		mSearchLatitude = obj.optDouble("latitude", 0);
		mSearchLongitude = obj.optDouble("longitude", 0);
		mCorrespondingAirportCode = obj.optString("correspondingAirlineCode", null);

		mCheckInDate = JodaUtils.getLocalDateFromJson(obj, "checkInLocalDate");
		mCheckOutDate = JodaUtils.getLocalDateFromJson(obj, "checkOutLocalDate");

		mNumAdults = obj.optInt("numAdults", 0);
		mChildren = JSONUtils.getJSONableList(obj, "children", ChildTraveler.class);

		if (obj.has("searchType")) {
			// TODO: remove the "if" part of this later. It's for backwards compatibility.
			// 2012.08.01
			if (obj.optString("searchType").equals("PROXIMITY")) {
				mSearchType = SearchType.VISIBLE_MAP_AREA;
			}
			else {
				mSearchType = SearchType.valueOf(obj.optString("searchType"));
			}
		}

		mRegionId = obj.optString("regionId", null);

		mIsFromWidget = obj.optBoolean("isFromWidget", false);

		mUserQuery = obj.optString("userFreeformLocation", null);

		return true;
	}

	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("freeformLocation", mQuery);
			if (mSearchLatLonUpToDate) {
				obj.put("hasLatLon", mSearchLatLonUpToDate);
				obj.put("latitude", mSearchLatitude);
				obj.put("longitude", mSearchLongitude);
			}
			obj.put("correspondingAirlineCode", mCorrespondingAirportCode);

			JodaUtils.putLocalDateInJson(obj, "checkInLocalDate", mCheckInDate);
			JodaUtils.putLocalDateInJson(obj, "checkOutLocalDate", mCheckOutDate);

			obj.put("numAdults", mNumAdults);
			JSONUtils.putJSONableList(obj, "children", mChildren);

			if (mSearchType != null) {
				obj.put("searchType", mSearchType);
			}

			obj.put("regionId", mRegionId);

			if (mIsFromWidget) {
				obj.put("isFromWidget", true);
			}

			obj.put("userFreeformLocation", mUserQuery);
		}
		catch (JSONException e) {
			Log.w("Could not write search params JSON.", e);
		}
		return obj;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HotelSearchParams) {
			HotelSearchParams other = (HotelSearchParams) o;

			// Note that "equals" doesn't mean strictly equals.  In this situation, it means that
			// the two HotelSearchParams objects will result in the equivalent search results.  It does not
			// compare some state variables (such as lat/lon, which are retrieved from the freeform location

			return this.getSearchType().equals(other.getSearchType())
					&& (mQuery == null || mQuery.equals(other.getQuery())) // mFreeformLocation may be null
					&& this.mSearchLatitude == other.getSearchLatitude()
					&& this.mSearchLongitude == other.getSearchLongitude()
					&& ((mCheckInDate != null && other.getCheckInDate() != null && this.mCheckInDate.equals(other
							.getCheckInDate())) || (mCheckInDate == null && other.getCheckInDate() == null))
					&& ((mCheckOutDate != null && other.getCheckOutDate() != null && this.mCheckOutDate.equals(other
							.getCheckOutDate())) || (mCheckOutDate == null && other.getCheckOutDate() == null))
					&& (this.mChildren == null ? other.getChildren() == null : mChildren.equals(other.getChildren()));
		}
		return false;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}

	public HotelSearchParams clone() {
		JSONObject json = this.toJson();
		HotelSearchParams params = new HotelSearchParams();
		params.fromJson(json);
		return params;
	}
}
