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
			JodaUtils.putDateTimeInJson(obj, "jodaOfferExpiresObj", mExpirationDate);
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

		if (obj.has("jodaOfferExpiresObj")) {
			mExpirationDate = JodaUtils.getDateTimeFromJsonBackCompat(obj, "jodaOfferExpiresObj", "");
		}
		else if (obj.has("offerExpires")) {
			JSONObject offerExpires = obj.optJSONObject("offerExpires");
			mExpirationDate = DateTimeParser.parseDateTime(offerExpires);
		}

		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AirAttach airAttach = (AirAttach) o;

		if (mAirAttachQualified != airAttach.mAirAttachQualified) {
			return false;
		}
		return mExpirationDate != null ? mExpirationDate.equals(airAttach.mExpirationDate)
			: airAttach.mExpirationDate == null;

	}

	@Override
	public int hashCode() {
		int result = (mAirAttachQualified ? 1 : 0);
		result = 31 * result + (mExpirationDate != null ? mExpirationDate.hashCode() : 0);
		return result;
	}
}
