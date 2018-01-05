package com.mobiata.flightlib.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONable;

public class FlightCode implements JSONable {
	// Version of the data, used for compatibility when changing format
	private static final int VERSION = 3;

	public static final String NO_AIRLINE_CODE = "NO_AIRLINE";

	public String mAirlineCode;
	public String mNumber;

	// This is a special field just for Expedia Flights.  We can sometimes
	// have a flight "operated by" an airline name alone, with no code
	// or number.  In that case, we need to allow for airline names.
	public String mAirlineName;

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass()) {
			return false;
		}

		FlightCode other = (FlightCode) o;

		return (mNumber == null || mNumber.equals(other.mNumber))
				&& (mAirlineCode == null || mAirlineCode.equals(other.mAirlineCode)) && (mAirlineName == null
				|| mAirlineName.equals(other.mAirlineName));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 31 * result + ((mNumber == null) ? 0 : mNumber.hashCode());
		result = 31 * result + ((mAirlineCode == null) ? 0 : mAirlineCode.hashCode());
		result = 31 * result + ((mAirlineName == null) ? 0 : mAirlineName.hashCode());

		return result;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("version", VERSION);
			obj.put("airlineCode", mAirlineCode);
			obj.putOpt("number", mNumber);
			obj.putOpt("airlineName", mAirlineName);
			return obj;
		}
		catch (JSONException e) {
			return null;
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		try {
			int version = obj.optInt("version", 1);
			if (version == 1) {
				if (obj.has("airline")) {
					Airline airline = new Airline();
					airline.fromJson(obj.getJSONObject("airline"));
					mAirlineCode = airline.mAirlineCode;
				}
			}
			else {
				if (obj.has("airlineCode")) {
					mAirlineCode = obj.optString("airlineCode");
				}
			}
			mNumber = obj.optString("number", null);

			mAirlineName = obj.optString("airlineName", null);
			return true;
		}
		catch (JSONException e) {
			return false;
		}
	}
}
