package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class SamsungWalletResponse extends Response implements JSONable {

	private String mTicketId;

	@Override
	public boolean isSuccess() {
		return !hasErrors();
	}

	public void setTicketId(String ticketId) {
		mTicketId = ticketId;
	}

	public String getTicketId() {
		return mTicketId;
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
			obj.put("ticketId", mTicketId);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert SamsungWalletResponse to JSON", e);
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);

		mTicketId = obj.optString("ticketId", null);
		return true;
	}
}
