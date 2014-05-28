package com.expedia.bookings.gear;

import org.json.JSONObject;

import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripResponse;

import java.util.TimeZone;

public class GearResponse {

	protected TripComponent tripComponent;

	protected JSONObject responseForGear;

	public void setTripComponent(TripComponent tripComponent) {
		this.tripComponent = tripComponent;
	}

	public JSONObject getResponseForGear() {
		return responseForGear;
	}

}