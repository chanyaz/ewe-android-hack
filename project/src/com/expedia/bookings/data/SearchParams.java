package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;

import com.expedia.bookings.R;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.SettingUtils;

public class SearchParams implements JSONable {

	public static enum SearchType {
		FREEFORM, KEYWORD, MY_LOCATION, PROXIMITY, PROPERTY
	}

	private SearchType mSearchType = SearchType.MY_LOCATION;
	private String mFreeformLocation;
	private Calendar mCheckInDate;
	private Calendar mCheckOutDate;
	private int mNumAdults;
	private List<Integer> mChildren;
	private Set<String> mPropertyIds;

	// These may be out of sync with freeform location; make sure to sync before
	// using.
	private double mSearchLatitude;
	private double mSearchLongitude;
	private boolean mSearchLatLonUpToDate;

	// This might get filled as a result of an autosuggestion
	private String mRegionId;

	// The variables below are just for analytics
	// mUserFreeformLocation is what the user entered for a freeform location.  We may disambiguate or
	// figure out a better string when we ultimately do a search.
	private String mUserFreeformLocation;

	public SearchParams() {
		init();
	}

	/**
	 * Creates a new SearchParams object populated with the globally stored defaults from the passed SharedPreferences object.
	 * @param prefs
	 */
	public SearchParams(SharedPreferences prefs) {
		init();
		String searchParamsJson = prefs.getString("searchParams", null);
		if (searchParamsJson != null) {
			try {
				JSONObject obj = new JSONObject(searchParamsJson);
				fromJson(obj);
				ensureValidCheckInDate();
			}
			catch (JSONException e) {
				Log.e("Failed to load saved search params.");
			}
		}
	}

	private void init() {
		mSearchLatLonUpToDate = false;

		setDefaultStay();

		// Setup default adults/children 
		mNumAdults = 1;
		mChildren = null;

		// Setup default number of results
		mFreeformLocation = null;
		mPropertyIds = new HashSet<String>();
	}

