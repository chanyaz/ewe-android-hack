package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Response implements JSONable {
	private String mSessionId;
	private List<ServerError> mErrors;

	// Session is just a session ID that can expire
	private Session mSession;

	public String getSessionId() {
		return mSessionId;
	}

	public void setSessionId(String sessionId) {
		this.mSessionId = sessionId;
	}

	public Session getSession() {
		return mSession;
	}

	public void setSession(Session session) {
		mSession = session;
	}

	public void addError(ServerError error) {
		if (mErrors == null) {
			mErrors = new ArrayList<ServerError>();
		}
		mErrors.add(error);
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
			obj.putOpt("sessionId", mSessionId);
			JSONUtils.putJSONable(obj, "session", mSession);
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
		mSessionId = obj.optString("sessionId", null);
		mSession = (Session) JSONUtils.getJSONable(obj, "session", Session.class);
		mErrors = (List<ServerError>) JSONUtils.getJSONableList(obj, "errors", ServerError.class);
		return true;
	}
}
