package com.expedia.bookings.model;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

@Table(name = "Searches")
public class Search extends Model implements JSONable {

	public Search() {
		super();
	}

	public Search(SearchParams searchParams) {
		super();

		if (searchParams.hasQuery()) {
			mQuery = searchParams.getQuery().trim();
		}
		if (searchParams.hasSearchLatLon()) {
			mLatitude = searchParams.getSearchLatitude();
			mLongitude = searchParams.getSearchLongitude();
		}
		if (searchParams.hasRegionId()) {
			mRegionId = searchParams.getRegionId();
		}
		mSearchType = searchParams.getSearchType().name();
	}

	// This column is named FreeFormLocation for legacy sake
	@Column(name = "FreeFormLocation")
	private String mQuery;

	@Column(name = "Latitude")
	private Double mLatitude;

	@Column(name = "Longitude")
	private Double mLongitude;

	@Column(name = "RegionId")
	private String mRegionId;

	@Column(name = "SearchType")
	private String mSearchType;

	public String getQuery() {
		return mQuery;
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

	public String getSearchType() {
		// TODO: This if block is for legacy searches. Eventually we shouldn't need it.
		if (TextUtils.isEmpty(mSearchType)) {
			return "FREEFORM";
		}

		return mSearchType;
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
		if (TextUtils.isEmpty(searchParams.getQuery())) {
			return;
		}

		delete(context, searchParams);
		new Search(searchParams).save();
	}

	public static void delete(Context context, SearchParams searchParams) {
		Search.delete(Search.class, "lower(FreeFormLocation) = ?", searchParams.getQuery().toLowerCase()
				.trim());
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("freeformLocation", mQuery);
			obj.putOpt("latitude", mLatitude);
			obj.putOpt("longitude", mLongitude);
			obj.putOpt("regionId", mRegionId);
			obj.putOpt("searchType", mSearchType);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert BillingInfo object to JSON.", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mQuery = obj.optString("freeformLocation", null);
		if (obj.has("latitude") && obj.has("longitude")) {
			mLatitude = obj.optDouble("latitude");
			mLongitude = obj.optDouble("longitude");
		}
		mRegionId = obj.optString("regionId", null);
		mSearchType = obj.optString("searchType", "FREEFORM");

		return true;
	}
}
