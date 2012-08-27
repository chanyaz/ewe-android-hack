package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearchLeg implements JSONable {

	private Date mDepartureDate;

	private String mDepartureAirportCode;
	private String mArrivalAirportCode;

	public FlightSearchLeg() {
	}

	public Date getDepartureDate() {
		return mDepartureDate;
	}

	public void setDepartureDate(Date departureDate) {
		mDepartureDate = departureDate;
	}

	public String getDepartureAirportCode() {
		if (TextUtils.isEmpty(mDepartureAirportCode)) {
			// Default return if we haven't had a departure setup yet
			return "MSP";
		}
		return mDepartureAirportCode;
	}

	public void setDepartureAirportCode(String departureAirportCode) {
		mDepartureAirportCode = departureAirportCode;
	}

	public String getArrivalAirportCode() {
		if (TextUtils.isEmpty(mArrivalAirportCode)) {
			// Default return if we haven't had an arrival setup yet
			return "SMF";
		}

		return mArrivalAirportCode;
	}

	public void setArrivalAirportCode(String arrivalAirportCode) {
		mArrivalAirportCode = arrivalAirportCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FlightSearchLeg)) {
			return false;
		}

		FlightSearchLeg other = (FlightSearchLeg) o;

		return ((this.mDepartureDate == null) == (other.mDepartureDate == null))
				&& (this.mDepartureDate == null || this.mDepartureDate.equals(other.mDepartureDate))
				&& ((this.mDepartureAirportCode == null) == (other.mDepartureAirportCode == null))
				&& (this.mDepartureAirportCode == null || this.mDepartureAirportCode
						.equals(other.mDepartureAirportCode))
				&& ((this.mArrivalAirportCode == null) == (other.mArrivalAirportCode == null))
				&& (this.mArrivalAirportCode == null || this.mArrivalAirportCode
						.equals(other.mArrivalAirportCode));
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "departureDate", mDepartureDate);
			obj.putOpt("departureAirportCode", mDepartureAirportCode);
			obj.putOpt("arrivalAirportCode", mArrivalAirportCode);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDepartureDate = JSONUtils.getJSONable(obj, "departureDate", Date.class);
		mDepartureAirportCode = obj.optString("departureAirportCode");
		mArrivalAirportCode = obj.optString("arrivalAirportCode");
		return true;
	}
}
