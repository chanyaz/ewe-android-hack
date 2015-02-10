package com.expedia.bookings.data;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.server.DateTimeParser;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class AirAttach implements JSONable {

	private boolean mAirAttachQualified;
	private DateTime mExpirationDate;

	public AirAttach() {
		// Default constructor for JSONable
	}

	public AirAttach(JSONObject obj) {
		fromJson(obj);
	}

	public boolean isAirAttachQualified() {
		return mAirAttachQualified && mExpirationDate != null;
	}

	public DateTime getExpirationDate() {
		return mExpirationDate;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("airAttachQualified", mAirAttachQualified);
			JodaUtils.putDateTimeInJson(obj, "offerExpires", mExpirationDate);
			return obj;
		}
		catch (JSONException e) {
			Log.e("AirAttach toJson() failed", e);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mAirAttachQualified = obj.optBoolean("airAttachQualified", false);
		mExpirationDate = DateTimeParser.parseDateTime(obj.opt("offerExpires"));
		return true;
	}
}
