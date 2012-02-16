package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

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

	public String gatherErrorMessage(Context context) {
		if (mErrors == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		for (ServerError error : mErrors) {
			String msg = error.getPresentableMessage(context);
			if (!TextUtils.isEmpty(msg)) {
				builder.append(msg);
				builder.append("\n");
			}
		}

		if (builder.length() == 0) {
			return null;
		}

		return builder.substring(0, builder.length() - 1);
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
