package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.mobiata.android.json.JSONUtils;

public class TripFlight extends TripComponent {

	private FlightTrip mFlightTrip;

	private List<Traveler> mTravelers;

	private List<FlightConfirmation> mConfirmations = new ArrayList<FlightConfirmation>();

	private String mDestinationRegionId;

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

	public void addConfirmation(FlightConfirmation confirmation) {
		mConfirmations.add(confirmation);
	}

	public List<FlightConfirmation> getConfirmations() {
		return mConfirmations;
	}

	public List<ChildTraveler> getChildTravelers() {
		List<ChildTraveler> childTravelers = new ArrayList<>();
		for (Traveler traveler : mTravelers) {
			if (traveler.getAge() <= GuestsPickerUtils.MAX_CHILD_AGE) {
				ChildTraveler childTraveler = new ChildTraveler();
				childTraveler.setAge(traveler.getAge());
				childTravelers.add(childTraveler);
			}
		}
		return childTravelers;
	}

	public String getDestinationRegionId() {
		return mDestinationRegionId;
	}

	public void setDestinationRegionId(String destinationRegionId) {
		mDestinationRegionId = destinationRegionId;
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
			JSONUtils.putJSONableList(obj, "confirmationNumbers", mConfirmations);
			obj.put("destinationRegionId", mDestinationRegionId);
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
		mConfirmations = JSONUtils.getJSONableList(obj, "confirmationNumbers", FlightConfirmation.class);
		mDestinationRegionId = obj.optString("destinationRegionId", "");
		return true;
	}
}
