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
		FIRST(R.string.cabin_code_first);

		private int mResId;

		private CabinCode(int resId) {
			mResId = resId;
		}

		public int getResId() {
			return mResId;
		}
	}

	private String mBookingCode;
	private CabinCode mCabinCode;
	
	public FlightSegmentAttributes() {
		// Default constructor for JSONable
	}

	public FlightSegmentAttributes(String bookingCode, CabinCode cabinCode) {
		mBookingCode = bookingCode;
		mCabinCode = cabinCode;
	}

	public String getBookingCode() {
		return mBookingCode;
	}

	public CabinCode getCabinCode() {
		return mCabinCode;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("bookingCode", mBookingCode);
			JSONUtils.putEnum(obj, "cabinCode", mCabinCode);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mBookingCode = obj.optString("bookingCode");
		mCabinCode = JSONUtils.getEnum(obj, "cabinCode", CabinCode.class);
		return true;
	}
}
