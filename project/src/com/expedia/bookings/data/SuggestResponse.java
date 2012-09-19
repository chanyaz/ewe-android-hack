package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class SuggestResponse extends Response implements JSONable {

	private List<Suggestion> mSuggestions = new ArrayList<Suggestion>();

	public SuggestResponse() {
		// Default constructor for JSONable
	}

	public SuggestResponse(JSONObject obj) {
		this();
		fromJson(obj);
	}

	public void addSuggestion(Suggestion suggestion) {
		mSuggestions.add(suggestion);
	}

	public List<Suggestion> getSuggestions() {
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
			JSONUtils.putJSONableList(obj, "suggestions", mSuggestions);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert SearchResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mSuggestions = JSONUtils.getJSONableList(obj, "suggestions", Suggestion.class);

		return true;
	}
}
