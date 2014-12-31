package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class FlightStatsRating implements JSONable {

	public static final double INVALID_PERCENT = -1.0d;

	private double mOnTimePercent = INVALID_PERCENT;

	private int mNumObservations;

	public void setOnTimePercent(double onTimePercent) {
		mOnTimePercent = onTimePercent;
	}

	public double getOnTimePercent() {
		return mOnTimePercent;
	}

	public String getFormattedPercent() {
		return Long.toString(Math.round(mOnTimePercent * 100));
	}

	public int getNumObservations() {
		return mNumObservations;
	}

	public void setNumObservations(int numObservations) {
		mNumObservations = numObservations;
	}

	public boolean hasValidPercent() {
		return mNumObservations > 20 && mOnTimePercent != INVALID_PERCENT;
	}

	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("percent", mOnTimePercent);
			json.putOpt("observations", mNumObservations);
			return json;
		}
		catch (JSONException e) {
			Log.e("Failed FlightStatsRating.toJson()");
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mOnTimePercent = obj.optDouble("percent");
		mNumObservations = obj.optInt("observations");
		return true;
	}

}
