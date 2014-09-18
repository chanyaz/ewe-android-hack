package com.expedia.bookings.data.trips;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Car;
import com.mobiata.android.json.JSONUtils;

public class TripCar extends TripComponent {

	private Car mCar;

	public TripCar() {
		super(Type.CAR);
	}

	public void setCar(Car car) {
		mCar = car;
	}

	public Car getCar() {
		return mCar;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "car", mCar);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mCar = JSONUtils.getJSONable(obj, "car", Car.class);
		return true;
	}
}
