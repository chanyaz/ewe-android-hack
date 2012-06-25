package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearchParams implements JSONable {

	private int mAdults;
	private List<Integer> mChildren;
	private List<FlightSearchLeg> mQueryLegs;

	private boolean mIsRoundTripMode = true; //TODO: remove init once necessary

	public FlightSearchParams() {
		mChildren = new ArrayList<Integer>();
		mQueryLegs = new ArrayList<FlightSearchLeg>();

		reset();
	}

	public void reset() {
		mAdults = 1;
		mChildren.clear();
		mQueryLegs.clear();

		// By default, have two FlightSearchLegs, must modify when we support multi-leg
		mQueryLegs.add(new FlightSearchLeg());
		mQueryLegs.add(new FlightSearchLeg());
	}

	public void setNumAdults(int numAdults) {
		mAdults = numAdults;
	}

	public int getNumAdults() {
		return mAdults;
	}

	public void setChildren(List<Integer> childAges) {
		if (childAges != null) {
			mChildren = childAges;
		}
	}

	public int getNumChildren() {
		return mChildren.size();
	}

	public void addQueryLeg(FlightSearchLeg queryLeg) {
		mQueryLegs.add(queryLeg);
	}

	public List<FlightSearchLeg> getQueryLegs() {
		return mQueryLegs;
	}

	public int getQueryLegCount() {
		return mQueryLegs.size();
	}

	public FlightSearchLeg getQueryLeg(int position) {
		return mQueryLegs.get(position);
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility methods
	//

	public boolean isRoundTripComplete() {
		if (mQueryLegs.get(0).isComplete() && mQueryLegs.get(1).isComplete()) {
			return true;
		}
		return false;
	}

	public void setDepartureDate(Date departureDate) {
		mQueryLegs.get(0).setDepartureDate(departureDate);
	}

	public Date getDepartureDate() {
		return mQueryLegs.get(0).getDepartureDate();
	}

	public void setReturnDate(Date returnDate) {
		mQueryLegs.get(1).setDepartureDate(returnDate);
	}

	public Date getReturnDate() {
		return mQueryLegs.get(1).getDepartureDate();
	}

	public void setDepartureAirportCode(String airportCode) {
		mQueryLegs.get(0).setDepartureAirportCode(airportCode);

		// set up the return leg arrival airport code in anticipation
		if (mIsRoundTripMode) {
			mQueryLegs.get(1).setArrivalAirportCode(airportCode);
		}
	}

	public String getDepartureAirportCode() {
		return mQueryLegs.get(0).getDepartureAirportCode();
	}

	public void setArrivalAirportCode(String airportCode) {
		mQueryLegs.get(0).setArrivalAirportCode(airportCode);

		// set up the return leg departure airport code in anticipation
		if (mIsRoundTripMode) {
			mQueryLegs.get(1).setDepartureAirportCode(airportCode);
		}
	}

	public String getArrivalAirportCode() {
		return mQueryLegs.get(0).getArrivalAirportCode();
	}

	public void setRoundTripMode(boolean isRoundTripMode) {
		mIsRoundTripMode = isRoundTripMode;

		if (isRoundTripMode) {
			//TODO: fill in the round trip details by inspecting
		}
		else {
			//TODO: make sure to clear out the round-trip specific code
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("adults", mAdults);
			JSONUtils.putIntList(obj, "children", mChildren);
			JSONUtils.putJSONableList(obj, "queryLegs", mQueryLegs);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean fromJson(JSONObject obj) {
		mAdults = obj.optInt("adults");
		mChildren = JSONUtils.getIntList(obj, "children");
		mQueryLegs = (List<FlightSearchLeg>) JSONUtils.getJSONableList(obj, "queryLegs", FlightSearchLeg.class);
		return true;
	}

}
