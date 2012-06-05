package com.expedia.bookings.data;

public class FlightSearch {

	private FlightSearchParams mSearchParams = new FlightSearchParams();
	private FlightSearchResponse mSearchResponse;
	private FlightLeg[] mSelectedLegs;

	public void reset() {
		mSearchParams.reset();
		mSearchResponse = null;
		mSelectedLegs = null;
	}

	public FlightSearchParams getSearchParams() {
		return mSearchParams;
	}

	public void setSearchResponse(FlightSearchResponse searchResponse) {
		mSearchResponse = searchResponse;
	}

	public FlightSearchResponse getSearchResponse() {
		return mSearchResponse;
	}

	public FlightLeg[] getSelectedLegs() {
		if (mSelectedLegs == null || mSelectedLegs.length != mSearchParams.getQueryLegCount()) {
			mSelectedLegs = new FlightLeg[mSearchParams.getQueryLegCount()];
		}

		return mSelectedLegs;
	}

	public void setSelectedLeg(int position, FlightLeg leg) {
		getSelectedLegs()[position] = leg;
	}
}
