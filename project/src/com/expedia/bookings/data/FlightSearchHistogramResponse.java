package com.expedia.bookings.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearchHistogramResponse extends Response implements JSONable {

	private List<FlightHistogram> mFlightHistograms;

	public void setFlightHistograms(List<FlightHistogram> flightHistograms) {
		mFlightHistograms = flightHistograms;
	}

	public List<FlightHistogram> getFlightHistograms() {
		return mFlightHistograms;
	}

	//////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONableList(obj, "histograms", mFlightHistograms);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException("Unable to FlightSearchHistogramResponse.toJson()");
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightHistograms = JSONUtils.getJSONableList(obj, "histograms", FlightHistogram.class);
		return true;
	}

}
