package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSegmentAttributes implements JSONable {

	public enum CabinCode {
		COACH(R.string.cabin_code_coach),
		PREMIUM_COACH(R.string.cabin_code_premium_coach),
		BUSINESS(R.string.cabin_code_business),
		FIRST(R.string.cabin_code_first),
		MIXED(R.string.cabin_code_mixed);

		private int mResId;

		private CabinCode(int resId) {
			mResId = resId;
		}

		public int getResId() {
			return mResId;
		}
	}

	private char mBookingCode;
	private CabinCode mCabinCode;

	public FlightSegmentAttributes() {
		// Default constructor for JSONable
	}

	public FlightSegmentAttributes(char bookingCode, CabinCode cabinCode) {
		mBookingCode = bookingCode;
		mCabinCode = cabinCode;
	}

	public char getBookingCode() {
		return mBookingCode;
	}

	public CabinCode getCabinCode() {
		return mCabinCode;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	private static final String KEY_BOOKING_CODE = "a";
	private static final String KEY_CABIN_CODE = "b";

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt(KEY_BOOKING_CODE, (int) mBookingCode);
			JSONUtils.putEnum(obj, KEY_CABIN_CODE, mCabinCode);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		// Uses old version; can be phased out eventually
		if (obj.has("bookingCode")) {
			mBookingCode = (char) obj.optInt("bookingCode");
			mCabinCode = JSONUtils.getEnum(obj, "cabinCode", CabinCode.class);
		}
		else {
			mBookingCode = (char) obj.optInt(KEY_BOOKING_CODE);
			mCabinCode = JSONUtils.getEnum(obj, KEY_CABIN_CODE, CabinCode.class);
		}

		return true;
	}
}
