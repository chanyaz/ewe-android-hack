package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Traveler;
import com.mobiata.android.json.JSONUtils;

public class TripFlight extends TripComponent {

	private FlightTrip mFlightTrip;

	private List<Traveler> mTravelers;

	private List<String> mDestinationImageUrls = new ArrayList<String>();

	public TripFlight() {
		super(Type.FLIGHT);
	}

	public void setFlightTrip(FlightTrip flightTrip) {
		mFlightTrip = flightTrip;
	}

	public FlightTrip getFlightTrip() {
		return mFlightTrip;
	}

	public void addTraveler(Traveler traveler) {
		if (mTravelers == null) {
			mTravelers = new ArrayList<Traveler>();
		}

		mTravelers.add(traveler);
	}

	public List<Traveler> getTravelers() {
		return mTravelers;
	}

	public String getLegDestinationImageUrl(int legNum) {
		if (legNum < mDestinationImageUrls.size()) {
			return mDestinationImageUrls.get(legNum);
		}
		return null;
	}

	public void setLegDestinationImageUrl(int legNum, String destinationImageUrl) {
		if (legNum == mDestinationImageUrls.size()) {
			mDestinationImageUrls.add(destinationImageUrl);
		}
		else {
			while (legNum >= mDestinationImageUrls.size()) {
				mDestinationImageUrls.add("");
			}
			mDestinationImageUrls.set(legNum, destinationImageUrl);
		}
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
			JSONUtils.putJSONable(obj, "flightTrip", mFlightTrip);
			JSONUtils.putJSONableList(obj, "travelers", mTravelers);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightTrip = JSONUtils.getJSONable(obj, "flightTrip", FlightTrip.class);
		mTravelers = JSONUtils.getJSONableList(obj, "travelers", Traveler.class);
		return true;
	}
}
