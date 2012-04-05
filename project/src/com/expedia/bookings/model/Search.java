package com.expedia.bookings.model;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

@Table(name = "Searches")
public class Search extends Model implements JSONable {

	public Search() {
		super();
	}

	public Search(SearchParams searchParams) {
		super();

		if (searchParams.hasFreeformLocation()) {
			mFreeformLocation = searchParams.getFreeformLocation().trim();
		}
		if (searchParams.hasSearchLatLon()) {
			mLatitude = searchParams.getSearchLatitude();
			mLongitude = searchParams.getSearchLongitude();
		}
		if (searchParams.hasRegionId()) {
			mRegionId = searchParams.getRegionId();
		}
	}

	@Column(name = "FreeFormLocation")
	private String mFreeformLocation;

	@Column(name = "Latitude")
	private Double mLatitude;

	@Column(name = "Longitude")
	private Double mLongitude;

	@Column(name = "RegionId")
	private String mRegionId;

	public String getFreeformLocation() {
		return mFreeformLocation;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public String getRegionId() {
		return mRegionId;
	}

	public boolean hasLatLng() {
		return mLatitude != null && mLongitude != null;
	}

	public static List<Search> getAllSearchParams(Context context) {
		List<Search> searches = new Select().from(Search.class).orderBy("Id DESC").execute();
		return searches;
	}

	public static List<Search> getRecentSearches(Context context, int maxSearches) {
		List<Search> searches = new Select().from(Search.class).orderBy("Id DESC").limit(maxSearches + "").execute();
		return searches;
	}

	public static void add(Context context, SearchParams searchParams) {
		if (searchParams.getSearchType() != SearchType.FREEFORM || searchParams.getFreeformLocation() == null
				|| searchParams.getFreeformLocation().length() == 0) {
			return;
		}

		Search.delete(Search.class, "lower(FreeFormLocation) = " + "\""
				+ searchParams.getFreeformLocation().toLowerCase().trim() + "\"");

		new Search(searchParams).save();
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("freeformLocation", mFreeformLocation);
			obj.putOpt("latitude", mLatitude);
			obj.putOpt("longitude", mLongitude);
			obj.putOpt("regionId", mRegionId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert BillingInfo object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mFreeformLocation = obj.optString("freeformLocation", null);
		if (obj.has("latitude") && obj.has("longitude")) {
			mLatitude = obj.optDouble("latitude");
			mLongitude = obj.optDouble("longitude");
		}
		mRegionId = obj.optString("regionId", null);

		return true;
	}
}
