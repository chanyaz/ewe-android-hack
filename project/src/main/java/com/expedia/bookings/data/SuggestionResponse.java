package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

/**
 * Uses the new V2 suggestions; may eventually replace SuggestResponse
 */
public class SuggestionResponse extends Response implements JSONable {

	private String mQuery;

	private List<SuggestionV2> mSuggestions = new ArrayList<SuggestionV2>();

	public void setQuery(String query) {
		mQuery = query;
	}

	public String getQuery() {
		return mQuery;
	}

	public void setSuggestions(List<SuggestionV2> suggestions) {
		mSuggestions = suggestions;
	}

	public List<SuggestionV2> getSuggestions() {
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
		mSuggestions = JSONUtils.getJSONableList(obj, "suggestions", SuggestionV2.class);

		return true;
	}
}
