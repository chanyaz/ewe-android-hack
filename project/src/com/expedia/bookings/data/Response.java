package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Response implements JSONable {
	private List<ServerError> mErrors;

	public boolean isSuccess() {
		return !hasErrors();
	}

	public void addError(ServerError error) {
		if (error == null) {
			return;
		}
		if (mErrors == null) {
			mErrors = new ArrayList<ServerError>();
		}
		mErrors.add(error);
	}

	public void addErrors(List<ServerError> errors) {
		if (errors == null) {
			return;
		}
		for (ServerError error : errors) {
			addError(error);
		}
	}

	public List<ServerError> getErrors() {
		return mErrors;
	}

	public boolean hasErrors() {
		if (mErrors == null) {
			return false;
		}
		return (mErrors.size() > 0);
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "errors", mErrors);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Response to JSON", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {
		mErrors = (List<ServerError>) JSONUtils.getJSONableList(obj, "errors", ServerError.class);
		return true;
	}
}
