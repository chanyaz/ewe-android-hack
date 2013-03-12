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
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.SettingUtils;

public class SearchParams implements JSONable {

	private static final String SEARCH_PARAMS_KEY = "searchParams";

	public static enum SearchType {
		MY_LOCATION(true, false),
		ADDRESS(true, true),
		POI(true, true),
		CITY(false, false),
		VISIBLE_MAP_AREA(false, false),
		FREEFORM(false, false),
		HOTEL(true, true),
		;

		private boolean mShouldShowDistance;
		private boolean mShouldShowExactLocation;

		private SearchType(boolean shouldShowDistance, boolean shouldShowExactLocation) {
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

	private SearchType mSearchType = SearchType.MY_LOCATION;

	private String mQuery;
	private Date mCheckInDate;
	private Date mCheckOutDate;
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

	// This will get set if the SearchParams object was created from the widget
	private boolean mIsFromWidget;

	/**
	 *  The variables below are just for analytics
	 *  mUserFreeformLocation is what the user entered for a freeform location.  We may disambiguate or
	 *  figure out a better string when we ultimately do a search.
	 */
	private String mUserQuery;

	public SearchParams() {
		init();
	}

	/**
	 * Creates a new SearchParams object populated with the globally stored defaults from the passed SharedPreferences object.
	 * @param prefs
	 */
	public SearchParams(SharedPreferences prefs) {
		init();
		String searchParamsJson = prefs.getString(SEARCH_PARAMS_KEY, null);
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
		mQuery = null;
		mPropertyIds = new HashSet<String>();
	}

	public void setDefaultStay() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		mCheckInDate = new Date(new GregorianCalendar(year, month, dayOfMonth));
		mCheckOutDate = new Date(new GregorianCalendar(year, month, dayOfMonth + 1));
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
		if (changed && (searchType == SearchType.MY_LOCATION || searchType == SearchType.VISIBLE_MAP_AREA)) {
			clearQuery();
		}
		mSearchType = searchType;
		return changed;
	}

	/**
	 * Sets the location query for this SearchParams object. Also marks the (latitude, longitude)
	 * position as not up to date and clears the regionId. Returns false if the location passed was
	 * the same as before.
	 * @param query
	 * @return
	 */
	public boolean setQuery(String query) {
		if (mQuery != null && mQuery.equals(query)) {
			Log.v("Not resetting freeform location; already searching this spot: " + query);
			return false;
		}

		mQuery = query;
		mSearchLatLonUpToDate = false;
		mRegionId = null;
		return true;
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

	public void fillFromSearch(Search search) {
		setSearchType(SearchType.valueOf(search.getSearchType()));
		setQuery(search.getQuery());
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

	public boolean hasValidCheckInDate() {
		Calendar now = Calendar.getInstance();
		return getCheckInDate().get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
				|| now.before(getCheckInDate());
	}

	public void ensureValidCheckInDate() {
		if (!hasValidCheckInDate()) {
			Log.d("Search params had a checkin date previous to today, resetting checkin/checkout dates.");
			setDefaultStay();
		}
	}

	/**
	 * Ensures that the check in date is not AFTER the check out date. Keeps the
	 * duration of the stay the same.
	 */
	public void setCheckInDate(Date date) {
		if (mCheckInDate != null && mCheckOutDate != null && (date.equals(mCheckOutDate) || date.after(mCheckOutDate))) {
			int stayDuration = getStayDuration();
			Calendar checkout = (Calendar) date.getCalendar().clone();
			checkout.add(Calendar.DAY_OF_MONTH, stayDuration);
			mCheckOutDate = new Date(checkout);
		}

		mCheckInDate = date;
	}

	/**
	 * It is preferable to use the Date version of this method.
	 */
	public void setCheckInDate(Calendar cal) {
		setCheckInDate(new Date(cal));
	}

	public Calendar getCheckInDate() {
		return mCheckInDate.getCalendar();
	}

	/**
	 * Ensures that the check out date is not BEFORE the check in date. Keeps
	 * the duration of the stay the same.
	 */
	public void setCheckOutDate(Date date) {
		if (mCheckInDate != null && mCheckOutDate != null && (date.equals(mCheckInDate) || date.before(mCheckInDate))) {
			int stayDuration = getStayDuration();
			Calendar checkin = (Calendar) date.getCalendar().clone();
			checkin.add(Calendar.DAY_OF_MONTH, -stayDuration);
			mCheckInDate = new Date(checkin);
		}

		mCheckOutDate = date;
	}

	/**
	 * It is preferable to use the Date version of this method.
	 */
	public void setCheckOutDate(Calendar cal) {
		setCheckOutDate(new Date(cal));
	}

	/**
	 * @return the current stay duration in days; 0 if check in or check out not
	 *         specified yet.
	 */
	public int getStayDuration() {
		if (mCheckInDate == null || mCheckOutDate == null) {
			return 0;
		}

		return (int) CalendarUtils.getDaysBetween(mCheckInDate.getCalendar(), mCheckOutDate.getCalendar());
	}

	public Calendar getCheckOutDate() {
		return mCheckOutDate.getCalendar();
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

	public void setFromWidget() {
		mIsFromWidget = true;
	}

	public boolean isFromWidget() {
		return mIsFromWidget;
	}

	public boolean fromJson(JSONObject obj) {
		mQuery = obj.optString("freeformLocation", null);
		mSearchLatLonUpToDate = obj.optBoolean("hasLatLon", false);
		mSearchLatitude = obj.optDouble("latitude", 0);
		mSearchLongitude = obj.optDouble("longitude", 0);

		JSONObject checkinDate = obj.optJSONObject("checkinDate");
		if (checkinDate != null) {
			mCheckInDate = new Date();
			mCheckInDate.fromJson(checkinDate);
		}
		JSONObject checkoutDate = obj.optJSONObject("checkoutDate");
		if (checkoutDate != null) {
			mCheckOutDate = new Date();
			mCheckOutDate.fromJson(checkoutDate);
		}

		mNumAdults = obj.optInt("numAdults", 0);
		mChildren = JSONUtils.getIntList(obj, "children");

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
			obj.put("freeformLocation", mQuery);
			if (mSearchLatLonUpToDate) {
				obj.put("hasLatLon", mSearchLatLonUpToDate);
				obj.put("latitude", mSearchLatitude);
				obj.put("longitude", mSearchLongitude);
			}
			if (mCheckInDate != null) {
				obj.put("checkinDate", mCheckInDate.toJson());
			}
			if (mCheckOutDate != null) {
				obj.put("checkoutDate", mCheckOutDate.toJson());
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
		if (o instanceof SearchParams) {
			SearchParams other = (SearchParams) o;

			// Note that "equals" doesn't mean strictly equals.  In this situation, it means that
			// the two SearchParams objects will result in the equivalent search results.  It does not
			// compare some state variables (such as lat/lon, which are retrieved from the freeform location

			return this.getSearchType().equals(other.getSearchType())
					&& (mQuery != null ? mQuery.equals(other.getQuery()) : true) // mFreeformLocation may be null
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
		editor.putString(SEARCH_PARAMS_KEY, toJson().toString());
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
