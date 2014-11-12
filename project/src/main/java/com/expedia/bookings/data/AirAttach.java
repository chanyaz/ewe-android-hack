package com.expedia.bookings.data;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class AirAttach implements JSONable {

	private boolean mAirAttachQualified;
	private DateTime mExpirationDate;

	public boolean isAirAttachQualified() {
		return mAirAttachQualified;
	}

	public void setAirAttachQualified(boolean airAttachQualified) {
		mAirAttachQualified = airAttachQualified;
	}

	public DateTime getExpirationDate() {
		return mExpirationDate;
	}

	public void setExpirationDate(DateTime expirationDate) {
		mExpirationDate = expirationDate;
	}

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("airAttachQualified", mAirAttachQualified);
			JodaUtils.putDateTimeInJson(obj, "expirationDate", mExpirationDate);
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
		mExpirationDate = JodaUtils.getDateTimeFromJsonBackCompat(obj, "expirationDate", "");
		return true;
	}
}
