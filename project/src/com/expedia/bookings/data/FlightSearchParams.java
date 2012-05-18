package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearchParams implements JSONable {

	private int mAdults;
	private List<Integer> mChildren;
	private List<FlightSearchLeg> mQueryLegs;

	public FlightSearchParams() {
		mChildren = new ArrayList<Integer>();
		mQueryLegs = new ArrayList<FlightSearchLeg>();

		reset();
	}

	public void reset() {
		mAdults = 1;
		mChildren.clear();
		mQueryLegs.clear();

		// By default, we always have at least one flight search leg
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
	// Not strictly necessary, but these make the simple cases (one-way and
	// round trip) easier to handle.

	public boolean isSimpleFlight() {
		return mQueryLegs.size() <= 2;
	}

	public boolean isRoundTrip() {
		return mQueryLegs.size() == 2;
	}

	public void setDepartureDate(Date departureDate) {
		mQueryLegs.get(0).setDepartureDate(departureDate);
	}

	public Date getDepartureDate() {
		return mQueryLegs.get(0).getDepartureDate();
	}

	public void setReturnDate(Date returnDate) {
		ensureRoundTripData();

		mQueryLegs.get(1).setDepartureDate(returnDate);
	}

	public Date getReturnDate() {
		ensureRoundTripData();

		return mQueryLegs.get(1).getDepartureDate();
	}

	public void setDepartureAirportCode(String airportCode) {
		mQueryLegs.get(0).setDepartureAirportCode(airportCode);

		if (isRoundTrip()) {
			mQueryLegs.get(1).setArrivalAirportCode(airportCode);
		}
	}

	public String getDepartureAirportCode() {
		return mQueryLegs.get(0).getDepartureAirportCode();
	}

	public void setArrivalAirportCode(String airportCode) {
		mQueryLegs.get(0).setArrivalAirportCode(airportCode);

		if (isRoundTrip()) {
			mQueryLegs.get(1).setDepartureAirportCode(airportCode);
		}
	}

	public String getArrivalAirportCode() {
		return mQueryLegs.get(0).getArrivalAirportCode();
	}

	// If we want this to be a round trip flight, ensures that we have round trip data
	private void ensureRoundTripData() {
		if (!isRoundTrip()) {
			FlightSearchLeg departureLeg = new FlightSearchLeg();
			FlightSearchLeg returnLeg = new FlightSearchLeg();

			returnLeg.setDepartureAirportCode(departureLeg.getArrivalAirportCode());
			returnLeg.setArrivalAirportCode(departureLeg.getDepartureAirportCode());

			mQueryLegs.add(returnLeg);
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
