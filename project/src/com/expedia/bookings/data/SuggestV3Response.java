package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class SuggestV3Response extends Response implements JSONable {

	private String mQuery;

	private List<SuggestionV3> mSuggestions = new ArrayList<SuggestionV3>();

	public void setQuery(String query) {
		mQuery = query;
	}

	public String getQuery() {
		return mQuery;
	}

	public void setSuggestions(List<SuggestionV3> suggestions) {
		mSuggestions = suggestions;
	}

	public List<SuggestionV3> getSuggestions() {
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
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mQuery = obj.optString("query", null);
		mSuggestions = JSONUtils.getJSONableList(obj, "suggestions", SuggestionV3.class);

		return true;
	}
}
