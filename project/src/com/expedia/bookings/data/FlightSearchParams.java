package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class FlightSearchParams implements JSONable {

	private int mAdults;
	private List<ChildTraveler> mChildren;
	private List<FlightSearchLeg> mQueryLegs;

	public FlightSearchParams() {
		mChildren = new ArrayList<ChildTraveler>();
		mQueryLegs = new ArrayList<FlightSearchLeg>();

		reset();
	}

	public FlightSearchParams(FlightSearchParams params) {
		fromJson(params.toJson());
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

	public void setChildren(List<ChildTraveler> children) {
		if (children != null) {
			mChildren = children;
		}
	}

	public List<ChildTraveler> getChildren() {
		if (mChildren == null) {
			mChildren = new ArrayList<ChildTraveler>();
		}

		return mChildren;
	}

	public int getNumTravelers() {
		return getNumAdults() + getNumChildren();
	}

	public int getNumChildren() {
		return mChildren == null ? 0 : mChildren.size();
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

	/**
	 * Ensures we have a valid search date entered.  If we do not, we simply
	 * clear out the dates entered
	 * @return
	 */
	public void ensureValidDates() {
		LocalDate start = getDepartureDate();
		if (start == null || start.isBefore(LocalDate.now())) {
			Log.i("Search dates are invalid, resetting.");
			setDepartureDate(null);
			setReturnDate(null);
		}
	}

	/**
	 * @return true if we can do a search with the data we currently have
	 */
	public boolean isFilled() {
		return getDepartureDate() != null && getDepartureLocation() != null && getArrivalLocation() != null;
	}

	/**
	 * @return true if the departure and arrival airports differ
	 */
	public boolean hasDifferentAirports() {
		if (!isFilled()) {
			return false;
		}

		Location depLoc = getDepartureLocation();
		Location arrLoc = getArrivalLocation();
		return !depLoc.getDestinationId().equalsIgnoreCase(arrLoc.getDestinationId());
	}

	public boolean isRoundTrip() {
		return mQueryLegs.size() == 2;
	}

	private static final String INDIA_COUNTRY_CODE = "IND";

	public boolean blockIndiaDomesticFlightSearch() {
		if (PointOfSale.getPointOfSale().blockDomesticFlightSearch()) {
			for (FlightSearchLeg leg : mQueryLegs) {
				String dep = leg.getDepartureLocation().getCountryCode();
				String arr = leg.getArrivalLocation().getCountryCode();

				if (INDIA_COUNTRY_CODE.equals(dep) && INDIA_COUNTRY_CODE.equals(arr)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setDepartureDate(LocalDate departureDate) {
		mQueryLegs.get(0).setDepartureDate(departureDate);
	}

	public LocalDate getDepartureDate() {
		return mQueryLegs.get(0).getDepartureDate();
	}

	public void setReturnDate(LocalDate returnDate) {
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

	public LocalDate getReturnDate() {
		if (isRoundTrip()) {
			return mQueryLegs.get(1).getDepartureDate();
		}
		else {
			return null;
		}
	}

	public void setDepartureLocation(Location location) {
		mQueryLegs.get(0).setDepartureLocation(location);

		// set up the return leg arrival airport code in anticipation
		if (isRoundTrip()) {
			mQueryLegs.get(1).setArrivalLocation(location);
		}
	}

	public Location getDepartureLocation() {
		return mQueryLegs.get(0).getDepartureLocation();
	}

	public void setArrivalLocation(Location location) {
		mQueryLegs.get(0).setArrivalLocation(location);

		// set up the return leg departure airport code in anticipation
		if (isRoundTrip()) {
			mQueryLegs.get(1).setDepartureLocation(location);
		}
	}

	public Location getArrivalLocation() {
		return mQueryLegs.get(0).getArrivalLocation();
	}

	public Location getLocation(int legPos, boolean departureLocation) {
		return mQueryLegs.get(legPos).getLocation(departureLocation);
	}

	// If we want this to be a round trip flight, ensures that we have round trip data
	private void ensureRoundTripData() {
		if (!isRoundTrip()) {
			FlightSearchLeg departureLeg = mQueryLegs.get(0);
			FlightSearchLeg returnLeg = new FlightSearchLeg();

			returnLeg.setDepartureLocation(departureLeg.getArrivalLocation());
			returnLeg.setArrivalLocation(departureLeg.getDepartureLocation());

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
			JSONUtils.putJSONableList(obj, "children", mChildren);
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
		mChildren = JSONUtils.getJSONableList(obj, "children", ChildTraveler.class);
		mQueryLegs = JSONUtils.getJSONableList(obj, "queryLegs", FlightSearchLeg.class);
		return true;
	}

}
