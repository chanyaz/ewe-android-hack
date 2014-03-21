package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Response implements JSONable {
	private List<ServerError> mErrors;

	// Represents roughly when the response was created.  Should be used as a
	// general guideline, but not an exact figure.
	private DateTime mTimestamp = DateTime.now();

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

	public void addErrorToFront(ServerError error) {
		if (error == null) {
			return;
		}
		if (mErrors == null) {
			mErrors = new ArrayList<ServerError>();
		}
		mErrors.add(0, error);
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

		TreeSet<String> errorStrings = new TreeSet<String>();
		for (ServerError error : mErrors) {
			String msg = error.getPresentableMessage(context);
			if (!TextUtils.isEmpty(msg)) {
				errorStrings.add(msg);
			}
		}

		if (errorStrings.size() == 0) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		for (String msg : errorStrings) {
			builder.append(msg);
			builder.append("\n");
		}

		return builder.substring(0, builder.length() - 1);
	}

	public DateTime getTimestamp() {
		return mTimestamp;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONableList(obj, "errors", mErrors);
			JodaUtils.putDateTimeInJson(obj, "timestampV2", mTimestamp);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Response to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mErrors = JSONUtils.getJSONableList(obj, "errors", ServerError.class);
		if (obj.has("timestamp")) {
			mTimestamp = new DateTime(obj.optLong("timestamp"));
		}
		else {
			mTimestamp = JodaUtils.getDateTimeFromJsonBackCompat(obj, "timestampV2", null);
		}
		return true;
	}
}
