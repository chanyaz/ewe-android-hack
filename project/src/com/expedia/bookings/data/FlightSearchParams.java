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

	public FlightSearchParams() {
		mChildren = new ArrayList<Integer>();
		mQueryLegs = new ArrayList<FlightSearchLeg>();

		reset();
	}

	public void reset() {
		mAdults = 1;
		mChildren.clear();
		mQueryLegs.clear();

		// Must have at least one query leg (though it won't have all the details filled in initially)
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

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FlightSearchParams)) {
			return false;
		}

		FlightSearchParams other = (FlightSearchParams) o;

		return this.mAdults == other.mAdults && this.mChildren.equals(other.mChildren)
				&& this.mQueryLegs.equals(other.mQueryLegs);
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility methods
	//

	public boolean isRoundTrip() {
		return mQueryLegs.size() == 2;
	}

	public void setDepartureDate(Date departureDate) {
		mQueryLegs.get(0).setDepartureDate(departureDate);
	}

	public Date getDepartureDate() {
		return mQueryLegs.get(0).getDepartureDate();
	}

	// FOR DEBUG PURPOSES ONLY - GET RID OF THIS EVENTUALLY AND REPLACE WITH getDepartureDate()
	public Date getDepartureDateWithDefault() {
		Date depDate = mQueryLegs.get(0).getDepartureDate();
		if (depDate == null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			depDate = new com.expedia.bookings.data.Date(cal);
		}
		return depDate;
	}

	public void setReturnDate(Date returnDate) {
		if (returnDate != null) {
			ensureRoundTripData();
			mQueryLegs.get(1).setDepartureDate(returnDate);
		}
		else {
			// If we're nulling the return date, delete the return flight leg
			if (mQueryLegs.size() > 1) {
				mQueryLegs.remove(1);
			}
		}
	}

	public Date getReturnDate() {
		if (isRoundTrip()) {
			return mQueryLegs.get(1).getDepartureDate();
		}
		else {
			return null;
		}
	}

	public void setDepartureAirportCode(String airportCode) {
		mQueryLegs.get(0).setDepartureAirportCode(airportCode);

		// set up the return leg arrival airport code in anticipation
		if (isRoundTrip()) {
			mQueryLegs.get(1).setArrivalAirportCode(airportCode);
		}
	}

	public String getDepartureAirportCode() {
		return mQueryLegs.get(0).getDepartureAirportCode();
	}

	public void setArrivalAirportCode(String airportCode) {
		mQueryLegs.get(0).setArrivalAirportCode(airportCode);

		// set up the return leg departure airport code in anticipation
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
			FlightSearchLeg departureLeg = mQueryLegs.get(0);
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

	@Override
	public boolean fromJson(JSONObject obj) {
		mAdults = obj.optInt("adults");
		mChildren = JSONUtils.getIntList(obj, "children");
		mQueryLegs = (List<FlightSearchLeg>) JSONUtils.getJSONableList(obj, "queryLegs", FlightSearchLeg.class);
		return true;
	}

}
