package com.expedia.bookings.data;

import java.text.NumberFormat;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.json.JSONable;

public class Distance implements JSONable, Comparable<Distance> {

	public static enum DistanceUnit {
		MILES, KILOMETERS;

		public static DistanceUnit getDefaultDistanceUnit() {
			if (Locale.getDefault().getCountry().toLowerCase().equals("us")) {
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

	/**
	 * Constructor that calculates the distance based on two coordinates
	 * @param lat1 latitude of start
	 * @param lon1 longitude of start
	 * @param lat2 latitude of end
	 * @param lon2 longitude of end
	 * @param unit the unit to calculate the results in
	 */
	public Distance(double lat1, double lon1, double lat2, double lon2, DistanceUnit unit) {
		mUnit = unit;

		// Calculate the distance
		double miles = MapUtils.getDistance(lat1, lon1, lat2, lon2);
		if (unit == DistanceUnit.MILES) {
			mDistance = miles;
		}
		else {
			mDistance = MapUtils.milesToKilometers(miles);
		}
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

	/**
	 * Sets the unit system for this object.  If convertDistance is true,
	 * it automatically converts the existing distance value for the new
	 * unit system.
	 * @param unit the unit to measure distance in
	 * @param convertDistance if true, converts existing distance
	 */
	public void setUnit(DistanceUnit unit, boolean convertDistance) {
		if (convertDistance) {
			mDistance = getDistance(unit);
		}

		mUnit = unit;
	}

	public DistanceUnit getUnit() {
		return mUnit;
	}

	public String formatDistance(Context context) {
		return formatDistance(context, mUnit);
	}

	public String formatDistance(Context context, DistanceUnit distanceUnit) {
		double distance = getDistance(distanceUnit);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		int unitStrId = (distanceUnit == DistanceUnit.KILOMETERS) ? R.string.unit_kilometers : R.string.unit_miles;
		return context.getString(R.string.distance_template, nf.format(distance), context.getString(unitStrId));
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
	public int compareTo(Distance other) {
		double myMiles = (mUnit == DistanceUnit.KILOMETERS) ? MapUtils.kilometersToMiles(mDistance) : mDistance;
		double otherMiles = (other.getUnit() == DistanceUnit.KILOMETERS) ? MapUtils.kilometersToMiles(other
				.getDistance()) : other.getDistance();

		if (myMiles > otherMiles) {
			return 1;
		}
		else if (myMiles == otherMiles) {
			return 0;
		}
		return -1;
	}
}