	public void setDefaultStay() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		mCheckInDate = new GregorianCalendar(year, month, dayOfMonth);
		mCheckOutDate = new GregorianCalendar(year, month, dayOfMonth + 1);
	}

	public SearchParams(JSONObject obj) {
		if (obj != null) {
			fromJson(obj);
		}
	}

	public SearchParams copy() {
		return new SearchParams(toJson());
	}

	public SearchType getSearchType() {
		return mSearchType;
	}

	/**
	 * Sets the type of search for this SearchParams. Returns true if the type has changed.
	 * @param searchType
	 * @return
	 */
	public boolean setSearchType(SearchType searchType) {
		boolean changed = mSearchType != searchType;
		if (searchType != SearchType.FREEFORM) {
			invalidateFreeformLocation();
		}
		mSearchType = searchType;
		return changed;
	}

	/**
	 * Sets the freeform location for this SearchParams object. Also marks the (latitude, longitude) 
	 * position as not up to date and clears the regionId. Returns false if the location passed was 
	 * the same as before.
	 * @param freeformLocation
	 * @return
	 */
	public boolean setFreeformLocation(String freeformLocation) {
		if (mFreeformLocation != null && mFreeformLocation.equals(freeformLocation)) {
			Log.v("Not resetting freeform location; already searching this spot: " + freeformLocation);
			return false;
		}

		mFreeformLocation = freeformLocation;
		mSearchLatLonUpToDate = false;
		mRegionId = null;
		return true;
	}

	public void invalidateFreeformLocation() {
		mFreeformLocation = null;
		mSearchLatLonUpToDate = false;
		mRegionId = null;
	}

	public void setFreeformLocation(Address address) {
		setFreeformLocation(LocationServices.formatAddress(address));
	}

	public String getFreeformLocation() {
		return mFreeformLocation;
	}

	public void setUserFreeformLocation(String userFreeformLocation) {
		mUserFreeformLocation = userFreeformLocation;
	}

	public String getUserFreeformLocation() {
		return mUserFreeformLocation;
	}

	public boolean hasFreeformLocation() {
		return mFreeformLocation != null && mFreeformLocation.length() > 0;
	}

	/**
	 * Returns whether this SearchParams object has enough information to query E3.
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
		case FREEFORM:
			return mFreeformLocation;
		case MY_LOCATION:
			return context.getString(R.string.current_location);
		case PROXIMITY:
			return context.getString(R.string.visible_map_area);
		default:
			return null;
		}
	}

	public void fillFromSearch(Search search) {
		setSearchType(SearchType.FREEFORM);
		setFreeformLocation(search.getFreeformLocation());
		setRegionId(search.getRegionId());
		if (search.hasLatLng()) {
			setSearchLatLon(search.getLatitude(), search.getLongitude());
		}
	}

	public void addPropertyId(String propertyId) {
		mPropertyIds.add(propertyId);
	}

	public Set<String> getPropertyIds() {
		return mPropertyIds;
	}

	public boolean hasPropertyIds() {
		return mPropertyIds != null && mPropertyIds.size() > 0;
	}

	public void clearPropertyIds() {
		mPropertyIds.clear();
	}

	public void ensureValidCheckInDate() {
		Calendar now = Calendar.getInstance();
		if (getCheckInDate().get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR) && getCheckInDate().before(now)) {
			Log.d("Search params had a checkin date previous to today, setting it to today's date.");
			setCheckInDate(now);
		}
	}

	/**
	 * Ensures that the check in date is not AFTER the check out date. Keeps the
	 * duration of the stay the same.
	 * 
	 * @param cal
	 */
	public void setCheckInDate(Calendar cal) {
		if (mCheckInDate != null
				&& mCheckOutDate != null
				&& (cal.after(mCheckOutDate) || (cal.get(Calendar.YEAR) == mCheckOutDate.get(Calendar.YEAR)
						&& cal.get(Calendar.MONTH) == mCheckOutDate.get(Calendar.MONTH) && cal
						.get(Calendar.DAY_OF_MONTH) == mCheckOutDate.get(Calendar.DAY_OF_MONTH)))) {
			int stayDuration = getStayDuration();
			mCheckOutDate = (Calendar) cal.clone();
			mCheckOutDate.add(Calendar.DAY_OF_MONTH, stayDuration);
		}

		mCheckInDate = cal;
	}

	public Calendar getCheckInDate() {
		// To be safe, make sure that we're always dealing with UTC
		mCheckInDate.setTimeZone(CalendarUtils.getFormatTimeZone());
		return mCheckInDate;
	}

	/**
	 * Ensures that the check out date is not BEFORE the check in date. Keeps
	 * the duration of the stay the same.
	 * 
	 * @param cal
	 */
	public void setCheckOutDate(Calendar cal) {
		if (mCheckInDate != null
				&& mCheckOutDate != null
				&& (cal.before(mCheckInDate) || (cal.get(Calendar.YEAR) == mCheckInDate.get(Calendar.YEAR)
						&& cal.get(Calendar.MONTH) == mCheckInDate.get(Calendar.MONTH) && cal
						.get(Calendar.DAY_OF_MONTH) == mCheckInDate.get(Calendar.DAY_OF_MONTH)))) {
			int stayDuration = getStayDuration();
			mCheckInDate = (Calendar) cal.clone();
			mCheckInDate.add(Calendar.DAY_OF_MONTH, -stayDuration);
		}

		mCheckOutDate = cal;
	}

	/**
	 * @return the current stay duration in days; 0 if check in or check out not
	 *         specified yet.
	 */
	public int getStayDuration() {
		if (mCheckInDate == null || mCheckOutDate == null) {
			return 0;
		}

		return (int) CalendarUtils.getDaysBetween(mCheckInDate, mCheckOutDate);
	}

	public Calendar getCheckOutDate() {
		// To be safe, make sure that we're always dealing with GMT
		mCheckOutDate.setTimeZone(CalendarUtils.getFormatTimeZone());
		return mCheckOutDate;
	}

	public void setNumAdults(int numAdults) {
		// We don't allow someone to set zero adults.
		if (numAdults < 1) {
			numAdults = 1;
		}
		mNumAdults = numAdults;
	}

	public int getNumAdults() {
		return mNumAdults;
	}

	public void setChildren(List<Integer> children) {
		mChildren = children;
	}

	public List<Integer> getChildren() {
		if (mChildren == null) {
			mChildren = new ArrayList<Integer>();
		}

		return mChildren;
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

	public boolean hasRegionId() {
		return mRegionId != null;
	}

	public boolean fromJson(JSONObject obj) {
		mFreeformLocation = obj.optString("freeformLocation", null);
		mSearchLatLonUpToDate = obj.optBoolean("hasLatLon", false);
		mSearchLatitude = obj.optDouble("latitude", 0);
		mSearchLongitude = obj.optDouble("longitude", 0);

		long checkinMillis = obj.optLong("checkinDate", -1);
		if (checkinMillis != -1) {
			mCheckInDate = Calendar.getInstance();
			mCheckInDate.setTimeInMillis(checkinMillis);
		}
		long checkoutMillis = obj.optLong("checkoutDate", -1);
		if (checkoutMillis != -1) {
			mCheckOutDate = Calendar.getInstance();
			mCheckOutDate.setTimeInMillis(checkoutMillis);
		}

		mNumAdults = obj.optInt("numAdults", 0);
		mChildren = JSONUtils.getIntList(obj, "children");

		if (obj.has("searchType")) {
			mSearchType = SearchType.valueOf(obj.optString("searchType"));
		}

		mRegionId = obj.optString("regionId", null);

		mUserFreeformLocation = obj.optString("userFreeformLocation", null);

		try {
			mPropertyIds = new HashSet<String>();

			JSONArray arr;
			arr = obj.getJSONArray("propertyIds");
			for (int i = 0; i < arr.length(); i++) {
				mPropertyIds.add(arr.getString(i));
			}
		}
		catch (JSONException e) {
			Log.w("Could not read search params JSON.", e);
			return false;
		}

		return true;
	}

	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("freeformLocation", mFreeformLocation);
			if (mSearchLatLonUpToDate) {
				obj.put("hasLatLon", mSearchLatLonUpToDate);
				obj.put("latitude", mSearchLatitude);
				obj.put("longitude", mSearchLongitude);
			}
			if (mCheckInDate != null) {
				obj.put("checkinDate", mCheckInDate.getTimeInMillis());
			}
			if (mCheckOutDate != null) {
				obj.put("checkoutDate", mCheckOutDate.getTimeInMillis());
			}
			obj.put("numAdults", mNumAdults);
			JSONUtils.putIntList(obj, "children", mChildren);

			if (mSearchType != null) {
				obj.put("searchType", mSearchType);
			}

			JSONArray propertyIds = new JSONArray();
			for (String propertyId : mPropertyIds) {
				propertyIds.put(propertyId);
			}
			obj.put("propertyIds", propertyIds);

			obj.put("regionId", mRegionId);

			obj.put("userFreeformLocation", mUserFreeformLocation);
		}
		catch (JSONException e) {
			Log.w("Could not write search params JSON.", e);
		}
		return obj;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SearchParams) {
			SearchParams other = (SearchParams) o;

			// Note that "equals" doesn't mean strictly equals.  In this situation, it means that
			// the two SearchParams objects will result in the equivalent search results.  It does not
			// compare some state variables (such as lat/lon, which are retrieved from the freeform location

			return this.getSearchType().equals(other.getSearchType())
					&& (mFreeformLocation != null ? mFreeformLocation.equals(other.getFreeformLocation()) : true) // mFreeformLocation may be null
					&& this.mSearchLatitude == other.getSearchLatitude()
					&& this.mSearchLongitude == other.getSearchLongitude()
					&& this.mPropertyIds.equals(other.getPropertyIds())
					&& this.mCheckInDate.equals(other.getCheckInDate())
					&& this.mCheckOutDate.equals(other.getCheckOutDate()) && this.mNumAdults == other.getNumAdults()
					&& (this.mChildren == null ? other.getChildren() == null : mChildren.equals(other.getChildren()));
		}
		return false;
	}

	public void saveToSharedPreferences(SharedPreferences prefs) {
		Editor editor = prefs.edit();
		editor.putString("searchParams", toJson().toString());
		SettingUtils.commitOrApply(editor);
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
}
