package com.expedia.bookings.data;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.maps.MapUtils;

public class Distance implements JSONable, Comparable<Distance> {

	public enum DistanceUnit {
		MILES, KILOMETERS;

		public static DistanceUnit getDefaultDistanceUnit() {
			if (Locale.getDefault().getCountry().toLowerCase(Locale.ENGLISH).equals("us")) {
				return DistanceUnit.MILES;
			}
			return DistanceUnit.KILOMETERS;
		}
	}

	private double mDistance;
	private DistanceUnit mUnit;

	public Distance() {
		// default constructor
	}

	public Distance(double distance, DistanceUnit unit) {
		mDistance = distance;
		mUnit = unit;
	}

	public Distance(JSONObject obj) {
		fromJson(obj);
	}

	public void setDistance(double distance) {
		mDistance = distance;
	}

	public double getDistance() {
		return mDistance;
	}

	public double getDistance(DistanceUnit distanceUnit) {
		if (distanceUnit != mUnit) {
			if (distanceUnit == DistanceUnit.MILES) {
				return MapUtils.kilometersToMiles(mDistance);
			}
			else {
				return MapUtils.milesToKilometers(mDistance);
			}
		}
		else {
			return mDistance;
		}
	}

	public DistanceUnit getUnit() {
		return mUnit;
	}

	public boolean fromJson(JSONObject obj) {
		mDistance = obj.optDouble("distance", 0);
		if (obj.has("unit")) {
			mUnit = DistanceUnit.valueOf(obj.optString("unit"));
		}
		return true;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("distance", mDistance);
			obj.putOpt("unit", mUnit);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Distance object to JSON.", e);
			return null;
		}
	}

	@Override
	public int compareTo(@NonNull Distance other) {
		double myMiles = getDistance(DistanceUnit.MILES);
		double otherMiles = other.getDistance(DistanceUnit.MILES);

		if (myMiles > otherMiles) {
			return 1;
		}
		else if (myMiles == otherMiles) {
			return 0;
		}
		return -1;
	}
}
