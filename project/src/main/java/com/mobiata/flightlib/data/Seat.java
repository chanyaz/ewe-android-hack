package com.mobiata.flightlib.data;

import org.json.JSONException;
import org.json.JSONObject;


import com.mobiata.android.json.JSONable;

public class Seat implements JSONable {

	private String assigned = null;
	private String passenger = null;

	public Seat() {

	}

	public Seat(String assigned, String passenger) {
		this.assigned = assigned;
		this.passenger = passenger;
	}

	public Seat(String assigned) {
		this.assigned = assigned;
	}

	public String getAssigned() {
		return assigned;
	}

	public void setAssigned(String assigned) {
		this.assigned = assigned;
	}

	public String getPassenger() {
		return passenger;
	}

	public void setPassenger(String passenger) {
		this.passenger = passenger;
	}


	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("assigned", assigned);
			obj.putOpt("passengerName", passenger);
			return obj;
		}
		catch (JSONException e) {
			return null;
		}

	}

	@Override
	public boolean fromJson(JSONObject obj) {
		assigned = obj.optString("assigned", null);
		passenger = obj.optString("passengerName", null);
		return true;
	}
}
