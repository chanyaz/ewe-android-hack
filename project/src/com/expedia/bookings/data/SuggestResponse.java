package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.model.Search;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class SuggestResponse extends Response implements JSONable {

	private String mQuery;
	private List<Search> mSuggestions;

	public SuggestResponse() {
	}

	public SuggestResponse(JSONObject obj) {
		this();
		fromJson(obj);
	}
	
	public void setQuery(String query) {
		mQuery = query;
	}
	
	public String getQuery() {
		return mQuery;
	}

	public void setSuggestions(List<Search> suggestions) {
		mSuggestions = suggestions;
	}

	public List<Search> getSuggestions() {
		return mSuggestions;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable interface

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			obj.putOpt("query", mQuery);
			JSONUtils.putJSONableList(obj, "suggestions", mSuggestions);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert SearchResponse to JSON", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mQuery = obj.optString("query", null);
		mSuggestions = (List<Search>) JSONUtils.getJSONableList(obj, "suggestions", Search.class);
		
		return true;
	}
}
