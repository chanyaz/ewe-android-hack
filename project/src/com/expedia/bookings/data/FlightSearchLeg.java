package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearchLeg implements JSONable {

	private Date mDepartureDate;

	private Location mDepartureLocation;
	private Location mArrivalLocation;

	public FlightSearchLeg() {
	}

	public Date getDepartureDate() {
		return mDepartureDate;
	}

	public void setDepartureDate(Date departureDate) {
		mDepartureDate = departureDate;
	}

	public Location getDepartureLocation() {
		return mDepartureLocation;
	}

	public void setDepartureLocation(Location departureAirportCode) {
		mDepartureLocation = departureAirportCode;
	}

	public Location getArrivalLocation() {
		return mArrivalLocation;
	}

	public void setArrivalLocation(Location arrivalAirportCode) {
		mArrivalLocation = arrivalAirportCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FlightSearchLeg)) {
			return false;
		}

		FlightSearchLeg other = (FlightSearchLeg) o;

		return ((this.mDepartureDate == null) == (other.mDepartureDate == null))
				&& (this.mDepartureDate == null || this.mDepartureDate.equals(other.mDepartureDate))
				&& ((this.mDepartureLocation == null) == (other.mDepartureLocation == null))
				&& (this.mDepartureLocation == null || this.mDepartureLocation
						.equals(other.mDepartureLocation))
				&& ((this.mArrivalLocation == null) == (other.mArrivalLocation == null))
				&& (this.mArrivalLocation == null || this.mArrivalLocation
						.equals(other.mArrivalLocation));
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putJSONable(obj, "departureDate", mDepartureDate);
			JSONUtils.putJSONable(obj, "departureLocation", mDepartureLocation);
			JSONUtils.putJSONable(obj, "arrivalLocation", mArrivalLocation);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mDepartureDate = JSONUtils.getJSONable(obj, "departureDate", Date.class);
		mDepartureLocation = JSONUtils.getJSONable(obj, "departureLocation", Location.class);
		mArrivalLocation = JSONUtils.getJSONable(obj, "arrivalLocation", Location.class);
		return true;
	}
}
